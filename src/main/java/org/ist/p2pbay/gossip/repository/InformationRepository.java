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

package org.ist.p2pbay.gossip.repository;

import org.ist.p2pbay.gossip.GossipObject;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by shelan on 11/29/14.
 */
public class InformationRepository {
    
    GossipObject infoHolder;
    Lock lock = new ReentrantLock();
    
    public GossipObject getinfoHolder() {
        return infoHolder;
    }

    public void setinfoHolder(GossipObject infoHolder) {
        this.infoHolder = infoHolder;
    }

    /**
     * Object will be sliced into half
     * @return
     */
    public GossipObject sliceGossipObject() {
        lock.lock();
        try {
            infoHolder.setCount(infoHolder.getCount() / 2);
            infoHolder.setWeight(infoHolder.getWeight() / 2);
            return infoHolder;
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Object will be merged into current object
     * @param gossipObject
     */
    public void mergeGossipObject(GossipObject gossipObject){
        lock.lock();
        try{
            double currentCount = infoHolder.getCount();
            double currentWeight = infoHolder.getWeight();

            this.infoHolder.setCount(currentCount + gossipObject.getCount());
            this.infoHolder.setWeight(currentWeight + gossipObject.getWeight());
        }finally {
           lock.unlock();
        }
    }
}
