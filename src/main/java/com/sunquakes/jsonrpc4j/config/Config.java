package com.sunquakes.jsonrpc4j.config;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/6 6:20 PM
 **/
@Slf4j
public class Config<T> {
    private final Collection<ConfigEntry<T>> entries;

    public Config() {
        entries = new HashSet<>();
    }

    public ConfigEntry<T> get(String name) {
        Iterator<ConfigEntry<T>> iterator = entries.iterator();

        ConfigEntry<T> entry;
        do {
            if (!iterator.hasNext()) {
                return null;
            }
            entry = iterator.next();
        } while (!entry.name().equals(name));

        return entry;
    }

    public void put(ConfigEntry<T> entry) {
        entries.add(entry);
    }
}

