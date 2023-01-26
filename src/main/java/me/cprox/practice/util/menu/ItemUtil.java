package me.cprox.practice.util.menu;

import lombok.RequiredArgsConstructor;
import me.cprox.practice.util.external.BukkitReflection;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class ItemUtil {

    private ItemUtil() {
        throw new RuntimeException("Cannot instantiate a utility class.");
    }

    public static String formatMaterial(Material material) {
        String name = material.toString();
        name = name.replace('_', ' ');
        String result = "" + name.charAt(0);
        for (int i = 1; i < name.length(); i++) {
            if (name.charAt(i - 1) == ' ') {
                result += name.charAt(i);
            } else {
                result += Character.toLowerCase(name.charAt(i));
            }
        }
        return result;
    }

    public static ItemStack enchantItem(ItemStack itemStack, ItemEnchant... enchantments) {
        Arrays.asList(enchantments).forEach(enchantment -> itemStack.addUnsafeEnchantment(enchantment.enchantment, enchantment.level));
        return itemStack;
    }


    public static String getName(ItemStack item) {
        if (item.getDurability() != 0) {
            String reflectedName = BukkitReflection.getItemStackName(item);

            if (reflectedName != null) {
                if (reflectedName.contains(".")) {
                    reflectedName = WordUtils.capitalize(item.getType().toString().toLowerCase().replace("_", " "));
                }

                return reflectedName;
            }
        }

        String string = item.getType().toString().replace("_", " ");
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;

        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') {
                found = false;
            }
        }

        return String.valueOf(chars);
    }

    public static ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack createItem(Material material, String name, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack createItem(Material material, String name, int amount, short damage) {
        ItemStack item = new ItemStack(material, amount, damage);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack hideEnchants(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack setUnbreakable(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.spigot().setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack renameItem(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack reloreItem(ItemStack item, String... lores) {
        return reloreItem(ReloreType.OVERWRITE, item, lores);
    }

    public static ItemStack reloreItem(ReloreType type, ItemStack item, String... lores) {
        ItemMeta meta = item.getItemMeta();

        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new LinkedList<>();
        }

        switch (type) {
            case APPEND:
                lore.addAll(Arrays.asList(lores));
                meta.setLore(lore);
                break;
            case PREPEND:
                List<String> nLore = new LinkedList<>(Arrays.asList(lores));
                nLore.addAll(lore);
                meta.setLore(nLore);
                break;
            case OVERWRITE:
                meta.setLore(Arrays.asList(lores));
                break;
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack addItemFlag(ItemStack item, ItemFlag flag) {
        ItemMeta meta = item.getItemMeta();

        meta.addItemFlags(flag);
        item.setItemMeta(meta);

        return item;
    }

    public enum ReloreType {
        OVERWRITE,
        PREPEND,
        APPEND
    }

    @RequiredArgsConstructor
    public static class ItemEnchant {
        private final Enchantment enchantment;
        private final int level;
    }

}