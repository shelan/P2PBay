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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/*
Search is case insensitive
 */
public class SearchManager {

    private Peer peer;


    public SearchManager(Peer peer) {
        this.peer = peer;
    }

    public void addItemToKeyword(String itemTitle) {
        String keyword = itemTitle.toLowerCase();
        if(keyword.contains(" ")) {
            String[] keywordArray = getKeywordsArray(keyword);
            for(String word : keywordArray) {
                addKeywordObject(word, itemTitle);
            }
        } else {
            addKeywordObject(keyword, itemTitle);
        }
    }

    public void removeItemFromKeywordObjects(String itemTitle) {
        String keyword = itemTitle.toLowerCase();
        if (keyword.contains(" ")) {
            String[] keywordArray = getKeywordsArray(keyword);
            for (String word : keywordArray) {
                Keyword keywordObj = getKeywordObject(word);
                keywordObj.removeItem(itemTitle);
                replaceKeywordObject(word, keywordObj);
            }
        } else {
            Keyword keywordObj = getKeywordObject(keyword);
            keywordObj.removeItem(itemTitle);
            replaceKeywordObject(keyword, keywordObj);
        }
    }

    private String[] getKeywordsArray(String keywordString) {
        // TODO avoid common words - the , on, ...
        return keywordString.split(" ");
    }

    private void addKeywordObject(String keyword, String itemId) {
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

    private void replaceKeywordObject(String keyword, Keyword keywordObj) {
        try {
            peer.put(Number160.createHash(keyword)).setData(new Data(keywordObj)).setDomainKey(Number160.createHash(Constants.KEYWORD_DOMAIN))
                    .start().awaitUninterruptibly();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error while replacing keyword object for keyword: " + keyword);
        }
    }

    public String[] getMatchingItems(String keyword) {
        keyword = keyword.toLowerCase();
        Keyword keywordObj = getKeywordObject(keyword);
        if( keywordObj!= null) {
            return keywordObj.getItems().toArray(new String[keywordObj.getItems().size()]);
        }
        return new String[0];
    }

    public String[] getMatchingItems(String[] keywords, Character operation) {
        Set<String> resultSet = new HashSet<String>();
        if(keywords != null && keywords.length > 0) {
            for (int i = 0; i < keywords.length ; i++) {
                Set result1;
                Set result2;

                if(i == 0) {
                    result1 = new HashSet(Arrays.asList(getMatchingItems(keywords[i])));
                    result2 = new HashSet(Arrays.asList(getMatchingItems(keywords[i+1])));
                    i++;
                } else {
                    result1 = resultSet;
                    result2 = new HashSet(Arrays.asList(getMatchingItems(keywords[i])));
                }
                switch (operation) {
                    case Constants.AND_OPERATION:
                        result1.retainAll(result2);
                        resultSet = result1;
                        break;

                    case Constants.OR_OPERATION:
                        result1.addAll(result2);
                        resultSet = result1;
                        break;
                }

            }
        }
        return (String[])resultSet.toArray(new String[resultSet.size()]);
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
