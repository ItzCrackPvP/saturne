package me.cprox.practice.util.external;

import me.cprox.practice.arena.impl.StandaloneArena;
import me.cprox.practice.match.impl.solo.SoloBattleRushMatch;
import me.cprox.practice.match.impl.solo.SoloBridgeMatch;
import me.cprox.practice.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class LocationUtil {

    private String world;

    private double x;
    private double y;
    private double z;

    private float yaw;
    private float pitch;

    public LocationUtil() {
    }


    public static String serialize(Location location) {
        return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ() +
                ":" + location.getYaw() + ":" + location.getPitch();
    }

    public Location toBukkitLocation() {
        return new Location(this.toBukkitWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public World toBukkitWorld() {
        if (this.world == null) {
            return Bukkit.getServer().getWorlds().get(0);
        } else {
            return Bukkit.getServer().getWorld(this.world);
        }
    }

    public static Location deserialize(String source) {
        if (source == null) {
            return null;
        }

        String[] split = source.split(":");
        World world = Bukkit.getServer().getWorld(split[0]);

        if (world == null) {
            return null;
        }

        return new Location(world, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }

    public static boolean isTeamPortalBridge(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());
        SoloBridgeMatch match = (SoloBridgeMatch) profile.getMatch();

        StandaloneArena arena = (StandaloneArena) match.getArena();

        if (match.getTeamPlayerA().getPlayer() == player) {
            return arena.getRedCuboid().contains(player.getLocation());
        } else {
            return arena.getBlueCuboid().contains(player.getLocation());
        }
    }

    public static boolean isTeamPortalBattleRush(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());
        SoloBattleRushMatch match = (SoloBattleRushMatch) profile.getMatch();

        StandaloneArena arena = (StandaloneArena) match.getArena();

        if (match.getTeamPlayerA().getPlayer() == player) {
            return arena.getRedCuboid().contains(player.getLocation());
        } else {
            return arena.getBlueCuboid().contains(player.getLocation());
        }
    }
}
