package me.cprox.practice.listeners;

import me.cprox.practice.Practice;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (event.getMessage().startsWith("?") || event.getMessage().startsWith("!")) {
            if (profile.getParty() != null) {
                event.setCancelled(true);
                profile.getParty().broadcast(CC.RESET + " " + event.getPlayer().getName() + CC.RESET + ": " + ChatColor.stripColor(event.getMessage().substring(1)));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Profile profile = Profile.getProfiles().get(event.getPlayer().getUniqueId());

        if (profile != null) {
            if (profile.getParty() != null) {
                if (profile.getParty().isLeader(event.getPlayer().getUniqueId())) {
                    profile.getParty().disband();
                } else {
                    profile.getParty().leave(event.getPlayer(), false);
                }
            }
        }
    }

}
