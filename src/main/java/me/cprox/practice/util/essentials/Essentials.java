package me.cprox.practice.util.essentials;

import lombok.Getter;
import lombok.Setter;
import me.cprox.practice.Practice;
import me.cprox.practice.arena.cuboid.Cuboid;
import me.cprox.practice.util.bootstrap.Bootstrapped;
import me.cprox.practice.util.essentials.event.SpawnTeleportEvent;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.external.LocationUtil;
import org.bukkit.Location;
import me.cprox.practice.profile.Profile;
import org.bukkit.entity.Player;

import java.io.IOException;

public class Essentials extends Bootstrapped {

    private Location spawn;

    @Getter @Setter private Cuboid packShowcase;

    public Essentials(Practice Practice) {
        super(Practice);

        spawn = LocationUtil.deserialize(Practice.getMainConfig().getStringOrDefault("Practice.Spawn", null));
    }

    public void setSpawn(Location location) {
        spawn = location;

        Practice.getMainConfig().getConfiguration().set("Practice.Spawn", LocationUtil.serialize(this.spawn));

        try {
            Practice.getMainConfig().getConfiguration().save(Practice.getMainConfig().getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void teleportToSpawn(Player player) {
        Location location = spawn;

        SpawnTeleportEvent event = new SpawnTeleportEvent(player, location);
        event.call();

        if (!event.isCancelled() && event.getLocation() != null) {
            player.teleport(event.getLocation());
        }

        PlayerUtil.reset(player);
        Profile profile = Profile.getByUuid(player.getPlayer());
        profile.refreshHotbar();
    }
}