package com.sunquakes.jsonrpc4j.utils;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Detects values that a JSON-RPC method produces incrementally and exposes
 * them as a plain {@link Iterator}.
 *
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 3.1.0
 * @since 3.1.0
 **/
public class StreamableUtils {

    private StreamableUtils() {
    }

    public static boolean isStreamable(Object value) {
        if (value == null) {
            return false;
        }
        return value.getClass().isArray()
                || value instanceof Iterable
                || value instanceof Iterator
                || value instanceof Stream;
    }

    @SuppressWarnings("unchecked")
    public static Iterator<Object> toIterator(Object value) {
        if (value.getClass().isArray()) {
            return new Iterator<>() {
                private int index;

                @Override
                public boolean hasNext() {
                    return index < Array.getLength(value);
                }

                @Override
                public Object next() {
                    return Array.get(value, index++);
                }
            };
        }
        if (value instanceof Iterator) {
            return (Iterator<Object>) value;
        }
        if (value instanceof Iterable) {
            return ((Iterable<Object>) value).iterator();
        }
        if (value instanceof Stream) {
            return ((Stream<Object>) value).iterator();
        }
        throw new IllegalArgumentException("The value is not streamable.");
    }
}
