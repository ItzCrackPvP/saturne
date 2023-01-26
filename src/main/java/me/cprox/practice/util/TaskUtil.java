package me.cprox.practice.util;

import me.cprox.practice.Practice;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskUtil {
    public TaskUtil() {
    }

    public static void run(Runnable runnable) {
        Practice.get().getServer().getScheduler().runTask(Practice.get(), runnable);
    }

    public static void runTimer(Runnable runnable, long delay, long timer) {
        Practice.get().getServer().getScheduler().runTaskTimer(Practice.get(), runnable, delay, timer);
    }

    public static void runTimer(BukkitRunnable runnable, long delay, long timer) {
        runnable.runTaskTimer(Practice.get(), delay, timer);
    }

    public static void runLater(Runnable runnable, long delay) {
        Practice.get().getServer().getScheduler().runTaskLater(Practice.get(), runnable, delay);
    }

    public static void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread())
            runnable.run();
        else
            Bukkit.getScheduler().runTask(Practice.get(), runnable);
    }

    public static void runAsync(Runnable runnable) {
        if (Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTaskAsynchronously(Practice.get(), runnable);
        else
            runnable.run();
    }

    public static void runTimerAsync(Runnable runnable, long delay, long timer) {
        try {
            Practice.get().getServer().getScheduler().runTaskTimerAsynchronously(Practice.get(), runnable, delay, timer);
        } catch (IllegalStateException e) {
            Practice.get().getServer().getScheduler().runTaskTimer(Practice.get(), runnable, delay, timer);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static void runTimerAsync(BukkitRunnable runnable, long delay, long timer) {
        Practice.get().getServer().getScheduler().runTaskTimerAsynchronously(Practice.get(), runnable, delay, timer);
    }
    public interface Callable {
        void call();
    }
}
