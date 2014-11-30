package org.ist.p2pbay.info;

import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;

/**
 * Created by shelan on 11/25/14.
 */
public class NodeCounter extends Thread{

    Peer peer;



    public NodeCounter(Peer peer){
        this.peer = peer;
        peer.getPeerBean().getPeerMap().addPeerMapChangeListener(new PeerMapChangeListener() {
            @Override
            public void peerInserted(PeerAddress peerAddress) {
                System.out.println("peer added " + peerAddress.toString());
            }

            @Override
            public void peerRemoved(PeerAddress peerAddress) {
                System.out.println("peer removed " + peerAddress.toString());
            }

            @Override
            public void peerUpdated(PeerAddress peerAddress) {
                System.out.println("peer updated " + peerAddress.toString());
            }

        });
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(2000);
                System.out.println("size of peer table : " +peer.getPeerBean().getPeerMap().size());
                System.out.printf("Peers "+peer.getPeerBean().getPeerMap().getAll().size());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
