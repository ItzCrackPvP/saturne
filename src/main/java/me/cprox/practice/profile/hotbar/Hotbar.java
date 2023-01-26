package me.cprox.practice.profile.hotbar;

import java.util.HashMap;

import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.HotbarType;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.Map;

public class Hotbar
{
    private static Map<HotbarType, ItemStack> items;
    
    public Hotbar() {
        preload();
    }
    
    public static void preload() {
        Hotbar.items.put(HotbarType.QUEUE_JOIN_UNRANKED, new ItemBuilder(Material.IRON_SWORD).name(CC.DARK_RED + "Join Unranked Queue" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.QUEUE_JOIN_RANKED, new ItemBuilder(Material.DIAMOND_SWORD).name(CC.DARK_RED + "Join Ranked Queue" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.QUEUE_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave Queue" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.PARTY_EVENTS, new ItemBuilder(Material.DIAMOND_AXE).name(CC.DARK_RED + "Party Events" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.PARTY_CREATE, new ItemBuilder(Material.NAME_TAG).name(CC.DARK_RED + "Create Party" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.PARTY_DISBAND, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Disband Party" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.PARTY_SETTINGS, new ItemBuilder(Material.ANVIL).name(CC.DARK_RED + "Party Settings" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.PARTY_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave Party" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.PARTY_INFO, new ItemBuilder(Material.PAPER).name(CC.DARK_RED + "Party Information" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.OTHER_PARTIES, new ItemBuilder(Material.REDSTONE_TORCH_ON).name(CC.DARK_RED + "Duel Other Parties" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.LEADERBOARDS_MENU, new ItemBuilder(Material.EMERALD).name(CC.DARK_RED + "Leaderboards" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.MAIN_MENU, new ItemBuilder(Material.SKULL_ITEM).name(CC.DARK_RED + "Main Menu" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.KIT_EDITOR, new ItemBuilder(Material.BOOK).name(CC.DARK_RED + "Kit Editor" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.ARENA_MANAGER, new ItemBuilder(Material.PAPER).name(CC.DARK_RED + "Arena Manager" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.KIT_MANAGER, new ItemBuilder(Material.BOOK).name(CC.DARK_RED + "Arena Manager" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.SPECTATE_MATCH, new ItemBuilder(Material.COMPASS).name(CC.DARK_RED + "Match List" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.SYSTEMMANAGER_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.DARK_RED + "Leave System Manager" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.TELEPORT_SPECTATE, new ItemBuilder(Material.COMPASS).durability(0).name(CC.DARK_RED + "View match players" + CC.GRAY + " (Right Click)").build());
        Hotbar.items.put(HotbarType.SPECTATE_STOP, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.DARK_RED + CC.BOLD + "Stop Spectating" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.EVENT_JOIN, new ItemBuilder(Material.EYE_OF_ENDER).name(CC.DARK_RED + "Event Host" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.SUMO_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave Sumo" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.SKYWARS_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave SkyWars" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.BRACKETS_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave Brackets" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.LMS_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave LMS" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.PARKOUR_SPAWN, new ItemBuilder(Material.ARROW).name(CC.GREEN + "Back to Checkpoint" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.PARKOUR_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave Parkour" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.SPLEEF_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + "Leave Spleef" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.REMATCH_REQUEST, new ItemBuilder(Material.BLAZE_POWDER).name(CC.DARK_RED + "Request Rematch" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.REMATCH_ACCEPT, new ItemBuilder(Material.DIAMOND).name(CC.DARK_RED + "Accept Rematch" + CC.GRAY + " (Right-Click)").build());
        Hotbar.items.put(HotbarType.DEFAULT_KIT, new ItemBuilder(Material.BOOK).name(CC.DARK_RED + "Default Kit" + CC.GRAY + " (Right-Click)").build());
    }
    
    public static ItemStack[] getLayout(final HotbarLayout layout, final Profile profile) {
        final ItemStack[] toReturn = new ItemStack[9];
        Arrays.fill(toReturn, null);
        switch (layout) {
            case LOBBY: {
                if (profile.getParty() == null) {
                    toReturn[0] = Hotbar.items.get(HotbarType.QUEUE_JOIN_UNRANKED);
                    toReturn[1] = Hotbar.items.get(HotbarType.QUEUE_JOIN_RANKED);
                    toReturn[2] = Hotbar.items.get(HotbarType.KIT_EDITOR);
                    if (profile.getRematchData() != null) {
                        toReturn[2] = Hotbar.items.get(HotbarType.REMATCH_REQUEST);
                    }
                    toReturn[4] = Hotbar.items.get(HotbarType.PARTY_CREATE);
                    toReturn[6] = Hotbar.items.get(HotbarType.EVENT_JOIN);
                    toReturn[7] = Hotbar.items.get(HotbarType.LEADERBOARDS_MENU);
                    toReturn[8] = Hotbar.items.get(HotbarType.MAIN_MENU);
                    break;
                }
                if (profile.getParty().isLeader(profile.getUuid())) {
                    toReturn[0] = Hotbar.items.get(HotbarType.PARTY_EVENTS);
                    toReturn[1] = Hotbar.items.get(HotbarType.PARTY_INFO);
                    toReturn[4] = Hotbar.items.get(HotbarType.OTHER_PARTIES);
                    toReturn[6] = Hotbar.items.get(HotbarType.KIT_EDITOR);
                    toReturn[7] = Hotbar.items.get(HotbarType.PARTY_SETTINGS);
                    toReturn[8] = Hotbar.items.get(HotbarType.PARTY_DISBAND);
                    break;
                }
                toReturn[0] = Hotbar.items.get(HotbarType.PARTY_INFO);
                toReturn[4] = Hotbar.items.get(HotbarType.OTHER_PARTIES);
                toReturn[7] = Hotbar.items.get(HotbarType.KIT_EDITOR);
                toReturn[8] = Hotbar.items.get(HotbarType.PARTY_LEAVE);
                break;
            }
            case SYSTEMMANAGER: {
                toReturn[0] = Hotbar.items.get(HotbarType.ARENA_MANAGER);
                toReturn[1] = Hotbar.items.get(HotbarType.KIT_MANAGER);
                toReturn[4] = Hotbar.items.get(HotbarType.SPECTATE_MATCH);
                toReturn[8] = Hotbar.items.get(HotbarType.SYSTEMMANAGER_LEAVE);
                break;
            }
            case QUEUE: {
                toReturn[8] = Hotbar.items.get(HotbarType.QUEUE_LEAVE);
                break;
            }
            case SUMO_SPECTATE: {
                toReturn[8] = Hotbar.items.get(HotbarType.SUMO_LEAVE);
                break;
            }
            case BRACKETS_SPECTATE: {
                toReturn[8] = Hotbar.items.get(HotbarType.BRACKETS_LEAVE);
                break;
            }
            case SKYWARS_SPECTATE: {
                toReturn[8] = Hotbar.items.get(HotbarType.SKYWARS_LEAVE);
                break;
            }
            case LMS_SPECTATE: {
                toReturn[8] = Hotbar.items.get(HotbarType.LMS_LEAVE);
                break;
            }
            case PARKOUR_SPECTATE: {
                toReturn[0] = Hotbar.items.get(HotbarType.PARKOUR_SPAWN);
                toReturn[8] = Hotbar.items.get(HotbarType.PARKOUR_LEAVE);
                break;
            }
            case SPLEEF_SPECTATE: {
                toReturn[8] = Hotbar.items.get(HotbarType.SPLEEF_LEAVE);
                break;
            }
            case MATCH_SPECTATE: {
                toReturn[0] = Hotbar.items.get(HotbarType.TELEPORT_SPECTATE);
                toReturn[8] = Hotbar.items.get(HotbarType.SPECTATE_STOP);
                break;
            }
        }
        return toReturn;
    }
    
    public static HotbarType fromItemStack(final ItemStack itemStack) {
        for (final Map.Entry<HotbarType, ItemStack> entry : getItems().entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(itemStack)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    public static Map<HotbarType, ItemStack> getItems() {
        return Hotbar.items;
    }
    
    static {
        Hotbar.items = new HashMap<>();
    }
}
