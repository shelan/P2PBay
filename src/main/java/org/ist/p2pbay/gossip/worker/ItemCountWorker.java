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
import org.ist.p2pbay.gossip.message.ItemCountMessage;
import org.ist.p2pbay.gossip.repository.InformationRepository;
import org.ist.p2pbay.util.Constants;

import java.util.List;
import java.util.Random;

public class ItemCountWorker extends Thread {

    private Peer peer;
    private InformationRepository itemInfoRepo;
    public static final Log log = LogFactory.getLog(ItemCountWorker.class);
    private GossipManager manager;

    public ItemCountWorker(Peer peer, InformationRepository itemInfoRepo, GossipManager manager) {
        this.itemInfoRepo = itemInfoRepo;
        this.peer = peer;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
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

                GossipObject dataHolder = itemInfoRepo.getinfoHolder();
                if (log.isDebugEnabled())
                    log.debug(" @ sender current count: " + dataHolder.getCount() + "current weight :" +
                            dataHolder.getWeight() + " Item count --> " +
                            dataHolder.getCount() / dataHolder.getWeight());

                ItemCountMessage message = new ItemCountMessage(itemInfoRepo.sliceGossipObject(),
                        manager.getGossipRound().get());

                FutureResponse futureResponse = peer.sendDirect(address).setObject(message).start();
                futureResponse.awaitUninterruptibly();

                String response ="FAILED";
                if(futureResponse.getResponse() != null)
                    response = futureResponse.getResponse().getType().name();
                if (!"OK".equals(response)) {
                    itemInfoRepo.mergeGossipObject(message.getGossipObject());
                }

            }
        }

    }

}
