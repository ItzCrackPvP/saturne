package me.cprox.practice.menu.queue.ranked;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.Match;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.queue.Queue;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.ItemBuilder;
import me.cprox.practice.util.menu.Button;
import me.cprox.practice.util.menu.Menu;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RankedSoloMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.translate("&7&lRanked Solo Queue");
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Button BLACK_PANE = Button.placeholder(Material.STAINED_GLASS_PANE, (byte)15, " ");
        Map<Integer, Button> button = Maps.newHashMap();
        QueueType queueType = QueueType.RANKED;

        int x = 1;
        int y = 1;
        for (Queue queue : Queue.getQueues()) {

            Kit kit = queue.getKit();
            if (!kit.isEnabled()) continue;
            if (!queueType.equals(queue.getType())) continue;
            button.put(getSlot(x++, y), new SelectKitButton(queue));

            if (x == 8) {
                y++;
                x = 1;
            }

            for (int i = 0; i < getSlots(); i++) {
                button.putIfAbsent(i, BLACK_PANE);
            }
        }

        return button;
    }

    @AllArgsConstructor
    private static class SelectKitButton extends Button {

        private final Queue queue;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add("&fPlaying: &7" + Match.getInFights(queue));
            lore.add("&fQueuing: &7" + queue.getPlayers().size());
            lore.add("  ");
            lore.add("&aClick to play!");

            return new ItemBuilder(queue.getKit().getDisplayIcon())
                    .name(queue.getKit().getDisplayName())
                    .lore(lore)
                    .amount(Match.getInFights(queue))
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Profile profile = Profile.getByUuid(player.getUniqueId());

            if (player.hasMetadata("frozen")) {
                player.sendMessage(CC.RED + "You cannot queue while frozen.");
                return;
            }

            if (profile.isBusy(player)) {
                player.sendMessage(CC.RED + "You cannot queue right now.");
                return;
            }

            player.closeInventory();
            queue.addPlayer(player, (profile.getStatisticsData().get(queue.getKit())).getElo());
        }

    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }

    public int getSlots() {
        if (Kit.getRankedKits().size() <= 7) return 27;
        if (Kit.getRankedKits().size() > 7 && Kit.getRankedKits().size() <= 14) return 36;
        if (Kit.getRankedKits().size() > 14 && Kit.getRankedKits().size() <= 21) return 45;
        if (Kit.getRankedKits().size() > 21 && Kit.getRankedKits().size() <= 28) return 54;
        if (Kit.getRankedKits().size() > 28 && Kit.getRankedKits().size() <= 35) return 63;
        if (Kit.getRankedKits().size() > 35 && Kit.getRankedKits().size() <= 42) return 72;
        return 99;
    }
}