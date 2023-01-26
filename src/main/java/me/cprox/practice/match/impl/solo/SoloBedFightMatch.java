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

public class SoloBedFightMatch extends SoloMatch {
    private boolean redBed = true;
    private boolean blueBed = true;

    public SoloBedFightMatch(Queue queue, TeamPlayer playerA, TeamPlayer playerB, Kit kit, Arena arena, QueueType queueType) {
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
        InventoryUtil.giveBedFightKit(getPlayerA().getPlayer());
        InventoryUtil.giveBedFightKit(getPlayerB().getPlayer());
    }

    @Override
    public boolean canEnd() {
        if (getRoundsNeeded(getPlayerA()) == 0 || getRoundsNeeded(getPlayerB()) == 0)
            return true;
        return getPlayerA().isDisconnected() || getPlayerB().isDisconnected();
    }

    @Override
    public Player getWinningPlayer() {
        if (getPlayerA().isDisconnected() || !getPlayerA().isAlive()) {
            return getPlayerB().getPlayer();
        } else {
            return getPlayerA().getPlayer();
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
        InventoryUtil.giveBedFightKit(player);
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

    public void handleBed(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);
        if (teamPlayer == null)
            return;
        if (!isFighting())
            return;
        if (getTeamPlayerA().equals(teamPlayer)) {
            blueBed = false;
        } else {
            redBed = false;
        }
    }

    public void handleEnd(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);
        Profile playerAProfile = Profile.getByUuid(getPlayerA().getPlayer());
        Profile playerBProfile = Profile.getByUuid(getPlayerB().getPlayer());
        if (teamPlayer == null)
            return;
        if (!isFighting())
            return;
        if (getTeamPlayerA().equals(teamPlayer)) {
            end();
            PlayerUtil.reset(getPlayerA().getPlayer());
            PlayerUtil.reset(getPlayerB().getPlayer());
            playerAProfile.getKitEditor().setSelectedKitInventory(null);
            playerBProfile.getKitEditor().setSelectedKitInventory(null);
        } else {
            end();
            PlayerUtil.reset(getPlayerA().getPlayer());
            PlayerUtil.reset(getPlayerB().getPlayer());
            playerAProfile.getKitEditor().setSelectedKitInventory(null);
            playerBProfile.getKitEditor().setSelectedKitInventory(null);
        }
    }

    public boolean getRedBed() {
        return this.redBed;
    }

    public boolean getBlueBed() {
        return this.blueBed;
    }
}