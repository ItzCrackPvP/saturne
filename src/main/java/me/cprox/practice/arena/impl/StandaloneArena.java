package me.cprox.practice.arena.impl;

import lombok.Getter;
import lombok.Setter;
import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.arena.cuboid.Cuboid;
import me.cprox.practice.profile.enums.ArenaType;
import me.cprox.practice.util.external.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class StandaloneArena extends Arena {
    private final List<Arena> duplicates = new ArrayList<>();

    @Setter private Cuboid redCuboid;
    @Setter private Cuboid blueCuboid;

    @Setter private Location redBed;
    @Setter private Location blueBed;

    public List<Arena> getDuplicates() {
        return this.duplicates;
    }

    public StandaloneArena(String name, Location location1, Location location2) {
        super(name, location1, location2);
    }

    public ArenaType getType() {
        return ArenaType.STANDALONE;
    }

    @Override
    public void save() {
        String path = "arenas." + getName();

        FileConfiguration configuration = Practice.get().getArenasConfig().getConfiguration();
        configuration.set(path, null);
        configuration.set(path + ".type", getType().name());
        configuration.set(path + ".icon.material", displayIcon.getType().name());
        configuration.set(path + ".icon.durability", displayIcon.getDurability());
        configuration.set(path + ".spawn1", LocationUtil.serialize(spawn1));
        configuration.set(path + ".spawn2", LocationUtil.serialize(spawn2));
        configuration.set(path + ".min", LocationUtil.serialize(min));
        configuration.set(path + ".max", LocationUtil.serialize(max));
        configuration.set(path + ".kits", getKits());
        if (this.redCuboid != null) {
            configuration.set(path + ".redportal.location1", LocationUtil.serialize(this.redCuboid.getLowerCorner()));
            configuration.set(path + ".redportal.location2", LocationUtil.serialize(this.redCuboid.getUpperCorner()));
        }
        if (this.blueCuboid != null) {
            configuration.set(path + ".blueportal.location1", LocationUtil.serialize(this.blueCuboid.getLowerCorner()));
            configuration.set(path + ".blueportal.location2", LocationUtil.serialize(this.blueCuboid.getUpperCorner()));
        }
        if (this.redBed != null) {
            configuration.set(path + ".redbed.location", LocationUtil.serialize(this.redBed.getBlock().getLocation()));
        }
        if (this.blueBed != null) {
            configuration.set(path + ".bluebed.location", LocationUtil.serialize(this.blueBed.getBlock().getLocation()));
        }
        if (!this.duplicates.isEmpty()) {
            int i = 0;
            for (Arena duplicate : this.duplicates) {
                i++;
                configuration.set(path + ".duplicates." + i + ".cuboid.location1", LocationUtil.serialize(duplicate.getLowerCorner()));
                configuration.set(path + ".duplicates." + i + ".cuboid.location2", LocationUtil.serialize(duplicate.getUpperCorner()));
                configuration.set(path + ".duplicates." + i + ".spawnA", LocationUtil.serialize(duplicate.getSpawn1()));
                configuration.set(path + ".duplicates." + i + ".spawnB", LocationUtil.serialize(duplicate.getSpawn2()));
            }
        }
        try {
            configuration.save(Practice.get().getArenasConfig().getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        super.delete();
        YamlConfiguration configuration = Practice.get().getArenasConfig().getConfiguration();
        configuration.set("arenas." + getName(), null);
        try {
            configuration.save(Practice.get().getArenasConfig().getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}