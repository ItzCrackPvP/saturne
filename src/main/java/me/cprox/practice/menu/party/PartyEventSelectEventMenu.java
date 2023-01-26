package me.cprox.practice.menu.party;

import lombok.AllArgsConstructor;
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

public class PartyEventSelectEventMenu extends Menu {
    @Override
    public String getTitle(Player player) {
        return "&cSelect a party event";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(3, new SelectEventButton(PartyEvent.SPLIT));
        buttons.put(5, new SelectEventButton(PartyEvent.FFA));
        return buttons;
    }

    @AllArgsConstructor
    private static class SelectEventButton extends Button {
        private final PartyEvent partyEvent;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(partyEvent.getMaterial())
                    .name("&c" + partyEvent.getName())
                    .lore("&7" + partyEvent.getLore())
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You are not in a party.");
                return;
            }

            if (partyEvent == PartyEvent.FFA || partyEvent == PartyEvent.SPLIT) {
                Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);
                new PartyEventSelectKitMenu(partyEvent).openMenu(player);
            }
        }
    }
}
