package me.gsqlin.chatgpt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatGPT extends JavaPlugin{
    File data;
    FileListener listener;

    List<String> msgs = new ArrayList<>();
    String javaSend = "!ThisIsJavaSend!!ThisGSQ!!!";

    String pythonSend = "!ThisIsPythonSend!!ThisGSQ!!!";

    Gson gson = new Gson();

    SendJson sendJson = new SendJson();
    @Override
    public void onLoad() {
        getLogger().info("§3插件加载中ing...");
        getLogger().info("§c温馨提示:本插件需要有python环境");
        getLogger().info("§c启动后前往本插件配置目录中运行ChatGPT.py文件");
        getLogger().info("§c第一次运行请在本插件配置目录下运行[pip/pip3] install -r requirements.txt");
    }

    @Override
    public void onEnable() {
        reload();
        data = new File(getDataFolder().getAbsolutePath()+File.separator+"data.txt");
        getCommand("chatgpt").setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new PlayerListener(),this);
        listener = new FileListener(getDataFolder().getAbsolutePath(),"data.txt",null);
        listener.setRun(()->{
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            String msg;
            try {
                msg = FileUtils.readFileToString(data,"UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (msg.contains(javaSend)) return;
            String reply = getConfig().getString("ReplyFormat")
                    .replace("{reply}",msg.replace(pythonSend,""))
                    .replace("&","§");
            getLogger().info(reply);
            for (Player player : players) {
                player.sendMessage(reply);
            }
        });
        new BukkitRunnable(){
            @Override
            public void run() {
                if (msgs.size() >= 1){
                    String send = msgs.get(0);
                    msgs.remove(send);
                    String reply = gpt003Send(getConfig().getString("APISet.APIKey"),send);
                    getLogger().info(reply);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(getConfig().getString("ReplyFormat")
                                .replace("&","§")
                                .replace("{reply}",reply).replace("\n",""));
                    }
                }
            }
        }.runTaskTimerAsynchronously(this,0,20);
        getLogger().info("§a插件已载入");
    }

    @Override
    public void onDisable() {
        for (FileAlterationMonitor monitor : FileListener.monitors) {
            try {
                monitor.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ChatGPT getInstance() {
        return ((ChatGPT) Bukkit.getServer().getPluginManager().getPlugin("ChatGPT"));
    }

    public String gpt003Send(String key,String msg){
        String originalReply = null;
        try {
            URL url = new URL("https://api.openai.com/v1/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("Authorization", "Bearer "+key);
            conn.setDoOutput(true);

            if (getConfig().getBoolean("APISet.Record")){
                SendJson.record.add("Player:" + msg);
                msg = SendJson.getInformationWithRecords();
            }
            sendJson.setPrompt(msg);
            sendJson.setTemperature(getConfig().getDouble("APISet.Temperature"));
            sendJson.setMaxTokens(getConfig().getInt("APISet.Maximum-length"));
            String data = gson.toJson(sendJson);
            OutputStream ot = conn.getOutputStream();
            ot.write(data.getBytes("UTF-8"));
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code :继续"+ conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));
            StringBuilder line = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                line.append(output);
            }
            originalReply = line.toString();
            conn.disconnect();
            ot.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("原句:"+originalReply);
        JsonObject json = gson.fromJson(originalReply+"", JsonObject.class);
        String reply = json.get("choices").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
        if (getConfig().getBoolean("APISet.Record")) SendJson.record.add("GPT:"+reply);
        return reply;
    }

    public void reload(){
        saveDefaultConfig();
        saveResource("data.txt",true);
        saveResource("ChatGPT.py",false);
        saveResource("requirements.txt",true);
    }
}
