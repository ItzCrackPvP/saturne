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

public class DeathEffectsMenu extends Menu {
    @Override
    public String getTitle(final Player player) {
        return "&7&lDeath Effects";
    }

    @Override
    public Map<Integer, Button> getButtons(final Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(0, new ClearInventoryButton());
        buttons.put(1, new FlyButton());
        buttons.put(2, new LightingButton());
        buttons.put(3, new FireWorkButton());
        buttons.put(4, new FlameButton());
        buttons.put(5, new ExplosionButton());
        buttons.put(6, new BloodButton());

        return buttons;
    }

    private static class ClearInventoryButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.ITEM_FRAME)
                    .name("&7&lClear Inventory: " + (profile.getSettings().isClearinventory() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            profile.getSettings().setClearinventory(!profile.getSettings().isClearinventory());
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class FlyButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.FEATHER)
                    .name("&7&lFly: " + (profile.getSettings().isFlying() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            profile.getSettings().setFlying(!profile.getSettings().isFlying());
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class LightingButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.BEACON)
                    .name("&7&lLighting: " + (profile.getSettings().isLightning() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            profile.getSettings().setLightning(!profile.getSettings().isLightning());
            profile.getSettings().setFireworkeffect(false);
            profile.getSettings().setFlameeffect(false);
            profile.getSettings().setExplosioneffect(false);
            profile.getSettings().setBloodeffect(false);
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class FireWorkButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.FIREWORK_CHARGE)
                    .name("&7&lFirework: " + (profile.getSettings().isFireworkeffect() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            profile.getSettings().setFireworkeffect(!profile.getSettings().isFireworkeffect());
            profile.getSettings().setLightning(false);
            profile.getSettings().setFlameeffect(false);
            profile.getSettings().setExplosioneffect(false);
            profile.getSettings().setBloodeffect(false);
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class FlameButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            return new ItemBuilder(Material.BLAZE_POWDER)
                    .name("&7&lFlame: " + (profile.getSettings().isFlameeffect() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.getSettings().setFlameeffect(!profile.getSettings().isFlameeffect());
            profile.getSettings().setLightning(false);
            profile.getSettings().setFireworkeffect(false);
            profile.getSettings().setExplosioneffect(false);
            profile.getSettings().setBloodeffect(false);
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class ExplosionButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            return new ItemBuilder(Material.TNT)
                    .name("&7&lTnT: " + (profile.getSettings().isExplosioneffect() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.getSettings().setExplosioneffect(!profile.getSettings().isExplosioneffect());
            profile.getSettings().setLightning(false);
            profile.getSettings().setFireworkeffect(false);
            profile.getSettings().setFlameeffect(false);
            profile.getSettings().setBloodeffect(false);
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class BloodButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            return new ItemBuilder(Material.REDSTONE)
                    .name("&7&lBlood: " + (profile.getSettings().isBloodeffect() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.getSettings().setBloodeffect(!profile.getSettings().isBloodeffect());
            profile.getSettings().setLightning(false);
            profile.getSettings().setFireworkeffect(false);
            profile.getSettings().setFlameeffect(false);
            profile.getSettings().setExplosioneffect(false);
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