package me.cprox.practice.adapters;

import me.cprox.practice.arena.Arena;
import me.cprox.practice.events.brackets.Brackets;
import me.cprox.practice.events.spleef.Spleef;
import me.cprox.practice.events.sumo.Sumo;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.impl.solo.*;
import me.cprox.practice.match.team.Team;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.queue.Queue;
import me.cprox.practice.queue.QueueProfile;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.config.BasicConfigurationFile;
import me.cprox.practice.util.external.TimeUtil;
import me.cprox.practice.util.scoreboard.scoreboard.Board;
import me.cprox.practice.util.scoreboard.scoreboard.BoardAdapter;
import me.cprox.practice.util.scoreboard.scoreboard.cooldown.BoardCooldown;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.cprox.practice.util.TaskUtil;
import me.cprox.practice.Practice;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Scoreboard implements BoardAdapter {

    public static String title;

    @Override
    public String getTitle(Player player) {
        return CC.translate(getScoreboardTitle());
    }

    @Override
    public List<String> getScoreboard(Player player, Board board, Set<BoardCooldown> cooldowns) {
        Profile profile = Profile.getByUuid(player.getUniqueId());
        final List<String> lines = new ArrayList<>();
        BasicConfigurationFile config = Practice.get().getScoreboardConfig();
        Match match = profile.getMatch();

        if (!profile.getSettings().isShowScoreboard()) {
            return null;
        }

        lines.add(config.getStringOrDefault("SCOREBOARD.BARS", "&7----------------------"));

        if (profile.isInLobby() || profile.isInQueue() || profile.isManaging()) {
            config.getStringList("SCOREBOARD.LOBBY").forEach(string ->
                    lines.add(CC.translate(string)
                            .replace("<online>", String.valueOf(Bukkit.getServer().getOnlinePlayers().size()))
                            .replace("<playing>", String.valueOf(getInFights()))
                            .replace("<queuing>", String.valueOf(getInQueues()))
                            .replace("<elo>", String.valueOf(profile.getGlobalElo()))
                            .replace("<league>", String.valueOf(profile.getEloLeague()))));

            if (profile.isInQueue()) {
                QueueProfile queueProfile = profile.getQueueProfile();
                final Queue queue = profile.getQueue();
                switch (queue.getType()) {
                    case UNRANKED:
                        config.getStringList("SCOREBOARD.QUEUE.UNRANKED").forEach(string -> lines.add(CC.translate(string)
                                .replace("<queue>", queue.getQueueName())
                                .replace("<duration>", TimeUtil.millisToTimer(queueProfile.getPassed()))));
                        break;
                    case RANKED:
                        config.getStringList("SCOREBOARD.QUEUE.RANKED").forEach(string -> lines.add(CC.translate(string)
                                .replace("<queue>", queue.getQueueName())
                                .replace("<range>", getEloRangeFormat(profile))
                                .replace("<duration>", TimeUtil.millisToTimer(queueProfile.getPassed()))));
                        break;
                }
                config.getStringList("SCOREBOARD.QUEUE").forEach(string -> lines.add(CC.translate(string)
                        .replace("<queue>", queue.getQueueName())
                        .replace("<range>", getEloRangeFormat(profile))
                        .replace("<duration>", TimeUtil.millisToTimer(queueProfile.getPassed()))));
            } else if (profile.getState() == ProfileState.MANAGING) {
                lines.add("");
                lines.add("&6&lPractice Manager");
                lines.add("&fArenas: &6" + Arena.getArenas().size());
                lines.add("&fKits: &6" + Kit.getKits().size());
                //lines.add("&fFights: &6" + Match.getMatches());
            }
        } else if (profile.isInFight()) {
            if (match.isSoloMatch()) {
                if (match.isStarting()) {
                    final TeamPlayer self = match.getTeamPlayer(player);
                    final TeamPlayer opponent = match.getOpponentTeamPlayer(player);

                    config.getStringList("SCOREBOARD.MATCH.STARTING").forEach(string -> lines.add(CC.translate(string)
                            .replace("<opponent>", opponent.getUsername())
                            .replace("<yourping>", String.valueOf(self.getPing()))
                            .replace("<targetping>", String.valueOf(opponent.getPing()))));
                } else if (match.isFighting()) {
                    if (match.getKit().getGameRules().isBoxing()) {
                        final TeamPlayer self = match.getTeamPlayer(player);
                        final TeamPlayer opponent = match.getOpponentTeamPlayer(player);
                        String hitDifferent = (self.getHits() < opponent.getHits()) ? (CC.translate("&7(&c-" + (Math.max(self.getHits(), opponent.getHits()) - Math.min(self.getHits(), opponent.getHits())) + CC.translate("&7)"))) : (CC.translate("&7(&a+&a" + (Math.max(self.getHits(), opponent.getHits()) - Math.min(self.getHits(), opponent.getHits())) + CC.translate("&7)")));
                        String isSelfCombo = (self.getCombo() >= 2 ? self.getCombo() + " Combo" : "&f");
                        String isOpponentCombo = (opponent.getCombo() >= 2 ? opponent.getCombo() + " Combo" : "&f");

                        config.getStringList("SCOREBOARD.MATCH.STARTED.BOXING").forEach(string -> lines.add(CC.translate(string)
                                .replace("<opponent>", opponent.getUsername())
                                .replace("<duration>", String.valueOf(match.getDuration()))
                                .replace("<yourping>", String.valueOf(self.getPing()))
                                .replace("<targetping>", String.valueOf(opponent.getPing()))
                                .replace("<hits>", hitDifferent)
                                .replace("<yourhits>", String.valueOf(self.getHits()))
                                .replace("<targethits>", String.valueOf(opponent.getHits()))
                                .replace("<isselfcombo>", isSelfCombo)
                                .replace("<istargetcombo>", isOpponentCombo)));
                    } else if (match.getKit().getGameRules().isBridge()) {
                        final TeamPlayer self = match.getTeamPlayer(player);
                        final TeamPlayer opponent = match.getOpponentTeamPlayer(player);

                        config.getStringList("SCOREBOARD.MATCH.STARTED.BRIDGE").forEach(string -> lines.add(CC.translate(string)
                                .replace("<opponent>", opponent.getUsername())
                                .replace("<duration>", String.valueOf(match.getDuration()))
                                .replace("<yourping>", String.valueOf(self.getPing()))
                                .replace("<targetping>", String.valueOf(opponent.getPing()))
                                .replace("<red>", getBridgePoints(match.getTeamPlayerA().getPlayer()))
                                .replace("<blue>", getBridgePoints(match.getTeamPlayerB().getPlayer()))
                                .replace("<kills>", String.valueOf(self.getTheBridgeKills()))
                                .replace("<team>", ((match.getTeamPlayerA().getPlayer() == player) ? config.getString("SCOREBOARD.TEAM_PREFIX") : ""))
                                .replace("<targetteam>", ((match.getTeamPlayerB().getPlayer() == player) ? config.getString("SCOREBOARD.TEAM_PREFIX") : ""))));
                    } else if (match.getKit().getGameRules().isBedFight()) {
                        final TeamPlayer self = match.getTeamPlayer(player);
                        final TeamPlayer opponent = match.getOpponentTeamPlayer(player);
                        SoloBedFightMatch bedFightMatch = (SoloBedFightMatch) match;
                        String isRedBed = (bedFightMatch.getRedBed() ? config.getString("SCOREBOARD.BED.RED_YES") : "SCOREBOARD.BED.RED_NO");
                        String isBlueBed = (bedFightMatch.getBlueBed() ? config.getString("SCOREBOARD.BED.BLUE_YES") : "SCOREBOARD.BED.BLUE_NO");


                        config.getStringList("SCOREBOARD.MATCH.STARTED.BEDFIGHT").forEach(string -> lines.add(CC.translate(string)
                                .replace("<opponent>", opponent.getUsername())
                                .replace("<duration>", String.valueOf(match.getDuration()))
                                .replace("<yourping>", String.valueOf(self.getPing()))
                                .replace("<targetping>", String.valueOf(opponent.getPing()))
                                .replace("<red>", isRedBed)
                                .replace("<blue>", isBlueBed)
                                .replace("<kills>", String.valueOf(self.getBedFightKills()))
                                .replace("<team>", ((match.getTeamPlayerA().getPlayer() == player) ? config.getString("SCOREBOARD.TEAM_PREFIX") : ""))
                                .replace("<targetteam>", ((match.getTeamPlayerB().getPlayer() == player) ? config.getString("SCOREBOARD.TEAM_PREFIX") : ""))));
                    } else if (match.getKit().getGameRules().isBattleRush()) {
                        final TeamPlayer self = match.getTeamPlayer(player);
                        final TeamPlayer opponent = match.getOpponentTeamPlayer(player);

                        config.getStringList("SCOREBOARD.MATCH.STARTED.BATTLERUSH").forEach(string -> lines.add(CC.translate(string)
                                .replace("<opponent>", opponent.getUsername())
                                .replace("<duration>", String.valueOf(match.getDuration()))
                                .replace("<yourping>", String.valueOf(self.getPing()))
                                .replace("<targetping>", String.valueOf(opponent.getPing()))
                                .replace("<red>", getBattleRushPoints(match.getTeamPlayerA().getPlayer()))
                                .replace("<blue>", getBattleRushPoints(match.getTeamPlayerB().getPlayer()))
                                .replace("<kills>", String.valueOf(self.getBattleRushKills()))
                                .replace("<team>", ((match.getTeamPlayerA().getPlayer() == player) ? config.getString("SCOREBOARD.TEAM_PREFIX") : ""))
                                .replace("<targetteam>", ((match.getTeamPlayerB().getPlayer() == player) ? config.getString("SCOREBOARD.TEAM_PREFIX") : ""))));
                    } else if (match.getKit().getGameRules().isSumo()) {
                        TeamPlayer self = match.getTeamPlayer(player);
                        TeamPlayer opponent = match.getOpponentTeamPlayer(player);
                        config.getStringList("SCOREBOARD.MATCH.STARTED.SUMO").forEach(string -> lines.add(CC.translate(string)
                                .replace("<opponent>", opponent.getUsername())
                                .replace("<duration>", String.valueOf(match.getDuration()))
                                .replace("<yourping>", String.valueOf(self.getPing()))
                                .replace("<targetping>", String.valueOf(opponent.getPing()))));
                    } else {
                        final TeamPlayer self = match.getTeamPlayer(player);
                        final TeamPlayer opponent = match.getOpponentTeamPlayer(player);

                        config.getStringList("SCOREBOARD.MATCH.STARTED.DEFAULT").forEach(string ->
                                lines.add(CC.translate(string)
                                        .replace("<opponent>", opponent.getUsername())
                                        .replace("<duration>", String.valueOf(match.getDuration()))
                                        .replace("<yourping>", String.valueOf(self.getPing()))
                                        .replace("<targetping>", String.valueOf(opponent.getPing()))));
                    }
                } else if (match.isEnding()) {
                    TeamPlayer self = match.getTeamPlayer(player);
                    TeamPlayer opponent = match.getOpponentTeamPlayer(player);
                    config.getStringList("SCOREBOARD.MATCH.ENDING").forEach(string -> lines.add(CC.translate(string)
                            .replace("<opponent>", opponent.getUsername())
                            .replace("<duration>", String.valueOf(match.getDuration()))
                            .replace("<yourping>", String.valueOf(self.getPing()))
                            .replace("<targetping>", String.valueOf(opponent.getPing()))));
                }
            } else if (match.isTeamMatch()) {
                if (match.isStarting()) {
                    final Team team = match.getTeam(player);
                    final Team opponentTeam = match.getOpponentTeam(player);

                    if ((team.getPlayers().size() + opponentTeam.getPlayers().size()) == 2) {
                        lines.add("&fOpponent Team:");
                        lines.add(" &c" + opponentTeam.getTeamPlayers().get(0).getUsername());
                    } else if (team.getPlayers().size() <= 3 && opponentTeam.getPlayers().size() <= 3) {
                        lines.add("&fOpponent Team:");
                        for (TeamPlayer opponentPlayers : opponentTeam.getTeamPlayers()) {
                            lines.add(" &c" + opponentPlayers.getUsername());
                        }
                    }

                } else if (match.isFighting()) {
                    final Team team = match.getTeam(player);
                    final Team opponentTeam = match.getOpponentTeam(player);

                    if ((team.getPlayers().size() + opponentTeam.getPlayers().size()) == 2) {
                        lines.add("&fYour Team:");
                        lines.add(" &a" + team.getTeamPlayers().get(0).getUsername() + " " + team.getTeamPlayers().get(0).getPing() + "ms");
                        lines.add("");
                        lines.add("&fOpponent Team:");
                        lines.add(" &c" + opponentTeam.getTeamPlayers().get(0).getUsername() + " " + opponentTeam.getTeamPlayers().get(0).getPing() + "ms");
                    } else if (team.getPlayers().size() <=4 && opponentTeam.getPlayers().size() <= 4) {
                        lines.add("&fYour Team:");
                        for (TeamPlayer teamPlayers : team.getTeamPlayers()) {
                            if (teamPlayers.isAlive()) {
                                lines.add(" &a" + teamPlayers.getUsername() + " &8" + teamPlayers.getPing() + "ms");
                                lines.add(getHearts(teamPlayers.getPlayer()) + " &7┃ " + getPots(teamPlayers.getPlayer()));
                            } else {
                                lines.add(" &7&m" + teamPlayers.getUsername());
                                lines.add(" &4Dead");
                            }
                        }

                        lines.add("&fOpponent Team:");
                        for (TeamPlayer opponentPlayers : opponentTeam.getTeamPlayers()) {
                            if (opponentPlayers.isAlive()) {
                                lines.add(" &c" + opponentPlayers.getUsername() + " &8" + opponentPlayers.getPing() + "ms");
                            } else {
                                lines.add(" &7&m" + opponentPlayers.getUsername());
                                lines.add(" &4Dead");
                            }
                        }
                    }
                } else if (match.isEnding()) {
                    lines.add("Match ended");
                }
            } else if (match.isFreeForAllMatch()) {
                    final Team team = match.getTeam(player);

                    lines.add("&7Players: &4" + team.getAliveCount());
                    lines.add("&7Duration: &4" + match.getDuration());
                } else if (match.isBotMatch()) {
                    lines.add("&7Duration: &4" + match.getDuration());
                    lines.add("");
                    lines.add("&7Your Ping: &40" + "ms");
                }
        } else if (profile.isSpectating()) {
            final Sumo sumo = profile.getSumo();
            final Brackets brackets = profile.getBrackets();
                        final Spleef spleef = profile.getSpleef();

            if (match != null) {
                lines.add("&7Duration: &4" + match.getDuration());
                lines.add("");

                if (match.isSoloMatch() || match.getKit().getGameRules().isSumo()) {
                    int playera = PlayerUtil.getPing(match.getTeamPlayerA().getPlayer());
                    int playerb = PlayerUtil.getPing(match.getTeamPlayerB().getPlayer());

                    lines.add(CC.GREEN + match.getTeamPlayerA().getUsername() + CC.translate(" &8(&2" + playera + "&8)"));
                    lines.add("&7vs");
                    lines.add(CC.RED + match.getTeamPlayerB().getUsername() + CC.translate(" &8(&4" + playerb + "&8)"));
                } else if (match.isTeamMatch()) {
                    lines.add("&4" + match.getTeamA().getLeader().getUsername() + "'s Team &8(&7" + match.getTeamA().getPlayers().size() + "&8)");
                    lines.add("&7vs");
                    lines.add("&4" + match.getTeamB().getLeader().getUsername() + "'s Team &8(&7" + match.getTeamB().getPlayers().size() + "&8)");
                } else {
                    final Team team2 = match.getTeam(player);

                    lines.add("&7Alive: &4" + team2.getAliveCount() + "/" + team2.getTeamPlayers().size());
                }
            } else if (sumo != null) {

                if (sumo.isWaiting()) {
                    lines.add("&4&lSumo Event");
                    lines.add("");
                    lines.add(CC.translate("&7Host: &4" + sumo.getName()));
                    lines.add("&7Players: &4" + sumo.getEventPlayers().size() + "&8/&4" + Sumo.getMaxPlayers());
                    lines.add("");

                    if (sumo.getCooldown() == null) {
                        lines.add(CC.translate("&7Waiting for players..."));
                    } else {
                        String remaining = TimeUtil.millisToSeconds(sumo.getCooldown().getRemaining());
                        if (remaining.startsWith("-")) {
                            remaining = "0";
                        }
                        lines.add("&7Starting in " + CC.GOLD + remaining + "&7s");
                    }
                } else {
                    int playera = PlayerUtil.getPing(sumo.getRoundPlayerA().getPlayer());
                    int playerb = PlayerUtil.getPing(sumo.getRoundPlayerB().getPlayer());

                    lines.add("&4&lSumo Event");
                    lines.add("");
                    lines.add("&7Round Duration: &4" + sumo.getRoundDuration());
                    lines.add("&7Players: &4" + sumo.getRemainingPlayers().size() + "&8/&4" + Sumo.getMaxPlayers());
                    lines.add("");
                    lines.add("&a" + sumo.getRoundPlayerA().getUsername() + " &8(&2" + playera + "&8)");
                    lines.add("&7vs");
                    lines.add("&c" + sumo.getRoundPlayerB().getUsername() + " &8(&4" + playerb + "&8)");
                }
            } else if (brackets != null) {
                if (brackets.isWaiting()) {
                    lines.add("&4&lBrackets Event");
                    lines.add("");
                    lines.add(CC.translate("&7Host: &4" + brackets.getName()));
                    lines.add("&7Players: &4" + brackets.getEventPlayers().size() + "&8/&4" + Brackets.getMaxPlayers());
                    lines.add("");
                    if (brackets.getCooldown() == null) {
                        lines.add(CC.translate("&7Waiting for players..."));
                    } else {
                        String remaining = TimeUtil.millisToSeconds(brackets.getCooldown().getRemaining());
                        if (remaining.startsWith("-")) {
                            remaining = "0.0";
                        }
                        lines.add(CC.translate("&7Starting in " + remaining + "s"));
                    }
                } else {
                    int playera = PlayerUtil.getPing(brackets.getRoundPlayerA().getPlayer());
                    int playerb = PlayerUtil.getPing(brackets.getRoundPlayerB().getPlayer());

                    lines.add("&4&lBrackets Event");
                    lines.add("");
                    lines.add("&7Round Duration: &4" + brackets.getRoundDuration());
                    lines.add("&7Players: &4" + brackets.getRemainingPlayers().size() + "&8/&4" + Brackets.getMaxPlayers());
                    lines.add("");
                    lines.add("&a" + brackets.getRoundPlayerA().getUsername() + " &8(&2" + playera + "&8)");
                    lines.add("&7vs");
                    lines.add("&c" + brackets.getRoundPlayerB().getUsername() + " &8(&4" + playerb + "&8)");
                }
            } else if (spleef != null) {
                if (spleef.isWaiting()) {
                    lines.add("&4&lSpleef Event");
                    lines.add("");
                    lines.add(CC.translate("&7Host: &4" + spleef.getName()));
                    lines.add("&7Players: &4" + spleef.getEventPlayers().size() + "/" + Spleef.getMaxPlayers());
                    lines.add("");
                    if (spleef.getCooldown() == null) {
                        lines.add(CC.translate("&7Waiting for players..."));
                    } else {
                        String remaining = TimeUtil.millisToSeconds(spleef.getCooldown().getRemaining());
                        if (remaining.startsWith("-")) {
                            remaining = "0.0";
                        }
                        lines.add(CC.translate("&7Starting in " + remaining + "s"));
                    }
                } else {
                    lines.add("&4&lSpleef Event");
                    lines.add("");
                    lines.add("&7Players: &4" + spleef.getRemainingPlayers().size() + "/" + Spleef.getMaxPlayers());
                    lines.add("&7Duration: &4" + spleef.getRoundDuration());
                }
            }
        } else if (profile.isInSumo()) {
            final Sumo sumo2 = profile.getSumo();

            if (sumo2.isWaiting()) {
                lines.add("&4&lSumo Event");
                lines.add("");
                lines.add(CC.translate("&7Host: &4" + sumo2.getName()));
                lines.add("&7Players: &4" + sumo2.getEventPlayers().size() + "&8/&4" + Sumo.getMaxPlayers());
                lines.add("");
                if (sumo2.getCooldown() == null) {
                    lines.add(CC.translate("&7Waiting for players..."));
                } else {
                    String remaining = TimeUtil.millisToSeconds(sumo2.getCooldown().getRemaining());
                    if (remaining.startsWith("-")) {
                        remaining = "0";
                    }
                    lines.add(CC.translate("&7Starting in: &4" + CC.GOLD + remaining));
                }
            } else {
                int playera = PlayerUtil.getPing(sumo2.getRoundPlayerA().getPlayer());
                int playerb = PlayerUtil.getPing(sumo2.getRoundPlayerB().getPlayer());

                lines.add("&4&lSumo Event");
                lines.add("");
                lines.add("&7Round Duration: &4" + sumo2.getRoundDuration());
                lines.add("&7Players: &4" + sumo2.getRemainingPlayers().size() + "&8/&4" + Sumo.getMaxPlayers());
                lines.add("");
                lines.add("&a" + sumo2.getRoundPlayerA().getUsername() + " &8(&2" + playera + "&8)");
                lines.add("&7vs");
                lines.add("&c" + sumo2.getRoundPlayerB().getUsername() + " &8(&4" + playerb + "&8)");
            }
        } else if (profile.isInBrackets()) {
            final Brackets brackets2 = profile.getBrackets();

            if (brackets2.isWaiting()) {
                lines.add("&4&lBrackets Event");
                lines.add("");
                lines.add(CC.translate("&7Host: &4" + brackets2.getName()));
                lines.add("&7Players: &4" + brackets2.getEventPlayers().size() + "/" + Brackets.getMaxPlayers());
                lines.add("");
                if (brackets2.getCooldown() == null) {
                    lines.add(CC.translate("&7Waiting for players..."));
                } else {
                    String remaining = TimeUtil.millisToSeconds(brackets2.getCooldown().getRemaining());
                    if (remaining.startsWith("-")) {
                        remaining = "0.0";
                    }
                    lines.add(CC.translate("&7Starting in " + remaining + "s"));
                }
            } else {
                int playera = PlayerUtil.getPing(brackets2.getRoundPlayerA().getPlayer());
                int playerb = PlayerUtil.getPing(brackets2.getRoundPlayerB().getPlayer());

                lines.add("&4&lBrackets Event");
                lines.add("");
                lines.add("&7Players: &4" + brackets2.getRemainingPlayers().size() + "/" + Brackets.getMaxPlayers());
                lines.add("&7Duration: &4" + brackets2.getRoundDuration());
                lines.add("");
                lines.add("&a" + brackets2.getRoundPlayerA().getUsername() + " &8(&2" + playera + "&8)");
                lines.add("&7vs");
                lines.add("&c" + brackets2.getRoundPlayerB().getUsername() + " &8(&4" + playerb + "&8)");
            }
        } else if (profile.isInSpleef()) {
            final Spleef spleef2 = profile.getSpleef();

            if (spleef2.isWaiting()) {
                lines.add("&4&lSpleef Event");
                lines.add("");
                lines.add(CC.translate("&7Host: &4" + spleef2.getName()));
                lines.add("&7Players: &4" + spleef2.getEventPlayers().size() + "/" + Spleef.getMaxPlayers());
                lines.add("");

                if (spleef2.getCooldown() == null) {
                    lines.add(CC.translate("&7Waiting for players..."));
                } else {
                    String remaining = TimeUtil.millisToSeconds(spleef2.getCooldown().getRemaining());
                    if (remaining.startsWith("-")) {
                        remaining = "0.0";
                    }
                    lines.add(CC.translate("&7Starting in " + remaining + "s"));
                }
            } else {
                lines.add("&4&lSpleef Event");
                lines.add("");
                lines.add("&7Players: &4" + spleef2.getRemainingPlayers().size() + "/" + Spleef.getMaxPlayers());
                lines.add("&7Duration: &4" + spleef2.getRoundDuration());
            }
        }

        lines.add("");
        lines.add(config.getStringOrDefault("SCOREBOARD.FOOTER", "&7rest.rip"));
        lines.add(config.getStringOrDefault("SCOREBOARD.BARS", "&7----------------------"));
        return lines;
    }

    public String getBridgePoints(Player paramPlayer) {
        Profile profile = Profile.getByUuid(paramPlayer);
        Match match = profile.getMatch();
        String str = "&a";
        int i = 0;

        if (profile.isInFight()) {
            if (match.getKit().getGameRules().isBridge()) {
                TeamPlayer teamPlayer = match.getTeamPlayer(paramPlayer);
                SoloBridgeMatch bridgeMatch = (SoloBridgeMatch) match;
                i = match.getTeamPlayerA().equals(teamPlayer) ? bridgeMatch.getPlayerAPoints() : bridgeMatch.getPlayerBPoints();
                str = match.getTeamPlayerA().equals(teamPlayer) ? "&c" : "&9";
            }
        }

        switch (i) {
            case 5:
                return CC.translate(str + "⬤⬤⬤⬤⬤");
            case 4:
                return CC.translate(str + "⬤⬤⬤⬤&7⬤");
            case 3:
                return CC.translate(str + "⬤⬤⬤&7⬤⬤");
            case 2:
                return CC.translate(str + "⬤⬤&7⬤⬤⬤");
            case 1:
                return CC.translate(str + "⬤&7⬤⬤⬤⬤");
        }
        return CC.translate("&7⬤⬤⬤⬤⬤");
    }

    public String getBattleRushPoints(Player paramPlayer) {
        Profile profile = Profile.getByUuid(paramPlayer);
        Match match = profile.getMatch();
        String str = "&a";
        int i = 0;

        if (profile.isInFight()) {
            if (match.getKit().getGameRules().isBattleRush()) {
                TeamPlayer teamPlayer = match.getTeamPlayer(paramPlayer);
                SoloBattleRushMatch battleRushMatch = (SoloBattleRushMatch) match;
                i = match.getTeamPlayerA().equals(teamPlayer) ? battleRushMatch.getPlayerAPoints() : battleRushMatch.getPlayerBPoints();
                str = match.getTeamPlayerA().equals(teamPlayer) ? "&c" : "&9";
            }
        }

        switch (i) {
            case 1:
                return CC.translate(str + "⬤&7⬤⬤");
            case 2:
                return CC.translate(str + "⬤⬤&7⬤");
            case 3:
                return CC.translate(str + "⬤⬤⬤");
        }
        return CC.translate("&7⬤⬤⬤");
    }

    public static int getInFights() {
        int inFights = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.isInFight() || profile.isInEvent()) {
                inFights++;
            }
        }
        return inFights;
    }

    public static int getInQueues() {
        int inQueues = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.isInQueue()) {
                inQueues++;
            }
        }

        return inQueues;
    }

    public static void create() {
        List<String> titles = Practice.get().getScoreboardConfig().getStringList("SCOREBOARD.HEADER");
        final int[] p = {0};
        TaskUtil.runTimer(() -> {
            if (p[0] == titles.size()) p[0] = 0;
            title = titles.get(p[0]++);
        }, 0L, (long) (Practice.get().getScoreboardConfig().getDouble("SCOREBOARD.UPDATE_DELAY") * 20L));
    }

    public static String getScoreboardTitle() {
        return title;
    }

    public String getEloRangeFormat(Profile profile) {
        return Practice.get().getScoreboardConfig().getStringOrDefault("SCOREBOARD.ELO_FORMAT", "<min_range> -> <max_range>")
                .replace("<min_range>", String.valueOf(profile.getQueueProfile().getMinRange()))
                .replace("<max_range>", String.valueOf(profile.getQueueProfile().getMaxRange()));
    }

    public String getHearts(Player player) {
        double health = player.getHealth() / 2;
        String color = "&a";

        if(health <= 8.5) {
            color = "&e";
        } else if (health <= 5.5) {
            color = "&6";
        } else if (health <= 4.5) {
            color = "&c";
        }

        DecimalFormat twoDForm = new DecimalFormat("#.#");

        return CC.translate(color + twoDForm.format(health) + " ❤");
    }

    public String getPots(Player player) {
        String color = "&a";
        int pots = Profile.getByUuid(player.getUniqueId()).getMatch().getTeamPlayer(player).getPotions();

        if (pots <= 3) {
            color = "&4";
        } else if (pots <= 8) {
            color = "&c";
        } else if (pots <= 12) {
            color = "&6";
        } else if (pots <= 20) {
            color = "&e";
        }

        return CC.translate(color + pots + " pots");
    }
}