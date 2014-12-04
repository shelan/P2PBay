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

package org.ist.p2pbay.gossip.handler;


import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.FutureResponse;
import net.tomp2p.p2p.Peer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.p2pbay.gossip.GossipManager;
import org.ist.p2pbay.gossip.GossipObject;
import org.ist.p2pbay.gossip.message.UserCountMessage;
import org.ist.p2pbay.gossip.repository.InformationRepository;

public class UserCountHandler {

    private GossipManager gossipManager;
    private InformationRepository userInfoRepo;
    private Peer peer;
    public static final Log log = LogFactory.getLog(UserCountHandler.class);


    public UserCountHandler(Peer peer, InformationRepository userInfoRepo, GossipManager gossipManager) {
        this.gossipManager = gossipManager;
        this.peer = peer;
        this.userInfoRepo = userInfoRepo;
    }

    public BaseFuture.FutureType handleMessage(UserCountMessage userCountMessage) {

        if (userCountMessage.getGossipRound() == gossipManager.getGossipRound().get()) {
            GossipObject dataHolder = userInfoRepo.getinfoHolder();
            if (log.isDebugEnabled())
                log.debug(" @ reciever current count: " + dataHolder.getCount() + "current weight :" +
                        dataHolder.getWeight() + "user count --> " +
                        dataHolder.getCount() / dataHolder.getWeight());
            userInfoRepo.mergeGossipObject(userCountMessage.getGossipObject());
            return FutureResponse.FutureType.OK;
        }
    else if (userCountMessage.getGossipRound() > gossipManager.getGossipRound().get()) {
        gossipManager.adjustGossip(userCountMessage.getGossipRound());
        return null;
    } else {
        return null;
    }

    }
}
