package me.gsqlin.chatgpt;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {
    ChatGPT plugin = ChatGPT.getInstance();

    String[] help = new String[]{
            "HELP↓",
            "help",
            "chat",
            "reload",
            "clean"
    };
    @Override
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
        if (!sender.isOp()){
            sender.sendMessage("§c你没有权限!");
            return false;
        }
        if (args.length >= 1){
            if (args[0].equalsIgnoreCase("chat")){
                if (args.length >= 2){
                    String sendMsg = getMsg(args,1);
                    plugin.toBeSend.add(sendMsg);
                    sender.sendMessage("§a发送:"+ sendMsg);
                }else{
                    sender.sendMessage("§c没有发送内容不能发送");
                }
                return false;
            }
            if (args[0].equalsIgnoreCase("reload")){
                plugin.reload();
                sender.sendMessage("§a重载完成!");
                return false;
            }
            if (args[0].equalsIgnoreCase("clean")){
                SendJson.record.clear();
                sender.sendMessage("§a已经清理了对话记录");
                return false;
            }
            sender.sendMessage(help);
        }else{
            sender.sendMessage(help);
        }
        return false;
    }
    public String getMsg(String[] args,int i){
        StringBuilder builder = new StringBuilder();
        int x = i;
        boolean b = false;
        do {
            if (b){builder.append(" "+args[x]);
            }else{builder.append(args[x]);}
            if (!b) b = true;
            x++;
        }while (x < args.length);
        return builder.toString();
    }
}
