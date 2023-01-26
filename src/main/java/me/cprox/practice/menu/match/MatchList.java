package me.cprox.practice.menu.match;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import me.cprox.practice.match.Match;
import me.cprox.practice.util.SkullCreator;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MatchList extends Menu {

    @Override
    public String getTitle(Player player) {
        return "&6&lCurrent Matches";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = Maps.newHashMap();
        int x = 1;
        int y = 1;

        for (Match match : Match.getMatches()) {
            buttons.put(getSlot(x++, y), new MatchButton(match));

            if (x == 8) {
                y++;
                x = 1;
            }
        }
        return buttons;
    }

    @AllArgsConstructor
    private static class MatchButton extends Button {
        Match match;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(SkullCreator.itemFromUuid(player.getUniqueId()))
                    .name(CC.GREEN + match.getTeamPlayerA().getUsername() + " &7vs " + CC.RED + match.getTeamPlayerB().getUsername())
                    .lore(Arrays.asList(
                            "",
                            "&6Kit: &b&l" + match.getKit().getName(),
                            "&6Duration: &b&l" + match.getDuration(),
                            "&6State: &b&l" + match.getState(),
                            "",
                            "&7&oLeft-Click to spectate",
                            "&7&oRight-Click to view inventories"))
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType.isLeftClick()) {
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/spectate " + match.getTeamPlayerA());
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
