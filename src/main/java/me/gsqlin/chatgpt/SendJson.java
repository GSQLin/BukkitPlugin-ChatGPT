package me.gsqlin.chatgpt;

public class SendJson {
    private String model;
    private String prompt;
    private Double temperature;
    private Integer max_tokens;
    public SendJson(){
        this.model = "text-davinci-003";
        this.prompt = "你好";
        this.temperature = 0.7;
        this.max_tokens = 7;
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
}
