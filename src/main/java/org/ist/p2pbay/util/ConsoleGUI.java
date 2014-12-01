package org.ist.p2pbay.util;

import org.ist.p2pbay.data.Item;
import org.ist.p2pbay.data.User;
import org.ist.p2pbay.exception.P2PBayException;
import org.ist.p2pbay.manager.BidManager;
import org.ist.p2pbay.manager.SalesManager;
import org.ist.p2pbay.manager.SearchManager;
import org.ist.p2pbay.manager.UserManager;

import java.io.Console;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/**
 * Created by shelan on 11/4/14.
 */
public class ConsoleGUI {

    private UserManager userManager;
    private SalesManager salesManager;
    private BidManager bidManager;
    private SearchManager searchManager;

    private static Console console = System.console();

    public ConsoleGUI(UserManager userManager, SalesManager salesManager, BidManager bidManager,
                      SearchManager searchManager) {
        this.userManager = userManager;
        this.salesManager = salesManager;
        this.bidManager = bidManager;
        this.searchManager = searchManager;
    }

    public boolean registerUserUi() {
        boolean newAccountCreated = false;
        String userName = console.readLine("Enter a username: ");
        try {
            User user = userManager.getUser(userName);

            if (user != null) {
                System.out.println("Username already exist.");
                return newAccountCreated;
            }

            String password = String.valueOf(console.readPassword("Enter password: "));
            String confPassword = String.valueOf(console.readPassword("Re enter password: "));
            if (!password.equals(confPassword)) {
                System.out.println("Password does not match. Please try again");
                return newAccountCreated;
            }

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(password.getBytes("UTF-8"));
            // password to store
            byte[] passwordDigest = md.digest();
            user = new User(userName, passwordDigest);
            user.setName(userName);
            user.setPassword(passwordDigest);
            userManager.addUser(userName, user);
            newAccountCreated = true;
            System.out.println("New USER_DOMAIN has been created for " + userName);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (P2PBayException e) {
            e.printStackTrace();
        }
        return newAccountCreated;
    }

    public boolean login() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        boolean isLoggedIn = false;
        String userName = console.readLine("Enter a username: ");
        User user = userManager.getUser(userName);
        if (user == null) {
            System.out.println("User does not exist in the system. Please try again with a valid USER_DOMAIN name");
            return false;
        }
        String givenPassword = String.valueOf(console.readPassword("Enter password: "));

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(givenPassword.getBytes("UTF-8"));
        byte[] enteredDigest = messageDigest.digest();

        String originalDecoded = new String(user.getPassword(), "UTF-8");
        String enteredDecoded = new String(enteredDigest, "UTF-8");

        if (originalDecoded.equals(enteredDecoded)) {
            System.out.println("Successfully logged in.");
            isLoggedIn = true;
          //  userManager.addLoggedInUser(userName, user);
        } else {
            System.out.println("Password does not match. Please try again");
        }
        return isLoggedIn;
    }


    private void startConsoleApp() throws NoSuchAlgorithmException, IOException, ClassNotFoundException, P2PBayException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        String userName;
        String password;
        String flag = "";

