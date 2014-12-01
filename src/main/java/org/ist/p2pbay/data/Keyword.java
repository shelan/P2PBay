
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

package org.ist.p2pbay.data;

import java.io.Serializable;
import java.util.ArrayList;

public class Keyword implements Serializable {
    private ArrayList<String> items = new ArrayList<String>();

    public ArrayList<String> getItems() {
        return items;
    }

    public void addItem(String itemId) {
        items.add(itemId);
    }

    public void removeItem(String itemId) {
        items.remove(itemId);
    }
}
