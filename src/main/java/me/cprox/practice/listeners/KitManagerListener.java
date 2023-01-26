package me.cprox.practice.listeners;

import me.cprox.practice.kit.Kit;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.meta.StatisticsData;
import me.cprox.practice.util.chat.CC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class KitManagerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        if (profile.getKitManager().isManagingKnockback()) {
            event.setCancelled(true);

            profile.getKitManager().getSelectedKit().setKnockbackProfile(event.getMessage());
            profile.getKitManager().setActive(false);
            profile.getKitManager().setKnockback(false);
            profile.getKitManager().setSelectedKit(null);
            event.getPlayer().closeInventory();
            event.getPlayer().sendMessage(CC.translate("&4" + profile.getKitManager().getSelectedKit().getName() + "&f's knockback profile has been updated to the &4" + event.getMessage()));
        }
        if (profile.getKitManager().isCreating()) {
            event.setCancelled(true);
            Kit kit = new Kit(event.getMessage());
            kit.save();
            Kit.getKits().add(kit);
            kit.setEnabled(true);
            kit.getGameRules().setRanked(false);
            for ( Profile profilee : Profile.getProfiles().values() ) {
                profilee.getStatisticsData().put(kit, new StatisticsData());
            }
            profile.getKitManager().setActive(false);
            profile.getKitManager().setCreating(false);
            profile.getKitManager().setSelectedKit(null);
            event.getPlayer().closeInventory();
            event.getPlayer().sendMessage(CC.translate("&aKit " + event.getMessage() + " has been created successfully"));
        }
        if (profile.getKitManager().isRenaming()) {
            event.setCancelled(true);
            profile.getKitManager().setActive(false);
            profile.getKitManager().setRenaming(false);
            profile.getKitManager().setSelectedKit(null);
            profile.getKitManager().getSelectedKit().setDisplayName(event.getMessage());
            event.getPlayer().closeInventory();
            event.getPlayer().sendMessage(CC.translate("&4" + profile.getKitManager().getSelectedKit().getName() + "&f has been renamed to a &4" + event.getMessage()));
        }
    }
}