        if (console != null) {
            getUserInput();
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.println("USER_DOMAIN manager " + userManager);
            while (!"exit".equals(flag)) {

                System.out.println("enter USER_DOMAIN name");
                userName = scanner.nextLine();

                //////User USER_DOMAIN = userManager.get(userName);
                User user = userManager.getUser(userName);

                if (user == null) {
                    System.out.println("No USER_DOMAIN found. Do you want to Register ? Y/N");
                    String response = scanner.nextLine();
                    if ("n".equals(response.toLowerCase())) {
                        System.exit(0);
                    }
                    System.out.println("Please enter your USER_DOMAIN name");
                    userName = scanner.nextLine();
                    System.out.println("Please enter your password");
                    password = scanner.nextLine();

                    messageDigest.update(password.getBytes("UTF-8"));
                    // password to store
                    byte[] passwordDigest = messageDigest.digest();
                    user = new User(userName,passwordDigest);

                    /////userManager.store(userName, USER_DOMAIN);
                    userManager.addUser(userName, user);
                    System.out.println("New USER_DOMAIN has been created for " + userName);
                } else {
                    System.out.println("Please enter your password");
                    password = scanner.nextLine();
                    messageDigest.update(password.getBytes("UTF-8"));
                    byte[] passwordDigest = messageDigest.digest();
                    if (new String(user.getPassword(), "UTF-8").equals(new String(passwordDigest, "UTF-8"))) {
                        System.out.println("You are logged in.....");
                        //  userManager.getUserActionToPerform();

                    }

                }
            }
        }
    }

    //TODO You need to redo this code ......!!!!!!!!!!!!!!!!
    private void getUserInput() throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
        System.out.println("1 Login");
        System.out.println("2 Register");
        System.out.println("3 Exit");
        int choice = 0;
        try {
            choice = Integer.valueOf(console.readLine("Please Enter your choice:"));
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid option number");
        }

        switch (choice) {
            case 1:
                boolean logedIn = login();
                if (!logedIn) {
                    getUserInput();
                } else {
                  //  getUserActionToPerform(userManager.getLoggedInUser().getName());
                }
                break;

            case 2:
                boolean accountCreated = registerUserUi();
                getUserInput();
                break;

            case 3:
                System.exit(0);

            default:
                getUserInput();
        }
    }

    private void getUserActionToPerform(String userID) throws IOException, P2PBayException {
        // TODO Auto-generated method stub
        //Runtime.getRuntime().exec("cls");//to cleat the console
        System.out.println("1 Create Item ");
        System.out.println("2 Bid for an ITEM_DOMAIN");
        System.out.println("3 Search Item");
        System.out.println("4 Exit");
        int choice = 0;//default to create an ITEM_DOMAIN

        if (console != null) {
            try {
                choice = Integer.valueOf(console.readLine("Please Enter your choice:"));
            } catch (NumberFormatException e) {
                System.out.println("Please enter valid option number");
            }
            switch (choice) {
                case 1:
                    Item newItem = createNewItem(userID);
                    salesManager.addItem(newItem.getTitle(), newItem);

                    /*System.out.println("Just to test whether it is added");
                    String title= console.readLine("Item Title:");
                    Item ITEM_DOMAIN = salesManager.getItem(title);
                    System.out.println("seller is "+ITEM_DOMAIN.getSellerId());
                    System.out.println("Item title is "+ITEM_DOMAIN.getTitle());
                    System.out.println("Item description is "+ITEM_DOMAIN.getDescription());*/

                    //add ITEM_DOMAIN for KEYWORD_DOMAIN
                    // TODO..... should split in search manager side
                    searchManager.addItemToKeyword(newItem.getTitle());

                    /*String KEYWORD_DOMAIN= console.readLine("Search ITEM_DOMAIN:");
                    String items[] = searchManager.getMatchingItems(KEYWORD_DOMAIN);
                    System.out.println("Matching items:");
                    for(String i: items) {
                        System.out.println("   " + i);
                    }*/

                    getUserActionToPerform(userID);
                    break;
                case 2:
                    String itemTitle = "";
                    double bid = 0;
                    try {
                        //TODO.... should allow to select from the search list
                        // TODO and should show current highest bid for the ITEM_DOMAIN
                        itemTitle = console.readLine("Enter the ITEM_DOMAIN title:");
                        bid = Double.valueOf(console.readLine("Enter your bid:"));
                    } catch (NumberFormatException e) {
                        System.out.println("Bid amount should be a number, please retry.");
                        getUserActionToPerform(userID);
                        break;
                    }
                    boolean updateResult = salesManager.bidItem(itemTitle, userID, bid);
                    //TODO: show an output for user
                    System.out.println(updateResult);
                    getUserActionToPerform(userID);
                    break;

                case 3:
                    String keyword = console.readLine("Search ITEM_DOMAIN:");
                    String items[];
                    /////
                    if (keyword.contains(String.valueOf(Constants.AND_OPERATION))) {
                        items = searchManager.getMatchingItems(keyword.split(String.valueOf(Constants.AND_OPERATION))[0].trim(),
                                keyword.split(String.valueOf(Constants.AND_OPERATION))[1].trim(), Constants.AND_OPERATION);
                    } else if (keyword.contains(String.valueOf(Constants.OR_OPERATION))) {
                        items = searchManager.getMatchingItems(keyword.split(String.valueOf("\\" + Constants.OR_OPERATION))[0].trim(),
                                keyword.split(String.valueOf("\\" + Constants.OR_OPERATION))[1].trim(), Constants.OR_OPERATION);
                    } else {
                        items = searchManager.getMatchingItems(keyword);
                    }
                    /////
                    //items = searchManager.getMatchingItems(KEYWORD_DOMAIN);
                    if (items != null && items.length != 0) {
                        System.out.println("Matching items:");
                        for (String i : items) {
                            System.out.println("   " + i);
                        }
                    } else {
                        System.out.println("No items found for KEYWORD_DOMAIN: " + keyword);
                    }

                    getUserActionToPerform(userID);
                    break;

                case 4:
                    System.exit(0);

                default:
                    getUserActionToPerform(userID);
                    break;
            }
        } else {
            // TODO Auto-generated method stub
            //Runtime.getRuntime().exec("cls");//to cleat the console
            System.out.println("1 Create Item ");
            System.out.println("2 Bid for an ITEM_DOMAIN");
            System.out.println("3 Search Item");
            System.out.println("Exit");
            Scanner scanner = new Scanner(System.in);

            try {
                System.out.println("Please Enter your choice:");
                choice = Integer.valueOf(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter valid option number");
            }
            switch (choice) {
                case 1:
                    Item newItem = createNewItem(userID);
                    salesManager.addItem(newItem.getTitle(), newItem);
                    System.out.println("Just to test whether it is added");
                    System.out.println("Please Enter your added title:");
                    String title = scanner.nextLine();
                    Item item = salesManager.getItem(title);
                    System.out.println("seller is " + item.getSellerId());
                    System.out.println("Item title is " + item.getTitle());
                    System.out.println("Item description is " + item.getDescription());
                    getUserActionToPerform(userID);
                    break;
                case 2:
                    System.out.println("To be implemented...");
                    getUserActionToPerform(userID);
                    break;

                case 3:
                    //TODO merge code for this
                    getUserActionToPerform(userID);
                    break;
                default:
                    System.exit(0);
            }
        }
    }

    public Item createNewItem(String userId) {
        if(console != null) {
            // TODO Auto-generated method stub
            System.out.println("------Please enter details to create an Item -----");

            String itemTitle = console.readLine("Item Title:");
            String itemDesc =console.readLine("Item Description:");
            Item item=new Item(itemTitle,itemDesc);
            item.setSellerId(userId);
            return item;
        } else {
            Scanner scanner = new Scanner(System.in);
            System.out.println("------Please enter details to create an Item -----");

            System.out.println("Item Title:");
            String itemTitle = (scanner.nextLine());
            System.out.println("Item Description:");
            String itemDesc = (scanner.nextLine());
            Item item=new Item(itemTitle,itemDesc);
            item.setSellerId(userId);
            return item;
        }
    }

}
