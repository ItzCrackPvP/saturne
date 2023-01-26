package me.cprox.practice.party;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OtherPartyEvent {

    KIT("Normal Kit"),
    HCF("HCF Kit");

    private final String name;

}
