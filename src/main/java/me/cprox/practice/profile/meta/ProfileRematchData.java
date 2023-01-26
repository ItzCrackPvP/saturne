package me.cprox.practice.profile.meta;

import me.cprox.practice.Practice;
import me.cprox.practice.arena.Arena;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.match.Match;
import me.cprox.practice.match.impl.*;
import me.cprox.practice.match.impl.solo.SoloBridgeMatch;
import me.cprox.practice.match.team.TeamPlayer;
import me.cprox.practice.menu.duel.DuelSelectKitMenu;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.QueueType;
import me.cprox.practice.util.chat.CC;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class ProfileRematchData {

    private final long timestamp = System.currentTimeMillis();
    @Setter private boolean receive;
    @Setter private boolean sent;
    @Setter private Arena arena;
    private final UUID sender;
    private final UUID target;
    @Setter private Kit kit;
    private final UUID key;

    public ProfileRematchData(UUID key, UUID sender, UUID target, Kit kit, Arena arena) {
        this.key = key;
        this.sender = sender;
        this.target = target;
        this.kit = kit;
        this.arena = arena;
    }

    public void request() {

        Player sender = Practice.get().getServer().getPlayer(this.sender);
        Player target = Practice.get().getServer().getPlayer(this.target);

        if (sender == null || target == null) {
            return;
        }

        Profile senderProfile = Profile.getByUuid(sender.getUniqueId());
        Profile targetProfile = Profile.getByUuid(target.getUniqueId());

        if (senderProfile.getRematchData() == null || targetProfile.getRematchData() == null ||
                !senderProfile.getRematchData().getKey().equals(targetProfile.getRematchData().getKey())) {
            return;
        }

        if (senderProfile.isBusy(sender)) {
            sender.sendMessage(CC.RED + "You cannot duel right now.");
            return;
        }

        new DuelSelectKitMenu("rematch").openMenu(sender);

        this.sent = true;
        targetProfile.getRematchData().receive = true;

        senderProfile.checkForHotbarUpdate();
        targetProfile.checkForHotbarUpdate();
        Bukkit.getScheduler().runTaskLaterAsynchronously(Practice.get(), () -> {
            senderProfile.checkForHotbarUpdate();
            targetProfile.checkForHotbarUpdate();
        }, 15 * 20);
    }

    public void accept() {
        Player sender = Practice.get().getServer().getPlayer(this.sender);
        Player target = Practice.get().getServer().getPlayer(this.target);

        if (sender == null || target == null || !sender.isOnline() || !target.isOnline()) {
            return;
        }

        Profile senderProfile = Profile.getByUuid(sender.getUniqueId());
        Profile targetProfile = Profile.getByUuid(target.getUniqueId());

        if (senderProfile.getRematchData() == null || targetProfile.getRematchData() == null ||
                !senderProfile.getRematchData().getKey().equals(targetProfile.getRematchData().getKey())) {
            return;
        }

        if (senderProfile.isBusy(sender)) {
            sender.sendMessage(CC.RED + "You cannot duel right now.");
            return;
        }

        if (targetProfile.isBusy(target)) {
            sender.sendMessage(CC.translate(CC.RED + target.getName()) + CC.RED + " is currently busy.");
            return;
        }

        Arena arena = this.arena;

        if (arena.isActive()) {
            arena = Arena.getRandom(kit);
        }

        if (arena == null) {
            sender.sendMessage(CC.RED + "Tried to start a match but there are no available arenas.");
            return;
        }

        arena.setActive(true);
        Match match;

        if (kit.getGameRules().isBridge()) {
            match = new SoloBridgeMatch(null, new TeamPlayer(sender), new TeamPlayer(target), kit, arena, QueueType.UNRANKED);
        } else {
            match = new SoloMatch(null, new TeamPlayer(sender), new TeamPlayer(target), kit, arena, QueueType.UNRANKED);
        }
        match.start();
    }

}

