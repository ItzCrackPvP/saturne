 package me.cprox.practice.util.timer.event.impl;

 import java.util.UUID;
 import java.util.concurrent.TimeUnit;

 import me.cprox.practice.profile.Profile;
 import me.cprox.practice.profile.enums.MatchState;
 import me.cprox.practice.util.chat.CC;
 import me.cprox.practice.util.external.ItemBuilder;
 import me.cprox.practice.util.timer.PlayerTimer;
 import org.apache.commons.lang.time.DurationFormatUtils;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.player.PlayerInteractEvent;

 public class BridgeArrowTimer extends PlayerTimer implements Listener {

     public BridgeArrowTimer() {
         super("Arrow", TimeUnit.SECONDS.toMillis(4L));
     }


     protected void handleExpiry(Player player, UUID playerUUID) {
         super.handleExpiry(player, playerUUID);

         if (player == null) {
             return;
         }

         if (!player.getInventory().contains(Material.ARROW)) {
             player.getInventory().setItem(8, (new ItemBuilder(Material.ARROW)).durability(0).amount(1).build());
         }
     }

     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {

         if ((event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) || !event.hasItem()) {
             return;
         }

         Player player = event.getPlayer();

         if (event.getItem().getType() == Material.ARROW || event.getItem().getType() == Material.BOW) {
             int seconds = Math.round(getRemaining(player)) / 1_000;
             long cooldown = getRemaining(player);

             if (cooldown > 0L) {

                 event.setCancelled(true);

                 player.sendMessage(CC.translate("&cYou must wait more " + DurationFormatUtils.formatDurationWords(cooldown, true, true) + " to shoot arrow!"));
                 player.setLevel(seconds);
                 player.setExp(getRemaining(player) / 16_000.0F);
                 player.updateInventory();
             }
         }
     }

     @EventHandler
     public void onArrowShoot(ProjectileLaunchEvent event) {
         if (event.getEntity().getShooter() instanceof Player &&
                 event.getEntity() instanceof org.bukkit.entity.Arrow) {
             Player player = (Player) event.getEntity().getShooter();
             Profile profile = Profile.getByUuid(player.getUniqueId());
             if (profile.isInFight() && profile.getMatch().getKit().getGameRules().isBridge()) {
                 if (profile.getMatch().getState().equals(MatchState.STARTING)) {
                     event.setCancelled(true);
                     return;
                 }
                 if (!profile.getMatch().getKit().getGameRules().isBridge()) {
                     return;
                 }
                 setCooldown(player, player.getUniqueId());
             }
         }
     }
 }