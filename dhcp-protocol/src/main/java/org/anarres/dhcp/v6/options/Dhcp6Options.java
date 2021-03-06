/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *
 */
package org.anarres.dhcp.v6.options;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class Dhcp6Options implements Iterable<Dhcp6Option> {

    private final Map<Short, Dhcp6Option> options = new HashMap<Short, Dhcp6Option>();

    public boolean isEmpty() {
        return options.isEmpty();
    }

    @Override
    public Iterator<Dhcp6Option> iterator() {
        return options.values().iterator();
    }

    @CheckForNull
    public <T extends Dhcp6Option> T get(@Nonnull Class<T> type) {
        Dhcp6OptionsRegistry registry = Dhcp6OptionsRegistry.getInstance();
        Dhcp6Option option = get(registry.getOptionTag(type));
        if (option == null)
            return null;
        if (type.isInstance(option))
            return type.cast(option);
        T impl = Dhcp6OptionsRegistry.newInstance(type);
        impl.setData(option.getData());
        return impl;
    }

    @CheckForNull
    public Dhcp6Option get(short tag) {
        return options.get(tag);
    }

    public void add(@Nonnull Dhcp6Option option) {
        options.put(option.getTag(), option);
    }

    public void addAll(@CheckForNull Iterable<? extends Dhcp6Option> options) {
        if (options == null)
            return;
        for (Dhcp6Option option : options)
            add(option);
    }

    /**
     * Remove instances of the given option class.
     * 
     * @param type
     */
    public void remove(@Nonnull Class<? extends Dhcp6Option> type) {
        Dhcp6OptionsRegistry registry = Dhcp6OptionsRegistry.getInstance();
        remove(registry.getOptionTag(type));
    }

    /**
     * Remove options matching the given tag
     * 
     * @param tag
     */
    public void remove(short tag) {
        options.remove(tag);
    }

    /**
     * @see Map#clear()
     */
    public void clear() {
        options.clear();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + options.values() + ")";
    }
}
