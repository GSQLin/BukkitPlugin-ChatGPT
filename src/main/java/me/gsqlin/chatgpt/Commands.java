package me.gsqlin.chatgpt;

import me.gsqlin.chatgpt.data.SendJson;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {
    ChatGPT plugin = ChatGPT.getInstance();
    static List<CommandSender> senders = new ArrayList<>();

    String[] help = new String[]{
            "HELP↓",
            "help",
            "chat",
            "reload",
            "clean",
            "test"
    };
    @Override
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
        if (args.length >= 1){
            if (args[0].equalsIgnoreCase("chat")){
                if (!sender.hasPermission("chatgpt.chat")){
                    if (sender instanceof Player){
                        sender.sendMessage("§c你没有权限");
                        return false;
                    }
                }
                if (args.length >= 2){
                    //该部分是事件限制
                    if (plugin.getConfig().getBoolean("Limit.time.enable")){
                        if (!sender.hasPermission("chatgpt.unlimited")){
                            Player player = (Player) sender;
                            if (plugin.limitedPlayer.contains(player)){
                                player.sendMessage(plugin.getConfig().getString("Limit.time.message").replace("&","§"));
                                return false;
                            }else{
                                plugin.limitedPlayer.add(player);
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        plugin.limitedPlayer.remove(player);
                                    }
                                }.runTaskLater(plugin,
                                        plugin.getConfig().getInt("Limit.time.value") * 60 * 20);
                            }
                        }
                    }
                    //该部分是概率限制
                    if (plugin.getConfig().getBoolean("Limit.probability.enable")){
                        if (!sender.hasPermission("chatgpt.unlimited")){
                            double value = plugin.getConfig().getDouble("Limit.probability.value");
                            double rd = plugin.random.nextDouble();
                            if (rd <= value) return false;
                        }
                    }
                    String sendMsg = getMsg(args,1);
                    plugin.toBeSend.add(sendMsg);
                    senders.add(sender);
                }else{
                    sender.sendMessage("§c没有发送内容不能发送");
                }
                return false;
            }
            if (args[0].equalsIgnoreCase("reload")){
                if (!sender.hasPermission("chatgpt.reload")){
                    sender.sendMessage("§c你没有权限");
                    return false;
                }
                plugin.reload();
                sender.sendMessage("§a重载完成!");
                return false;
            }
            if (args[0].equalsIgnoreCase("clean")){
                if (!sender.hasPermission("chatgpt.clean")){
                    sender.sendMessage("§c你没有权限");
                    return false;
                }
                SendJson.record.clear();
                sender.sendMessage("§a已经清理了对话记录");
                return false;
            }
            if (args[0].equalsIgnoreCase("test")){
                sender.sendMessage("该指令用来测试的，本次测试是测试两句话直接的相似度的");
                sender.sendMessage(String.valueOf(plugin.getSimilarity(plugin.getParticiple(args[1]),plugin.getParticiple(args[2]))));
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
