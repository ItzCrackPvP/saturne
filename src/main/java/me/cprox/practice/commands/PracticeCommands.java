package me.cprox.practice.commands;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.menu.settings.SettingsMenu;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;

public class PracticeCommands extends BaseCommand {
    @Command(name = "practice", aliases = {"practice.ver", "ver", "version", "practice.version"})
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();

        player.sendMessage(CC.translate("&7This server is running &4&lRestPractice 1.3 &7developed by &4gukka13#2283"));
    }

    public static class ToggleDuelRequestsCommand extends BaseCommand {

        @Command(name = "tduel", aliases = {"toggleduel", "dueltoggle", "tdr", "toggleduelrequests"})
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /toggleduelrequests"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.getSettings().setReceiveDuelRequests(!profile.getSettings().isReceiveDuelRequests());
            if (profile.getSettings().isReceiveDuelRequests()) {
                sender.sendMessage(CC.translate("&aYou are now allowing duel requests."));
            } else {
                sender.sendMessage(CC.translate("&cYou are no longer allowing duel requests."));
            }
        }
    }

    public static class ToggleSidebarCommand extends BaseCommand {

        @Command(name = "tsb", aliases = {"togglesb", "togglesidebar"})
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /tsb"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.getSettings().setShowScoreboard(!profile.getSettings().isShowScoreboard());
            if (profile.getSettings().isShowScoreboard()) {
                sender.sendMessage(CC.translate("&aYou can now see the sidebar."));
            } else {
                sender.sendMessage(CC.translate("&cYou can no longer see the sidebar."));
            }
        }
    }

    public class Spawn extends BaseCommand {
        @Command(name = "spawn", aliases = "practice.spawn", permission = "practice.staff")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /spawn"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.isInSomeSortOfFight() && !profile.isInLobby()) {
                player.sendMessage(CC.translate("Unable to teleport to spawn, Please finish your current task!"));
            }
            Practice.get().getEssentials().teleportToSpawn(player);
            profile.refreshHotbar();
        }
    }

    public static class SettingsCommand extends BaseCommand {

        @Command(name = "settings", aliases = {"options", "preferences"})
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /settings"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            new SettingsMenu().openMenu(player);
        }
    }

    public static class SetLobbyCommand extends BaseCommand {

        @Command(name = "practice.setlobby", aliases = {"practice.setspawn", "practice.setlobbyspawn", "practice.set"}, permission = "practice.staff")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /practice setspawn"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Practice.get().getEssentials().setSpawn(player.getLocation());
            player.sendMessage(CC.translate("&aYou have updated the spawn location"));
        }
    }

    public static class SaveCommand extends BaseCommand {
        @Command(name = "practice.save", aliases = {"save", "save-everything", "saveeverything", "saveall"}, permission = "practice.staff")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /practice save"));
                return;
            }

            Profile.getProfiles().values().forEach(Profile::save);
            Profile.loadAllProfiles();
            Kit.getKits().forEach(Kit::save);
            Kit.getKits().forEach(Kit::updateKitLeaderboards);
            Profile.loadGlobalLeaderboards();
            Profile.loadGlobalUnrankedLeaderboards();
            Profile.loadGlobalWinStreakleaderboards();
            Arena.getArenas().forEach(Arena::save);
            player.sendMessage(" ");
            player.sendMessage(CC.translate("&aSuccessfully saved profile datas."));
            player.sendMessage(CC.translate("&aSuccessfully saved arenas.yml"));
            player.sendMessage(CC.translate("&aSuccessfully saved kits.yml"));
            player.sendMessage(CC.translate("&aSuccessfully saved events.yml"));
            player.sendMessage(CC.translate("&aSuccessfully saved leaderboards."));
            player.sendMessage(" ");

        }
    }
}