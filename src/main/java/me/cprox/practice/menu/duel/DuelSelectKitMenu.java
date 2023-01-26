package me.cprox.practice.menu.duel;

import lombok.AllArgsConstructor;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DuelSelectKitMenu extends Menu {
    String type;

    public DuelSelectKitMenu(String type) {
        this.type = type;
    }

    @Override
    public String getTitle(Player player) {
        return "&7&lSelect a duel kit";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Button BLACK_PANE = Button.placeholder(Material.STAINED_GLASS_PANE, (byte)15, " ");
        Map<Integer, Button> button = new HashMap<>();
        int x = 1;
        int y = 1;

        for (Kit kit : Kit.getKits()) {
            if (!kit.isEnabled()) continue;
            button.put(getSlot(x++, y), new SelectKitButton(kit));

            if (x == 8) {
                y++;
                x = 1;
            }

            for (int i = 0; i < getSlots(); i++) {
                button.putIfAbsent(i, BLACK_PANE);
            }
        }

        return button;
    }

    @Override
    public void onClose(Player player) {
        if (!isClosedByMenu()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.setDuelProcedure(null);
        }
    }

    @AllArgsConstructor
    private class SelectKitButton extends Button {

        private final Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(kit.getDisplayIcon())
                    .name(kit.getDisplayName())
                    .lore(Arrays.asList(
                            "",
                            "&7&oClick to send a duel with this kit."))
                    .clearFlags()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (type.equalsIgnoreCase("normal")) {
                if (profile.getDuelProcedure() == null) {
                    player.sendMessage(CC.RED + "Could not find duel procedure.");
                    return;
                }

                Arena arena = Arena.getRandom(kit);
                profile.getDuelProcedure().setKit(kit);
                profile.getDuelProcedure().setArena(arena);
                Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

                if (profile.getSettings().isUsingMapSelector()) {
                    new DuelSelectArenaMenu().openMenu(player);
                } else {
                    profile.getDuelProcedure().send();
                    player.closeInventory();
                }
            }
        }
    }

    public int getSlots() {
        if (Kit.getEnabledKits().size() <= 7) return 27;
        if (Kit.getEnabledKits().size() > 7 && Kit.getEnabledKits().size() <= 14) return 36;
        if (Kit.getEnabledKits().size() > 14 && Kit.getEnabledKits().size() <= 21) return 45;
        if (Kit.getEnabledKits().size() > 21 && Kit.getEnabledKits().size() <= 28) return 54;
        if (Kit.getEnabledKits().size() > 28 && Kit.getEnabledKits().size() <= 35) return 63;
        if (Kit.getEnabledKits().size() > 35 && Kit.getEnabledKits().size() <= 42) return 72;
        return 99;
    }
}