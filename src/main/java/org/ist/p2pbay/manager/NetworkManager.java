
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

import net.tomp2p.connection.Bindings;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Random;

/**
 * This class will communicate with TomP2P for operations and manage all network related activities
 * such as joining to network etc.
 */
public class NetworkManager {

    private Peer peer;

    /**
     * Bootstrapping a node to a ring or start own by your self
     * @param ip bootstrapping ip
     * @param bootstrapPort bootstrapping port
     * @param port local port
     * @return created Peer object
     * @throws IOException
     */
    public Peer bootstrapNode(String ip, String bootstrapPort, String port) throws IOException {
        Random random = new Random();
        Bindings bindings = new Bindings();
        //b.addInterface("eth0");

        // Replicas are set so that nodes will manage the repilcas even if some
        // nodes leave
        peer = new PeerMaker(new Number160(random)).setPorts(Integer.parseInt(port)).setEnableIndirectReplication(true)
                .setBindings(bindings).makeAndListen();
        peer.getConfiguration().setBehindFirewall(true);

        InetAddress address = Inet4Address.getByName(ip);
        FutureDiscover futureDiscover = peer.discover().setInetAddress(address)
                .setPorts(Integer.parseInt(bootstrapPort)).start();
        futureDiscover.awaitUninterruptibly();

        if (futureDiscover.isSuccess()) {
           // System.out.println("found that my outside address is " + futureDiscover.getPeerAddress());
        } else {
            System.out.println("failed " + futureDiscover.getFailedReason());
        }

        FutureBootstrap futureBootstrap = peer.bootstrap().setInetAddress(address)
                .setPorts(Integer.parseInt(bootstrapPort)).start();
        futureBootstrap.awaitUninterruptibly();

        return peer;
    }


}