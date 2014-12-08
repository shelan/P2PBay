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

import java.util.ArrayList;
import java.util.List;

public class NodeRepository {

    ArrayList<P2PBayApp> networkedAppList = new ArrayList<P2PBayApp>();
    public static final Log log = LogFactory.getLog(NodeRepository.class);


    public ArrayList<P2PBayApp> getNetworkedAppList() {
        return networkedAppList;
    }

    public ArrayList<P2PBayApp> createAppNetWork(int numberOfApps) throws Exception {
        for (int i =0 ; i< numberOfApps ; i++) {
            P2PBayApp app = new P2PBayApp();
            app.bootstrap ("127.0.0.1",String.valueOf(4003),String.valueOf(4003+i));
            networkedAppList.add(app);
        }
        return networkedAppList;
    }

    public void shutDownAppNetwork(List<P2PBayApp> networkedAppList){
        for (P2PBayApp p2PBayApp : networkedAppList) {
            log.info("shutting down "+p2PBayApp.getPeer().getPeerAddress().getID());
            p2PBayApp.shutDown();
        }
    }


}
