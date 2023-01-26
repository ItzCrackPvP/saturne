package me.cprox.practice.match.impl.solo;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.impl.SoloMatch;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.profile.meta.StatisticsData;
import me.cprox.practice.queue.Queue;
import me.cprox.practice.util.InventoryUtil;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.TaskUtil;
import me.cprox.practice.util.nametag.NameTags;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class SoloPearlFightMatch extends SoloMatch {
    private int pearlFightPlayerAPoints = 3;
    private int pearlFightPlayerBPoints = 3;

    public SoloPearlFightMatch(Queue queue, TeamPlayer playerA, TeamPlayer playerB, Kit kit, Arena arena, QueueType queueType) {
        super(queue, playerA, playerB, kit, arena, queueType);
    }

    @Override
    public void setupPlayer(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);

        if (teamPlayer.isDisconnected()) {
            return;
        }
        teamPlayer.setAlive(true);

        PlayerUtil.reset(player);
        PlayerUtil.denyMovement(player);

        if (!getKit().getGameRules().isNoitems()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            StatisticsData kitData = profile.getStatisticsData().get(getKit());
            if (kitData.getKitCount() > 0) {
                if (profile.getKitEditor().getSelectedKitInventory() == null) {
                    TaskUtil.runLater(() -> Profile.getByUuid(player.getUniqueId()).getStatisticsData().get(this.getKit()).getKitItems().forEach((integer, itemStack) -> player.getInventory().setItem(integer, itemStack)), 10L);
                }
            } else {
                player.getInventory().setArmorContents(getKit().getKitInventory().getArmor());
                player.getInventory().setContents(getKit().getKitInventory().getContents());
            }
        }

        Location spawn = getPlayerA().equals(teamPlayer) ? getArena().getSpawn1() : getArena().getSpawn2();

        if (spawn.getBlock().getType() == Material.AIR) {
            player.teleport(spawn);
        } else {
            player.teleport(spawn.add(0, 2, 0));
        }
        teamPlayer.setPlayerSpawn(spawn);

        NameTags.color(player, getPlayerA().getPlayer(), org.bukkit.ChatColor.RED, false);
        NameTags.color(player, getPlayerB().getPlayer(), org.bukkit.ChatColor.BLUE, false);
    }

    @Override
    public void onStart() {
        InventoryUtil.givePearlFightKit(getPlayerA().getPlayer());
        InventoryUtil.givePearlFightKit(getPlayerB().getPlayer());
    }

    @Override
    public boolean canEnd() {
        if (getRoundsNeeded(getPlayerA()) == 0 || getRoundsNeeded(getPlayerB()) == 0)
            return true;
        return getPlayerA().isDisconnected() || getPlayerB().isDisconnected();
    }

    @Override
    public Player getWinningPlayer() {
        if (getKit().getGameRules().isPearlFight()) {
            return (getPlayerA().isDisconnected() || getPlayerBPoints() == 0) ? getPlayerB().getPlayer() : ((getPlayerB().isDisconnected() || getPlayerAPoints() == 0) ? getPlayerA().getPlayer() : null);
        } else {
            if (getPlayerA().isDisconnected() || !getPlayerA().isAlive()) {
                return getPlayerB().getPlayer();
            } else {
                return getPlayerA().getPlayer();
            }
        }
    }

    @Override
    public void onDeath(Player player, Player killer) {
        for (Player players : getPlayers()) {
            Profile profile = Profile.getByUuid(players);
            profile.handleVisibility();
        }
        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.refreshHotbar();
        profile.handleVisibility();
        InventoryUtil.givePearlFightKit(player);
        player.teleport(profile.getMatch().getTeamPlayer(player).getPlayerSpawn());
    }

    @Override
    public void onRespawn(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);
        if (!isFighting())
            return;
        if (teamPlayer.isDisconnected())
            return;
        for (Player players : getPlayers()) {
            Profile profile = Profile.getByUuid(players);
            profile.handleVisibility();
        }
        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.refreshHotbar();
        profile.handleVisibility();
        InventoryUtil.givePearlFightKit(player);
        PlayerUtil.allowMovement(player);
        TaskUtil.runLater(() -> setupPlayer(player), 1L);
    }

    public void onLow(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);
        Profile playerAProfile = Profile.getByUuid(getPlayerA().getPlayer());
        Profile playerBProfile = Profile.getByUuid(getPlayerB().getPlayer());
        if (teamPlayer == null)
            return;
        if (!isFighting())
            return;
        if (getTeamPlayerA().equals(teamPlayer)) {
            this.pearlFightPlayerAPoints--;
            if (pearlFightPlayerAPoints == 0) {
                end();
                PlayerUtil.reset(getPlayerA().getPlayer());
                PlayerUtil.reset(getPlayerB().getPlayer());
                getPlayerA().setPearlFightKills(0);
                getPlayerB().setPearlFightKills(0);
                playerAProfile.getKitEditor().setSelectedKitInventory(null);
                playerBProfile.getKitEditor().setSelectedKitInventory(null);
            }
        } else {
            this.pearlFightPlayerBPoints--;
            if (pearlFightPlayerBPoints == 3) {
                end();
                PlayerUtil.reset(getPlayerA().getPlayer());
                PlayerUtil.reset(getPlayerB().getPlayer());
                getPlayerA().setPearlFightKills(0);
                getPlayerB().setPearlFightKills(0);
                playerAProfile.getKitEditor().setSelectedKitInventory(null);
                playerBProfile.getKitEditor().setSelectedKitInventory(null);
            }
        }
    }

    @Override
    public org.bukkit.ChatColor getRelationColor(Player viewer, Player target) {
        if (viewer.equals(target)) {
            return org.bukkit.ChatColor.GREEN;
        }

        if (getPlayerA().getUuid().equals(viewer.getUniqueId()) || getPlayerB().getUuid().equals(viewer.getUniqueId())) {
            return org.bukkit.ChatColor.RED;
        } else {
            return org.bukkit.ChatColor.GREEN;
        }
    }

    public int getPlayerAPoints() {
        return this.pearlFightPlayerAPoints;
    }

    public int getPlayerBPoints() {
        return this.pearlFightPlayerBPoints;
    }
}