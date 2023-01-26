package me.cprox.practice.commands.event;

import me.cprox.practice.Practice;
import me.cprox.practice.events.sumo.Sumo;
import me.cprox.practice.events.sumo.SumoState;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.Cooldown;
import org.bukkit.command.CommandSender;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;
import org.bukkit.entity.Player;

public class SumoCommands extends BaseCommand {
    @Command(name = "sumo", aliases = "sumo.help", permission = "sumo.commands.help", inGameOnly = false)
    public void onCommand(CommandArgs commandArgs) {
        CommandSender sender = commandArgs.getSender();
        sender.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
        sender.sendMessage(CC.translate("&4&lSumo &8(&7Commands&8)"));
        sender.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
        sender.sendMessage(CC.translate(" &7* &4/sumo cancel &8(&7&oCancel currently running sumo&8)"));
        sender.sendMessage(CC.translate(" &7* &4/sumo cooldown &8(&7&oReset cooldown of the sumo&8)"));
        sender.sendMessage(CC.translate(" &7* &4/sumo host &8(&7&oHost the sumo&8)"));
        sender.sendMessage(CC.translate(" &7* &4/sumo forcestart &8(&7&oStart forcibly the sumo&8)"));
        sender.sendMessage(CC.translate(" &7* &4/sumo join &8(&7&oJoin sumo&8)"));
        sender.sendMessage(CC.translate(" &7* &4/sumo leave &8(&7&oLeave sumo&8)"));
        sender.sendMessage(CC.translate(" &7* &4/sumo tp &8(&7&oTeleport to the sumo arena&8)"));
        sender.sendMessage(CC.translate(" &7* &4/sumo setspawn &8<&cfirst&8/&9second&8/&aspec&8> &8(&7&oSet the spawns of sumo&8)"));
        sender.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
    }

    public static class SumoCancelCommand extends BaseCommand {
        @Command(name = "sumo.cancel", permission = "sumo.commands.cancel")
        public void onCommand(CommandArgs commandArgs) {
            CommandSender sender = commandArgs.getSender();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                sender.sendMessage(CC.translate("&cUsage: /sumo cancel"));
                return;
            }

            if (Practice.get().getSumoManager().getActiveSumo() == null) {
                sender.sendMessage(CC.RED + "There isn't an active Sumo Event.");
                return;
            }

            Practice.get().getSumoManager().getActiveSumo().end();
        }

    }

    public static class SumoCooldownCommand extends BaseCommand {
        @Command(name = "sumo.cooldown", permission = "sumo.commands.cooldown")
        public void onCommand(CommandArgs commandArgs) {
            CommandSender sender = commandArgs.getSender();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                sender.sendMessage(CC.translate("&cUsage: /sumo cooldown"));
                return;
            }

            if (Practice.get().getSumoManager().getCooldown().hasExpired()) {
                sender.sendMessage(CC.RED + "There isn't any cooldown for the Sumo Event.");
                return;
            }

            sender.sendMessage(CC.GREEN + "You reseted the Sumo event cooldown.");

            Practice.get().getSumoManager().setCooldown(new Cooldown(0));
        }

    }

    public static class SumoForceStartCommand extends BaseCommand {
        @Command(name = "sumo.forcestart", permission = "sumo.commands.forcestart")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /sumo forcestart"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            if (Practice.get().getSumoManager().getActiveSumo() == null) {
                sender.sendMessage(CC.RED + "There isn't an active Sumo Event.");
                return;
            }

            Profile profile =Profile.getByUuid(player);
            profile.getSumo().onRound();
        }
    }

    public static class SumoJoinCommand extends BaseCommand {
        @Command(name = "sumo.join")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /sumo join"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Profile profile = Profile.getByUuid(player.getUniqueId());
            Sumo activeSumo = Practice.get().getSumoManager().getActiveSumo();

            if (profile.isBusy(player)) {
                player.sendMessage(CC.RED + "You cannot join the Sumo Event right now.");
                return;
            }

            if (activeSumo == null) {
                player.sendMessage(CC.RED + "There isn't an active Sumo Event.");
                return;
            }

            if (activeSumo.getState() != SumoState.WAITING) {
                player.sendMessage(CC.RED + "That Sumo Event is currently on-going and cannot be joined.");
                return;
            }

            Practice.get().getSumoManager().getActiveSumo().handleJoin(player);
        }

    }

    public static class SumoHostCommand extends BaseCommand {
        @Command(name = "sumo.host", permission = "sumo.commands.host")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /sumo host"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }
            if (Practice.get().getSumoManager().getActiveSumo() != null) {
                player.sendMessage(CC.RED + "There is already an active Sumo Event.");
                return;
            }

            if (!Practice.get().getSumoManager().getCooldown().hasExpired()) {
                player.sendMessage(CC.RED + "There is a Sumo Event cooldown active.");
                return;
            }

            Practice.get().getSumoManager().setActiveSumo(new Sumo(player));
            player.performCommand("sumo join");
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

    public static class SumoSetSpawnCommand extends BaseCommand {
        @Command(name = "sumo.setspawn", permission = "sumo.commands.setspawn")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();
            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /sumo setspawn <first/second/spec"));
                return;
            }

            if (args.length >= 2) {
                player.sendMessage(CC.translate("&cUsage: /sumo setspawn <first/second/spec"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            String position = args[0];
            if (!(position.equals("1") || position.equals("2") || position.equals("spec"))) {
                player.sendMessage(CC.RED + "The position must be 1 or 2.");
            } else {
                if (position.equals("1")) {
                    Practice.get().getSumoManager().setSumoSpawn1(player.getLocation());
                } else if (position.equals("2")) {
                    Practice.get().getSumoManager().setSumoSpawn2(player.getLocation());
                } else {
                    Practice.get().getSumoManager().setSumoSpectator(player.getLocation());
                }

                player.sendMessage(CC.GREEN + "Updated sumo's spawn location " + position + ".");

                Practice.get().getSumoManager().save();
            }
        }

    }

    public static class SumoLeaveCommand extends BaseCommand {
        @Command(name = "sumo.leave")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /sumo leave"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }
            Profile profile = Profile.getByUuid(player.getUniqueId());
            Sumo activeSumo = Practice.get().getSumoManager().getActiveSumo();

            if (activeSumo == null) {
                player.sendMessage(CC.RED + "There isn't an active Sumo Event.");
                return;
            }

            if (!profile.isInSumo() || !activeSumo.getEventPlayers().containsKey(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You are not apart of the active Sumo Event.");
                return;
            }

            Practice.get().getSumoManager().getActiveSumo().handleLeave(player);
            PlayerUtil.reset(player);
        }

    }

    public static class SumoTpCommand extends BaseCommand {

        @Command(name = "sumo.tp", permission = "sumo.commands.tp")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /sumo tp"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            player.teleport(Practice.get().getSumoManager().getSumoSpectator());
            player.sendMessage(CC.GREEN + "Teleported to sumo's spawn location.");
        }

    }
}
