package me.cprox.practice.match.task;

import me.cprox.practice.match.Match;
import me.cprox.practice.match.impl.solo.*;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.InventoryUtil;
import me.cprox.practice.util.PlayerUtil;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchRespawnTask extends BukkitRunnable {
    private final Match match;
    private int ticks;
    private final Player player;

    public MatchRespawnTask(Match match, Player player) {
        this.player = player;
        this.match = match;
    }

    @Override
    public void run() {
        int seconds = 3 - ticks;
        if (match.isEnding()) {
            cancel();
            return;
        }
            if (seconds == 0) {
                if (match.getKit().getGameRules().isBedFight()) {
                    Profile profile = Profile.getByUuid(player);
                    SoloBedFightMatch bedFightMatch = (SoloBedFightMatch) match;
                    player.setFallDistance(0);
                    player.setHealth(20.0);
                    player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                    player.getInventory().clear();
                    PlayerUtil.reset(player);
                    bedFightMatch.setupPlayer(player);
                    if (profile.getKitEditor().getSelectedKitInventory() != null) {
                        player.getInventory().setArmorContents(profile.getKitEditor().getSelectedKitInventory().getArmor());
                        player.getInventory().setContents(profile.getKitEditor().getSelectedKitInventory().getContents());
                    }
                    PlayerUtil.allowMovement(player);
                    InventoryUtil.giveBedFightKit(player);
                } else if (match.getKit().getGameRules().isBattleRush()) {
                    Profile profile = Profile.getByUuid(player);
                    SoloBattleRushMatch battleRushMatch = (SoloBattleRushMatch) match;
                    player.setFallDistance(0);
                    player.setHealth(20.0);
                    player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                    player.getInventory().clear();
                    PlayerUtil.reset(player);
                    battleRushMatch.setupPlayer(player);
                    if (profile.getKitEditor().getSelectedKitInventory() != null) {
                        player.getInventory().setArmorContents(profile.getKitEditor().getSelectedKitInventory().getArmor());
                        player.getInventory().setContents(profile.getKitEditor().getSelectedKitInventory().getContents());
                    }
                    PlayerUtil.allowMovement(player);
                    InventoryUtil.giveBattleRushKit(player);
                } else if (match.getKit().getGameRules().isPearlFight()) {
                    Profile profile = Profile.getByUuid(player);
                    SoloPearlFightMatch pearlFightMatch = (SoloPearlFightMatch) match;
                    player.setFallDistance(0);
                    player.setHealth(20.0);
                    player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                    player.getInventory().clear();
                    PlayerUtil.reset(player);
                    pearlFightMatch.setupPlayer(player);
                    if (profile.getKitEditor().getSelectedKitInventory() != null) {
                        player.getInventory().setArmorContents(profile.getKitEditor().getSelectedKitInventory().getArmor());
                        player.getInventory().setContents(profile.getKitEditor().getSelectedKitInventory().getContents());
                    }
                    PlayerUtil.allowMovement(player);
                    InventoryUtil.givePearlFightKit(player);
                }

                cancel();
                return;
            }

        PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer
                .a("{\"text\": \"" + "Respawning" + "\",color:" + ChatColor.BOLD.name().toLowerCase() + "\",color:" + ChatColor.DARK_RED.name().toLowerCase() + "}")
        );
        PacketPlayOutTitle packetPlayOutSubtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer
                .a("{\"text\": \"" + ChatColor.GRAY + "in " + ChatColor.DARK_RED + seconds + ChatColor.GRAY + "..." + "\",color:" + ChatColor.WHITE.name().toLowerCase() + "}")
        );

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutTitle);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutSubtitle);
        ticks++;
    }
}
