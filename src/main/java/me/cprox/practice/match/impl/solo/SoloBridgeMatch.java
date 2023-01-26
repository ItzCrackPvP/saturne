package me.cprox.practice.match.impl.solo;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.impl.SoloMatch;
import me.cprox.practice.match.task.MatchStartTask;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.MatchState;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.profile.meta.StatisticsData;
import me.cprox.practice.queue.Queue;
import me.cprox.practice.util.InventoryUtil;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.TaskUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.LocationUtil;
import me.cprox.practice.util.nametag.NameTags;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class SoloBridgeMatch extends SoloMatch {

    private int bridgePlayerAPoints = 0;
    private int bridgePlayerBPoints = 0;

    public SoloBridgeMatch(Queue queue, TeamPlayer playerA, TeamPlayer playerB, Kit kit, Arena arena, QueueType queueType) {
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

        NameTags.color(player, getPlayerA().getPlayer(), org.bukkit.ChatColor.RED, true);
        NameTags.color(player, getPlayerB().getPlayer(), org.bukkit.ChatColor.BLUE, true);
    }

    @Override
    public void onStart() {
        InventoryUtil.giveBridgeKit(getPlayerA().getPlayer());
        InventoryUtil.giveBridgeKit(getPlayerB().getPlayer());
    }

    @Override
    public boolean canEnd() {
        if (getRoundsNeeded(getPlayerA()) == 0 || getRoundsNeeded(getPlayerB()) == 0)
            return true;
        return getPlayerA().isDisconnected() || getPlayerB().isDisconnected();
    }

    @Override
    public Player getWinningPlayer() {
        if (getKit().getGameRules().isBridge()) {
            return (getPlayerA().isDisconnected() || bridgePlayerBPoints == 5) ? getPlayerB().getPlayer() : ((getPlayerB().isDisconnected() || bridgePlayerBPoints == 5) ? getPlayerA().getPlayer() : null);
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
        InventoryUtil.giveBridgeKit(player);
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
        InventoryUtil.giveBridgeKit(player);
        PlayerUtil.allowMovement(player);
        TaskUtil.runLater(() -> setupPlayer(player), 1L);
    }

    public void handlePortal(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);
        Profile playerAProfile = Profile.getByUuid(getPlayerA().getPlayer());
        Profile playerBProfile = Profile.getByUuid(getPlayerB().getPlayer());
        if (teamPlayer == null)
            return;
        if (!isFighting())
            return;
        if (LocationUtil.isTeamPortalBridge(player)) {
            teamPlayer.getPlayer().teleport(teamPlayer.getPlayerSpawn());
            player.sendMessage(CC.RED + "You Jumped in wrong portal.");
            player.setHealth(20);
            return;
        }
        if (getTeamPlayerA().equals(teamPlayer)) {
            this.bridgePlayerAPoints++;
            getPlayerA().getPlayer().teleport(getPlayerA().getPlayerSpawn());
            getPlayerB().getPlayer().teleport(getPlayerB().getPlayerSpawn());
            broadcastMessage("");
            broadcastMessage(CC.translate("&c&l" + getPlayerA().getPlayer().getName() + " &7scored!"));
            broadcastMessage(CC.translate(" &c" + bridgePlayerAPoints + " &7- " + "&9" + bridgePlayerBPoints));
            broadcastMessage("");
            setupPlayer(getPlayerA().getPlayer());
            setupPlayer(getPlayerB().getPlayer());
            if (playerAProfile.getKitEditor().getSelectedKitInventory() != null) {
                getPlayerA().getPlayer().getInventory().setArmorContents(playerAProfile.getKitEditor().getSelectedKitInventory().getArmor());
                getPlayerA().getPlayer().getInventory().setContents(playerAProfile.getKitEditor().getSelectedKitInventory().getContents());
            }
            if (playerBProfile.getKitEditor().getSelectedKitInventory() != null) {
                getPlayerB().getPlayer().getInventory().setArmorContents(playerBProfile.getKitEditor().getSelectedKitInventory().getArmor());
                getPlayerB().getPlayer().getInventory().setContents(playerBProfile.getKitEditor().getSelectedKitInventory().getContents());
            }
            InventoryUtil.giveBridgeKit(getPlayerA().getPlayer());
            InventoryUtil.giveBridgeKit(getPlayerB().getPlayer());
            if (bridgePlayerAPoints == 5) {
                end();
                PlayerUtil.reset(getPlayerA().getPlayer());
                PlayerUtil.reset(getPlayerB().getPlayer());
                getPlayerA().setTheBridgeKills(0);
                getPlayerB().setTheBridgeKills(0);
                playerAProfile.getKitEditor().setSelectedKitInventory(null);
                playerBProfile.getKitEditor().setSelectedKitInventory(null);
                return;
            }
        } else {
            this.bridgePlayerBPoints++;
            getPlayerA().getPlayer().teleport(getPlayerA().getPlayerSpawn());
            getPlayerB().getPlayer().teleport(getPlayerB().getPlayerSpawn());
            broadcastMessage("");
            broadcastMessage(CC.translate("&9&l" + getPlayerB().getPlayer().getName() + " &7scored!"));
            broadcastMessage(CC.translate(" &c" + bridgePlayerAPoints + " &7- " + "&9" + bridgePlayerBPoints));
            broadcastMessage("");
            setupPlayer(getPlayerA().getPlayer());
            setupPlayer(getPlayerB().getPlayer());
            if (playerAProfile.getKitEditor().getSelectedKitInventory() != null) {
                getPlayerA().getPlayer().getInventory().setArmorContents(playerAProfile.getKitEditor().getSelectedKitInventory().getArmor());
                getPlayerA().getPlayer().getInventory().setContents(playerAProfile.getKitEditor().getSelectedKitInventory().getContents());
            }
            if (playerBProfile.getKitEditor().getSelectedKitInventory() != null) {
                getPlayerB().getPlayer().getInventory().setArmorContents(playerBProfile.getKitEditor().getSelectedKitInventory().getArmor());
                getPlayerB().getPlayer().getInventory().setContents(playerBProfile.getKitEditor().getSelectedKitInventory().getContents());
            }
            InventoryUtil.giveBridgeKit(getPlayerA().getPlayer());
            InventoryUtil.giveBridgeKit(getPlayerB().getPlayer());
            if (bridgePlayerBPoints == 5) {
                end();
                PlayerUtil.reset(getPlayerA().getPlayer());
                PlayerUtil.reset(getPlayerB().getPlayer());
                getPlayerA().setTheBridgeKills(0);
                getPlayerB().setTheBridgeKills(0);
                playerAProfile.getKitEditor().setSelectedKitInventory(null);
                playerBProfile.getKitEditor().setSelectedKitInventory(null);
                return;
            }
        }
        setState(MatchState.STARTING);
        new MatchStartTask(this).runTaskTimer(Practice.get(), 20L, 20L);
    }

    @Override
    public ChatColor getRelationColor(Player viewer, Player target) {
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
        return this.bridgePlayerAPoints;
    }

    public int getPlayerBPoints() {
        return this.bridgePlayerBPoints;
    }
}