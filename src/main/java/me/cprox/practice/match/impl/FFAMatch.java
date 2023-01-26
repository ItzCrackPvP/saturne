package me.cprox.practice.match.impl;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.team.Team;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.menu.match.InventorySnapshot;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.util.Circle;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.external.ChatComponentBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FFAMatch extends Match {

    private final Team team;

    public FFAMatch(Team team, Kit kit, Arena arena) {
        super(null, kit, arena, QueueType.UNRANKED);

        this.team = team;
    }

    @Override
    public boolean isFreeForAllMatch() {
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
            //TaskUtil.runLater(() -> Profile.getByUuid(player.getUniqueId()).getStatisticsData().get(this.getKit()).getKitItems().forEach((integer, itemStack) -> player.getInventory().setItem(integer, itemStack)), 10L);
        }

        if (!getKit().getGameRules().isCombo()) {
            player.setMaximumNoDamageTicks(getKit().getGameRules().getHitDelay());
        }

        if (getKit().getGameRules().isCombo()) {
            player.setMaximumNoDamageTicks(0);
            player.setNoDamageTicks(3);
        }

        if (getKit().getGameRules().isStickspawn()) {
            PlayerUtil.denyMovement(player);
        }

        Team team = getTeam(player);
    }

    public double getAverage(double one, double two) {
        double three = one + two;
        three = three / 2;
        return three;
    }

    @Override
    public void onStart() {
        int i = 0;
        for ( Player player : getPlayers() ) {
            Location midSpawn = this.getMidSpawn();
            List<Location> circleLocations = Circle.getCircle(midSpawn, 7, this.getPlayers().size());
            Location center = midSpawn.clone();
            Location loc = circleLocations.get(i);
            Location target = loc.setDirection(center.subtract(loc).toVector());
            player.teleport(target.add(0, 0.5, 0));
            circleLocations.remove(i);
            i++;
        }
    }

    @Override
    public boolean onEnd() {
        Map<UUID, InventorySnapshot> InventorySnapshotMAP = new LinkedHashMap<>();
        for (TeamPlayer teamPlayer : team.getTeamPlayers()) {
            if (!teamPlayer.isDisconnected() && teamPlayer.isAlive()) {
                Player player = teamPlayer.getPlayer();

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
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (TeamPlayer firstTeamPlayer : team.getTeamPlayers()) {
                    if (!firstTeamPlayer.isDisconnected()) {
                        Player player = firstTeamPlayer.getPlayer();
                        if (player != null) {
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
                            profile.handleVisibility();
                            PlayerUtil.reset(player, false);
                            profile.refreshHotbar();
                            //KnockbackProfile kbprofile = KnockbackModule.getDefault();
                            //((CraftPlayer) player).getHandle().setKnockback(kbprofile);
                            Practice.get().getEssentials().teleportToSpawn(player);
                        }
                    }
                }
            }
        }.runTaskLater(Practice.get(), (getKit().getGameRules().isWaterkill() || getKit().getGameRules().isLavakill()) ? 0L : 40L);

        Player winningTeam = getWinningPlayer();

        ChatComponentBuilder winnerInventories = new ChatComponentBuilder("");
        winnerInventories.append("Winners: ").color(net.md_5.bungee.api.ChatColor.GREEN);

        ChatComponentBuilder loserInventories = new ChatComponentBuilder("");
        loserInventories.append("Losers: ").color(net.md_5.bungee.api.ChatColor.RED);

        winnerInventories.append(winningTeam.getName()).color(net.md_5.bungee.api.ChatColor.YELLOW);
        winnerInventories.setCurrentHoverEvent(getHoverEvent(getTeamPlayer(winningTeam)))
                .setCurrentClickEvent(getClickEvent(getTeamPlayer(winningTeam))).color(net.md_5.bungee.api.ChatColor.YELLOW);

        for (TeamPlayer teamPlayer : team.getTeamPlayers()) {
            if (teamPlayer.equals(getTeamPlayer(winningTeam))) continue;
            loserInventories.append(teamPlayer.getUsername()).color(net.md_5.bungee.api.ChatColor.YELLOW);
            loserInventories.setCurrentHoverEvent(getHoverEvent(teamPlayer))
                    .setCurrentClickEvent(getClickEvent(teamPlayer))
                   .append(", ")
                    .color(net.md_5.bungee.api.ChatColor.GRAY);
        }
        loserInventories.getCurrent().setText(loserInventories.getCurrent().getText().substring(0,
                loserInventories.getCurrent().getText().length() - 2));

        List<BaseComponent[]> components = new ArrayList<>();
        components.add(new ChatComponentBuilder("").parse(Practice.get().getMessagesConfig().getString("MATCH.DETAILS")).create());
        components.add(winnerInventories.create());
        components.add(loserInventories.create());

        for (Player player : getPlayersAndSpectators()) {
            components.forEach(components1 -> player.spigot().sendMessage(components1));
        }
        return true;
    }

    @Override
    public boolean canEnd() {
        return team.getAliveTeamPlayers().size() == 1;
    }

    @Override
    public void onDeath(Player player, Player killer) {
        TeamPlayer teamPlayer = getTeamPlayer(player);
        if (!hasSnapshot(player.getUniqueId())) {
            addSnapshot(player);
        }

        if (killer != null) {
            if (!hasSnapshot(killer.getUniqueId())) {
                addSnapshot(killer);
            }
        }

        PlayerUtil.reset(player);

        if (!canEnd() && !teamPlayer.isDisconnected()) {
            player.teleport(getArena().getSpawn1());
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

    }

    @Override
    public Player getWinningPlayer() {

            if (team.getAliveTeamPlayers().size() == 1) {
                return team.getAliveTeamPlayers().get(0).getPlayer();
            } else {
                return null;
            }
    }

    @Override
    public Team getWinningTeam() {
        throw new UnsupportedOperationException("Cannot getInstance winning team from a Juggernaut match");
    }

    @Override
    public TeamPlayer getTeamPlayerA() {
        throw new UnsupportedOperationException("Cannot getInstance team player from a Juggernaut match");
    }

    @Override
    public TeamPlayer getTeamPlayerB() {
        throw new UnsupportedOperationException("Cannot getInstance team player from a Juggernaut match");
    }

    @Override
    public List<TeamPlayer> getTeamPlayers() {
        throw new UnsupportedOperationException("Cannot getInstance team player from a Juggernaut match");
    }

    @Override
    public List<Player> getPlayers() {
        return team.getPlayers();
    }

    public List<Player> getAlivePlayers() {
        List<Player> players = new ArrayList<>();
        for (Player player : team.getPlayers()) {
            if (getTeamPlayer(player).isAlive()) {
                players.add(player);
            }
        }
        return players;
    }

    @Override
    public Team getTeamA() {
        throw new UnsupportedOperationException("Cannot getInstance team from a Juggernaut match");
    }

    @Override
    public Team getTeamB() {
        throw new UnsupportedOperationException("Cannot getInstance team from a Juggernaut match");
    }

    @Override
    public Team getTeam(Player player) {
        return team;
    }

    @Override
    public TeamPlayer getTeamPlayer(Player player) {
        for (TeamPlayer teamPlayer : team.getTeamPlayers()) {
            if (teamPlayer.getUuid().equals(player.getUniqueId())) {
                return teamPlayer;
            }
        }

        return null;
    }

    @Override
    public Team getOpponentTeam(Team team) {
        throw new UnsupportedOperationException("Cannot getInstance opponent team from a Juggernaut match");
    }

    @Override
    public Team getOpponentTeam(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance opponent team from a Juggernaut match");
    }

    @Override
    public Player getOpponentPlayer(Player player) {
        throw new IllegalStateException("Cannot getInstance opponent player in Juggernaut match");
    }

    @Override
    public TeamPlayer getOpponentTeamPlayer(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance opponent team from a Juggernaut match");
    }

    @Override
    public int getRoundsNeeded(Team Team) {
        return 0;
    }

    @Override
    public int getRoundsNeeded(TeamPlayer teamPlayer) {
        return 0;
    }

    @Override
    public ChatColor getRelationColor(Player viewer, Player target) {
        return ChatColor.RED;
    }

}
