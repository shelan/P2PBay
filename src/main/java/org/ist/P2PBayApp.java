package org.ist;

import net.tomp2p.p2p.Peer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.p2pbay.data.BidInfo;
import org.ist.p2pbay.data.Item;
import org.ist.p2pbay.data.User;
import org.ist.p2pbay.exception.P2PBayException;
import org.ist.p2pbay.gossip.GossipManager;
import org.ist.p2pbay.info.NodeCounter;
import org.ist.p2pbay.manager.*;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        //app.bootstrap(args[0], args[1], args[2]);
        app.bootstrap(args[0], args[1], args[2]);

        //app.startConsoleApp();

    }

    //private void bootstrap(String bootstrapIp, String bootstrapPort, String currentPort ) throws Exception{
    public void bootstrap(String bootstrapIp, String bootstrapPort, String currentPort) throws Exception {
        //Node joins the network
        //args[0] :node-id, args[1]:bootstrap-ip-address, node2:bootstrap port, node3:currentport
        peer = getNetworkManager().bootstrapNode(bootstrapIp, bootstrapPort, currentPort);

        Thread statCounter = new NodeCounter(getPeer());
        // statCounter.start();

        this.gossipManager = new GossipManager(peer);
        this.userManager = new UserManager(peer,gossipManager);
        this.bidManager = new BidManager(peer);
        this.salesManager = new SalesManager(peer,bidManager,gossipManager);
        this.searchManager = new SearchManager(peer);

        gossipManager.runGossip();

        //remove this during production

        /*if ("127.0.0.1".equals(bootstrapIp) && "4000".equals(bootstrapPort) && "4000".equals(currentPort)) {
            TempUtil.addUser(getUserManager());
        }*/
    }

    public void shutDown(){
        try {
            gossipManager.stopGossip();
            Thread.sleep(5000);
            peer.shutdown();
        } catch (Exception e) {
           log.error("Error while shutting down the peer",e);
        }
    }

    public boolean registerUser(String name,byte[] password) throws P2PBayException {
        User newUser = new User(name,password);
        userManager.addUser(name,newUser);
        return true;
    }

    public boolean login (String userName, String password) throws P2PBayException {
       return userManager.login(userName,password);
    }

    public boolean logout(String userName){
        return userManager.logout(userName);
    }

    public void addItem(String title, String description, String user) throws P2PBayException {
        Item item = new Item(title,user);
        item.setDescription(description);
        salesManager.addItem(title,item);
    }

    public boolean bidItem(String itemName, double amount, String user) throws P2PBayException{
        if(bidManager.getHighestBid(itemName).getAmount()>amount){
            return false;
        }
        BidInfo bid = new BidInfo(user,amount);
        return bidManager.addBid(itemName,bid);
    }

    public List<Item> simpleSearch(String keyword){
        String []  items = searchManager.getMatchingItems(keyword);
        ArrayList<Item> fetchedItems = new ArrayList<Item>();
        for (String item : items) {
            fetchedItems.add(salesManager.getItem(item));
        }
        return fetchedItems;
    }

    public List<Item> advanceSearch(String keyword1, String keyword2, char operator){
        String []  items = searchManager.getMatchingItems(keyword1, keyword2, operator);
        ArrayList<Item> fetchedItems = new ArrayList<Item>();
        for (String item : items) {
            fetchedItems.add(salesManager.getItem(item));
        }
        return fetchedItems;
    }

    public void closeAuction(String item) throws P2PBayException {

            salesManager.closeAuction(item);
            BidInfo highestBid = bidManager.getHighestBid(item);
            User winningUser = userManager.getUser(highestBid.getUserId());
            User seller = userManager.getUser(salesManager.getItem(item).getSellerId());

            winningUser.addPurchasedItems(item, highestBid.getAmount());
            winningUser.removeFromBadeItems(item);

            seller.removeFromSellingItems(item);

            userManager.addUser(seller.getName(), seller);
            userManager.addUser(winningUser.getName(), winningUser);

            salesManager.removeItem(item);
    }

    public List <BidInfo> getBidHistory(String itemName){
        return bidManager.getBidList(itemName);
    }

    private Map<String,Double> getPurchaseHistory(String username){
        User user = userManager.getUser(username);
       return user.getPurchasedItems();
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
