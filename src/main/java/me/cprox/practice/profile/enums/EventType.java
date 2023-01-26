package me.cprox.practice.profile.enums;

import me.cprox.practice.Practice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.cprox.practice.events.brackets.Brackets;
import me.cprox.practice.events.spleef.Spleef;
import me.cprox.practice.events.sumo.Sumo;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public enum EventType {
    BRACKETS(Practice.get().getBracketsManager().getActiveBrackets(), "&4&lBrackets", Material.IRON_SWORD, Brackets.isEnabled(), Brackets.getMaxPlayers()),
    SUMO(Practice.get().getSumoManager().getActiveSumo(), "&4&lSumo", Material.LEASH, Sumo.isEnabled(), Sumo.getMaxPlayers()),
    SPLEEF(Practice.get().getSpleefManager().getActiveSpleef(), "&4&lSpleef", Material.SNOW_BALL, Spleef.isEnabled(), Spleef.getMaxPlayers());

    private final Object object;
    private final String title;
    private final Material material;
    private final boolean enabled;
    private final int limit;
}
