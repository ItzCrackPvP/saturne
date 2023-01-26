package me.cprox.practice.commands;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.arena.impl.StandaloneArena;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.duel.DuelProcedure;
import me.cprox.practice.match.duel.DuelRequest;
import me.cprox.practice.match.impl.*;
import me.cprox.practice.match.impl.solo.*;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.menu.duel.DuelSelectKitMenu;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ArenaType;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;

public class DuelCommands extends BaseCommand {
    @Command(name = "duel")
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();
        String[] args = commandArgs.getArgs();

        if (args.length == 0) {
            player.sendMessage(CC.translate("&cUsage: /duel <player>"));
            return;
        }

        if (args.length >= 2) {
            player.sendMessage(CC.translate("&cUsage: /duel <player>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.PLAYER_NOT_FOUND").replace("<player>", args[0]));
            return;
        }

        CommandSender sender = commandArgs.getSender();
        if (!(sender instanceof Player)) {
            sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
            return;
        }

        if (player.getUniqueId().equals(Bukkit.getPlayer(args[0]).getUniqueId())) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.CANNOT_DUEL_YOURSELF"));
            return;
        }

        final Profile senderProfile = Profile.getByUuid(player.getUniqueId());
        final Profile receiverProfile = Profile.getByUuid(Bukkit.getPlayer(args[0]).getUniqueId());

        if (senderProfile.isBusy(player)) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.CANNOT_DUEL_RIGHT_NOW"));
            return;
        }

        if (receiverProfile.isBusy(Bukkit.getPlayer(args[0]))) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.IS_BUSY").replace("<player>", args[0]));
            return;
        }

        if (!receiverProfile.getSettings().isReceiveDuelRequests()) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.DONT_RECEIVE_DUELS").replace("<player>", args[0]));
            return;
        }

        if (senderProfile.canSendDuelRequest(player)) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.ALREADY_SENT").replace("<player>", args[0]));
            return;
        }

        final DuelProcedure procedure = new DuelProcedure(player, Bukkit.getPlayer(args[0]), false);
        senderProfile.setDuelProcedure(procedure);
        new DuelSelectKitMenu("normal").openMenu(player);

    }

    public static class DuelAcceptCommand extends BaseCommand {
        @Command(name = "duel.accept")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /duel accept <player>"));
                return;
            }

            if (args.length >= 2) {
                player.sendMessage(CC.translate("&cUsage: /duel accept <player>"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.PLAYER_NOT_FOUND").replace("<player>", args[0]));
                return;
            }

            Profile senderProfile = Profile.getByUuid(player.getUniqueId());

            if (senderProfile.isBusy(player)) {
                player.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.CANNOT_DUEL_RIGHT_NOW"));
                return;
            }

            Profile receiverProfile = Profile.getByUuid(target.getUniqueId());

            if (!receiverProfile.isPendingDuelRequest(player)) {
                player.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.DONT_HAVE_DUEL_REQUEST_PLAYER").replace("<player>", target.getName()));
                return;
            }

            if (receiverProfile.isBusy(target)) {
                player.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.IS_BUSY").replace("<player>", target.getName()));
                return;
            }

            DuelRequest request = receiverProfile.getSentDuelRequests().get(player.getUniqueId());

            if (request == null) {
                return;
            }

            Arena arena = request.getArena();

            if (arena == null) {
                player.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.NO_ARENAS"));
                return;
            }

            if (arena.isActive()) {
                if (arena.getType().equals(ArenaType.STANDALONE)) {
                    StandaloneArena sarena = (StandaloneArena) arena;
                    if (sarena.getDuplicates() != null) {
                        boolean foundarena = false;
                        for (Arena darena : sarena.getDuplicates()) {
                            if (!darena.isActive()) {
                                arena = darena;
                                foundarena = true;
                                break;
                            }
                        }
                        if (!foundarena) {
                            player.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.NO_ARENAS"));
                            return;
                        }
                    }
                } else {
                    player.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.NO_ARENAS"));
                    return;
                }
            }
            if (!arena.getType().equals(ArenaType.SHARED)) {
                arena.setActive(true);
            }

            Match match;

            if (request.getKit().getGameRules().isBridge()) {
                match = new SoloBridgeMatch(null, new TeamPlayer(player), new TeamPlayer(target), request.getKit(), arena, QueueType.UNRANKED);
            } else if (request.getKit().getGameRules().isBedFight()) {
                match = new SoloBedFightMatch(null, new TeamPlayer(player), new TeamPlayer(target), request.getKit(), arena, QueueType.UNRANKED);
            } else if (request.getKit().getGameRules().isBattleRush()) {
                match = new SoloBattleRushMatch(null, new TeamPlayer(player), new TeamPlayer(target), request.getKit(), arena, QueueType.UNRANKED);
            } else if (request.getKit().getGameRules().isPearlFight()) {
                match = new SoloPearlFightMatch(null, new TeamPlayer(player), new TeamPlayer(target), request.getKit(), arena, QueueType.UNRANKED);
            }  else {
                match = new SoloMatch(null, new TeamPlayer(player), new TeamPlayer(target), request.getKit(), arena, QueueType.UNRANKED);
            }

            for (String string : Practice.get().getMessagesConfig().getStringList("DUEL.MESSAGE_PLAYERA")) {
                final String opponentMessages=this.formatMessages(string, player.getName(), target.getName());
                final String message=CC.translate(opponentMessages)
                        .replace("<kit>", request.getKit().getName())
                        .replace("<arena>", request.getArena().getName())
                        .replace("<playerBPing>", String.valueOf(PlayerUtil.getPing(((Player) target))));
                sender.sendMessage(message);
            }

            for (String string : Practice.get().getMessagesConfig().getStringList("DUEL.MESSAGE_PLAYERB")) {
                final String opponentMessages=this.formatMessages(string, player.getName(), target.getName());
                final String message=CC.translate(opponentMessages)
                        .replace("<kit>", request.getKit().getName())
                        .replace("<arena>", request.getArena().getName())
                        .replace("<playerAPing>", String.valueOf(PlayerUtil.getPing(((Player) sender))));
                target.sendMessage(message);
            }

            match.start();
        }

        private String formatMessages(final String string, final String player1, final String player2) {
            String player1Format;
            String player2Format;
            player1Format = player1;
            player2Format = player2;
            return string.replace("<playerA>", player1Format).replace("<playerB>", player2Format);
        }
    }

    public static class DuelDenyCommand extends BaseCommand {
        @Command(name = "duel.deny")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /duel deny <player>"));
                return;
            }

            if (args.length >= 2) {
                player.sendMessage(CC.translate("&cUsage: /duel deny <player>"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.PLAYER_NOT_FOUND").replace("<player>", args[0]));
                return;
            }

            Profile senderProfile = Profile.getByUuid(target.getUniqueId());
            Profile receiverProfile = Profile.getByUuid(player.getUniqueId());

            if (receiverProfile.isInFight() || receiverProfile.isInEvent()) {
                player.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.HAS_EXPIRED"));
                return;
            }

            if (!senderProfile.getSentDuelRequests().isEmpty() && senderProfile.getSentDuelRequests().containsKey(player.getUniqueId())) {
                Profile.getByUuid(target.getUniqueId()).getSentDuelRequests().clear();
                player.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.YOU_DENIED").replace("<player>", args[0]));
                target.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.PLAYER_DENIED_YOUR_REQUEST").replace("<sender>", sender.getName()));
            }
        }
    }
}