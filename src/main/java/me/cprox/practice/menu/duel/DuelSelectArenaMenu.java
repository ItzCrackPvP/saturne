package me.cprox.practice.menu.duel;

import lombok.AllArgsConstructor;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ArenaType;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.pagination.PaginatedMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DuelSelectArenaMenu extends PaginatedMenu {
    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&7&lSelect an arena";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Button BLACK_PANE = Button.placeholder(Material.STAINED_GLASS_PANE, (byte)15, " ");
        Map<Integer, Button> button = new HashMap<>();
        int x = 1;
        int y = 1;

        for (Arena arena : Arena.getArenas()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            if (!arena.isSetup()) continue;
            if (arena.getType() == ArenaType.DUPLICATE) continue;
            if (profile.getDuelProcedure().getKit().getGameRules().isBuild() && arena.getType() == ArenaType.SHARED) continue;
            if (!arena.getKits().contains(profile.getDuelProcedure().getKit().getName())) continue;

            button.put(getSlot(x++, y), new SelectArenaButton(arena));

            for (int i = 0; i < getSlots(profile.getDuelProcedure().getKit().getName()); i++) {
                button.putIfAbsent(i, BLACK_PANE);
            }

            if (x == 8) {
                y++;
                x = 1;
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
    private static class SelectArenaButton extends Button {

        private final Arena arena;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(arena.getDisplayIcon())
                    .name(arena.getName())
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.getDuelProcedure() == null) {
                player.sendMessage(CC.RED + "Could not find duel procedure.");
                return;
            }

            profile.getDuelProcedure().setArena(arena);
            profile.getDuelProcedure().send();
            player.closeInventory();

        }
    }

    public int getSlots(String kit) {
        if (Arena.getSetupArenas(kit).size() <= 7) return 27;
        if (Arena.getSetupArenas(kit).size() > 7 && Arena.getSetupArenas(kit).size() <= 14) return 36;
        if (Arena.getSetupArenas(kit).size() > 14 && Arena.getSetupArenas(kit).size() <= 21) return 45;
        if (Arena.getSetupArenas(kit).size() > 21 && Arena.getSetupArenas(kit).size() <= 28) return 54;
        if (Arena.getSetupArenas(kit).size() > 28 && Arena.getSetupArenas(kit).size() <= 35) return 63;
        if (Arena.getSetupArenas(kit).size() > 35 && Arena.getSetupArenas(kit).size() <= 42) return 72;
        if (Arena.getSetupArenas(kit).size() > 42 && Arena.getSetupArenas(kit).size() <= 49) return 81;
        return 90;
    }
}