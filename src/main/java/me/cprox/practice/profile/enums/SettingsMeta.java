package me.cprox.practice.profile.enums;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class SettingsMeta {

    //TOURNAMENT
    private boolean allowTournamentMessages = true;

    //MATCH ADDONS
    private boolean allowSpectators = true;
    private boolean usingMapSelector = false;
    private boolean receiveDuelRequests = true;
    private boolean showScoreboard = true;
    private boolean clearBowls = false;
    private boolean dropbottles = true;

    //DEATH EFFECTS
    private boolean flying = false;
    private boolean lightning = false;
    private boolean flameeffect = false;
    private boolean bloodeffect = false;
    private boolean fireworkeffect = false;
    private boolean clearinventory = false;
    private boolean explosioneffect = false;
}