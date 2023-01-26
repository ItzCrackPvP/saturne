package me.cprox.practice.menu.queue;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import me.cprox.practice.Practice;
import me.cprox.practice.menu.queue.unranked.UnrankedSoloMenu;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;

public class UnrankedMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.translate("&7&lUnranked Queues");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Button BLACK_PANE = Button.placeholder(Material.STAINED_GLASS_PANE, (byte)15, " ");
        Map<Integer, Button> button = Maps.newHashMap();

        button.put(getSlot(2, 1), new UnrankedSoloButton());
        button.put(getSlot(4, 1), new UnrankedDuosButton());
        button.put(getSlot(6, 1), new FFAButton());

        for (int i = 0; i < 27; i++) {
            button.putIfAbsent(i, BLACK_PANE);
        }
        return button;
    }

    @AllArgsConstructor
    private static class UnrankedSoloButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.DIAMOND_SWORD)
                    .name("&7&lSolos")
                    .lore(Arrays.asList(
                            "&fCasual 1v1s with",
                            "&fno loss penalty.",
                            "",
                            "&fPlayers: &7" + getSoloFights(),
                            "",
                            "&7Click to play!"))
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (player.hasMetadata("frozen")) {
                player.sendMessage(CC.RED + "You cannot queue while frozen.");
                return;
            }

            if (profile.isBusy(player)) {
                player.sendMessage(CC.RED + "You cannot queue right now.");
                return;
            }

            new UnrankedSoloMenu().openMenu(player);
        }

    }

    @AllArgsConstructor
    private static class UnrankedDuosButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.IRON_SWORD)
                    .name("&7&lDuos")
                    .lore(Arrays.asList(
                            "&fCasual 2v2s with",
                            "&fno loss penalty.",
                            "",
                            "&fPlayers: &70",
                            "",
                            "&7Click to play!"))
                    .build();
        }
    }

    @AllArgsConstructor
    private static class FFAButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.GOLD_AXE)
                    .name("&7&lFFA SOON!")
                    .lore(Arrays.asList(
                            "&fFree for all with",
                            "&finfinite respawns.",
                            "",
                            "&fPlayers: &70",
                            "",
                            "&7Click to play!"))
                    .build();
        }
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }

    public static int getSoloFights() {
        int inFights = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.isInFight()) {
                inFights++;
            }
        }
        return inFights;
    }
}
