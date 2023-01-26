package me.cprox.practice.match.impl;

import lombok.Getter;
import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.menu.match.InventorySnapshot;
import me.cprox.practice.profile.enums.MatchState;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.team.Team;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.profile.meta.StatisticsData;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.TaskUtil;
import me.cprox.practice.util.external.ChatComponentBuilder;
import me.cprox.practice.util.nametag.NameTags;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
public class TeamMatch extends Match {

    private final Team teamA;
    private final Team teamB;
    private final int teamARoundWins = 0;
    private final int teamBRoundWins = 0;

    public TeamMatch(Team teamA, Team teamB, Kit kit, Arena arena) {
        super(null, kit, arena, QueueType.UNRANKED);

        this.teamA = teamA;
        this.teamB = teamB;
    }

    @Override
    public boolean isTeamMatch() {
        return true;
    }

    @Override
    public void setupPlayer(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);

        if (teamPlayer.isDisconnected()) {
            return;
        }

        teamPlayer.setAlive(true);

        PlayerUtil.reset(player);

        if (!getKit().getGameRules().isNoitems()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            StatisticsData kitData = profile.getStatisticsData().get(getKit());
            if (kitData.getKitCount() > 0) {
                TaskUtil.runLater(() -> Profile.getByUuid(player.getUniqueId()).getStatisticsData().get(this.getKit()).getKitItems().forEach((integer, itemStack) -> player.getInventory().setItem(integer, itemStack)), 10L);
            } else {
                player.getInventory().setArmorContents(getKit().getKitInventory().getArmor());
                player.getInventory().setContents(getKit().getKitInventory().getContents());
            }
        }

        player.setMaximumNoDamageTicks(getKit().getGameRules().getHitDelay());

        if (getKit().getGameRules().isStickspawn()) {
            PlayerUtil.denyMovement(player);
        }

        Team team = getTeam(player);

        Location spawn = team.equals(teamA) ? getArena().getSpawn1() : getArena().getSpawn2();

