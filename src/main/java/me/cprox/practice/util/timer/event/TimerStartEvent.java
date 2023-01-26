package me.cprox.practice.util.timer.event;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import me.cprox.practice.util.timer.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TimerStartEvent
  extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Optional<Player> player;
    private final Optional<UUID> userUUID;
    private final Timer timer;
    private final long duration;

    public TimerStartEvent(Timer timer, long duration) {
        this.player = Optional.empty();
        this.userUUID = Optional.empty();
        this.timer = timer;
        this.duration = duration;
    }

    public TimerStartEvent(@Nullable Player player, UUID uniqueId, Timer timer, long duration) {
        this.player = Optional.ofNullable(player);
        this.userUUID = Optional.ofNullable(uniqueId);
        this.timer = timer;
        this.duration = duration;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Optional<Player> getPlayer() {
        return this.player;
    }

    public Optional<UUID> getUserUUID() {
        return this.userUUID;
    }

    public Timer getTimer() {
        return this.timer;
    }

    public long getDuration() {
        return this.duration;
    }


    public HandlerList getHandlers() {
        return HANDLERS;
    }
}