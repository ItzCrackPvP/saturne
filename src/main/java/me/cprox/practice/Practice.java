package me.cprox.practice;

import me.cprox.practice.arena.Arena;
import me.cprox.practice.adapters.Scoreboard;
import me.cprox.practice.commands.ArenaCommands;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.listeners.*;
import me.cprox.practice.managers.*;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.task.ExpBarRunnable;
import me.cprox.practice.profile.hotbar.Hotbar;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.queue.QueueThread;
import me.cprox.practice.util.Placeholders;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.essentials.Essentials;
import me.cprox.practice.util.events.WorldListener;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.CommandFramework;
import me.cprox.practice.util.menu.InvListener;
import me.cprox.practice.util.menu.MenuListener;
import me.cprox.practice.util.scoreboard.Aether;
import me.cprox.practice.util.scoreboard.AetherOptions;
import me.cprox.practice.util.timer.TimerManager;
import me.cprox.practice.util.timer.event.impl.BridgeArrowTimer;
import me.cprox.practice.util.config.BasicConfigurationFile;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.plugin.java.JavaPlugin;
import com.mongodb.client.MongoDatabase;
import com.mongodb.*;
import org.yaml.snakeyaml.error.YAMLException;

import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

@Getter
public class Practice extends JavaPlugin {

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public static Practice get() {
        return practice;
    }

    private BasicConfigurationFile scoreboardConfig, messagesConfig, eventMessages, arenasConfig, eventsConfig, mainConfig, kitsConfig;
    private CommandFramework commandsHandler;
    private BracketsManager bracketsManager;
    private SpleefManager spleefManager;
    private ArenaCommands arenaCommands;
    private MongoDatabase mongoDatabase;
    private TimerManager timerManager;
    private static Practice practice;
    private ProfileManager profileManager;
    private SumoManager sumoManager;
    private Essentials essentials;

    @Override
    public void onEnable() {
        practice = this;

        mainConfig = new BasicConfigurationFile(this, "config");
        scoreboardConfig = new BasicConfigurationFile(this, "scoreboard");
        messagesConfig = new BasicConfigurationFile(this, "messages");
        eventMessages = new BasicConfigurationFile(this, "eventmessages");
        arenasConfig = new BasicConfigurationFile(this, "arenas");
        eventsConfig = new BasicConfigurationFile(this, "events");
        kitsConfig = new BasicConfigurationFile(this, "kits");
        enableMongo();
        Profile.preload();
        Kit.preload();
        Arena.preload();
 
        Hotbar.preload();
        Match.preload();

        essentials = new Essentials(this);
        commandsHandler = new CommandFramework(this);
        loadCommands();
        timerManager = new TimerManager(this);
        bracketsManager = new BracketsManager();
        profileManager = new ProfileManager();
        spleefManager = new SpleefManager();
        arenaCommands = new ArenaCommands();
        sumoManager = new SumoManager();
        getServer().getScheduler().runTaskTimerAsynchronously(this, new ExpBarRunnable(), 1L, 1L);
        timerManager.registerTimer(new BridgeArrowTimer());

        for (World world : getServer().getWorlds()) {
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("doMobSpawning", "false");
            world.setDifficulty(Difficulty.HARD);
        }

        new Aether(this, new Scoreboard());
        new AetherOptions().hook(true);
        Scoreboard.create();

        new QueueThread().start();
        new Placeholders().register();

        loadListeners();

        Profile.loadGlobalWinStreakleaderboards();
        Profile.loadGlobalUnrankedLeaderboards();
        Profile.loadGlobalLeaderboards();
    }

    public void loadCommands() {
        for (Field field : getClass().getDeclaredFields()) {
            if (BaseCommand.class.isAssignableFrom(field.getType()) && field.getType().getSuperclass() == BaseCommand.class) {
                field.setAccessible(true);
                try {
                    Constructor<?> constructor = field.getType().getDeclaredConstructor();
                    constructor.newInstance();
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadListeners() {
        for (Listener listener : Arrays.asList(
                new ArenaSelectionListener(),
                new HotbarListener(),
                new KitEditorListener(),
                new KitManagerListener(),
                new MatchListener(),
                new PearlCooldownListener(),
                new ProfileListener(),
                new MenuListener(),
                new QueueListener(),
                new SumoListener(),
                new BracketsListener(),
                new SpleefListener(),
                new WorldListener(),
                new InvListener()
        )) {
            Practice.get().getServer().getPluginManager().registerEvents(listener, Practice.get());
        }
    }

    @Override
    public void onDisable() {
        Match.cleanup();
        Arena.getArenas().forEach(Arena::save);
        Kit.getKits().forEach(Kit::save);
        Profile.getProfiles().values().forEach(Profile::save);
        Profile.getPlayerList().clear();
    }

    private void enableMongo() {
        if (getMainConfig().getBoolean("Mongo.URI-Enabled")) {
            Bukkit.getConsoleSender().sendMessage(CC.translate("&aConnecting to the database."));
            MongoClient client = new MongoClient(new MongoClientURI(mainConfig.getString("Mongo.URI")));
            mongoDatabase = client.getDatabase(mainConfig.getString("Mongo.Database"));
        } else {
            Bukkit.getConsoleSender().sendMessage(CC.translate("&aConnecting to the database."));
            if (mainConfig.getBoolean("Mongo.Auth.Enabled")) {
                mongoDatabase = new MongoClient(
                        new ServerAddress(
                                mainConfig.getString("Mongo.Normal.Host"),
                                mainConfig.getInteger("Mongo.Normal.Port")
                        ),
                        MongoCredential.createCredential(
                                mainConfig.getString("Mongo.Normal.Auth.Username"),
                                "admin", mainConfig.getString("Mongo.Normal.Auth.Password").toCharArray()
                        ),
                        MongoClientOptions.builder().build()
                ).getDatabase(mainConfig.getString("Mongo.Normal.Database"));
            } else {
                mongoDatabase = new MongoClient(mainConfig.getString("Mongo.Normal.Host"),
                        mainConfig.getInteger("Mongo.Normal.Port"))
                        .getDatabase(mainConfig.getString("Mongo.Normal.Database"));
            }
        }
    }

    public void shutDown() {
        onDisable();
        Bukkit.getPluginManager().disablePlugin(this);
    }

}