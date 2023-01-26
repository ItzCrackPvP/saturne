package me.cprox.practice.menu.settings;

import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SettingsMenu extends Menu {
    @Override
    public String getTitle(final Player player) {
        return "&7&lSettings";
    }

    @Override
    public Map<Integer, Button> getButtons(final Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(0, new TournamentMessagesButton());
        buttons.put(1, new AllowSpectatorsButton());
        buttons.put(2, new DuelRequestsButton());
        buttons.put(3, new DisableScoreboardButton());
        buttons.put(4, new WorldTimeButton());
        //buttons.put(5, new GlobalChatButton());
        //buttons.put(6, new PrivateMessagesButton());
        buttons.put(5, new DeathEffectsButton());
        buttons.put(6, new MatchmakingSettingsButton());
        buttons.put(7, new SystemManagerButton());

        return buttons;
    }

    private static class TournamentMessagesButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.PAPER)
                    .name("&7&lTournament Messages: " + (profile.getSettings().isAllowTournamentMessages() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            profile.getSettings().setAllowTournamentMessages(!profile.getSettings().isAllowTournamentMessages());
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class AllowSpectatorsButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.ENDER_PEARL)
                    .name("&7&lAllow Spectators: " + (profile.getSettings().isAllowSpectators() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            profile.getSettings().setAllowSpectators(!profile.getSettings().isAllowSpectators());
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class DuelRequestsButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.DIAMOND_SWORD)
                    .name("&7&lDuel Requests: " + (profile.getSettings().isReceiveDuelRequests() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            profile.getSettings().setReceiveDuelRequests(!profile.getSettings().isReceiveDuelRequests());
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class WorldTimeButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());

            return new ItemBuilder(Material.WATCH)
                    .name("&7&lWorld Time: &f" + profile.getWorldTime())
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            if (profile.getWorldTime().equals("Day")) {
                profile.setWorldTime("Night");
                player.setPlayerTime(20000L, false);
            } else if (profile.getWorldTime().equals("Night")) {
                profile.setWorldTime("Sunset");
                player.setPlayerTime(12500L, false);
            } else {
                profile.setWorldTime("Day");
                player.setPlayerTime(0L, false);
            }
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

/*    private static class GlobalChatButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            PlayerData playerData = ZenoCore.INSTANCE.getPlayerManagement().getPlayerData(player.getUniqueId());
            return new ItemBuilder(Material.CACTUS)
                    .name("&4&lGlobal Chat: " + (playerData.getMessageSystem().isGlobalChat() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to manage!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            PlayerData playerData = ZenoCore.INSTANCE.getPlayerManagement().getPlayerData(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            playerData.getMessageSystem().setGlobalChat(!playerData.getMessageSystem().isGlobalChat());
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class PrivateMessagesButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            PlayerData playerData = ZenoCore.INSTANCE.getPlayerManagement().getPlayerData(player.getUniqueId());
            return new ItemBuilder(Material.GOLD_INGOT)
                    .name("&4&lPrivate Messages: " + (playerData.getMessageSystem().isMessagesToggled() ? "&aEnabled" : "&cDisabled"))
                    .lore(" ")
                    .lore("&7&oClick to manage!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            PlayerData playerData = ZenoCore.INSTANCE.getPlayerManagement().getPlayerData(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 20F, 15F);
            playerData.getMessageSystem().setMessagesToggled(!playerData.getMessageSystem().isMessagesToggled());
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }*/

    private static class DeathEffectsButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(Material.SKULL_ITEM)
                    .name("&7&lDeath Effects")
                    .lore(" ")
                    .lore("&7&oClick to view!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            if (player.hasPermission("practice.deatheffects")) {
                player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);
                new DeathEffectsMenu().openMenu(player);
            } else {
                player.sendMessage(CC.translate("&cYou do not have permission to use this."));
                player.closeInventory();
            }
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class MatchmakingSettingsButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(Material.ENCHANTED_BOOK)
                    .name("&7&lMatchmaking Settings")
                    .lore(" ")
                    .lore("&7&oClick to view!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            if (player.hasPermission("practice.matchmaking")) {
                player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);
                new MatchmakingMenu().openMenu(player);
            } else {
                player.sendMessage(CC.translate("&cYou do not have permission to use this."));
                player.closeInventory();
            }
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class DisableScoreboardButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            Profile profile = Profile.getByUuid(player);
            return new ItemBuilder(Material.ITEM_FRAME)
                    .name("&7&lScoreboard: " + (profile.getSettings().isShowScoreboard() ? CC.translate("&aEnabled") : CC.translate("&cDisabled")))
                    .lore(" ")
                    .lore("&7&oClick to toggle!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            final Profile profile = Profile.getByUuid(player.getUniqueId());
            player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);
            profile.getSettings().setShowScoreboard(!profile.getSettings().isShowScoreboard());
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    private static class SystemManagerButton extends Button {
        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(Material.REDSTONE_COMPARATOR)
                    .name("&7&lSystem Manager")
                    .lore(" ")
                    .lore("&7&oClick to enter!")
                    .build();
        }

        @Override
        public void clicked(final Player player, final ClickType clickType) {
            if (player.hasPermission("practice.manager")) {
                player.closeInventory();
                Profile playerProfile = Profile.getByUuid(player);
                player.playSound(player.getLocation(), Sound.CLICK, 20F, 1F);
                playerProfile.setState(ProfileState.MANAGING);
                player.updateInventory();
                playerProfile.refreshHotbar();
                playerProfile.setBuilder(true);
            } else {
                player.sendMessage(CC.translate("&cYou do not have permission to use this."));
                player.closeInventory();
            }
        }

        @Override
        public boolean shouldUpdate(final Player player, final ClickType clickType) {
            return true;
        }
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }
}