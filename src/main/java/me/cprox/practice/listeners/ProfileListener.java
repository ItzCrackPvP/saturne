package me.cprox.practice.listeners;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.selection.Selection;
import me.cprox.practice.events.match.MatchEvent;
import me.cprox.practice.events.match.MatchStartEvent;
import me.cprox.practice.match.Match;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.util.TaskUtil;
import me.cprox.practice.util.chat.CC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.essentials.event.SpawnTeleportEvent;
import me.cprox.practice.util.nametag.NameTags;

import java.util.List;

public class ProfileListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerMatchStart(MatchEvent e) {
        if (e instanceof MatchStartEvent) {
            Match match = e.getMatch();
            Bukkit.getScheduler().runTaskLaterAsynchronously(Practice.get(), () -> match.getPlayers().forEach(player -> {
                Profile profile = Profile.getByUuid(player.getUniqueId());
                List<Player> followers = profile.getFollower();
                for (Player follower : followers) {
                    if (follower != null) {
                        follower.chat("/spec " + profile.getName());
                    }
                }
            }), 20L);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getClickedBlock() == null) {
            return;
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem().getType() == Material.PAINTING) {
                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    if (!e.getPlayer().isOp())
                        e.setCancelled(true);
                }
            }

            if (e.getClickedBlock().getState() instanceof ItemFrame) {
                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    if (!e.getPlayer().isOp()) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawnTeleportEvent(SpawnTeleportEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (!profile.isBusy(event.getPlayer()) && event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            PlayerUtil.reset(event.getPlayer(), true);
            Player player = event.getPlayer();
            player.getActivePotionEffects().clear();
            player.setHealth(20.0D);
            player.setFoodLevel(20);
            profile.refreshHotbar();
            profile.handleVisibility();
            player.setWalkSpeed(0.2F);
            player.setSprinting(true);
            player.removePotionEffect(PotionEffectType.JUMP);
        }
        for (Player ps : Bukkit.getOnlinePlayers()) {
            NameTags.color(event.getPlayer(), ps, ChatColor.GREEN, false);
            if (!Profile.getByUuid(ps).isBusy(ps) && !Profile.getByUuid(ps).isInSomeSortOfFight()) {
                if (Profile.getByUuid(ps).getState() == ProfileState.IN_LOBBY || Profile.getByUuid(ps).getState() == ProfileState.IN_QUEUE)
                    NameTags.color(ps, event.getPlayer(), ChatColor.GREEN, false);
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (!profile.isInSomeSortOfFight()) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (!(profile.isInSomeSortOfFight())) {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.isInSomeSortOfFight()) {
            if (!profile.isInFight() && !profile.isInSpleef()) {
                if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                    if (!event.getPlayer().isOp()) {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        } else {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.isInSomeSortOfFight()) {
            if (!profile.isInFight()) {
                if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                    if (!event.getPlayer().isOp()) {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        } else {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBucket(PlayerBucketEmptyEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.isInSomeSortOfFight()) {
            if (!profile.isInFight()) {
                if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                    if (!event.getPlayer().isOp()) {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            }
        } else {
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                if (!event.getPlayer().isOp()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamageEvent(PlayerItemDamageEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

        if (profile.isInLobby() || profile.isManaging()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());

            if (profile.isInLobby() || profile.isInQueue() || profile.isManaging()) {
                event.setCancelled(true);

                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    Practice.get().getEssentials().teleportToSpawn((Player) event.getEntity());
                }
            }
        }
    }

    @EventHandler
    public void onFoodLoss(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Profile profile = Profile.getByUuid(event.getEntity().getUniqueId());

            if (profile.isInLobby() || profile.isInQueue() || profile.isManaging()) {
                event.setCancelled(true);
            }
            if (profile.isInSumo()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        Profile.getPlayerList().add(player);
        Profile profile = new Profile(player.getUniqueId());
        PlayerUtil.reset(player, true);
        TaskUtil.runAsync(() -> {
            for (Profile other : Profile.getProfiles().values()) {
                other.handleVisibility();
            }
            try {
                profile.load();
            } catch (Exception e) {
                e.printStackTrace();
                event.getPlayer().kickPlayer("Failed to load your profile, please contact an plugin author!");
                return;
            }
            Profile.getProfiles().put(player.getUniqueId(), profile);
            profile.setName(player.getName());
            Practice.get().getEssentials().teleportToSpawn(player);
            profile.getKitEditor().setActive(false);
            profile.getKitEditor().setRename(false);
            profile.refreshHotbar();
        });
        new BukkitRunnable() {
            @Override
            public void run() {
                profile.handleVisibility();
            }
        }.runTaskLaterAsynchronously(Practice.get(), 5L);

        for (Player ps : Bukkit.getOnlinePlayers()) {
            NameTags.color(player, ps, ChatColor.GREEN, false);
            if (!Profile.getByUuid(ps).isBusy(ps) && !Profile.getByUuid(ps).isInSomeSortOfFight()) {
                if (Profile.getByUuid(ps).getState() == ProfileState.IN_LOBBY || Profile.getByUuid(ps).getState() == ProfileState.IN_QUEUE)
                    NameTags.color(ps, player, ChatColor.GREEN, false);
            }
        }
        switch (profile.getWorldTime()) {
            case "Day":
                player.setPlayerTime(0L, false);
                break;
            case "Night":
                player.setPlayerTime(20000L, false);
                break;
            case "Sunset":
                player.setPlayerTime(12500, false);
                break;
            default:
                player.resetPlayerTime();
                break;
        }
    }

    @EventHandler
    public void onPlayerKickEvent(PlayerKickEvent event) {
        if (event.getReason() != null) {
            if (event.getReason().contains("Flying is not enabled")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPressurePlate(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.PHYSICAL)) {
            event.setCancelled(true);
        }
    }

    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();

        if (item != null && item.equals(Selection.SELECTION_WAND)) {
            Player player = event.getPlayer();
            Block clicked = event.getClickedBlock();
            int location = 0;

            Selection selection = Selection.createOrGetSelection(player);

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                selection.setPoint2(clicked.getLocation());
                location = 2;
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                selection.setPoint1(clicked.getLocation());
                location = 1;
            }

            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            event.setUseInteractedBlock(Event.Result.DENY);


            String message = CC.AQUA + ((location == 1) ? "First" : "Second") + " location " + CC.YELLOW + "(" + CC.GREEN + clicked.getX() + CC.YELLOW + ", " + CC.GREEN + clicked.getY() + CC.YELLOW + ", " + CC.GREEN + clicked.getZ() + CC.YELLOW + ")" + CC.AQUA + " has been set!";

            if (selection.isFullObject()) {
                message = message + CC.RED + " (" + CC.YELLOW + selection.getCuboid().volume() + CC.AQUA + " blocks" + CC.RED + ")";
            }


            player.sendMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChatTabComplete(PlayerChatTabCompleteEvent event) {
        List<String> completions = (List<String>) event.getTabCompletions();
        completions.clear();
        String token = event.getLastToken();
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (StringUtil.startsWithIgnoreCase(p.getName(), token)) {
                completions.add(p.getName());
            }
        }
    }
}