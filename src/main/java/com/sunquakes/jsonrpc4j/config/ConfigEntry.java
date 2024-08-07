package com.sunquakes.jsonrpc4j.config;

/**
 * @author Shing Rui {@link "mailto:sunquakes@outlook.com"}
 * @version 2.1.0
 * @since 1.0.0
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
