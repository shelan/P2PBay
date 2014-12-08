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

package org.ist.p2pbay.gossip.message;

import java.io.Serializable;

public class HandoverDetailsMessage implements Serializable,Message {

    private double userCount;
    private double itemCount;
    private double gossipNodeCount;
    private double gossipWeight;

    private  double weight;

    public HandoverDetailsMessage(double userCount, double itemCount, double gossipNodeCount, double gossipWeight,
                                  double weight) {
        this.userCount = userCount;
        this.itemCount = itemCount;
        this.gossipNodeCount = gossipNodeCount;
        this.gossipWeight = gossipWeight;
        this.weight = weight;
    }

    @Override
    public MessageType getMSGType() {
        return MessageType.HANDOVER;
    }


    public double getUserCount() {
        return userCount;
    }

    public double getItemCount() {
        return itemCount;
    }

    public double getGossipNodeCount() {
        return gossipNodeCount;
    }

    public double getWeight() {
        return weight;
    }

    public double getGossipWeight() {
        return gossipWeight;
    }

}
