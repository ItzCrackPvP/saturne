package me.cprox.practice.profile.meta;

import me.cprox.practice.kit.KitInventory;
import me.cprox.practice.profile.enums.HotbarType;
import me.cprox.practice.profile.hotbar.Hotbar;
import me.cprox.practice.util.chat.CC;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Getter
@Setter
public class StatisticsData {
    @Getter
    @Setter
    private KitInventory[] loadouts = new KitInventory[4];
    @Getter
    @Setter
    private int tournamentWins = 0;
    @Getter
    @Setter
    private int tournamentLost = 0;
    @Getter
    @Setter
    private int unrankedLost = 0;
    @Getter
    @Setter
    private int unrankedWon = 0;
    @Getter
    @Setter
    private int rankedLost = 0;
    @Getter
    @Setter
    private int rankedWon = 0;
    @Getter
    @Setter
    private int winStreak = 0;
    @Getter
    @Setter
    private int bestWinSterak = 0;
    private int unrankedElo = 1000;
    private int elo = 1000;

    public void incrementRankedWon() {
        this.rankedWon++;
    }

    public void incrementRankedLost() {
        this.rankedLost++;
    }

    public void incrementUnrankedWon() {
        this.unrankedWon++;
    }

    public void incrementUnrankedLost() {
        this.unrankedLost++;
    }

    public void incrementWinStreak() {
        this.winStreak++;
    }

    public KitInventory getLoadout(int index) {
        return loadouts[index];
    }

    public void replaceKit(int index, KitInventory loadout) {
        loadouts[index] = loadout;
    }

    public void deleteKit(KitInventory loadout) {
        for (int i = 0; i < 4; i++) {
            if (loadouts[i] != null && loadouts[i].equals(loadout)) {
                loadouts[i] = null;
                break;
            }
        }
    }

    public int getKitCount() {
        int i = 0;
        for (KitInventory loadout : this.loadouts) {
            if (loadout != null) {
                i++;
            }
        }
        return i;
    }

    public Map<Integer, ItemStack> getKitItems() {
        final HashMap<Integer, ItemStack> toReturn = new HashMap<>();

        List<KitInventory> reversedLoadouts = new ArrayList<>(Arrays.asList(this.loadouts));

        Collections.reverse(reversedLoadouts);

        for (int slot = 0; slot < this.loadouts.length; slot++) {
            for (KitInventory kitInventory : reversedLoadouts) {
                if (kitInventory == null)
                    continue;
                ItemStack itemStack1 = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta itemMeta = itemStack1.getItemMeta();
                itemMeta.setDisplayName(CC.translate(kitInventory.getCustomName() + " &7(Right-Click)"));
                itemMeta.setLore(Arrays.asList(ChatColor.GRAY + "Right click this book", ChatColor.GRAY + "to receive the kit."));
                itemStack1.setItemMeta(itemMeta);
                if (!toReturn.containsValue(itemStack1))
                    toReturn.put(slot, itemStack1);
            }
        }
        if (toReturn.size() != 0) {
            toReturn.put(8, Hotbar.getItems().get(HotbarType.DEFAULT_KIT));
        }

        return toReturn;
    }

    public void giveBooks(Player player) {
        List<KitInventory> loadouts = new ArrayList<>();
        for (KitInventory loadout : this.loadouts) {
            if (loadout != null) {
                loadouts.add(loadout);
            }
        }
        if (loadouts.isEmpty()) {
            player.getInventory().setItem(0, Hotbar.getItems().get(HotbarType.DEFAULT_KIT));
        } else {
            player.getInventory().setItem(8, Hotbar.getItems().get(HotbarType.DEFAULT_KIT));
            for (KitInventory loadout : loadouts) {
                ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(CC.translate(loadout.getCustomName() + " &7(Right-Click)"));
                itemMeta.setLore(Arrays.asList(ChatColor.GRAY + "Right click this book", ChatColor.GRAY + "to receive the kit."));
                itemStack.setItemMeta(itemMeta);
                player.getInventory().addItem(itemStack);
            }
        }
        player.updateInventory();
    }
}