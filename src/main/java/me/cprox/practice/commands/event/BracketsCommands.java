package me.cprox.practice.commands.event;

import me.cprox.practice.Practice;
import me.cprox.practice.events.brackets.Brackets;
import me.cprox.practice.events.brackets.BracketsState;
import me.cprox.practice.menu.events.SelectEventKitMenu;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.Cooldown;
import org.bukkit.command.CommandSender;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;
import org.bukkit.entity.Player;

public class BracketsCommands extends BaseCommand {
    @Command(name = "brackets", aliases = "brackets.help", permission = "brackets.commands.help", inGameOnly = false)
    public void onCommand(CommandArgs commandArgs) {
        CommandSender sender = commandArgs.getSender();

        sender.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
        sender.sendMessage(CC.translate("&4&lBrackets &8(&7Commands&8)"));
        sender.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
        sender.sendMessage(CC.translate(" &7* &4/brackets cancel &8(&7&oCancel currently running brackets&8)"));
        sender.sendMessage(CC.translate(" &7* &4/brackets cooldown &8(&7&oReset cooldown of the brackets&8)"));
        sender.sendMessage(CC.translate(" &7* &4/brackets host &8(&7&oHost the brackets&8)"));
        sender.sendMessage(CC.translate(" &7* &4/brackets forcestart &8(&7&oStart forcibly the brackets&8)"));
        sender.sendMessage(CC.translate(" &7* &4/brackets join &8(&7&oJoin brackets&8)"));
        sender.sendMessage(CC.translate(" &7* &4/brackets leave &8(&7&oLeave brackets&8)"));
        sender.sendMessage(CC.translate(" &7* &4/brackets tp &8(&7&oTeleport to the brackets arena&8)"));
        sender.sendMessage(CC.translate(" &7* &4/brackets setspawn &8<&cfirst&8/&9second&8/&aspec&8> &8(&7&oSet the spawns of brackets arena&8)"));
        sender.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));

    }

    public static class BracketsCooldownCommand extends BaseCommand {
        @Command(name = "brackets.cooldown", permission = "brackets.commands.cooldown")
        public void onCommand(CommandArgs commandArgs) {
            CommandSender sender = commandArgs.getSender();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                sender.sendMessage(CC.translate("&cUsage: /brackets cooldown"));
                return;
            }

            if (Practice.get().getBracketsManager().getCooldown().hasExpired()) {
                sender.sendMessage(CC.translate("&cBrackets does not have any cooldown at the moment."));
                return;
            }

            sender.sendMessage(CC.translate("&aBrackets cooldown has been canceled."));

            Practice.get().getBracketsManager().setCooldown(new Cooldown(0));
        }
    }

    public static class BracketsCancelCommand extends BaseCommand {
        @Command(name = "brackets.cancel", permission = "brackets.commands.cancel")
        public void onCommand(CommandArgs commandArgs) {
            CommandSender sender = commandArgs.getSender();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                sender.sendMessage(CC.translate("&cUsage: /brackets cancel"));
                return;
            }

            if (Practice.get().getBracketsManager().getActiveBrackets() == null) {
                sender.sendMessage(CC.translate("&cThere isn't an active Brackets event."));
                return;
            }

            Practice.get().getBracketsManager().getActiveBrackets().end();
        }
    }

    public static class BracketsForceStartCommand extends BaseCommand {
        @Command(name = "brackets.forcestart", permission = "brackets.commands.forcestart")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /brackets forcestart"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            if (Practice.get().getBracketsManager().getActiveBrackets() == null) {
                sender.sendMessage(CC.translate("&cThere isn't an active Brackets event."));
                return;
            }

            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.getBrackets().onRound();
        }
    }

    public static class BracketsTpCommand extends BaseCommand {

        @Command(name = "brackets.tp", permission = "brackets.commands.tp")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /brackets tp"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            player.teleport(Practice.get().getBracketsManager().getBracketsSpectator());
            player.sendMessage(CC.translate("&aTeleported to brackets's spawn location."));
        }
    }

    public static class BracketsSetSpawnCommand extends BaseCommand {
        @Command(name = "brackets.setspawn", permission = "brackets.commands.setspawn")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();
            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /brackets setspawn <first/second/spec"));
                return;
            }

            if (args.length >= 2) {
                player.sendMessage(CC.translate("&cUsage: /brackets setspawn <first/second/spec"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            String position = args[0];
            if (!(position.equals("first") || position.equals("second") || position.equals("spec"))) {
                player.sendMessage(CC.RED + "The position must be first|second|spec.");
            } else {
                if (position.equals("first")) {
                    Practice.get().getBracketsManager().setBracketsSpawn1(player.getLocation());
                } else if (position.equals("second")) {
                    Practice.get().getBracketsManager().setBracketsSpawn2(player.getLocation());
                } else {
                    Practice.get().getBracketsManager().setBracketsSpectator(player.getLocation());
                }

                player.sendMessage(CC.translate("&aUpdated brackets's spawn location " + args[0] + "."));

                Practice.get().getBracketsManager().save();
            }
        }
    }

    public static class BracketsLeaveCommand extends BaseCommand {
        @Command(name = "brackets.leave")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /brackets leave"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Profile profile = Profile.getByUuid(player.getUniqueId());
            Brackets activeBrackets = Practice.get().getBracketsManager().getActiveBrackets();

            if (activeBrackets == null) {
                player.sendMessage(CC.RED + "There isn't any active Brackets Events.");
                return;
            }

            if (!profile.isInBrackets() || !activeBrackets.getEventPlayers().containsKey(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You are not apart of the active Brackets Event.");
                return;
            }

            Practice.get().getBracketsManager().getActiveBrackets().handleLeave(player);
            PlayerUtil.reset(player);
        }
    }

    public static class BracketsJoinCommand extends BaseCommand {
        @Command(name = "brackets.join")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /brackets join"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Profile profile = Profile.getByUuid(player.getUniqueId());
            Brackets activeBrackets = Practice.get().getBracketsManager().getActiveBrackets();

            if (profile.isBusy(player)) {
                player.sendMessage(CC.RED + "You cannot join the brackets right now.");
                return;
            }

            if (activeBrackets == null) {
                player.sendMessage(CC.RED + "There isn't any active brackets right now.");
                return;
            }

            if (activeBrackets.getState() != BracketsState.WAITING) {
                player.sendMessage(CC.RED + "This brackets is currently running.");
                return;
            }

            Practice.get().getBracketsManager().getActiveBrackets().handleJoin(player);
        }
    }

    public static class BracketsHostCommand extends BaseCommand {
        @Command(name = "brackets.host", permission = "brackets.commands.host")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /brackets host"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            new SelectEventKitMenu().openMenu(player);
        }
    }
}
