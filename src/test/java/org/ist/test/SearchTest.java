
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.P2PBayApp;
import org.ist.p2pbay.data.Item;
import org.ist.p2pbay.data.User;
import org.ist.p2pbay.exception.P2PBayException;
import org.ist.p2pbay.manager.SalesManager;
import org.ist.p2pbay.manager.UserManager;
import org.restlet.resource.ClientResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

public class SearchTest {

    NodeRepository repository = new NodeRepository();
    List<P2PBayApp> appList;
    public static final Log log = LogFactory.getLog(P2pGossipTest.class);
    int numberOfApps = 5;
    int numberOfUsers = 10;
    int numberOfItems = 100;
    String baseUserName = "user";
    String basePassword = "userpw";
    String baseItemName = "item";
    String baseDescription = "Description";
    String host = "http://planetlab-um00.di.uminho.pt";
    //String host = "http://127.0.0.1";

    public static void main(String[] args) throws P2PBayException, ClassNotFoundException, NoSuchAlgorithmException, InterruptedException, IOException {
        SearchTest test = new SearchTest();
        //test.init();
         //test.testUserAdding();
        test.testAddandGetItems();
        //test.search();
    }

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

        for (int i = 0; i < 10; i++) {
            String userName = baseUserName + i;
            String password = basePassword + i;

            messageDigest.update(password.getBytes("UTF-8"));
            // password to store
            byte[] passwordDigest = messageDigest.digest();
            /*String urlString = "http://54.174.141.109:4567/node/user/create/"+userName+"/"+password;
            URL url = new URL(urlString);
            System.out.println(url.getPath());*/
            ClientResource resource = new ClientResource(host+ ":4567/node/user/create/"+userName+"/"+password);
            //resource.get().write(System.out);
            System.out.println(resource.get().getText());
//            appList.get(new Random().nextInt(numberOfApps)).registerUser(userName, passwordDigest);
        }
        Thread.sleep(3000);
        System.out.println("users added");
    }

    public void search() throws IOException {
        ClientResource resource = new ClientResource(host+":4567/node/ad-search/&/cup,sell");
        resource.get().write(System.out);
    }
    @Test(dependsOnMethods = {"testUserAdding"})
    public void testAddandGetItems() throws P2PBayException, IOException {

        String[] titleParts = {"item","short","two","old","new","other","ayyo","work","tshirt","sell","cloths","cup","slippers","table","skirt"};
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
                String itemTitle = titleParts[random.nextInt(14)] + "-" + titleParts[random.nextInt(14)];
            ClientResource resource = new ClientResource(host+":4567/node/item/create/"+
            itemTitle+"/"+baseDescription+i+"/"+baseUserName+i);
            resource.get().write(System.out);

        }

        System.out.println("items added...");

    }
}
