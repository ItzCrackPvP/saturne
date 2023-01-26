package me.cprox.practice.profile.enums;

import java.beans.ConstructorProperties;

public enum HotbarType
{
    QUEUE_JOIN_RANKED(null), 
    QUEUE_JOIN_UNRANKED(null),
    QUEUE_LEAVE(null), 
    PARTY_EVENTS(null), 
    PARTY_CREATE("party create"), 
    PARTY_DISBAND("party disband"),
    PARTY_LEAVE("party leave"),
    PARTY_INFORMATION("party info"), 
    PARTY_SETTINGS(null), 
    OTHER_PARTIES(null),
    PARTY_INFO(null),
    LEADERBOARDS_MENU(null),
    MAIN_MENU(null),
    KIT_EDITOR(null),
    ARENA_MANAGER(null),
    KIT_MANAGER(null),
    SPECTATE_MATCH(null),
    SYSTEMMANAGER_LEAVE(null),
    TELEPORT_SPECTATE(null),
    SPECTATE_STOP("stopspectating"),
    EVENT_JOIN("event"),
    SUMO_LEAVE("sumo leave"), 
    BRACKETS_LEAVE("brackets leave"), 
    LMS_LEAVE("lms leave"),
    PARKOUR_LEAVE("parkour leave"),
    PARKOUR_SPAWN(null),
    SPLEEF_LEAVE("spleef leave"),
    REMATCH_REQUEST("rematch"),
    REMATCH_ACCEPT("rematch"),
    SKYWARS_LEAVE("skywars leave"),
    DEFAULT_KIT(null);
    
    private final String command;
    
    @ConstructorProperties({ "command" })
    HotbarType(final String command) {
        this.command = command;
    }
    
    public String getCommand() {
        return this.command;
    }
}
