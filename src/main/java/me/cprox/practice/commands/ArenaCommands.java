package me.cprox.practice.commands;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.arena.impl.SharedArena;
import me.cprox.practice.arena.impl.StandaloneArena;
import me.cprox.practice.arena.selection.Selection;
import me.cprox.practice.arena.selection.StandalonePasteRunnable;
import me.cprox.practice.commands.event.*;
import me.cprox.practice.commands.match.*;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.profile.enums.ArenaType;
import me.cprox.practice.util.TaskUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.chat.ChatHelper;
import me.cprox.practice.util.external.ChatComponentBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import me.cprox.practice.util.external.BaseCommand;
import me.cprox.practice.util.external.Command;
import me.cprox.practice.util.external.CommandArgs;
import org.bukkit.entity.Player;
import org.bukkit.material.Bed;

import java.util.Arrays;

public class ArenaCommands extends BaseCommand {
    public ArenaCommands() {
        new ArenaAddKitCommand();
        new ArenaCreateCommand();
        new ArenaDeleteCommand();
        new ArenaGenerateCommand();
        new ArenaKitsCommand();
        new ArenaListCommand();
        new ArenaRemoveKitCommand();
        new ArenaSaveCommand();
        new ArenaSetDisplayNameCommand();
        new ArenaSetIconCommand();
        new ArenaSetPortalCommand();
        new ArenaSetBedCommand();
        new ArenaSetSpawnCommand();
        new ArenaSetMinCommand();
        new ArenaSetMaxCommand();
        new ArenaStatusCommand();
        new ArenaTpCommand();
        new ArenaWandCommand();
        new DuelCommands.DuelAcceptCommand();
        new DuelCommands();
        new DuelCommands.DuelDenyCommand();
        new BracketsCommands.BracketsCancelCommand();
        new BracketsCommands.BracketsCooldownCommand();
        new BracketsCommands.BracketsForceStartCommand();
        new BracketsCommands();
        new BracketsCommands.BracketsHostCommand();
        new BracketsCommands.BracketsJoinCommand();
        new BracketsCommands.BracketsLeaveCommand();
        new BracketsCommands.BracketsSetSpawnCommand();
        new SpleefCommands.SpleefCancelCommand();
        new SpleefCommands.SpleefCooldownCommand();
        new SpleefCommands.SpleefForceStartCommand();
        new SpleefCommands();
        new SpleefCommands.SpleefHostCommand();
        new SpleefCommands.SpleefJoinCommand();
        new SpleefCommands.SpleefLeaveCommand();
        new SpleefCommands.SpleefSetSpawnCommand();
        new SpleefCommands.SpleefTpCommand();
        new SumoCommands.SumoCancelCommand();
        new SumoCommands.SumoCooldownCommand();
        new SumoCommands.SumoForceStartCommand();
        new SumoCommands();
        new SumoCommands.SumoHostCommand();
        new SumoCommands.SumoJoinCommand();
        new SumoCommands.SumoLeaveCommand();
        new SumoCommands.SumoSetSpawnCommand();
        new SumoCommands.SumoTpCommand();
        new EventCommands();
        new EventCommands.EventHelpCommand();
        new KillCommand();
        new SpectateCommand();
        new StopSpectatingCommand();
        new ViewInventoryCommand();
        new RateCommand();
        new RateCommand.RateNotBest();
        new RateCommand.RateOkay();
        new RateCommand.RateGood();
        new RateCommand.RateAmazing();
        new PracticeCommands();
        new ResetStatsCommand();
        new PracticeCommands.SetLobbyCommand();
        new PracticeCommands.SettingsCommand();
        new PracticeCommands.ToggleDuelRequestsCommand();
        new PracticeCommands.ToggleSidebarCommand();
        new PracticeCommands.SaveCommand();
        new StaffCommands();
        new StaffCommands.FollowCommand();
        new StaffCommands.UnFollowCommand();
        new LeaderboardsCommands();
        new LeaderboardsCommands.StatsCommand();
        new PartyCommands();
        new PartyCommands.PartyBanCommand();
        new PartyCommands.PartyInfoCommand();
        new PartyCommands.PartyCreateCommand();
        new PartyCommands.PartyDisbandCommand();
        new PartyCommands.PartyInfoCommand();
        new PartyCommands.PartyCloseCommand();
        new PartyCommands.PartyInviteCommand();
        new PartyCommands.PartyUnBanCommand();
        new PartyCommands.PartyOpenCommand();
        new PartyCommands.PartyKickCommand();
        new PartyCommands.PartyMakeLeaderCommand();
        new PartyCommands.PartyLeaveCommand();
        new PartyCommands.PartyJoinCommand();
    }

