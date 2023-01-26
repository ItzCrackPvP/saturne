package me.cprox.practice.util;

import me.cprox.practice.Practice;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.AsyncCatcher;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerUtil {

  public static Player getLastAttacker(Player victim) {
      if (victim.hasMetadata("lastAttacker")) {
          return Bukkit.getPlayer((UUID) victim.getMetadata("lastAttacker").get(0).value());
      }
      return null;
  }

    public static void reset(Player player) {
        reset(player, true);
    }

    public static void reset(Player player, boolean resetHeldSlot) {
        AsyncCatcher.enabled = false;
        player.getActivePotionEffects().clear();
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setExp(0f);
        player.setFireTicks(0);
        player.setMaximumNoDamageTicks(20);
        player.setSaturation(20);
        player.setFlySpeed(0.2F);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setWalkSpeed(0.2F);
        player.setSprinting(true);
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setContents(new ItemStack[36]);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

        if (resetHeldSlot) {
            player.getInventory().setHeldItemSlot(0);
        }

        player.updateInventory();
    }

    public static int getPing(Player player) {
        return (player == null) ? 0 : ((((CraftPlayer)player).getHandle() != null) ? (((CraftPlayer)player).getHandle()).ping : 0);
    }

    public static void spectator(Player player) {
        reset(player);

        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(0.2F);
        player.updateInventory();
    }

    public static void denyMovement(Player player) {
        player.setWalkSpeed(0.0F);
        player.setFlySpeed(0.0F);
        player.setFoodLevel(0);
        player.setSprinting(false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 200));
    }

    public static void allowMovement(Player player) {
        player.setWalkSpeed(0.2F);
        player.setFlySpeed(0.0001F);
        player.setFoodLevel(20);
        player.setSprinting(true);
        player.removePotionEffect(PotionEffectType.JUMP);
    }

    public static List<Player> convertUUIDListToPlayerList(List<UUID> list) {
        return list.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static CraftEntity getLastDamager(final Player p) {
        final EntityLiving lastAttacker = ((CraftPlayer) p).getHandle().lastDamager;
        return (lastAttacker == null) ? null : lastAttacker.getBukkitEntity();
    }

    public static Player getPlayer(String name) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return Bukkit.getPlayer(name);
    }
}