package me.cprox.practice.party;

import lombok.AllArgsConstructor;
import me.cprox.practice.util.chat.CC;

import java.text.MessageFormat;

@AllArgsConstructor
public enum PartyMessage {

    YOU_HAVE_BEEN_INVITED("&7You have been invited to join &c{0}&7''s party."),
    CLICK_TO_JOIN("&a(Click to accept)"),
    PLAYER_INVITED("&c{0} &7has been invited to your party."),
    PLAYER_JOINED("&c{0} &7joined your party."),
    PLAYER_LEFT("&c{0} &7has left your party."),
    CREATED("&aYou created a party."),
    DISBANDED("&cYour party has been disbanded."),
    PUBLIC("&c{0}&7 is hosting a public party"),
    PRIVACY_CHANGED("&7Your party privacy has been changed to: &c{0}");

    private final String message;

    public String format(Object... objects) {
        return CC.translate(new MessageFormat(this.message).format(objects));
    }

}
