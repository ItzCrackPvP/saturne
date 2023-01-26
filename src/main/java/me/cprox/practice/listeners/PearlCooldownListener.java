package me.cprox.practice.listeners;

import me.cprox.practice.Practice;
import me.cprox.practice.events.match.MatchEndEvent;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class PearlCooldownListener implements Listener {

    private static final long PEARL_COOLDOWN_MILLIS = TimeUnit.SECONDS.toMillis(16);

    private final Map<UUID, Long> pearlCooldown = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() != EntityType.ENDER_PEARL) {
            return;
        }

        EnderPearl pearl = (EnderPearl) event.getEntity();
        Player player = (Player) pearl.getShooter();

        Profile profile = Profile.getByUuid(player);

        if (profile.isInFight() || profile.isInEvent()) {
            Match match = profile.getMatch();
            Kit kit = match.getKit();

            if (!kit.getGameRules().isPearlFight() || profile.isInBrackets()) {
                pearlCooldown.put(player.getUniqueId(), System.currentTimeMillis() + PEARL_COOLDOWN_MILLIS);

                new BukkitRunnable() {
                    public void run() {
                        long cooldownExpires = pearlCooldown.getOrDefault(player.getUniqueId(), 0L);

                        if (cooldownExpires < System.currentTimeMillis()) {
                            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.PEARL_EXPIRED"));
                            cancel();
                            return;
                        }

                        if (profile.getState() == ProfileState.IN_LOBBY) {
                            cancel();
                            return;
                        }

                        int millisLeft = (int) (cooldownExpires - System.currentTimeMillis());
                        float percentLeft = (float) millisLeft / PEARL_COOLDOWN_MILLIS;

                        player.setExp(percentLeft);
                        player.setLevel(millisLeft / 1_000);
                    }

                }.runTaskTimer(Practice.get(), 1L, 1L);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || event.getItem().getType() != Material.ENDER_PEARL || !event.getAction().name().contains("RIGHT_")) {
            return;
        }

        Player player = event.getPlayer();
        long cooldownExpires = pearlCooldown.getOrDefault(player.getUniqueId(), 0L);

        if (cooldownExpires < System.currentTimeMillis()) {
            return;
        }

        int millisLeft = (int) (cooldownExpires - System.currentTimeMillis());
        double duration = millisLeft / 1000D;
        duration = Math.round(10D * duration) / 10D;

        event.setCancelled(true);
        player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.PEARL_DELAY").replace("<duration>", String.valueOf(duration)));
        player.updateInventory();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        pearlCooldown.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        for (EnderPearl pearl : player.getWorld().getEntitiesByClass(EnderPearl.class)) {
            if (pearl.getShooter() == player) {
                pearl.remove();
            }
        }

        pearlCooldown.remove(player.getUniqueId());
    }

    @EventHandler
    public void onEnd(MatchEndEvent event) {
        for (TeamPlayer team : event.getMatch().getTeamPlayers()) {
            pearlCooldown.remove(team.getPlayer().getUniqueId());
        }
    }
}