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
import org.ist.p2pbay.exception.P2PBayException;
import org.ist.p2pbay.util.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class handles all the bid related inforamtion
 */
public class BidManager {

    private Peer peer;
    public static final Log log = LogFactory.getLog(BidManager.class);


    public BidManager(Peer peer) {
        this.peer = peer;
    }

    public boolean addBid(String itemName, BidInfo bid) throws P2PBayException {
        boolean isSuccess = false;
        try {
            FutureDHT futureDHT = peer.add(Number160.createHash(Constants.BID_DOMAIN + itemName)).setData((new Data(bid))).
                    setDomainKey(Number160.createHash(Constants.BID_DOMAIN)).
                    start();
            futureDHT.awaitUninterruptibly();
            isSuccess = futureDHT.isSuccess();

        } catch (Exception ex) {
            throw new P2PBayException("Exception thrown while accessing object");
        }
        return isSuccess;
    }

    public BidInfo getHighestBid(String item) throws P2PBayException {

        BidInfo highestBid = new BidInfo("none", 0.0);
        try {
            FutureDHT futureDHT = peer.get(Number160.createHash(Constants.BID_DOMAIN + item)).setAll().
                    setDomainKey((Number160.createHash(Constants.BID_DOMAIN))).
                    start();
            futureDHT.awaitUninterruptibly();

            Iterator<Data> dataItr = futureDHT.getDataMap().values().iterator();

            while (dataItr.hasNext()) {
                BidInfo currentBid = (BidInfo) dataItr.next().getObject();
                if (highestBid.getAmount() < currentBid.getAmount())
                    highestBid = currentBid;
            }

        } catch (Exception ex) {
            throw new P2PBayException("Exception thrown while retrieving object");
        }

        return highestBid;
    }

    public int getBidCount(String item) {
        try {
            FutureDHT futureDHT = peer.get(Number160.createHash(Constants.BID_DOMAIN + item)).setAll().
                    setDomainKey((Number160.createHash(Constants.BID_DOMAIN))).
                    start();
            futureDHT.awaitUninterruptibly();
            if (futureDHT.getDataMap() != null) {
                return futureDHT.getDataMap().values().size();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception thrown while retrieving object");
        }
        return 0;
    }

    public List<BidInfo> getBidList(String itemName) {
        ArrayList<BidInfo> bidList = new ArrayList<BidInfo>();
        try {
            FutureDHT futureDHT = peer.get(Number160.createHash(Constants.BID_DOMAIN + itemName)).setAll().
                    setDomainKey((Number160.createHash(Constants.BID_DOMAIN))).
                    start();
            futureDHT.awaitUninterruptibly();
            if (futureDHT.getDataMap() != null) {
                Iterator<Data> dataItr = futureDHT.getDataMap().values().iterator();
                while (dataItr.hasNext()) {
                    BidInfo currentBid = (BidInfo) dataItr.next().getObject();
                    bidList.add(currentBid);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception thrown while retrieving object");
        }
        return bidList;
    }


}
