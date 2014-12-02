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
import org.ist.p2pbay.gossip.GossipObject;
import org.ist.p2pbay.gossip.message.UserCountMessage;
import org.ist.p2pbay.gossip.repository.InformationRepository;

import java.util.List;
import java.util.Random;

public class UserCountWorker extends Thread {

    private boolean stop;
    private Peer peer;
    private InformationRepository userInfoRepo;
    public static final Log log = LogFactory.getLog(UserCountWorker.class);

    public UserCountWorker( Peer peer, InformationRepository userInfoRepo) {
        this.userInfoRepo = userInfoRepo;
        this.peer = peer;
        this.stop = false;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!stop) {
            List<PeerAddress> peerAddressList = peer.getPeerBean().getPeerMap().getAll();
            if (peerAddressList.size() > 0) {
                PeerAddress address = peerAddressList.get(new Random().nextInt(peerAddressList.size()));
                //System.out.println("Sending request from " + peer);

                GossipObject dataHolder = userInfoRepo.getinfoHolder();

                if(log.isDebugEnabled())
                log.debug(" @ sender current count: " + dataHolder.getCount() + "current weight :"+
                            dataHolder.getWeight() + " user count --> "+
                            dataHolder.getCount()/ dataHolder.getWeight());

                UserCountMessage message = new UserCountMessage(userInfoRepo.sliceGossipObject());

                FutureResponse futureResponse = peer.sendDirect(address).setObject(message).start();
                futureResponse.awaitUninterruptibly();

                if(!"OK".equals(futureResponse.getResponse().getType().name())) {
                    userInfoRepo.mergeGossipObject(message.getGossipObject());
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    log.error("interrupted ",e);
                }

            }
        }

    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
