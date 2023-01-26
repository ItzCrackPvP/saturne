package me.cprox.practice.util.timer.event;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import me.cprox.practice.Practice;
import me.cprox.practice.util.timer.Timer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TimerClearEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private final Optional<UUID> userUUID;
  private final Timer timer;
  private Optional<Player> player;

  public TimerClearEvent(Timer timer) {
    this.userUUID = Optional.empty();
    this.timer = timer;
  }

  public TimerClearEvent(UUID userUUID, Timer timer) {
    this.userUUID = Optional.of(userUUID);
    this.timer = timer;
  }

  public TimerClearEvent(Player player, Timer timer) {
    Objects.requireNonNull(player);

    this.player = Optional.of(player);
    this.userUUID = Optional.of(player.getUniqueId());
    this.timer = timer;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  public Optional<Player> getPlayer() {
    if (this.player == null)
      this.player = this.userUUID.isPresent() ? Optional.<Player>of(Practice.get().getServer().getPlayer(this.userUUID.get())) : Optional.<Player>empty();

    return this.player;
  }

  public Optional<UUID> getUserUUID() {
    return this.userUUID;
  }

  public Timer getTimer() {
    return this.timer;
  }


  public HandlerList getHandlers() {
    return HANDLERS;
  }
}