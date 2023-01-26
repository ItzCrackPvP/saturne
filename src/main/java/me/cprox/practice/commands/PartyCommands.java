package me.cprox.practice.commands;

import me.cprox.practice.party.Party;
import me.cprox.practice.party.PartyMessage;
import me.cprox.practice.party.PartyPrivacy;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PartyCommands extends BaseCommand {
    @Command(name = "party", aliases = {"party.help", "p.help", "p"}, inGameOnly = false)
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();

        player.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
        player.sendMessage(CC.translate("&4&lParty &8(&7Commands&8)"));
        player.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
        player.sendMessage(CC.translate("&4General Commands:"));
        player.sendMessage(CC.translate("&7/party create <Name> &8- &7Create a party"));
        player.sendMessage(CC.translate("&7/party leave &8- &7Leave your party"));
        player.sendMessage(CC.translate("&7/party join <Name> &8- &7Join a party"));
        player.sendMessage(CC.translate("&7"));
        player.sendMessage(CC.translate("&4Leader Commands:"));
        player.sendMessage(CC.translate("&7/party disband &8- &7Disband your party"));
        player.sendMessage(CC.translate("&7/party kick <Name> &8- &7Kick a player"));
        player.sendMessage(CC.translate("&7/party leader &8- &7Make a player leader"));
        player.sendMessage(CC.translate("&7/party open &8- &7Make your party open"));
        player.sendMessage(CC.translate("&7/party close &8- &7Make your party closed"));
        player.sendMessage(CC.translate("&7"));
        player.sendMessage(CC.translate("&7To use the &cparty chat&7, prefix your message with a '&c?&7' or '&c!&7' sign"));
        player.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
    }

    public static class PartyCreateCommand extends BaseCommand {
        @Command(name = "party.create", aliases = "p.create", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();

            if (player.getPlayer().hasMetadata("frozen")) {
                player.sendMessage(CC.RED + "You cannot create a party while frozen.");
                return;
            }

            Profile profile = Profile.getByUuid(player.getPlayer());

            if (profile.getParty() != null) {
                player.sendMessage(CC.RED + "You already have a party.");
                return;
            }

            if (!profile.isInLobby()) {
                player.sendMessage(CC.RED + "You must be in the lobby to create a party.");
                return;
            }

            profile.setParty(new Party(commandArgs.getPlayer()));
            profile.refreshHotbar();

            player.sendMessage(PartyMessage.CREATED.format());
        }
    }

    public static class PartyDisbandCommand extends BaseCommand {
        @Command(name = "party.disband", aliases = "p.disband", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Profile profile = Profile.getByUuid(commandArgs.getPlayer().getUniqueId());
            Player player = commandArgs.getPlayer();

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You do not have a party.");
                return;
            }

            if (!profile.getParty().isLeader(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You are not the leader of your party.");
                return;
            }

            if (profile.getMatch() != null) {
                player.sendMessage(CC.RED + "You can not do that when you're in a match");
                return;
            }

            profile.getParty().disband();
        }
    }

    public static class PartyInviteCommand extends BaseCommand {
        @Command(name = "party.invite", aliases = "p.invite", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                player.sendMessage(CC.RED + "A player with that name could not be found.");
                return;
            }

            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You do not have a party.");
                return;
            }

            if (profile.getParty().getInvite(target.getPlayer().getUniqueId()) != null) {
                player.sendMessage(CC.RED + "That player has already been invited to your party.");
                return;
            }

            if (profile.getParty().containsPlayer(target)) {
                player.sendMessage(CC.RED + "That player is already in your party.");
                return;
            }

            if (profile.getParty().getPrivacy() == PartyPrivacy.OPEN) {
                player.sendMessage(CC.RED + "The party state is Open. You do not need to invite players.");
                return;
            }

            Profile targetData = Profile.getByUuid(target.getUniqueId());

            if (targetData.isBusy(target)) {
                player.sendMessage(target.getDisplayName() + CC.RED + " is currently busy.");
                return;
            }

            profile.getParty().invite(target);
        }
    }

    public static class PartyJoinCommand extends BaseCommand {
        @Command(name = "party.join", aliases = "p.join", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                player.sendMessage(CC.RED + "A player with that name could not be found.");
                return;
            }

            if (player.hasMetadata("frozen")) {
                player.sendMessage(CC.RED + "You cannot join a party while frozen.");
                return;
            }

            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.isBusy(player)) {
                player.sendMessage(CC.RED + "You can not do that right now");
                return;
            }

            if (profile.getParty() != null) {
                player.sendMessage(CC.RED + "You already have a party.");
                return;
            }

            Profile targetProfile = Profile.getByUuid(target.getUniqueId());
            Party party = targetProfile.getParty();

            if (party == null) {
                player.sendMessage(CC.RED + "A party with that name could not be found.");
                return;
            }

            if (party.getPrivacy() == PartyPrivacy.CLOSED) {
                if (party.getInvite(player.getUniqueId()) == null) {
                    player.sendMessage(CC.RED + "You have not been invited to that party.");
                    return;
                }
            }

            if (party.getPlayers().size() >= party.getLimit()) {
                player.sendMessage(CC.RED + "That party is full and cannot hold anymore players.");
                return;
            }

            if (party.getBanned().contains(player)) {
                player.sendMessage(CC.RED + "You have been banned from that party");
                return;
            }

            party.join(player);
        }
    }

    public static class PartyLeaveCommand extends BaseCommand {
        @Command(name = "party.leave", aliases = "p.leave", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();

            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You do not have a party.");
                return;
            }

            if (profile.getParty().getLeader().getUuid().equals(player.getUniqueId())) {
                profile.getParty().disband();
            } else {
                profile.getParty().leave(player, false);
            }
        }
    }

    public static class PartyMakeLeaderCommand extends BaseCommand {
        @Command(name = "party.setleader", aliases = {"p.setleader", "p.makeleader", "party.makeleader", "p.leader", "party.leader"}, inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You do not have a party.");
                return;
            }

            if (!profile.getParty().isLeader(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You are not the leader of your party.");
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);
            
            if (!profile.getParty().containsPlayer(target)) {
                player.sendMessage(CC.RED + "That player is not a member of your party.");
                return;
            }

            if (player.equals(target)) {
                player.sendMessage(CC.RED + "You cannot yourself leader your party, because you have it already.");
                return;
            }

            profile.getParty().leader(player, target);
        }
    }

    public static class PartyKickCommand extends BaseCommand {
        @Command(name = "party.kick", aliases = "p.kick", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You do not have a party.");
                return;
            }

            if (!profile.getParty().isLeader(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You are not the leader of your party.");
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);
            
            if (!profile.getParty().containsPlayer(target)) {
                player.sendMessage(CC.RED + "That player is not a member of your party.");
                return;
            }

            if (player.equals(target)) {
                player.sendMessage(CC.RED + "You cannot kick yourself from your party.");
                return;
            }

            player.sendMessage(CC.GREEN + "Successfully kicked that player");
            target.sendMessage(CC.RED + "You have been kicked from the party");
            profile.getParty().leave(target, true);
        }
    }

    public static class PartyInfoCommand extends BaseCommand {
        @Command(name = "party.info", aliases = "p.info", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You do not have a party.");
                return;
            }

            profile.getParty().sendInformation(player);
        }
    }

    public static class PartyOpenCommand extends BaseCommand {
        @Command(name = "party.open", aliases = "p.open", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You do not have a party.");
                return;
            }

            if (!profile.getParty().isLeader(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You are not the leader of your party.");
                return;
            }

            profile.getParty().setPrivacy(PartyPrivacy.OPEN);
        }
    }

    public static class PartyCloseCommand extends BaseCommand {
        @Command(name = "party.close", aliases = "p.close", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You do not have a party.");
                return;
            }

            if (!profile.getParty().isLeader(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You are not the leader of your party.");
                return;
            }

            profile.getParty().setPrivacy(PartyPrivacy.CLOSED);
        }
    }

    public static class PartyBanCommand extends BaseCommand {
        @Command(name = "party.ban", aliases = "p.ban", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You do not have a party.");
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                player.sendMessage(CC.RED + "That player is not online");
                return;
            }

            if (!profile.getParty().isLeader(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You are not the leader of your party.");
                return;
            }

            if (player.equals(target)) {
                player.sendMessage(CC.RED + "You cannot ban yourself from your party.");
                return;
            }

            if (profile.getParty().getBanned().contains(target)) {
                player.sendMessage(CC.RED + "That player is already banned.");
                return;
            }

            if (profile.getParty().containsPlayer(target)) {
                profile.getParty().leave(target, true);
            }

            player.sendMessage(CC.GREEN + "Successfully banned that player");
            profile.getParty().ban(target);
        }
    }

    public static class PartyUnBanCommand extends BaseCommand {
        @Command(name = "party.unban", aliases = "p.unban", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You do not have a party.");
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                player.sendMessage(CC.RED + "That player is not online");
                return;
            }

            if (!profile.getParty().isLeader(player.getUniqueId())) {
                player.sendMessage(CC.RED + "You are not the leader of your party.");
                return;
            }

            if (player.equals(target)) {
                player.sendMessage(CC.RED + "You cannot unban yourself from your party.");
                return;
            }

            if (!profile.getParty().getBanned().contains(target)) {
                player.sendMessage(CC.RED + "That player is not banned from your party.");
                return;
            }

            player.sendMessage(CC.GREEN + "Successfully unbanned that player");
            profile.getParty().unban(target);

        }
    }
}