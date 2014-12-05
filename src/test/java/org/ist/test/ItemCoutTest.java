
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

package org.ist.test;

import org.ist.p2pbay.exception.P2PBayException;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class ItemCoutTest {

    private int rounds = 10;
    private int incrementSize = 10;
    String baseUserName = "user";
    String basePassword = "password";
    String baseDescription = "Description";
    String host1 = "http://planetlab-um00.di.uminho.pt";
    String host2 = "http://planetlab-tea.ait.ie";
//    String host2 = "http://planetlab-um00.di.uminho.pt";
    /*String host1 = "http://127.0.0.1";
    String host2 = "http://127.0.0.1";*/

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, P2PBayException, InterruptedException {
        ItemCoutTest test = new ItemCoutTest();
        //test.addUsers();
        test.testItemCountConvergeTime();
    }

    private void testItemCountConvergeTime() throws IOException, InterruptedException {
        for (int i = 0; i < rounds; i++) {
            addItemsAndTest(i);
        }
    }

    public void addUsers() throws NoSuchAlgorithmException, IOException, ClassNotFoundException,
            InterruptedException, P2PBayException {

        System.out.println("Adding users");
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        ClientResource resource = new ClientResource("tes");

        for (int i = 0; i < rounds; i++) {
            String userName = baseUserName + i;
            String password = basePassword + i;

            messageDigest.update(password.getBytes("UTF-8"));
            // password to store
            byte[] passwordDigest = messageDigest.digest();
            resource.setReference(host1+ ":4567/node/user/create/"+userName+"/"+password);
            System.out.println(resource.get().getText());

        }

        System.out.println("users added");
    }

    private void addItemsAndTest(int round) throws IOException, InterruptedException {
        System.out.println("adding items......");
        String[] titleParts = {"item","short","two","old","new","other","ayyo","work","tshirt","sell","cloths","cup","slippers","table","skirt"};
        String[] hostNames = {"planetlab1.virtues.fi","planetlab2.ci.pwr.wroc.pl",
                "plab4.ple.silweb.pl",
                "planck227ple.test.ibbt.be",
                "planet-lab-node1.netgroup.uniroma2.it",
                "planet-lab-node2.netgroup.uniroma2.it",
                "planetlab-3.imperial.ac.uk",
                "planetlab-4.imperial.ac.uk",
                "planetlab-node3.it-sudparis.eu","planetlab-tea.ait.ie","planetlab-um00.di.uminho.pt"};
        Random random = new Random();
        ClientResource resource = new ClientResource("test");
        for (int i = 0; i < incrementSize; i++) {
            String itemTitle = titleParts[random.nextInt(14)] + "-" + titleParts[random.nextInt(14)];
            host1 = "http://"+hostNames[random.nextInt(10)];
            resource.setReference(host1 + ":4567/node/item/create/" +
                    itemTitle + "/" + baseDescription + i + "/" + baseUserName + i);
            resource.get().getText();
        }

        System.out.println("checking count.....");
        ClientResource resource2 = new ClientResource("http://"+hostNames[random.nextInt(10)]+":4567/node/item-count");
        int gossipItemCount=0;
        long t1 = System.currentTimeMillis();

        while (gossipItemCount != incrementSize*(round+1)) {
            String count = resource2.get().getText();
            gossipItemCount = Integer.valueOf(count);

        }
        System.out.println(System.currentTimeMillis() - t1);
    }
}
