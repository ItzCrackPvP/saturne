package me.cprox.practice.match;

import me.cprox.practice.events.match.MatchEndEvent;
import me.cprox.practice.events.match.MatchStartEvent;
import me.cprox.practice.match.impl.solo.*;
import me.cprox.practice.match.task.*;
import me.cprox.practice.menu.match.InventorySnapshot;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.profile.enums.MatchState;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.queue.Queue;
import me.cprox.practice.util.EntityUtil;
import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.team.Team;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ChatComponentBuilder;
import me.cprox.practice.util.external.TimeUtil;
import me.cprox.practice.util.nametag.NameTags;
import lombok.Getter;
import lombok.Setter;
import me.cprox.practice.util.timer.event.impl.BridgeArrowTimer;
import net.md_5.bungee.api.chat.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.*;

@Getter
public abstract class Match {

    private final Practice plugin = Practice.get();
    @Getter protected static List<Match> matches = new ArrayList<>();
    private final UUID matchId = UUID.randomUUID();
    private final Queue queue;
    private final Kit kit;
    private final Arena arena;
    private final QueueType queueType;
    protected final List<Item> droppedItems;
    @Getter private final Map<UUID, InventorySnapshot> snapshots = new HashMap<>();
    public Map<UUID, EnderPearl> pearlMap = new HashMap<>();
    @Setter public MatchState state = MatchState.STARTING;
    public final List<UUID> spectators = new ArrayList<>();
    private final List<Entity> entities = new ArrayList<>();
    private final List<Location> placedBlocks = new ArrayList<>();
    private final List<BlockState> changedBlocks = new ArrayList<>();
    private final List<Location> brokenBlocks = new ArrayList<>();
    @Getter public BukkitTask task;
    @Setter private long startTimestamp;
    @Getter @Setter private BukkitTask matchWaterCheck;
    private static Field STATUS_PACKET_ID_FIELD;
    private static Field STATUS_PACKET_STATUS_FIELD;
    private static Field SPAWN_PACKET_ID_FIELD;
    public Practice getPlugin() {
        return this.plugin;
    }

    public Match(Queue queue, Kit kit, Arena arena, QueueType queueType) {
        this.queue = queue;
        this.kit = kit;
        this.arena = arena;
        this.queueType = queueType;
        this.droppedItems = new ArrayList<>();
        matches.add(this);
    }

