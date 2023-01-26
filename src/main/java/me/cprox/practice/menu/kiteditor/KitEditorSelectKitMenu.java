package me.cprox.practice.menu.kiteditor;

import lombok.AllArgsConstructor;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitEditorSelectKitMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "&4&lSelect a kit for edit";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Button BLACK_PANE = Button.placeholder(Material.STAINED_GLASS_PANE, (byte)15, " ");
        Map<Integer, Button> buttons = new HashMap<>();
        int x = 1;
        int y = 1;
        for (Kit kit : Kit.getKits()) {
            if (!kit.isEnabled() || !kit.getGameRules().isEditable()) continue;

            buttons.put(getSlot(x++, y), new KitDisplayButton(kit));

            if (x == 8) {
                y++;
                x = 1;
            }

            for (int i = 0; i < getSlot(); i++) {
                buttons.putIfAbsent(i, BLACK_PANE);
            }
        }

        return buttons;
    }

    @AllArgsConstructor
    private static class KitDisplayButton extends Button {

        private Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(kit.getDisplayIcon())
                    .name(kit.getDisplayName())
                    .lore("")
                    .lore("&fClick to view &7&l" + kit.getName() + "&f's kits")
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {

            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.getKitEditor().setSelectedKit(kit);
            profile.getKitEditor().setPreviousState(profile.getState());

            new KitManagementMenu(kit).openMenu(player);
            player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);
        }

    }

    public int getSlot() {
        if (Kit.getEditableKits().size() <= 7) return 27;
        if (Kit.getEditableKits().size() > 7 && Kit.getEditableKits().size() <= 14) return 36;
        if (Kit.getEditableKits().size() > 14 && Kit.getEditableKits().size() <= 21) return 45;
        if (Kit.getEditableKits().size() > 21 && Kit.getEditableKits().size() <= 28) return 54;
        if (Kit.getEditableKits().size() > 28 && Kit.getEditableKits().size() <= 35) return 73;
        if (Kit.getEditableKits().size() > 35 && Kit.getEditableKits().size() <= 42) return 62;
        if (Kit.getEditableKits().size() > 42 && Kit.getEditableKits().size() <= 49) return 81;
        return 90;
    }
}