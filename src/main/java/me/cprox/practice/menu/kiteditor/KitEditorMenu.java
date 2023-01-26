package me.cprox.practice.menu.kiteditor;

import me.cprox.practice.Practice;
import me.cprox.practice.kit.KitInventory;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.external.BukkitReflection;
import me.cprox.practice.util.external.ItemBuilder;
import lombok.AllArgsConstructor;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import me.cprox.practice.util.menu.button.DisplayButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitEditorMenu extends Menu {

    private static final int[] ITEM_POSITIONS = new int[]{20, 21, 22, 23, 24, 25, 26, 29, 30, 31, 32, 33, 34, 35, 38, 39, 40, 41, 42, 43, 44, 47, 48, 49, 50, 51, 52, 53};
    private static final int[] BORDER_POSITIONS = new int[]{1, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 28, 37, 46};
    private static final Button BORDER_BUTTON = Button.placeholder(Material.WOOL, (byte) 15, " ");

    private final Kit kit;

    public KitEditorMenu(Kit kit) {
        this.kit = kit;
    }

    {
        setUpdateAfterClick(false);
    }

    @Override
    public String getTitle(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());
        return "&7&lEditing: " + profile.getKitEditor().getSelectedKit().getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Button BLACK_PANE = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 15, " ");
        Map<Integer, Button> buttons = new HashMap<>();

        for (int border : BORDER_POSITIONS) {
            buttons.put(border, BORDER_BUTTON);
        }

        buttons.put(0, new CurrentKitButton());
        buttons.put(2, new SaveButton());
        buttons.put(6, new LoadDefaultKitButton());
        buttons.put(7, new ClearInventoryButton());
        buttons.put(8, new CancelButton());

        Profile profile = Profile.getByUuid(player.getUniqueId());
        Kit kit = profile.getKitEditor().getSelectedKit();
        KitInventory kitLoadout = profile.getKitEditor().getSelectedKitInventory();

        buttons.put(18, new ArmorDisplayButton(kitLoadout.getArmor()[3]));
        buttons.put(27, new ArmorDisplayButton(kitLoadout.getArmor()[2]));
        buttons.put(36, new ArmorDisplayButton(kitLoadout.getArmor()[1]));
        buttons.put(45, new ArmorDisplayButton(kitLoadout.getArmor()[0]));

        List<ItemStack> items = kit.getEditRules().getEditorItems();

        if (!kit.getEditRules().getEditorItems().isEmpty()) {
            for (int i = 20; i < (kit.getEditRules().getEditorItems().size() + 20); i++) {
                buttons.put(ITEM_POSITIONS[i - 20], new InfiniteItemButton(items.get(i - 20)));
            }
        }

        for (int i = 0; i < 54; i++) {
            buttons.putIfAbsent(i, BLACK_PANE);
        }

        return buttons;
    }

    @Override
    public void onOpen(Player player) {
        if (!isClosedByMenu()) {
            PlayerUtil.reset(player);

            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.getKitEditor().setActive(true);
            profile.getKitEditor().setSelectedKit(kit);

            if (profile.getKitEditor().getSelectedKit() != null) {
                player.getInventory().setContents(profile.getKitEditor().getSelectedKitInventory().getContents());
            }

            player.updateInventory();
        }
    }

    @AllArgsConstructor
    private static class ArmorDisplayButton extends Button {

        private final ItemStack itemStack;

        @Override
        public ItemStack getButtonItem(Player player) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return new ItemStack(Material.AIR);
            }

            return new ItemBuilder(itemStack.clone())
                    .name(CC.AQUA + BukkitReflection.getItemStackName(itemStack))
                    .lore(" ")
                    .lore(CC.AQUA + "This is automatically equipped.")
                    .build();
        }

    }

    @AllArgsConstructor
    private static class CurrentKitButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.NAME_TAG)
                    .name("&7&lEditing: &f" + profile.getKitEditor().getSelectedKit().getName())
                    .build();
        }

    }

    @AllArgsConstructor
    private static class ClearInventoryButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.WOOL)
                    .durability(1)
                    .name("&6&lClear Inventory")
                    .lore(Arrays.asList(
                            "&7This will clear your inventory",
                            "&7so you can start over."))
                    .build();
        }

        @Override
        public void clicked(Player player, int i, ClickType clickType, int hb) {
            player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);
            player.getInventory().clear();
            player.updateInventory();
        }

        @Override
        public boolean shouldUpdate(Player player, ClickType clickType) {
            return true;
        }

    }


    @AllArgsConstructor
    private static class LoadDefaultKitButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.WOOL)
                    .durability(4)
                    .name("&e&lLoad default kit")
                    .lore(Arrays.asList(
                            "&7Click this to load the default kit",
                            "&7into the kit editing menu."))
                    .build();
        }

        @Override
        public void clicked(Player player, int i, ClickType clickType, int hb) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);

            player.getInventory().setContents(profile.getKitEditor().getSelectedKit().getKitInventory().getContents());
            player.updateInventory();
        }

        @Override
        public boolean shouldUpdate(Player player, ClickType clickType) {
            return true;
        }

    }

    @AllArgsConstructor
    private static class SaveButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.WOOL)
                    .durability(5)
                    .name("&a&lSave")
                    .lore("&7Click this to save your kit.")
                    .build();
        }

        @Override
        public void clicked(Player player, int i, ClickType clickType, int hb) {
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (profile.getKitEditor().getSelectedKitInventory() != null)
                profile.getKitEditor().getSelectedKitInventory().setContents(player.getInventory().getContents());
            profile.save();
            profile.refreshHotbar();
            profile.getKitEditor().setSelectedKitInventory(null);
            (new KitManagementMenu(profile.getKitEditor().getSelectedKit())).openMenu(player);
        }

    }

    @AllArgsConstructor
    private static class CancelButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.WOOL)
                    .durability(14)
                    .name("&c&lCancel")
                    .lore(Arrays.asList(
                            "&7Click this to abort editing your kit,",
                            "&7and return to the kit menu."))
                    .build();
        }

        @Override
        public void clicked(Player player, int i, ClickType clickType, int hb) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);

            if (profile.getKitEditor().getSelectedKit() != null) {
                profile.refreshHotbar();
                profile.getKitEditor().setSelectedKitInventory(null);
                new KitManagementMenu(profile.getKitEditor().getSelectedKit()).openMenu(player);
            }
        }

    }

    private static class InfiniteItemButton extends DisplayButton {

        InfiniteItemButton(ItemStack itemStack) {
            super(itemStack, false);
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbar) {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            ItemStack itemStack = inventory.getItem(slot);

            inventory.setItem(slot, itemStack);

            player.setItemOnCursor(itemStack);
            player.updateInventory();
        }

    }
}