package me.cprox.practice.party;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PartyPrivacy {

    OPEN("Public"),
    CLOSED("Closed");

    private final String readable;

}
