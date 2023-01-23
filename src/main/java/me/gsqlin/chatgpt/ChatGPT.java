package me.gsqlin.chatgpt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.gsqlin.chatgpt.data.CustomReply;
import me.gsqlin.chatgpt.data.SendJson;
import me.gsqlin.chatgpt.util.ParamParser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class ChatGPT extends JavaPlugin {
    BukkitRunnable regularCleanRunnable;
    Random random; //不想重复实例随机工具
    Gson gson = new Gson();
    SendJson sendJson;
    ArrayList<String> toBeSend = new ArrayList<>(); //待发送的列表
    Boolean isHandle = false;
    ArrayList<Player> limitedPlayer = new ArrayList<>();
    Analyzer analyzer;

    @Override
    public void onLoad() {
        getLogger().info("§a初始化所需参数");
        sendJson = new SendJson();
        random = new Random();
        analyzer = new IKAnalyzer(true);
        try {
            analyzer.tokenStream("null","null").close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getLogger().info("§a初始化完成");
    }

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
        analyzer.close();
    }
    public static ChatGPT getInstance() {
        return getPlugin(ChatGPT.class);
    }
    public void reload() {
        saveDefaultConfig();
        reloadConfig();
        sendJson = new SendJson();
        //判断关闭定时清理，然后判断是否开了定时清理后在创建一个
        if (regularCleanRunnable != null) regularCleanRunnable.cancel();
        if (getConfig().getBoolean("RegularClean.enable"))RegularClean();
        CustomReply.customReplyList.clear();
        for (String s : getConfig().getStringList("CustomReply")) {
            //CustomReply已经自动加到静态list
            new CustomReply(new ParamParser(s));
        }
        analyzer.close();
        analyzer = new IKAnalyzer(true);
        try {
            analyzer.tokenStream("null","null").close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        isHandle = false;
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
                //判断待发送内容和是否正在发送中
                if (toBeSend.size() < 1 || isHandle) return;
                //设置正在发送中
                isHandle = true;
                //获取后就删除
                String sendMsg = toBeSend.get(0);
                toBeSend.remove(sendMsg);
                //获取格式
                String format = getConfig().getString("ReplyFormat");
                //进行分词判断
                Set<String> set1 = getParticiple(sendMsg);
                CustomReply customReply = null;
                for (CustomReply cly : CustomReply.customReplyList) {
                    Set<String> set2 = getParticiple(cly.getValue());
                    //判断相似度
                    if (getSimilarity(set1,set2) >= cly.getSimilarity()){
                        customReply = cly;
                        break;
                    }
                }
                //有相似的进行判断类型
                boolean needSetReply = false;
                String consoleMsg;
                if (customReply != null){
                    String command = customReply.getCommand();
                    if (customReply.getType().equals(CustomReply.TYPE.CACHE)){
                        if (customReply.getReply() != null){
                            consoleMsg = SendInPlayer(format,customReply.getReply());
                            getLogger().info(consoleMsg);
                            isHandle = false;
                            return;
                        }else needSetReply = true;
                    }
                    if (customReply.getType().equals(CustomReply.TYPE.CUSTOM)){
                        consoleMsg = SendInPlayer(format,customReply.getReply());
                        getLogger().info(consoleMsg);
                        isHandle = false;
                        return;
                    }
                    //执行命令
                    if (command != null){
                        CommandSender sender = Commands.senders.get(0);
                        Bukkit.dispatchCommand(sender,command
                                .replace("{PLAYER}",sender.getName()));
                    }
                }
                //如果上面两种类型都没发送就会运行以下内容了
                String ogReply = getConnReply(getConfig().getString("APISet.APIKey"),sendMsg+"\nAI:");
                String reply = getTextInReply(ogReply);
                //发送信息然后获取网页内容后获取主要信息,之后判断是否开启记录功能并记录
                if (getConfig().getBoolean("APISet.Record")){
                    SendJson.record.add("Human:"+sendMsg+"\nAI:");
                    SendJson.record.add(reply+"\n\n");
                }
                //判断是否需要缓存并缓存
                if (needSetReply){
                    CustomReply.customReplyList.remove(customReply);
                    customReply.setReply(reply);
                    CustomReply.customReplyList.add(customReply);
                }
                //获取发送格式，替换颜色以及内容，在SendInPlayer中实现了发送给所有玩家带按钮格式的信息并返回原String信息
                consoleMsg = SendInPlayer(format,reply);
                //获取原信息后单独给后台发一份
                getLogger().info(consoleMsg);
                isHandle = false;
            }
        }.runTaskTimerAsynchronously(this,0,60);
    }
    //专门用来清理的记录的
    public void RegularClean(){
        regularCleanRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                SendJson.record.clear();
                for (Player player : getServer().getOnlinePlayers()) {
                    player.sendMessage(getConfig().getString("RegularClean.message").replace("&","§"));
                }
            }
        };
        regularCleanRunnable.runTaskTimer(this,0,
                20*60*getConfig().getInt("RegularClean.time"));
    }
    //获取分词
    public Set<String> getParticiple(String text){
        Set<String> set = new HashSet<>();
        try {
            TokenStream ik = analyzer.tokenStream("null",new StringReader(text));
            CharTermAttribute term = ik.addAttribute(CharTermAttribute.class);
            ik.reset();
            while (ik.incrementToken()) {
                set.add(term.toString());
            }
            ik.close();
        }catch (Exception e){
            new RuntimeException(e);
        }
        return set;
    }
    //获取相似度
    public float getSimilarity(Set<String> set1,Set<String> set2){
        float our = set1.stream().filter(set2::contains).count();
        float q = our / (set1.size() + set2.size() - our);
        return q;
    }
    public String SendInPlayer(String format,String reply){
        reply = format.replace("&","§").replace("{reply}",reply);
        return SendButtonInPlayers(
                getConfig().getString("ChatGPTButton.display").replace("&","§")
                ,getConfig().getString("ChatGPTButton.hover").replace("&","§")
                ,getConfig().getString("ChatGPTButton.click").replace("&","§")
                ,reply);
    }
}
