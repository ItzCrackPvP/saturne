package me.cprox.practice.events.match;

import lombok.Getter;
import me.cprox.practice.match.Match;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class MatchEvent extends Event implements Cancellable {
    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();
    private final Match match;

    public MatchEvent(final Match match) {
        this.match = match;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean b) {
        this.cancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Match getMatch() {
        return this.match;
    }
}
