
/*
 *
 *  * Copyright 2014
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.ist.p2pbay.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.P2PBayApp;
import org.ist.p2pbay.data.BidInfo;
import org.ist.p2pbay.data.Item;
import org.ist.p2pbay.exception.P2PBayException;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class UI {

    private P2PBayApp app;

    private static Console console = System.console();
    private static Log log = LogFactory.getLog(UI.class);
    private String loggedInUserName = null;

    public UI(P2PBayApp app) {
        this.app = app;
    }

    public void launchMainMenuUI() {
        int choice = 0;
        System.out.println("\n1 Login");
        System.out.println("2 Register");
        System.out.println("3 Admin Console");
        System.out.println("4 Exit");
        try {
            choice = Integer.valueOf(console.readLine("Please Enter your choice:"));
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid option number");
        }

        switch (choice) {
            case 0:
                launchMainMenuUI();
                break;

            case 1:
                boolean isLoginSuccessful = login();
                if(!isLoginSuccessful) {
                    launchMainMenuUI();
                } else {
                    showUserHomeActions();
                }
                break;

            case 2:
                boolean isRegistrationSuccessful = registerUser();
                launchMainMenuUI(); // can login or register again
                break;

            case 3:
                boolean isLoggedIn = loginToAdminConsole();
                if(isLoggedIn)
                    showAdminHomeDetails();
                else
                    launchMainMenuUI();
                break;

            case 4:
                String exit = console.readLine("Do you really want to exit? (Y/N)");
                if(Constants.EXIT.equalsIgnoreCase(exit)) {
                    app.shutDown();
                    System.exit(0);
                } else {
                    launchMainMenuUI();
                }
                break;

            default:
                launchMainMenuUI();
                break;
        }
    }

    private boolean login() {
        boolean isLoggedIn = false;
        String userName = "";
            userName = console.readLine("\nEnter a username: ");
            String givenPassword = String.valueOf(console.readPassword("Enter password: "));
            try {
                isLoggedIn = app.getUserManager().login(userName, givenPassword);
                if(isLoggedIn) {
                    loggedInUserName = userName;
                } else {
                    System.out.println("Invalid username or password. Please try again");
                }
            } catch (P2PBayException e) {
                log.error("Error while logging in. " + e.getMessage());
            }
        return isLoggedIn;
    }

    private boolean registerUser() {
        boolean newAccountCreated = false;
        String userName = console.readLine("\nEnter a username: ");
        if(app.getUser(userName) != null) {
            System.out.println("Username already exists. Please try again with a different username");
            return newAccountCreated;
        }
        String password = String.valueOf(console.readPassword("Enter password: "));
        String confPassword = String.valueOf(console.readPassword("Re enter password: "));
        if (!password.equals(confPassword)) {
            System.out.println("Password does not match. Please try again");
            return newAccountCreated;
        }

        try {
            app.registerUser(userName, getMessageDigest(password));
            System.out.println("User account created for user: " + userName);
        } catch (P2PBayException e) {
            log.error("Error while registering user " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            log.error("Error while registering user " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            log.error("Error while registering user " + e.getMessage());
        }
        return newAccountCreated;
    }

    private boolean loginToAdminConsole() {
        boolean isLoggedIn = false;
        try {
            String enteredPassword = String.valueOf(console.readPassword("\nEnter admin password: "));
            String adminPassword = getAdminPassword();
            if(adminPassword == null) {
                log.fatal("Admin password not specified in property file");
                return false;
            }
            if (adminPassword.equals(enteredPassword)) {
                isLoggedIn = true;
                System.out.println("Successfully logged in as admin");
            } else {
                System.out.println("Wrong admin password...!!!");
            }
        } catch (IOException e) {
            log.error("Error while retrieving admin password");
        }
        return isLoggedIn;
    }

    private String getAdminPassword() throws IOException {
        Properties p2pBayProperties = new Properties();
            InputStream input = null;
            try {
                input = getClass().getResourceAsStream("/p2pbay.properties");
                p2pBayProperties.load(input);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        return p2pBayProperties.getProperty(Constants.ADMIN_PASSWORD);
    }

    private void showAdminHomeDetails() {
        System.out.println("\nNode count: " + app.getNodeCount());
        System.out.println("Item count: " + app.getItemCount());
        System.out.println("User count: " + app.getUserCount() + "\n");

        System.out.println("1 Refresh Details");
        System.out.println("2 Exit Admin Console");

        int choice = 0;
        try {
            choice = Integer.valueOf(console.readLine("Please Enter your choice:"));
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid option number");
            showAdminHomeDetails();
        }

        switch (choice) {
            case 1:
                showAdminHomeDetails();
                break;

            case 2:
                launchMainMenuUI();
                break;

            default:
                System.out.println("Please enter a valid option number");
                showAdminHomeDetails();
                break;
        }
    }

    private void showUserHomeActions() {
        System.out.println("\n1 Create Item ");
        System.out.println("2 Search Item");
        System.out.println("3 View Item Details");
        System.out.println("4 Bid for an item");
        System.out.println("5 View Bid History");
        System.out.println("6 View Purchased History");
        System.out.println("7 Close Auction");
        System.out.println("8 Logout");

        int choice = 0;
        if (console != null) {
            try {
                choice = Integer.valueOf(console.readLine("Please Enter your choice:"));
            } catch (NumberFormatException e) {
                System.out.println("Please enter valid option number");
                showUserHomeActions();
            }
        }

        switch (choice) {
            case 1 :
                if(loggedInUserName != null)
                    createItem(loggedInUserName);
                break;

            case 2:
                search();
                break;

            case 3:
                viewItemDetails();
                break;

            case 4:
                if(loggedInUserName != null)
                    bid(loggedInUserName);
                break;

            case 5:
                viewBidHistory(loggedInUserName);
                break;

            case 6:
                viewPurchasedHistory(loggedInUserName);
                break;

            case 7:
                closeAuction(loggedInUserName);
                break;

            case 8:
                String logout = console.readLine("Do you really want to logout? (Y/N)");
                if(Constants.EXIT.equalsIgnoreCase(logout)) {
                    app.logout(loggedInUserName);
                    loggedInUserName = null;
                    launchMainMenuUI();
                }
                break;

            default:
                break;
        }
        showUserHomeActions();
    }

    private void createItem(String userName) {
        String itemTitle = "";
        String itemDesc = "";
        try {
            if (console != null) {
                System.out.println("\n------Please enter details to create an Item -----");
                itemTitle = console.readLine("Item Title:");
                if(app.getItem(itemTitle) != null) {
                    System.out.println("Item with the same title already exists. Please try again with a different title");
                    return;
                }
                itemDesc = console.readLine("Item Description:");
                app.addItem(itemTitle, itemDesc, userName);

            } else {
                Scanner scanner = new Scanner(System.in);
                System.out.println("\n ------Please enter details to create an Item -----");
                System.out.println("Item Title:");
                itemTitle = (scanner.nextLine());
                System.out.println("Item Description:");
                itemDesc = (scanner.nextLine());
                app.addItem(itemTitle, itemDesc, userName);
            }
            System.out.println("Item added successfully");
        } catch (P2PBayException e) {
            log.error("Error while creating item with title " + itemTitle);
            log.error(e.getMessage());
        }
    }

    private void search() {
        String keywordString = console.readLine("\nEnter keyword:");
        String[] resultItems;

        if(keywordString.contains(String.valueOf(Constants.AND_OPERATION)) || keywordString.contains(String.valueOf(Constants.OR_OPERATION))) {
            char operation = keywordString.trim().charAt(0);
            String[] keywords = keywordString.substring(1).trim().split(" ");
            resultItems = app.advanceSearch(keywords, operation);
        } else {
            resultItems = app.simpleSearch(keywordString);
        }

        if (resultItems != null && resultItems.length > 0) {
            System.out.println("Matching items:");
            for (String itemTitle : resultItems) {
                System.out.println("  " + itemTitle);
            }
        } else {
            System.out.println("No items found for keywords: " + keywordString);
        }
    }

    private void viewItemDetails() {
        String itemTitle = console.readLine("\nEnter Item Title:");
        Item item = app.getItem(itemTitle);
        if(item != null) {
            System.out.println("Item Details:");
            System.out.println("#############\n" + item.getTitle() + "\n#############");
            System.out.println("Description: " + item.getDescription());
            System.out.println("Bid History:");
            List<BidInfo> bidHistory = app.getItemBidHistory(item.getTitle());
            if(bidHistory != null && bidHistory.size() > 0) {
                for (BidInfo bidInfo : bidHistory) {
                    System.out.println("  " + bidInfo.getUserId() + " : " + bidInfo.getAmount());
                }
            } else {
                System.out.println("  No bids for item");
            }
        } else {
            System.out.println("No item found with title " + itemTitle + ". Please enter a valid title");
        }
    }

    private void bid(String userName) {
        String itemTitle = "";
        try {
            itemTitle = console.readLine("\nEnter the item title to bid:");
            if(app.getItem(itemTitle) != null) {
                double bid = Double.valueOf(console.readLine("Enter your bid:"));
                boolean isSuccessful = app.bidItem(itemTitle, bid, userName);
                if(isSuccessful) {
                    BidInfo highestBid = app.getHighestBid(itemTitle);
                    if(bid < highestBid.getAmount())
                        System.out.println("Your bid was outbidded...!!!. Highest bid: " + highestBid.getAmount());
                    else
                        System.out.println("Bid added successfully");
                } else {
                    System.out.println("Failed to add Bid. Please try again");
                }
            } else {
                System.out.println("No item found with title " + itemTitle + ". Please enter a valid title");
            }

        } catch (NumberFormatException e) {
            System.out.println("Bid amount should be a number, please retry..!!!.");
        } catch (P2PBayException e) {
            log.error("Error while adding the bid for item: " + itemTitle);
        }
    }

    private void viewBidHistory(String userName) {
        Map<String, Vector> bidHistory = app.getUserBidHistory(userName);
        if(bidHistory!= null && bidHistory.size() > 0) {
            System.out.println("\nBid History:");
            for (String itemTitle : bidHistory.keySet()) {
                System.out.println("  Item: " + itemTitle);
                Vector<Double> badeAmounts = bidHistory.get(itemTitle);
                for (Double badeAmount : badeAmounts) {
                    System.out.println("  Bid amount: " + badeAmount+ "\n");
                }

            }
        } else {
            System.out.println("\nYou do not have any pending bids.");
        }
    }

    private void viewPurchasedHistory(String userName) {
        Map<String, Double> purchasedHistory = app.getUserPurchasedHistory(userName);
        if(purchasedHistory!= null && purchasedHistory.size() != 0) {
            System.out.println("\nPurchased History:");
            for (String itemTitle : purchasedHistory.keySet()) {
                System.out.println("  Item: " + itemTitle);
                System.out.println("  Bid amount: " + purchasedHistory.get(itemTitle) + "\n");
            }
        } else {
            System.out.println("\nYou have not purchased any items yet");
        }
    }

    private void closeAuction(String userName) {
        String itemTitle = "";
        try {
            itemTitle = console.readLine("\nClose auction of item with title:");
            if (app.getItem(itemTitle) != null) {
                BidInfo highestBid = app.closeAuction(userName, itemTitle);
                if(highestBid != null)
                    System.out.println("Sold the item to " + highestBid.getUserId() + " for " + highestBid.getAmount());
                else
                    System.out.println("Close auction failed.");

            } else {
                System.out.println("No item found with title " + itemTitle + ". Please enter a valid title");
            }
        } catch (P2PBayException e) {
            log.error("Error while closing auction for item: " + itemTitle + " by " + userName);
        }
    }

    private synchronized byte[] getMessageDigest(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance(Constants.ALGORITHM_SHA_256);
        messageDigest.update(password.getBytes(Constants.CHARSET_NAME_UTF_8));
        return messageDigest.digest();
    }
}