    @Command(name = "arena", permission = "arena.commands.help", inGameOnly = false)
    public void onCommand(CommandArgs commandArgs) {
        CommandSender player = commandArgs.getSender();

        player.sendMessage(CC.translate("&4&lArena Commands"));
        player.sendMessage(CC.translate(" &4/arena create &8<&4arena&8> &8<&4shared&8/&4standalone&8> &7 - &fCreate an arena"));
        player.sendMessage(CC.translate(" &4/arena delete &8<&4arena&8> &7 - &fDelete an arena"));
        player.sendMessage(CC.translate(" &4/arena list &7 - &fView arena list"));
        player.sendMessage(CC.translate(" &4/arena kits &8<&4arena&8> &7 - &fView arena kits"));
        player.sendMessage(CC.translate(" &4/arena setdisplayname &7 - &fSet new displayname of the arena"));
        player.sendMessage(CC.translate(" &4/arena setmin &8<&4arena&8> &7 - &fSet the arena minimum location"));
        player.sendMessage(CC.translate(" &4/arena setmax &8<&4arena&8> &7 - &fSet the arena maximum location"));
        player.sendMessage(CC.translate(" &4/arena seticon &8<&4arena&8> &7 - &fSet the arena icon"));
        player.sendMessage(CC.translate(" &4/arena setspawn &8<&4arena&8> &8<&c1&8/&92&8> &7 - &fSet the arena spawns"));
        player.sendMessage(CC.translate(" &4/arena addkit &8<&4arena&8> &8<&4kit&8> &7 - &fAdd a kit into the arena"));
        player.sendMessage(CC.translate(" &4/arena removekit &8<&4arena&8> <&4kit&8> &7 - &fRemove a kit from the arena"));
        player.sendMessage(CC.translate(" &4/arena disablepearls &7 - &fEnable or disable pearls on the arena"));
        player.sendMessage(CC.translate(" &4/arena setportal &8<&4arena&8> &8<&cRed&8/&9Blue&8> &7 - &fSet the arena portals"));
        player.sendMessage(CC.translate(" &4/arena wand &7 - &fRecieve the arena portal selector"));
        player.sendMessage(CC.translate(" &4/arena manager &7 - &fOpen the arena manager menu"));
        player.sendMessage(CC.translate(" &4/arena status &7 - &fView arena details"));
        player.sendMessage(CC.translate(" &4/arena teleport &7 - &fTeleport to the arena"));
        player.sendMessage(CC.translate(" &4/arena save &7 - &fSave all arenas"));
        player.sendMessage(CC.translate("&4&m--------&8&m-------------------------------------&4&m--------"));
    }

    public static class ArenaAddKitCommand extends BaseCommand {

