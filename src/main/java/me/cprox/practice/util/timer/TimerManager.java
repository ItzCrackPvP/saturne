package me.cprox.practice.util.timer;

import java.util.LinkedHashSet;
import java.util.Set;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class TimerManager implements Listener {
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TimerManager)) {
            return false;
        }
        final TimerManager other = (TimerManager) o;
        if (!other.canEqual(this)) {
            return false;
        }
        final Object this$timers = this.getTimers();
        final Object other$timers = other.getTimers();
        Label_0065:
        {
            if (this$timers == null) {
                if (other$timers == null) {
                    break Label_0065;
                }
            } else if (this$timers.equals(other$timers)) {
                break Label_0065;
            }
            return false;
        }
        final Object this$plugin = this.getPlugin();
        final Object other$plugin = other.getPlugin();
        if (this$plugin == null) {
            return other$plugin == null;
        } else return this$plugin.equals(other$plugin);
    }

    protected boolean canEqual(Object other) {
        return other instanceof TimerManager;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $timers = this.getTimers();
        result = result * 59 + (($timers == null) ? 0 : $timers.hashCode());
        final Object $plugin = this.getPlugin();
        result = result * 59 + (($plugin == null) ? 0 : $plugin.hashCode());
        return result;
    }

    public String toString() {
        return "TimerManager(timers=" + getTimers() + ", plugin=" + getPlugin() + ")";
    }

    private final Set<Timer> timers = new LinkedHashSet<>();

    private final JavaPlugin plugin;

    public Set<Timer> getTimers() {
        return this.timers;
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    public TimerManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerTimer(Timer timer) {
        this.timers.add(timer);
        if (timer instanceof Listener)
            this.plugin.getServer().getPluginManager().registerEvents((Listener)timer, (Plugin)this.plugin);
    }

    public void unregisterTimer(Timer timer) {
        this.timers.remove(timer);
    }

    public <T extends Timer> T getTimer(Class<T> timerClass) {
        for (Timer timer : this.timers) {
            if (timer.getClass().equals(timerClass))
                return (T)timer;
        }
        return null;
    }
}
