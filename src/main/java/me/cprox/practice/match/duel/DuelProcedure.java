package me.cprox.practice.match.duel;

import me.cprox.practice.arena.Arena;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.util.external.ChatComponentBuilder;
import lombok.Getter;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.Practice;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import me.cprox.practice.util.PlayerUtil;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DuelProcedure {

    @Getter private final boolean party;
    @Getter private final Player sender;
    @Getter private final Player target;
    @Getter @Setter private Kit kit;
    @Getter @Setter private Arena arena;

    public DuelProcedure(Player sender, Player target, boolean party) {
        this.sender = sender;
        this.target = target;
        this.party = party;
    }

    public void send() {
        if (!sender.isOnline() || !target.isOnline()) {
            return;
        }

        Profile senderProfile = Profile.getByUuid(sender.getUniqueId());
        DuelRequest request = new DuelRequest(sender.getUniqueId(), party);
        request.setKit(kit);
        request.setArena(arena);

        if (!senderProfile.getSentDuelRequests().isEmpty() && senderProfile.getSentDuelRequests().containsKey(target.getUniqueId())) {
            sender.sendMessage(Practice.get().getMessagesConfig().getString("DUEL.ALREADY_SENT").replace("<player>", target.getName()));
            return;
        }

        senderProfile.setDuelProcedure(null);
        senderProfile.getSentDuelRequests().put(target.getUniqueId(), request);

        for (String string : Practice.get().getMessagesConfig().getStringList("DUEL.SENDER_MESSAGE")) {
            final String message = string
            .replace("<kit>", kit.getName())
            .replace("<arena>", arena.getName())
            .replace("<playerB>", target.getName())
            .replace("<playerBPing>", String.valueOf(PlayerUtil.getPing(target)));
            sender.sendMessage(CC.translate(message));
        }

        for (String string : Practice.get().getMessagesConfig().getStringList("DUEL.TARGET_MESSAGE")) {
            final String message = string
            .replace("<kit>", kit.getName())
            .replace("<arena>", arena.getName())
            .replace("<playerA>", sender.getName())
            .replace("<playerAPing>", String.valueOf(PlayerUtil.getPing(sender)));
            target.sendMessage(CC.translate(message));
        }

        ChatComponentBuilder message = new ChatComponentBuilder("");
        List<BaseComponent[]> components = new ArrayList<>();
        List<BaseComponent[]> AIR = new ArrayList<>();
        AIR.add(0, new ChatComponentBuilder("").parse("").create());

        message.append("[Accept] ").color(net.md_5.bungee.api.ChatColor.GREEN).bold(true);
        message.setCurrentHoverEvent(getAcceptHoverEvent()).setCurrentClickEvent(getAcceptClickEvent()).append("[Deny]").color(net.md_5.bungee.api.ChatColor.RED);
        message.setCurrentHoverEvent(getDenyHoverEvent()).setCurrentClickEvent(getDenyClickEvent());
        components.add(message.create());
        components.forEach(components1 -> target.spigot().sendMessage(components1));
        AIR.forEach(components1 -> target.spigot().sendMessage(components1));
    }

    protected HoverEvent getAcceptHoverEvent() {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder("")
                .parse(Practice.get().getMessagesConfig().getString("DUEL.CLICK_TO_ACCEPT")).create());
    }

    protected ClickEvent getAcceptClickEvent() {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept " + sender.getName());
    }

    protected HoverEvent getDenyHoverEvent() {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder("")
                .parse(Practice.get().getMessagesConfig().getString("DUEL.CLICK_TO_DENY")).create());
    }

    protected ClickEvent getDenyClickEvent() {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel deny " + sender.getName());
    }
}
