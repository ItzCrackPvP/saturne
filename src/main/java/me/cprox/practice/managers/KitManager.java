package me.cprox.practice.managers;

import lombok.Getter;
import lombok.Setter;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.kit.KitInventory;

@Setter
@Getter
public class KitManager {

    private boolean active;

    private boolean creating;

    private boolean knockback;

    private Kit selectedKit;

    private boolean deleting;

    private boolean renaming;

    private KitInventory selectedKitInventory;

    public boolean isRenaming() {
        return this.renaming;
    }

    public boolean isManagingKnockback() {
        return this.knockback;
    }

    public boolean isCreating() {
        return this.creating;
    }
}
