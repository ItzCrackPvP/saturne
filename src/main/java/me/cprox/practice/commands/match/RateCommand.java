package me.cprox.practice.commands.match;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class RateCommand extends BaseCommand {

    @Command(name = "arenaratingrateterrible")
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();
        String[] args = commandArgs.getArgs();

        if (args.length == 0) {
            player.sendMessage(CC.translate("&cUsage: /arenaratingrateterrible arena"));
            return;
        }

        Arena arena = Arena.getByName(args[0]);
        player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1.0F, 1.0F);
        assert arena != null;
        Profile.getByUuid(player).addRatingTimer(player, arena.getName());
        //arena.getRatingList().add(1);
        //Arena.calculateRating(arena);
        player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.YOU_VOTED").replace("<arena>", arena.getName()).replace("<rating>", Arena.getArenaRating(arena)));
        player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.RECORDED_VOTE"));
    }

    public static class RateNotBest extends BaseCommand {
        @Command(name = "arenaratingratenotbest")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /arenaratingratenotbest arena"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1.0F, 1.0F);
            assert arena != null;
            Profile.getByUuid(player).addRatingTimer(player, arena.getName());
            //arena.getRatingList().add(2);
            //Arena.calculateRating(arena);
            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.YOU_VOTED").replace("<arena>", arena.getName()).replace("<rating>", Arena.getArenaRating(arena)));
            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.RECORDED_VOTE"));
        }
    }

    public static class RateOkay extends BaseCommand {
        @Command(name = "arenaratingrateokay")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /arenaratingrateokay arena"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1.0F, 1.0F);
            Profile.getByUuid(player).addRatingTimer(player, arena.getName());
            //arena.getRatingList().add(3);
            //Arena.calculateRating(arena);
            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.YOU_VOTED").replace("<arena>", arena.getName()).replace("<rating>", Arena.getArenaRating(arena)));
            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.RECORDED_VOTE"));
        }
    }

    public static class RateGood extends BaseCommand {
        @Command(name = "arenaratingrategood")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /arenaratingrategood arena"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1.0F, 1.0F);
            Profile.getByUuid(player).addRatingTimer(player, arena.getName());
            //arena.getRatingList().add(4);
            //Arena.calculateRating(arena);
            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.YOU_VOTED").replace("<arena>", arena.getName()).replace("<rating>", Arena.getArenaRating(arena)));
            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.RECORDED_VOTE"));
        }
    }

    public static class RateAmazing extends BaseCommand {
        @Command(name = "arenaratingrateamazing")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /arenaratingrateamazing arena"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            player.playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1.0F, 1.0F);
            Profile.getByUuid(player).addRatingTimer(player, arena.getName());
           // arena.getRatingList().add(5);
            //Arena.calculateRating(arena);
            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.YOU_VOTED").replace("<arena>", arena.getName()).replace("<rating>", Arena.getArenaRating(arena)));
            player.sendMessage(Practice.get().getMessagesConfig().getString("MATCH.RECORDED_VOTE"));
        }
    }
}