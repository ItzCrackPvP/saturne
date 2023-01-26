package me.cprox.practice.util.menu.button;

import lombok.AllArgsConstructor;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@AllArgsConstructor
public class BackButton extends Button {

    private Menu back;

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.REDSTONE)
                .name("&6&lBack")
                .lore(Arrays.asList(
                        "&7&oClick to return to",
                        "&7&othe previous menu."))
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        back.openMenu(player);
        player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);
    }

    @Override
    public boolean shouldUpdate(final Player player, final ClickType clickType) {
        return true;
    }
}
