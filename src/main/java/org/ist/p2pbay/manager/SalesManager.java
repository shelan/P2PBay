
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

package org.ist.p2pbay.manager;

import net.tomp2p.futures.FutureDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.p2pbay.data.BidInfo;
import org.ist.p2pbay.data.Item;
import org.ist.p2pbay.gossip.GossipManager;
import org.ist.p2pbay.gossip.GossipObject;
import org.ist.p2pbay.util.Constants;

import java.io.Console;

public class SalesManager {

    private static Log log = LogFactory.getLog(SalesManager.class);
    private Peer peer;
    private static Console console = System.console();
    private BidManager bidManager;
    private GossipManager gossipManager;

    public SalesManager(Peer peer, BidManager bidManager, GossipManager gossipManager) {
        this.peer = peer;
        this.bidManager = bidManager;
        this.gossipManager = gossipManager;
    }

    /**
     * @param userId
     * @param title
     * @param description
     * @return
     */
    public Item createNewItemObj(String userId, String title, String description) {
        Item item = new Item(title, userId);
        return item;
    }

    /**
     * @param name
     * @param item
     */
    public void addItem(String name, Item item) {
        try {
            FutureDHT futureDHT = peer.put(Number160.createHash(name)).setData(new Data(item))
                    .setDomainKey(Number160.createHash(Constants.ITEM_DOMAIN))
                    .start();
            futureDHT.awaitUninterruptibly();
            notifyGossipManager(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception thrown while accessing object");
        }
    }

    /**
     * @param title
     * @return
     */
    public Item getItem(String title) {
        try {
            FutureDHT futureDHT = peer.get(Number160.createHash(title)).
                    setDomainKey((Number160.createHash(Constants.ITEM_DOMAIN)))
                    .start();
            futureDHT.awaitUninterruptibly();
            if (futureDHT.isSuccess()) {
                System.out.println("future sucess");
                System.out.println(futureDHT.getData());
                Item item = (Item) futureDHT.getData().getObject();
                return item;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception thrown while retrieving object");
        }
        return null;
    }

    /**
     * @param itemTitle
     * @param userName
     * @param bid
     * @return
     */
    public synchronized boolean bidItem(String itemTitle, String userName, double bid) {
        try {
            BidInfo bidInfo = bidManager.getHighestBid(itemTitle);
            if (bidInfo.getAmount() > bid) {
                log.info("User have been out bade. Current highest bid is " + bidInfo.getAmount());
                return false;
            }
            bidManager.addBid(itemTitle, new BidInfo(userName, bid));
            return true;
        } catch (Exception ex) {
            log.error("Error while updating the bid. " + ex);
            return false;
        }
    }

    /**
     * @param itemTitle
     * @return
     */

    //After closing the auction user object has to be updated with purchased list and for seller sold list
    public BidInfo closeAuction(String itemTitle) {
        try {
            BidInfo bidInfo = bidManager.getHighestBid(itemTitle);
            Item currentItem = getItem(itemTitle);
            currentItem.setIsSold(true);
            addItem(itemTitle, currentItem);
            log.info("Accepted " + bidInfo.getAmount() + "of user " + bidInfo.getUserId() + " for item " + itemTitle);
            return bidInfo;
        } catch (Exception ex) {
            log.error("Error while updating the bid. " + ex);
            return null;
        }
    }

    public void removeItem(String itemName){
        FutureDHT futureDHT = peer.remove(Number160.createHash(itemName)).
                setDomainKey((Number160.createHash(Constants.ITEM_DOMAIN)))
                .start();
        futureDHT.awaitUninterruptibly();
        notifyGossipManager(false);
    }

    private void notifyGossipManager(boolean isAdd){
        if(isAdd) {
            GossipObject gossipObject = new GossipObject();
            gossipObject.setCount(1.0);
            gossipObject.setWeight(0.0);
            gossipManager.getItemInfoRepo().mergeGossipObject(gossipObject);
        }else {
            GossipObject gossipObject = new GossipObject();
            gossipObject.setCount(-1.0);
            gossipObject.setWeight(0.0);
            gossipManager.getItemInfoRepo().mergeGossipObject(gossipObject);
        }
    }

}