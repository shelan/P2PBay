
/*
 *
 *  * Copyright 2014
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.ist.p2pbay.info;

import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.p2pbay.gossip.GossipManager;
import org.ist.p2pbay.gossip.GossipObject;

public class StatPublisher extends Thread {

    Peer peer;
    GossipManager gossipManager;
    private static Log log = LogFactory.getLog(StatPublisher.class);
    boolean shouldRun = false;

    public StatPublisher(Peer peer, GossipManager gossipManager, boolean shouldRun) {
        this.peer = peer;
        this.gossipManager = gossipManager;
        this.shouldRun = shouldRun;

        if(this.shouldRun) {
            peer.getPeerBean().getPeerMap().addPeerMapChangeListener(new PeerMapChangeListener() {
                @Override
                public void peerInserted(PeerAddress peerAddress) {
                    log.info("peer added " + peerAddress.toString());
                }

                @Override
                public void peerRemoved(PeerAddress peerAddress) {
                    log.info("peer removed " + peerAddress.toString());
                }

                @Override
                public void peerUpdated(PeerAddress peerAddress) {
                    log.info("peer updated " + peerAddress.toString());
                }

            });
        }
    }

    @Override
    public void run() {
        while (shouldRun) {
            try {
                Thread.sleep(3000);
                GossipObject itemInfo = gossipManager.getItemInfoRepo().getinfoHolder();
                GossipObject nodeInfo = gossipManager.getNodeInfoRepo().getinfoHolder();
                GossipObject userInfo = gossipManager.getUserInfoRepo().getinfoHolder();

                log.info("peers in peer map -----> "+peer.getPeerBean().getPeerMap().size());
                log.info(("node count " + nodeInfo.getCount() + "/ weight " + nodeInfo.getWeight() + " = " + nodeInfo.getCount() / nodeInfo.getWeight()));
                log.info(("item count " + itemInfo.getCount() + "/ weight " + itemInfo.getWeight() + " = " + itemInfo.getCount() / itemInfo.getWeight()));
                log.info(("user count " + userInfo.getCount() + "/ weight " + userInfo.getWeight() + " = " + userInfo.getCount() / userInfo.getWeight()));

               /* System.out.println(("Node count : ---------> " +nodeInfo.getCount()/nodeInfo.getWeight()));
                System.out.println("User count : ---------> " + userInfo.getCount() / userInfo.getWeight());
                System.out.println("Item count : ---------> " + itemInfo.getCount() / itemInfo.getWeight());
                System.out.println("\n");*/


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
