package org.ist.p2pbay.gossip.worker;

import org.ist.p2pbay.gossip.GossipManager;

import java.util.Random;

public class GossipRescheduler extends Thread {
    GossipManager gossipManager;
    private boolean isStop = false;

    public GossipRescheduler(GossipManager gossipManager) {
        this.gossipManager = gossipManager;
    }

    @Override
    public void run() {

        while (!isInterrupted()) {
            try {
                int random = new Random().nextInt(3);
                Thread.sleep(60000*(random+1));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

           // System.out.println("resetting gossip");
            gossipManager.resetGossip();

        }
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean isStop) {
        this.isStop = isStop;
    }
}
