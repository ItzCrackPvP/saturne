package me.cprox.practice.commands.match;

import me.cprox.practice.Practice;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.util.TaskUtil;
import me.cprox.practice.util.chat.CC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;

public class SpectateCommand extends BaseCommand {

    @Command(name = "spectate", aliases = "spec")
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();
        String[] args = commandArgs.getArgs();

        if (args.length == 0) {
            player.sendMessage(CC.translate("&cUsage: /spectate <player>"));
            return;
        }

        if (args.length >= 2) {
            player.sendMessage(CC.translate("&cUsage: /spectate <player>"));
            return;
        }

        CommandSender sender = commandArgs.getSender();
        if (!(sender instanceof Player)) {
            sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
            return;
        }

        Player target = this.plugin.getServer().getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.PLAYER_NOT_FOUND").replace("<player>", args[0]));
            return;
        }

        Profile playerProfile = Profile.getByUuid(player.getUniqueId());

        if (playerProfile.isBusy(player)) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.CANNOT_EXECUTE"));
            return;
        }

        Profile targetProfile = Profile.getByUuid(target.getUniqueId());

        if (targetProfile.getState() != ProfileState.IN_FIGHT) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_NOT_FIGHTING").replace("<player>", args[0]));
            return;
        }

        if (targetProfile.isInTournament(target)) {
            player.sendMessage(CC.translate("&c" + args[0] + " is in a tournament, Tournament spectating is currently in development!"));
            return;
        }

        if (!targetProfile.getSettings().isAllowSpectators() && !player.hasPermission("practice.spectator")) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.NOT_ALLOWING_SPECTATE").replace("<player>", args[0]));
            return;
        }

        if (targetProfile.getMatch() != null) {
            if (!targetProfile.getMatch().isFreeForAllMatch()) {
                for (TeamPlayer teamPlayer : targetProfile.getMatch().getTeamPlayers()) {
                    Player inMatchPlayer = teamPlayer.getPlayer();
                    if (inMatchPlayer != null) {
                        Profile inMatchProfile = Profile.getByUuid(inMatchPlayer.getUniqueId());

                        if (!inMatchProfile.getSettings().isAllowSpectators() && !player.hasPermission("practice.spectator")) {
                            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.NOT_ALLOWING_SPECTATE").replace("<player>", args[0]));
                            return;
                        }
                    }
                }
            }
        }

        TaskUtil.run(() -> targetProfile.getMatch().addSpectator(player, target));
    }
}