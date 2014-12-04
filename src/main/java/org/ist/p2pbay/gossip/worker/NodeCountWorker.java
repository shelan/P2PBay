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

package org.ist.p2pbay.gossip.worker;

import net.tomp2p.futures.FutureResponse;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.PeerAddress;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.p2pbay.gossip.GossipManager;
import org.ist.p2pbay.gossip.GossipObject;
import org.ist.p2pbay.gossip.message.NodeCountMessage;
import org.ist.p2pbay.gossip.repository.InformationRepository;
import org.ist.p2pbay.util.Constants;

import java.util.List;
import java.util.Random;

/**
 * Created by shelan on 11/27/14.
 */
public class NodeCountWorker extends Thread {

    private Peer peer;
    private InformationRepository infoRepo;
    public static final Log log = LogFactory.getLog(NodeCountWorker.class);
    private boolean isStop = false;
    private GossipManager manager;

    public NodeCountWorker(Peer peer, InformationRepository infoRepo, GossipManager manager) {
        this.peer = peer;
        this.infoRepo = infoRepo;
        this.manager = manager;

    }

    @Override
    public void run() {
        {
            //delay starting the gossip
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (!isInterrupted()) {
                try {
                    Thread.sleep(Constants.GOSSIP_FREQUENCY_IN_MS);
                } catch (InterruptedException e) {
                    return;
                }

                List<PeerAddress> peerAddressList = peer.getPeerBean().getPeerMap().getAll();
                if (peerAddressList.size() > 0) {
                    PeerAddress address = peerAddressList.get(new Random().nextInt(peerAddressList.size()));
                    //System.out.println("Sending request from " + peer);

                    GossipObject dataHolder = infoRepo.getinfoHolder();

                    if (log.isDebugEnabled())
                        log.debug(" @ sender current count: " + dataHolder.getCount() + "current weight :" +
                                dataHolder.getWeight() + "node count --> " +
                                dataHolder.getCount() / dataHolder.getWeight());

                    NodeCountMessage message = new NodeCountMessage(infoRepo.sliceGossipObject(),
                            manager.getGossipRound().get());

                    FutureResponse futureResponse = peer.sendDirect(address).setObject(message).start();
                    futureResponse.awaitUninterruptibly();

                    //rollback message if not successful
                    if (!"OK".equals(futureResponse.getResponse().getType().name())) {
                        infoRepo.mergeGossipObject(message.getGossipObject());
                    }

                }
            }
        }
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean isStop) {
        this.isStop = isStop;
    }
}
