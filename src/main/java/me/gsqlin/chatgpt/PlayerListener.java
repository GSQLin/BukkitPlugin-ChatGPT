package me.gsqlin.chatgpt;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e){
        if (e.getMessage().contains("@ChatGPT")){
            e.getPlayer().performCommand("chatgpt chat "+ e.getMessage().replace("@ChatGPT",""));
        }
    }
}
