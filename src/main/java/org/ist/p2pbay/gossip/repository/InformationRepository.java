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
import org.ist.p2pbay.util.Constants;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InformationRepository {
    
    GossipObject infoHolder;
    private double initialWeight;
    AtomicLong totalCount ;
    Lock lock = new ReentrantLock();

    public InformationRepository(GossipObject infoHolder){
        this.infoHolder = infoHolder;
        this.initialWeight = infoHolder.getWeight();
        this.totalCount = new AtomicLong((long) infoHolder.getCount());
    }

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

    public void resetGossipObject(){
        lock.lock();
        try{
            infoHolder.setWeight(initialWeight);
            infoHolder.setCount(totalCount.get());
        }finally {
            lock.unlock();
        }
    }

    public GossipObject getInfoToHandover(int type) {
        lock.lock();
        try {
            switch (type) {
                case Constants.HANDOVER_TYPE_NODE:
                    infoHolder.setCount(infoHolder.getCount() -1 );
                    infoHolder.setWeight(infoHolder.getWeight());
                    return infoHolder;

                default:
                    return null;
            }
        }
        finally {
            lock.unlock();
        }
    }

    public void incrementCounter(){
        totalCount.getAndIncrement();
    }

    public void decrementCounter(){
        totalCount.getAndDecrement();
    }
}
