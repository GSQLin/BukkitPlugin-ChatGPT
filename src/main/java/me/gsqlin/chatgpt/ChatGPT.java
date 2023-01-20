package me.gsqlin.chatgpt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.swing.plaf.basic.ComboPopup;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public final class ChatGPT extends JavaPlugin {
    Gson gson = new Gson();
    SendJson sendJson = new SendJson();
    ArrayList<String> toBeSend = new ArrayList<>(); //待发送的列表

    Boolean isHandle = false;
    @Override
    public void onEnable() {
        //载好配置
        reload();
        //写入api需要发送的内容
        setSendJsonData();
        //建立一个自动对待发送列表的内容进行发送获取回答等操作
        GPT003Runnable();
        //注册指令和事件
        registerCmdOrListener();
        getLogger().info("§aPlugin enabled!");
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static ChatGPT getInstance() {
        return getPlugin(ChatGPT.class);
    }
    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        sendJson = new SendJson();
    }
    public void setSendJsonData(){
        sendJson.setMaxTokens(getConfig().getInt("APISet.Max_tokens"));
        sendJson.setTemperature(getConfig().getDouble("APISet.Temperature"));
    }

    public void registerCmdOrListener(){
        getCommand("chatgpt").setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new PlayerListener(),this);
    }
    public String getConnReply(String key,String msg){
        String reply = null;
        try {
            //配好连接和apikey
            URL url = new URL("https://api.openai.com/v1/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("Authorization", "Bearer "+key);
            conn.setDoOutput(true);
            /*
            给sendJson写入要发送的内容
            把它序列化成String并写入到输出流
            根据是否要带记忆的信息
             */
            sendJson.setPrompt(getConfig().getBoolean("APISet.Record")?
                    SendJson.getInformationWithRecords(msg)
                    :msg);
            String data = gson.toJson(sendJson);
            OutputStream ot = conn.getOutputStream();
            ot.write(data.getBytes("UTF-8"));
            //判断是否请求成功
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("请求失败,响应代码:"+ conn.getResponseCode());
            }
            //成功后获取输入流,获取返回的信息
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));
            StringBuilder line = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                line.append(output);
            }
            reply = line.toString();
            conn.disconnect();
            ot.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reply;
    }
    public String getTextInReply(String ogReply){
        JsonObject json = gson.fromJson(ogReply+"", JsonObject.class);
        String reply = json.get("choices").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
        /*
        因为获取过来的信息最前面总是有两个\n 也就是回车,所以要把它去掉
        如果开启了保留记忆发送,那么范围的就有可能出现AI:所以要替换成""
        */
        return reply.trim().replace("AI:","");
    }
    public String SendButtonInPlayers(String display,String hoverString,String clickString,String msg){
        String headString = msg.substring(0, msg.indexOf("{button}"));
        String tailString = msg.substring(msg.indexOf("{button}") + 8);
        String buttonText = headString+display+tailString;
        BaseComponent[] text = new ComponentBuilder("")
                .append(new ComponentBuilder(headString).create())
                .append(new ComponentBuilder(display)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,clickString))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder(hoverString).create()))
                        .create())
                .append(new ComponentBuilder(tailString).create())
                .create();
        for (Player player : getServer().getOnlinePlayers()) {
            player.spigot().sendMessage(text);
        }
        return buttonText;
    }
    public void GPT003Runnable(){
        new BukkitRunnable(){
            @Override
            public void run() {
                if (toBeSend.size() < 1)return;//没有内容就不处理了
                //做到不处理多条问题
                if (isHandle) return;
                isHandle = true;
                //获取待发送要发送的信息//并删除
                String sendMsg = toBeSend.get(0);
                toBeSend.remove(sendMsg);
                //获取网页请求返回原始json信息
                String ogReply = getConnReply(getConfig().getString("APISet.APIKey"),sendMsg+"\nAI:");
                //获取主要的回答内容
                String reply = getTextInReply(ogReply);
                //开了记忆对话就加入
                if (getConfig().getBoolean("APISet.Record")){
                    SendJson.record.add("Human:"+sendMsg+"\nAI:");
                    SendJson.record.add(reply+"\n\n");
                }
                //获取格式把内容填进去,并用到Button发送
                String format = getConfig().getString("ReplyFormat");
                reply = format.replace("&","§").replace("{reply}",reply);
                String buttonText = SendButtonInPlayers(
                        getConfig().getString("ChatGPTButton.display").replace("&","§")
                        ,getConfig().getString("ChatGPTButton.hover").replace("&","§")
                        ,getConfig().getString("ChatGPTButton.click").replace("&","§")
                        ,reply);
                //单独给后台发送
                getLogger().info(buttonText);
                isHandle = false;
            }
        }.runTaskTimerAsynchronously(this,0,60);
    }
}
