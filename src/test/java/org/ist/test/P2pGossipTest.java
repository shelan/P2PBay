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

package org.ist.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.P2PBayApp;
import org.ist.p2pbay.data.Item;
import org.ist.p2pbay.data.User;
import org.ist.p2pbay.exception.P2PBayException;
import org.ist.p2pbay.gossip.GossipObject;
import org.ist.p2pbay.manager.SalesManager;
import org.ist.p2pbay.manager.UserManager;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

@Test
public class P2pGossipTest{

    NodeRepository repository = new NodeRepository();
    List<P2PBayApp> appList;
    public static final Log log = LogFactory.getLog(P2pGossipTest.class);
    int numberOfApps = 5;
    int numberOfIterations = 100;
    String baseUserName = "user";
    String basePassword = "TestPassword123#";
    String baseItemName = "item";


    @BeforeClass()
    public void init() {
        try {
            appList = repository.createAppNetWork(numberOfApps);
            Thread.sleep(3000);
        } catch (Exception e) {
            log.error("error while initializing", e);
        }
    }


    @Test(groups = {"gossiptest"})
    public void testNodeCountGossip() {
        GossipObject gossipObject = appList.get(0).getGossipManager().getNodeInfoRepo().getinfoHolder();
        //System.out.printf(String.valueOf(gossipObject.getCount()));
        System.out.println();
        Assert.assertEquals((int) Math.round(gossipObject.getCount() / gossipObject.getWeight()), 5);
        //assertEquals();
    }

    @Test(groups = {"gossiptest"})
    public void testUserAdding() throws InterruptedException, P2PBayException, NoSuchAlgorithmException,
            UnsupportedEncodingException {

        System.out.println("Adding users");
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        for (int i = 0; i < numberOfIterations; i++) {
            String userName = baseUserName + i;
            String password = basePassword + i;

            UserManager userManager = appList.get(new Random().nextInt(numberOfApps)).getUserManager();
            messageDigest.update(password.getBytes("UTF-8"));
            // password to store
            byte[] passwordDigest = messageDigest.digest();
            userManager.addUser(userName, new User(userName, passwordDigest));
        }
        Thread.sleep(5000);
        GossipObject info = appList.get(new Random().nextInt(numberOfApps)).
                getGossipManager().getUserInfoRepo().getinfoHolder();
        System.out.println("user count " + info.getCount() / info.getWeight());
        Assert.assertEquals((int) Math.round(info.getCount() / info.getWeight()), 100);


    }

    @Test(groups = {"gossiptest"}, dependsOnMethods = {"testUserAdding"})
    public void testUserLogin() throws P2PBayException {
        log.info("Starting user tests");
        for (int i = 0; i < numberOfIterations; i++) {
            UserManager userManager = appList.get(new Random().nextInt(numberOfApps)).getUserManager();
            boolean isLoggedIn = userManager.login(baseUserName + i, basePassword + i);
            Assert.assertEquals(isLoggedIn, true);
        }
    }


    @Test(groups = {"gossiptest"}, dependsOnMethods = {"testUserAdding"})
    public void testUserRemove() throws NoSuchAlgorithmException, IOException, ClassNotFoundException, InterruptedException {
        System.out.println("removing 10 users");
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        for (int i = 0; i < 15; i++) {
            String userName = baseUserName + i;
            String password = basePassword + i;

            UserManager userManager = appList.get(new Random().nextInt(numberOfApps)).getUserManager();
            messageDigest.update(password.getBytes("UTF-8"));
            // password to store
            byte[] passwordDigest = messageDigest.digest();
            userManager.removeUser(userName);
        }
        Thread.sleep(2000);
        GossipObject info = appList.get(new Random().nextInt(numberOfApps)).
                getGossipManager().getUserInfoRepo().getinfoHolder();
        System.out.println("user count " + info.getCount() / info.getWeight());
        Assert.assertEquals((int) Math.round(info.getCount() / info.getWeight()), 85);
    }

    @Test(groups = {"gossiptest"})
    public void testItemAdding() throws InterruptedException, P2PBayException {
        for (int i = 0; i < 100; i++) {
            SalesManager salesManager = appList.get(new Random().nextInt(numberOfApps)).getSalesManager();
            salesManager.addItem(baseItemName + i, new Item(baseItemName + i, baseUserName + i));
        }
            Thread.sleep(2000);
            GossipObject info = appList.get(new Random().nextInt(numberOfApps)).
                    getGossipManager().getItemInfoRepo().getinfoHolder();
            System.out.println("Item count " + info.getCount() / info.getWeight());
            Assert.assertEquals((int) Math.round(info.getCount() / info.getWeight()), 100);

    }


    @Test(groups = {"gossiptest"})
    public void testItemRemoving() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            SalesManager salesManager = appList.get(new Random().nextInt(numberOfApps)).getSalesManager();
            salesManager.removeItem(baseItemName + i);
        }
            Thread.sleep(2000);

            GossipObject info = appList.get(new Random().nextInt(numberOfApps)).
                    getGossipManager().getItemInfoRepo().getinfoHolder();
            System.out.println("Item count " + info.getCount() / info.getWeight());
            Assert.assertEquals((int) Math.round(info.getCount() / info.getWeight()), 90);

    }

    @AfterClass
    public void shutdown(){
        repository.shutDownAppNetwork(appList);
    }

}
