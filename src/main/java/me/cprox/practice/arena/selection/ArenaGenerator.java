package me.cprox.practice.arena.selection;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.arena.impl.SharedArena;
import me.cprox.practice.arena.impl.StandaloneArena;
import me.cprox.practice.profile.enums.ArenaType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

public class ArenaGenerator {
    private final String name;

    private final World world;

    private final Schematic schematic;

    private final ArenaType type;

    public ArenaGenerator(String name, World world, Schematic schematic, ArenaType type) {
        this.name = name;
        this.world = world;
        this.schematic = schematic;
        this.type = type;
    }

    public void generate(final File file, StandaloneArena parentArena) {
        int minX, maxX, minZ, maxZ, minY, maxY;
        final Arena arena;
        log("Generating " + this.type.name() + " " + this.name + " arena...");
        int range = 500;
        int attempts = 0;
        int preciseX = ThreadLocalRandom.current().nextInt(range);
        int preciseZ = ThreadLocalRandom.current().nextInt(range);
        if (ThreadLocalRandom.current().nextBoolean())
            preciseX = -preciseX;
        if (ThreadLocalRandom.current().nextBoolean())
            preciseZ = -preciseZ;
        label93: while (true) {
            attempts++;
            if (attempts >= 5) {
                preciseX = ThreadLocalRandom.current().nextInt(range);
                preciseZ = ThreadLocalRandom.current().nextInt(range);
                if (ThreadLocalRandom.current().nextBoolean())
                    preciseX = -preciseX;
                if (ThreadLocalRandom.current().nextBoolean())
                    preciseZ = -preciseZ;
                range += 500;
                log("Increased range to: " + range);
            }
            if (this.world.getBlockAt(preciseX, 72, preciseZ) == null)
                continue;
            minX = preciseX - this.schematic.getClipBoard().getWidth() - 200;
            maxX = preciseX + this.schematic.getClipBoard().getWidth() + 200;
            minZ = preciseZ - this.schematic.getClipBoard().getLength() - 200;
            maxZ = preciseZ + this.schematic.getClipBoard().getLength() + 200;
            minY = 72;
            maxY = 72 + this.schematic.getClipBoard().getHeight();
            for (int i = minX; i < maxX; i++) {
                for (int z = minZ; z < maxZ; z++) {
                    for (int y = minY; y < maxY; y++) {
                        if (this.world.getBlockAt(i, y, z).getType() != Material.AIR)
                            continue label93;
                    }
                }
            }
            break;
        }
        Location minCorner = new Location(this.world, minX, minY, minZ);
        Location maxCorner = new Location(this.world, maxX, maxY, maxZ);
        int finalPreciseX = preciseX;
        int finalPreciseZ = preciseZ;
        try {
            (new Schematic(file)).pasteSchematic(this.world, finalPreciseX, 76, finalPreciseZ);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.type == ArenaType.STANDALONE) {
            arena = new StandaloneArena(this.name, minCorner, maxCorner);
        } else if (this.type == ArenaType.DUPLICATE) {
            arena = new Arena(this.name, minCorner, maxCorner);
            parentArena.getDuplicates().add(arena);
        } else {
            arena = new SharedArena(this.name, minCorner, maxCorner);
        }
        int x;
        label94: for (x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y < maxY; y++) {
                    if (this.world.getBlockAt(x, y, z).getType() == Material.SPONGE) {
                        final Block origin = this.world.getBlockAt(x, y, z);
                        final Block up = origin.getRelative(BlockFace.UP, 1);
                        if (up.getState() instanceof Sign) {
                            Sign sign = (Sign)up.getState();
                            if (!sign.getLine(0).isEmpty() && !sign.getLine(1).isEmpty()) {
                                float pitch = Float.parseFloat(sign.getLine(0));
                                float yaw = Float.parseFloat(sign.getLine(1));
                                Location loc = new Location(origin.getWorld(), origin.getX(), origin.getY(), origin.getZ(), yaw, pitch);
                                (new BukkitRunnable() {
                                    public void run() {
                                        up.setType(Material.AIR);
                                        origin.setType(origin.getRelative(BlockFace.NORTH).getType());
                                    }
                                }).runTask(Practice.get());
                                if (arena.getSpawn1() == null) {
                                    arena.setSpawn2(loc);
                                } else if (arena.getSpawn2() == null) {
                                    arena.setSpawn2(loc);
                                    break label94;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (arena.getType() == ArenaType.DUPLICATE) {
            parentArena.save();
        } else {
            arena.save();
        }
        Arena.getArenas().add(arena);
        if (this.type == ArenaType.STANDALONE)
            for (int i = 0; i < 5; i++) {
                (new BukkitRunnable() {
                    public void run() {
                        ArenaGenerator.this.log("Generating duplicate...");
                        (new ArenaGenerator(ArenaGenerator.this.name, ArenaGenerator.this.world, ArenaGenerator.this.schematic, ArenaType.DUPLICATE))
                                .generate(file, (StandaloneArena)arena);
                    }
                }).runTask(Practice.get());
            }
        log(String.format("Pasted schematic at %1$s, %2$s, %3$s", preciseX, 76, preciseZ));
    }

    private void log(String message) {
        Practice.get().getLogger().info("[ArenaGenerate] " + message);
    }
}