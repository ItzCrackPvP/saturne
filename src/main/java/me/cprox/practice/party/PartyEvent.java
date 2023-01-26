package me.cprox.practice.party;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public enum PartyEvent {

    FFA("FFA", "Let your party members fight for themselves", Material.REDSTONE_TORCH_ON),
    SPLIT("Split", "Split your party in 2 teams and fight!", Material.DIAMOND_SWORD);

    private final String name;
    private final String lore;
    private final Material material;

}
