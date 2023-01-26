package me.cprox.practice.kit;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
public class KitEditRules {

    @Getter
    private final List<ItemStack> editorItems = new ArrayList<>();
    @Getter
    @Setter
    private boolean allowPotionFill;

}
