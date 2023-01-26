package me.cprox.practice.profile.enums;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class KitGameRules {

    private boolean ranked, editable, boxing, build, spleef, sumo,
            combo, bridge, battleRush, bedFight, skywars, pearlFight,
            botfight, partyffa, partysplit, ffacenter, antifoodloss, stickspawn,
            voidspawn, waterkill, lavakill, showhealth, noitems, timed, bowhp;
    private int hitDelay = 20;
}
