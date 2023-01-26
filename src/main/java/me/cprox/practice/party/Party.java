package me.cprox.practice.party;

import lombok.Getter;
import lombok.Setter;
import me.cprox.practice.Practice;
import me.cprox.practice.match.duel.DuelRequest;
import me.cprox.practice.match.team.Team;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.chat.ChatHelper;
import me.cprox.practice.util.external.ChatComponentBuilder;
import me.cprox.practice.util.nametag.NameTags;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Getter
public class Party extends Team {

    @Getter
    private static final List<Party> parties = new ArrayList<>();

    @Getter
    @Setter
    private int Limit = 10;

    @Getter
    @Setter
    private boolean isPublic = false;

    private PartyPrivacy privacy = PartyPrivacy.CLOSED;
    private final List<PartyInvite> invites = new ArrayList<>();
    @Getter
    private final List<Player> banned = new ArrayList<>();
    private boolean disbanded;

    public Party(Player player) {
        super(new TeamPlayer(player.getUniqueId(), player.getName()));

        parties.add(this);
    }

    public static void init() {
        // Remove expired invites from each party every 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                Party.getParties().forEach(party -> party.getInvites().removeIf(PartyInvite::hasExpired));
            }
        }.runTaskTimerAsynchronously(Practice.get(), 20L * 5, 20L * 5);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Party party : getParties()) {
                    if (party.isPublic()) {
                        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(PartyMessage.PUBLIC.format(party.getLeader().getUsername())));
                        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.spigot().sendMessage(new ChatComponentBuilder("")
                                .parse(PartyMessage.CLICK_TO_JOIN.format())
                                .attachToEachPart(ChatHelper.click("/party join " + party.getLeader().getUsername()))
                                .attachToEachPart(ChatHelper.hover(PartyMessage.CLICK_TO_JOIN.format()))
                                .create()));
                    }
                }
            }
        }.runTaskTimerAsynchronously(Practice.get(), 1200L, 1200L);
    }

    public void setPrivacy(PartyPrivacy privacy) {
        this.privacy = privacy;

        broadcast(PartyMessage.PRIVACY_CHANGED.format(privacy.getReadable()));
    }

    public PartyInvite getInvite(UUID uuid) {
        Iterator<PartyInvite> iterator = invites.iterator();

        while (iterator.hasNext()) {
            PartyInvite invite = iterator.next();

            if (invite.getUuid().equals(uuid)) {
                if (invite.hasExpired()) {
                    iterator.remove();
                    return null;
                } else {
                    return invite;
                }
            }
        }

        return null;
    }

    public void invite(Player target) {
        invites.add(new PartyInvite(target.getUniqueId()));

        target.sendMessage(PartyMessage.YOU_HAVE_BEEN_INVITED.format(getLeader().getUsername()));
        target.spigot().sendMessage(new ChatComponentBuilder("")
                .parse(PartyMessage.CLICK_TO_JOIN.format())
                .attachToEachPart(ChatHelper.click("/party join " + getLeader().getUsername()))
                .attachToEachPart(ChatHelper.hover(CC.GREEN + "Click to to accept this party invite"))
                .create());

        broadcast(PartyMessage.PLAYER_INVITED.format(target.getName()));
    }

    public void ban(Player target) {
        banned.add(target);
    }

    public void unban(Player target) {
        banned.remove(target);
    }

    public void join(Player player) {
        invites.removeIf(invite -> invite.getUuid().equals(player.getUniqueId()));

        getTeamPlayers().add(new TeamPlayer(player));

        broadcast(PartyMessage.PLAYER_JOINED.format(player.getName()));

        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setParty(this);

        if (profile.isInLobby() || profile.isInQueue()) {
            profile.refreshHotbar();
            profile.handleVisibility();
        }

        for (TeamPlayer teamPlayer : getTeamPlayers()) {
            Player otherPlayer = teamPlayer.getPlayer();

            if (otherPlayer != null) {
                Profile teamProfile = Profile.getByUuid(teamPlayer.getUuid());
                teamProfile.handleVisibility(otherPlayer, player);
            }
        }
    }

    public void leave(Player player, boolean kick) {
        broadcast(PartyMessage.PLAYER_LEFT.format(player.getName(), (kick ? "been kicked from" : "left")));

        getTeamPlayers().removeIf(member -> member.getUuid().equals(player.getUniqueId()));

        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setParty(null);

        if (profile.isInLobby() || profile.isInQueue()) {
            profile.handleVisibility();
            profile.refreshHotbar();
        }

        if (profile.isInFight()) {
            profile.getMatch().handleDeath(player, null, true);
            if (profile.getMatch().isTeamMatch() || profile.getMatch().isHCFMatch()) {
                for (TeamPlayer secondTeamPlayer : getTeamPlayers()) {
                    if (secondTeamPlayer.isDisconnected()) {
                        continue;
                    }

                    if (secondTeamPlayer.getUuid().equals(player.getUniqueId())) {
                        continue;
                    }

                    Player secondPlayer = secondTeamPlayer.getPlayer();

                    if (secondPlayer != null) {
                        player.hidePlayer(secondPlayer);
                    }

                    NameTags.reset(player, secondPlayer);
                }
            }

            player.setFireTicks(0);
            player.updateInventory();

            profile.setState(ProfileState.IN_LOBBY);
            profile.setMatch(null);
            profile.refreshHotbar();
            profile.handleVisibility();

            Practice.get().getEssentials().teleportToSpawn(player);
        }

        for (TeamPlayer teamPlayer : getTeamPlayers()) {
            Player otherPlayer = teamPlayer.getPlayer();

            if (otherPlayer != null) {
                NameTags.reset(player, otherPlayer);
                NameTags.reset(otherPlayer, player);

                Profile otherProfile = Profile.getByUuid(teamPlayer.getUuid());
                otherProfile.handleVisibility(otherPlayer, player);
            }
        }
    }

    public void leader(Player player, Player target) {

        Profile profile = Profile.getByUuid(player.getUniqueId());
        Profile targetprofile = Profile.getByUuid(target.getUniqueId());

        for (TeamPlayer teamPlayer : getTeamPlayers()) {
            if (teamPlayer.getPlayer().equals(targetprofile.getPlayer()))
                targetprofile.getParty().setLeader(teamPlayer);
        }

        if (profile.isInLobby()) {
            profile.refreshHotbar();
        }
        if (targetprofile.isInLobby()) {
            targetprofile.refreshHotbar();
        }
    }

    public void disband() {
        parties.remove(this);
        disbanded = true;

        broadcast(PartyMessage.DISBANDED.format());

        Profile leaderProfile = Profile.getByUuid(getLeader().getUuid());

        leaderProfile.getSentDuelRequests().values().removeIf(DuelRequest::isParty);

        getPlayers().forEach(player -> {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.setParty(null);

            if (profile.isInLobby() || profile.isInQueue()) {
                profile.refreshHotbar();
                profile.handleVisibility();
            }
        });
    }

    public void sendInformation(Player player) {
        StringBuilder builder = new StringBuilder();

        for (Player member : getPlayers()) {
            if (getPlayers().size() == 1) {
                builder.append(CC.RESET)
                        .append("None")
                        .append(CC.GRAY)
                        .append(", ");
            } else {
                if (member.equals(getLeader().getPlayer())) continue;
                builder.append(CC.RESET)
                        .append(member.getName())
                        .append(CC.GRAY)
                        .append(", ");
            }
        }

        String[] lines = new String[]{
                CC.CHAT_BAR,
                CC.RED + "Party Information",
                CC.RED + "Privacy: " + CC.GRAY + privacy.getReadable(),
                CC.RED + "Leader: " + CC.RESET + getLeader().getUsername(),
                CC.RED + "Members: " + CC.GRAY + "(" + (getTeamPlayers().size() - 1) + ") " +
                        builder.substring(0, builder.length() - 2),
                CC.CHAT_BAR
        };

        player.sendMessage(lines);
    }

    public boolean isDisbanded() {
        return disbanded;
    }

}