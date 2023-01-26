package me.cprox.practice.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.cprox.practice.menu.match.InventorySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class ProfileManager {
    private final Map<UUID, InventorySnapshot> snapshots = new HashMap<>();

    public void addSnapshot(InventorySnapshot snapshot) {
        this.snapshots.put(snapshot.getSnapshotId(), snapshot);
    }
    private boolean pasting;

    public InventorySnapshot getSnapshot(UUID snapshotId) {
        return this.snapshots.get(snapshotId);
    }
}
