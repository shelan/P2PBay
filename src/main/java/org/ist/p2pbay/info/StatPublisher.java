package org.ist.p2pbay.info;

import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.p2pbay.gossip.GossipManager;
import org.ist.p2pbay.gossip.GossipObject;

/**
 * Created by shelan on 11/25/14.
 */
public class StatPublisher extends Thread{

    Peer peer;
    GossipManager gossipManager;
    private static Log log = LogFactory.getLog(StatPublisher.class);

    public StatPublisher(Peer peer, GossipManager gossipManager){
        this.peer = peer;
        this.gossipManager = gossipManager;

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

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(3000);
              GossipObject itemInfo = gossipManager.getItemInfoRepo().getinfoHolder();
              GossipObject nodeInfo = gossipManager.getNodeInfoRepo().getinfoHolder();
              GossipObject userInfo = gossipManager.getUserInfoRepo().getinfoHolder();

                System.out.println(("Node count : ---------> " +nodeInfo.getCount()/nodeInfo.getWeight()));
                System.out.println("User count : ---------> " + userInfo.getCount() / userInfo.getWeight());
                System.out.println("Item count : ---------> " + itemInfo.getCount() / itemInfo.getWeight());
                System.out.println("\n");


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
