package me.cprox.practice.events.sumo.task;

import me.cprox.practice.Practice;
import me.cprox.practice.events.sumo.Sumo;
import me.cprox.practice.events.sumo.SumoState;
import me.cprox.practice.events.sumo.SumoTask;
import me.cprox.practice.util.PlayerUtil;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class SumoRoundStartTask extends SumoTask {

	public SumoRoundStartTask(Sumo sumo) {
		super(sumo, SumoState.ROUND_STARTING);
	}
	static String EVENT_PREFIX = Practice.get().getEventMessages().getString("EVENT.SUMO.PREFIX");

	@Override
	public void onRun() {
		if (getTicks() >= 3) {

			this.getSumo().setEventTask(null);
			this.getSumo().setState(SumoState.ROUND_FIGHTING);

			this.getSumo().broadcastMessage(Practice.get().getEventMessages().getString("EVENT.SUMO.STARTED"));
			Player playerA = this.getSumo().getRoundPlayerA().getPlayer();
			Player playerB = this.getSumo().getRoundPlayerB().getPlayer();

			if (playerA != null) {
				playerA.playSound(playerA.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
				PlayerUtil.allowMovement(playerA);
			}

			if (playerB != null) {
				playerB.playSound(playerB.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
				PlayerUtil.allowMovement(playerB);
			}

			(this.getSumo()).setRoundStart(System.currentTimeMillis());
		} else {
			int seconds = getSeconds();
			Player playerA = this.getSumo().getRoundPlayerA().getPlayer();
			Player playerB = this.getSumo().getRoundPlayerB().getPlayer();

			if (playerA != null) {
				playerA.playSound(playerA.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
			}

			if (playerB != null) {
				playerB.playSound(playerB.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
			}

			this.getSumo().broadcastMessage(Practice.get().getEventMessages().getString("EVENT.SUMO.STARTING_IN").replace("<seconds>", String.valueOf(seconds)).replace("<brackets_prefix>", EVENT_PREFIX));
		}
	}

}
