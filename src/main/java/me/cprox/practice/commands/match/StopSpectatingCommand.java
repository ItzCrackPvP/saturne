package me.cprox.practice.commands.match;

import me.cprox.practice.Practice;
import me.cprox.practice.match.Match;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import org.bukkit.entity.Player;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;

public class StopSpectatingCommand extends BaseCommand {
     @Command(name = "stopspectating")
     public void onCommand(CommandArgs commandArgs) {
         Player player = commandArgs.getPlayer();
         Profile profile = Profile.getByUuid(player.getUniqueId());

         if (profile.getState() == ProfileState.IN_FIGHT && !profile.getMatch().getTeamPlayer(player).isAlive()) {
             profile.getMatch().getTeamPlayer(player).setDisconnected(true);
             profile.setState(ProfileState.IN_LOBBY);
             profile.setMatch(null);
         } else if (profile.getState() == ProfileState.SPECTATE_MATCH) {
             Match match = profile.getMatch();
             match.removeSpectator(player);
         } else {
             player.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
         }
     }
 }