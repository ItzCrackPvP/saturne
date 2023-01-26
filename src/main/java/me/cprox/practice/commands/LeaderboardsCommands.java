package me.cprox.practice.commands;

import me.cprox.practice.Practice;
import me.cprox.practice.menu.stats.RankedLeaderboards;
import me.cprox.practice.menu.stats.StatisticsMenu;
import me.cprox.practice.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;

public class LeaderboardsCommands extends BaseCommand {
    @Command(name = "leaderboard", aliases = {"lb", "top", "lbs", "leaderboards"})
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();
        String[] args = commandArgs.getArgs();

        if (args.length >= 1) {
            player.sendMessage(CC.translate("&cUsage: /leaderboards"));
            return;
        }

        CommandSender sender = commandArgs.getSender();
        if (!(sender instanceof Player)) {
            sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
            return;
        }

        new RankedLeaderboards().openMenu(player);
    }

    public static class StatsCommand extends BaseCommand {

        @Command(name = "stats", aliases = {"statistics", "info", "aboutme"})
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                new StatisticsMenu(player).openMenu(player);
                return;
            }

            String target2 = args[0];
            Player target = Bukkit.getOfflinePlayer(target2).getPlayer();

            if (args.length == 1) {
                new StatisticsMenu(target).openMenu(player);
                return;
            }

            if (args.length >= 3) {
                player.sendMessage(CC.translate("&cUsage: /stats"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
            }

        }
    }
}
