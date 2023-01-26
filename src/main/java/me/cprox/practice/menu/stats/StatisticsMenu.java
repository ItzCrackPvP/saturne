package me.cprox.practice.menu.stats;

import me.cprox.practice.kit.Kit;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsMenu extends Menu {
    private final Player target;

    @Override
    public String getTitle(final Player player) {
        return "&7&lStats for " + target.getName();
    }

    @ConstructorProperties ({"target"})
    public StatisticsMenu(final Player target) {
        this.target = target;
    }

    @Override
    public Map<Integer, Button> getButtons(final Player player) {
        Button BLACK_PANE = Button.placeholder(Material.STAINED_GLASS_PANE, (byte)15, " ");
        Map<Integer, Button> button = new HashMap<>();

        button.put(4, new GlobalLeaderboardsButton());
        int y = 2;
        int x = 1;

        for (Kit kit : Kit.getKits()) {
            if (!kit.isEnabled()) continue;
            button.put(getSlot(x++, y), new KitLeaderboardsButton(kit));
            if (x == 8) {
                y++;
                x = 1;
            }
        }

        for (int i = 0; i < getSlots(); i++) {
            button.putIfAbsent(i, BLACK_PANE);
            if (getSlots() == 45) {
                button.put(getSlot(2, 4), new UnrankedLeaderboardsButton());
                button.put(getSlot(3, 4), new WinStreakLeaderboardsButton());
                button.put(getSlot(4, 4), new StatisticsButton());
                button.put(getSlot(5, 4), new RankedLeaderboardsButton());
                button.put(getSlot(6, 4), new TournamentLeaderboardsButton());
            } else if (getSlots() == 54) {
                button.put(getSlot(2, 5), new UnrankedLeaderboardsButton());
                button.put(getSlot(3, 5), new WinStreakLeaderboardsButton());
                button.put(getSlot(4, 5), new StatisticsButton());
                button.put(getSlot(5, 5), new RankedLeaderboardsButton());
                button.put(getSlot(6, 5), new TournamentLeaderboardsButton());
            } else if (getSlots() == 63) {
                button.put(getSlot(2, 6), new UnrankedLeaderboardsButton());
                button.put(getSlot(3, 6), new WinStreakLeaderboardsButton());
                button.put(getSlot(4, 6), new StatisticsButton());
                button.put(getSlot(5, 6), new RankedLeaderboardsButton());
                button.put(getSlot(6, 6), new TournamentLeaderboardsButton());
            } else if (getSlots() == 72) {
                button.put(getSlot(2, 7), new UnrankedLeaderboardsButton());
                button.put(getSlot(3, 7), new WinStreakLeaderboardsButton());
                button.put(getSlot(4, 7), new StatisticsButton());
                button.put(getSlot(5, 7), new RankedLeaderboardsButton());
                button.put(getSlot(6, 7), new TournamentLeaderboardsButton());
            } else if (getSlots() == 81) {
                button.put(getSlot(2, 8), new UnrankedLeaderboardsButton());
                button.put(getSlot(3, 8), new WinStreakLeaderboardsButton());
                button.put(getSlot(4, 8), new StatisticsButton());
                button.put(getSlot(5, 8), new RankedLeaderboardsButton());
                button.put(getSlot(6, 8), new TournamentLeaderboardsButton());
            } else if (getSlots() == 90) {
                button.put(getSlot(2, 9), new UnrankedLeaderboardsButton());
                button.put(getSlot(3, 9), new WinStreakLeaderboardsButton());
                button.put(getSlot(4, 9), new StatisticsButton());
                button.put(getSlot(5, 9), new RankedLeaderboardsButton());
                button.put(getSlot(6, 9), new TournamentLeaderboardsButton());
            }
        }
        return button;
    }

    private static class KitLeaderboardsButton extends Button {
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(final Player player) {
            List<String> lore = new ArrayList<>();
            Profile profile = Profile.getByUuid(player.getUniqueId());
            String elo = kit.getGameRules().isRanked() ? Integer.toString(profile.getStatisticsData().get(kit).getElo()) : "N/A";
            lore.add("&7&lUnranked");
            lore.add(" &7Wins: &f" + profile.getStatisticsData().get(kit).getUnrankedWon());
            lore.add(" &7Title: &f" + profile.getUnrankedEloLeague());
            lore.add("");
            lore.add(" &7Current Streak: &f" + profile.getStatisticsData().get(kit).getWinStreak());
            lore.add(" &7Best Streak: &f" + profile.getStatisticsData().get(kit).getBestWinSterak());
            lore.add(" &7Best Daily Streak: &f");
            lore.add("");
            lore.add("&7&lRanked");
            lore.add(" &7ELO: &f" + elo);
            lore.add("  &7Peak: &f" + elo);
            lore.add(" &7Wins: &f" + profile.getStatisticsData().get(kit).getRankedWon());
            lore.add(" &7Losses: &f" + profile.getStatisticsData().get(kit).getRankedLost());
            lore.add(" &7W/L Ratio: &f" + profile.getWLR());
            lore.add("");
            lore.add("&7&lTournament");
            lore.add(" &7Wins: &f" + profile.getStatisticsData().get(kit).getTournamentWins());
            lore.add(" &7Losses: &f" + profile.getStatisticsData().get(kit).getTournamentLost());

            return new ItemBuilder(kit.getDisplayIcon())
                    .name("&7&l" + kit.getName())
                    .lore(lore)
                    .build();
        }

        @ConstructorProperties({"kit"})
        public KitLeaderboardsButton(final Kit kit) {
            this.kit = kit;
        }
    }

    private static class GlobalLeaderboardsButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            final List<String> lore = new ArrayList<>();
            Profile profile = Profile.getByUuid(player.getUniqueId());
            lore.add("&7&lRanked");
            lore.add(" &7ELO: &f" + profile.getGlobalElo());
            lore.add(" &7Wins: &f" + profile.getTotalWins());
            lore.add(" &7Losses: &f" + profile.getTotalLost());
            lore.add(" &7W/L Ratio: &f" + profile.getWLR());
            lore.add("");
            lore.add("&7&lUnranked");
            lore.add(" &7Best Streak: &f");
            lore.add(" &7Best Daily Streak: &f");
            lore.add("");
            lore.add("&7&lFFA");
            lore.add(" &7Kills: &f");
            lore.add(" &7Deaths: &f");
            return new ItemBuilder(Material.NETHER_STAR)
                    .durability(0)
                    .name("&7&lGlobal")
                    .lore(lore)
                    .build();
        }
    }

    private static class UnrankedLeaderboardsButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(Material.CARPET)
                    .durability(14)
                    .name("&7&lUnranked Leaderboards")
                    .lore(" ")
                    .lore("&fClick to view!")
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            new UnrankedLeaderboards().openMenu(player);
        }
    }

    private static class WinStreakLeaderboardsButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(Material.CARPET)
                    .durability(14)
                    .name("&7&lWinStreak Leaderboards")
                    .lore(" ")
                    .lore("&fClick to view!")
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            new WinStreakLeaderboards().openMenu(player);
        }
    }

    private static class StatisticsButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(Material.PAPER)
                    .name("&7&lYour Stats")
                    .lore(" ")
                    .lore("&7&oYou are here!")
                    .build();
        }
    }

    private static class RankedLeaderboardsButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(Material.CARPET)
                    .durability(14)
                    .name("&7&lRanked Leaderboards")
                    .lore(" ")
                    .lore("&fClick to view!")
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            new RankedLeaderboards().openMenu(player);
        }
    }

    private static class TournamentLeaderboardsButton extends Button {

        @Override
        public ItemStack getButtonItem(final Player player) {
            return new ItemBuilder(Material.CARPET)
                    .durability(14)
                    .name("&7&lTournament Leaderboards")
                    .lore(" ")
                    .lore("&fClick to view!")
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            new TournamentLeaderboards().openMenu(player);
        }
    }

    public int getSlots() {
        if (Kit.getEnabledKits().size() <= 7) return 45;
        if (Kit.getEnabledKits().size() > 7 && Kit.getEnabledKits().size() <= 14) return 54;
        if (Kit.getEnabledKits().size() > 14 && Kit.getEnabledKits().size() <= 21) return 63;
        if (Kit.getEnabledKits().size() > 21 && Kit.getEnabledKits().size() <= 28) return 72;
        if (Kit.getEnabledKits().size() > 28 && Kit.getEnabledKits().size() <= 35) return 81;
        if (Kit.getEnabledKits().size() > 35 && Kit.getEnabledKits().size() <= 42) return 90;
        return 54;
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }
}
