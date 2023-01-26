package me.cprox.practice.match.impl;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.task.MatchStartTask;
import me.cprox.practice.match.team.Team;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.menu.match.InventorySnapshot;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.profile.enums.MatchState;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.profile.meta.ProfileRematchData;
import me.cprox.practice.profile.meta.StatisticsData;
import me.cprox.practice.queue.Queue;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.TaskUtil;
import me.cprox.practice.util.external.EloCalculator;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.external.ChatComponentBuilder;
import me.cprox.practice.util.nametag.NameTags;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
public class SoloMatch extends Match {
    private String specMessage;
    private String eloMessage;
    private final TeamPlayer playerA;
    private final TeamPlayer playerB;

    public SoloMatch(Queue queue, TeamPlayer playerA, TeamPlayer playerB, Kit kit, Arena arena, QueueType queueType) {
        super(queue, kit, arena, queueType);

        this.playerA = playerA;
        this.playerB = playerB;
    }

    @Override
    public boolean isSoloMatch() {
        return true;
    }

    @Override
    public void setupPlayer(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);

        if (teamPlayer.isDisconnected()) {
            return;
        }

        teamPlayer.setAlive(true);

        PlayerUtil.reset(player, true);

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

        if (getKit().getGameRules().isBoxing()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100000, 1));
        }

        player.setMaximumNoDamageTicks(getKit().getGameRules().getHitDelay());

        if (getKit().getGameRules().isStickspawn()) {
            PlayerUtil.denyMovement(player);
        }

        Location spawn = playerA.equals(teamPlayer) ? getArena().getSpawn1() : getArena().getSpawn2();

        if (spawn.getBlock().getType() == Material.AIR) {
            player.teleport(spawn);
        } else {
            player.teleport(spawn.add(0, 2, 0));
        }
        teamPlayer.setPlayerSpawn(spawn);

        NameTags.color(player, getPlayerA().getPlayer(), org.bukkit.ChatColor.RED, getKit().getGameRules().isShowhealth());
        NameTags.color(player, getPlayerB().getPlayer(), org.bukkit.ChatColor.RED, getKit().getGameRules().isShowhealth());
    }

    @Override
    public void onStart() {
        if (getKit().getGameRules().isTimed()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!getState().equals(MatchState.FIGHTING))
                        return;

                    if (getDuration().equalsIgnoreCase("01:00") || (getDuration().equalsIgnoreCase("01:01") && getState().equals(MatchState.FIGHTING)) || (getDuration().equalsIgnoreCase("01:02") && getState().equals(MatchState.FIGHTING))) {
                        onEnd();
                        cancel();
                    }
                }
            }.runTaskTimer(Practice.get(), 20L, 20L);
        }
    }

    @Override
    public boolean onEnd() {
        UUID rematchKey = UUID.randomUUID();
        Map<UUID, InventorySnapshot> InventorySnapshotMAP = new LinkedHashMap<>();
        for (TeamPlayer teamPlayer : new TeamPlayer[]{getTeamPlayerA(), getTeamPlayerB()}) {
            if (!teamPlayer.isDisconnected() && teamPlayer.isAlive()) {
                Player player = teamPlayer.getPlayer();

                getSnapshots().values().forEach(snapshot -> Practice.get().getProfileManager().addSnapshot(snapshot));
                if (player != null) {
                    if (!hasSnapshot(player.getUniqueId())) {
                        addSnapshot(player);
                    }
                }
                assert player != null;
                InventorySnapshotMAP.put(player.getUniqueId(), getSnapshot(player.getUniqueId()));

                for (InventorySnapshot snapshot : getSnapshots().values()) {
                    Practice.get().getProfileManager().addSnapshot(snapshot);
                }
            }
        }

        if (getKit().getGameRules().isTimed()) {
            TeamPlayer roundLoser = getTeamPlayer(getWinningPlayer());
            TeamPlayer roundWinner = getOpponentTeamPlayer(getOpponentPlayer(getWinningPlayer()));

            if (!hasSnapshot(roundLoser.getPlayer().getUniqueId())) {
                addSnapshot(roundLoser.getPlayer());
            }

            if (roundWinner.getPlayer() != null) {
                if (!hasSnapshot(roundWinner.getPlayer().getUniqueId())) {
                    addSnapshot(roundWinner.getPlayer());
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (TeamPlayer teamPlayer : new TeamPlayer[]{getTeamPlayerA(), getTeamPlayerB()}) {
                    if (!teamPlayer.isDisconnected()) {
                        Player player = teamPlayer.getPlayer();
                        Player opponent = getOpponentPlayer(player);

                        if (player != null) {
                            NameTags.reset(player, opponent);

                            player.setFireTicks(0);
                            player.updateInventory();

                            Profile profile = Profile.getByUuid(player.getUniqueId());
                            profile.setState(ProfileState.IN_LOBBY);
                            profile.setMatch(null);
                            NameTags.reset(player, teamPlayer.getPlayer());
                            TaskUtil.runSync(profile::refreshHotbar);
                            profile.handleVisibility();
                            if (opponent != null) {
                                profile.setRematchData(new ProfileRematchData(rematchKey, player.getUniqueId(),
                                        opponent.getUniqueId(), getKit(), getArena()));
                            }

                            Practice.get().getEssentials().teleportToSpawn(player);
                        }
                    }
                }
            }
        }.runTaskLaterAsynchronously(Practice.get(), 125L);

        Player winningPlayer = getWinningPlayer();
        Player losingPlayer = getOpponentPlayer(winningPlayer);

        TeamPlayer winningTeamPlayer = getTeamPlayer(winningPlayer);
        TeamPlayer losingTeamPlayer = getTeamPlayer(losingPlayer);

        Profile winningProfile = Profile.getByUuid(winningPlayer.getUniqueId());
        Profile losingProfile = Profile.getByUuid(losingPlayer.getUniqueId());

        ChatComponentBuilder inventoriesBuilder = new ChatComponentBuilder("");

        inventoriesBuilder.append("Winner: ").color(ChatColor.GREEN).append(winningPlayer.getName()).color(ChatColor.YELLOW);
        inventoriesBuilder.setCurrentHoverEvent(getHoverEvent(winningTeamPlayer)).setCurrentClickEvent(getClickEvent(winningTeamPlayer)).append(" - ").color(ChatColor.GRAY).append("Loser: ").color(ChatColor.RED).append(losingPlayer.getName()).color(ChatColor.YELLOW);
        inventoriesBuilder.setCurrentHoverEvent(getHoverEvent(losingTeamPlayer)).setCurrentClickEvent(getClickEvent(losingTeamPlayer));

        List<BaseComponent[]> components = new ArrayList<>();
        components.add(new ChatComponentBuilder("").parse("&6&lMatch Details &7(click name to view)").create());
        components.add(inventoriesBuilder.create());

        ChatComponentBuilder playerABuilder = new ChatComponentBuilder("");
        ChatComponentBuilder playerBBuilder = new ChatComponentBuilder("");
        List<BaseComponent[]> playerAComponent = new ArrayList<>();
        List<BaseComponent[]> playerBComponent = new ArrayList<>();

        if (playerA.getPlayer() == winningPlayer) {
            winningProfile.getStatisticsData().get(getKit()).incrementWinStreak();
        } else if (playerA.getPlayer() == losingPlayer) {
            losingProfile.getStatisticsData().get(getKit()).setWinStreak(0);
        }

        if (playerB.getPlayer() == winningPlayer) {
            winningProfile.getStatisticsData().get(getKit()).incrementWinStreak();
        } else if (playerB.getPlayer() == losingPlayer) {
            losingProfile.getStatisticsData().get(getKit()).setWinStreak(0);
        }

        if (winningProfile.getStatisticsData().get(getKit()).getWinStreak() >= winningProfile.getStatisticsData().get(getKit()).getBestWinSterak())
            winningProfile.getStatisticsData().get(getKit()).setBestWinSterak(winningProfile.getStatisticsData().get(getKit()).getWinStreak());

        if (getQueueType() == QueueType.UNRANKED) {
            int oldWinnerElo = winningTeamPlayer.getUnrankedElo();
            int oldLoserElo = losingTeamPlayer.getUnrankedElo();
            int[] newWinnerElo = EloCalculator.getNewRankings(oldWinnerElo, oldLoserElo, true);

            winningProfile.getStatisticsData().get(getKit()).setUnrankedElo(newWinnerElo[0]);
            losingProfile.getStatisticsData().get(getKit()).setUnrankedElo(newWinnerElo[1]);
            winningProfile.getStatisticsData().get(getKit()).incrementUnrankedWon();
            losingProfile.getStatisticsData().get(getKit()).incrementUnrankedLost();
            winningProfile.calculateGlobalUnrankedElo();
            losingProfile.calculateGlobalUnrankedElo();
        }


        if (getQueueType() == QueueType.RANKED) {
            int oldWinnerElo = winningTeamPlayer.getRankedElo();
            int oldLoserElo = losingTeamPlayer.getRankedElo();
            int[] newWinnerElo = EloCalculator.getNewRankings(oldWinnerElo, oldLoserElo, true);
            int[] newLoserElo = EloCalculator.getNewRankings(oldLoserElo, oldWinnerElo, false);

            winningProfile.getStatisticsData().get(getKit()).setElo(newWinnerElo[0]);
            losingProfile.getStatisticsData().get(getKit()).setElo(newLoserElo[0]);
            winningProfile.getStatisticsData().get(getKit()).incrementRankedWon();
            losingProfile.getStatisticsData().get(getKit()).incrementRankedLost();
            winningProfile.calculateGlobalElo();
            losingProfile.calculateGlobalElo();

            int winnerEloChange = newWinnerElo[0] - oldWinnerElo;
            int loserEloChange = oldLoserElo - newWinnerElo[1];

            eloMessage = Practice.get().getMessagesConfig().getString("MATCH.ELO_CHANGES").replace("<winner_name>", winningPlayer.getName()).replace("<winner_elo_change>", String.valueOf(winnerEloChange)).replace("<winner_elo>", Arrays.toString(newWinnerElo)).replace("<loser_name>", losingPlayer.getName()).replace("<loser_elo_change>", String.valueOf(loserEloChange)).replace("<loser_elo>", Arrays.toString(newLoserElo));
        }


        StringBuilder builder = new StringBuilder();

        if (!(getSpectators().size() <= 0)) {
            ArrayList<Player> specs = new ArrayList<>(getSpectators());
            int i = 0;
            for (Player spectator : getSpectators()) {
                Profile profile = Profile.getByUuid(spectator.getUniqueId());
                if (getSpectators().size() >= 1) {
                    if (profile.isSilent()) {
                        specs.remove(spectator);
                    } else {
                        if (!specs.contains(spectator))
                            specs.add(spectator);
                    }
                    if (i != getSpectators().size()) {
                        i++;
                        if (i == getSpectators().size()) {
                            if (!profile.isSilent()) {
                                builder.append(CC.GRAY).append(spectator.getName());
                            }
                        } else {
                            if (!profile.isSilent()) {
                                builder.append(CC.GRAY).append(spectator.getName()).append(CC.GRAY).append(", ");
                            }
                        }

                    }
                }
            }
            if (specs.size() >= 1) {
                this.specMessage = Practice.get().getMessagesConfig().getString("MATCH.SPECTATORS").replace("<spec_size>", String.valueOf(specs.size())).replace("<spectators>", builder.substring(0, builder.length()));
            }
        }
        List<BaseComponent[]> AIR = new ArrayList<>();
        AIR.add(0, new ChatComponentBuilder("").parse("").create());

        for (Player player : new Player[]{winningPlayer, losingPlayer}) {
            AIR.forEach(components1 -> player.spigot().sendMessage(components1));
            components.forEach(components1 -> player.spigot().sendMessage(components1));
            AIR.forEach(components1 -> player.spigot().sendMessage(components1));
        }

        playerAComponent.add(playerABuilder.create());
        playerAComponent.forEach(components1 -> playerA.getPlayer().sendMessage(components1));
        playerBComponent.add(playerBBuilder.create());
        playerBComponent.forEach(components1 -> playerB.getPlayer().sendMessage(components1));

        for (Player player : this.getSpectators()) {
            AIR.forEach(components1 -> player.spigot().sendMessage(components1));
            components.forEach(components1 -> player.spigot().sendMessage(components1));
            AIR.forEach(components1 -> player.spigot().sendMessage(components1));
        }
        if (getKit().getGameRules().isSumo()) {
            if (getMatchWaterCheck() != null) {
                getMatchWaterCheck().cancel();
            }

            winningProfile.setSumoRounds(0);
            losingProfile.setSumoRounds(0);
        }

        winningProfile.getKitEditor().setSelectedKitInventory(null);
        losingProfile.getKitEditor().setSelectedKitInventory(null);

        return true;
    }

    @Override
    public boolean canEnd() {
        if (getRoundsNeeded(playerA) == 3 || getRoundsNeeded(playerB) == 3)
            return true;
        return !playerA.isAlive() || !playerB.isAlive();
    }

    @Override
    public Player getWinningPlayer() {
        if (getKit().getGameRules().isTimed()) {
            if (playerA.isDisconnected()) {
                return playerB.getPlayer();
            } else if (playerB.isDisconnected()) {
                return playerB.getPlayer();
            } else if (playerA.getHits() > playerB.getHits()) {
                return playerA.getPlayer();
            } else {
                return playerB.getPlayer();
            }
        } else {
            if (playerA.isDisconnected() || !playerA.isAlive()) {
                return playerB.getPlayer();
            } else {
                return playerA.getPlayer();
            }
        }
    }

    @Override
    public Team getWinningTeam() {
        throw new UnsupportedOperationException("Cannot getInstance winning team from a SoloMatch");
    }

    @Override
    public TeamPlayer getTeamPlayerA() {
        return playerA;
    }

    @Override
    public TeamPlayer getTeamPlayerB() {
        return playerB;
    }

    @Override
    public List<TeamPlayer> getTeamPlayers() {
        return Arrays.asList(playerA, playerB);
    }

    @Override
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();

        Player playerA = this.playerA.getPlayer();

        if (playerA != null) {
            players.add(playerA);
        }

        Player playerB = this.playerB.getPlayer();

        if (playerB != null) {
            players.add(playerB);
        }

        return players;
    }

    @Override
    public List<Player> getAlivePlayers() {
        List<Player> players = new ArrayList<>();

        Player playerA = this.playerA.getPlayer();

        if (playerA != null) {
            players.add(playerA);
        }

        Player playerB = this.playerB.getPlayer();

        if (playerB != null) {
            players.add(playerB);
        }

        return players;
    }

    @Override
    public Team getTeamA() {
        throw new UnsupportedOperationException("Cannot getInstance team from a SoloMatch");
    }

    @Override
    public Team getTeamB() {
        throw new UnsupportedOperationException("Cannot getInstance team from a SoloMatch");
    }

    @Override
    public Team getTeam(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance team from a SoloMatch");
    }

    @Override
    public TeamPlayer getTeamPlayer(Player player) {
        if (player == null) {
            return null;
        }

        if (playerA.getUuid().equals(player.getUniqueId())) {
            return playerA;
        } else if (playerB.getUuid().equals(player.getUniqueId())) {
            return playerB;
        } else {
            return null;
        }
    }

    @Override
    public Team getOpponentTeam(Team team) {
        throw new UnsupportedOperationException("Cannot getInstance opponent team from a SoloMatch");
    }

    @Override
    public Team getOpponentTeam(Player player) {
        throw new UnsupportedOperationException("Cannot getInstance opponent team from a SoloMatch");
    }

    @Override
    public Player getOpponentPlayer(Player player) {
        if (player == null) {
            return null;
        }

        if (playerA.getUuid().equals(player.getUniqueId())) {
            return playerB.getPlayer();
        } else if (playerB.getUuid().equals(player.getUniqueId())) {
            return playerA.getPlayer();
        } else {
            return null;
        }
    }

    @Override
    public TeamPlayer getOpponentTeamPlayer(Player player) {
        if (player == null) {
            return null;
        }

        if (playerA.getUuid().equals(player.getUniqueId())) {
            return playerB;
        } else if (playerB.getUuid().equals(player.getUniqueId())) {
            return playerA;
        } else {
            return null;
        }
    }

    @Override
    public int getRoundsNeeded(TeamPlayer teamPlayer) {
        Profile aProfile = Profile.getByUuid(playerA.getUuid());
        Profile bProfile = Profile.getByUuid(playerB.getUuid());

        if (getKit().getGameRules().isSumo()) {
            if (playerA.equals(teamPlayer)) {
                return 3 - aProfile.getSumoRounds();
            } else if (playerB.equals(teamPlayer)) {
                return 3 - bProfile.getSumoRounds();
            } else {
                return -1;
            }
        } else {
            if (playerA.equals(teamPlayer)) {
                return 3;
            } else if (playerB.equals(teamPlayer)) {
                return 3;
            } else {
                return -1;
            }
        }
    }

    @Override
    public int getRoundsNeeded(Team team) {
        throw new UnsupportedOperationException("Cannot getInstance team round wins from SoloMatch");
    }

    @Override
    public void onDeath(Player deadPlayer, Player killerPlayer) {
        TeamPlayer roundLoser = getTeamPlayer(deadPlayer);
        TeamPlayer roundWinner = getOpponentTeamPlayer(deadPlayer);

        if (!hasSnapshot(roundLoser.getPlayer().getUniqueId())) {
            addSnapshot(roundLoser.getPlayer());
        }

        if (roundWinner.getPlayer() != null) {
            if (!hasSnapshot(roundWinner.getPlayer().getUniqueId())) {
                addSnapshot(roundWinner.getPlayer());
            }
        }

        PlayerUtil.reset(deadPlayer, true);

        for (Player otherPlayer : getPlayersAndSpectators()) {
            Profile profile = Profile.getByUuid(otherPlayer.getUniqueId());
            profile.handleVisibility(otherPlayer, deadPlayer);
        }
        if (getKit().getGameRules().isSumo()) {
            Profile aProfile = Profile.getByUuid(playerA.getUuid());
            Profile bProfile = Profile.getByUuid(playerB.getUuid());

            if (getQueueType() == QueueType.RANKED) {
                if (deadPlayer.isOnline()) {
                    if (getRoundsNeeded(playerA) != 0 || getRoundsNeeded(playerB) != 0) {
                        if (getWinningPlayer().getUniqueId().toString().equals(playerA.getUuid().toString())) {
                            aProfile.setSumoRounds(aProfile.getSumoRounds() + 1);
                        } else if (getWinningPlayer().getUniqueId().toString().equals(playerB.getUuid().toString())) {
                            bProfile.setSumoRounds(bProfile.getSumoRounds() + 1);
                        }

                        if (aProfile.getSumoRounds() >= 3 || bProfile.getSumoRounds() >= 3) {

                            if (!hasSnapshot(roundLoser.getPlayer().getUniqueId())) {
                                addSnapshot(roundLoser.getPlayer());
                            }

                            if (roundWinner.getPlayer() != null) {
                                if (!hasSnapshot(roundWinner.getPlayer().getUniqueId())) {
                                    addSnapshot(roundWinner.getPlayer());
                                }
                            }

                            PlayerUtil.reset(deadPlayer);

                            for (Player otherPlayer : getPlayersAndSpectators()) {
                                Profile profile = Profile.getByUuid(otherPlayer.getUniqueId());
                                profile.handleVisibility(otherPlayer, deadPlayer);
                            }
                            setState(MatchState.ENDING);
                            end();
                        } else {
                            setupPlayer(playerA.getPlayer());
                            setupPlayer(playerB.getPlayer());

                            playerA.getPlayer().showPlayer(playerB.getPlayer());
                            playerB.getPlayer().showPlayer(playerA.getPlayer());

                            onStart();
                            setState(MatchState.STARTING);
                            setStartTimestamp(-1);
                            new MatchStartTask(this).runTaskTimer(Practice.get(), 20L, 20L);
                        }
                    }
                }
            } else {

                if (!hasSnapshot(roundLoser.getPlayer().getUniqueId())) {
                    addSnapshot(roundLoser.getPlayer());
                }

                if (roundWinner.getPlayer() != null) {
                    if (!hasSnapshot(roundWinner.getPlayer().getUniqueId())) {
                        addSnapshot(roundWinner.getPlayer());
                    }
                }

                PlayerUtil.reset(deadPlayer);

                for (Player otherPlayer : getPlayersAndSpectators()) {
                    Profile profile = Profile.getByUuid(otherPlayer.getUniqueId());
                    profile.handleVisibility(otherPlayer, deadPlayer);
                }
                setState(MatchState.ENDING);
                end();
            }
        }
    }

    @Override
    public void onRespawn(Player player) {
        Practice.get().getEssentials().teleportToSpawn(player);
    }

    public void onWater(Player player) {
        TeamPlayer teamPlayer = getTeamPlayer(player);
        if (teamPlayer == null)
            return;
        if (!isFighting())
            return;
        PlayerUtil.reset(playerA.getPlayer());
        PlayerUtil.reset(playerB.getPlayer());
        end();
    }

    @Override
    public org.bukkit.ChatColor getRelationColor(Player viewer, Player target) {
        if (viewer.equals(target)) {
            return org.bukkit.ChatColor.GREEN;
        }

        if (playerA.getUuid().equals(viewer.getUniqueId()) || playerB.getUuid().equals(viewer.getUniqueId())) {
            return org.bukkit.ChatColor.RED;
        } else {
            return org.bukkit.ChatColor.DARK_RED;
        }
    }
}