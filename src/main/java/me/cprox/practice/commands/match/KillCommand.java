package me.cprox.practice.commands.match;

import me.cprox.practice.Practice;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;

public class KillCommand extends BaseCommand {
    @Command(name = "kill")
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        String[] args = commandArgs.getArgs();

        if (args.length == 0) {
            player.setHealth(0.0);
            return;
        }

        if (args.length >= 2) {
            player.sendMessage(CC.translate("&cUsage: /kill <player>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (player.isOp()) {
            if (profile.isInFight()) {
                if (target != null) {
                    target.setHealth(0.0);
                    profile.getMatch().broadcastMessage(CC.translate("&4" + target.getName() + " &7has been killed."));
                } else {
                    player.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.PLAYER_NOT_FOUND").replace("<player>", args[0]));
                }
            } else {
                player.sendMessage(CC.translate("&cYou should be in a match to execute this command."));
            }
        } else {
            player.sendMessage(ChatColor.RED + "No permission.");
        }
    }
}