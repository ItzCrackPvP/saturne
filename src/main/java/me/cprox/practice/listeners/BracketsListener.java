package me.cprox.practice.listeners;

import me.cprox.practice.Practice;
import me.cprox.practice.events.brackets.Brackets;
import me.cprox.practice.events.brackets.BracketsState;
import me.cprox.practice.kit.KitInventory;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.HotbarType;
import me.cprox.practice.profile.hotbar.Hotbar;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.Cooldown;
import me.cprox.practice.util.external.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class BracketsListener implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onDeath(EntityDeathEvent event) {
			if (event.getEntity() instanceof Player) {

				Player player = (Player) event.getEntity();

				Profile profile = Profile.getProfiles().get(event.getEntity().getUniqueId());
				if (profile.isInBrackets()) {
				profile.getBrackets().handleDeath(player);
				player.teleport(Practice.get().getBracketsManager().getBracketsSpectator());
				event.getDrops().clear();
				PlayerUtil.reset(player);

			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Player attacker;

		if (event.getDamager() instanceof Player) {
			attacker = (Player) event.getDamager();
		} else if (event.getDamager() instanceof Projectile) {
			if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
				attacker = (Player) ((Projectile) event.getDamager()).getShooter();
			} else {
				event.setCancelled(true);
				return;
			}
		} else {
			event.setCancelled(true);
			return;
		}

		if (attacker != null && event.getEntity() instanceof Player) {
			Player damaged = (Player) event.getEntity();
			Profile damagedProfile = Profile.getByUuid(damaged.getUniqueId());
			Profile attackerProfile = Profile.getByUuid(attacker.getUniqueId());

			if (damagedProfile.isInBrackets() && attackerProfile.isInBrackets()) {
				Brackets brackets = damagedProfile.getBrackets();

				if (!brackets.isFighting() || !brackets.isFighting(damaged.getUniqueId()) ||
						!brackets.isFighting(attacker.getUniqueId())) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

		if (profile.isInBrackets()) {
			if (!profile.getBrackets().isFighting(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
				return;
			}
		}

		if (profile.isInBrackets() && profile.getBrackets().isFighting()) {
			if (event.getItemDrop().getItemStack().getType() == Material.GLASS_BOTTLE) {
				event.getItemDrop().remove();
				return;
			}

			profile.getBrackets().getEntities().add(event.getItemDrop());
		}
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
		if (profile.isInBrackets()) {
			if (!profile.getBrackets().isFighting(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerFallDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());
			if (profile.isInBrackets()) {
				if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

		if (profile.isInBrackets()) {
			profile.getBrackets().handleLeave(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getItem() != null && event.getAction().name().contains("RIGHT")) {
			Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

			if (profile.isInBrackets()) {
				if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {
					if (event.getItem().equals(Hotbar.getItems().get(HotbarType.DEFAULT_KIT))) {
						KitInventory kitInventory= profile.getBrackets().getKit().getKitInventory();
						event.getPlayer().getInventory().setArmorContents(kitInventory.getArmor());
						event.getPlayer().getInventory().setContents(kitInventory.getContents());
						event.getPlayer().updateInventory();
						event.setCancelled(true);
						return;
					}
				}

				if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {
					String displayName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());

					if (displayName.startsWith("Kit: ")) {
						String kitName = displayName.replace("Kit: ", "");

						for ( KitInventory kitInventory : profile.getStatisticsData().get(profile.getBrackets().getKit()).getLoadouts()) {
							if (kitInventory != null && ChatColor.stripColor(kitInventory.getCustomName()).equals(kitName)) {
								event.getPlayer().getInventory().setArmorContents(kitInventory.getArmor());
								event.getPlayer().getInventory().setContents(kitInventory.getContents());
								event.getPlayer().updateInventory();
								event.setCancelled(true);
								return;
							}
						}
					}
				}

				Player player = event.getPlayer();
				if (((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.RIGHT_CLICK_AIR)) &&
						(player.getItemInHand().getType() == Material.MUSHROOM_SOUP))
				{
					int health = (int)player.getHealth();
					if ((health == 20)) {
						player.getItemInHand().setType(Material.MUSHROOM_SOUP);
					} else if (health >= 13) {
						player.setHealth(20.0D);
						player.getItemInHand().setType(Material.BOWL);
					} else {
						player.setHealth(health + 7);
						player.getItemInHand().setType(Material.BOWL);
					}
				}

				if (event.getItem().getType() == Material.ENDER_PEARL) {
					if (profile.getBrackets().getState().equals(BracketsState.ROUND_STARTING)) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
}
