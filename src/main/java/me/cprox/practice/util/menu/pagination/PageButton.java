package me.cprox.practice.util.menu.pagination;

import lombok.AllArgsConstructor;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@AllArgsConstructor
public class PageButton extends Button {

    private int mod;
    private PaginatedMenu menu;

    @Override
    public ItemStack getButtonItem(Player player) {
        if (this.mod > 0) {
            if (hasNext(player)) {
                return new ItemBuilder(Material.PAPER)
                        .name("&c&lNext Page")
                        .lore(Arrays.asList(
                                "&7Click here to switch to",
                                "&7the next page."
                        ))
                        .build();
            } else {
                return new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .durability(15)
                        .name("&f")
                        .build();
            }
        } else {
            if (hasPrevious(player)) {
                return new ItemBuilder(Material.PAPER)
                        .name("&c&lPrevious Page")
                        .lore(Arrays.asList(
                                "&7Click here to switch to",
                                "&7the previous page."
                        ))
                        .build();
            } else {
                return new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .durability(15)
                        .name("&f")
                        .build();
            }
        }
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (this.mod > 0) {
            if (hasNext(player)) {
                this.menu.modPage(player, this.mod);
            }
        } else {
            if (hasPrevious(player)) {
                this.menu.modPage(player, this.mod);
            }
        }
    }

    private boolean hasNext(Player player) {
        int pg = this.menu.getPage() + this.mod;
        return this.menu.getPages(player) >= pg;
    }

    private boolean hasPrevious(Player player) {
        int pg = this.menu.getPage() + this.mod;
        return pg > 0;
    }

}