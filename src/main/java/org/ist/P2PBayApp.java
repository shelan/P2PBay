package org.ist;

import net.tomp2p.p2p.Peer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.p2pbay.data.BidInfo;
import org.ist.p2pbay.data.Item;
import org.ist.p2pbay.data.User;
import org.ist.p2pbay.exception.P2PBayException;
import org.ist.p2pbay.gossip.GossipManager;
import org.ist.p2pbay.gossip.GossipObject;
import org.ist.p2pbay.info.StatPublisher;
import org.ist.p2pbay.manager.*;
import org.ist.p2pbay.rest.RestAPi;
import org.ist.p2pbay.util.UI;

import java.io.Console;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Main Application
 */
public class P2PBayApp {

    private static Log log = LogFactory.getLog(P2PBayApp.class);
    private Console console = System.console();
    private Peer peer;

    private UserManager userManager;
    private SalesManager salesManager;
    private SearchManager searchManager;
    private NetworkManager networkManager;
    private BidManager bidManager;
    private GossipManager gossipManager;

    public P2PBayApp() {
        this.networkManager = new NetworkManager();
    }

    public static void main(String[] args) throws Exception {

        if (args[0] == null || args[1] == null || args[2] == null) {
            System.out.println("Should provide >> String bootstrapIp, String bootstrapPort, String currentPort ");
            return;
        }

        P2PBayApp app = new P2PBayApp();
        app.bootstrap(args[0], args[1], args[2]);

        if (args.length >= 4 && Boolean.valueOf(args[3])) {
            RestAPi restAPI = new RestAPi(app);
            restAPI.startRestApi();
        }

        if (args.length == 5) {
            Thread statPublisher = new StatPublisher(app.peer, app.gossipManager, Boolean.valueOf(args[4]));
            statPublisher.start();
        }

        app.startConsoleApp();
    }

    private void startConsoleApp() {
        new UI(this).launchMainMenuUI();
    }

    //private void bootstrap(String bootstrapIp, String bootstrapPort, String currentPort ) throws Exception{
    public void bootstrap(String bootstrapIp, String bootstrapPort, String currentPort) throws Exception {
        //Node joins the network
        log.info("Bootstrapping p2p app ..");

        //args[0] :node-id, args[1]:bootstrap-ip-address, node2:bootstrap port, node3:currentport
        peer = getNetworkManager().bootstrapNode(bootstrapIp, bootstrapPort, currentPort);

        this.gossipManager = new GossipManager(peer);
        this.userManager = new UserManager(peer, gossipManager);
        this.bidManager = new BidManager(peer);
        this.salesManager = new SalesManager(peer, bidManager, gossipManager);
        this.searchManager = new SearchManager(peer);

        gossipManager.runGossip();

        //remove this during production

        /*if ("127.0.0.1".equals(bootstrapIp) && "4000".equals(bootstrapPort) && "4000".equals(currentPort)) {
            TempUtil.addUser(getUserManager());
        }*/
        log.info("P2P App is started ..");
    }

    public void shutDown() {
        try {
            log.info("Shutting down node ...!!!");
            gossipManager.stopGossip();
            peer.shutdown();
        } catch (Exception e) {
            log.error("Error while shutting down the peer", e);
        }
    }

    public boolean registerUser(String name, byte[] password) throws P2PBayException {
        User newUser = new User(name, password);
        userManager.addUser(name, newUser);
        return true;
    }

    public User getUser(String userName) {
        return userManager.getUser(userName);
    }

    public boolean login (String userName, String password) throws P2PBayException {
       return userManager.login(userName,password);
    }

    public boolean logout(String userName) {
        return userManager.logout(userName);
    }

    public void addItem(String title, String description, String user) throws P2PBayException {
        Item item = new Item(title, user);
        item.setDescription(description);
        salesManager.addItem(title, item);
        searchManager.addItemToKeyword(title);
    }

    public Item getItem(String title) {
        return salesManager.getItem(title);
    }

    public boolean bidItem(String itemName, double amount, String userName) throws P2PBayException {
        if (bidManager.getHighestBid(itemName).getAmount() >= amount) {
            return false;
        }
        BidInfo bid = new BidInfo(userName, amount);
        boolean isSuccessful = bidManager.addBid(itemName, bid);
        if (isSuccessful) {
            User user = userManager.getUser(userName);
            user.addBadeItems(itemName, amount);
            userManager.addUser(userName, user);
        }
        return isSuccessful;
    }

    public String[] simpleSearch(String keyword) {
        String[] items = searchManager.getMatchingItems(keyword);
        return items;
    }

    public String[] advanceSearch(String[] keywords, char operator) {
        String[] items = searchManager.getMatchingItems(keywords, operator);
        return items;
    }

    public BidInfo closeAuction(String userName, String itemTitle) throws P2PBayException {

        BidInfo bidInfo = salesManager.closeAuction(userName, itemTitle);
        if (bidInfo != null) {
            BidInfo highestBid = bidManager.getHighestBid(itemTitle);
            User winningUser = userManager.getUser(highestBid.getUserId());
            User seller = userManager.getUser(salesManager.getItem(itemTitle).getSellerId());

            winningUser.addPurchasedItems(itemTitle, highestBid.getAmount());
            winningUser.removeFromBadeItems(itemTitle);

            seller.removeFromSellingItems(itemTitle);

            userManager.addUser(seller.getName(), seller);
            userManager.addUser(winningUser.getName(), winningUser);

            salesManager.removeItem(itemTitle);
            searchManager.removeItemFromKeywordObjects(itemTitle);
            //TODO remove from gossip???? but as per tests remove item should do
        }
        return bidInfo;
    }

    public List<BidInfo> getItemBidHistory(String itemName) {
        return bidManager.getBidList(itemName);
    }

    public Map<String, Vector> getUserBidHistory(String userName) {
        User user = userManager.getUser(userName);
        if (user != null) {
            return user.getBadeItems();
        }
        return new Hashtable<String, Vector>();
    }

    public Map<String, Double> getUserPurchasedHistory(String userName) {
        User user = userManager.getUser(userName);
        if (user != null) {
            return user.getPurchasedItems();
        }
        return new Hashtable<String, Double>();
    }

    public int getNodeCount() {
        GossipObject gossipObject = gossipManager.getNodeInfoRepo().getinfoHolder();
        return (int) Math.round(gossipObject.getCount() / gossipObject.getWeight());
    }

    public int getItemCount() {
        GossipObject gossipObject = gossipManager.getItemInfoRepo().getinfoHolder();
        return (int) Math.round(gossipObject.getCount() / gossipObject.getWeight());
    }

    public int getUserCount() {
        GossipObject gossipObject = gossipManager.getUserInfoRepo().getinfoHolder();
        return (int) Math.round(gossipObject.getCount() / gossipObject.getWeight());
    }

    public SalesManager getSalesManager() {
        return salesManager;
    }

    public SearchManager getSearchManager() {
        return searchManager;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public Peer getPeer() {
        return peer;
    }

    public GossipManager getGossipManager() {
        return gossipManager;
    }

    public BidManager getBidManager() {
        return bidManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }
}
