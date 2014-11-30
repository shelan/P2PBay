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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.util.List;

public class P2PBaseTest {

    NodeRepository repository = new NodeRepository();
    List<P2PBayApp> appList;
    int numberOfApps = 5;
    public static final Log log = LogFactory.getLog(P2pGossipTest.class);

    @BeforeClass()
    public void init() {
        try {
            appList = repository.createAppNetWork(numberOfApps);
            Thread.sleep(3000);
        } catch (Exception e) {
            log.error("error while initializing", e);
        }
    }

    @AfterClass
    public void shutdown(){
        for (P2PBayApp p2PBayApp : appList) {
            log.info("shutting down "+p2PBayApp.getPeer().getPeerAddress().getID());
            p2PBayApp.shutDown();
        }
    }
}
