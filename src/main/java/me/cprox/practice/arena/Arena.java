package me.cprox.practice.arena;

import com.google.common.collect.Maps;
import me.cprox.practice.Practice;
import me.cprox.practice.arena.cuboid.Cuboid;
import me.cprox.practice.arena.impl.*;
import me.cprox.practice.profile.enums.ArenaType;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.external.LocationUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter @Setter
public class Arena extends Cuboid {

    @Getter private static final List<Arena> arenas = new ArrayList<>();
    protected String name;
    @Getter @Setter private static boolean pasting;
    protected boolean active, duplicate;
    @Setter protected Location spawn1, spawn2, min, max;
    int deadZone = 7;
    private List<String> kits = new ArrayList<>();
    public org.bukkit.inventory.ItemStack displayIcon;
    private Double rating;

    private final transient Map<Long, ChunkSnapshot> chunkSnapshots = Maps.newHashMap();

    public Double getRating() {
        return this.rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    List<Integer> ratingList = new ArrayList<>();

    public List<Integer> getRatingList() {
        return this.ratingList;
    }

    public void setRatingList(List<Integer> ratingList) {
        this.ratingList = ratingList;
    }

    public List<String> getKits() {
        return this.kits;
    }

    public void setKits(List<String> kits) {
        this.kits = kits;
    }

    public static List<Arena> getSetupArenas(String kit) {
        return arenas.stream().filter(arena -> arena.getKits().contains(kit)).collect(Collectors.toList());
    }

    public Arena(String name, Location location1, Location location2) {
        super(location1, location2);
        this.name = name;
        this.displayIcon = new ItemStack(Material.GRASS);
    }

    public void setDisplayIcon(ItemStack displayIcon) {
        this.displayIcon = displayIcon;
    }

    public static void preload() {
        FileConfiguration configuration = Practice.get().getArenasConfig().getConfiguration();

        if (configuration.contains("arenas")) {
            if (configuration.getConfigurationSection("arenas") == null) return;
            for (String arenaName : configuration.getConfigurationSection("arenas").getKeys(false)) {
                String path = "arenas." + arenaName;
                ArenaType arenaType = ArenaType.valueOf(configuration.getString(path + ".type"));
                Arena arena;
                Location location1 = LocationUtil.deserialize(configuration.getString(path + ".min"));
                Location location2 = LocationUtil.deserialize(configuration.getString(path + ".max"));
                if (arenaType == ArenaType.STANDALONE) {
                    arena = new StandaloneArena(arenaName, location1, location2);
                } else if (arenaType == ArenaType.SHARED) {
                    arena = new SharedArena(arenaName, location1, location2);
                } else {
                    continue;
                }

                arena.setDisplayIcon(new ItemBuilder(Material.valueOf(configuration.getString(path + ".icon.material")))
                        .durability(configuration.getInt(path + ".icon.durability"))
                        .build());

                if (configuration.contains(path + ".spawn1")) {
                    arena.setSpawn1(LocationUtil.deserialize(configuration.getString(path + ".spawn1")));
                }

                if (configuration.contains(path + ".spawn2")) {
                    arena.setSpawn2(LocationUtil.deserialize(configuration.getString(path + ".spawn2")));
                }

                if (configuration.contains(path + ".max")) {
                    arena.setMax(LocationUtil.deserialize(configuration.getString(path + ".max")));
                }
                if (configuration.contains(path + ".min")) {
                    arena.setMin(LocationUtil.deserialize(configuration.getString(path + ".min")));
                }

                if (configuration.contains(path + ".kits")) {
                    for (String kitName : configuration.getStringList(path + ".kits")) {
                        arena.getKits().add(kitName);
                    }
                }

                if (arena instanceof StandaloneArena && configuration.contains(path + ".redportal") && configuration.contains(path + ".blueportal")) {
                    StandaloneArena standaloneArena = (StandaloneArena)arena;
                    location1 = LocationUtil.deserialize(configuration.getString(path + ".redportal.location1"));
                    location2 = LocationUtil.deserialize(configuration.getString(path + ".redportal.location2"));
                    standaloneArena.setRedCuboid(new Cuboid(location1, location2));
                    location1 = LocationUtil.deserialize(configuration.getString(path + ".blueportal.location1"));
                    location2 = LocationUtil.deserialize(configuration.getString(path + ".blueportal.location2"));
                    standaloneArena.setBlueCuboid(new Cuboid(location1, location2));
                }
                if (arena instanceof StandaloneArena && configuration.contains(path + ".redbed") && configuration.contains(path + ".bluebed")) {
                    StandaloneArena standaloneArena = (StandaloneArena)arena;
                    location1 = LocationUtil.deserialize(configuration.getString(path + ".redbed.location"));
                    standaloneArena.setRedBed(location1);
                    location1 = LocationUtil.deserialize(configuration.getString(path + ".bluebed.location"));
                    standaloneArena.setBlueBed(location1);
                }

                if (arena instanceof StandaloneArena && configuration.contains(path + ".duplicates"))
                    for (String duplicateId : configuration.getConfigurationSection(path + ".duplicates").getKeys(false)) {
                        location1 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".cuboid.location1"));
                        location2 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".cuboid.location2"));
                        Location spawn1 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".spawnA"));
                        Location spawn2 = LocationUtil.deserialize(configuration.getString(path + ".duplicates." + duplicateId + ".spawnB"));
                        Arena duplicate = new Arena(arenaName, location1, location2);
                        duplicate.setSpawn1(spawn1);
                        duplicate.setSpawn2(spawn2);
                        duplicate.setKits(arena.getKits());
                        ((StandaloneArena)arena).getDuplicates().add(duplicate);
                        getArenas().add(duplicate);
                    }

                Arena.getArenas().add(arena);
            }
        }
        getArenas().forEach(arenas -> arenas.getChunks().forEach(Chunk::load));
    }

    public static Arena getByName(String name) {
        for (Arena arena : arenas) {
            if (arena.getType() != ArenaType.DUPLICATE && arena.getName() != null &&
                    arena.getName().equalsIgnoreCase(name)) {
                return arena;
            }
        }

        return null;
    }

    public ItemStack getDisplayIcon() {
        return this.displayIcon.clone();
    }
  
    public static Arena getRandom(Kit kit) {
        List<Arena> _arenas = new ArrayList<>();

        for (Arena arena : arenas) {
            if (!arena.isSetup()) continue;

            if (!arena.getKits().contains(kit.getName())) continue;

            if (!arena.isActive() && (arena.getType() == ArenaType.STANDALONE || arena.getType() == ArenaType.DUPLICATE)) {
                _arenas.add(arena);
            } else if (!kit.getGameRules().isBuild() && arena.getType() == ArenaType.SHARED) {
                _arenas.add(arena);
            }
        }

        if (_arenas.isEmpty()) {
            return null;
        }

        return _arenas.get(ThreadLocalRandom.current().nextInt(_arenas.size()));
    }

    public ArenaType getType() {
        return ArenaType.DUPLICATE;
    }

    public boolean isSetup() {
        return spawn1 != null && spawn2 != null;
    }

    public int getMaxBuildHeight() {
        int highest = (int) (Math.max(spawn1.getY(), spawn2.getY()));
        return highest + 5;
    }

    public Location getSpawn1() {
        if (spawn1 == null) {
            return null;
        }

        return spawn1.clone();
    }

    public Location getSpawn2() {
        if (spawn2 == null) {
            return null;
        }

        return spawn2.clone();
    }

    public static String getArenaRating(Arena arena) {
        if (arena.getRating() <= 1.0D)
            return CC.translate("&c&l[" + arena.getRating() + "");
        if (arena.getRating() > 1.0D && arena.getRating() < 2.0D)
            return CC.translate("&6&l[" + arena.getRating() + "");
        if (arena.getRating() > 2.0D && arena.getRating() < 3.0D)
            return CC.translate("&e&l[" + arena.getRating() + "");
        if (arena.getRating() > 3.0D && arena.getRating() < 5.0D)
            return CC.translate("&a&l[" + arena.getRating() + "");
        return CC.translate("&2&l[" + arena.getRating() + "");
    }

    public static void calculateRating(Arena arena) {
        //  int size = arena.getRatingList().size();
        double result = arena.getRatingList().stream().reduce(0, Integer::sum);
        arena.setRating(Math.round(10.0D * result) / 10.0D);
        arena.save();
    }

    public void setActive(boolean active) {
        if (getType() != ArenaType.SHARED) {
            this.active = active;
        }
    }

    public void save() {

    }

    public void delete() {

    }
}