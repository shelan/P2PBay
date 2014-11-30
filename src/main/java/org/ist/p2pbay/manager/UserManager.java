
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
import org.ist.p2pbay.data.User;
import org.ist.p2pbay.exception.P2PBayException;
import org.ist.p2pbay.gossip.GossipManager;
import org.ist.p2pbay.gossip.GossipObject;
import org.ist.p2pbay.util.Constants;

import java.security.MessageDigest;
import java.util.HashMap;

public class UserManager {

    private Peer peer;
    private GossipManager gossipManager;
    private HashMap<String, User> loggedInUsers = new HashMap<String, User>();
    public static final Log log = LogFactory.getLog(UserManager.class);

    public UserManager(Peer peer, GossipManager manager) throws Exception {
        this.peer = peer;
        this.gossipManager = manager;
    }

    public boolean login(String username, String password) throws P2PBayException {

        User user = getUser(username);
        if (user == null) {
            return false;
        }

        String originalDecoded;
        String enteredDecoded;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(Constants.ALGORITHM);
            messageDigest.update(password.getBytes(Constants.CHARSET_NAME));
            byte[] enteredDigest = messageDigest.digest();

            originalDecoded = new String(user.getPassword(), Constants.CHARSET_NAME);
            enteredDecoded = new String(enteredDigest, Constants.CHARSET_NAME);
        } catch (Exception e) {
            log.error(e);
            throw new P2PBayException("Error while encoding");
        }

        boolean isLoggedIn = false;
        if (originalDecoded.equals(enteredDecoded)) {
            System.out.println("Successfully logged in.");
            isLoggedIn = true;
            addLoggedInUser(username, user);
        } else {
            System.out.println("Password does not match. Please try again");
        }
        return isLoggedIn;
    }

    /**
     * Add user to the system
     *
     * @param name
     * @param obj
     */
    public void addUser(String name, User obj) throws P2PBayException {
        try {
            peer.put(Number160.createHash(name)).setData(new Data(obj)).setDomainKey(Number160.createHash(Constants.USER_DOMAIN))
                    .start().awaitUninterruptibly();
            notifyGossipManager(true);

        } catch (Exception ex) {
           log.error(ex);
           throw new P2PBayException("Exception thrown while accessing object");
        }
    }

    public void removeUser(String name) {
        try {
            peer.remove(Number160.createHash(name)).setDomainKey(Number160.createHash(Constants.USER_DOMAIN))
                    .start().awaitUninterruptibly();
            notifyGossipManager(false);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception thrown while accessing object");
        }
    }

    public User getUser(String name) {
        try {
            FutureDHT futureDHT = peer.get(Number160.createHash(name)).
                    setDomainKey((Number160.createHash(Constants.USER_DOMAIN)))
                    .start();
            futureDHT.awaitUninterruptibly();
            if (futureDHT.isSuccess()) {
                User user = (User) futureDHT.getData().getObject();
                return user;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception thrown while retrieving object");
        }
        return null;
    }

    public User getLoggedInUser(String userName) {
        return loggedInUsers.get(userName);
    }

    public boolean logout(String userName) {
        loggedInUsers.remove(userName);
        return loggedInUsers.containsKey(userName);
    }

    public boolean isLoggedIn(String userName) {
        return loggedInUsers.containsKey(userName);
    }

    public void addLoggedInUser(String name, User loggedInUser) {
        loggedInUsers.put(name, loggedInUser);
    }

    private void notifyGossipManager(boolean isAdd) {
        if (isAdd) {
            GossipObject gossipObject = new GossipObject();
            gossipObject.setCount(1.0);
            gossipObject.setWeight(0.0);
            gossipManager.getUserInfoRepo().mergeGossipObject(gossipObject);
        } else {
            GossipObject gossipObject = new GossipObject();
            gossipObject.setCount(-1.0);
            gossipObject.setWeight(0.0);
            gossipManager.getUserInfoRepo().mergeGossipObject(gossipObject);
        }
    }
}