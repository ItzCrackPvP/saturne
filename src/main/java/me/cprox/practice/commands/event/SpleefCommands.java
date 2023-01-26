package me.cprox.practice.commands.event;

import me.cprox.practice.Practice;
import me.cprox.practice.events.spleef.Spleef;
import me.cprox.practice.events.spleef.SpleefState;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.Cooldown;
import org.bukkit.command.CommandSender;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;
import org.bukkit.entity.Player;

public class SpleefCommands extends BaseCommand {
    @Command(name = "spleef", aliases = "spleef.help", permission = "spleef.commands.help", inGameOnly = false)
    public void onCommand(CommandArgs commandArgs) {
        CommandSender sender = commandArgs.getSender();

        sender.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
        sender.sendMessage(CC.translate("&4&lSpleef &8(&7Commands&8)"));
        sender.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
        sender.sendMessage(CC.translate(" &7* &4/spleef cancel &8(&7&oCancel currently running spleef&8)"));
        sender.sendMessage(CC.translate(" &7* &4/spleef cooldown &8(&7&oReset the spleef cooldown&8)"));
        sender.sendMessage(CC.translate(" &7* &4/spleef host &8(&7&oHost the spleef&8)"));
        sender.sendMessage(CC.translate(" &7* &4/spleef forcestart &8(&7&oForcestart a spleef event&8)"));
        sender.sendMessage(CC.translate(" &7* &4/spleef join &8(&7&oJoin spleef&8)"));
        sender.sendMessage(CC.translate(" &7* &4/spleef leave &8(&7&oLeave spleef&8)"));
        sender.sendMessage(CC.translate(" &7* &4/spleef tp &8(&7&oTeleport to the spleef arena&8)"));
        sender.sendMessage(CC.translate(" &7* &4/spleef setspawn &8(&7&oSet the spawns for spleef arena&8)"));
        sender.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
    }

    public static class SpleefCooldownCommand extends BaseCommand {
        @Command(name = "spleef.cooldown", permission = "spleef.commands.cooldown")
        public void onCommand(CommandArgs commandArgs) {
            CommandSender sender = commandArgs.getSender();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                sender.sendMessage(CC.translate("&cUsage: /spleef cooldown"));
                return;
            }

            if (Practice.get().getSpleefManager().getCooldown().hasExpired()) {
                sender.sendMessage(CC.RED + "There isn't a Spleef Event cooldown.");
                return;
            }

            sender.sendMessage(CC.GREEN + "You reset the Spleef Event cooldown.");

            Practice.get().getSpleefManager().setCooldown(new Cooldown(0));
        }

    }

    public static class SpleefCancelCommand extends BaseCommand {
        @Command(name = "spleef.cancel", permission = "spleef.commands.cancel")
        public void onCommand(CommandArgs commandArgs) {
            CommandSender sender = commandArgs.getSender();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                sender.sendMessage(CC.translate("&cUsage: /spleef cancel"));
                return;
            }

            if (Practice.get().getSpleefManager().getActiveSpleef() == null) {
                sender.sendMessage(CC.RED + "There isn't an active Spleef event.");
                return;
            }

            Practice.get().getSpleefManager().getActiveSpleef().end();
        }

    }

    public static class SpleefForceStartCommand extends BaseCommand {
        @Command(name = "spleef.forcestart", permission = "spleef.commands.forcestart")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /spleef forcestart"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            if (Practice.get().getSpleefManager().getActiveSpleef() == null) {
                sender.sendMessage(CC.RED + "There isn't an active Spleef event.");
                return;
            }

            Profile profile =Profile.getByUuid(player);
            profile.getSpleef().onRound();
        }
    }

    public static class SpleefHostCommand extends BaseCommand {
        @Command(name = "spleef.host", permission = "spleef.commands.host")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /spleef host"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }
            if (Practice.get().getSpleefManager().getActiveSpleef() != null) {
                player.sendMessage(CC.RED + "There is already an active Spleef Event.");
                return;
            }

            if (!Practice.get().getSpleefManager().getCooldown().hasExpired()) {
                player.sendMessage(CC.RED + "There is an active cooldown for the Spleef Event.");
                return;
            }

            Practice.get().getSpleefManager().setActiveSpleef(new Spleef(player));
            player.performCommand("spleef join");
            for (Player other : Practice.get().getServer().getOnlinePlayers()) {
                Profile profile = Profile.getByUuid(other.getUniqueId());

                if (profile.isInLobby()) {
                    if (!profile.getKitEditor().isActive()) {
                        profile.refreshHotbar();
                    }
                }
            }
        }

    }

    public static class SpleefJoinCommand extends BaseCommand {
        @Command(name = "spleef.join")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /spleef join"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }
            Profile profile = Profile.getByUuid(player.getUniqueId());
            Spleef activeSpleef = Practice.get().getSpleefManager().getActiveSpleef();

            if (profile.isBusy(player)) {
                player.sendMessage(CC.RED + "You cannot join the spleef right now.");
                return;
            }

            if (activeSpleef == null) {
                player.sendMessage(CC.RED + "There isn't any active Spleef Events right now.");
                return;
            }

            if (activeSpleef.getState() != SpleefState.WAITING) {
                player.sendMessage(CC.RED + "This Spleef Event is currently on-going and cannot be joined.");
                return;
            }

            Practice.get().getSpleefManager().getActiveSpleef().handleJoin(player);
        }

    }

    public static class SpleefLeaveCommand extends BaseCommand {
        @Command(name = "spleef.leave")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /spleef leave"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }
            Profile profile = Profile.getByUuid(player.getUniqueId());
            Spleef activeSpleef = Practice.get().getSpleefManager().getActiveSpleef();

            if (activeSpleef == null) {
                player.sendMessage(CC.RED + "There isn't any active Spleef Events.");
                return;
            }

            if (!profile.isInSpleef() || !activeSpleef.getEventPlayers().containsKey(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You are not apart of the active Spleef Event.");
                return;
            }

            Practice.get().getSpleefManager().getActiveSpleef().handleLeave(player);
            PlayerUtil.reset(player);
        }

    }

    public static class SpleefSetSpawnCommand extends BaseCommand {
        @Command(name = "spleef.setspawn", permission = "spleef.commands.setspawn")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();
            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /spleef setspawn <first/second/spec"));
                return;
            }

            if (args.length >= 2) {
                player.sendMessage(CC.translate("&cUsage: /spleef setspawn <first/second/spec"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }
            Practice.get().getSpleefManager().setSpleefSpectator(player.getLocation());

            player.sendMessage(CC.GREEN + "Set spleef's spawn location.");

            Practice.get().getSpleefManager().save();
        }

    }

    public static class SpleefTpCommand extends BaseCommand {

        @Command(name = "spleef.tp", permission = "spleef.commands.tp")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /spleef tp"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            player.teleport(Practice.get().getSpleefManager().getSpleefSpectator());
            player.sendMessage(CC.GREEN + "Teleported to spleef's spawn location.");
        }

    }
}
