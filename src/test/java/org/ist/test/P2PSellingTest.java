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
import org.ist.p2pbay.data.BidInfo;
import org.ist.p2pbay.data.Item;
import org.ist.p2pbay.data.User;
import org.ist.p2pbay.exception.P2PBayException;
import org.ist.p2pbay.manager.BidManager;
import org.ist.p2pbay.manager.SalesManager;
import org.ist.p2pbay.manager.UserManager;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class P2PSellingTest {

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

    @Test
    public void testUserAdding() throws NoSuchAlgorithmException, IOException, ClassNotFoundException,
            InterruptedException, P2PBayException {

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
        Thread.sleep(3000);
    }

    @Test(dependsOnMethods = {"testUserAdding"})
    public void testAddandGetItems() throws P2PBayException {
        for (int i = 0; i < numberOfIterations; i++) {
            SalesManager salesManager = appList.get(new Random().nextInt(numberOfApps)).getSalesManager();
            UserManager userManager = appList.get(new Random().nextInt(numberOfApps)).getUserManager();

            salesManager.addItem(baseItemName + i, new Item(baseItemName + i, baseUserName + i));
            User user = userManager.getUser(baseUserName + i);
            user.addToSellingItems(baseItemName + i);
            userManager.addUser(baseUserName + i, user);
        }

        for (int i = 0; i < numberOfIterations; i++) {
            SalesManager salesManager = appList.get(new Random().nextInt(numberOfApps)).getSalesManager();
            UserManager userManager = appList.get(new Random().nextInt(numberOfApps)).getUserManager();
            Item item = salesManager.getItem(baseItemName + i);
            User user = userManager.getUser(baseUserName + i);

            Assert.assertEquals(item.getSellerId(), baseUserName + i);
            Assert.assertEquals(item.getIsSold(), false);
            Assert.assertEquals(user.getSellingItems().size(), 1);
            Assert.assertEquals(user.getSellingItems().get(0), baseItemName + i);

        }
    }

    @Test(dependsOnMethods = {"testAddandGetItems"})
    public void testBidding() throws IOException, InterruptedException, P2PBayException {
        for (int i = 0; i < numberOfIterations; i++) {
            BidManager bidManager = appList.get(new Random().nextInt(numberOfApps)).getBidManager();
            UserManager userManager = appList.get(new Random().nextInt(numberOfApps)).getUserManager();

            double bidAmount = 1.0 + 1.0 * i;
            bidManager.addBid(baseItemName + 1, new BidInfo(baseUserName + i, bidAmount));

            User user = userManager.getUser(baseUserName + i);
            user.addBadeItems(baseItemName + 1, bidAmount);
            userManager.addUser(baseUserName + i, user);
        }


        SalesManager salesManager = appList.get(new Random().nextInt(numberOfApps)).getSalesManager();
        UserManager userManager = appList.get(new Random().nextInt(numberOfApps)).getUserManager();
        BidManager bidManager = appList.get(new Random().nextInt(numberOfApps)).getBidManager();


        Assert.assertEquals(bidManager.getBidCount(baseItemName + 1), 100);
        Vector<Double> amounts = userManager.getUser(baseUserName + 3).getBadeItems().get("item1");
        Assert.assertEquals(amounts.get(0), 1.0 + 1.0 * 3);


        salesManager.closeAuction(baseUserName +1, baseItemName + 1);

        //update information about the transaction.
        BidInfo highestBid = bidManager.getHighestBid(baseItemName + 1);
        User winningUser = userManager.getUser(highestBid.getUserId());
        User seller = userManager.getUser(salesManager.getItem(baseItemName + 1).getSellerId());

        winningUser.addPurchasedItems(baseItemName + 1, highestBid.getAmount());
        winningUser.removeFromBadeItems(baseItemName + 1);

        seller.removeFromSellingItems(baseItemName + 1);

        userManager.addUser(seller.getName(), seller);
        userManager.addUser(winningUser.getName(), winningUser);

        Thread.sleep(2000);

        Assert.assertEquals(salesManager.getItem(baseItemName + 1).getIsSold(), true);
        //ordinary not sold item
        Assert.assertEquals(salesManager.getItem(baseItemName + 6).getIsSold(), false);
        //purchased by
        Assert.assertEquals(userManager.getUser(baseUserName + 99).getPurchasedItems().containsKey("item1"), true);
        //no longer in the bidding list
        Assert.assertEquals(userManager.getUser(baseUserName + 99).getBadeItems().containsKey("item1"), false);
        //no longer selling this item
        Assert.assertEquals(userManager.getUser(baseUserName + 1).getSellingItems().contains("item1"), false);

        // remove item from inventory
        salesManager.removeItem(baseItemName + 1);

    }


    @AfterClass
    public void shutdown() {
        repository.shutDownAppNetwork(appList);
    }

}
