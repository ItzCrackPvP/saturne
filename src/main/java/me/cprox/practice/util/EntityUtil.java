package me.cprox.practice.util;

import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

public final class EntityUtil {
    private static final Map<EntityType, String> displayNames = new EnumMap(EntityType.class);
    private static int currentFakeEntityId = -1;

    private EntityUtil() {
    }

    public static String getName(EntityType type) {
        return displayNames.get(type);
    }

    public static EntityType parse(String input) {
        Iterator<Map.Entry<EntityType, String>> var1 = displayNames.entrySet().iterator();

        Map.Entry entry;
        do {
            if (!var1.hasNext()) {
                EntityType[] var5 = EntityType.values();
                int var6 = var5.length;

                for(int var3 = 0; var3 < var6; ++var3) {
                    EntityType type = var5[var3];
                    if (input.equalsIgnoreCase(type.toString())) {
                        return type;
                    }
                }

                return null;
            }

            entry = var1.next();
        } while(!((String)entry.getValue()).replace(" ", "").equalsIgnoreCase(input));

        return (EntityType)entry.getKey();
    }

    public static int getFakeEntityId() {
        return currentFakeEntityId--;
    }

    static {
        displayNames.put(EntityType.ARROW, "Arrow");
        displayNames.put(EntityType.BAT, "Bat");
        displayNames.put(EntityType.BLAZE, "Blaze");
        displayNames.put(EntityType.BOAT, "Boat");
        displayNames.put(EntityType.CAVE_SPIDER, "Cave Spider");
        displayNames.put(EntityType.CHICKEN, "Chicken");
        displayNames.put(EntityType.COMPLEX_PART, "Complex Part");
        displayNames.put(EntityType.COW, "Cow");
        displayNames.put(EntityType.CREEPER, "Creeper");
        displayNames.put(EntityType.DROPPED_ITEM, "Item");
        displayNames.put(EntityType.EGG, "Egg");
        displayNames.put(EntityType.ENDER_CRYSTAL, "Ender Crystal");
        displayNames.put(EntityType.ENDER_DRAGON, "Ender Dragon");
        displayNames.put(EntityType.ENDER_PEARL, "Ender Pearl");
        displayNames.put(EntityType.ENDER_SIGNAL, "Ender Signal");
        displayNames.put(EntityType.ENDERMAN, "Enderman");
        displayNames.put(EntityType.EXPERIENCE_ORB, "Experience Orb");
        displayNames.put(EntityType.FALLING_BLOCK, "Falling Block");
        displayNames.put(EntityType.FIREBALL, "Fireball");
        displayNames.put(EntityType.FIREWORK, "Firework");
        displayNames.put(EntityType.FISHING_HOOK, "Fishing Rod Hook");
        displayNames.put(EntityType.GHAST, "Ghast");
        displayNames.put(EntityType.GIANT, "Giant");
        displayNames.put(EntityType.HORSE, "Horse");
        displayNames.put(EntityType.IRON_GOLEM, "Iron Golem");
        displayNames.put(EntityType.ITEM_FRAME, "Item Frame");
        displayNames.put(EntityType.LEASH_HITCH, "Lead Hitch");
        displayNames.put(EntityType.LIGHTNING, "Lightning");
        displayNames.put(EntityType.MAGMA_CUBE, "Magma Cube");
        displayNames.put(EntityType.MINECART, "Minecart");
        displayNames.put(EntityType.MINECART_CHEST, "Chest Minecart");
        displayNames.put(EntityType.MINECART_FURNACE, "Furnace Minecart");
        displayNames.put(EntityType.MINECART_HOPPER, "Hopper Minecart");
        displayNames.put(EntityType.MINECART_MOB_SPAWNER, "Spawner Minecart");
        displayNames.put(EntityType.MINECART_TNT, "TNT Minecart");
        displayNames.put(EntityType.OCELOT, "Ocelot");
        displayNames.put(EntityType.PAINTING, "Painting");
        displayNames.put(EntityType.PIG, "Pig");
        displayNames.put(EntityType.PIG_ZOMBIE, "Zombie Pigman");
        displayNames.put(EntityType.PLAYER, "Player");
        displayNames.put(EntityType.PRIMED_TNT, "TNT");
        displayNames.put(EntityType.SHEEP, "Sheep");
        displayNames.put(EntityType.SILVERFISH, "Silverfish");
        displayNames.put(EntityType.SKELETON, "Skeleton");
        displayNames.put(EntityType.SLIME, "Slime");
        displayNames.put(EntityType.SMALL_FIREBALL, "Fireball");
        displayNames.put(EntityType.SNOWBALL, "Snowball");
        displayNames.put(EntityType.SNOWMAN, "Snowman");
        displayNames.put(EntityType.SPIDER, "Spider");
        displayNames.put(EntityType.SPLASH_POTION, "Potion");
        displayNames.put(EntityType.SQUID, "Squid");
        displayNames.put(EntityType.THROWN_EXP_BOTTLE, "Experience Bottle");
        displayNames.put(EntityType.UNKNOWN, "Custom");
        displayNames.put(EntityType.VILLAGER, "Villager");
        displayNames.put(EntityType.WEATHER, "Weather");
        displayNames.put(EntityType.WITCH, "Witch");
        displayNames.put(EntityType.WITHER, "Wither");
        displayNames.put(EntityType.WITHER_SKULL, "Wither Skull");
        displayNames.put(EntityType.WOLF, "Wolf");
        displayNames.put(EntityType.ZOMBIE, "Zombie");
    }
}
