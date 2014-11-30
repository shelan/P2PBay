package org.ist;

import org.ist.p2pbay.data.BidInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by shelan on 11/26/14.
 */
public class TestRunner {
    public static void main(String[] args) throws Exception {

        final ArrayList<P2PBayApp> apps = new ArrayList<P2PBayApp>();

        int numOfNodes = 4;
        //Semaphore available = new Semaphore(4, true);
        final AtomicInteger atomicInteger = new AtomicInteger(numOfNodes);
        for (int i =0 ; i< numOfNodes ; i++) {
            P2PBayApp app = new P2PBayApp();
            if(i==0)
            app.bootstrap (String.valueOf(i),"127.0.0.1",String.valueOf(4000),String.valueOf(4000+i));
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
                        } catch (IOException e) {
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
