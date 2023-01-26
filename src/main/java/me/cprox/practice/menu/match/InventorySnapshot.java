package me.cprox.practice.menu.match;

import lombok.Getter;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.util.SkullCreator;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.*;
import me.cprox.practice.util.menu.InventoryUI;
import me.cprox.practice.util.menu.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

@Getter
public class InventorySnapshot {

    private final InventoryUI inventoryUI;
    private final ItemStack[] originalInventory;
    private final ItemStack[] originalArmor;

    @Getter
    private final UUID snapshotId = UUID.randomUUID();

    public InventorySnapshot(Player player, Match match) {
        this.inventoryUI = new InventoryUI(CC.WHITE + player.getName() + CC.GRAY + "'s inventory", true, 6);
        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();

        this.originalInventory = contents;
        this.originalArmor = armor;

        List<String> potionEffectStrings = new ArrayList<>();

        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (player.getActivePotionEffects() == null) {
                potionEffectStrings.add(CC.translate("&cNo potion effects."));
            } else {
                String name = PotionUtil.getName(effect.getType()) + " " + (effect.getAmplifier() + 1);
                String duration = " (" + TimeUtil.millisToTimer((effect.getDuration() / 20) * 1000L) + ")";

                potionEffectStrings.add(CC.translate("&f" + name + "&7" + duration));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.inventoryUI.setItem(i + 27, new InventoryUI.EmptyClickableItem(contents[i]));
            this.inventoryUI.setItem(i + 18, new InventoryUI.EmptyClickableItem(contents[i + 27]));
            this.inventoryUI.setItem(i + 9, new InventoryUI.EmptyClickableItem(contents[i + 18]));
            this.inventoryUI.setItem(i, new InventoryUI.EmptyClickableItem(contents[i + 9]));
        }

        final TeamPlayer teamPlayer = match.getTeamPlayer(player.getPlayer());
        if (teamPlayer.isAlive()) {

            this.inventoryUI.setItem(47, new InventoryUI.EmptyClickableItem((
                    new ItemBuilder(SkullCreator.itemFromUuid(player.getUniqueId()))
                            .durability(3)
                            .amount((int) player.getHealth())
                            .name("&7Health: &4" + (int) player.getHealth() + " / 20 ❤")
                            .build())));

            this.inventoryUI.setItem(48, new InventoryUI.EmptyClickableItem(

                    new ItemBuilder(Material.RAW_BEEF)
                            .amount(player.getFoodLevel())
                            .name("&7Hunger: &4" + player.getFoodLevel() + " / 20")
                            .build()));
        } else {

            this.inventoryUI.setItem(47, new InventoryUI.EmptyClickableItem(
                    new ItemBuilder(Material.SKULL_ITEM)
                            .amount(0)
                            .name("&7Health: &cDead")
                            .build()));

            this.inventoryUI.setItem(48, new InventoryUI.EmptyClickableItem(

                    new ItemBuilder(Material.COOKED_BEEF)
                            .amount(0)
                            .name("&7Hunger: &cDead")
                            .build()));
        }

        this.inventoryUI.setItem(49, new InventoryUI.EmptyClickableItem(
                ItemUtil.reloreItem(
                        ItemUtil.createItem(Material.BREWING_STAND_ITEM, CC.translate("&7&lPotion Effects"), 2)
                        , potionEffectStrings.toArray(new String[]{}))));


        for (ItemStack itemStack : player.getInventory().getContents()) {
            int potCount = (int) Arrays.stream(contents).filter(Objects::nonNull).map(ItemStack::getDurability).filter(d -> d == 16421).count();
            int soupCount = (int) Arrays.stream(contents).filter(Objects::nonNull).map(ItemStack::getDurability).filter(d -> d == 0).count();
            if (itemStack != null) {
                if (itemStack.getType() == Material.POTION && itemStack.getDurability() == 16421 || match.getKit().getName().contains("NoDebuff") || match.getKit().getName().contains("Debuff")) {
                    this.inventoryUI.setItem(50, new InventoryUI.EmptyClickableItem(
                            new ItemBuilder(Material.POTION)
                                    .durability(16421)
                                    .amount(potCount == 0 ? 1 : potCount)
                                    .name("&7&lHealth Potions: &f" + potCount)
                                    .lore(Arrays.asList(
                                            "&fMissed: &7" + teamPlayer.getPotionsMissed(),
                                            "&fWasted: &7" + teamPlayer.getWasted() + "❤",
                                            "&fAccuracy: &7" + teamPlayer.getPotionAccuracy() + "%",
                                            "&fThrown: &7" + teamPlayer.getPotionsThrown()))
                                    .build()));
                    break;
                }

                if (itemStack.getType() == Material.MUSHROOM_SOUP) {
                    this.inventoryUI.setItem(50, new InventoryUI.EmptyClickableItem(
                            new ItemBuilder(Material.MUSHROOM_SOUP)
                                    .durability(0)
                                    .amount(soupCount == 0 ? 1 : soupCount)
                                    .name("&7&lSoups:" + soupCount)
                                    .lore("&fUsed: &7" + teamPlayer.getUsedSoups())
                                    .build()));
                    break;
                }

                if (itemStack.getType() == Material.BOW || itemStack.getType() == Material.FISHING_ROD) {
                    if (itemStack.getType() == Material.BOW) {
                        this.inventoryUI.setItem(50, new InventoryUI.EmptyClickableItem(
                                new ItemBuilder(Material.BOW)
                                        .name("&7&lProjectile Statistics")
                                        .lore("&7&lBow Statistics")
                                        .lore(" &fMissed: &80")
                                        .lore(" &fLongest Combo: &80")
                                        .lore(" &fAccuracy: &8100%")
                                        .build()));
                    } else if (itemStack.getType() == Material.FISHING_ROD) {
                        this.inventoryUI.setItem(50, new InventoryUI.EmptyClickableItem(
                                new ItemBuilder(Material.BOW)
                                        .name("&7&lProjectile Statistics")
                                        .lore("&7&lRod Statistics")
                                        .lore(" &fMissed: &80")
                                        .lore(" &fLongest Combo: &80")
                                        .lore(" &fAccuracy: &8100%")
                                        .build()));
                    } else if (itemStack.getType() == Material.BOW && itemStack.getType() == Material.FISHING_ROD) {
                        this.inventoryUI.setItem(50, new InventoryUI.EmptyClickableItem(
                                new ItemBuilder(Material.BOW)
                                        .name("&7&lProjectile Statistics")
                                        .lore("&7&lBow Statistics")
                                        .lore(" &fMissed: &80")
                                        .lore(" &fLongest Combo: &80")
                                        .lore(" &fAccuracy: &8100%")
                                        .lore("&7&lRod Statistics")
                                        .lore(" &fMissed: &80")
                                        .lore(" &fLongest Combo: &80")
                                        .lore(" &fAccuracy: &8100%")
                                        .build()));
                    }
                } else {
                    this.inventoryUI.setItem(50, new InventoryUI.EmptyClickableItem(

                            new ItemBuilder(Material.HAY_BLOCK)
                                    .name(" ")
                                    .build()));
                }
            }
        }

        this.inventoryUI.setItem(51, new InventoryUI.EmptyClickableItem(

                new ItemBuilder(Material.DIAMOND_SWORD)
                        .name("&7&lMatch Statistics")
                        .lore(Arrays.asList(
                                "&fLongest Combo: &8" + teamPlayer.getLongestCombo(),
                                "&fTotal Hits: &8" + teamPlayer.getHits(),
                                "",
                                "&7&lDamage",
                                " &fCritical Hits: &8" + teamPlayer.getCriticalHits(),
                                " &fBlocked Hits: &8" + teamPlayer.getBlockedHits(),
                                "",
                                "&7&lPlayer",
                                " &fRegen: &8" + teamPlayer.getRegen() + "&4❤",
                                " &fW-Taps: &80"
                        ))
                        .build()));

        for (int i = 36; i < 40; i++) {
            this.inventoryUI.setItem(i, new InventoryUI.EmptyClickableItem(armor[39 - i]));
        }

        if (match.isSoloMatch()) {
            TeamPlayer opponent = match.getOpponentTeamPlayer(player);

            for (int j = 0; j < 2; ++j) {
                this.inventoryUI.setItem((j == 0) ? 53 : 45, new InventoryUI.AbstractClickableItem((
                        new ItemBuilder(Material.PAPER)
                                .name("&7View &f" + opponent.getUsername() + "&7's Inventory")
                                .lore("&7&oClick to switch view")
                                .build())) {

                    @Override
                    public void onClick(InventoryClickEvent event) {
                        TeamPlayer oTeamPlayer = match.getTeamPlayer(opponent.getPlayer());
                        Player clicker = (Player) event.getWhoClicked();
                        clicker.performCommand("_ " + match.getSnapshot(oTeamPlayer.getUuid()).getSnapshotId());
                    }
                });
            }
        }
    }
}