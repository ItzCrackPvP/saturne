package me.cprox.practice.commands;

import me.cprox.practice.Practice;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;

public class StaffCommands extends BaseCommand {
    @Command(name = "silent", permission = "practice.staff")
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();
        String[] args = commandArgs.getArgs();

        if (args.length >= 1) {
            player.sendMessage(CC.translate("&cUsage: /silent"));
            return;
        }

        CommandSender sender = commandArgs.getSender();
        if (!(sender instanceof Player)) {
            sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.isFollowMode()) {
            player.sendMessage(CC.translate("&cYou are currently following somebody!"));
            return;
        }

        profile.setSilent(!profile.isSilent());

        player.sendMessage(CC.translate("&7You have " + (profile.isSilent() ? "&aenabled" : "&cdisabled") + " &7silent mode."));
    }

    public static class FollowCommand extends BaseCommand {
        @Command(name = "follow", permission = "practice.staff")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /follow <player>"));
                return;
            }

            if (args.length >= 2) {
                player.sendMessage(CC.translate("&cUsage: /follow <player>"));
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

            Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.isFollowMode()) {
                player.sendMessage(CC.translate("&7You are already following somebody, /unfollow before following someone again."));
                return;
            }
            profile.setFollowMode(true);
            profile.setSilent(true);
            profile.setFollowing(target);
            Profile.getByUuid(target.getUniqueId()).getFollower().add(player);
            player.sendMessage(CC.translate("&7You have &4started &7following &4" + target.getName() + "&7."));

            Profile targetProfile = Profile.getByUuid(target.getUniqueId());
            if (targetProfile.isInSomeSortOfFight()) {
                if (targetProfile.isInMatch()) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Practice.get(), () -> player.chat("/spec " + target.getName()), 20L);
                }
            }
        }
    }

    public static class UnFollowCommand extends BaseCommand {
        @Command(name = "unfollow", permission = "practice.staff")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /unfollow <player>"));
                return;
            }

            if (args.length >= 2) {
                player.sendMessage(CC.translate("&cUsage: /unfollow <player>"));
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

            Profile profile = Profile.getByUuid(player.getUniqueId());
            if (!profile.isFollowMode()) {
                player.sendMessage(CC.translate("&cYou aren't following anybody."));
                return;
            }

            Profile.getByUuid(profile.getFollowing().getUniqueId()).getFollower().remove(player);
            profile.setFollowMode(false);
            profile.setSilent(false);
            profile.setFollowing(null);

            player.sendMessage(CC.translate("&7You have &cexited &7follow mode."));
        }
    }
}
