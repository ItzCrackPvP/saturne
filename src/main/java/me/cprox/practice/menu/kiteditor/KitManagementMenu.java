package me.cprox.practice.menu.kiteditor;

import me.cprox.practice.kit.KitInventory;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.util.external.ItemBuilder;
import lombok.AllArgsConstructor;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import me.cprox.practice.util.menu.button.BackButton;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KitManagementMenu extends Menu {

    private static final Button PLACEHOLDER = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 14, " ");

    private final Kit kit;

    public KitManagementMenu(Kit kit) {

        this.kit = kit;
        setPlaceholder(true);
        setUpdateAfterClick(false);
    }

    @Override
    public String getTitle(Player player) {
        return "&7&lViewing &f" + kit.getName() + "&7&l's kits";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        KitInventory[] kitInventories = profile.getStatisticsData().get(kit).getLoadouts();

        if (kitInventories == null) {
            return buttons;
        }

        int startPos = -1;

        for (int i = 0; i < 4; i++) {
            startPos += 2;

            KitInventory kitInventory = kitInventories[i];
            buttons.put(startPos, kitInventory == null ? new CreateKitButton(i) : new KitDisplayButton(kitInventory));
            buttons.put(startPos + 18, kitInventory == null ? new CreateNewLoadoutButton(i) : new LoadKitButton(i));
            buttons.put(startPos + 27, kitInventory == null ? PLACEHOLDER : new RenameKitButton(kitInventory));
            buttons.put(startPos + 36, kitInventory == null ? PLACEHOLDER : new DeleteKitButton(kitInventory));
        }

        buttons.put(45, new BackButton(new KitEditorSelectKitMenu()));

        return buttons;
    }

    @Override
    public void onOpen(Player player) {
        if (!isClosedByMenu()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.setState(profile.getKitEditor().getPreviousState());
            profile.getKitEditor().setSelectedKit(this.kit);
        }
    }

    @Override
    public void onClose(Player player) {
        if (!isClosedByMenu()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.setState(profile.getKitEditor().getPreviousState());
            profile.getKitEditor().setSelectedKit(null);
        }
    }

    @AllArgsConstructor
    private class DeleteKitButton extends Button {

        private final KitInventory kitInventory;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.WOOL)
                    .name("&c&lDelete")
                    .durability(14)
                    .lore(Arrays.asList(
                            "&7Click to delete this kit.",
                            "&7You will &c&lNOT &7be able to",
                            "&7recover this kit."
                    ))
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.getStatisticsData().get(kit).deleteKit(kitInventory);
            profile.save();
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    @AllArgsConstructor
    private static class CreateKitButton extends Button {

        private final int index;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.IRON_SWORD)
                    .name("&a&lCreate Kit")
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            Kit kit = profile.getKitEditor().getSelectedKit();
            player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);

            if (kit == null) {
                player.closeInventory();
                return;
            }

            KitInventory kitLoadout = new KitInventory("Kit " + (index + 1));

            if (kit.getKitInventory() != null) {
                if (kit.getKitInventory().getArmor() != null) {
                    kitLoadout.setArmor(kit.getKitInventory().getArmor());
                }

                if (kit.getKitInventory().getContents() != null) {
                    kitLoadout.setContents(kit.getKitInventory().getContents());
                }
            }

            profile.getStatisticsData().get(kit).replaceKit(index, kitLoadout);
            profile.getKitEditor().setSelectedKitInventory(kitLoadout);
            profile.getKitEditor().getSelectedKitInventory().setCustomName(CC.translate("&7&lKit " + (index + 1)));
            profile.save();
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    @AllArgsConstructor
    private static class CreateNewLoadoutButton extends Button {

        private final int index;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BOOK)

                    .name("&a&lLoad/Edit")
                    .lore("&7Click to edit this kit.")
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            Kit kit = profile.getKitEditor().getSelectedKit();
            player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);

            if (profile.getKitEditor().getSelectedKit() == null) {
                return;
            }

            KitInventory kitLoadout = new KitInventory("Kit " + (index + 1));

            if (kit.getKitInventory() != null) {
                if (kit.getKitInventory().getArmor() != null) {
                    kitLoadout.setArmor(kit.getKitInventory().getArmor());
                }

                if (kit.getKitInventory().getContents() != null) {
                    kitLoadout.setContents(kit.getKitInventory().getContents());
                }
            }

            profile.getStatisticsData().get(kit).replaceKit(index, kitLoadout);
            profile.getKitEditor().setSelectedKitInventory(kitLoadout);

            KitInventory kitt = profile.getStatisticsData().get(profile.getKitEditor().getSelectedKit()).getLoadout(index);

            if (kitt == null) {
                kitt = new KitInventory("Kit " + (index + 1));
                kitt.setArmor(profile.getKitEditor().getSelectedKit().getKitInventory().getArmor());
                kitt.setContents(profile.getKitEditor().getSelectedKit().getKitInventory().getContents());
                profile.getStatisticsData().get(profile.getKitEditor().getSelectedKit()).replaceKit(index, kitt);
            }
            profile.getKitEditor().getSelectedKitInventory().setCustomName(CC.translate("&4&lKit " + (index + 1)));
            profile.getKitEditor().setSelectedKitInventory(kitt);
            profile.save();

            new KitEditorMenu(kit).openMenu(player);
        }
    }

    @AllArgsConstructor
    private static class RenameKitButton extends Button {

        private final KitInventory kitInventory;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.SIGN)
                    .name("&e&lRename")
                    .lore("&7Click to rename this kit.")
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);
            Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);

            profile.getKitEditor().setActive(true);
            profile.getKitEditor().setRename(true);
            profile.getKitEditor().setSelectedKitInventory(kitInventory);

            player.closeInventory();
            player.sendMessage(CC.translate("&eRenaming " + kitInventory.getCustomName() + "&e, Enter the new name of your kit (Color codes also applicable)"));
        }

    }

    @AllArgsConstructor
    private static class LoadKitButton extends Button {

        private final int index;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BOOK)

                    .name("&a&lLoad/Edit")
                    .lore("&7Click to edit this kit.")
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);

            if (profile.getKitEditor().getSelectedKit() == null) {
                return;
            }

            KitInventory kit = profile.getStatisticsData().get(profile.getKitEditor().getSelectedKit()).getLoadout(index);

            if (kit == null) {
                kit = new KitInventory("Kit " + (index + 1));
                kit.setArmor(profile.getKitEditor().getSelectedKit().getKitInventory().getArmor());
                kit.setContents(profile.getKitEditor().getSelectedKit().getKitInventory().getContents());
                profile.getStatisticsData().get(profile.getKitEditor().getSelectedKit()).replaceKit(index, kit);
            }

            profile.getKitEditor().setSelectedKitInventory(kit);
            profile.getKitEditor().getSelectedKitInventory().setCustomName(CC.translate("&7&lKit " + (index + 1)));
            profile.save();
            Kit kitt = profile.getKitEditor().getSelectedKit();
            new KitEditorMenu(kitt).openMenu(player);
        }
    }

    @AllArgsConstructor
    private static class KitDisplayButton extends Button {
        private final KitInventory kitInventory;
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BOOK)
                    .name(kitInventory.getCustomName())
                    .build();
        }
    }
}