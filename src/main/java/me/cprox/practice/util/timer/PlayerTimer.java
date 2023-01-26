package me.cprox.practice.util.timer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import me.cprox.practice.Practice;
import me.cprox.practice.util.timer.event.TimerClearEvent;
import me.cprox.practice.util.timer.event.TimerExtendEvent;
import me.cprox.practice.util.timer.event.TimerPauseEvent;
import me.cprox.practice.util.timer.event.TimerStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class PlayerTimer extends Timer {
    protected final Map<UUID, TimerCooldown> cooldowns = new ConcurrentHashMap<>();

    public Map<UUID, TimerCooldown> getCooldowns() {
        return this.cooldowns;
    }

    public PlayerTimer(String name, long defaultCooldown) {
        super(name, defaultCooldown);
    }

    protected void handleExpiry(Player player, UUID playerUUID) {
        this.cooldowns.remove(playerUUID);
    }

    public TimerCooldown clearCooldown(UUID uuid) {
        return clearCooldown(null, uuid);
    }

    public TimerCooldown clearCooldown(Player player) {
        Objects.requireNonNull(player);
        return clearCooldown(player, player.getUniqueId());
    }

    public TimerCooldown clearCooldown(Player player, UUID playerUUID) {
        TimerCooldown runnable = this.cooldowns.remove(playerUUID);
        if (runnable != null) {
            runnable.cancel();
            if (player == null) {
                Practice.get().getServer().getPluginManager().callEvent((Event) new TimerClearEvent(playerUUID, this));
            } else {
                Practice.get().getServer().getPluginManager().callEvent((Event) new TimerClearEvent(player, this));
            }
        }

        return runnable;
    }

    public boolean isPaused(Player player) {
        return isPaused(player.getUniqueId());
    }

    public boolean isPaused(UUID playerUUID) {
        TimerCooldown runnable = this.cooldowns.get(playerUUID);
        return (runnable != null && runnable.isPaused());
    }

    public void setPaused(UUID playerUUID, boolean paused) {
        TimerCooldown runnable = this.cooldowns.get(playerUUID);
        if (runnable != null && runnable.isPaused() != paused) {
            TimerPauseEvent event = new TimerPauseEvent(playerUUID, this, paused);
            Bukkit.getPluginManager().callEvent((Event) event);
            if (!event.isCancelled()) {
                runnable.setPaused(paused);
            }
        }
    }

    public long getRemaining(Player player) {
        return getRemaining(player.getUniqueId());
    }

    public long getRemaining(UUID playerUUID) {
        TimerCooldown runnable = this.cooldowns.get(playerUUID);
        return (runnable == null) ? 0L : runnable.getRemaining();
    }

    public boolean setCooldown(Player player, UUID playerUUID) {
        return setCooldown(player, playerUUID, this.defaultCooldown, false);
    }

    public boolean setCooldown(Player player, UUID playerUUID, long duration, boolean overwrite) {
        return setCooldown(player, playerUUID, duration, overwrite, null);
    }

    public boolean setCooldown(Player player, UUID playerUUID, long duration, boolean overwrite, Predicate<Long> currentCooldownPredicate) {
        TimerCooldown runnable = (duration > 0L) ? this.cooldowns.get(playerUUID) : clearCooldown(player, playerUUID);
        if (runnable != null) {
            long remaining = runnable.getRemaining();
            if (!overwrite && remaining > 0L && duration <= remaining) {
                return false;
            }

            TimerExtendEvent event = new TimerExtendEvent(player, playerUUID, this, remaining, duration);
            Practice.get().getServer().getPluginManager().callEvent((Event) event);
            if (event.isCancelled()) {
                return false;
            }

            boolean flag = true;
            if (currentCooldownPredicate != null) {
                flag = currentCooldownPredicate.test(Long.valueOf(remaining));
            }
            if (flag) {
                runnable.setRemaining(duration);
            }

            return flag;
        }
        Practice.get().getServer().getPluginManager().callEvent((Event) new TimerStartEvent(player, playerUUID, this, duration));
         runnable = new TimerCooldown(this, playerUUID, duration);


        this.cooldowns.put(playerUUID, runnable);
        return true;
    }
}