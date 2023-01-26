package me.cprox.practice.commands;

import me.cprox.practice.Practice;
import me.cprox.practice.menu.events.HostEventMenu;
import me.cprox.practice.util.chat.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;

public class EventCommands extends BaseCommand {
    @Command(name = "events", aliases = {"event", "host"}, permission = "events.command")
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();
        String[] args = commandArgs.getArgs();

        if (args.length >= 1) {
            player.sendMessage(CC.translate("&cUsage: /host"));
            return;
        }

        CommandSender sender = commandArgs.getSender();
        if (!(sender instanceof Player)) {
            sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
            return;
        }

        new HostEventMenu().openMenu(player);
    }

    public static class EventHelpCommand extends BaseCommand {

        @Command(name = "events.help", aliases = "event.help", permission = "events.commands.help")
        public void onCommand(CommandArgs commandArgs) {
            CommandSender sender = commandArgs.getSender();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                sender.sendMessage(CC.translate("&cUsage: /event help"));
                return;
            }

            sender.sendMessage(CC.translate(""));
            sender.sendMessage(CC.translate("&4Event Commands"));
            sender.sendMessage(CC.translate(""));
            sender.sendMessage(CC.translate("&7/host &7- &4Open Events Menu"));
            sender.sendMessage(CC.translate("&7/brackets &7- &4View Bracket Commands"));
            sender.sendMessage(CC.translate("&7/sumo &7- &4View Sumo Commands"));
            sender.sendMessage(CC.translate("&7/lms &7- &4View LMS Commands"));
            sender.sendMessage(CC.translate("&7/parkour &7- &4View Parkour Commands"));
            sender.sendMessage(CC.translate("&7/skywars &7- &4View skywars Commands"));
        }
    }
}
