package me.cprox.practice.queue;

import lombok.Data;

import java.util.UUID;

@Data
public class QueueProfile {

    private long start = System.currentTimeMillis();
    private UUID playerUuid;
    private int range = 25;
    private int ticked;
    private int ping;
    private int elo;

    public QueueProfile(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public void tickRange() {
        ticked++;

        if (ticked >= 3) {
            range += 3;
            ticked = 0;
        }
    }

    public boolean isInRange(int elo) {
        return elo < (this.elo - this.range) || elo > (this.elo + this.range);
    }

    public long getPassed() {
        return System.currentTimeMillis() - this.start;
    }

    public int getMinRange() {
        int min = this.elo - this.range;

        return Math.max(min, 0);
    }

    public int getMaxRange() {
        int max = this.elo + this.range;

        return Math.min(max, 2500);
    }

    public int getMinPingRange() {
        int min = this.ping - this.range;

        return Math.max(min, 50);
    }

    public int getMaxPingRange() {
        int max = this.ping + this.range;

        return Math.min(max, 300);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof QueueProfile && ((QueueProfile) o).getPlayerUuid().equals(this.playerUuid);
    }

}
