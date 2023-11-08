package com.sunquakes.jsonrpc4j.config;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/6 6:20 PM
 **/
public class ConfigEntry<T> {

    private final String name;

    private final T value;

    public ConfigEntry(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public T value() {
        return value;
    }
}
