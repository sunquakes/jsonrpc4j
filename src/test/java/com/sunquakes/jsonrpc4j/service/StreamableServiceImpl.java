package com.sunquakes.jsonrpc4j.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamableServiceImpl implements IStreamableService {

    @Override
    public int[] range(int count) {
        return IntStream.range(0, count).toArray();
    }

    @Override
    public Iterable<Integer> sequence(int count) {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(i);
        }
        return values;
    }

    @Override
    public Stream<Integer> numbers(int count) {
        return IntStream.range(0, count).boxed();
    }

    @Override
    public Stream<Integer> broken() {
        return Stream.of(0).map(value -> {
            throw new IllegalStateException("broken");
        });
    }

    @Override
    public CompletableFuture<String> later() {
        return CompletableFuture.completedFuture("later");
    }

    @Override
    public String echo(String value) {
        return value;
    }

    @Override
    public String fail() {
        throw new IllegalStateException("boom");
    }
}
