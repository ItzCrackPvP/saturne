package me.cprox.practice.menu.party;

import lombok.AllArgsConstructor;
import me.cprox.practice.party.PartyManage;
import me.cprox.practice.party.PartyPrivacy;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class ManagePartySettings extends Menu {
    {
        setUpdateAfterClick(false);
    }

    @Override
    public String getTitle(Player player) {
        return "&cParty Settings";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(2, new SelectManageButton(PartyManage.INCREMENTLIMIT));
        buttons.put(4, new SelectManageButton(PartyManage.PUBLIC));
        buttons.put(6, new SelectManageButton(PartyManage.DECREASELIMIT));
        return buttons;
    }

    @AllArgsConstructor
    private static class SelectManageButton extends Button {
        private final PartyManage partyManage;

        @Override
        public ItemStack getButtonItem(Player player) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            if (partyManage == PartyManage.INCREMENTLIMIT) {
                return new ItemBuilder(Material.INK_SACK)
                        .durability(10)
                        .name("&c" + partyManage.getName())
                        .lore("&7Limit: " + profile.getParty().getLimit())
                        .build();
            } else if (partyManage == PartyManage.PUBLIC) {
                return new ItemBuilder(Material.PAPER)
                        .name("&c" + partyManage.getName())
                        .lore("&7Public: " + profile.getParty().isPublic())
                        .build();
            } else {
                return new ItemBuilder(Material.INK_SACK)
                        .durability(1)
                        .name("&c" + partyManage.getName())
                        .lore("&7Limit: " + profile.getParty().getLimit())
                        .build();
            }
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You are not in a party.");
                Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);
                player.closeInventory();
                return;
            }

            if (!player.hasPermission("practice.donator")) {
                player.sendMessage(CC.RED + "You need a Donator Rank for this");
                Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);
                player.closeInventory();
                return;
            }

            if (partyManage == PartyManage.INCREMENTLIMIT) {
                if (profile.getParty().getLimit() < 100) {
                    profile.getParty().setLimit(profile.getParty().getLimit() + 1);
                }
            } else if (partyManage == PartyManage.PUBLIC) {
                if (!profile.getParty().isPublic()) {
                    profile.getParty().setPublic(true);
                    profile.getParty().setPrivacy(PartyPrivacy.OPEN);
                } else {
                    profile.getParty().setPublic(false);
                    profile.getParty().setPrivacy(PartyPrivacy.CLOSED);
                }
            } else {
                if (profile.getParty().getLimit() > 1) {
                    profile.getParty().setLimit(profile.getParty().getLimit() - 1);
                }
            }
        }

        @Override
        public boolean shouldUpdate(Player player, ClickType clickType) {
            return true;
        }
    }
}
