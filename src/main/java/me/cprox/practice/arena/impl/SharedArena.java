package me.cprox.practice.arena.impl;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.profile.enums.ArenaType;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;

public class SharedArena extends Arena {

    public SharedArena(String name, Location min, Location max) {
        super(name, min, max);
    }

    @Override
    public ArenaType getType() {
        return ArenaType.SHARED;
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
        configuration.set(path + ".min", LocationUtil.serialize(getLowerCorner()));
        configuration.set(path + ".max", LocationUtil.serialize(getUpperCorner()));
        configuration.set(path + ".kits", getKits());

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