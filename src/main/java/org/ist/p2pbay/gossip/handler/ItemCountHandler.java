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


import net.tomp2p.p2p.Peer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.p2pbay.gossip.GossipObject;
import org.ist.p2pbay.gossip.message.ItemCountMessage;
import org.ist.p2pbay.gossip.repository.InformationRepository;

public class ItemCountHandler {

    private InformationRepository itemInfoRepo;
    private Peer peer ;
    public static final Log log = LogFactory.getLog(ItemCountHandler.class);


    public ItemCountHandler(Peer peer, InformationRepository itemInfoRepo) {
        this.peer = peer;
        this.itemInfoRepo = itemInfoRepo;
    }

    public void handleMessage(ItemCountMessage itemCountMessage){
        GossipObject dataHolder = itemInfoRepo.getinfoHolder();
        if(log.isDebugEnabled())
       log.debug(" @ reciever current count: " + dataHolder.getCount() + "current weight :"+
                dataHolder.getWeight() + "Item count --> "+
                dataHolder.getCount()/ dataHolder.getWeight());
        itemInfoRepo.mergeGossipObject(itemCountMessage.getGossipObject());


    }
}
