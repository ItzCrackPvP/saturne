package me.cprox.practice.menu.settings;

import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MatchmakingMenu extends Menu {
    @Override
    public String getTitle(final Player player) {
        return "&7&lMatchmaking Settings";
    }

    @Override
    public Map<Integer, Button> getButtons(final Player player) {
        final Map<Integer, Button> button = new HashMap<>();
        button.put(0, new RemoveBottleButton());
        button.put(1, new RemoveBowlButton());
        button.put(2, new PingFactorButton());
        button.put(3, new MapSelectorButton());

        return button;
    }

    private static class RemoveBottleButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.GLASS_BOTTLE)
                    .name("&7&lRemove Bottle: " + (profile.getSettings().isDropbottles() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            profile.getSettings().setDropbottles(!profile.getSettings().isDropbottles());
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class RemoveBowlButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.BOWL)
                    .name("&7&lRemove Bowls: " + (profile.getSettings().isClearBowls() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            profile.getSettings().setClearBowls(!profile.getSettings().isClearBowls());
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class PingFactorButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.STICK)
                    .name("&7&lPing Factor: &f" + profile.getPingFactor())
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            if (clickType.isLeftClick()) {
                if (profile.getPingFactor() == 0) {
                    profile.setPingFactor(50);
                } else if (profile.getPingFactor() == 50) {
                    profile.setPingFactor(75);
                } else if (profile.getPingFactor() == 75) {
                    profile.setPingFactor(100);
                } else if (profile.getPingFactor() == 100) {
                    profile.setPingFactor(125);
                } else if (profile.getPingFactor() == 125) {
                    profile.setPingFactor(150);
                } else if (profile.getPingFactor() == 150) {
                    profile.setPingFactor(200);
                } else if (profile.getPingFactor() == 200) {
                    profile.setPingFactor(250);
                } else if (profile.getPingFactor() == 250) {
                    profile.setPingFactor(300);
                } else {
                    profile.setPingFactor(0);
                }
            }

            if (clickType.isRightClick()) {
                if (profile.getPingFactor() == 300) {
                    profile.setPingFactor(250);
                } else if (profile.getPingFactor() == 250) {
                    profile.setPingFactor(200);
                } else if (profile.getPingFactor() == 200) {
                    profile.setPingFactor(150);
                } else if (profile.getPingFactor() == 150) {
                    profile.setPingFactor(125);
                } else if (profile.getPingFactor() == 125) {
                    profile.setPingFactor(100);
                } else if (profile.getPingFactor() == 100) {
                    profile.setPingFactor(75);
                } else if (profile.getPingFactor() == 75) {
                    profile.setPingFactor(50);
                } else if (profile.getPingFactor() == 50) {
                    profile.setPingFactor(0);
                } else {
                    profile.setPingFactor(0);
                }
            }
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class MapSelectorButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.MAP)
                    .name("&7&lMap Selector: " + (profile.getSettings().isUsingMapSelector() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            profile.getSettings().setUsingMapSelector(!profile.getSettings().isUsingMapSelector());
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }
}