        if (spawn.getBlock().getType() == Material.AIR) {
            player.teleport(spawn);
        } else {
            player.teleport(spawn.add(0, 2, 0));
        }
        teamPlayer.setPlayerSpawn(spawn);

    }

    @Override
    public void onStart() {
        if (getPlayers().size() < 1) {
            return;
        }

        if (getKit().getGameRules().isTimed())
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!getState().equals(MatchState.FIGHTING))
                        return;

                    if (getDuration().equalsIgnoreCase("01:00")) {
                        onEnd();
                        cancel();
                    }
                }
            }.runTaskTimer(Practice.get(), 20L, 20L);
    }

    @Override
    public boolean onEnd() {
        for (TeamPlayer teamPlayer : getTeamPlayers()) {
            /*if (!teamPlayer.isDisconnected() && teamPlayer.isAlive()) {*/
                Player player = teamPlayer.getPlayer();
                Map<UUID, InventorySnapshot> InventorySnapshotMAP = new LinkedHashMap<>();
                if (player != null) {
                    Profile profile = Profile.getByUuid(player.getUniqueId());
                    profile.handleVisibility();

                    getSnapshots().values().forEach(snapshot -> Practice.get().getProfileManager().addSnapshot(snapshot));
                    if (!hasSnapshot(player.getUniqueId())) {
                        addSnapshot(player);
                    }
                    InventorySnapshotMAP.put(player.getUniqueId(), getSnapshot(player.getUniqueId()));

                    for (InventorySnapshot snapshot : getSnapshots().values()) {
                        Practice.get().getProfileManager().addSnapshot(snapshot);
                    }
               // }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (TeamPlayer firstTeamPlayer : getTeamPlayers()) {
                    if (!firstTeamPlayer.isDisconnected()) {
                        Player player = firstTeamPlayer.getPlayer();

                        if (player != null) {
                            for (TeamPlayer secondTeamPlayer : getTeamPlayers()) {
                                if (secondTeamPlayer.isDisconnected()) {
                                    continue;
                                }

                                if (secondTeamPlayer.getUuid().equals(player.getUniqueId())) {
                                    continue;
                                }

                                Player secondPlayer = secondTeamPlayer.getPlayer();

                                if (secondPlayer != null) {
                                    player.hidePlayer(secondPlayer);
                                }

                                NameTags.reset(player, secondPlayer);
                                NameTags.reset(player, firstTeamPlayer.getPlayer());
                            }

                            if (firstTeamPlayer.isAlive()) {
                                if (!hasSnapshot(firstTeamPlayer.getPlayer().getUniqueId())) {
                                addSnapshot(firstTeamPlayer.getPlayer());
                            }
                            }

                            player.setFireTicks(0);
                            player.updateInventory();

                            Profile profile = Profile.getByUuid(player.getUniqueId());
                            profile.setState(ProfileState.IN_LOBBY);
                            profile.setMatch(null);
                            NameTags.reset(player, firstTeamPlayer.getPlayer());
                            PlayerUtil.reset(player, false);
                            profile.refreshHotbar();
                            profile.handleVisibility();
                            Practice.get().getEssentials().teleportToSpawn(player);
                            PlayerUtil.reset(player, false);
                            profile.refreshHotbar();
                        }
                    }
                }
            }
        }.runTaskLaterAsynchronously(Practice.get(), 110L);

        Team winningTeam = getWinningTeam();
        Team losingTeam = getOpponentTeam(winningTeam);


        ChatComponentBuilder winnerInventories = new ChatComponentBuilder("");
        winnerInventories.append("Winners: ").color(ChatColor.GREEN);

        ChatComponentBuilder loserInventories = new ChatComponentBuilder("");
        loserInventories.append("Losers: ").color(ChatColor.RED);

        for (TeamPlayer teamPlayer : winningTeam.getTeamPlayers()) {
            winnerInventories.append(teamPlayer.getUsername()).color(ChatColor.YELLOW);
        }
        for (TeamPlayer teamPlayer : losingTeam.getTeamPlayers()) {
            loserInventories.append(teamPlayer.getUsername()).color(ChatColor.YELLOW);
            loserInventories.setCurrentHoverEvent(getHoverEvent(teamPlayer))
                    .setCurrentClickEvent(getClickEvent(teamPlayer))
                    .append(", ")
                    .color(ChatColor.GRAY);
        }

        winnerInventories.getCurrent().setText(winnerInventories.getCurrent().getText().substring(0,
                winnerInventories.getCurrent().getText().length() - 2));
        loserInventories.getCurrent().setText(loserInventories.getCurrent().getText().substring(0,
                loserInventories.getCurrent().getText().length() - 2));

        List<BaseComponent[]> components = new ArrayList<>();
        components.add(new ChatComponentBuilder("").parse(Practice.get().getMessagesConfig().getString("MATCH.DETAILS")).create());
        components.add(new ChatComponentBuilder("").create());
        components.add(winnerInventories.create());
        components.add(loserInventories.create());

        for (Player player : getPlayersAndSpectators()) {
            components.forEach(components1 -> player.spigot().sendMessage(components1));
        }
        return true;
    }

    @Override
    public boolean canEnd() {
        return teamA.getAliveTeamPlayers().isEmpty() || teamB.getAliveTeamPlayers().isEmpty();
    }

    @Override
    public void onDeath(Player player, Player killer) {

        TeamPlayer teamPlayer = getTeamPlayer(player);

        if (teamPlayer.isAlive()) {
            if (!hasSnapshot(teamPlayer.getPlayer().getUniqueId())) {
                addSnapshot(teamPlayer.getPlayer());
            }
        }

        PlayerUtil.reset(player);

        if (!canEnd() && !teamPlayer.isDisconnected()) {
            Team team = getTeam(player);
            Location spawn = team.equals(teamA) ? getArena().getSpawn1() : getArena().getSpawn2();
            player.teleport(spawn);
            Profile profile = Profile.getByUuid(player.getUniqueId());
            PlayerUtil.reset(player, false);
            profile.refreshHotbar();
            player.setAllowFlight(true);
            player.setFlying(true);
            profile.setState(ProfileState.SPECTATE_MATCH);
        }
    }

    @Override
    public void onRespawn(Player player) {
        if (getKit().getGameRules().isSumo() && !isEnding()) {
            for (TeamPlayer teamPlayer : teamA.getTeamPlayers()) {
                if (teamPlayer.isDisconnected()) {
                    continue;
                }

                Player toPlayer = teamPlayer.getPlayer();

                if (toPlayer != null && toPlayer.isOnline()) {
                    toPlayer.teleport(getArena().getSpawn1());
                }
            }

            for (TeamPlayer teamPlayer : teamB.getTeamPlayers()) {
                if (teamPlayer.isDisconnected()) {
                    continue;
                }

                Player toPlayer = teamPlayer.getPlayer();

                if (toPlayer != null && toPlayer.isOnline()) {
                    toPlayer.teleport(getArena().getSpawn2());
                }
            }
        } else {
            player.teleport(player.getLocation().clone().add(0, 3, 0));
        }
    }

    @Override
    public Player getWinningPlayer() {
        throw new UnsupportedOperationException("Cannot getInstance solo winning player from a TeamMatch");
    }

    @Override
    public Team getWinningTeam() {
        if (getKit().getGameRules().isTimed()) {
            if (teamA.getAliveTeamPlayers().isEmpty()) {
                return teamB;
            } else if (teamB.getAliveTeamPlayers().isEmpty()) {
                return teamA;
            } else if (teamA.getTotalHits() > teamB.getTotalHits()) {
                return teamA;
            } else {
                return teamB;
            }
        } else {
            if (teamA.getAliveTeamPlayers().isEmpty()) {
                return teamB;
            } else if (teamB.getAliveTeamPlayers().isEmpty()) {
                return teamA;
            } else {
                return null;
            }
        }
    }

    @Override
    public TeamPlayer getTeamPlayerA() {
        throw new UnsupportedOperationException("Cannot getInstance solo match player from a TeamMatch");
    }

    @Override
    public TeamPlayer getTeamPlayerB() {
        throw new UnsupportedOperationException("Cannot getInstance solo match player from a TeamMatch");
    }

    @Override
    public List<TeamPlayer> getTeamPlayers() {
        List<TeamPlayer> TeamPlayers = new ArrayList<>();
        TeamPlayers.addAll(teamA.getTeamPlayers());
        TeamPlayers.addAll(teamB.getTeamPlayers());
        return TeamPlayers;
    }

    @Override
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();

        teamA.getTeamPlayers().forEach(TeamPlayer -> {
            Player player = TeamPlayer.getPlayer();

            if (player != null) {
                players.add(player);
            }
        });

        teamB.getTeamPlayers().forEach(TeamPlayer -> {
            Player player = TeamPlayer.getPlayer();

            if (player != null) {
                players.add(player);
            }
        });

        return players;
    }

    @Override
    public List<Player> getAlivePlayers() {
        List<Player> players = new ArrayList<>();

        teamA.getTeamPlayers().forEach(TeamPlayer -> {
            Player player = TeamPlayer.getPlayer();

            if (player != null) {
                if (TeamPlayer.isAlive()) {
                    players.add(player);
                }
            }
        });

        teamB.getTeamPlayers().forEach(TeamPlayer -> {
            Player player = TeamPlayer.getPlayer();

            if (player != null) {
                if (TeamPlayer.isAlive()) {
                    players.add(player);
                }
            }
        });

        return players;
    }

    @Override
    public Team getTeamA() {
        return teamA;
    }

    @Override
    public Team getTeamB() {
        return teamB;
    }

    @Override
    public Team getTeam(Player player) {
        for (TeamPlayer teamTeamPlayer : teamA.getTeamPlayers()) {
            if (teamTeamPlayer.getUuid().equals(player.getUniqueId())) {
                return teamA;
            }
        }

        for (TeamPlayer teamTeamPlayer : teamB.getTeamPlayers()) {
            if (teamTeamPlayer.getUuid().equals(player.getUniqueId())) {
                return teamB;
            }
        }

        return null;
    }

    @Override
    public TeamPlayer getTeamPlayer(Player player) {
        for (TeamPlayer teamPlayer : teamA.getTeamPlayers()) {
            if (teamPlayer.getUuid().equals(player.getUniqueId())) {
                return teamPlayer;
            }
        }

        for (TeamPlayer teamPlayer : teamB.getTeamPlayers()) {
            if (teamPlayer.getUuid().equals(player.getUniqueId())) {
                return teamPlayer;
            }
        }

        return null;
    }

    @Override
    public Team getOpponentTeam(Team team) {
        if (teamA.equals(team)) {
            return teamB;
        } else if (teamB.equals(team)) {
            return teamA;
        } else {
            return null;
        }
    }

    @Override
    public Team getOpponentTeam(Player player) {
        if (teamA.containsPlayer(player)) {
            return teamB;
        } else if (teamB.containsPlayer(player)) {
            return teamA;
        } else {
            return null;
        }
    }

    @Override
    public Player getOpponentPlayer(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance solo opponent player from TeamMatch");
    }

    @Override
    public TeamPlayer getOpponentTeamPlayer(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance solo opponent match player from TeamMatch");
    }

    @Override
    public int getRoundsNeeded(TeamPlayer teamPlayer) {
        throw new UnsupportedOperationException("Cannot getInstance solo rounds needed from TeamMatch");
    }

    @Override
    public int getRoundsNeeded(Team Team) {
        if (teamA.equals(Team)) {
            return 3 - teamARoundWins;
        } else if (teamB.equals(Team)) {
            return 3 - teamBRoundWins;
        } else {
            return -1;
        }
    }

    @Override
    public org.bukkit.ChatColor getRelationColor(Player viewer, Player target) {
        if (viewer.equals(target)) {
            return org.bukkit.ChatColor.GREEN;
        }

        Team team = getTeam(target);
        Team viewerTeam = getTeam(viewer);

        if (team == null || viewerTeam == null) {
            return org.bukkit.ChatColor.DARK_RED;
        }

        if (team.equals(viewerTeam)) {
            return org.bukkit.ChatColor.GREEN;
        } else {
            return org.bukkit.ChatColor.RED;
        }
    }

}
