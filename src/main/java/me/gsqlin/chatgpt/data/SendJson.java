package me.gsqlin.chatgpt.data;

import java.util.ArrayList;
import java.util.List;

public class SendJson {
    public static List<String> record = new ArrayList<>();
    private String[] stop;
    private int top_p;
    private String model;
    private String prompt;
    private Double temperature;
    private Integer max_tokens;
    public SendJson(){
        this.stop = new String[]{"\nHuman:","\n\nAI:"};
        this.model = "text-davinci-003";
        this.prompt = "你好";
        this.temperature = 0.7;
        this.max_tokens = 7;
        this.top_p = 1;
    }
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return max_tokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.max_tokens = maxTokens;
    }

    public static String getInformationWithRecords(String nowMsg){
        StringBuilder builder = new StringBuilder();
        for (String s : record) {
            builder.append(s);
        }
        String msg = builder.append("Human:"+nowMsg).toString();
        return msg;
    }

    public String[] getStop() {
        return stop;
    }

    public void setStop(String[] stop) {
        this.stop = stop;
    }

    public int getTop_p() {
        return top_p;
    }

    public void setTop_p(int top_p) {
        this.top_p = top_p;
    }
}
