package me.cprox.practice.profile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import lombok.Getter;
import lombok.Setter;
import me.cprox.practice.Practice;
import me.cprox.practice.events.brackets.Brackets;
import me.cprox.practice.events.brackets.player.BracketsPlayer;
import me.cprox.practice.events.brackets.player.BracketsPlayerState;
import me.cprox.practice.events.spleef.Spleef;
import me.cprox.practice.events.spleef.player.SpleefPlayer;
import me.cprox.practice.events.spleef.player.SpleefPlayerState;
import me.cprox.practice.events.sumo.Sumo;
import me.cprox.practice.events.sumo.player.SumoPlayer;
import me.cprox.practice.events.sumo.player.SumoPlayerState;
import me.cprox.practice.kit.*;
import me.cprox.practice.managers.KitManager;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.duel.DuelProcedure;
import me.cprox.practice.match.duel.DuelRequest;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.party.Party;
import me.cprox.practice.profile.enums.HotbarType;
import me.cprox.practice.profile.enums.MatchState;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.profile.enums.SettingsMeta;
import me.cprox.practice.profile.hotbar.Hotbar;
import me.cprox.practice.profile.hotbar.HotbarLayout;
import me.cprox.practice.profile.meta.ProfileRematchData;
import me.cprox.practice.queue.Queue;
import me.cprox.practice.queue.QueueProfile;
import me.cprox.practice.profile.meta.StatisticsData;
import me.cprox.practice.util.InventoryUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.nametag.NameTags;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Profile {
    @Getter private static final Map<UUID, Profile> profiles = new HashMap<>();
    @Getter private static final List<KitLeaderboards> globalUnrankedEloLeaderboards = new ArrayList<>();
    @Getter private static final List<KitLeaderboards> globalWinStreakLeaderboards = new ArrayList<>();
    @Getter private static final List<KitLeaderboards> globalEloLeaderboards = new ArrayList<>();
    @Getter private static Map<Integer, String> eloLeagues = new HashMap<>();
    @Getter private static MongoCollection<Document> allProfiles;
    private static MongoCollection<Document> collection;
    @Getter private final KitEditor kitEditor = new KitEditor();
    @Getter private final Map<Kit, StatisticsData> statisticsData= new LinkedHashMap<>();
    @Getter private final Map<UUID, DuelRequest> sentDuelRequests = new HashMap<>();
    @Getter @Setter private Map<String, UUID> canRateTheArena = new HashMap<>();
    @Getter @Setter private List<Player> follower = new ArrayList<>();
    @Getter private final SettingsMeta settings = new SettingsMeta();
    @Getter static final List<Player> playerList = new ArrayList<>();
    @Getter private final List<Location> plates = new ArrayList<>();
    @Getter private final KitManager kitManager = new KitManager();
    @Getter @Setter private ProfileRematchData rematchData;
    @Getter @Setter private DuelProcedure duelProcedure;
    @Getter @Setter private boolean followMode = false;
    @Getter @Setter private boolean visibility = false;
    @Getter @Setter private QueueProfile queueProfile;
    @Getter @Setter private boolean canRate = false;
    @Getter @Setter private boolean builder = false;
    @Getter @Setter private boolean silent = false;
    @Getter @Setter private MatchState matchState;
    @Getter @Setter int globalUnrankedElo = 1000;
    @Getter @Setter private ProfileState state;
    @Getter @Setter private Brackets brackets;
    @Getter @Setter private Player spectating;
    @Getter @Setter String worldTime = "Day";
    @Getter @Setter private long ratingTimer;
    @Getter @Setter private Player following;
    @Getter @Setter int bestWinStreak = 0;
    @Getter @Setter private Spleef spleef;
    @Getter @Setter int globalElo = 1000;
    @Getter @Setter private Match match;
    @Getter @Setter private Queue queue;
    @Getter @Setter private Party party;
    @Getter @Setter int sumoRounds = 0;
    @Getter @Setter int pingFactor = 0;
    @Getter @Setter int winStreak = 0;
    @Getter @Setter private Sumo sumo;
    @Getter private final UUID uuid;
    @Getter @Setter String name;

    public Profile(UUID uuid) {
        this.uuid = uuid;
        this.state = ProfileState.IN_LOBBY;

        for (Kit kit : Kit.getKits()) {
            this.statisticsData.put(kit, new StatisticsData());
        }

        this.calculateGlobalElo();
        this.calculateGlobalWinStreak();
        this.calculateGlobalUnrankedElo();
    }

    public static void preload() {
        collection = Practice.get().getMongoDatabase().getCollection("profiles");

        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = new Profile(player.getUniqueId());

            try {
                profile.load();
            } catch (Exception e) {
                player.kickPlayer(CC.RED + "The server is loading...");
                continue;
            }

            profiles.put(player.getUniqueId(), profile);
        }

        getEloLeagues().put(1019, "Diamond III");
        getEloLeagues().put(1018, "Diamond III");
        getEloLeagues().put(1017, "Diamond II");
        getEloLeagues().put(1016, "Diamond II");
        getEloLeagues().put(1015, "Diamond I");
        getEloLeagues().put(1014, "Diamond I");
        getEloLeagues().put(1013, "Gold III");
        getEloLeagues().put(1012, "Gold III");
        getEloLeagues().put(1011, "Gold II");
        getEloLeagues().put(1010, "Gold II");
        getEloLeagues().put(1009, "Gold I");
        getEloLeagues().put(1008, "Gold I");
        getEloLeagues().put(1007, "Silver IV");
        getEloLeagues().put(1006, "Silver IV");
        getEloLeagues().put(1005, "Silver III");
        getEloLeagues().put(1004, "Silver III");
        getEloLeagues().put(1003, "Silver II");
        getEloLeagues().put(1002, "Silver II");
        getEloLeagues().put(1001, "Silver I");
        getEloLeagues().put(1000, "Silver I");
        getEloLeagues().put(999, "Bronze V");
        getEloLeagues().put(998, "Bronze V");
        getEloLeagues().put(997, "Bronze IV");
        getEloLeagues().put(996, "Bronze IV");
        getEloLeagues().put(995, "Bronze III");
        getEloLeagues().put(994, "Bronze III");
        getEloLeagues().put(993, "Bronze II");
        getEloLeagues().put(992, "Bronze II");
        getEloLeagues().put(991, "Bronze I");
        getEloLeagues().put(990, "Bronze I");

        eloLeagues = eloLeagues.entrySet().stream().sorted(Map.Entry.<Integer, String>comparingByKey().reversed()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Profile profile : Profile.getProfiles().values()) {
                    profile.save();
                }
            }
        }.runTaskTimerAsynchronously(Practice.get(), 36000L, 36000L);

        Profile.loadAllProfiles();
        new BukkitRunnable() {
            @Override
            public void run() {
                Profile.loadAllProfiles();
                Kit.getKits().forEach(Kit::updateKitLeaderboards);
            }
        }.runTaskTimerAsynchronously(Practice.get(), 600L, 600L);

        new BukkitRunnable() {
            @Override
            public void run() {
                loadGlobalLeaderboards();
                loadGlobalWinStreakleaderboards();
                loadGlobalUnrankedLeaderboards();
            }
        }.runTaskTimerAsynchronously(Practice.get(), 600L, 600L);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Profile profile : Profile.getProfiles().values()) {
                    profile.checkForHotbarUpdate();
                }
            }
        }.runTaskTimerAsynchronously(Practice.get(), 60L, 60L);
    }

    public static Profile getByUuid(UUID uuid) {
        Profile profile = profiles.get(uuid);

        if (profile == null) {
            profile = new Profile(uuid);
        }

        return profile;
    }

    public static Profile getByUuid(Player player) {
        Profile profile = profiles.get(player.getUniqueId());

        if (profile == null) {
            profile = new Profile(player.getUniqueId());
        }

        return profile;
    }

    public static void loadAllProfiles() {
        allProfiles = Practice.get().getMongoDatabase().getCollection("profiles");
    }

    public static void loadGlobalLeaderboards() {
        if (!getGlobalEloLeaderboards().isEmpty()) getGlobalEloLeaderboards().clear();

        for (Document document : Profile.getAllProfiles().find().sort(Sorts.descending("globalElo")).limit(10).into(new ArrayList<>())) {
            KitLeaderboards kitLeaderboards = new KitLeaderboards();
            kitLeaderboards.setName((String) document.get("name"));
            kitLeaderboards.setElo((Integer) document.get("globalElo"));
            getGlobalEloLeaderboards().add(kitLeaderboards);
        }
    }

    public static void loadGlobalWinStreakleaderboards() {
        if (!getGlobalWinStreakLeaderboards().isEmpty()) getGlobalWinStreakLeaderboards().clear();

        for (Document document : Profile.getAllProfiles().find().sort(Sorts.descending("winStreak")).limit(10).into(new ArrayList<>())) {
            KitLeaderboards kitLeaderboards = new KitLeaderboards();
            kitLeaderboards.setName((String) document.get("name"));
            kitLeaderboards.setElo((Integer) document.get("winStreak"));
            getGlobalWinStreakLeaderboards().add(kitLeaderboards);
        }
    }

    public static void loadGlobalUnrankedLeaderboards() {
        if (!getGlobalUnrankedEloLeaderboards().isEmpty()) getGlobalUnrankedEloLeaderboards().clear();

        for (Document document : Profile.getAllProfiles().find().sort(Sorts.descending("globalUnrankedElo")).limit(10).into(new ArrayList<>())) {
            KitLeaderboards kitLeaderboards = new KitLeaderboards();
            kitLeaderboards.setName((String) document.get("name"));
            kitLeaderboards.setElo((Integer) document.get("globalUnrankedElo"));
            getGlobalUnrankedEloLeaderboards().add(kitLeaderboards);
        }
    }

    public void load() {
        Document document = collection.find(Filters.eq("uuid", uuid.toString())).first();

        if (document == null) {
            this.save();
            return;
        }

        this.globalElo = document.getInteger("globalElo");
        this.winStreak = document.getInteger("winStreak");
        this.bestWinStreak = document.getInteger("bestWinStreak");
        this.globalUnrankedElo = document.getInteger("globalUnrankedElo");
        this.worldTime = document.getString("worldTime");
        this.pingFactor = document.getInteger("pingFactor");

        Document options = (Document) document.get("settings");
        this.settings.setReceiveDuelRequests(options.getBoolean("receiveDuelRequests"));
        this.settings.setAllowTournamentMessages(options.getBoolean("allowTournamentMessages"));
        this.settings.setAllowSpectators(options.getBoolean("allowSpectators"));
        this.settings.setShowScoreboard(options.getBoolean("showScoreboard"));

        Document deathEffects = (Document) document.get("deathEffects");
        this.settings.setClearinventory(deathEffects.getBoolean("clearInventory"));
        this.settings.setFlying(deathEffects.getBoolean("toggleFly"));
        this.settings.setLightning(deathEffects.getBoolean("toggleLighting"));
        this.settings.setFireworkeffect(deathEffects.getBoolean("toggleFirework"));
        this.settings.setFlameeffect(deathEffects.getBoolean("toggleFlame"));
        this.settings.setExplosioneffect(deathEffects.getBoolean("toggleExplosion"));
        this.settings.setBloodeffect(deathEffects.getBoolean("toggleBlood"));

        Document matchSettings = (Document) document.get("matchSettings");
        this.settings.setClearBowls(matchSettings.getBoolean("clearBowls"));
        this.settings.setDropbottles(matchSettings.getBoolean("dropBottles"));
        this.settings.setUsingMapSelector(matchSettings.getBoolean("usingMapSelector"));

        Document kitStatistics = (Document) document.get("kitStatistics");
        for (String key : kitStatistics.keySet()) {
            Document kitDocument = (Document) kitStatistics.get(key);
            StatisticsData statisticsData= new StatisticsData();
            Kit kit = Kit.getByName(key);

            if (kit != null) {

                statisticsData.setElo(kitDocument.getInteger("elo"));
                statisticsData.setWinStreak(kitDocument.getInteger("winStreak"));
                statisticsData.setBestWinSterak(kitDocument.getInteger("bestWinStreak"));
                statisticsData.setUnrankedElo(kitDocument.getInteger("unrankedElo"));
                statisticsData.setRankedWon(kitDocument.getInteger("rankedWon"));
                statisticsData.setRankedLost(kitDocument.getInteger("rankedLost"));
                statisticsData.setTournamentWins(kitDocument.getInteger("tournamentWins"));
                statisticsData.setTournamentLost(kitDocument.getInteger("tournamentLost"));

                this.statisticsData.put(kit, statisticsData);
            }
        }

        Document kitsDocument = (Document) document.get("loadouts");
        for (String key : kitsDocument.keySet()) {
            Kit kit = Kit.getByName(key);

            if (kit != null) {
                JsonArray kitsArray = new JsonParser().parse(kitsDocument.getString(key)).getAsJsonArray();
                KitInventory[] loadouts = new KitInventory[4];

                for (JsonElement kitElement : kitsArray) {
                    JsonObject kitObject = kitElement.getAsJsonObject();

                    KitInventory loadout = new KitInventory(kitObject.get("name").getAsString());
                    loadout.setArmor(InventoryUtil.deserializeInventory(kitObject.get("armor").getAsString()));
                    loadout.setContents(InventoryUtil.deserializeInventory(kitObject.get("contents").getAsString()));

                    loadouts[kitObject.get("index").getAsInt()] = loadout;
                }

                statisticsData.get(kit).setLoadouts(loadouts);
            }
        }

    }

    public void save() {
        Document document = new Document();
        document.put("uuid", uuid.toString());
        document.put("name", Bukkit.getOfflinePlayer(uuid).getName());
        document.put("globalElo", globalElo);
        document.put("winStreak", winStreak);
        document.put("bestWinStreak", bestWinStreak);
        document.put("globalUnrankedElo", globalUnrankedElo);
        document.put("worldTime", this.worldTime);
        document.put("pingFactor", this.pingFactor);

        Document optionsDocument = new Document();
        optionsDocument.put("receiveDuelRequests", settings.isReceiveDuelRequests());
        optionsDocument.put("allowTournamentMessages", settings.isAllowTournamentMessages());
        optionsDocument.put("allowSpectators", settings.isAllowSpectators());
        optionsDocument.put("showScoreboard", settings.isShowScoreboard());
        document.put("settings", optionsDocument);

        Document deathEffectsDocument = new Document();
        deathEffectsDocument.put("clearInventory", settings.isClearinventory());
        deathEffectsDocument.put("toggleFly", settings.isFlying());
        deathEffectsDocument.put("toggleLighting", settings.isLightning());
        deathEffectsDocument.put("toggleFirework", settings.isFireworkeffect());
        deathEffectsDocument.put("toggleFlame", settings.isFlameeffect());
        deathEffectsDocument.put("toggleExplosion", settings.isExplosioneffect());
        deathEffectsDocument.put("toggleBlood", settings.isBloodeffect());
        document.put("deathEffects", deathEffectsDocument);

        Document matchSettingsDocument = new Document();
        matchSettingsDocument.put("clearBowls", settings.isClearBowls());
        matchSettingsDocument.put("dropBottles", settings.isDropbottles());
        matchSettingsDocument.put("usingMapSelector", settings.isUsingMapSelector());
        document.put("matchSettings", matchSettingsDocument);

        Document kitStatisticsDocument = new Document();

        for (Map.Entry<Kit, StatisticsData> entry : statisticsData.entrySet()) {
            Document kitDocument = new Document();
            kitDocument.put("elo", entry.getValue().getElo());
            kitDocument.put("winStreak", entry.getValue().getWinStreak());
            kitDocument.put("bestWinStreak", entry.getValue().getBestWinSterak());
            kitDocument.put("unrankedElo", entry.getValue().getUnrankedElo());
            kitDocument.put("rankedWon", entry.getValue().getRankedWon());
            kitDocument.put("rankedLost", entry.getValue().getRankedLost());
            kitDocument.put("tournamentWins", entry.getValue().getTournamentWins());
            kitDocument.put("tournamentLost", entry.getValue().getTournamentLost());
            kitStatisticsDocument.put(entry.getKey().getName(), kitDocument);
        }
        document.put("kitStatistics", kitStatisticsDocument);

        Document kitsDocument = new Document();

        for (Map.Entry<Kit, StatisticsData> entry : statisticsData.entrySet()) {
            JsonArray kitsArray = new JsonArray();

            for (int i = 0; i < 4; i++) {
                KitInventory loadout = entry.getValue().getLoadout(i);

                if (loadout != null) {
                    JsonObject kitObject = new JsonObject();
                    kitObject.addProperty("index", i);
                    kitObject.addProperty("name", loadout.getCustomName());
                    kitObject.addProperty("armor", InventoryUtil.serializeInventory(loadout.getArmor()));
                    kitObject.addProperty("contents", InventoryUtil.serializeInventory(loadout.getContents()));
                    kitsArray.add(kitObject);
                }
            }

            kitsDocument.put(entry.getKey().getName(), kitsArray.toString());
        }

        document.put("loadouts", kitsDocument);

        collection.replaceOne(Filters.eq("uuid", uuid.toString()), document, new ReplaceOptions().upsert(true));
    }

    public void msg(String msg) {
        if (getPlayer() == null) {
            return;
        }
        getPlayer().sendMessage(CC.translate(msg));
    }

    public void addRatingTimer(Player player, String arena) {
        canRateTheArena.put(arena, player.getUniqueId());
        ratingTimer = System.currentTimeMillis();
        if (ratingTimer < System.currentTimeMillis() - 86400000L * 10L) {
            if (canRateTheArena.containsKey(arena)) {
                canRateTheArena.clear();
            }
        }
    }

    public void calculateGlobalElo() {
        int globalElo = 0;
        int kitCounter = 0;
        for (Kit kit : this.statisticsData.keySet()) {
            if (kit.getGameRules().isRanked()) {
                globalElo += this.statisticsData.get(kit).getElo();
                kitCounter++;
            }
        }
        this.globalElo = Math.round(globalElo / kitCounter);
    }

    public void calculateGlobalWinStreak() {
        int winStreak = 0;
        int winStreakCounter = 0;
        for (Kit kit : this.statisticsData.keySet()) {
            if (kit.isEnabled()) {
                winStreak += this.statisticsData.get(kit).getWinStreak();
                winStreakCounter++;
            }
        }
        this.globalElo = Math.round(winStreak / winStreakCounter);
    }

    public void calculateGlobalUnrankedElo() {
        int globalUnrankedElo = 0;
        int kitCounter = 0;
        for (Kit kit : this.statisticsData.keySet()) {
                globalUnrankedElo += this.statisticsData.get(kit).getUnrankedElo();
                kitCounter++;
        }
        this.globalElo = Math.round(globalUnrankedElo / kitCounter);
    }

    public String getUnrankedEloLeague() {
        String toReturn = "&8Bronze 1";
        for (Integer elo : getEloLeagues().keySet()) {
            if (this.globalUnrankedElo >= elo) {
                toReturn = getEloLeagues().get(elo);
                break;
            }
        }
        if (this.globalUnrankedElo >= 1020) toReturn = "&c&lChampion";
        return toReturn;
    }

    public Integer getTotalWins() {
        return this.statisticsData.values().stream().mapToInt(StatisticsData::getRankedWon).sum() + this.statisticsData.values().stream().mapToInt(StatisticsData::getRankedWon).sum();
    }

    public Integer getTotalLost() {
        return this.statisticsData.values().stream().mapToInt(StatisticsData::getRankedLost).sum() + this.statisticsData.values().stream().mapToInt(StatisticsData::getRankedLost).sum();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean canSendDuelRequest(Player player) {
        DuelRequest request = sentDuelRequests.get(player.getUniqueId());

        if (!sentDuelRequests.containsKey(player.getUniqueId())) {
            return false;
        }

        if (request.isExpired()) {
            sentDuelRequests.remove(player.getUniqueId());
            return false;
        } else {
            return true;
        }
    }

    public boolean isPendingDuelRequest(Player player) {
        DuelRequest request = sentDuelRequests.get(player.getUniqueId());

        if (!sentDuelRequests.containsKey(player.getUniqueId())) {
            return false;
        }

        if (request.isExpired()) {
            sentDuelRequests.remove(player.getUniqueId());
            return false;
        } else {
            return true;
        }
    }

    public boolean isInLobby() {
        return state == ProfileState.IN_LOBBY;
    }

    public boolean isManaging() {
        return state == ProfileState.MANAGING;
    }

    public boolean isInFFA() {
        return state == ProfileState.FFA;
    }

    public boolean isInQueue() {
        return state == ProfileState.IN_QUEUE;
    }

    public boolean isInMatch() {
        return match != null;
    }

    public boolean isInFight() {
        return state == ProfileState.IN_FIGHT && match != null;
    }

    public boolean isEnding() {
        return matchState == MatchState.ENDING && match != null;
    }

    public boolean isSpectating() {
        return state == ProfileState.SPECTATE_MATCH && (
                match != null || sumo != null || brackets != null || spleef != null);
    }

    public boolean isInEvent() {
        return state == ProfileState.IN_EVENT;
    }

    public boolean isInTournament(Player player) {
        return false;
    }

    public boolean isInSumo() {
        return state == ProfileState.IN_EVENT && sumo != null;
    }

    public boolean isInBrackets() {
        return state == ProfileState.IN_EVENT && brackets != null;
    }

    public boolean isInSpleef() {
        return state == ProfileState.IN_EVENT && spleef != null;
    }

    public boolean isInSomeSortOfFight() {
        return (state == ProfileState.IN_FIGHT && match != null) || (state == ProfileState.IN_EVENT);

    }

    public boolean isBusy(Player player) {
        return isInQueue() || isInFight() || isInEvent() || isSpectating() || isInTournament(player) || isFollowMode();
    }

    public void checkForHotbarUpdate() {
        Player player = getPlayer();

        if (player == null) {
            return;
        }

        if (isInLobby() && !kitEditor.isActive()) {
            boolean update = false;

            if (this.rematchData != null) {
                final Player target=Bukkit.getPlayer(this.rematchData.getTarget());
                if (System.currentTimeMillis() - this.rematchData.getTimestamp() >= 30000L) {
                    this.rematchData=null;
                    update=true;
                } else if (target == null || !target.isOnline()) {
                    this.rematchData=null;
                    update=true;
                } else {
                    final Profile profile=getByUuid(target.getUniqueId());
                    if (!profile.isInLobby() && !profile.isInQueue()) {
                        this.rematchData=null;
                        update=true;
                    } else if (this.getRematchData() == null) {
                        this.rematchData=null;
                        update=true;
                    } else if (!this.rematchData.getKey().equals(this.getRematchData().getKey())) {
                        this.rematchData=null;
                        update=true;
                    } else if (this.rematchData.isReceive()) {
                        int requestSlot=player.getInventory().first(Hotbar.getItems().get(HotbarType.REMATCH_ACCEPT));
                        if (requestSlot != -1) {
                            update=true;
                        }
                    }
                }

                {
                    boolean activeEvent=(Practice.get().getSumoManager().getActiveSumo() != null && Practice.get().getSumoManager().getActiveSumo().isWaiting())
                            || (Practice.get().getBracketsManager().getActiveBrackets() != null && Practice.get().getBracketsManager().getActiveBrackets().isWaiting())
                            || (Practice.get().getSpleefManager().getActiveSpleef() != null && Practice.get().getSpleefManager().getActiveSpleef().isWaiting());
                    int eventSlot=player.getInventory().first(Hotbar.getItems().get(HotbarType.EVENT_JOIN));

                    if (eventSlot == -1 && activeEvent) {
                        update=true;
                    } else if (eventSlot != -1 && !activeEvent) {
                        update=true;
                    }
                }
            }

            if (update) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        refreshHotbar();
                    }
                }.runTask(Practice.get());
            }
        }
    }

    public void refreshHotbar() {
        Player player = getPlayer();

        if (player != null) {
            PlayerUtil.reset(player, false);

            if (isInLobby()) {
                player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.LOBBY, this));
            } else if (isManaging()) {
                player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.SYSTEMMANAGER, this));
            } else if (isInQueue()) {
                player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.QUEUE, this));
            } else if (isSpectating()) {
                PlayerUtil.spectator(player);
                player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.MATCH_SPECTATE, this));
            } else if (isInSumo()) {
                if (getSumo().getEventPlayer(player).getState().equals(SumoPlayerState.ELIMINATED)) {
                    PlayerUtil.spectator(player);
                }
                player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.SUMO_SPECTATE, this));
            } else if (isInBrackets()) {
                if (getBrackets().getEventPlayer(player).getState().equals(BracketsPlayerState.ELIMINATED)) {
                    PlayerUtil.spectator(player);
                }
                player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.BRACKETS_SPECTATE, this));
        
            } else if (isInSpleef()) {
                if (getSpleef().getEventPlayer(player).getState().equals(SpleefPlayerState.ELIMINATED)) {
                    PlayerUtil.spectator(player);
                }
                player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.SPLEEF_SPECTATE, this));
            } else if (isInFight()) {
                if (!match.getTeamPlayer(player).isAlive()) {
                    player.getInventory().setContents(Hotbar.getLayout(HotbarLayout.MATCH_SPECTATE, this));
                }
            }
            player.updateInventory();
        }
    }

    public String getWLR() {
        DecimalFormat format = new DecimalFormat("#.##");

        double totalWins = this.getTotalWins();
        double totalLosses = this.getTotalLost();
        double ratio = totalWins / Math.max(totalLosses, 1);

        return format.format(ratio);
    }

    public void handleVisibility(Player player, Player otherPlayer) {
        if (player == null || otherPlayer == null) return;

        boolean hide = true;

        if (state == ProfileState.IN_LOBBY || state == ProfileState.IN_QUEUE) {
            hide = !visibility;
            if (visibility) {
                if (!player.hasPermission("practice.seespawnplayers")) {
                    Profile oProfile = getByUuid(otherPlayer);
                    if (oProfile.isSilent()) {
                        hide = true;
                    }
                }
            }

            if (party != null && party.containsPlayer(otherPlayer)) {
                hide = false;
                NameTags.color(player, otherPlayer, ChatColor.DARK_RED, false);
            }
        } else if (isInFight()) {
            TeamPlayer teamPlayer = match.getTeamPlayer(otherPlayer);

            if (teamPlayer != null && teamPlayer.isAlive()) {
                hide = false;
            }
        } else if (isSpectating()) {
            if (sumo != null) {
                SumoPlayer sumoPlayer = sumo.getEventPlayer(otherPlayer);
                if (sumoPlayer != null && sumoPlayer.getState() == SumoPlayerState.WAITING) {
                    hide = false;
                }
            } else if (brackets != null) {
                BracketsPlayer bracketsPlayer = brackets.getEventPlayer(otherPlayer);
                if (bracketsPlayer != null && bracketsPlayer.getState() == BracketsPlayerState.WAITING) {
                    hide = false;
                }
            } else if (spleef != null) {
                SpleefPlayer spleefPlayer = spleef.getEventPlayer(otherPlayer);
                if (spleefPlayer != null && spleefPlayer.getState() == SpleefPlayerState.WAITING) {
                    hide = false;
                }
            } else {
                TeamPlayer teamPlayer = match.getTeamPlayer(otherPlayer);
                if (teamPlayer != null && teamPlayer.isAlive()) {
                    hide = false;
                }
            }
        } else if (isInEvent()) {
            if (sumo != null) {
                if (!sumo.getSpectators().contains(otherPlayer.getUniqueId())) {
                    SumoPlayer sumoPlayer = sumo.getEventPlayer(otherPlayer);
                    if (sumoPlayer != null && sumoPlayer.getState() == SumoPlayerState.WAITING) {
                        hide = false;
                    }
                }
            } else if (brackets != null) {
                BracketsPlayer bracketsPlayer = brackets.getEventPlayer(otherPlayer);
                if (bracketsPlayer != null && bracketsPlayer.getState() == BracketsPlayerState.WAITING) {
                    hide = false;
                }
            } else if (spleef != null) {
                SpleefPlayer spleefPlayer = spleef.getEventPlayer(otherPlayer);
                if (spleefPlayer != null && spleefPlayer.getState() == SpleefPlayerState.WAITING) {
                    hide = false;
                }
            }

        }

        if (hide) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.hidePlayer(otherPlayer);
                }
            }.runTask(Practice.get());
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.showPlayer(otherPlayer);
                }
            }.runTask(Practice.get());
        }
    }

    public void handleVisibility() {
        Player player = getPlayer();

        if (player != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                        handleVisibility(player, otherPlayer);
                    }
                }
            }.runTaskAsynchronously(Practice.get());
        }
    }

    public String getEloLeague() {
        String toReturn = "&8Bronze I";
        for (Integer elo : getEloLeagues().keySet()) {
            if (this.globalElo >= elo) {
                toReturn = getEloLeagues().get(elo);
                break;
            }
        }
        if (this.globalElo >= 1020) toReturn = "&4&lCrown";
        return toReturn;
    }
}
