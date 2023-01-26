package me.cprox.practice.kit;

import com.mongodb.client.model.Sorts;
import lombok.Getter;
import lombok.Setter;
import me.cprox.practice.Practice;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.KitGameRules;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.queue.Queue;
import me.cprox.practice.util.InventoryUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.config.BasicConfigurationFile;
import me.cprox.practice.util.external.ItemBuilder;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Kit {

    @Getter private static final List<Kit> kits = new ArrayList<>();
    public static List<Kit> getEnabledKits() {
        return kits.stream().filter(Kit::isEnabled).collect(Collectors.toList());
    }
    public static List<Kit> getRankedKits() {
        return kits.stream().filter(kit -> kit.getGameRules().isRanked()).collect(Collectors.toList());
    }
    public static List<Kit> getEditableKits() {
        return kits.stream().filter(kit -> kit.getGameRules().isEditable()).collect(Collectors.toList());
    }
    private final String name;
    @Getter private final KitInventory kitInventory = new KitInventory();
    private final KitEditRules editRules = new KitEditRules();
    private final KitGameRules gameRules = new KitGameRules();
    @Setter private Queue unrankedQueue;
    @Setter private Queue rankedQueue;
    @Setter private boolean enabled;
    @Setter private String knockbackProfile;
    @Setter private ItemStack displayIcon;
    @Setter private String displayName;
    @Getter private final List<KitLeaderboards> unrankedEloLeaderboards = new ArrayList<>();
    @Getter private final List<KitLeaderboards> rankedEloLeaderboards = new ArrayList<>();
    @Getter private final List<KitLeaderboards> winStreakLeaderboards = new ArrayList<>();
    @Getter private final List<KitLeaderboards> unrankedWinsLeaderboards = new ArrayList<>();
    @Getter private final List<KitLeaderboards> rankedWinsLeaderboards = new ArrayList<>();
    @Getter private final List<KitLeaderboards> tournamentWinsLeaderboards = new ArrayList<>();

    public Kit(String name) {
        this.name = name;
        this.displayName = CC.DARK_RED + name;
        this.displayIcon = new ItemStack(Material.DIAMOND_CHESTPLATE);
        this.knockbackProfile = "default";
    }

    public void delete() {
        kits.remove(this);
        Queue.getQueues().remove(rankedQueue);
        Queue.getQueues().remove(unrankedQueue);
        Practice.get().getKitsConfig().getConfiguration().set("kits." + getName(), null);
        try {
            Practice.get().getKitsConfig().getConfiguration().save(Practice.get().getKitsConfig().getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void preload() {
        FileConfiguration config = Practice.get().getKitsConfig().getConfiguration();
        for (String key : config.getConfigurationSection("kits").getKeys(false)) {
            String path = "kits." + key;

            Kit kit = new Kit(key);
            kit.setEnabled(config.getBoolean(path + ".enabled"));
            if (config.contains(path + ".display-name")) {
                kit.setDisplayName(CC.translate(config.getString(path + ".display-name")));
            }
            kit.setKnockbackProfile(config.getString(path + ".knockback-profile"));

            kit.setDisplayIcon(new ItemBuilder(Material.valueOf(config.getString(path + ".icon.material")))
                    .durability(config.getInt(path + ".icon.durability"))
                    .build());

            if (config.contains(path + ".loadout.armor")) {
                kit.getKitInventory().setArmor(InventoryUtil.deserializeInventory(config.getString(path + ".loadout.armor")));
            }

            if (config.contains(path + ".loadout.contents")) {
                kit.getKitInventory().setContents(InventoryUtil.deserializeInventory(config.getString(path + ".loadout.contents")));
            }

            if (config.contains(path + ".loadout.effects")) {
                kit.getKitInventory().setEffects(InventoryUtil.deserializeEffects(config.getString(path + ".loadout.effects")));
            }

            kit.getGameRules().setRanked(config.getBoolean(path + ".game-rules.ranked"));
            kit.getGameRules().setEditable(config.getBoolean(path + ".game-rules.editable"));
            kit.getGameRules().setBoxing(config.getBoolean(path + ".game-rules.boxing"));
            kit.getGameRules().setBuild(config.getBoolean(path + ".game-rules.build"));
            kit.getGameRules().setSumo(config.getBoolean(path + ".game-rules.sumo"));
            kit.getGameRules().setCombo(config.getBoolean(path + ".game-rules.combo"));
            kit.getGameRules().setHitDelay(config.getInt(path + ".game-rules.hit-delay"));
            kit.getGameRules().setSpleef(config.getBoolean(path + ".game-rules.spleef"));
            kit.getGameRules().setBridge(config.getBoolean(path + ".game-rules.bridge"));
            kit.getGameRules().setBattleRush(config.getBoolean(path + ".game-rules.battlerush"));
            kit.getGameRules().setBedFight(config.getBoolean(path + ".game-rules.bedwars"));
            kit.getGameRules().setSkywars(config.getBoolean(path + ".game-rules.skywars"));
            kit.getGameRules().setPearlFight(config.getBoolean(path + ".game-rules.pearlfight"));
            kit.getGameRules().setBotfight(config.getBoolean(path + ".game-rules.botfight"));
            kit.getGameRules().setPartyffa(config.getBoolean(path + ".game-rules.partyffa"));
            kit.getGameRules().setPartysplit(config.getBoolean(path + ".game-rules.partysplit"));
            kit.getGameRules().setFfacenter(config.getBoolean(path + ".game-rules.ffacenter"));
            kit.getGameRules().setAntifoodloss(config.getBoolean(path + ".game-rules.antifoodloss"));
            kit.getGameRules().setStickspawn(config.getBoolean(path + ".game-rules.stickspawn"));
            kit.getGameRules().setVoidspawn(config.getBoolean(path + ".game-rules.voidspawn"));
            kit.getGameRules().setWaterkill(config.getBoolean(path + ".game-rules.water-kill"));
            kit.getGameRules().setLavakill(config.getBoolean(path + ".game-rules.lava-kill"));
            kit.getGameRules().setShowhealth(config.getBoolean(path + ".game-rules.showhealth"));
            kit.getGameRules().setNoitems(config.getBoolean(path + ".game-rules.noitems"));
            kit.getGameRules().setTimed(config.getBoolean(path + ".game-rules.timed"));
            kit.getGameRules().setBowhp(config.getBoolean(path + ".game-rules.bow-hp"));
            kit.getEditRules().setAllowPotionFill(config.getBoolean(".edit-rules.allow-potion-fill"));
            if (config.getConfigurationSection(path + ".edit-rules.items") != null) {
                for (String itemKey : config.getConfigurationSection(path + ".edit-rules.items").getKeys(false)) {
                    kit.getEditRules().getEditorItems().add(
                            new ItemBuilder(Material.valueOf(config.getString(path + ".edit-rules.items." + itemKey + ".material")))
                                    .durability(config.getInt(path + ".edit-rules.items." + itemKey + ".durability"))
                                    .amount(config.getInt(path + ".edit-rules.items." + itemKey + ".amount"))
                                    .build());
                }
            }
            kits.add(kit);
        }

        kits.forEach(kit -> {
            if (kit.isEnabled()) {
                kit.setUnrankedQueue(new Queue(kit, QueueType.UNRANKED));
                if (kit.getGameRules().isRanked()) {
                    kit.setRankedQueue(new Queue(kit, QueueType.RANKED));
                }
            }
        });

        Kit.getKits().forEach(Kit::updateKitLeaderboards);
    }

    public static Kit getByName(String name) {
        for (Kit kit : kits) {
            if (kit.getName().equalsIgnoreCase(name)) {
                return kit;
            }
        }

        return null;
    }

    public ItemStack getDisplayIcon() {
        return this.displayIcon.clone();
    }

    public void save() {
        String path = "kits." + name;

        BasicConfigurationFile configFile = Practice.get().getKitsConfig();
        configFile.getConfiguration().set(path + ".enabled", enabled);
        configFile.getConfiguration().set(path + ".display-name", displayName);
        configFile.getConfiguration().set(path + ".knockback-profile", knockbackProfile);
        configFile.getConfiguration().set(path + ".icon.material", displayIcon.getType().name());
        configFile.getConfiguration().set(path + ".icon.durability", displayIcon.getDurability());
        configFile.getConfiguration().set(path + ".loadout.armor", InventoryUtil.serializeInventory(kitInventory.getArmor()));
        configFile.getConfiguration().set(path + ".loadout.contents", InventoryUtil.serializeInventory(kitInventory.getContents()));
        configFile.getConfiguration().set(path + ".loadout.effects", InventoryUtil.serializeEffects(kitInventory.getEffects()));
        configFile.getConfiguration().set(path + ".game-rules.ranked", gameRules.isRanked());
        configFile.getConfiguration().set(path + ".game-rules.editable", gameRules.isEditable());
        configFile.getConfiguration().set(path + ".game-rules.boxing", gameRules.isBoxing());
        configFile.getConfiguration().set(path + ".game-rules.build", gameRules.isBuild());
        configFile.getConfiguration().set(path + ".game-rules.sumo", gameRules.isSumo());
        configFile.getConfiguration().set(path + ".game-rules.combo", gameRules.isCombo());
        configFile.getConfiguration().set(path + ".game-rules.hit-delay", gameRules.getHitDelay());
        configFile.getConfiguration().set(path + ".game-rules.spleef", gameRules.isSpleef());
        configFile.getConfiguration().set(path + ".game-rules.bridge", gameRules.isBridge());
        configFile.getConfiguration().set(path + ".game-rules.battlerush", gameRules.isBattleRush());
        configFile.getConfiguration().set(path + ".game-rules.bedwars", gameRules.isBedFight());
        configFile.getConfiguration().set(path + ".game-rules.skywars", gameRules.isSkywars());
        configFile.getConfiguration().set(path + ".game-rules.pearlfight", gameRules.isPearlFight());
        configFile.getConfiguration().set(path + ".game-rules.botfight", gameRules.isBotfight());
        configFile.getConfiguration().set(path + ".game-rules.partyffa", gameRules.isPartyffa());
        configFile.getConfiguration().set(path + ".game-rules.partysplit", gameRules.isPartysplit());
        configFile.getConfiguration().set(path + ".game-rules.ffacenter", gameRules.isFfacenter());
        configFile.getConfiguration().set(path + ".game-rules.antifoodloss", gameRules.isAntifoodloss());
        configFile.getConfiguration().set(path + ".game-rules.stickspawn", gameRules.isStickspawn());
        configFile.getConfiguration().set(path + ".game-rules.voidspawn", gameRules.isVoidspawn());
        configFile.getConfiguration().set(path + ".game-rules.water-kill", gameRules.isWaterkill());
        configFile.getConfiguration().set(path + ".game-rules.lava-kill", gameRules.isLavakill());
        configFile.getConfiguration().set(path + ".game-rules.showhealth", gameRules.isShowhealth());
        configFile.getConfiguration().set(path + ".game-rules.noitems", gameRules.isNoitems());
        configFile.getConfiguration().set(path + ".game-rules.timed", gameRules.isTimed());
        configFile.getConfiguration().set(path + ".game-rules.bow-hp", gameRules.isBowhp());
        configFile.getConfiguration().set(path + ".edit-rules.allow-potion-fill", editRules.isAllowPotionFill());

        try {
            configFile.getConfiguration().save(configFile.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void updateKitLeaderboards() {

        if (!this.getRankedEloLeaderboards().isEmpty()) this.getRankedEloLeaderboards().clear();
        for (Document document : Profile.getAllProfiles().find().sort(Sorts.descending("kitStatistics." + getName() + ".elo")).limit(10).into(new ArrayList<>())) {
            Document kitStatistics = (Document) document.get("kitStatistics");
            if (kitStatistics.containsKey(getName())) {
                Document kitDocument = (Document) kitStatistics.get(getName());
                KitLeaderboards kitLeaderboards = new KitLeaderboards();
                kitLeaderboards.setName((String) document.get("name"));
                kitLeaderboards.setElo((Integer) kitDocument.get("elo"));
                this.getRankedEloLeaderboards().add(kitLeaderboards);
            }
        }

        if (!this.getWinStreakLeaderboards().isEmpty()) this.getWinStreakLeaderboards().clear();
        for (Document document : Profile.getAllProfiles().find().sort(Sorts.descending("kitStatistics." + getName() + ".winStreak")).limit(10).into(new ArrayList<>())) {
            Document kitStatistics = (Document) document.get("kitStatistics");
            if (kitStatistics.containsKey(getName())) {
                Document kitDocument = (Document) kitStatistics.get(getName());
                KitLeaderboards kitLeaderboards = new KitLeaderboards();
                kitLeaderboards.setName((String) document.get("name"));
                kitLeaderboards.setElo((Integer) kitDocument.get("winStreak"));
                this.getWinStreakLeaderboards().add(kitLeaderboards);
            }
        }

        if (!this.getTournamentWinsLeaderboards().isEmpty()) this.getTournamentWinsLeaderboards().clear();
        for (Document document : Profile.getAllProfiles().find().sort(Sorts.descending("kitStatistics." + getName() + ".tournamentWins")).limit(10).into(new ArrayList<>())) {
            Document kitStatistics = (Document) document.get("kitStatistics");
            if (kitStatistics.containsKey(getName())) {
                Document kitDocument = (Document) kitStatistics.get(getName());
                KitLeaderboards kitLeaderboards = new KitLeaderboards();
                kitLeaderboards.setName((String) document.get("name"));
                kitLeaderboards.setElo((Integer) kitDocument.get("tournamentWins"));
                this.getTournamentWinsLeaderboards().add(kitLeaderboards);
            }
        }

        if (!this.getUnrankedEloLeaderboards().isEmpty()) this.getUnrankedEloLeaderboards().clear();
        for (Document document : Profile.getAllProfiles().find().sort(Sorts.descending("kitStatistics." + getName() + ".unrankedElo")).limit(10).into(new ArrayList<>())) {
            Document kitStatistics = (Document) document.get("kitStatistics");
            if (kitStatistics.containsKey(getName())) {
                Document kitDocument = (Document) kitStatistics.get(getName());
                KitLeaderboards kitLeaderboards = new KitLeaderboards();
                kitLeaderboards.setName((String) document.get("name"));
                kitLeaderboards.setElo((Integer) kitDocument.get("unrankedElo"));
                this.getUnrankedEloLeaderboards().add(kitLeaderboards);
            }
        }

        if (!this.getRankedWinsLeaderboards().isEmpty()) this.getRankedWinsLeaderboards().clear();
        for (Document document : Profile.getAllProfiles().find().sort(Sorts.descending("kitStatistics." + getName() + ".rankedWon")).limit(10).into(new ArrayList<>())) {
            Document kitStatistics = (Document) document.get("kitStatistics");
            if (kitStatistics.containsKey(getName())) {
                Document kitDocument = (Document) kitStatistics.get(getName());
                KitLeaderboards kitLeaderboards = new KitLeaderboards();
                kitLeaderboards.setName((String) document.get("name"));
                kitLeaderboards.setElo((Integer) kitDocument.get("rankedWon"));
                this.getRankedWinsLeaderboards().add(kitLeaderboards);
            }
        }
    }

    public void applyToPlayer(Player paramPlayer) {
        paramPlayer.getInventory().setArmorContents(getKitInventory().getArmor());
        paramPlayer.getInventory().setContents(getKitInventory().getContents());
        paramPlayer.updateInventory();
    }
}
