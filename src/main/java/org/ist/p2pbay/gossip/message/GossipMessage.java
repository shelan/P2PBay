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

package org.ist.p2pbay.gossip.message;

import org.ist.p2pbay.gossip.GossipObject;

import java.io.Serializable;

/**
 * Created by shelan on 11/29/14.
 */
public abstract class GossipMessage implements Serializable,Message {
    private GossipObject gossipObject;
    private int gossipRound;


    public GossipObject getGossipObject() {
        return gossipObject;
    }

    public void setGossipObject(GossipObject gossipObject) {
        this.gossipObject = gossipObject;
    }

    public int getGossipRound() {
        return gossipRound;
    }

    public void setGossipRound(int gossipRound) {
        this.gossipRound = gossipRound;
    }
}
