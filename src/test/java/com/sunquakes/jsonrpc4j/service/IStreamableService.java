package com.sunquakes.jsonrpc4j.service;

import com.sunquakes.jsonrpc4j.JsonRpcService;

import java.util.stream.Stream;

@JsonRpcService("Streamable")
public interface IStreamableService {

    int[] range(int count);

    Iterable<Integer> sequence(int count);

    Stream<Integer> numbers(int count);

    String echo(String value);

    String fail();
}
