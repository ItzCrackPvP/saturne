package me.cprox.practice.match.task;

import me.cprox.practice.match.Match;
import me.cprox.practice.match.impl.SoloMatch;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.MatchState;
import me.cprox.practice.profile.enums.ProfileState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchWaterCheckTask extends BukkitRunnable {
    private final Match match;

    public MatchWaterCheckTask(Match match) {
        this.match = match;
    }

    @Override
    public void run() {
        if (match == null || match.getAlivePlayers().isEmpty() || match.getAlivePlayers().size() <= 1) {
            return;
        }

        for (Player player : match.getAlivePlayers()) {
            if (player == null || Profile.getByUuid(player.getUniqueId()).getState() != ProfileState.IN_FIGHT) {
                return;
            }

            Block legs = player.getLocation().getBlock();
            Block head = legs.getRelative(BlockFace.UP);
            if (legs.getType() == Material.WATER || legs.getType() == Material.STATIONARY_WATER || head.getType() == Material.WATER || head.getType() == Material.STATIONARY_WATER) {
                if (!(match.getState() == MatchState.ENDING) || !(match.getState() == MatchState.STARTING)) {
                    if (match.getKit().getGameRules().isSumo()) {
                        if (match instanceof SoloMatch) {
                            SoloMatch sumoMatch = (SoloMatch) match;
                            sumoMatch.onWater(player);
                        }/* else {
                            TeamMatch sumoMatch = (TeamMatch) match;
                            sumoMatch.onWater(player);*/
                    }
                }
            }
        }
    }
}
