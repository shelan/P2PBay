
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

package org.ist;

import org.ist.p2pbay.data.BidInfo;
import org.ist.p2pbay.exception.P2PBayException;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class TestRunner {
    public static void main(String[] args) throws Exception {

        final ArrayList<P2PBayApp> apps = new ArrayList<P2PBayApp>();

        int numOfNodes = 4;
        //Semaphore available = new Semaphore(4, true);
        final AtomicInteger atomicInteger = new AtomicInteger(numOfNodes);
        for (int i =0 ; i< numOfNodes ; i++) {
            P2PBayApp app = new P2PBayApp();
            if(i==0)
            app.bootstrap ("127.0.0.1",String.valueOf(4000),String.valueOf(4000+i));
            apps.add(app);
        }

        apps.get(0).getBidManager().addBid("item0", new BidInfo("user0",1.0));

        for (int i = 0; i <apps.size(); i++) {
            final int finalI = i;
            new Thread(){
                @Override
                public void run() {
                    try{
                    for (int j = 0; j < 10 ; j++) {
                        //     System.out.println("Running "+finalI +" loop "+j);
                        BidInfo bidInfo = apps.get(finalI).getBidManager().getHighestBid("item0");
                        Double newBid = bidInfo.getAmount() + 1.0 + 1.0*finalI;
                        bidInfo.setAmount(newBid);
                        bidInfo.setUserId("user"+finalI);
                        apps.get(finalI).getBidManager().addBid("item0", bidInfo);
                        System.out.println("adding " + newBid + "by " + finalI + " loop " + j);
                        Thread.sleep(1000);
                    }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (P2PBayException e) {
                        e.printStackTrace();
                    } finally {
                        atomicInteger.getAndDecrement();
                    }


                }
            }.start();
        }

        while (atomicInteger.get() != 0){
    //      System.out.println("atomic integer "+atomicInteger);
        }

       BidInfo bidInfo = apps.get(0).getBidManager().getHighestBid("item0");
        System.out.println("Highest bid of "+bidInfo.getAmount() + "by "+bidInfo.getUserId());
        System.out.println("Bid count " + apps.get(0).getBidManager().getBidCount("item0"));
        System.exit(0);

    }




}
