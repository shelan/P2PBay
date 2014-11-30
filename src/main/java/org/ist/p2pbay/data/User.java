
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

package org.ist.p2pbay.data;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class User implements Serializable {

    private String name;
    private byte[] password;
    private List<String> sellingItems = new Vector<String>();
    private Map<String, Double> badeItems = new Hashtable<String, Double>(); //ItemID, bade amount
    private Map<String, Double> purchasedItems = new Hashtable<String, Double>(); //ItemID, amount

    public User(String name, byte[] password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSellingItems() {
        return sellingItems;
    }

    public void addToSellingItems(String itemName) {
        this.sellingItems.add(itemName);
    }

    public void removeFromSellingItems(String itemName){
        this.sellingItems.remove(itemName);
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public Map<String, Double> getBadeItems() {
        return badeItems;
    }

    public void addBadeItems(String itemID, double amount) {
        badeItems.put(itemID, amount);
    }

    public void removeFromBadeItems(String itemId){
        badeItems.remove(itemId);
    }

    public Map<String, Double> getPurchasedItems() {
        return purchasedItems;
    }

    public void addPurchasedItems(String itemId, double amount) {
        purchasedItems.put(itemId, amount);
    }
}
