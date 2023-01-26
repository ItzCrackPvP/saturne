package me.cprox.practice.util.essentials.event;

import lombok.Getter;
import lombok.Setter;
import me.cprox.practice.util.external.BaseEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class RoomTeleportEvent extends BaseEvent implements Cancellable {

    @Getter
    private final Player player;
    @Getter @Setter
    private Location location;
    @Getter @Setter private boolean cancelled;

    public RoomTeleportEvent(Player player, Location location) {
        this.player = player;
        this.location = location;
    }

}
