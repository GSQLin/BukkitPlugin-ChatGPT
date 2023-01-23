package me.gsqlin.chatgpt.data;

import me.gsqlin.chatgpt.util.ParamParser;

import java.util.ArrayList;
import java.util.List;

public class CustomReply {
    public enum TYPE{
        CUSTOM, CACHE
    }
    public static List<CustomReply> customReplyList = new ArrayList<>();
    String value;
    CustomReply.TYPE type;
    float similarity;

    String reply;
    String command;
    public CustomReply(ParamParser parser){
        this.value = parser.getValue("value");
        this.type = CustomReply.TYPE.valueOf(parser.getValue("type"));
        this.similarity = Float.parseFloat(parser.getValue("similarity"));
        this.reply = parser.getValue("reply");
        this.command = parser.getValue("command");
        customReplyList.add(this);
    }

    public String getValue() {
        return value;
    }

    public CustomReply.TYPE getType() {
        return type;
    }

    public float getSimilarity() {
        return similarity;
    }

    public String getReply() {
        return reply;
    }

    public String getCommand() {
        return command;
    }
    public void setReply(String reply) {
        this.reply = reply;
    }
}
