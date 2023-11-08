package com.sunquakes.jsonrpc4j.config;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/6 6:20 PM
 **/
@Slf4j
public class Config {
    private final Collection<ConfigEntry> entries;

    public Config() {
        entries = new HashSet<>();
    }

    public ConfigEntry get(String name) {
        Iterator iterator = entries.iterator();

        ConfigEntry entry;
        do {
            if (!iterator.hasNext()) {
                return null;
            }
            entry = (ConfigEntry) iterator.next();
        } while (!entry.name().equals(name));

        return entry;
    }

    public void put(ConfigEntry entry) {
        entries.add(entry);
    }
}

