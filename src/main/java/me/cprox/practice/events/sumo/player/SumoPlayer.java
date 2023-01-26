package me.cprox.practice.events.sumo.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SumoPlayer {

	@Getter private final UUID uuid;
	@Getter private final String username;
	@Getter @Setter private SumoPlayerState state = SumoPlayerState.WAITING;
	@Getter @Setter private int roundWins = 0;

	public SumoPlayer(Player player) {
		this.uuid = player.getUniqueId();
		this.username = player.getName();
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	public void incrementRoundWins() {
		this.roundWins++;
	}

}
