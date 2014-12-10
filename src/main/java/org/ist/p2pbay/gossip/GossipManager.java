/*
 * Copyright 2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ist.p2pbay.gossip;

import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.ist.p2pbay.gossip.handler.ItemCountHandler;
import org.ist.p2pbay.gossip.handler.NodeCountHandler;
import org.ist.p2pbay.gossip.handler.UserCountHandler;
import org.ist.p2pbay.gossip.message.*;
import org.ist.p2pbay.gossip.repository.InformationRepository;
import org.ist.p2pbay.gossip.worker.GossipRescheduler;
import org.ist.p2pbay.gossip.worker.ItemCountWorker;
import org.ist.p2pbay.gossip.worker.NodeCountWorker;
import org.ist.p2pbay.gossip.worker.UserCountWorker;
import org.ist.p2pbay.util.Constants;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class GossipManager {
    private Peer peer;
    private NodeCountHandler nodeCountHandler;
    private UserCountHandler userCountHandler;
    private ItemCountHandler itemCountHandler;
    private AtomicBoolean isGossipStopped = new AtomicBoolean(false);


    private InformationRepository nodeInfoRepo;
    private InformationRepository userInfoRepo;
    private InformationRepository itemInfoRepo;

    NodeCountWorker nodeCountWorker;
    UserCountWorker userCountWorker;
    ItemCountWorker itemCountWorker;

    GossipRescheduler rescheduler;

    private AtomicInteger gossipRound = new AtomicInteger(0);

    public InformationRepository getNodeInfoRepo() {
        return nodeInfoRepo;
    }

    public InformationRepository getUserInfoRepo() {
        return userInfoRepo;
    }


    public GossipManager(Peer peer) {
        this.peer = peer;
        // add initial values to repos
        initInfoRepos();
        // setup handlers for incoming messages
        setupReplyHandlers();

        this.nodeCountHandler = new NodeCountHandler(peer, nodeInfoRepo, this);
        this.userCountHandler = new UserCountHandler(peer, userInfoRepo, this);
        this.itemCountHandler = new ItemCountHandler(peer, itemInfoRepo, this);

    }

    private void initInfoRepos() {
        //this is the inital node since there are no peers for it.
        //for first node in the ring gossip will have 1.0 weight
        if (peer.getPeerBean().getPeerMap().size() == 0) {
            this.nodeInfoRepo = new InformationRepository(new GossipObject(1.0, 1.0));
            this.userInfoRepo = new InformationRepository(new GossipObject(1.0, 0.0));
            this.itemInfoRepo = new InformationRepository(new GossipObject(1.0, 0.0));

            //for user info we do not have initial data
        } else {
            this.nodeInfoRepo = new InformationRepository(new GossipObject(0.0, 1.0));
            this.userInfoRepo = new InformationRepository(new GossipObject(0.0, 0.0));
            this.itemInfoRepo = new InformationRepository(new GossipObject(0.0, 0.0));
        }
    }

    public void runGossip() {
        rescheduler = new GossipRescheduler(this);
        nodeCountWorker = new NodeCountWorker(peer, nodeInfoRepo, this);
        userCountWorker = new UserCountWorker(peer, userInfoRepo, this);
        itemCountWorker = new ItemCountWorker(peer, itemInfoRepo, this);

        nodeCountWorker.start();
        userCountWorker.start();
        itemCountWorker.start();

        rescheduler.start();

        isGossipStopped.set(false);

    }

    public void stopGossip() {
        nodeCountWorker.interrupt();
        userCountWorker.interrupt();
        itemCountWorker.interrupt();
        isGossipStopped.set(true);
    }

    public void resumeGossip() {

        nodeCountWorker = new NodeCountWorker(peer, nodeInfoRepo, this);
        userCountWorker = new UserCountWorker(peer, userInfoRepo, this);
        itemCountWorker = new ItemCountWorker(peer, itemInfoRepo, this);

        nodeCountWorker.start();
        userCountWorker.start();
        itemCountWorker.start();

        isGossipStopped.set(false);

    }


    public void handoverGossipInfo() {
        List<PeerAddress> peerAddressList = peer.getPeerBean().getPeerMap().getAll();
        String response = "FAILED";
        int count = 0;

        while (!"OK".equals(response)) {
            if (count == 10)
                break;

            if (peerAddressList.size() > 0) {
                PeerAddress address1 = peerAddressList.get(new Random().nextInt(peerAddressList.size()));
                HandoverDetailsMessage handoverDetailsMessage = new HandoverDetailsMessage(
                        userInfoRepo.getTotalCount().get(), itemInfoRepo.getTotalCount().get(), nodeInfoRepo.getinfoHolder().
                        getCount() - 1, nodeInfoRepo.getinfoHolder().getWeight(), nodeInfoRepo.getInitialWeight());
                FutureResponse futureResponse = peer.sendDirect(address1).setObject(handoverDetailsMessage).start();
                futureResponse.awaitUninterruptibly();
                if (futureResponse != null && futureResponse.getResponse() != null) {
                    response = futureResponse.getResponse().getType().name();
                }

            }
            count++;
        }
    }

    public void resetGossip() {
        stopGossip();
        //wait twice as gossip frequency
        try {
            Thread.sleep(3 * Constants.GOSSIP_FREQUENCY_IN_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //so incoming messages will not be accepted after this ..!!!
        gossipRound.getAndIncrement();

        //we are resetting gossip object to new state
        nodeInfoRepo.resetGossipObject();
        itemInfoRepo.resetGossipObject();
        userInfoRepo.resetGossipObject();
        //we are ready to be back into business.
        resumeGossip();
    }

    public void adjustGossip(int recievedGossipVal) {
        stopGossip();
        //wait thrice as gossip frequency
        try {
            Thread.sleep(3 * Constants.GOSSIP_FREQUENCY_IN_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //we are resetting gossip object to new state
        nodeInfoRepo.resetGossipObject();
        itemInfoRepo.resetGossipObject();
        userInfoRepo.resetGossipObject();

        gossipRound.set(recievedGossipVal);
        //we are ready to be back into business.
        resumeGossip();
    }


    private void setupReplyHandlers() {
        peer.setObjectDataReply(new ObjectDataReply() {
            @Override
            public Object reply(PeerAddress sender, Object incomingMsg) throws Exception {
                if (isGossipStopped.get()) {
                    return null;
                }
                Message message = (Message) incomingMsg;
                switch (message.getMSGType()) {
                    case NODE_COUNT:
                        return nodeCountHandler.handleMessage((NodeCountMessage) message);
                    case USER_COUNT:
                        return userCountHandler.handleMessage((UserCountMessage) message);
                    case ITEM_COUNT:
                        return itemCountHandler.handleMessage((ItemCountMessage) message);
                    case HANDOVER:
                        return acceptHandoverDetails((HandoverDetailsMessage) message);

                }
                return null;
            }
        });


    }

    private BaseFuture.FutureType acceptHandoverDetails(HandoverDetailsMessage handoverDetailsMessage) {
        nodeInfoRepo.mergeHandoverCounts(0.0, handoverDetailsMessage.getWeight());
        itemInfoRepo.mergeHandoverCounts(handoverDetailsMessage.getItemCount(), handoverDetailsMessage.getWeight());
        userInfoRepo.mergeHandoverCounts(handoverDetailsMessage.getUserCount(), handoverDetailsMessage.getWeight());

        nodeInfoRepo.mergeGossipObject(new GossipObject(handoverDetailsMessage.getGossipWeight(),
                handoverDetailsMessage.getGossipNodeCount()));
        return FutureResponse.FutureType.OK;
    }

    public InformationRepository getItemInfoRepo() {
        return itemInfoRepo;
    }

    public AtomicInteger getGossipRound() {
        return gossipRound;
    }
}
