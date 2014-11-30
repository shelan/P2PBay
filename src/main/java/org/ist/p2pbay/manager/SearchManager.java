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
import org.ist.p2pbay.util.Constants;
import org.ist.p2pbay.data.Keyword;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ashansa on 11/4/14.
 */

/*
Search is case insensitive
 */
public class SearchManager {

    private Peer peer;


    public SearchManager(Peer peer) {
        this.peer = peer;
    }

    public void addItemToKeyword(String keyword, String itemId) {
        keyword = keyword.toLowerCase();
        if(keyword.contains(" ")) {
            String[] keywordArray = getKeywordsArray(keyword);
            for(String word : keywordArray) {
                addItem(word, itemId);
            }
        } else {
            addItem(keyword, itemId);
        }
    }

    private String[] getKeywordsArray(String keywordString) {
        // TODO avoid common words - the , on, ...
        return keywordString.split(" ");
    }

    private void addItem(String keyword, String itemId) {
        try {
            Keyword existingObject = getKeywordObject(keyword);
            if(existingObject == null) {
                //add a new object for that KEYWORD_DOMAIN
                Keyword newKeyword = new Keyword();
                newKeyword.addItem(itemId);
                peer.put(Number160.createHash(keyword)).setData(new Data(newKeyword)).setDomainKey(Number160.createHash(Constants.KEYWORD_DOMAIN))
                        .start().awaitUninterruptibly();
            } else {

                ////// TODO how to update without put again??
                existingObject.addItem(itemId);
                peer.put(Number160.createHash(keyword)).setData(new Data(existingObject)).setDomainKey(Number160.createHash(Constants.KEYWORD_DOMAIN))
                        .start().awaitUninterruptibly();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception thrown while accessing object");
        }
    }

    public String[] getMatchingItems(String keyword) {
        keyword = keyword.toLowerCase();
        Keyword keywordObj = getKeywordObject(keyword);
        if( keywordObj!= null) {
            return keywordObj.getItems().toArray(new String[keywordObj.getItems().size()]);
        }
        return null;
    }

    public String[] getMatchingItems(String keyword1, String keyword2, Character operation) {
        String[] result1 = getMatchingItems(keyword1);
        String[] result2 = getMatchingItems(keyword2);
        Set resultSet1;
        Set resultSet2;
        Set finalResult = new HashSet();

        if(result1 != null && result2 != null) {
            resultSet1 = new HashSet(Arrays.asList(result1));
            resultSet2 = new HashSet(Arrays.asList(result2));

            switch (operation) {
                case Constants.AND_OPERATION:
                    resultSet1.retainAll(resultSet2);
                    finalResult = resultSet1;
                    break;

                case Constants.OR_OPERATION:
                    resultSet1.addAll(resultSet2);
                    finalResult = resultSet1;
                    break;
            }

            if(finalResult.size() > 0) {
                return (String[])finalResult.toArray(new String[finalResult.size()]);
            } else {
                return null;
            }
        } else if(Constants.AND_OPERATION == operation){
            // we have results only for one KEYWORD_DOMAIN. So & operation won't have any results
            return null;
        } else {
            // only one KEYWORD_DOMAIN has results and operation is |. So send the results retrieved
            return result1 != null ? result1 : result2;
        }
    }

    private Keyword getKeywordObject(String keyword) {
        try {
            FutureDHT futureDHT = peer.get(Number160.createHash(keyword)).
                    setDomainKey((Number160.createHash(Constants.KEYWORD_DOMAIN)))
                    .start();
            futureDHT.awaitUninterruptibly();
            if (futureDHT.isSuccess()) {
                Keyword item = (Keyword) futureDHT.getData().getObject();
                return item;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception thrown while retrieving object");
        }
        return null;
    }
}
