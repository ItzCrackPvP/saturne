package me.cprox.practice.party;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PartyManage {

    LEADER("Make leader"),
    KICK("Kick player"),
    INCREMENTLIMIT("Increment limit by 1"),
    PUBLIC("Publish party"),
    DECREASELIMIT("Decrease limit by 1");

    private final String name;

}
