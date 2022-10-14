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
        ResultDto result = new ResultDto();
        result.setC(args.getA() + args.getB());
        return result;
    }

    @Override
    public ResultDto add3(ArgsDto args) {
        ResultDto innerResult = new ResultDto();
        innerResult.setC(args.getArgs().getA() + args.getArgs().getB());
        ResultDto result = new ResultDto();
        result.setResult(innerResult);
        return result;
    }
}
