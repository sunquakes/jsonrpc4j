package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.spring.boot.dto.ArgsDto;
import com.sunquakes.jsonrpc4j.spring.boot.dto.ResultDto;

public class JsonRpc2ServiceImpl implements IJsonRpc2Service {

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int sub(int a, int b) {
        return a - b;
    }

    @Override
    public ResultDto add2(ArgsDto args) {
        ResultDto resultDto = new ResultDto();
        resultDto.setC(args.getA() + args.getB());
        return resultDto;
    }
}
