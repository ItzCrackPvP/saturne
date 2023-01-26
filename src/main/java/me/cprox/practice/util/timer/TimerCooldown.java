package me.cprox.practice.util.timer;

import java.util.UUID;

import me.cprox.practice.Practice;
import me.cprox.practice.util.timer.event.TimerExpireEvent;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TimerCooldown {
    private final Timer timer;
    private final UUID owner;
    private BukkitTask eventNotificationTask;
    private long expiryMillis;
    private long pauseMillis;

    public Timer getTimer() {
        return this.timer;
    }

    public long getExpiryMillis() {
        return this.expiryMillis;
    }

    public long getPauseMillis() {
        return this.pauseMillis;
    }

    protected void setPauseMillis(long pauseMillis) {
        this.pauseMillis = pauseMillis;
    }

    protected TimerCooldown(Timer timer, long duration) {
        this.owner = null;
        this.timer = timer;
        setRemaining(duration);
    }

    protected TimerCooldown(Timer timer, UUID playerUUID, long duration) {
        this.timer = timer;
        this.owner = playerUUID;
        setRemaining(duration);
    }

    public long getRemaining() {
        return getRemaining(false);
    }

    protected void setRemaining(long milliseconds) throws IllegalStateException {
        if (milliseconds <= 0L) {
            cancel();

            return;
        }
        long expiryMillis = System.currentTimeMillis() + milliseconds;
        if (expiryMillis != this.expiryMillis) {
            this.expiryMillis = expiryMillis;

            if (this.eventNotificationTask != null) {
                this.eventNotificationTask.cancel();
            }

            long ticks = milliseconds / 50L;
            this.eventNotificationTask = (new BukkitRunnable() {
                public void run() {
                    if (TimerCooldown.this.timer instanceof PlayerTimer && TimerCooldown.this.owner != null)
                        ((PlayerTimer) TimerCooldown.this.timer).handleExpiry(Practice.get().getServer().getPlayer(TimerCooldown.this.owner), TimerCooldown.this.owner);
                    Practice.get().getServer().getPluginManager().callEvent((Event) new TimerExpireEvent(TimerCooldown.this.owner, TimerCooldown.this.timer));
                }
            }).runTaskLaterAsynchronously((Plugin) JavaPlugin.getProvidingPlugin(getClass()), ticks);
        }
    }

    protected long getRemaining(boolean ignorePaused) {
        if (!ignorePaused && this.pauseMillis != 0L) {
            return this.pauseMillis;
        }
        return this.expiryMillis - System.currentTimeMillis();
    }


    protected boolean isPaused() {
        return (this.pauseMillis != 0L);
    }

    public void setPaused(boolean paused) {
        if (paused != isPaused()) {
            if (paused) {
                this.pauseMillis = getRemaining(true);
                cancel();
            } else {
                setRemaining(this.pauseMillis);
                this.pauseMillis = 0L;
            }
        }
    }

    protected void cancel() throws IllegalStateException {
        if (this.eventNotificationTask != null) {
            this.eventNotificationTask.cancel();
            this.eventNotificationTask = null;
        }
    }
}