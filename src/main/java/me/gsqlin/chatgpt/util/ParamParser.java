package me.gsqlin.chatgpt.util;

import java.util.Scanner;

public class ParamParser {
    private String params;

    public ParamParser(String params) {
        this.params = params;
    }
//用于获取一个字符串中的参数，格式 type=value,name=value;
    public String getValue(String key) {
        // 使用 Scanner 类解析字符串并获取参数
        Scanner scanner = new Scanner(params);
        scanner.useDelimiter(";");
        while (scanner.hasNext()) {
            String param = scanner.next();
            if (param.startsWith(key + "=")) {
                return param.substring(key.length() + 1);
            }
        }
        return null;
    }
}
