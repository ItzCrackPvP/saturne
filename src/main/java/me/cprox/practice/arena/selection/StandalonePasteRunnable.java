package me.cprox.practice.arena.selection;

import lombok.Getter;
import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.arena.impl.StandaloneArena;
import me.cprox.practice.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
public class StandalonePasteRunnable extends DuplicateArenaRunnable {

    private final Practice plugin;
    private final StandaloneArena copiedArena;

    private int times;
    private int amount;
    private int arenaId = 0;

    public StandalonePasteRunnable(Practice plugin, StandaloneArena copiedArena, int copyAmount) {
        super(plugin, copiedArena, 1000, 1000, 500, 500);

        this.plugin = plugin;
        this.times = copyAmount;
        this.copiedArena = copiedArena;
    }

    @Override
    public void run() {
        amount = times;

        while (--times > 0) {
            arenaId++;
            super.run();
        }

        this.message();
    }

    @Override
    public void onComplete() {
        double minX = this.copiedArena.getMin().getX() + this.getOffsetX();
        double minZ = this.copiedArena.getMin().getZ() + this.getOffsetZ();
        double maxX = this.copiedArena.getMax().getX() + this.getOffsetX();
        double maxZ = this.copiedArena.getMax().getZ() + this.getOffsetZ();

        double aX = this.copiedArena.getSpawn1().getX() + this.getOffsetX();
        double aZ = this.copiedArena.getSpawn1().getZ() + this.getOffsetZ();
        double bX = this.copiedArena.getSpawn2().getX() + this.getOffsetX();
        double bZ = this.copiedArena.getSpawn2().getZ() + this.getOffsetZ();

        Location min = new Location(this.copiedArena.getSpawn1().getWorld(), minX, this.copiedArena.getMin().getY(), minZ, this.copiedArena.getMin().getYaw(), this.copiedArena.getMin().getPitch());
        Location max = new Location(this.copiedArena.getSpawn1().getWorld(), maxX, this.copiedArena.getMax().getY(), maxZ, this.copiedArena.getMax().getYaw(), this.copiedArena.getMax().getPitch());
        Location a = new Location(this.copiedArena.getSpawn1().getWorld(), aX, this.copiedArena.getSpawn1().getY(), aZ, this.copiedArena.getSpawn1().getYaw(), this.copiedArena.getSpawn1().getPitch());
        Location b = new Location(this.copiedArena.getSpawn1().getWorld(), bX, this.copiedArena.getSpawn2().getY(), bZ, this.copiedArena.getSpawn2().getYaw(), this.copiedArena.getSpawn2().getPitch());

        StandaloneArena duplicate = new StandaloneArena(this.copiedArena.getName() + "#" + arenaId, min, max);
        duplicate.setDuplicate(true);
        duplicate.setSpawn1(a);
        duplicate.setSpawn2(b);
        duplicate.setMax(max);
        duplicate.setMin(min);
        //duplicate.setDisplayName(this.copiedArena.getDisplayName());
        //duplicate.takeSnapshot();

        this.copiedArena.getDuplicates().add(duplicate);
    }

    public void message() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("array.arena.admin") || player.isOp()) {
                player.sendMessage(CC.translate("&8[&c&lArray&8] &7Finished pasting &c" + copiedArena.getName() + "&7's " + amount + " &7duplicate arenas."));
            }
        }
        //plugin.logger("&7Finished pasting &c" + copiedArena.getName() + "&7's " + amount + " &7duplicate arenas.");
        plugin.getProfileManager().setPasting(false);
        Arena.getArenas().forEach(Arena::save);
    }
}