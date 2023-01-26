package me.cprox.practice.events.brackets.task;

import me.cprox.practice.Practice;
import me.cprox.practice.events.brackets.Brackets;
import me.cprox.practice.events.brackets.BracketsState;
import me.cprox.practice.events.brackets.BracketsTask;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BracketsRoundStartTask extends BracketsTask {

	public BracketsRoundStartTask(Brackets brackets) {
		super(brackets, BracketsState.ROUND_STARTING);
	}
	static String EVENT_PREFIX = Practice.get().getEventMessages().getString("EVENT.BRACKETS.PREFIX");

	@Override
	public void onRun() {
		if (getTicks() >= 3) {
			this.getBrackets().setEventTask(null);
			this.getBrackets().setState(BracketsState.ROUND_FIGHTING);

			this.getBrackets().broadcastMessage(Practice.get().getEventMessages().getString("EVENT.BRACKETS.STARTED"));
			Player playerA = this.getBrackets().getRoundPlayerA().getPlayer();
			Player playerB = this.getBrackets().getRoundPlayerB().getPlayer();

			if (playerA != null) {
				playerA.playSound(playerA.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
			}

			if (playerB != null) {
				playerB.playSound(playerB.getLocation(), Sound.NOTE_BASS, 1.0F, 1.0F);
			}

			(this.getBrackets()).setRoundStart(System.currentTimeMillis());
		} else {
			int seconds = getSeconds();
			Player playerA = this.getBrackets().getRoundPlayerA().getPlayer();
			Player playerB = this.getBrackets().getRoundPlayerB().getPlayer();

			if (playerA != null) {
				playerA.playSound(playerA.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
			}

			if (playerB != null) {
				playerB.playSound(playerB.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
			}

			this.getBrackets().broadcastMessage(Practice.get().getEventMessages().getString("EVENT.BRACKETS.STARTING_IN").replace("<seconds>", String.valueOf(seconds)).replace("<brackets_prefix>", EVENT_PREFIX));
		}
	}

}