        @Command(name = "arena.addkit", permission = "arena.commands.addkit", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            String[] args = commandArgs.getArgs();
            CommandSender sender = commandArgs.getSender();

            if (args.length == 0) {
                sender.sendMessage(CC.translate("&cUsage: /arena addkit <arena> <kit>"));
                return;
            }

            if (args.length >= 3 ) {
                sender.sendMessage(CC.translate("&cUsage: /arena addkit <arena> <kit>"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            if (arena == null) {
                sender.sendMessage(CC.translate("&cArena " + args[0] + " does not exist."));
                return;
            }

            if (arena.getSpawn1() == null || arena.getSpawn2() == null) {
                sender.sendMessage(CC.translate("&cYou should set arena spawns first!"));
                return;
            }

            Kit kit = Kit.getByName(args[1]);
            if (kit == null) {
                sender.sendMessage(CC.translate("&cKit " + args[1] + " does not exist."));
                return;
            }

            if (arena.getType() == ArenaType.SHARED && kit.getGameRules().isBuild()) {
                sender.sendMessage(CC.translate("&cArena " + args[0] + " is shared type and you can't add build kits into it!"));
                return;
            }

            if (!arena.getKits().contains(kit.getName()))
                arena.getKits().add(kit.getName());

            arena.save();
            sender.sendMessage(CC.translate("&aThe kit " + args[1] + " has been added into the " + args[0]));
        }
    }

        public static class ArenaSetMinCommand extends BaseCommand {

        @Command(name = "arena.setmin", permission = "arena.commands.setmin", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            String[] args = commandArgs.getArgs();
            Player player = commandArgs.getPlayer();
            CommandSender sender = commandArgs.getSender();

            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            if (args.length == 0) {
                sender.sendMessage(CC.translate("&cUsage: /arena setmin <arena>"));
                return;
            }

            if (args.length >= 2 ) {
                sender.sendMessage(CC.translate("&cUsage: /arena setmin <arena>"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            if (arena == null) {
                sender.sendMessage(CC.translate("&cArena " + args[0] + " does not exist."));
                return;
            }
            
            if (arena.getSpawn1() == null || arena.getSpawn2() == null) {
                sender.sendMessage(CC.translate("&cYou should set arena spawns first!"));
                return;
            }

            arena.setMin(player.getLocation());

            arena.save();
            sender.sendMessage(CC.translate("&aSuccessfully set minimum of the arena."));
        }
    }

        public static class ArenaSetMaxCommand extends BaseCommand {

        @Command(name = "arena.setmax", permission = "arena.commands.setmax", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            String[] args = commandArgs.getArgs();
            Player player = commandArgs.getPlayer();
            CommandSender sender = commandArgs.getSender();

            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            if (args.length == 0) {
                sender.sendMessage(CC.translate("&cUsage: /arena setmax <arena>"));
                return;
            }

            if (args.length >= 2 ) {
                sender.sendMessage(CC.translate("&cUsage: /arena setmax <arena>"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            if (arena == null) {
                sender.sendMessage(CC.translate("&cArena " + args[0] + " does not exist."));
                return;
            }

            if (arena.getSpawn1() == null || arena.getSpawn2() == null) {
                sender.sendMessage(CC.translate("&cYou should set arena spawns first!"));
                return;
            }

            arena.setMax(player.getLocation());

            arena.save();
            sender.sendMessage(CC.translate("&aSuccessfully set maximum of the arena."));
        }
    }

    public static class ArenaCreateCommand extends BaseCommand {

        @Command(name = "arena.create", permission = "arena.commands.create", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();
            CommandSender sender = commandArgs.getSender();

            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            if (args.length == 0) {
                sender.sendMessage(CC.translate("&cUsage: /arena create <arena> <type>"));
                return;
            }

            if (args.length >= 3) {
                sender.sendMessage(CC.translate("&cUsage: /arena create <arena> <type>"));
                return;
            }

            String arenaName = args[0];
            if (arenaName == null) {
                sender.sendMessage(CC.translate("&cPlease provide an arena name."));
                return;
            }

            ArenaType arenaType = Arrays.stream(ArenaType.values()).filter(val -> val.name().equalsIgnoreCase(args[1])).findFirst().orElse(null);
            if (arenaType == null) {
                player.sendMessage(CC.translate("&cPlease use : Shared, Standalone"));
                return;
            }
            if (Arena.getByName(arenaName) == null) {
                Selection selection = Selection.createOrGetSelection(player);
                if (selection.isFullObject()) {
                    if (arenaType == ArenaType.STANDALONE) {
                        StandaloneArena standaloneArena = new StandaloneArena(arenaName, selection.getPoint1(), selection.getPoint2());
                        Arena.getArenas().add(standaloneArena);
                        player.sendMessage("&aCreated new Standalone " + args[0] + " arena.");
                    } else {
                        SharedArena sharedArena = new SharedArena(arenaName, selection.getPoint1(), selection.getPoint2());
                        Arena.getArenas().add(sharedArena);
                        player.sendMessage("&aCreated new Shared " + args[0] + " arena.");
                    }
                } else {
                    player.sendMessage(CC.RED + "Your selection is incomplete.");
                }
            } else {
                player.sendMessage(CC.RED + "An arena with that name already exists.");
            }
        }
    }

    public static class ArenaDeleteCommand extends BaseCommand {
        @Command(name = "arena.delete", permission = "arena.commands.delete")
        public void onCommand(CommandArgs commandArgs) {
            String[] args = commandArgs.getArgs();
            CommandSender sender = commandArgs.getSender();

            if (args.length == 0) {
                sender.sendMessage(CC.translate("&cUsage: /arena delete <arena>"));
                return;
            }

            if (args.length >= 2) {
                sender.sendMessage(CC.translate("&cUsage: /arena delete <arena>"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            if (arena != null) {
                arena.delete();
                Arena.getArenas().remove(arena);
                Arena.getArenas().forEach(Arena::save);
                sender.sendMessage(CC.translate("&cArena " + args[0] + " has been deleted."));
            } else {
                sender.sendMessage(CC.translate("&cArena " + args[0] + " does not exist."));
            }
        }
    }

    public static class ArenaGenerateCommand extends BaseCommand {
        @Command(name = "arena.generate", permission = "arena.commands.generate")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (!Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit") && !Bukkit.getPluginManager().isPluginEnabled("WorldEdit") ) {
                player.sendMessage(CC.translate("&7World Edit or FAWE not found, Arena Generating will not work!"));
                return;
            }

            String amount = args[1];

            if (Integer.parseInt(amount) >= 10) {
                player.sendMessage(CC.translate("&7That amount is too high, you can only place &c9 &7arenas at a time due to performance issues."));
                return;
            }

            if (plugin.getProfileManager().isPasting()) {
                player.sendMessage(CC.translate("&7Arenas are already generating, please wait!"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);

            assert arena != null;
            if (arena.getType() == ArenaType.SHARED || arena.isDuplicate()) {
                player.sendMessage(CC.translate("&7You can't generate Shared arena!"));
                return;
            }

            if (!arena.isSetup()) {
                player.sendMessage(CC.translate("&7Please finish your arena before generating!"));
                return;
            }

            plugin.getProfileManager().setPasting(true);
            TaskUtil.run(new StandalonePasteRunnable(plugin, (StandaloneArena) arena, Integer.parseInt(amount)));

            player.sendMessage(CC.translate("&7Pasting...."));
            Arena.getArenas().forEach(Arena::save);
        }

    }

    public static class ArenaKitsCommand extends BaseCommand {
        @Command(name = "arena.kits", permission = "arena.commands.kits", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            String[] args = commandArgs.getArgs();
            CommandSender sender = commandArgs.getSender();

            if (args.length == 0) {
                sender.sendMessage(CC.translate("&cUsage: /arena kits <arena>"));
                return;
            }

            if (args.length >= 2) {
                sender.sendMessage(CC.translate("&cUsage: /arena kits <arena>"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);

            if (arena == null) {
                sender.sendMessage(CC.translate("&cArena " + args[0] + " does not exist."));
            }

            sender.sendMessage(CC.translate("&4&l" + args[0] + "&7's kits"));
            sender.sendMessage(CC.translate(""));
            if (arena != null) {
            for (String string : arena.getKits()) {
                Kit kit = Kit.getByName(string);
                if (kit == null) {
                    sender.sendMessage("");
                    sender.sendMessage(CC.translate("There are no kits added for" + args[0]));
                    sender.sendMessage("");
                    return;
                }
                if (kit.isEnabled()) {
                    sender.sendMessage(CC.translate("&a • " + kit.getName()));
                } else {
                    sender.sendMessage(CC.translate("&c • " + kit.getName()));
                }
            }
        }
    }
    }

    public static class ArenaListCommand extends BaseCommand {

        @Command(name = "arena.list", permission = "arena.commands.list")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /arena list"));
                return;
            }

            player.sendMessage(CC.translate("&4&lArenas&4:"));

            if (Arena.getArenas().isEmpty()) {
                player.sendMessage(CC.GRAY + "There are no arenas.");
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            for (final Arena arena : Arena.getArenas()) {
                if (arena.getType() != ArenaType.DUPLICATE) {
                    ChatComponentBuilder builder = (new ChatComponentBuilder("")).parse("&7- " + (arena.isSetup() ? "&a• " : "&c• ") + arena.getName() + " &7(" + arena.getType().name() + ")");
                    ChatComponentBuilder status = (new ChatComponentBuilder("")).parse("&7[&4Status&7]");
                    status.attachToEachPart(ChatHelper.hover("&cClick to view this arena's status."));
                    status.attachToEachPart(ChatHelper.click("/arena status " + arena.getName()));

                    builder.append(" ");
                    for (BaseComponent component : status.create()) {
                        builder.append((TextComponent) component);
                    }
                    player.spigot().sendMessage(builder.create());
                }
            }
        }
    }

    public static class ArenaManagerCommand extends BaseCommand {

        @Command(name = "arena.manager", permission = "arena.commands.manager")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /arena manager"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }
        }
    }

    public static class ArenaRemoveKitCommand extends BaseCommand {

        @Command(name = "arena.removekit", permission = "arena.commands.removekit")
        public void onCommand(CommandArgs commandArgs) {
            String[] args = commandArgs.getArgs();
            CommandSender sender = commandArgs.getSender();

            if (args.length == 0) {
                sender.sendMessage(CC.translate("&cUsage: /arena removekit <arena> <kit>"));
                return;
            }

            if (args.length >= 3) {
                sender.sendMessage(CC.translate("&cUsage: /arena removekit <arena> <kit>"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            if (arena == null) {
                sender.sendMessage(CC.translate("&cArena " + args[0] + " does not exist."));
                return;
            }

            Kit kit = Kit.getByName(args[1]);
            if (kit == null) {
                sender.sendMessage(CC.translate("&cKit " + args[1] + " does not exist."));
                return;
            }
            arena.getKits().remove(kit.getName());
            arena.save();

            sender.sendMessage(CC.translate("&aKit " + args[1] + " has been removed from " + args[0]));
        }
    }

    public static class ArenaSaveCommand extends BaseCommand {

        @Command(name = "arena.save", permission = "arena.commands.save", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            String[] args = commandArgs.getArgs();
            CommandSender sender = commandArgs.getSender();

            if (args.length >= 1) {
                sender.sendMessage(CC.translate("&cUsage: /arena save"));
                return;
            }

            Arena.getArenas().forEach(Arena::save);
            sender.sendMessage(CC.translate("&a" + Arena.getArenas().size() + " arenas has been saved!"));
        }
    }

    public static class ArenaSetDisplayNameCommand extends BaseCommand {

        @Command(name = "arena.setdisplayname", permission = "arena.commands.setdisplayname", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            CommandSender sender = commandArgs.getSender();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                sender.sendMessage(CC.translate("&cUsage: /arena setdisplayname <arena> <name>"));
                return;
            }

            if (args.length >= 4) {
                sender.sendMessage(CC.translate("&cUsage: /arena setdisplayname <arena> <name>"));
                return;
            }

            String arenaName = args[0];
            Arena arena = Arena.getByName(arenaName);
            if (arena == null) {
                sender.sendMessage(CC.translate("&cArena " + args[0] + " does not exist"));
                return;
            }

            arena.save();
            sender.sendMessage(CC.translate("&aArena " + args[0] + "'s displayname has been updated to" + args[1]));
        }
    }

    public static class ArenaSetIconCommand extends BaseCommand {
        @Command(name = "arena.seticon", permission = "arena.commands.seticon")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /arena seticon <arena>"));
                return;
            }

            if (args.length >= 2) {
                player.sendMessage(CC.translate("&cUsage: /arena seticon <arena>"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            if (arena == null) {
                player.sendMessage(CC.translate("&cArena " + args[0] + " doesn't exist."));
                return;
            }

            arena.setDisplayIcon(player.getItemInHand());
            arena.save();
            player.sendMessage(CC.translate("&aSuccessfully set the " + args[0] + "'s icon to the item in your hand."));
        }
    }

    public static class ArenaSetPortalCommand extends BaseCommand {

        @Command(name = "arena.setportal", permission = "arena.commands.setportal")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /arena setportal <arena> <red/blue>"));
                return;
            }

            if (args.length >= 3) {
                player.sendMessage(CC.translate("&cUsage: /arena setportal <arena> <red/blue>"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            if (arena == null) {
                player.sendMessage(CC.translate("&cArena " + args[0] + " does not exist."));
                return;
            }

            String team = args[1];
            if (!team.equals("blue") && !team.equals("red")) {
                player.sendMessage(CC.translate("&c" + args[1] + " is not valid team."));
                return;
            }

            if (team.equalsIgnoreCase("blue")) {
                StandaloneArena bridgeArena = (StandaloneArena) arena;
                Selection selection = Selection.createOrGetSelection(player);
                if (!selection.isFullObject()) {
                    player.sendMessage(CC.translate("&7Your selection is incomplete."));
                    return;
                }
                bridgeArena.setBlueCuboid(selection.getCuboid());
                arena.save();
                player.sendMessage(CC.translate("&7Successfully set the &9Blue Portal&7!"));
            }
            if (team.equalsIgnoreCase("red")) {
                StandaloneArena bridgeArena = (StandaloneArena) arena;
                Selection selection = Selection.createOrGetSelection(player);
                if (!selection.isFullObject()) {
                    player.sendMessage(CC.translate("&7Your selection is incomplete."));
                    return;
                }
                bridgeArena.setRedCuboid(selection.getCuboid());
                arena.save();
                player.sendMessage(CC.translate("&7Successfully set the &cRed Portal&7!"));
            }
        }
    }

    public static class ArenaSetBedCommand extends BaseCommand {
        @Command(name = "arena.setbed", permission = "arena.commands.setbed")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (!(player.getLocation().getBlock() instanceof Bed)) {
                player.sendMessage(CC.translate("§cYou must stand on top of the bed!"));
            }

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /arena setbed <arena> <red/blue>"));
                return;
            }

            if (args.length >= 3) {
                player.sendMessage(CC.translate("&cUsage: /arena setbed <arena> <red/blue>"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            if (arena == null) {
                player.sendMessage(CC.translate("&cArena " + args[0] + " does not exist."));
                return;
            }

            String team = args[1];
            if (!team.equals("blue") && !team.equals("red")) {
                player.sendMessage(CC.translate("&c" + args[1] + " is not valid team."));
                return;
            }

            if (team.equalsIgnoreCase("blue")) {
                StandaloneArena bridgeArena = (StandaloneArena) arena;
                bridgeArena.setBlueBed(player.getLocation());
                arena.save();
                player.sendMessage(CC.translate("&7Successfully set the &9Blue Bed&7!"));
            }

            if (team.equalsIgnoreCase("red")) {
                StandaloneArena bridgeArena = (StandaloneArena) arena;
                bridgeArena.setRedBed(player.getLocation());
                arena.save();
                player.sendMessage(CC.translate("&7Successfully set the &cRed Bed&7!"));
            }
        }
    }

    public static class ArenaSetSpawnCommand extends BaseCommand {
        @Command(name = "arena.setspawn", permission = "arena.commands.setspawn")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /arena setspawn <arena> <1/2>"));
                return;
            }

            if (args.length >= 3) {
                player.sendMessage(CC.translate("&cUsage: /arena setspawn <arena> <1/2>"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            Location loc = new Location(player.getLocation().getWorld(), player.getLocation().getX(), player.getLocation().getY(),
                    player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
            String pos = args[1];
            if (arena != null) {
                if (pos.equals("1")) {
                    arena.setSpawn1(loc);
                    if (arena.getSpawn2() != null) arena.save();
                } else if (pos.equals("2")) {
                    arena.setSpawn2(loc);
                    if (arena.getSpawn1() != null) arena.save();
                }

                player.sendMessage(CC.translate("&aArena " + args[0] + "'s position has been set"));
                arena.save();
            }
        }
    }

    public static class ArenaStatusCommand extends BaseCommand {

        @Command(name = "arena.status", permission = "arena.commands.status", inGameOnly = false)
        public void onCommand(CommandArgs commandArgs) {
            CommandSender sender = commandArgs.getSender();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                sender.sendMessage(CC.translate("&cUsage: /arena status <arena>"));
                return;
            }

            if (args.length >= 2) {
                sender.sendMessage(CC.translate("&cUsage: /arena status <arena>"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            if (arena != null) {
                sender.sendMessage(CC.RED + CC.BOLD + "Arena Status " + CC.GRAY + "(" + (arena.isSetup() ? CC.GREEN : CC.RED) + arena.getName() + CC.GRAY + ")");
                sender.sendMessage(CC.RED + "Spawn A Location: " + CC.GREEN + ((arena.getSpawn1() == null) ? StringEscapeUtils.unescapeJava("✗") : StringEscapeUtils.unescapeJava("✓")));
                sender.sendMessage(CC.RED + "Spawn B Location: " + CC.GREEN + ((arena.getSpawn1() == null) ? StringEscapeUtils.unescapeJava("✗") : StringEscapeUtils.unescapeJava("✓")));
                sender.sendMessage(CC.RED + "Kits: " + CC.GREEN + StringUtils.join(arena.getKits(), ", "));
            } else {
                sender.sendMessage(CC.translate("&cArena " + args[0] + " does not exist."));
            }
        }
    }

    public static class ArenaTpCommand extends BaseCommand {
        @Command(name = "arena.teleport", aliases = "arena.tp", permission = "arena.commands.teleport")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /arena teleport <arena>"));
                return;
            }

            if (args.length >= 2) {
                player.sendMessage(CC.translate("&cUsage: /arena teleport <arena>"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            Arena arena = Arena.getByName(args[0]);
            if (arena == null) {
                player.sendMessage(CC.translate("&cArena " + args[0] + " does not exist."));
                return;
            }

            if (arena.getSpawn1() == null || arena.getSpawn2() == null) {
                player.sendMessage(CC.translate("&cThere must be at least 1 spawn for " + args[0] + " to teleport."));
            } else {
                if (arena.getSpawn1() == null) {
                    player.teleport(arena.getSpawn2());
                } else {
                    player.teleport(arena.getSpawn1());
                }
                player.sendMessage(CC.translate("&aYou have been teleported to the " + args[0]));
            }
        }
    }

    public static class ArenaWandCommand extends BaseCommand {

        @Command(name = "arena.wand", permission = "arena.commands.arenas")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();

            if (args.length >= 1) {
                player.sendMessage(CC.translate("&cUsage: /arena wand"));
                return;
            }

            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }

            if (player.getInventory().first(Selection.SELECTION_WAND) != -1) {
                player.getInventory().remove(Selection.SELECTION_WAND);
            } else {
                player.getInventory().addItem(Selection.SELECTION_WAND);
                player.sendMessage(CC.translate("&8[&4TIP&8] &7&oLeft-Click to select first position and Right-Click to select second position."));
            }

            player.updateInventory();
        }
    }

    public static class ResetStatsCommand extends BaseCommand {
    
        @Command(name = "practice.reset", aliases = {"practice.resetstats", "practice.resetplayer"}, permission = "practice.staff")
        public void onCommand(CommandArgs commandArgs) {
            Player player = commandArgs.getPlayer();
            String[] args = commandArgs.getArgs();
    
            if (args.length == 0) {
                player.sendMessage(CC.translate("&cUsage: /practice reset <player>"));
                return;
            }
    
            if (args.length >= 2) {
                player.sendMessage(CC.translate("&cUsage: /practice reset <player>"));
                return;
            }
    
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(CC.translate("&c" + args[0] + " is currently offline."));
                return;
            }
    
            CommandSender sender = commandArgs.getSender();
            if (!(sender instanceof Player)) {
                sender.sendMessage(Practice.get().getMessagesConfig().getString("COMMON_ERRORS.INGAME_ONLY"));
                return;
            }
    
            if (args[0] == null) {
                player.sendMessage(CC.RED + "Either that player does not exist or you did not specify a name!");
            }
    
            try {
                Practice.get().getMongoDatabase().getCollection("profiles").deleteOne(new Document("name", args[0]));
                target.getPlayer().kickPlayer("Your Profile was reset by an Admin, Please Rejoin!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}