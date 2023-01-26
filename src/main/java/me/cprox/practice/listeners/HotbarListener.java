package me.cprox.practice.listeners;

import me.cprox.practice.events.brackets.Brackets;
import me.cprox.practice.events.spleef.Spleef;
import me.cprox.practice.events.sumo.Sumo;
import me.cprox.practice.menu.events.HostEventMenu;
import me.cprox.practice.menu.kiteditor.KitEditorSelectKitMenu;
import me.cprox.practice.menu.match.MatchList;
import me.cprox.practice.menu.queue.UnrankedMenu;
import me.cprox.practice.menu.queue.ranked.RankedSoloMenu;
import me.cprox.practice.menu.settings.SettingsMenu;
import me.cprox.practice.party.Party;
import me.cprox.practice.menu.party.PartyEventSelectEventMenu;
import me.cprox.practice.menu.party.OtherPartiesMenu;
import me.cprox.practice.menu.party.ManagePartySettings;
import me.cprox.practice.menu.stats.RankedLeaderboards;
import me.cprox.practice.party.PartyMessage;
import me.cprox.practice.profile.enums.HotbarType;
import me.cprox.practice.profile.hotbar.Hotbar;
import me.cprox.practice.profile.meta.ProfileRematchData;
import me.cprox.practice.Practice;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class HotbarListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getItem() != null && event.getAction().name().contains("RIGHT")) {
            final Player player = event.getPlayer();
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            final HotbarType hotbarType= Hotbar.fromItemStack(event.getItem());
            if (hotbarType == null) {
                return;
            }
            event.setCancelled(true);
            switch (hotbarType) {
                case QUEUE_JOIN_UNRANKED: {
                    if (!profile.isBusy(player)) {
                        new UnrankedMenu().openMenu(player);
                        break;
                    }
                    break;
                }
                case QUEUE_JOIN_RANKED: {
                    if (!player.hasPermission("practice.ranked")) {
                            if (profile.getTotalWins() < 10) {
                                    player.sendMessage(CC.translate("&cYou need to win at least 10 matches to play ranked!"));
                                break;
                            }
                    }
                    if (!profile.isBusy(player)) {
                        new RankedSoloMenu().openMenu(player);
                        break;
                    }
                    break;
                }
                case PARTY_EVENTS: {
                    new PartyEventSelectEventMenu().openMenu(player);
                    break;
                }
                case OTHER_PARTIES: {
                    new OtherPartiesMenu().openMenu(event.getPlayer());
                    break;
                }
                case PARTY_INFO: {
                    profile.getParty().sendInformation(player);
                    break;
                }
                case PARTY_SETTINGS: {
                    new ManagePartySettings().openMenu(event.getPlayer());
                    break;
                }
                case PARTY_CREATE: {
                    if (profile.getParty() != null) {
                        player.sendMessage(CC.RED + "You already have a party.");
                        return;
                    }

                    if (!profile.isInLobby()) {
                        player.sendMessage(CC.RED + "You must be in the lobby to create a party.");
                        return;
                    }

                    profile.setParty(new Party(player));
                    profile.refreshHotbar();

                    player.sendMessage(PartyMessage.CREATED.format());
                    break;
                }
                case PARTY_DISBAND: {
                    if (profile.getParty() == null) {
                        player.sendMessage(CC.RED + "You do not have a party.");
                        return;
                    }

                    if (!profile.getParty().isLeader(player.getUniqueId())) {
                        player.sendMessage(CC.RED + "You are not the leader of your party.");
                        return;
                    }

                    profile.getParty().disband();
                    break;
                }
                case PARTY_INFORMATION: {
                    if (profile.getParty() == null) {
                        player.sendMessage(CC.RED + "You do not have a party.");
                        return;
                    }

                    profile.getParty().sendInformation(player);
                    break;
                }
                case PARTY_LEAVE: {
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
                case SPECTATE_MATCH: {
                    if (profile.isManaging()) {
                        new MatchList().openMenu(player);
                    }
                    break;
                }
                case SYSTEMMANAGER_LEAVE: {
                        profile.setState(ProfileState.IN_LOBBY);
                        PlayerUtil.reset(profile.getPlayer(), false);
                        profile.refreshHotbar();
                    break;
                }
                case QUEUE_LEAVE: {
                    if (profile.isInQueue()) {
                        profile.getQueue().removePlayer(profile.getQueueProfile());
                        break;
                    }
                    break;
                }
                case EVENT_JOIN: {
                    if (!event.getPlayer().hasPermission("event.host")) {
                        player.sendMessage(CC.translate("&cYou do not have permission to host events!"));
                        break;
                    }
                    new HostEventMenu().openMenu(player);
                    break;
                }
                case MAIN_MENU: {
                    new SettingsMenu().openMenu(player);
                    break;
                }
                case LEADERBOARDS_MENU: {
                    new RankedLeaderboards().openMenu(event.getPlayer());
                    break;
                }
                case KIT_EDITOR: {
                    if (profile.isInLobby() || profile.isInQueue()) {
                        new KitEditorSelectKitMenu().openMenu(event.getPlayer());
                        break;
                    }
                    break;
                }
                case SUMO_LEAVE: {
                    final Sumo activeSumo = Practice.get().getSumoManager().getActiveSumo();
                    if (activeSumo == null) {
                        player.sendMessage(CC.RED + "There is no active sumo.");
                        return;
                    }
                    if (!profile.isInSumo() || !activeSumo.getEventPlayers().containsKey(player.getUniqueId())) {
                        player.sendMessage(CC.RED + "You are not apart of the active sumo.");
                        return;
                    }
                    Practice.get().getSumoManager().getActiveSumo().handleLeave(player);
                    break;
                }
                case BRACKETS_LEAVE: {
                    final Brackets activeBrackets = Practice.get().getBracketsManager().getActiveBrackets();
                    if (activeBrackets == null) {
                        player.sendMessage(CC.RED + "There is no active brackets.");
                        return;
                    }
                    if (!profile.isInBrackets() || !activeBrackets.getEventPlayers().containsKey(player.getUniqueId())) {
                        player.sendMessage(CC.RED + "You are not apart of the active brackets.");
                        return;
                    }
                    Practice.get().getBracketsManager().getActiveBrackets().handleLeave(player);
                    break;
                }
                case SPLEEF_LEAVE: {
                    final Spleef activeSpleef = Practice.get().getSpleefManager().getActiveSpleef();
                    if (activeSpleef == null) {
                        player.sendMessage(CC.RED + "There is no active Spleef.");
                        return;
                    }
                    if (!profile.isInSpleef() || !activeSpleef.getEventPlayers().containsKey(player.getUniqueId())) {
                        player.sendMessage(CC.RED + "You are not apart of the active Spleef.");
                        return;
                    }
                    Practice.get().getSpleefManager().getActiveSpleef().handleLeave(player);
                    break;
                }
                case SPECTATE_STOP: {
                    if (profile.isInFight() && !profile.getMatch().getTeamPlayer(player).isAlive()) {
                        profile.getMatch().getTeamPlayer(player).setDisconnected(true);
                        profile.setState(ProfileState.IN_LOBBY);
                        profile.setMatch(null);
                        break;
                    }
                    if (!profile.isSpectating()) {
                        player.sendMessage(CC.RED + "You are not spectating a match.");
                        break;
                    }
                    if (profile.getMatch() != null) {
                        profile.getMatch().removeSpectator(player);
                        break;
                    }
                    if (profile.getSumo() != null) {
                        profile.getSumo().removeSpectator(player);
                        break;
                    }
                    if (profile.getBrackets() != null) {
                        profile.getBrackets().removeSpectator(player);
                        break;
                    }
                    if (profile.getSpleef() != null) {
                        profile.getSpleef().removeSpectator(player);
                        break;
                    }
                    break;
                }
                case REMATCH_REQUEST:
                case REMATCH_ACCEPT: {
                    if (profile.getRematchData() == null) {
                        player.sendMessage(CC.RED + "You do not have anyone to re-match.");
                        return;
                    }
                    profile.checkForHotbarUpdate();
                    if (profile.getRematchData() == null) {
                        player.sendMessage(CC.RED + "That player is no longer available.");
                        return;
                    }
                    final ProfileRematchData profileRematchData = profile.getRematchData();
                    if (profileRematchData.isReceive()) {
                        profileRematchData.accept();
                    }
                    else {
                        if (profileRematchData.isSent()) {
                            player.sendMessage(CC.RED + "You have already sent a rematch request to that player.");
                            return;
                        }
                        profileRematchData.request();
                    }
                    break;
                }
                default: {}
            }
        }
    }
}

