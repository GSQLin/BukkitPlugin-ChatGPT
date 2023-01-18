package me.gsqlin.chatgpt;

import org.apache.commons.io.FileUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;

public class Commands implements CommandExecutor {
    ChatGPT plugin = ChatGPT.getInstance();

    String[] help = new String[]{
            "HELP↓",
            "help",
            "chat",
            "setup",
            "reload",
            "clean"
    };
    @Override
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
        if (args.length >= 1){
            if (args[0].equalsIgnoreCase("chat")){
                if (args.length >= 2){
                    String Msg = getMsg(args,1);
                    String key = plugin.getConfig().getString("APISet.APIKey");
                    if (key == ""){
                        String bt = plugin.javaSend;
                        Msg += bt;
                        try {
                            String data = FileUtils.readFileToString(plugin.data,"UTF-8");
                            if (data.contains(bt)){
                                sender.sendMessage("§6我正在编辑>>>§r"+data.replace(bt,"")+"§6<<<这个问题的回答");
                            }else{
                                plugin.getLogger().info("§a往ChatGPT发送>>> "+Msg.replace(bt,""));
                                FileUtils.writeStringToFile(plugin.data,Msg,"UTF-8");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        plugin.getLogger().info("§a往ChatGPT发送>>> "+Msg);
                        plugin.msgs.add(Msg);
                    }
                }else{
                    sender.sendMessage("§c没有发送内容不能发送");
                }
                return false;
            }
            if (args[0].equalsIgnoreCase("setup")){
                if (args.length >= 2) {
                    String pip;
                    if (args[1].equalsIgnoreCase("3")){
                        pip = "pip3";
                    } else if (args[1].equalsIgnoreCase("2")) {
                        pip = "pip";
                    }else {
                        plugin.getLogger().info("§c只能填2或者3");
                        return false;
                    }
                    plugin.getLogger().info("开始执行");
                    try {
                        plugin.saveResource("requirements.txt",true);
                        Runtime.getRuntime().exec(pip+" install -r"+plugin.getDataFolder().getAbsolutePath()+File.separator+"requirements.txt");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return false;
                }
                sender.sendMessage("§c温馨提示>>> 在个一个2或者3的参数,根据你是python几");
                return false;
            }
            if (args[0].equalsIgnoreCase("reload")){
                plugin.reload();
                sender.sendMessage("§a重载完成!");
                return false;
            }
            if (args[0].equalsIgnoreCase("clean")){
                SendJson.record.clear();
                plugin.getLogger().info("§a已经清理了对话记录");
            }
            sender.sendMessage(help);
        }else{
            sender.sendMessage(help);
        }
        return false;
    }
    public String getMsg(String[] args,int i){
        StringBuilder builder = new StringBuilder();
        for (int x = i;x < args.length;x++){
            if (x == 0)builder.append(args[x]);
            builder.append(" "+args[x]);
        }
        return builder.toString();
    }
}
