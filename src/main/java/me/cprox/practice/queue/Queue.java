package me.cprox.practice.queue;

import lombok.Getter;
import me.cprox.practice.Practice;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.external.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class Queue {

    @Getter private static final Map<Kit, Queue> queueMap = new HashMap<>();
    private final LinkedList<QueueProfile> players = new LinkedList<>();
    @Getter private static final List<Queue> queues = new ArrayList<>();
    private final Map<UUID, Long> playerQueueTime = new HashMap<>();
    private final QueueType type;
    private final UUID uuid;
    private final Kit kit;

    public Queue(Kit kit, QueueType type) {
        this.kit = kit;
        this.type = type;
        this.uuid = UUID.randomUUID();
        queues.add(this);
        queueMap.put(kit, this);
    }

    public static Queue getByUuid(UUID uuid) {
        for (Queue queue : queues) {
            if (queue.getUuid().equals(uuid)) {
                return queue;
            }
        }
        return null;
    }

    public String getQueueName() {
        if (type == QueueType.RANKED) {
            return "Ranked " + kit.getName();
        } else if (type == QueueType.UNRANKED) {
            return "Unranked " + kit.getName();
        } else {
            throw new AssertionError();
        }
    }

    public String getDuration(Player player) {
        return TimeUtil.millisToTimer(this.getPlayerQueueTime(player.getUniqueId()));
    }

    public void addPlayer(Player player, int elo) {
        QueueProfile queueProfile = new QueueProfile(player.getUniqueId());
        queueProfile.setElo(elo);
        playerQueueTime.put(player.getUniqueId(), System.currentTimeMillis());
        Profile profile = Profile.getByUuid(player.getUniqueId());
        profile.setQueue(this);
        profile.setQueueProfile(queueProfile);
        profile.setState(ProfileState.IN_QUEUE);

        profile.refreshHotbar();
        player.sendMessage(Practice.get().getMessagesConfig().getString("QUEUE.ADDED").replace("<queue>", this.getQueueName()));
        players.add(queueProfile);
    }

    public void removePlayer(QueueProfile queueProfile) {
        players.remove(queueProfile);

        Player player = Bukkit.getPlayer(queueProfile.getPlayerUuid());

        if (player != null && player.isOnline()) {
            player.sendMessage(Practice.get().getMessagesConfig().getString("QUEUE.REMOVED").replace("<queue>", this.getQueueName()));
        }

        Profile profile = Profile.getByUuid(queueProfile.getPlayerUuid());
        profile.setQueue(null);
        profile.setQueueProfile(null);
        profile.setState(ProfileState.IN_LOBBY);
        PlayerUtil.reset(profile.getPlayer(), false);
        profile.refreshHotbar();
    }

    public long getPlayerQueueTime(UUID uuid) {
        return this.playerQueueTime.get(uuid);
    }

    public QueueType getQueueType() {
        return type;
    }
}
