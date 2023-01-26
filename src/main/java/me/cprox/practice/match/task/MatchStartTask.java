package me.cprox.practice.match.task;

import me.cprox.practice.Practice;
import me.cprox.practice.match.Match;
import me.cprox.practice.profile.enums.MatchState;
import me.cprox.practice.util.chat.CC;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.scheduler.BukkitRunnable;
import me.cprox.practice.util.PlayerUtil;

public class MatchStartTask extends BukkitRunnable {
    private final Match match;
    private int ticks;

    public MatchStartTask(Match match) {
        this.match = match;
    }

    @Override
    public void run() {
        int seconds = 5 - ticks;
        if (match.isEnding()) {
            cancel();
            return;
        }

        if (match.getKit().getGameRules().isSumo() || match.getKit().getGameRules().isBridge() || match.getKit().getGameRules().isBedFight() || match.getKit().getGameRules().isBattleRush() || match.getKit().getGameRules().isPearlFight()) {
            if (seconds == 0) {
                match.getPlayers().forEach(PlayerUtil::allowMovement);
                match.setState(MatchState.FIGHTING);
                match.setStartTimestamp(System.currentTimeMillis());
                match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.STARTED"));
                match.broadcastSound(Sound.NOTE_BASS);
                for (Player player : match.getPlayers()) {
                    player.getInventory().remove(Material.INK_SACK);
                    player.updateInventory();
                    for (Player oPlayer : match.getPlayers()) {
                        if (player.equals(oPlayer))
                            continue;
                        player.showPlayer(oPlayer);
                        oPlayer.showPlayer(player);
                    }
                }
                cancel();
                return;
            }
        } else {
            if (seconds == 0) {
                match.setState(MatchState.FIGHTING);
                match.setStartTimestamp(System.currentTimeMillis());
                match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.STARTED"));
                PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer
                        .a("{\"text\": \"" + "Match Started" + "\",color:" + ChatColor.BOLD.name().toLowerCase() + "\",color:" + ChatColor.GREEN.name().toLowerCase() + "}")
                );
                PacketPlayOutTitle packetPlayOutSubtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer
                        .a("{\"text\": \"" + "Good Luck!" + "\",color:" + ChatColor.GRAY.name().toLowerCase() + "}")
                );
                match.broadcastMessage("");
                match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.DISCLAIMER"));
                match.broadcastMessage("");
                if (match.getKit().getGameRules().isBoxing()) {
                    match.broadcastMessage(CC.translate("&3&lBoxing"));
                    match.broadcastMessage(CC.translate("  &bFirst to 100 hits wins!"));
                }
                match.getPlayers().forEach(player -> {
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutTitle);
                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutSubtitle);
                });

                match.broadcastSound(Sound.LEVEL_UP);
                match.getPlayers().forEach(player -> {
                    player.getInventory().remove(Material.INK_SACK);
                    player.updateInventory();
                });
                cancel();
                return;
            }

        }
        match.broadcastMessage(Practice.get().getMessagesConfig().getString("MATCH.START_TIMER").replace("<seconds>", String.valueOf(seconds)));
        PacketPlayOutTitle packetPlayOutTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer
                .a("{\"text\": \"" + "Match Starting" + "\",color:" + ChatColor.BOLD.name().toLowerCase() + "\",color:" + ChatColor.DARK_RED.name().toLowerCase() + "}")
        );
        PacketPlayOutTitle packetPlayOutSubtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer
                .a("{\"text\": \"" + ChatColor.GRAY + "in " + ChatColor.DARK_RED + seconds + ChatColor.GRAY + "..." + "\",color:" + ChatColor.WHITE.name().toLowerCase() + "}")
        );
        match.getPlayers().forEach(player -> {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutTitle);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutSubtitle);
        });
        match.broadcastSound(Sound.NOTE_PLING);
        ticks++;
    }
}