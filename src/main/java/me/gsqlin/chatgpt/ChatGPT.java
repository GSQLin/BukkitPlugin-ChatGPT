package me.gsqlin.chatgpt;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class ChatGPT extends JavaPlugin{
    File data;
    FileListener listener;

    String javaSend = "!ThisIsJavaSend!!ThisGSQ!!!";

    String pythonSend = "!ThisIsPythonSend!!ThisGSQ!!!";
    @Override
    public void onLoad() {
        getLogger().info("§3插件加载中ing...");
        getLogger().info("§c温馨提示:本插件需要有python环境");
        getLogger().info("§c启动后前往本插件配置目录中运行ChatGPT.py文件");
        getLogger().info("§c第一次运行请在本插件配置目录下运行[pip/pip3] install -r requirements.txt");
    }

    @Override
    public void onEnable() {
        saveResource("data.txt",true);
        saveResource("ChatGPT.py",false);
        saveResource("requirements.txt",true);
        data = new File(getDataFolder().getAbsolutePath()+File.separator+"data.txt");
        getCommand("chatgpt").setExecutor(new Commands());
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
            msg = "§bChat§3GPT§7>>> §r"+ msg.replace(pythonSend,"");
            getLogger().info(msg);
            for (Player player : players) {
                player.sendMessage(msg);
            }
        });
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
}
