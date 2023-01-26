package me.cprox.practice.menu.events;

import lombok.AllArgsConstructor;
import me.cprox.practice.Practice;
import me.cprox.practice.events.brackets.Brackets;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SelectEventKitMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "&4&lSelect a kit for event";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int x = 1;
        int y = 1;

        for (Kit kit : Kit.getKits()) {
            buttons.put(getSlot(x++, y), new SelectKitButton(kit));

            if (x == 8) {
                y++;
                x = 1;
            }
        }
        return buttons;
    }

    @AllArgsConstructor
    private static class SelectKitButton extends Button {
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(kit.getDisplayIcon())
                    .name(kit.getDisplayName())
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            player.closeInventory();
            if (Practice.get().getBracketsManager().getActiveBrackets() != null) {
                player.sendMessage(CC.RED + "There is already an active Brackets event.");
                return;
            }

            if (!Practice.get().getBracketsManager().getCooldown().hasExpired()) {
                player.sendMessage(CC.RED + "There is an active cooldown for the Brackets event.");
                return;
            }

            Practice.get().getBracketsManager().setActiveBrackets(new Brackets(player, kit));
            player.performCommand("brackets join");
            for (Player other : Practice.get().getServer().getOnlinePlayers()) {
                Profile profile = Profile.getByUuid(other.getUniqueId());

                if (profile.isInLobby()) {
                    if (!profile.getKitEditor().isActive()) {
                        PlayerUtil.reset(player, false);
                        profile.refreshHotbar();
                    }
                }
            }
        }
    }
}