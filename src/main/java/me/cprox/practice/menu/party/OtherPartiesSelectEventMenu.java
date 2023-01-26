package me.cprox.practice.menu.party;

import lombok.AllArgsConstructor;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.duel.DuelProcedure;
import me.cprox.practice.menu.duel.DuelSelectKitMenu;
import me.cprox.practice.party.OtherPartyEvent;
import me.cprox.practice.party.Party;
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
public class OtherPartiesSelectEventMenu extends Menu {
    Player player;
    Player target;
    Party party;

    @Override
    public String getTitle(Player player) {
        return "&cSelect an action for &4" + target.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(4, new SelectManageButton(OtherPartyEvent.KIT));
        return buttons;
    }

    @AllArgsConstructor
    private class SelectManageButton extends Button {
        private final OtherPartyEvent partyManage;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(partyManage == OtherPartyEvent.KIT ? Material.GOLD_SWORD : Material.GOLD_CHESTPLATE)
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

            if (partyManage == OtherPartyEvent.KIT) {
                Profile senderProfile = Profile.getByUuid(player.getUniqueId());
                DuelProcedure procedure = new DuelProcedure(player, party.getLeader().getPlayer(), true);
                senderProfile.setDuelProcedure(procedure);

                new DuelSelectKitMenu("normal").openMenu(player);
            } else {
                Profile senderProfile = Profile.getByUuid(player.getUniqueId());
                DuelProcedure procedure = new DuelProcedure(player, party.getLeader().getPlayer(), true);
                senderProfile.setDuelProcedure(procedure);
                Arena arena = Arena.getRandom(Kit.getByName("NoDebuff"));
                procedure.setKit(Kit.getByName("HCFDIAMOND"));
                procedure.setArena(arena);
                Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);
                player.closeInventory();

                procedure.send();
            }
        }
    }
}