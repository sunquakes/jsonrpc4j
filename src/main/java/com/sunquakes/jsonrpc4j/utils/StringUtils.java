package com.sunquakes.jsonrpc4j.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.utils
 * @Author: Robert
 * @CreateTime: 2022/6/13 8:19 PM
 **/
@UtilityClass
public class StringUtils {

    public String lineToHump(String str) {
        Pattern compile = Pattern.compile("_[a-z]");
        Matcher matcher = compile.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(0).toUpperCase().replace("_", ""));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public String firstToUpperCase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public String formatClassName(String str) {
        return firstToUpperCase(lineToHump(str));
    }
}
