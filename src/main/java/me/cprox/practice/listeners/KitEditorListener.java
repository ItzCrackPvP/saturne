package me.cprox.practice.listeners;

import me.cprox.practice.Practice;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.menu.kiteditor.KitManagementMenu;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.CraftingInventory;

public class KitEditorListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        Kit kit = profile.getKitEditor().getSelectedKit();

        if (profile.getKitEditor().isRenaming()) {
            event.setCancelled(true);

            if (event.getMessage().length() > 16) {
                event.getPlayer().sendMessage(CC.translate("&cA kit name cannot be more than 16 characters!"));
                return;
            }

            profile.getKitEditor().getSelectedKitInventory().setCustomName(CC.translate(event.getMessage()));
            event.getPlayer().sendMessage(CC.translate("&4" + profile.getKitEditor().getSelectedKit().getName() + "&f's kit has been &5renamed &fto the &4" + event.getMessage()));
            profile.getKitEditor().setActive(false);
            profile.getKitEditor().setRename(false);
            profile.save();

            new KitManagementMenu(kit).openMenu(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
                event.setCancelled(false);
            }
            if (!profile.getKitEditor().isActive() && profile.isInLobby() || profile.isInQueue()) {
                event.setCancelled(true);
            }

            if (profile.getKitEditor().getSelectedKit() != null) {
                event.setCancelled(false);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (event.getInventory().getTitle().contains("Editing")) {
            profile.getKitEditor().setActive(false);

            if (!profile.isInFight()) {
                player.getInventory().clear();
                player.updateInventory();
                Bukkit.getScheduler().scheduleSyncDelayedTask(Practice.get(), profile::refreshHotbar, 1L);
            }

            player.updateInventory();
        }
    }
}