package org.ist;

import net.tomp2p.p2p.Peer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ist.p2pbay.gossip.GossipManager;
import org.ist.p2pbay.info.NodeCounter;
import org.ist.p2pbay.manager.*;

import java.io.Console;

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

    public BidManager getBidManager() {
        return bidManager;
    }

    public void setBidManager(BidManager bidManager) {
        this.bidManager = bidManager;
    }

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
        app.bootstrap(args[3], args[0], args[1], args[2]);

        //app.startConsoleApp();

    }

    //private void bootstrap(String bootstrapIp, String bootstrapPort, String currentPort ) throws Exception{
    public void bootstrap(String id, String bootstrapIp, String bootstrapPort, String currentPort) throws Exception {
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
            peer.shutdown();
        } catch (Exception e) {
           log.error("Error while shutting down the peer",e);
        }
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public SalesManager getSalesManager() {
        return salesManager;
    }

    public void setSalesManager(SalesManager salesManager) {
        this.salesManager = salesManager;
    }

    public SearchManager getSearchManager() {
        return searchManager;
    }

    public void setSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void setNetworkManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public GossipManager getGossipManager() {
        return gossipManager;
    }
}
