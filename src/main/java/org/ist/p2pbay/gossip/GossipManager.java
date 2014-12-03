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

import net.tomp2p.futures.FutureResponse;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.ist.p2pbay.gossip.handler.ItemCountHandler;
import org.ist.p2pbay.gossip.handler.NodeCountHandler;
import org.ist.p2pbay.gossip.handler.UserCountHandler;
import org.ist.p2pbay.gossip.message.ItemCountMessage;
import org.ist.p2pbay.gossip.message.Message;
import org.ist.p2pbay.gossip.message.NodeCountMessage;
import org.ist.p2pbay.gossip.message.UserCountMessage;
import org.ist.p2pbay.gossip.repository.InformationRepository;
import org.ist.p2pbay.gossip.worker.ItemCountWorker;
import org.ist.p2pbay.gossip.worker.NodeCountWorker;
import org.ist.p2pbay.gossip.worker.UserCountWorker;
import org.ist.p2pbay.util.Constants;

import java.util.List;
import java.util.Random;


public class GossipManager {
    private Peer peer;
    private NodeCountHandler nodeCountHandler;
    private UserCountHandler userCountHandler;
    private ItemCountHandler itemCountHandler;


    private InformationRepository nodeInfoRepo;
    private InformationRepository userInfoRepo;
    private InformationRepository itemInfoRepo;

    NodeCountWorker nodeCountWorker;
    UserCountWorker userCountWorker;
    ItemCountWorker itemCountWorker;

    public InformationRepository getNodeInfoRepo() {
        return nodeInfoRepo;
    }

    public InformationRepository getUserInfoRepo() {
        return userInfoRepo;
    }


    public GossipManager(Peer peer) {
        this.peer = peer;

        this.nodeInfoRepo = new InformationRepository();
        this.userInfoRepo = new InformationRepository();
        this.itemInfoRepo = new InformationRepository();

        initInfoRepos();
        setupReplyHandlers();
        this.nodeCountHandler = new NodeCountHandler(peer, nodeInfoRepo);
        this.userCountHandler = new UserCountHandler(peer, userInfoRepo);
        this.itemCountHandler = new ItemCountHandler(peer, itemInfoRepo);

    }

    private void initInfoRepos() {
        //this is the inital node since there are no peers for it.
        //for first node in the ring gossip will have 1.0 weight
        if (peer.getPeerBean().getPeerMap().size() == 0) {
            nodeInfoRepo.setinfoHolder(new GossipObject(1.0, 1.0));
            userInfoRepo.setinfoHolder(new GossipObject(1.0, 0.0));
            itemInfoRepo.setinfoHolder(new GossipObject(1.0, 0.0));
            //for user info we do not have initial data
        } else {
            nodeInfoRepo.setinfoHolder(new GossipObject(0.0, 1.0));
            userInfoRepo.setinfoHolder(new GossipObject(0.0, 0.0));
            itemInfoRepo.setinfoHolder(new GossipObject(0.0, 0.0));
        }
    }

    public void runGossip() {

        nodeCountWorker = new NodeCountWorker(peer, nodeInfoRepo);
        userCountWorker = new UserCountWorker(peer, userInfoRepo);
        itemCountWorker = new ItemCountWorker(peer, itemInfoRepo);
        nodeCountWorker.start();
        userCountWorker.start();
        itemCountWorker.start();

    }

    public void stopGossip() {
       // handoverNodeCountInfo();
        nodeCountWorker.setStop(true);
    }

    private void handoverNodeCountInfo() {
        List<PeerAddress> peerAddressList = peer.getPeerBean().getPeerMap().getAll();
        if (peerAddressList.size() > 0) {
            PeerAddress address1 = peerAddressList.get(new Random().nextInt(peerAddressList.size()));
            NodeCountMessage nodeCountMessage = new NodeCountMessage(nodeInfoRepo.getInfoToHandover(Constants.HANDOVER_TYPE_NODE));
            FutureResponse futureResponse1 = peer.sendDirect(address1).setObject(nodeCountMessage).start();
            futureResponse1.awaitUninterruptibly();
        }
    }

    private void setupReplyHandlers() {
        peer.setObjectDataReply(new ObjectDataReply() {
            @Override
            public Object reply(PeerAddress sender, Object incomingMsg) throws Exception {
                Message message = (Message) incomingMsg;
                switch (message.getMSGType()) {
                    case NODE_COUNT:
                        nodeCountHandler.handleMessage((NodeCountMessage) message);
                        break;
                    case USER_COUNT:
                        userCountHandler.handleMessage((UserCountMessage) message);
                        break;
                    case ITEM_COUNT:
                        itemCountHandler.handleMessage((ItemCountMessage) message);
                        break;
                }

                return true;
            }
        });


    }


    public InformationRepository getItemInfoRepo() {
        return itemInfoRepo;
    }
}
