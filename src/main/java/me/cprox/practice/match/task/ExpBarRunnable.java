package me.cprox.practice.match.task;

import me.cprox.practice.Practice;
import me.cprox.practice.util.timer.event.impl.BridgeArrowTimer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ExpBarRunnable implements Runnable {

    private final Practice plugin = Practice.get();

    public void run() {
        BridgeArrowTimer bridgeArrowTimer = this.plugin.getTimerManager().getTimer(BridgeArrowTimer.class);

        for (UUID uuid : bridgeArrowTimer.getCooldowns().keySet()) {

            Player player = this.plugin.getServer().getPlayer(uuid);
            if (player != null) {
                long time = bridgeArrowTimer.getRemaining(player);
                int seconds = (int) Math.round(time / 1000.0D);
                player.setLevel(seconds);
                player.setExp((float) time / 4000.0F);
            }
        }
    }
}