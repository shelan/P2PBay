
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
                Thread.sleep(60000*(random+10));
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
