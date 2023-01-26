package me.cprox.practice.menu.party;

import lombok.AllArgsConstructor;
import me.cprox.practice.party.PartyManage;
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
public class ManagePartyMember extends Menu {
    Player target;

    @Override
    public String getTitle(Player player) {
        return "&cSelect an action for &4" + target.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(3, new SelectManageButton(PartyManage.LEADER));
        buttons.put(5, new SelectManageButton(PartyManage.KICK));
        return buttons;
    }

    @AllArgsConstructor
    private class SelectManageButton extends Button {
        private final PartyManage partyManage;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(partyManage == PartyManage.LEADER ? Material.GOLD_SWORD : Material.REDSTONE)
                    .name("&4&l" + partyManage.getName())
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getParty() == null) {
                player.sendMessage(CC.RED + "You are not in a party.");
                return;
            }

            if (partyManage == PartyManage.LEADER) {
                profile.getParty().leader(player, target);
            } else {
                profile.getParty().leave(target, true);
            }
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);
            player.closeInventory();
        }
    }
}
