package me.cprox.practice.queue;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.impl.*;
import me.cprox.practice.match.impl.solo.*;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class QueueThread extends Thread {

    Arena arena;
    Kit kit;

    @Override
    public void run() {
        while (true) {
            try {
                for (Queue queue : Queue.getQueues()) {
                    queue.getPlayers().forEach(QueueProfile::tickRange);
                    for (QueueProfile firstQueueProfile : queue.getPlayers()) {
                        final Player firstPlayer = Bukkit.getPlayer(firstQueueProfile.getPlayerUuid());
                        final Profile firstProfile = Profile.getByUuid(firstQueueProfile.getPlayerUuid());

                        if (firstPlayer == null) {
                            continue;
                        }

                        queue.getPlayers().forEach(queueProfile -> {
                            Player player = Bukkit.getPlayer(queueProfile.getPlayerUuid());

                            if (queue.getQueueType().equals(QueueType.UNRANKED)) {
                                if (firstProfile.getPingFactor() != 0) {
                                    for (String string : Practice.get().getMessagesConfig().getStringList("QUEUE.UNRANKED_PINGFACTOR_REPEATABLE")) {
                                        final String message = string
                                                .replace("<queue>", queue.getQueueName())
                                                .replace("<minping>", String.valueOf(queueProfile.getMinPingRange()))
                                                .replace("<maxping>", String.valueOf(queueProfile.getMinPingRange()));
                                        player.sendMessage(CC.translate(message));
                                    }
                                } else {
                                    for (String string : Practice.get().getMessagesConfig().getStringList("QUEUE.UNRANKED_NONPINGFACTOR_REPEATABLE")) {
                                        final String message = string
                                                .replace("<queue>", queue.getQueueName());
                                        player.sendMessage(CC.translate(message));
                                    }
                                }
                            }

                            if (queue.getQueueType().equals(QueueType.RANKED)) {

                                if (firstProfile.getPingFactor() != 0) {
                                    for (String string : Practice.get().getMessagesConfig().getStringList("QUEUE.RANKED_PINGFACTOR_REPEATABLE")) {
                                        final String message = string
                                                .replace("<queue>", queue.getQueueName())
                                                .replace("<minelo>", String.valueOf(queueProfile.getMinRange()))
                                                .replace("<maxelo>", String.valueOf(queueProfile.getMaxRange()))
                                                .replace("<minping>", String.valueOf(queueProfile.getMinPingRange()))
                                                .replace("<maxping>", String.valueOf(queueProfile.getMinPingRange()));
                                        player.sendMessage(CC.translate(message));
                                    }
                                } else {
                                    for (String string : Practice.get().getMessagesConfig().getStringList("QUEUE.RANKED_NONPINGFACTOR_REPEATABLE")) {
                                        final String message = string
                                                .replace("<queue>", queue.getQueueName())
                                                .replace("<minelo>", String.valueOf(queueProfile.getMinRange()))
                                                .replace("<maxelo>", String.valueOf(queueProfile.getMaxRange()));
                                        player.sendMessage(CC.translate(message));
                                    }
                                }
                            }
                        });

                        if (queue.getPlayers().size() < 2) {
                            continue;
                        }

                        for (QueueProfile secondQueueProfile : queue.getPlayers()) {
                            Player secondPlayer = Bukkit.getPlayer(secondQueueProfile.getPlayerUuid());
                            Profile secondProfile = Profile.getByUuid(secondQueueProfile.getPlayerUuid());

                            if (firstQueueProfile.equals(secondQueueProfile)) {
                                continue;
                            }


                            if (secondPlayer == null) {
                                continue;
                            }
                            if (firstProfile.getPingFactor() != 0 || secondProfile.getPingFactor() != 0) {
                                if (PlayerUtil.getPing(firstPlayer) >= PlayerUtil.getPing(secondPlayer)) {
                                    if (PlayerUtil.getPing(firstPlayer) - PlayerUtil.getPing(secondPlayer) >= 50) {
                                        continue;
                                    }
                                } else {
                                    if (PlayerUtil.getPing(secondPlayer) - PlayerUtil.getPing(firstPlayer) >= 50) {
                                        continue;
                                    }
                                }
                            }

                            if (queue.getType() == QueueType.RANKED) {
                                if (firstQueueProfile.isInRange(secondQueueProfile.getElo()) ||
                                        secondQueueProfile.isInRange(firstQueueProfile.getElo())) {
                                    continue;
                                }
                            }

                            arena = Arena.getRandom(queue.getKit());

                            if (arena == null) {
                                queue.getPlayers().remove(firstQueueProfile);
                                queue.getPlayers().remove(secondQueueProfile);
                                firstPlayer.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.NO_ARENAS"));
                                secondPlayer.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.NO_ARENAS"));
                                continue;
                            }

                            if (arena.isActive()) continue;
                            if (queue.getKit().getGameRules().isBuild()) arena.setActive(true);
                            queue.getPlayers().remove(firstQueueProfile);
                            queue.getPlayers().remove(secondQueueProfile);

                            TeamPlayer firstMatchPlayer = new TeamPlayer(firstPlayer);
                            TeamPlayer secondMatchPlayer = new TeamPlayer(secondPlayer);

                            if (queue.getType() == QueueType.RANKED) {
                                firstMatchPlayer.setRankedElo(firstProfile.getStatisticsData().get(queue.getKit()).getElo());
                                secondMatchPlayer.setRankedElo(secondProfile.getStatisticsData().get(queue.getKit()).getElo());
                                firstProfile.calculateGlobalElo();
                                secondProfile.calculateGlobalElo();

                            } else if (queue.getType() == QueueType.UNRANKED) {
                                firstMatchPlayer.setUnrankedElo(firstProfile.getStatisticsData().get(queue.getKit()).getUnrankedElo());
                                secondMatchPlayer.setUnrankedElo(secondProfile.getStatisticsData().get(queue.getKit()).getUnrankedElo());
                                firstProfile.calculateGlobalUnrankedElo();
                                secondProfile.calculateGlobalUnrankedElo();
                            }

                            kit = queue.getKit();
                            Match match;

                            if (queue.getKit().getGameRules().isBridge()) {
                                match = new SoloBridgeMatch(queue, firstMatchPlayer, secondMatchPlayer, queue.getKit(), arena, queue.getQueueType());
                            } else if (queue.getKit().getGameRules().isBedFight()) {
                                match = new SoloBedFightMatch(queue, firstMatchPlayer, secondMatchPlayer, queue.getKit(), arena, queue.getQueueType());
                            } else if (queue.getKit().getGameRules().isBattleRush()) {
                                match = new SoloBattleRushMatch(queue, firstMatchPlayer, secondMatchPlayer, queue.getKit(), arena, queue.getQueueType());
                            } else if (queue.getKit().getGameRules().isPearlFight()) {
                                match = new SoloPearlFightMatch(queue, firstMatchPlayer, secondMatchPlayer, queue.getKit(), arena, queue.getQueueType());
                            } else {
                                match = new SoloMatch(queue, firstMatchPlayer, secondMatchPlayer, queue.getKit(), arena, queue.getQueueType());
                            }

                            if (match.getQueueType() == QueueType.UNRANKED) {
                                for (String string : Practice.get().getMessagesConfig().getStringList("MATCH.UNRANKED_PLAYERA_INFO")) {
                                    final String message = string
                                            .replace("<kit>", kit.getName())
                                            .replace("<arena>", arena.getName())
                                            .replace("<playerB>", secondPlayer.getName())
                                            .replace("<playerBPing>", String.valueOf(secondMatchPlayer.getPing()));
                                    firstPlayer.sendMessage(CC.translate(message));
                                }

                                for (String string : Practice.get().getMessagesConfig().getStringList("MATCH.UNRANKED_PLAYERB_INFO")) {
                                    final String message = string
                                            .replace("<kit>", kit.getName())
                                            .replace("<arena>", arena.getName())
                                            .replace("<playerA>", firstPlayer.getName())
                                            .replace("<playerAPing>", String.valueOf(firstMatchPlayer.getPing()));
                                    secondPlayer.sendMessage(CC.translate(message));
                                }
                            }

                            if (match.getQueueType() == QueueType.RANKED) {
                                Profile firstelo = Profile.getByUuid(firstPlayer.getUniqueId());
                                Profile secondelo = Profile.getByUuid(secondPlayer.getUniqueId());

                                for (String string : Practice.get().getMessagesConfig().getStringList("MATCH.RANKED_PLAYERA_INFO")) {
                                    final String message = string
                                            .replace("<kit>", kit.getName())
                                            .replace("<arena>", arena.getName())
                                            .replace("<playerB>", secondPlayer.getName())
                                            .replace("<playerBElo>", String.valueOf(secondelo.getStatisticsData().get(kit).getElo()))
                                            .replace("<playerBPing>", String.valueOf(secondMatchPlayer.getPing()));
                                    firstPlayer.sendMessage(CC.translate(message));
                                }

                                for (String string : Practice.get().getMessagesConfig().getStringList("MATCH.RANKED_PLAYERB_INFO")) {
                                    final String message = string
                                            .replace("<kit>", kit.getName())
                                            .replace("<arena>", arena.getName())
                                            .replace("<playerA>", firstPlayer.getName())
                                            .replace("<playerAElo>", String.valueOf(firstelo.getStatisticsData().get(kit).getElo()))
                                            .replace("<playerAPing>", String.valueOf(firstMatchPlayer.getPing()));
                                    secondPlayer.sendMessage(CC.translate(message));
                                }
                            }

                            new BukkitRunnable() {
                                public void run() {
                                    match.start();
                                }
                            }.runTask(Practice.get());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(3700L);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                continue;
            }
            try {
                Thread.sleep(3700L);
            } catch (InterruptedException e3) {
                e3.printStackTrace();
            }
        }
    }
}