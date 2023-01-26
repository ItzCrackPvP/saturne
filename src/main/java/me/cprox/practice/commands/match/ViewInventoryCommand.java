package me.cprox.practice.commands.match;

import me.cprox.practice.Practice;
import me.cprox.practice.menu.match.InventorySnapshot;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.regex.Pattern;

public class ViewInventoryCommand extends BaseCommand {
    private static final Pattern UUID_PATTERN;

    static {
        UUID_PATTERN = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
    }

    @Command(name = "inventory", aliases = {"_", "viewinv"})
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();
        String[] args = commandArgs.getArgs();

        if (args.length == 0) {
            player.sendMessage(CC.translate("&cUsage: /viewinv <player>"));
            return;
        }

        if (args.length >= 2) {
            player.sendMessage(CC.translate("&cUsage: /viewinv <player>"));
            return;
        }

        CommandSender sender = commandArgs.getSender();
        if (!(sender instanceof Player)) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
            return;
        }

        if (!args[0].matches(ViewInventoryCommand.UUID_PATTERN.pattern())) {
            sender.sendMessage(CC.translate("&cCannot find the requested inventory. Maybe it expired?"));
            return;
        }

        InventorySnapshot snapshot = Practice.get().getProfileManager().getSnapshot(UUID.fromString(args[0]));
        if (snapshot == null) {
            sender.sendMessage(CC.translate("&cCannot find the requested inventory. Maybe it expired?"));
        } else {
            ((Player) sender).openInventory(snapshot.getInventoryUI().getCurrentPage());
        }
    }
}