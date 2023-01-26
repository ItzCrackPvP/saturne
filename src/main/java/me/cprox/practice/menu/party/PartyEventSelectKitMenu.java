package me.cprox.practice.menu.party;

import lombok.AllArgsConstructor;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.impl.FFAMatch;
import me.cprox.practice.match.impl.TeamMatch;
import me.cprox.practice.match.team.Team;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.party.Party;
import me.cprox.practice.party.PartyEvent;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@AllArgsConstructor
public class PartyEventSelectKitMenu extends Menu {
    private final PartyEvent partyEvent;

    @Override
    public String getTitle(Player player) {
        return "&cSelect a kit";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (Kit kit : Kit.getKits()) {
            if (kit.isEnabled() && kit.getGameRules().isPartyffa()) {
                buttons.put(buttons.size(), new SelectKitButton(partyEvent, kit));
            }
        }
        return buttons;
    }

    @AllArgsConstructor
    private static class SelectKitButton extends Button {
        private final PartyEvent partyEvent;
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(kit.getDisplayIcon())
                    .name("&4" + kit.getName())
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);
            player.closeInventory();
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You are not in a party.");
                return;
            }

            if (profile.getParty().getTeamPlayers().size() <= 1) {
                player.sendMessage(CC.RED + "You do not have enough players in your party to start an sumo.");
                return;
            }

            Party party = profile.getParty();
            Arena arena = Arena.getRandom(kit);

            if (arena == null) {
                player.sendMessage(CC.RED + "There are no available arenas.");
                return;
            }

            arena.setActive(true);
            Match match;

            if (partyEvent == PartyEvent.FFA) {
                Team team = new Team(new TeamPlayer(party.getLeader().getPlayer()));
                List<Player> players = new ArrayList<>(party.getPlayers());
                match = new FFAMatch(team, kit, arena);

                for (Player otherPlayer : players) {
                    if (team.getLeader().getUuid().equals(otherPlayer.getUniqueId())) {
                        continue;
                    }
                    team.getTeamPlayers().add(new TeamPlayer(otherPlayer));
                }
            } else {
                Team teamA = new Team(new TeamPlayer(party.getPlayers().get(0)));
                Team teamB = new Team(new TeamPlayer(party.getPlayers().get(1)));
                List<Player> players = new ArrayList<>(party.getPlayers());
                Collections.shuffle(players);

                // Create match
               /* if(kit.getGameRules().isSumo()) {
                    match = new SumoTeamMatch(teamA, teamB, kit, arena);
                } else {*/
                    match = new TeamMatch(teamA, teamB, kit, arena);
               // }

                for (Player otherPlayer : players) {
                    if (teamA.getLeader().getUuid().equals(otherPlayer.getUniqueId()) ||
                            teamB.getLeader().getUuid().equals(otherPlayer.getUniqueId())) {
                        continue;
                    }
                    if (teamA.getTeamPlayers().size() > teamB.getTeamPlayers().size()) {
                        teamB.getTeamPlayers().add(new TeamPlayer(otherPlayer));
                    } else {
                        teamA.getTeamPlayers().add(new TeamPlayer(otherPlayer));
                    }
                }
            }
            match.start();
        }
    }
}
