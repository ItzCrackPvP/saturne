package me.cprox.practice.menu.events;

import me.cprox.practice.Practice;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.external.TimeUtil;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class HostEventMenu extends Menu {

    @Override
    public String getTitle(final Player player) {
        return "&7&lHost Event";
    }

    @Override
    public Map<Integer, Button> getButtons(final Player player) {
        Button BLACK_PANE = Button.placeholder(Material.STAINED_GLASS_PANE, (byte)15, " ");
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(getSlot(2, 1), new BracketsButton());
        buttons.put(getSlot(3, 1), new SumoButton());
        buttons.put(getSlot(4, 1), new ParkourButton());
        buttons.put(getSlot(5, 1), new SpleefButton());
        buttons.put(getSlot(6, 1), new SkyWarsButton());

        for (int i = 0; i < 27; i++) {
            buttons.putIfAbsent(i, BLACK_PANE);
        }
        return buttons;
    }

    private static class BracketsButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            String brackets = (!Practice.get().getBracketsManager().getCooldown().hasExpired() ? "&cCooldown: " + TimeUtil.millisToTimer(Practice.get().getBracketsManager().getCooldown().getRemaining()) : "&aHostable");
            return new ItemBuilder(Material.DIAMOND_SWORD)
                    .name("&7&lBrackets")
                    .lore(Arrays.asList(
                            "&fFight around random players",
                            "&fbeat your opponent in 1v1",
                            "&fthe last player wins!",
                            "",
                            brackets,
                            "",
                            "&7&oLeft-Click to host",
                            "&7&oRight-Click to join"))
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType.isLeftClick()) {
                if (player.hasPermission("brackets.host")) {
                    new SelectEventKitMenu().openMenu(player);
                } else {
                    player.closeInventory();
                    player.sendMessage(CC.translate("&7You do not have permission to execute this command."));
                }
            } else {
                if (Practice.get().getBracketsManager().getActiveBrackets() != null) {
                    player.performCommand("brackets join");
                    player.closeInventory();
                } else {
                    player.closeInventory();
                }
            }
        }
    }

    private static class SumoButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            String sumo = (!Practice.get().getSumoManager().getCooldown().hasExpired() ? "&cCooldown: " + TimeUtil.millisToTimer(Practice.get().getSumoManager().getCooldown().getRemaining()) : "&aHostable");
            return new ItemBuilder(Material.LEASH)
                    .name("&7&lSumo")
                    .lore(Arrays.asList(
                            "&fKnock opponents from the",
                            "&fplatform until you will",
                            "&fthe latest player",
                            "",
                            sumo,
                            "",
                            "&7&oLeft-Click to host",
                            "&7&oRight-Click to join"))
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType.isLeftClick()) {
                if (player.hasPermission("sumo.host")) {
                    player.performCommand("sumo host");
                    player.closeInventory();
                } else {
                    player.closeInventory();
                    player.sendMessage(CC.translate("&7You do not have permission to execute this command."));
                }
            } else {
                if (Practice.get().getBracketsManager().getActiveBrackets() != null) {
                    player.performCommand("sumo join");
                    player.closeInventory();
                } else {
                    player.closeInventory();
                }
            }
        }
    }

    private static class ParkourButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(Material.FEATHER)
                    .name("&7&lParkour")
                    .lore(Arrays.asList(
                            "&fMake your way through the",
                            "&fcourse and beat the others!",
                            "&fThe player to reach the goal wins",
                            "",
                            "&7&oLeft-Click to host",
                            "&7&oRight-Click to join"))
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType.isLeftClick()) {
                if (player.hasPermission("brackets.host")) {
                    player.performCommand("brackets host");
                    player.closeInventory();
                } else {
                    player.closeInventory();
                    player.sendMessage(CC.translate("&7You do not have permission to execute this command."));
                }
            } else {
                if (Practice.get().getBracketsManager().getActiveBrackets() != null) {
                    player.performCommand("brackets join");
                    player.closeInventory();
                } else {
                    player.closeInventory();
                }
            }
        }
    }

    private static class SpleefButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            String spleef = (!Practice.get().getSpleefManager().getCooldown().hasExpired() ? "&cCooldown: " + TimeUtil.millisToTimer(Practice.get().getSpleefManager().getCooldown().getRemaining()) : "&aHostable");
            return new ItemBuilder(Material.DIAMOND_SPADE)
                    .name("&7&lSpleef")
                    .lore(Arrays.asList(
                            "&fBreak snow blocks",
                            "&favoid falling into the",
                            "&fwater, last player wins!",
                            "",
                            spleef,
                            "",
                            "&7&oLeft-Click to host",
                            "&7&oRight-Click to join"))
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType.isLeftClick()) {
                if (player.hasPermission("spleef.host")) {
                    player.performCommand("spleef host");
                    player.closeInventory();
                } else {
                    player.closeInventory();
                    player.sendMessage(CC.translate("&7You do not have permission to execute this command."));
                }
            } else {
                if (Practice.get().getBracketsManager().getActiveBrackets() != null) {
                    player.performCommand("spleef join");
                    player.closeInventory();
                } else {
                    player.closeInventory();
                }
            }
        }
    }

    private static class SkyWarsButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(Material.EYE_OF_ENDER)
                    .name("&7&lSkyWars")
                    .lore(Arrays.asList(
                            "&fLoot island chests",
                            "&favoid falling into the",
                            "&fvoid, last player wins!",
                            "",
                            "&7&oLeft-Click to host",
                            "&7&oRight-Click to join"))
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType.isLeftClick()) {
                if (player.hasPermission("brackets.host")) {
                    player.performCommand("brackets host");
                    player.closeInventory();
                } else {
                    player.closeInventory();
                    player.sendMessage(CC.translate("&7You do not have permission to execute this command."));
                }
            } else {
                if (Practice.get().getBracketsManager().getActiveBrackets() != null) {
                    player.performCommand("brackets join");
                    player.closeInventory();
                } else {
                    player.closeInventory();
                }
            }
        }
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }
}
