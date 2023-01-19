package me.gsqlin.chatgpt;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerListener implements Listener {
    ChatGPT plugin = ChatGPT.getInstance();
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
        String keyword = plugin.getConfig().getString("ChatSendGPT.keyword");
        if (e.getMessage().contains(keyword)){
            e.getPlayer().performCommand("chatgpt chat "+ e.getMessage().replace(keyword,""));
            e.setMessage(e.getMessage().replace(keyword,plugin.getConfig().getString("ChatSendGPT.replace")
                    .replace("&","§")));
        }
    }
}
