package me.cprox.practice.listeners;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.arena.impl.StandaloneArena;
import me.cprox.practice.kit.KitInventory;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.impl.solo.SoloBattleRushMatch;
import me.cprox.practice.match.impl.solo.SoloBedFightMatch;
import me.cprox.practice.match.impl.solo.SoloBridgeMatch;
import me.cprox.practice.match.impl.solo.SoloPearlFightMatch;
import me.cprox.practice.match.task.MatchRespawnTask;
import me.cprox.practice.match.team.Team;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.HotbarType;
import me.cprox.practice.profile.enums.MatchState;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.profile.hotbar.Hotbar;
import me.cprox.practice.util.InventoryUtil;
import me.cprox.practice.util.BlockUtil;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.timer.event.impl.BridgeArrowTimer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEnderPearl;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MatchListener implements Listener {

    private final Practice plugin = Practice.get();

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlaceEvent(final BlockPlaceEvent e) {
        final Profile profile = Profile.getByUuid(e.getPlayer().getUniqueId());
        if (profile.isInFight()) {
            final Match match = profile.getMatch();
            if (match.isStarting()) {
                e.setCancelled(true);
                return;
            }
            if (match.isEnding()) {
                e.setCancelled(true);
                return;
            }

            if (match.getKit().getGameRules().isPearlFight()) {
                Player player = e.getPlayer();
                Material block = e.getBlock().getType();
                TeamPlayer self = match.getTeamPlayer(e.getPlayer());
                if (block == Material.WOOL) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            e.getBlock().setType(Material.AIR);
                            if (player.getGameMode() == GameMode.SURVIVAL) {
                                if (self == match.getTeamPlayerA()) {
                                    ItemStack redWool = (new ItemBuilder(Material.WOOL).durability(14).build());

                                    if (player.getInventory().contains(redWool)) {
                                        if (redWool.getAmount() == 16) {
                                            e.setCancelled(true);
                                        } else {
                                            player.getInventory().addItem(new ItemBuilder(Material.WOOL).durability(14).amount(1).build());
                                        }
                                    }
                                } else {
                                    ItemStack blueWool = (new ItemBuilder(Material.WOOL).durability(11).build());

                                    if (player.getInventory().contains(blueWool)) {
                                        if (blueWool.getAmount() == 16) {
                                            e.setCancelled(true);
                                        } else {
                                            player.getInventory().addItem(new ItemBuilder(Material.WOOL).durability(11).amount(1).build());
                                        }
                                    }
                                }
                            }
                        }
                    }.runTaskLater(plugin, 160);
                }
            }
            if (match.getKit().getGameRules().isBattleRush()) {
                Player player = e.getPlayer();
                Material block = e.getBlock().getType();
                TeamPlayer self = match.getTeamPlayer(e.getPlayer());
                if (block == Material.WOOL) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            e.getBlock().setType(Material.AIR);
                            if (player.getGameMode() == GameMode.SURVIVAL) {
                                if (self == match.getTeamPlayerA()) {
                                    ItemStack redWool = (new ItemBuilder(Material.WOOL).durability(14).build());
                                    if (player.getInventory().contains(redWool)) {
                                        if (!(redWool.getAmount() == 64)) {
                                            player.getInventory().addItem(new ItemBuilder(Material.WOOL).durability(14).amount(1).build());
                                        }
                                    }
                                } else {
                                    ItemStack blueWool = (new ItemBuilder(Material.WOOL).durability(11).build());
                                    if (player.getInventory().contains(blueWool)) {
                                        if (!(blueWool.getAmount() == 64)) {
                                            player.getInventory().addItem(new ItemBuilder(Material.WOOL).durability(11).amount(1).build());
                                        }
                                    }
                                }
                            }
                        }
                    }.runTaskLater(plugin, 160);
                }
            }

            if (!profile.getMatch().isHCFMatch()) {
                if (match.getKit().getGameRules().isBuild() && profile.getMatch().isFighting()) {
                    if (match.getKit().getGameRules().isSpleef()) {
                        e.setCancelled(true);
                        return;
                    }
                    final Arena arena = match.getArena();
                    final int y = (int) e.getBlockPlaced().getLocation().getY();
                    if (y > arena.getMaxBuildHeight()) {
                        e.getPlayer().sendMessage(Practice.get().getMessagesConfig().getString("ARENA.REACHED"));
                        e.setCancelled(true);
                        return;
                    }

                    match.getPlacedBlocks().add(e.getBlock().getLocation());
                } else {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        } else {
            if (!profile.isBuilder()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Profile profile = Profile.getByUuid(e.getPlayer().getUniqueId());
        if (profile.isInFight()) {
            Match match = profile.getMatch();
            if (match.getKit().getGameRules().isBuild() && match.isFighting()) {
                if (match.getKit().getGameRules().isSpleef()) {
                    if (e.getBlock().getType() == Material.SNOW_BLOCK || e.getBlock().getType() == Material.SNOW) {
                        match.getChangedBlocks().add(e.getBlock().getState());
                        e.getBlock().setType(Material.AIR);
                        player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 4));
                        player.updateInventory();
                    } else {
                        e.setCancelled(true);
                    }
                } else if (match.getPlacedBlocks().remove(e.getBlock().getLocation()) && !match.getKit().getGameRules().isTimed()) {
                    e.getBlock().setType(Material.AIR);
                    e.getPlayer().getInventory().addItem(new ItemStack(e.getBlock().getType(), 1));
                    e.getPlayer().updateInventory();
                } else if (!match.getKit().getGameRules().isTimed()) {
                    e.setCancelled(true);
                }

                if (match.getKit().getGameRules().isBedFight()) {
                    if (e.getBlock().getType().equals(Material.BED_BLOCK)) {
                        SoloBedFightMatch bedFightMatch = (SoloBedFightMatch) profile.getMatch();
                        StandaloneArena bedFightArena = (StandaloneArena) match.getArena();
                        if (bedFightMatch.getRedBed()) {
                            if (e.getBlock().getLocation().equals(bedFightArena.getRedBed())) {
                                if (match.getTeamPlayerA().getPlayer() == player) {
                                    player.sendMessage(CC.translate("&cYou cannot break your own bed!"));
                                } else {
                                    bedFightMatch.handleBed(player);
                                    match.broadcastMessage("");
                                    match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.BED.DESTROYED_RED").replace("<player>", match.getTeamPlayerB().getUsername()));
                                    match.broadcastMessage("");
                                    match.getTeamPlayerA().getPlayer().playSound(match.getTeamPlayerA().getPlayer().getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
                                    match.getTeamPlayerB().getPlayer().playSound(match.getTeamPlayerB().getPlayer().getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
                                }
                            }
                        }
                        if (bedFightMatch.getBlueBed()) {
                            if (e.getBlock().getLocation().equals(bedFightArena.getBlueBed())) {
                                if (match.getTeamPlayerB().getPlayer() == player) {
                                    player.sendMessage(CC.translate("&cYou cannot break your own bed!"));
                                } else {
                                    bedFightMatch.handleBed(player);
                                    match.broadcastMessage("");
                                    match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.BED.DESTROYED_BLUE").replace("<player>", match.getTeamPlayerA().getUsername()));
                                    match.broadcastMessage("");
                                    match.getTeamPlayerA().getPlayer().playSound(match.getTeamPlayerA().getPlayer().getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
                                    match.getTeamPlayerB().getPlayer().playSound(match.getTeamPlayerB().getPlayer().getLocation(), Sound.ENDERDRAGON_GROWL, 1.0F, 1.0F);
                                }
                            }
                        }
                    }
                }
            } else if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE || profile.getState() == ProfileState.SPECTATE_MATCH) {
                e.setCancelled(true);
            }
        } else {
            if (!profile.isBuilder()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketEmptyEvent(final PlayerBucketEmptyEvent e) {
        final Profile profile = Profile.getByUuid(e.getPlayer().getUniqueId());
        if (profile.isInFight()) {
            final Match match = profile.getMatch();
            if (!profile.getMatch().isHCFMatch()) {
                if (match.getKit().getGameRules().isBuild() && profile.getMatch().isFighting()) {
                    final Arena arena = match.getArena();
                    final Block block = e.getBlockClicked().getRelative(e.getBlockFace());
                    final int y = (int) block.getLocation().getY();
                    if (y > arena.getMaxBuildHeight()) {
                        e.getPlayer().sendMessage(Practice.get().getMessagesConfig().getString("ARENA.REACHED"));
                        e.setCancelled(true);
                        return;
                    }
                    match.getPlacedBlocks().add(block.getLocation());
                } else {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockMove(final BlockFromToEvent e) {
        int id = e.getBlock().getTypeId();
        if (id >= 8 && id <= 11) {
            final Block b = e.getToBlock();
            int toId = b.getTypeId();
            if (toId == 0 && BlockUtil.generatesCobble(id, b)) {
                e.setCancelled(true);
            }
        }
        final Location l = e.getToBlock().getLocation();
        final List<UUID> playersIsInArena = new ArrayList<>();
        for (final Entity entity : BlockUtil.getNearbyEntities(l, 50)) {
            if (entity instanceof Player) {
                playersIsInArena.add(((Player) entity).getPlayer().getUniqueId());
            }
        }
        if (playersIsInArena.size() > 0) {
            final Profile profile = Profile.getByUuid(playersIsInArena.get(0));
            if (profile.isInFight()) {
                final Match match = profile.getMatch();
                match.getPlacedBlocks().add(e.getToBlock().getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerPickupItemEvent(final PlayerPickupItemEvent e) {
        final Profile profile = Profile.getByUuid(e.getPlayer().getUniqueId());
        if (profile.isSpectating()) {
            e.setCancelled(true);
            return;
        }
        if (!profile.isInLobby()) {
            if (profile.isInFight()) {
                if (!profile.getMatch().getTeamPlayer(e.getPlayer()).isAlive()) {
                    e.setCancelled(true);
                    return;
                }
                if (e.getItem().getItemStack().getType().name().contains("BOOK")) {
                    e.setCancelled(true);
                    return;
                }
                if (profile.getMatch().isEnding()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDropItemEvent(final PlayerDropItemEvent e) {
        Profile profile = Profile.getByUuid(e.getPlayer().getUniqueId());
        if (profile.isSpectating()) {
            e.setCancelled(true);
        }
        if (profile.isInFight()) {

            if (e.getItemDrop().getItemStack().getType() == Material.BOOK || e.getItemDrop().getItemStack().getType() == Material.ENCHANTED_BOOK) {
                e.setCancelled(true);
                return;
            }
        }
        if (profile.isInFight()) {
            if (e.getItemDrop().getItemStack().getType() == Material.DIAMOND_SWORD) {
                e.getPlayer().sendMessage(Practice.get().getMessagesConfig().getString("MATCH.CANT_DROP_SWORD"));
                e.setCancelled(true);
                return;
            }
        }
        if (e.getItemDrop().getItemStack().getType() == Material.INK_SACK) {
            e.getItemDrop().remove();
            return;
        }
        if (profile.isInSomeSortOfFight()) {
            if (profile.getMatch() != null) {
                profile.getMatch().getEntities().add(e.getItemDrop());
                if (e.getItemDrop().getItemStack().getType() == Material.GLASS_BOTTLE) {
                    e.getItemDrop().setPickupDelay(50000);
                }
            }
        }
    }

    @EventHandler
    public void dropDeath(final ItemSpawnEvent e) {
        new BukkitRunnable() {
            public void run() {
                e.getEntity().remove();
            }
        }.runTaskLater(Practice.get(), 130L);
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e) {
        e.setDeathMessage(null);

        Player player = e.getEntity().getPlayer();
        Profile profile = Profile.getByUuid(e.getEntity().getUniqueId());
        e.getEntity().getPlayer().setNoDamageTicks(20);
        if (profile.isInFight()) {
            e.setDroppedExp(0);
            player.setHealth(20.0D);
            player.setFoodLevel(20);

            if (profile.getMatch().getKit().getGameRules().isBridge()) {
                Practice.get().getServer().getScheduler().runTaskLater(Practice.get(), () -> onPlayerDeathInTheBridge(player), 1L);
                e.getDrops().clear();
                return;
            }

            if (profile.getMatch().getKit().getGameRules().isBedFight()) {
                Practice.get().getServer().getScheduler().runTaskLater(Practice.get(), () -> onPlayerDeathInBedFight(player), 1L);
                e.getDrops().clear();
                return;
            }

            if (PlayerUtil.getLastDamager(e.getEntity()) instanceof CraftPlayer) {
                final Player killer = (Player) PlayerUtil.getLastDamager(e.getEntity());
                profile.getMatch().handleDeath(e.getEntity(), killer, false);
            } else {
                profile.getMatch().handleDeath(e.getEntity(), e.getEntity().getKiller(), false);
            }
        }
    }

    public void onPlayerDeathInTheBridge(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());

        SoloBridgeMatch SoloBridgeMatch = (SoloBridgeMatch) profile.getMatch();

        Match match = profile.getMatch();

        if (match.getKit().getGameRules().isBridge()) {
            if (player.getKiller() == null) {
                player.getInventory().clear();
                SoloBridgeMatch.setupPlayer(player);
                if (profile.getKitEditor().getSelectedKitInventory() != null) {
                    player.getInventory().setArmorContents(profile.getKitEditor().getSelectedKitInventory().getArmor());
                    player.getInventory().setContents(profile.getKitEditor().getSelectedKitInventory().getContents());
                }
                InventoryUtil.giveBridgeKit(player);
                (this.plugin.getTimerManager().getTimer(BridgeArrowTimer.class)).clearCooldown(player.getUniqueId());
                player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                PlayerUtil.allowMovement(player);
                return;
            }

            if (player.getKiller() != null) {
                TeamPlayer teamPlayerForKiller = SoloBridgeMatch.getTeamPlayer(player.getKiller());
                teamPlayerForKiller.setTheBridgeKills(teamPlayerForKiller.getTheBridgeKills() + 1);

                double health = Math.ceil(player.getKiller().getHealth()) / 2.0;

                match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.BRIDGE_PLAYER_KILLED_BY_PLAYER").replace("<player>", player.getName()).replace("<killerPlayer>", player.getKiller().getName()).replace("<hp>", CC.translate(String.valueOf(health))));

                player.getInventory().clear();
                SoloBridgeMatch.setupPlayer(player);
                if (profile.getKitEditor().getSelectedKitInventory() != null) {
                    player.getInventory().setArmorContents(profile.getKitEditor().getSelectedKitInventory().getArmor());
                    player.getInventory().setContents(profile.getKitEditor().getSelectedKitInventory().getContents());
                }
                InventoryUtil.giveBridgeKit(player);
                (this.plugin.getTimerManager().getTimer(BridgeArrowTimer.class)).clearCooldown(player.getUniqueId());
                player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                PlayerUtil.allowMovement(player);
            }
        }
    }

    public void onPlayerDeathInBedFight(Player player) {
        Profile profile = Profile.getByUuid(player.getUniqueId());
        SoloBedFightMatch bedFightMatch = (SoloBedFightMatch) profile.getMatch();
        Match match = profile.getMatch();

        if (match.getKit().getGameRules().isBedFight()) {
            if (player.getKiller() != null) {
                double health = Math.ceil(player.getKiller().getHealth()) / 2.0;
                match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.BRIDGE_PLAYER_KILLED_BY_PLAYER").replace("<player>", player.getName()).replace("<killerPlayer>", player.getKiller().getName()).replace("<hp>", CC.translate(String.valueOf(health))));
                if (bedFightMatch.getRedBed() && bedFightMatch.getTeamPlayerA().getPlayer() == player) {
                    if (player.getGameMode() == GameMode.SURVIVAL) {
                        player.setGameMode(GameMode.SPECTATOR);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        PlayerUtil.denyMovement(player);
                        new MatchRespawnTask(bedFightMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                    }
                } else if (!bedFightMatch.getRedBed()) {
                    bedFightMatch.handleEnd(bedFightMatch.getTeamPlayerA().getPlayer());
                }

                if (bedFightMatch.getBlueBed() && bedFightMatch.getTeamPlayerB().getPlayer() == player) {
                    if (player.getGameMode() == GameMode.SURVIVAL) {
                        player.setGameMode(GameMode.SPECTATOR);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        PlayerUtil.denyMovement(player);
                        new MatchRespawnTask(bedFightMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                    }
                } else if (!bedFightMatch.getBlueBed()) {
                    bedFightMatch.handleEnd(bedFightMatch.getTeamPlayerB().getPlayer());
                }
            } else {
                if (bedFightMatch.getRedBed() && bedFightMatch.getTeamPlayerA().getPlayer() == player) {
                    if (player.getGameMode() == GameMode.SURVIVAL) {
                        player.setGameMode(GameMode.SPECTATOR);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        PlayerUtil.denyMovement(player);
                        new MatchRespawnTask(bedFightMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                    }
                } else if (!bedFightMatch.getRedBed()) {
                    bedFightMatch.handleEnd(bedFightMatch.getTeamPlayerA().getPlayer());
                }

                if (bedFightMatch.getBlueBed() && bedFightMatch.getTeamPlayerB().getPlayer() == player) {
                    if (player.getGameMode() == GameMode.SURVIVAL) {
                        player.setGameMode(GameMode.SPECTATOR);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        PlayerUtil.denyMovement(player);
                        new MatchRespawnTask(bedFightMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                    }
                } else if (!bedFightMatch.getBlueBed()) {
                    bedFightMatch.handleEnd(bedFightMatch.getTeamPlayerB().getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        e.setRespawnLocation(e.getPlayer().getLocation());
        final Profile profile = Profile.getByUuid(e.getPlayer().getUniqueId());
        Match match = profile.getMatch();
        if (profile.isInFight()) {
            profile.getMatch().handleRespawn(e.getPlayer());
            if (profile.getMatch().getKit().getGameRules().isBridge()) {
                SoloBridgeMatch SoloBridgeMatch = (SoloBridgeMatch) profile.getMatch();
                InventoryUtil.giveBridgeKit(e.getPlayer());
                e.getPlayer().teleport(match.getTeamPlayer(e.getPlayer()).getPlayerSpawn());
                PlayerUtil.reset(e.getPlayer());
                SoloBridgeMatch.setupPlayer(e.getPlayer());
                if (profile.getKitEditor().getSelectedKitInventory() != null) {
                    e.getPlayer().getInventory().setArmorContents(profile.getKitEditor().getSelectedKitInventory().getArmor());
                    e.getPlayer().getInventory().setContents(profile.getKitEditor().getSelectedKitInventory().getContents());
                }
                PlayerUtil.allowMovement(e.getPlayer());
            }

            if (profile.getMatch().getKit().getGameRules().isBedFight()) {
                SoloBedFightMatch bedFightMatch = (SoloBedFightMatch) profile.getMatch();
                e.getPlayer().teleport(match.getTeamPlayer(e.getPlayer()).getPlayerSpawn());
                PlayerUtil.reset(e.getPlayer());
                bedFightMatch.setupPlayer(e.getPlayer());
                InventoryUtil.giveBedFightKit(e.getPlayer());
                if (profile.getKitEditor().getSelectedKitInventory() != null) {
                    e.getPlayer().getInventory().setArmorContents(profile.getKitEditor().getSelectedKitInventory().getArmor());
                    e.getPlayer().getInventory().setContents(profile.getKitEditor().getSelectedKitInventory().getContents());
                }
                PlayerUtil.allowMovement(e.getPlayer());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunchEvent(final ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof ThrownPotion && e.getEntity().getShooter() instanceof Player) {
            final Player shooter = (Player) e.getEntity().getShooter();
            final Profile shooterData = Profile.getByUuid(shooter.getUniqueId());
            if (shooterData.isInFight() && shooterData.getMatch().isFighting()) {
                shooterData.getMatch().getTeamPlayer(shooter).incrementPotionsThrown();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHitEvent(final ProjectileHitEvent e) {

        if (e.getEntity() instanceof Arrow && e.getEntity().getShooter() instanceof Player) {
            final Player shooter = (Player) e.getEntity().getShooter();
            final Profile shooterData = Profile.getByUuid(shooter.getUniqueId());
            if (shooterData.isInFight()) {
                shooterData.getMatch().getEntities().add(e.getEntity());
                shooterData.getMatch().getTeamPlayer(shooter).handleHit();
            }
            if (shooterData.getMatch().getKit().getGameRules().isBridge()) {
                if (e.getEntityType() == EntityType.ARROW) {
                    e.getEntity().remove();
                }
            }
        }
        if (e.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player) e.getEntity().getShooter();
            Profile shooterData = Profile.getByUuid(shooter.getUniqueId());
            if (shooterData.getState() == ProfileState.IN_EVENT) {
                if (e.getEntityType() == EntityType.ARROW) {
                    e.getEntity().remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPotionSplashEvent(final PotionSplashEvent e) {
        if (e.getPotion().getShooter() instanceof Player) {
            final Player shooter = (Player) e.getPotion().getShooter();
            final Profile shooterData = Profile.getByUuid(shooter.getUniqueId());
            if (shooterData.isSpectating()) {
                e.setCancelled(true);
            }
            if (shooterData.isInFight() && e.getIntensity(shooter) <= 0.5) {
                shooterData.getMatch().getTeamPlayer(shooter).incrementPotionsMissed();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityRegainHealth(final EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        if (e.getEntity() instanceof Player && e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            e.setCancelled(true);
        }

        Profile profile = Profile.getByUuid((Player) e.getEntity());
        Match match = profile.getMatch();
        final Player shooter = (Player) e.getEntity();
        final Profile shooterData = Profile.getByUuid(shooter.getUniqueId());

        if (match == null) {
            return;
        }
        EntityRegainHealthEvent.RegainReason reason = e.getRegainReason();
        double amount = e.getAmount();
        if (reason == EntityRegainHealthEvent.RegainReason.MAGIC && amount > 2.0) {
            shooterData.getMatch().getTeamPlayer(shooter).setWastedHP(8.0 - amount);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPortal(EntityPortalEnterEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        Player player = (Player) e.getEntity();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        Match match = profile.getMatch();
        if (!profile.isInFight())
            return;
        if (player.getLocation().getBlock().getType() != Material.ENDER_PORTAL && player.getLocation().getBlock().getType() != Material.ENDER_PORTAL_FRAME)
            return;
        if (match.getKit().getGameRules().isBridge()) {
            SoloBridgeMatch SoloBridgeMatch = (SoloBridgeMatch) match;
            SoloBridgeMatch.handlePortal(player);
        }
        if (match.getKit().getGameRules().isBattleRush()) {
            SoloBattleRushMatch battleRushMatch = (SoloBattleRushMatch) match;
            battleRushMatch.handlePortal(player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() == Material.TRAP_DOOR || e.getClickedBlock().getType() == Material.WORKBENCH || e.getClickedBlock().getType() == Material.FENCE_GATE || e.getClickedBlock().getType() == Material.ANVIL || e.getClickedBlock().getType() == Material.CHEST) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(final EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player player = (Player) e.getEntity();
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.isInFight()) {
                if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    if (profile.getMatch().getKit().getGameRules().isBridge() || profile.getMatch().getKit().getGameRules().isBattleRush() || profile.getMatch().getKit().getGameRules().isBedFight() || profile.getMatch().getKit().getGameRules().isPearlFight()) {
                        e.setCancelled(true);
                    }
                }

                if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    if (profile.getMatch().getKit().getGameRules().isVoidspawn()) {
                        e.setDamage(0.0);
                        player.setFallDistance(0);
                        player.setHealth(20.0);

                        player.teleport(profile.getMatch().getTeamPlayer(player).getPlayerSpawn());
                        return;
                    }

                    if (!profile.getMatch().getKit().getGameRules().isBridge() || !profile.getMatch().getKit().getGameRules().isPearlFight() || !profile.getMatch().getKit().getGameRules().isBedFight()) {
                        profile.getMatch().handleDeath(player, null, false);
                        return;
                    }

                }

                if (!profile.getMatch().isFighting()) {
                    e.setCancelled(true);
                    return;
                }

                if ((profile.getMatch().isTeamMatch() || profile.getMatch().isHCFMatch()) && !profile.getMatch().getTeamPlayer(player).isAlive()) {
                    e.setCancelled(true);
                    return;
                }

                if (!profile.getMatch().isHCFMatch() && profile.getMatch().getKit().getGameRules().isSumo() || profile.getMatch().getKit().getGameRules().isPearlFight() || profile.getMatch().getKit().getGameRules().isBattleRush() || profile.getMatch().getKit().getGameRules().isBoxing()) {
                    e.setDamage(0.0);
                    player.setHealth(20.0);
                    player.updateInventory();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onLow(final PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Profile profile = Profile.getByUuid(player);
        if (profile.isInFight()) {
            Match match = profile.getMatch();
            double endLocation = (match.getArena().getSpawn1().getY() - 30);
            if (match.isFighting()) {
                if (match.isSoloMatch()) {
                    if (player.getLocation().getBlockY() <= endLocation) {
                        if (match.getKit().getGameRules().isSkywars()) {
                            if (player.getKiller() != null) {
                                profile.getMatch().handleDeath(e.getPlayer(), player.getKiller(), false);
                                double health = Math.ceil(player.getKiller().getHealth()) / 2.0;
                                match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_BY_PLAYER").replace("<player>", player.getName()).replace("<killerPlayer>", player.getKiller().getName()).replace("<hp>", String.valueOf(health)));

                            } else {
                                profile.getMatch().handleDeath(e.getPlayer(), null, false);
                                match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_IN_VOID").replace("<player>", player.getName()));
                            }
                        } else if (match.getKit().getGameRules().isBattleRush()) {
                            SoloBattleRushMatch battleRushMatch = (SoloBattleRushMatch) match;
                            if (player.getKiller() != null) {
                                if (player.getGameMode() == GameMode.SURVIVAL) {
                                    player.setGameMode(GameMode.SPECTATOR);
                                    player.setAllowFlight(true);
                                    player.setFlying(true);
                                    PlayerUtil.denyMovement(player);
                                    TeamPlayer teamPlayer = battleRushMatch.getTeamPlayer(player.getKiller());
                                    teamPlayer.setBattleRushKills(teamPlayer.getBattleRushKills() + 1);
                                    new MatchRespawnTask(battleRushMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                                    double health = Math.ceil(player.getKiller().getHealth()) / 2.0;
                                    match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_BY_PLAYER").replace("<player>", player.getName()).replace("<killerPlayer>", player.getKiller().getName()).replace("<hp>", String.valueOf(health)));
                                }
                            } else {
                                if (player.getGameMode() == GameMode.SURVIVAL) {
                                    player.setGameMode(GameMode.SPECTATOR);
                                    player.setAllowFlight(true);
                                    player.setFlying(true);
                                    PlayerUtil.denyMovement(player);
                                    new MatchRespawnTask(battleRushMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                                    match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_IN_VOID").replace("<player>", player.getName()));
                                }
                            }
                        } else if (match.getKit().getGameRules().isBedFight()) {
                            SoloBedFightMatch bedFightMatch = (SoloBedFightMatch) match;
                            if (player.getKiller() != null) {
                                if (bedFightMatch.getRedBed() && bedFightMatch.getTeamPlayerA().getPlayer() == player) {
                                    if (player.getGameMode() == GameMode.SURVIVAL) {
                                        player.setGameMode(GameMode.SPECTATOR);
                                        player.setAllowFlight(true);
                                        player.setFlying(true);
                                        PlayerUtil.denyMovement(player);
                                        TeamPlayer teamPlayer = bedFightMatch.getTeamPlayer(player.getKiller());
                                        teamPlayer.setBedFightKills(teamPlayer.getBedFightKills() + 1);
                                        new MatchRespawnTask(bedFightMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                                        double health = Math.ceil(player.getKiller().getHealth()) / 2.0;
                                        match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_BY_PLAYER").replace("<player>", player.getName()).replace("<killerPlayer>", player.getKiller().getName()).replace("<hp>", String.valueOf(health)));
                                    }
                                } else if (!bedFightMatch.getRedBed()) {
                                    bedFightMatch.handleEnd(bedFightMatch.getTeamPlayerA().getPlayer());
                                }

                                if (bedFightMatch.getBlueBed() && bedFightMatch.getTeamPlayerB().getPlayer() == player) {
                                    if (player.getGameMode() == GameMode.SURVIVAL) {
                                        player.setGameMode(GameMode.SPECTATOR);
                                        player.setAllowFlight(true);
                                        player.setFlying(true);
                                        PlayerUtil.denyMovement(player);
                                        TeamPlayer teamPlayer = bedFightMatch.getTeamPlayer(player.getKiller());
                                        teamPlayer.setBedFightKills(teamPlayer.getBedFightKills() + 1);
                                        double health = Math.ceil(player.getKiller().getHealth()) / 2.0;
                                        new MatchRespawnTask(bedFightMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                                        match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_BY_PLAYER").replace("<player>", player.getName()).replace("<killerPlayer>", player.getKiller().getName()).replace("<hp>", String.valueOf(health)));
                                    }
                                } else if (!bedFightMatch.getBlueBed()) {
                                    bedFightMatch.handleEnd(bedFightMatch.getTeamPlayerB().getPlayer());
                                }
                            } else {
                                if (bedFightMatch.getRedBed() && bedFightMatch.getTeamPlayerA().getPlayer() == player) {
                                    if (player.getGameMode() == GameMode.SURVIVAL) {
                                        player.setGameMode(GameMode.SPECTATOR);
                                        player.setAllowFlight(true);
                                        player.setFlying(true);
                                        PlayerUtil.denyMovement(player);
                                        new MatchRespawnTask(bedFightMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                                        match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_IN_VOID").replace("<player>", player.getName()));
                                    }
                                } else if (!bedFightMatch.getRedBed()) {
                                    bedFightMatch.handleEnd(bedFightMatch.getTeamPlayerA().getPlayer());
                                }

                                if (bedFightMatch.getBlueBed() && bedFightMatch.getTeamPlayerB().getPlayer() == player) {
                                    if (player.getGameMode() == GameMode.SURVIVAL) {
                                        player.setGameMode(GameMode.SPECTATOR);
                                        player.setAllowFlight(true);
                                        player.setFlying(true);
                                        PlayerUtil.denyMovement(player);
                                        new MatchRespawnTask(bedFightMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                                        match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_IN_VOID").replace("<player>", player.getName()));
                                    }
                                } else if (!bedFightMatch.getBlueBed()) {
                                    bedFightMatch.handleEnd(bedFightMatch.getTeamPlayerB().getPlayer());
                                }
                            }
                        } else if (match.getKit().getGameRules().isPearlFight()) {
                            SoloPearlFightMatch pearlFightMatch = (SoloPearlFightMatch) match;
                            if (player.getKiller() != null) {
                                if (pearlFightMatch.getPlayerAPoints() != 0) {
                                    if (player.getGameMode() == GameMode.SURVIVAL) {
                                        player.setGameMode(GameMode.SPECTATOR);
                                        player.setAllowFlight(true);
                                        player.setFlying(true);
                                        PlayerUtil.denyMovement(player);
                                        pearlFightMatch.onLow(player);
                                        TeamPlayer teamPlayer = pearlFightMatch.getTeamPlayer(player.getKiller());
                                        teamPlayer.setPearlFightKills(teamPlayer.getPearlFightKills() + 1);
                                        new MatchRespawnTask(pearlFightMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                                        double health = Math.ceil(player.getKiller().getHealth()) / 2.0;
                                        match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_BY_PLAYER").replace("<player>", player.getName()).replace("<killerPlayer>", player.getKiller().getName()).replace("<hp>", String.valueOf(health)));
                                    }
                                } else {
                                    pearlFightMatch.onLow(player);
                                    double health = Math.ceil(player.getKiller().getHealth()) / 2.0;
                                    match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_BY_PLAYER").replace("<player>", player.getName()).replace("<killerPlayer>", player.getKiller().getName()).replace("<hp>", String.valueOf(health)));
                                }
                            } else {
                                if (pearlFightMatch.getPlayerAPoints() != 0) {
                                    if (player.getGameMode() == GameMode.SURVIVAL) {
                                        player.setGameMode(GameMode.SPECTATOR);
                                        player.setAllowFlight(true);
                                        player.setFlying(true);
                                        PlayerUtil.denyMovement(player);
                                        pearlFightMatch.onLow(player);
                                        new MatchRespawnTask(pearlFightMatch, player).runTaskTimer(Practice.get(), 20L, 20L);
                                        match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_IN_VOID").replace("<player>", player.getName()));
                                    }
                                } else {
                                    pearlFightMatch.onLow(player);
                                }
                            }
                        } else if (match.getKit().getGameRules().isBridge()) {
                            SoloBridgeMatch SoloBridgeMatch = (SoloBridgeMatch) match;
                            if (player.getKiller() != null) {
                                TeamPlayer teamPlayer = SoloBridgeMatch.getTeamPlayer(player.getKiller());
                                teamPlayer.setTheBridgeKills(teamPlayer.getTheBridgeKills() + 1);
                                (this.plugin.getTimerManager().getTimer(BridgeArrowTimer.class)).clearCooldown(player.getUniqueId());
                                player.setFallDistance(0);
                                player.setHealth(20.0);
                                player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                                player.getInventory().clear();
                                PlayerUtil.reset(player);
                                SoloBridgeMatch.setupPlayer(player);
                                if (profile.getKitEditor().getSelectedKitInventory() != null) {
                                    player.getInventory().setArmorContents(profile.getKitEditor().getSelectedKitInventory().getArmor());
                                    player.getInventory().setContents(profile.getKitEditor().getSelectedKitInventory().getContents());
                                }
                                PlayerUtil.allowMovement(player);
                                InventoryUtil.giveBridgeKit(player);
                                double health = Math.ceil(player.getKiller().getHealth()) / 2.0;
                                match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_BY_PLAYER").replace("<player>", player.getName()).replace("<killerPlayer>", player.getKiller().getName()).replace("<hp>", String.valueOf(health)));
                            } else {
                                (this.plugin.getTimerManager().getTimer(BridgeArrowTimer.class)).clearCooldown(player.getUniqueId());
                                player.setFallDistance(0);
                                player.setHealth(20.0);
                                player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                                player.getInventory().clear();
                                PlayerUtil.reset(player);
                                SoloBridgeMatch.setupPlayer(player);
                                if (profile.getKitEditor().getSelectedKitInventory() != null) {
                                    player.getInventory().setArmorContents(profile.getKitEditor().getSelectedKitInventory().getArmor());
                                    player.getInventory().setContents(profile.getKitEditor().getSelectedKitInventory().getContents());
                                }
                                PlayerUtil.allowMovement(player);
                                InventoryUtil.giveBridgeKit(player);
                                match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KNOCKED_IN_VOID").replace("<player>", player.getName()));
                            }
                        }
                    }
                }
            } else if (match.isEnding()) {
                if (match.isSoloMatch()) {
                    if (match.getKit().getGameRules().isSkywars()) {
                        player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                        PlayerUtil.allowMovement(player);
                    }
                } else if (match.isBridgeMatch()) {
                    if (match.getKit().getGameRules().isBridge()) {
                        player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                        PlayerUtil.allowMovement(player);
                    }
                } else if (match.isBedFightMatch()) {
                    if (match.getKit().getGameRules().isBedFight()) {
                        player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                        PlayerUtil.allowMovement(player);
                    }
                } else if (match.isBattleRushMatch()) {
                    if (match.getKit().getGameRules().isBattleRush()) {
                        player.teleport(match.getTeamPlayer(player).getPlayerSpawn());
                        PlayerUtil.allowMovement(player);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Player player = (Player) e.getEntity();
        Player attacker;

        if (e.getCause() != null && e.getCause() != EntityDamageEvent.DamageCause.PROJECTILE && e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Profile profile = Profile.getByUuid((Player) e.getDamager());
            final Match match = profile.getMatch();

            if (profile.isSpectating()) {
                e.setCancelled(true);
            }

            if (!profile.isSpectating() && profile.isInFight()) {
                Player attacker2 = (Player) e.getDamager();
                Player victim = (Player) e.getEntity();

                match.getTeamPlayer(attacker2).runTimer();
            }
        }

        if (e.getDamager() instanceof Player) {
            attacker = (Player) e.getDamager();
        } else if (e.getDamager() instanceof Arrow && ((Projectile) e.getDamager()).getShooter() != ((Player) e.getEntity()).getPlayer()) {
            attacker = (Player) ((Projectile) e.getDamager()).getShooter();
        } else {
            if (!(e.getDamager() instanceof Projectile)) {
                e.setCancelled(true);
                return;
            }
            if (!(((Projectile) e.getDamager()).getShooter() instanceof Player)) {
                e.setCancelled(true);
                return;
            }
            attacker = (Player) ((Projectile) e.getDamager()).getShooter();
        }
        if (attacker != null && e.getEntity() instanceof Player) {
            final Player damaged = (Player) e.getEntity();
            final Profile damagedProfile = Profile.getByUuid(damaged.getUniqueId());
            final Profile attackerProfile = Profile.getByUuid(attacker.getUniqueId());
            if (attackerProfile.isSpectating() || damagedProfile.isSpectating()) {
                e.setCancelled(true);
                return;
            }

            if (e.getDamager() instanceof CraftEnderPearl) {
                e.setCancelled(false);

                return;
            }
            if (damagedProfile.isInFight() && attackerProfile.isInFight()) {
                final Match match = attackerProfile.getMatch();
                if (!match.isHCFMatch() && match.getKit().getGameRules().isSpleef() && !(e.getDamager() instanceof Projectile)) {
                    e.setCancelled(true);
                }

                if (!damagedProfile.getMatch().isHCFMatch() && damagedProfile.getMatch().getKit().getGameRules().isSpleef() && !(e.getDamager() instanceof Projectile)) {
                    e.setCancelled(true);
                    return;
                }
                if (!damagedProfile.getMatch().getMatchId().equals(attackerProfile.getMatch().getMatchId())) {
                    e.setCancelled(true);
                    return;
                }
                if (!match.getTeamPlayer(damaged).isAlive() || (!match.getTeamPlayer(attacker).isAlive() && !match.isFreeForAllMatch())) {
                    e.setCancelled(true);
                    return;
                }

                Profile profile = Profile.getByUuid(player);
                if (profile.getState() != ProfileState.FFA && profile.getState() == ProfileState.IN_LOBBY) {
                    e.setCancelled(true);
                }
                if (match.isSoloMatch() || match.isFreeForAllMatch() || match.getKit().getGameRules().isSumo() || match.isBridgeMatch()) {
                    if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
                        if (profile.isInFight()) {
                            Player whoWasHit = (Player) e.getEntity();
                            Player whoHit = (Player) e.getDamager();
                            TeamPlayer attackerTeam = match.getTeamPlayer(whoHit);
                            attackerProfile.getMatch().getTeamPlayer(whoHit).handleHit();
                            damagedProfile.getMatch().getTeamPlayer(whoWasHit).resetCombo();
                            if (isCritical(whoHit)) {
                                profile.getMatch().getTeamPlayer(whoHit).incrementCriticalHits();
                            }
                            if (whoWasHit.isBlocking()) {
                                profile.getMatch().getTeamPlayer(whoWasHit).incrementBlockedHits();
                            }
                            if (match.getKit().getGameRules().isBoxing() && attackerTeam.getHits() == 100) {
                                damagedProfile.getMatch().handleDeath(whoWasHit, whoHit, false);
                            }
                        } else e.setCancelled(!profile.isInBrackets() && !profile.isInSumo() && !profile.isInFFA());
                    }
                    if (e.getDamager() instanceof Arrow) {
                        final double health = Math.ceil(damaged.getHealth() - e.getFinalDamage()) / 2.0;
                        int range = (int) Math.ceil(e.getEntity().getLocation().distance(attacker.getLocation()));
                        if (match.getKit().getGameRules().isBowhp()) {
                            attacker.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.ARROW_HIT").replace("<player>", damaged.getName()).replace("<hp>", String.valueOf(health)).replace("<range>", String.valueOf(range)));
                        }
                    }
                } else if (match.isTeamMatch() || match.isHCFMatch()) {
                    final Team attackerTeam = match.getTeam(attacker);
                    final Team damagedTeam = match.getTeam(damaged);
                    if (attackerTeam == null || damagedTeam == null) {
                        e.setCancelled(true);
                    } else if (attackerTeam.equals(damagedTeam)) {
                        if (!damaged.getUniqueId().equals(attacker.getUniqueId())) {
                            e.setCancelled(true);
                        }
                    } else {
                        attackerProfile.getMatch().getTeamPlayer(attacker).handleHit();
                        damagedProfile.getMatch().getTeamPlayer(damaged).resetCombo();
                        if (e.getDamager() instanceof Arrow) {
                            final double health = Math.ceil(damaged.getHealth() - e.getFinalDamage()) / 2.0;
                            int range = (int) Math.ceil(e.getEntity().getLocation().distance(attacker.getLocation()));
                            if (match.getKit() == null || match.getKit().getGameRules().isBowhp()) {
                                if (!attacker.getName().equalsIgnoreCase(damaged.getName())) {
                                    attacker.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.ARROW_HIT").replace("<player>", damaged.getName()).replace("<hp>", String.valueOf(health)).replace("<range>", String.valueOf(range)));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(final PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        Profile profile = Profile.getByUuid(e.getPlayer());
        if (!profile.getSettings().isDropbottles()) {
            if (e.getItem().getType().equals(Material.POTION)) {
                Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(Practice.get(), () -> e.getPlayer().setItemInHand(new ItemStack(Material.AIR)), 1L);
            }
        }

        if (profile.getState() == ProfileState.IN_FIGHT && profile.getMatch().getKit().getGameRules().isBridge() && e.getItem().getType() == Material.GOLDEN_APPLE) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
            player.setHealth(20.0D);
        }

        if (e.getItem().getType() == Material.GOLDEN_APPLE && e.getItem().hasItemMeta() && e.getItem().getItemMeta().getDisplayName().contains("Golden Head")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
            player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
        }
    }

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player player = (Player) e.getEntity();
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.isInSomeSortOfFight()) {
                if (profile.getMatch() != null) {
                    if (profile.getMatch().getKit().getGameRules().isAntifoodloss()) {
                        if (e.getFoodLevel() >= 20) {
                            e.setFoodLevel(20);
                            player.setSaturation(20.0f);
                        }
                    } else {
                        e.setCancelled(false);
                    }
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        final Profile profile = Profile.getByUuid(e.getWhoClicked().getUniqueId());
        if (profile.isSpectating()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryInteract(final InventoryInteractEvent e) {
        final Profile profile = Profile.getByUuid(e.getWhoClicked().getUniqueId());
        if (profile.isSpectating()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractEvent(final PlayerInteractEvent e) {
        final Profile profile = Profile.getByUuid(e.getPlayer().getUniqueId());

        if (profile.isSpectating()) {
            e.setCancelled(true);
        }

        if (e.getItem() != null && e.getAction().name().contains("RIGHT") && profile.isInFight()) {
            if (e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasDisplayName()) {
                if (e.getItem().getType() == Material.ENCHANTED_BOOK || e.getItem().getType() == Material.BOOK) {
                    if (e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasDisplayName()) {
                        if (e.getItem().equals(Hotbar.getItems().get(HotbarType.DEFAULT_KIT))) {
                            final KitInventory kitInventory = profile.getMatch().getKit().getKitInventory();
                            e.getPlayer().getInventory().setArmorContents(kitInventory.getArmor());
                            e.getPlayer().getInventory().setContents(kitInventory.getContents());
                            e.getPlayer().getActivePotionEffects().clear();
                            if (profile.getMatch().getKit().getKitInventory().getEffects() != null) {
                                e.getPlayer().addPotionEffects(profile.getMatch().getKit().getKitInventory().getEffects());
                            }
                            if (profile.getMatch().getKit().getGameRules().isBridge()) {
                                InventoryUtil.giveBridgeKit(e.getPlayer());
                            } else if (profile.getMatch().getKit().getGameRules().isBedFight()) {
                                InventoryUtil.giveBedFightKit(e.getPlayer());
                            } else if (profile.getMatch().getKit().getGameRules().isBattleRush()) {
                                InventoryUtil.giveBattleRushKit(e.getPlayer());
                            }
                            e.getPlayer().updateInventory();
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
            if (!profile.getMatch().isHCFMatch() && e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasDisplayName() && e.getItem().getItemMeta().hasLore()) {
                final String displayName = ChatColor.stripColor(e.getItem().getItemMeta().getDisplayName());
                if (displayName.endsWith(" (Right-Click)")) {
                    final String kitName = displayName.replace(" (Right-Click)", "");
                    for (final KitInventory kitInventory2 : profile.getStatisticsData().get(profile.getMatch().getKit()).getLoadouts()) {
                        if (kitInventory2 != null && ChatColor.stripColor(kitInventory2.getCustomName()).equals(kitName)) {
                            e.getPlayer().getInventory().setArmorContents(kitInventory2.getArmor());
                            e.getPlayer().getInventory().setContents(kitInventory2.getContents());
                            e.getPlayer().getActivePotionEffects().clear();
                            profile.getKitEditor().setSelectedKitInventory(kitInventory2);
                            e.getPlayer().addPotionEffects(profile.getMatch().getKit().getKitInventory().getEffects());
                            if (profile.getMatch().getKit().getGameRules().isBridge()) {
                                InventoryUtil.giveBridgeKit(e.getPlayer());
                            } else if (profile.getMatch().getKit().getGameRules().isBedFight()) {
                                InventoryUtil.giveBedFightKit(e.getPlayer());
                            } else if (profile.getMatch().getKit().getGameRules().isBattleRush()) {
                                InventoryUtil.giveBattleRushKit(e.getPlayer());
                            }
                            e.getPlayer().updateInventory();
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }

            final Player player = e.getPlayer();
            if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) && player.getItemInHand().getType() == Material.MUSHROOM_SOUP) {
                final int health = (int) player.getHealth();
                if (health == 20) {
                    player.getItemInHand().setType(Material.MUSHROOM_SOUP);
                } else if (health >= 13) {
                    player.setHealth(20.0);
                    player.getItemInHand().setType(Material.BOWL);
                    profile.getMatch().getTeamPlayer(e.getPlayer()).incrementSoupsUsed();
                } else {
                    player.setHealth(health + 7);
                    player.getItemInHand().setType(Material.BOWL);
                    profile.getMatch().getTeamPlayer(e.getPlayer()).incrementSoupsUsed();
                }
            }
            if ((e.getItem().getType() == Material.ENDER_PEARL || (e.getItem().getType() == Material.POTION && e.getItem().getDurability() >= 16000)) && profile.isInFight() && profile.getMatch().isStarting()) {
                e.setCancelled(true);
                player.updateInventory();
                return;
            }
            if (e.getItem().getType() == Material.ENDER_PEARL && e.getClickedBlock() == null) {
                if (!profile.isInFight() || (profile.isInFight() && !profile.getMatch().isFighting())) {
                    e.setCancelled(true);
                    return;
                }
                if (profile.getMatch().isStarting()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPressurePlate(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.PHYSICAL) && e.getClickedBlock().getType() == Material.GOLD_PLATE) {
            Profile profile = Profile.getByUuid(e.getPlayer().getUniqueId());
            if (profile.isInFight() && !profile.getMatch().isHCFMatch())
                profile.getMatch().handleDeath(e.getPlayer(), null, false);
        }
    }

    @EventHandler
    public void onPotionSplash(final PotionSplashEvent e) {
        final Iterator<LivingEntity> iterator = e.getAffectedEntities().iterator();
        while (iterator.hasNext()) {
            final LivingEntity entity = iterator.next();
            if (entity instanceof Player) {
                final Player player = (Player) entity;
                final Profile profile = Profile.getByUuid(player.getUniqueId());
                if (!profile.isSpectating()) {
                    continue;
                }
                e.setIntensity(player, 0.0);
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Profile profile = Profile.getByUuid(e.getPlayer().getUniqueId());
        if (profile.isSpectating()) {
            profile.getMatch().removeSpectator(e.getPlayer());
        }
        if (profile.isInFight()) {
            if (profile.getMatch().getState() == MatchState.ENDING) {
                return;
            }

            if (profile.getMatch().getKit().getGameRules().isBridge()) {
                profile.getMatch().end();
                return;
            }
            profile.getMatch().end();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamageEntity(EntityDamageEvent e) {
        Player damaged = (Player) e.getEntity();
        Profile damagedProfile = Profile.getByUuid(damaged.getUniqueId());
        Match match = damagedProfile.getMatch();
        if (e.getEntity() instanceof Player) {
            if (damagedProfile.getMatch() != null) {
                if (match.isEnding()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    private boolean isCritical(Player attacker) {
        return attacker.getFallDistance() > 0.0F && !attacker.isOnGround() && !attacker.isInsideVehicle() && !attacker.hasPotionEffect(PotionEffectType.BLINDNESS) && attacker.getLocation().getBlock().getType() != Material.LADDER && attacker.getLocation().getBlock().getType() != Material.VINE;
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        e.getPlayer().setSprinting(true);
    }
}