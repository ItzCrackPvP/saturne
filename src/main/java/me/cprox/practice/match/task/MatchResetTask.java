package me.cprox.practice.match.task;

import me.cprox.practice.match.Match;
import lombok.AllArgsConstructor;
import me.cprox.practice.profile.enums.ArenaType;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class MatchResetTask extends BukkitRunnable {

    private final Match match;

    @Override
    public void run() {
        if (!match.isHCFMatch() ) {
            if (match.getKit().getGameRules().isBridge() || match.getKit().getGameRules().isBedFight() || match.getKit().getGameRules().isPearlFight() || match.getKit().getGameRules().isBattleRush() || match.getKit().getGameRules().isSkywars() || match.getKit().getGameRules().isBuild() && match.getPlacedBlocks().size() > 0) {
                match.getPlacedBlocks().forEach(l -> l.getBlock().setType(Material.AIR));
                match.getPlacedBlocks().clear();
            }
            if (match.getKit().getGameRules().isBridge() || match.getKit().getGameRules().isBedFight() || match.getKit().getGameRules().isPearlFight() || match.getKit().getGameRules().isBattleRush() || match.getKit().getGameRules().isSkywars() || match.getKit().getGameRules().isBuild() && match.getChangedBlocks().size() > 0) {
                match.getChangedBlocks().forEach(blockState -> blockState.getLocation().getBlock().setType(blockState.getType()));
                match.getChangedBlocks().clear();

            }
        }
    }

}