    public static void preload() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    world.setStorm(false);
                    world.setThundering(false);
                }
            }
        }.runTaskTimer(Practice.get(), 20, 20);
    }

    public static void cleanup() {
        for (Match match : matches) {
            match.getPlacedBlocks().forEach(location -> location.getBlock().setType(Material.AIR));
            match.getChangedBlocks().forEach((blockState) -> blockState.getLocation().getBlock().setType(blockState.getType()));
            match.getEntities().forEach(Entity::remove);
        }
    }

    public static int getInFights(Queue queue) {
        int i = 0;

        for (Match match : matches) {
            if (match.getQueue() != null && (match.isFighting() || match.isStarting())) {
                if (match.getQueue() != null && match.getQueue().equals(queue)) {
                    i = i + match.getTeamPlayers().size();
                }
            }
        }

        return i;
    }

    public boolean isStarting() {
        return state == MatchState.STARTING;
    }

    public boolean isFighting() {
        return state == MatchState.FIGHTING;
    }

    public boolean isEnding() {
        return state == MatchState.ENDING;
    }

    public void start() {
        for (Player player : getPlayers()) {

            Profile profile = Profile.getByUuid(player.getUniqueId());
            profile.setState(ProfileState.IN_FIGHT);

            profile.setMatch(this);

            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                profile.handleVisibility(player, otherPlayer);
            }
            if (getKit().getGameRules().isShowhealth()) {
                for (Player matchPlayers : this.getPlayers()) {
                    Objective objective = player.getScoreboard().getObjective(DisplaySlot.BELOW_NAME);

                    if (objective == null) {
                        objective = player.getScoreboard().registerNewObjective("showhealth", "health");
                    }

                    objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
                    objective.setDisplayName(CC.RED + "❤");
                    objective.getScore(matchPlayers.getName()).setScore((int) matchPlayers.getHealth());
                }
            }
            if (!getArena().getSpawn1().getChunk().isLoaded() || !getArena().getSpawn2().getChunk().isLoaded()) {
                getArena().getSpawn1().getChunk().load();
                getArena().getSpawn2().getChunk().load();
            }
            setupPlayer(player);
        }

        onStart();

        for (Player player : this.getPlayers()) {
            if (!Profile.getByUuid(player.getUniqueId()).getSentDuelRequests().isEmpty()) {
                Profile.getByUuid(player.getUniqueId()).getSentDuelRequests().clear();
            }
        }

        state = MatchState.STARTING;
        startTimestamp = -1;
        arena.setActive(true);

        if (getKit() != null) {
            if (getKit().getGameRules().isWaterkill() || getKit().getGameRules().isSumo()) {
                matchWaterCheck = new MatchWaterCheckTask(this).runTaskTimer(Practice.get(), 60L, 20L);
            }
        }

        task = new MatchStartTask(this).runTaskTimer(Practice.get(), 20L, 20L);
        if (!isBotMatch()) {
            for (Player shooter : getPlayers()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Profile shooterData = Profile.getByUuid(shooter.getUniqueId());

                        if (shooterData.isInFight()) {
                            int potions = 0;
                            for (ItemStack item : shooter.getInventory().getContents()) {
                                if (item == null)
                                    continue;
                                if (item.getType() == Material.AIR)
                                    continue;
                                if (item.getType() != Material.POTION)
                                    continue;
                                if (item.getDurability() != (short) 16421)
                                    continue;
                                potions++;
                            }
                            shooterData.getMatch().getTeamPlayer(shooter).setPotions(potions);
                        } else {
                            cancel();
                        }

                    }
                }.runTaskTimerAsynchronously(Practice.get(), 0L, 5L);
            }
        }
        final MatchStartEvent event = new MatchStartEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void end() {
        if (onEnd()) state = MatchState.ENDING;
        else return;
        this.getSnapshots().values().forEach(snapshot -> plugin.getProfileManager().addSnapshot(snapshot));

        this.getSpectators().forEach(this::removeSpectator);

        new MatchResetTask(this).runTask(Practice.get());

        this.getArena().setActive(false);

        matches.remove(this);
        this.getPlayers().forEach(this::removePearl);
        MatchEndEvent event = new MatchEndEvent(this);
        Bukkit.getPluginManager().callEvent(event);

        Bukkit.getScheduler().runTaskLaterAsynchronously(Practice.get(), () -> getPlayers().forEach(player -> ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0)), 10L);
        Bukkit.getScheduler().runTaskLaterAsynchronously(Practice.get(), () -> getPlayers().forEach(player -> ((CraftPlayer) player).getHandle().getDataWatcher().watch(9, (byte) 0)), 20L);

        if (Practice.get().getMainConfig().getBoolean("Rating-Enabled")) {
            this.handleRate();
        }
    }

    public void handleDeath(Player deadPlayer, Player killerPlayer, boolean disconnected) {
        TeamPlayer teamPlayer = this.getTeamPlayer(deadPlayer);

        if (teamPlayer == null) return;

        teamPlayer.setDisconnected(disconnected);

        if (!teamPlayer.isAlive()) return;
        teamPlayer.setAlive(false);

        List<Player> playersAndSpectators = getPlayersAndSpectators();

        for (Player player : playersAndSpectators) {
            if (teamPlayer.isDisconnected()) {
                player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_DISCONNECTED").replace("<player>", deadPlayer.getName()));
                continue;
            }
            if (killerPlayer == null) {
                player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_DIED").replace("<player>", deadPlayer.getName()));
            } else {
                player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.PLAYER_KILLED_BY_PLAYER").replace("<player>", deadPlayer.getName()).replace("<killerPlayer>", killerPlayer.getName()));
            }
        }

        onDeath(deadPlayer, killerPlayer);

        deadPlayer.setHealth(20);
        deadPlayer.setFoodLevel(20);
        if (!getKit().getGameRules().isSumo()) {
            deadPlayer.setFlySpeed(0.2F);
            deadPlayer.setAllowFlight(true);
            deadPlayer.setFlying(true);
        }

        if ((isSoloMatch()) && disconnected) {
            end();
            return;
        }

        if (!getKit().getGameRules().isSumo()) {
            if (canEnd()) end();
            else {
                PlayerUtil.spectator(deadPlayer);
                Bukkit.getScheduler().runTaskLaterAsynchronously(Practice.get(), () -> getPlayersAndSpectators().forEach(player -> Profile.getByUuid(player.getUniqueId()).handleVisibility(player, deadPlayer)), 40L);
            }
        } else {
            if (canEnd()) end();
        }
        
        if (PlayerUtil.getLastDamager(deadPlayer) instanceof CraftPlayer) {
            Player killer = (Player) PlayerUtil.getLastDamager(deadPlayer);

            assert killer != null;
            Profile profile = Profile.getByUuid(killer);
            if (profile.getSettings().isLightning()) {

                EntityLightning entityLightning = new EntityLightning(((CraftPlayer) deadPlayer).getHandle().getWorld(), deadPlayer.getLocation().getX(), deadPlayer.getLocation().getY(), deadPlayer.getLocation().getZ());

                PacketPlayOutSpawnEntityWeather lightning = new PacketPlayOutSpawnEntityWeather(entityLightning);

                PacketPlayOutNamedSoundEffect lightningSound = new PacketPlayOutNamedSoundEffect("ambient.weather.thunder", deadPlayer.getLocation().getX(), deadPlayer.getLocation().getY(), deadPlayer.getLocation().getZ(), 10000.0F, 63.0F);
                PacketPlayOutWorldParticles cloud = new PacketPlayOutWorldParticles(EnumParticle.CLOUD, false, (float) deadPlayer.getLocation().getX(), (float) deadPlayer.getLocation().getY(), (float) deadPlayer.getLocation().getZ(), 0.5F, 0.5F, 0.5F, 0.1F, 10);
                PacketPlayOutWorldParticles flame = new PacketPlayOutWorldParticles(EnumParticle.FLAME, false, (float) deadPlayer.getLocation().getX(), (float) deadPlayer.getLocation().getY(), (float) deadPlayer.getLocation().getZ(), 0.3F, 0.3F, 0.3F, 0.1F, 12);
                (((CraftPlayer) deadPlayer).getHandle()).playerConnection.sendPacket(lightning);
                (((CraftPlayer) deadPlayer).getHandle()).playerConnection.sendPacket(lightningSound);
                (((CraftPlayer) deadPlayer).getHandle()).playerConnection.sendPacket(cloud);
                (((CraftPlayer) deadPlayer).getHandle()).playerConnection.sendPacket(flame);
                for (final Player onlinePlayer : profile.getMatch().getPlayers()) {
                    (((CraftPlayer) onlinePlayer).getHandle()).playerConnection.sendPacket(lightning);
                    (((CraftPlayer) onlinePlayer).getHandle()).playerConnection.sendPacket(lightningSound);
                    (((CraftPlayer) onlinePlayer).getHandle()).playerConnection.sendPacket(cloud);
                    (((CraftPlayer) onlinePlayer).getHandle()).playerConnection.sendPacket(flame);
                }
            }

            if (profile.getSettings().isClearinventory()) {
                killer.getInventory().clear();
                killer.getInventory().setArmorContents(null);
            }

            if (profile.getSettings().isBloodeffect()) {
                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.BLOCK_CRACK, false, (float) deadPlayer.getLocation().getX(), (float) deadPlayer.getLocation().getY(), (float) deadPlayer.getLocation().getZ(), 0.2F, 0.2F, 0.2F, 1.0F, 20, Material.REDSTONE_BLOCK.getId());
                (((CraftPlayer) deadPlayer).getHandle()).playerConnection.sendPacket(packet);
                deadPlayer.playSound(deadPlayer.getLocation(), Sound.FALL_BIG, 1.0F, 0.9F);
                for (int i = 0; i < 5; i++)
                    for (final Player onlinePlayer : profile.getMatch().getPlayers()) {
                        (((CraftPlayer) onlinePlayer).getHandle()).playerConnection.sendPacket(packet);
                        onlinePlayer.playSound(deadPlayer.getLocation(), Sound.FALL_BIG, 1.0F, 0.9F);
                    }
            }
        }

    }

    public void handleRate() {
        TextComponent rateMapBase = new TextComponent(Practice.get().getMessagesConfig().getString("MATCH.VOTE_ARENA").replace("<arena>", getArena().getName()));
        TextComponent rateTerrible = new TextComponent();
        TextComponent rateNotBest = new TextComponent();
        TextComponent rateOkay = new TextComponent();
        TextComponent rateGood = new TextComponent();
        TextComponent rateAwesome = new TextComponent();

        rateTerrible.setText(Practice.get().getMessagesConfig().getString("MATCH.VOTE_ARENA_TERRIBLE"));
        rateNotBest.setText(Practice.get().getMessagesConfig().getString("MATCH.VOTE_ARENA_NOTBEST"));
        rateOkay.setText(Practice.get().getMessagesConfig().getString("MATCH.VOTE_ARENA_OKAY"));
        rateGood.setText(Practice.get().getMessagesConfig().getString("MATCH.VOTE_ARENA_GOOD"));
        rateAwesome.setText(Practice.get().getMessagesConfig().getString("MATCH.VOTE_ARENA_AWESOME"));

        rateTerrible.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Practice.get().getMessagesConfig().getString("MATCH.VOTE_ARENA_HOLD_TERRIBLE").replace("<arena>", getArena().getName())).create()));
        rateNotBest.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Practice.get().getMessagesConfig().getString("MATCH.VOTE_ARENA_HOLD_NOTBEST").replace("<arena>", getArena().getName())).create()));
        rateOkay.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Practice.get().getMessagesConfig().getString("MATCH.VOTE_ARENA_HOLD_OKAY").replace("<arena>", getArena().getName())).create()));
        rateGood.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Practice.get().getMessagesConfig().getString("MATCH.VOTE_ARENA_HOLD_GOOD").replace("<arena>", getArena().getName())).create()));
        rateAwesome.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Practice.get().getMessagesConfig().getString("MATCH.VOTE_ARENA_HOLD_AWESOME").replace("<arena>", getArena().getName())).create()));

        rateTerrible.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arenaratingrateterrible " + getArena().getName()));
        rateNotBest.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arenaratingratenotbest " + getArena().getName()));
        rateOkay.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arenaratingrateokay " + getArena().getName()));
        rateGood.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arenaratingrategood " + getArena().getName()));
        rateAwesome.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arenaratingrateamazing " + getArena().getName()));

        BaseComponent[] rateBases = {
                rateMapBase,
                rateTerrible,
                rateNotBest,
                rateOkay,
                rateGood,
                rateAwesome
        };

        this.getPlayers().stream().filter(Objects::nonNull).forEach(player -> {
            Profile profile = Profile.getByUuid(player);
            profile.setCanRate(true);
        });

        this.getPlayers().forEach(player -> player.spigot().sendMessage(rateBases));

        Bukkit.getScheduler().runTaskLater(Practice.get(), () -> this.getPlayers().forEach(player -> {
            Profile profile = Profile.getByUuid(player);
            profile.setCanRate(false);
        }), 20 * 20L);
    }

    public void handleRespawn(Player player) {
        player.setVelocity(new Vector());
        this.plugin.getTimerManager().getTimer(BridgeArrowTimer.class).clearCooldown(player);
        onRespawn(player);
    }

    public void removePearl(Player player) {
        EnderPearl pearl;
        if (player != null) if ((pearl = this.pearlMap.remove(player.getUniqueId())) != null) pearl.remove();
    }

    public String getDuration() {
        if (isStarting()) {
            return "00:00";
        } else if (isEnding()) {
            return "Ending";
        } else {
            return TimeUtil.millisToTimer(getElapsedDuration());
        }
    }

    public long getElapsedDuration() {
        return System.currentTimeMillis() - startTimestamp;
    }

    public void broadcastMessage(String message) {
        getPlayers().forEach(player -> player.sendMessage(message));
        getSpectators().forEach(player -> player.sendMessage(message));
    }

    public void broadcastSound(Sound sound) {
        getPlayers().forEach(player -> player.playSound(player.getLocation(), sound, 1.0F, 1.0F));
        getSpectators().forEach(player -> player.playSound(player.getLocation(), sound, 1.0F, 1.0F));
    }

    protected List<Player> getSpectators() {
        return PlayerUtil.convertUUIDListToPlayerList(spectators);
    }

    public void addSpectator(Player player, Player target) {
        Location spawn = Profile.getByUuid(target).getMatch().getArena().getSpawn1();
        spectators.add(player.getUniqueId());
        PlayerUtil.spectator(player);
        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setMatch(this);
        profile.setState(ProfileState.SPECTATE_MATCH);
        profile.refreshHotbar();
        profile.handleVisibility();
        player.teleport(spawn);
        player.teleport(target.getLocation().clone().add(0, 2, 0));
        player.spigot().setCollidesWithEntities(false);

        if (!profile.isSilent() || !player.hasPermission("practice.staff")) {
            for (Player otherPlayer : getPlayers()) {
                otherPlayer.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.START_SPECTATING").replace("<player>", player.getName()));
            }
        }


        Bukkit.getScheduler().runTaskLaterAsynchronously(Practice.get(), () -> {
            if (this.isSoloMatch()) {
                NameTags.color(player, target, ChatColor.RED, this.getKit().getGameRules().isBuild());

                target.hidePlayer(player);
                this.getOpponentPlayer(target).hidePlayer(player);
                NameTags.color(player, this.getOpponentPlayer(target), ChatColor.GREEN, this.getKit().getGameRules().isBuild());
            } else if (getKit().getGameRules().isSumo()) {
                NameTags.color(player, target, ChatColor.RED, this.getKit().getGameRules().isBuild());
                NameTags.color(player, this.getOpponentPlayer(target), ChatColor.GREEN, this.getKit().getGameRules().isBuild());
                target.hidePlayer(player);
                this.getOpponentPlayer(target).hidePlayer(player);
            } else if (this.isTeamMatch()) {
                this.getTeam(target).getPlayers().forEach(p -> NameTags.color(player, p, ChatColor.GREEN, this.getKit().getGameRules().isBuild()));
                for (Player targetPlayers : this.getTeam(target).getPlayers()) {
                    targetPlayers.hidePlayer(player);
                }
                this.getOpponentTeam(target).getPlayers().forEach(p -> NameTags.color(player, p, ChatColor.RED, this.getKit().getGameRules().isBuild()));
                for (Player targetPlayers : this.getOpponentTeam(target).getPlayers()) {
                    targetPlayers.hidePlayer(player);
                }
            } else if (this.isFreeForAllMatch()) {
                for (Player targetPlayers : this.getPlayers()) {
                    targetPlayers.hidePlayer(player);
                }
                this.getPlayers().forEach(p -> NameTags.color(player, p, ChatColor.RED, this.getKit().getGameRules().isBuild()));
            }
        }, 20L);
    }


    public void removeSpectator(Player player) {
        spectators.remove(player.getUniqueId());

        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setState(ProfileState.IN_LOBBY);
        profile.setMatch(null);
        PlayerUtil.reset(player);
        profile.refreshHotbar();
        profile.handleVisibility();
        Practice.get().getEssentials().teleportToSpawn(player);
        player.spigot().setCollidesWithEntities(true);
        if (state != MatchState.ENDING) {
            for (Player otherPlayer : getPlayers()) {
                otherPlayer.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.NO_LONGER_SPECTATING").replace("<player>", player.getName()));
            }
        }
    }

    public List<Player> getPlayersAndSpectators() {
        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(getPlayers());
        allPlayers.addAll(getSpectators());
        return allPlayers;
    }

    protected HoverEvent getHoverEvent(TeamPlayer teamPlayer) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder("")
                .parse(Practice.get().getMessagesConfig().getString("MATCH.INVENTORY_HOVER").replace("<player>", teamPlayer.getUsername())).create());
    }

    protected ClickEvent getClickEvent(TeamPlayer teamPlayer) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/_ " + getSnapshot(teamPlayer.getUuid()).getSnapshotId());
    }
    public double getAverage(double one, double two) {
        double three = one + two;
        three = three / 2;
        return three;
    }

    public Location getMidSpawn() {

        Location spawn = getArena().getSpawn1();
        Location spawn2 = getArena().getSpawn2();
        Location midSpawn = getArena().getSpawn1();

        midSpawn.setX(getAverage(spawn.getX(), spawn2.getX()));
        midSpawn.setZ(getAverage(spawn.getZ(), spawn2.getZ()));

        return midSpawn;
    }

    public boolean isSoloMatch() {
        return false;
    }

    public boolean isTeamMatch() {
        return false;
    }

    public boolean isFreeForAllMatch() {
        return false;
    }

    public boolean isHCFMatch() {
        return false;
    }

    public boolean isBotMatch() {
        return false;
    }

    public boolean isBridgeMatch() {
        return this.isSoloMatch() && (this instanceof SoloBridgeMatch /*|| this instanceof TeamBridgeMatch*/);
    }

    public boolean isBattleRushMatch() {
        return this.isSoloMatch() && (this instanceof SoloBattleRushMatch /*|| this instanceof TeamBattleRushMatch*/);
    }

    public boolean isBedFightMatch() {
        return this.isSoloMatch() && (this instanceof SoloBedFightMatch /*|| this instanceof TeamBedFightMatch*/);
    }

    public boolean isPearlFightMatch() {
        return this.isSoloMatch() && (this instanceof SoloPearlFightMatch /*|| this instanceof TeamPearlFightMatch*/);
    }

    public abstract void setupPlayer(Player player);
    public abstract void onStart();
    public abstract boolean onEnd();

    public abstract boolean canEnd();

    public abstract void onDeath(Player player, Player killer);

    public abstract void onRespawn(Player player);

    public abstract Player getWinningPlayer();

    public abstract Team getWinningTeam();

    public abstract TeamPlayer getTeamPlayerA();

    public abstract TeamPlayer getTeamPlayerB();

    public abstract List<TeamPlayer> getTeamPlayers();

    public abstract List<Player> getPlayers();

    public abstract List<Player> getAlivePlayers();

    public abstract Team getTeamA();

    public abstract Team getTeamB();

    public abstract Team getTeam(Player player);

    public abstract TeamPlayer getTeamPlayer(Player player);

    public abstract Team getOpponentTeam(Team Team);

    public abstract Team getOpponentTeam(Player player);

    public abstract TeamPlayer getOpponentTeamPlayer(Player player);

    public abstract Player getOpponentPlayer(Player player);

    public abstract int getRoundsNeeded(TeamPlayer teamPlayer);

    public abstract int getRoundsNeeded(Team Team);

    public abstract ChatColor getRelationColor(Player viewer, Player target);

    public void addSnapshot(Player player) {
        this.snapshots.put(player.getUniqueId(), new InventorySnapshot(player, this));
    }

    public boolean hasSnapshot(UUID uuid) {
        return this.snapshots.containsKey(uuid);
    }

    public InventorySnapshot getSnapshot(UUID uuid) {
        return this.snapshots.get(uuid);
    }

    static {
        try {
            STATUS_PACKET_ID_FIELD = PacketPlayOutEntityStatus.class.getDeclaredField("a");
            STATUS_PACKET_ID_FIELD.setAccessible(true);
            STATUS_PACKET_STATUS_FIELD = PacketPlayOutEntityStatus.class.getDeclaredField("b");
            STATUS_PACKET_STATUS_FIELD.setAccessible(true);
            SPAWN_PACKET_ID_FIELD = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("a");
            SPAWN_PACKET_ID_FIELD.setAccessible(true);
        } catch (NoSuchFieldException var1) {
            var1.printStackTrace();
        }

    }
}