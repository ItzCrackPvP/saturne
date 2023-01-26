package me.cprox.practice.profile.hotbar;

import org.bukkit.inventory.ItemStack;

public class HotbarEntry {
    private final ItemStack itemStack;
    private final int slot;

    public HotbarEntry(ItemStack itemStack, int slot) {
        this.itemStack = itemStack;
        this.slot = slot;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public int getSlot() {
        return this.slot;
    }
}