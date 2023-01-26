package me.cprox.practice.events.spleef.task;

import me.cprox.practice.Practice;
import me.cprox.practice.events.spleef.Spleef;
import me.cprox.practice.events.spleef.SpleefState;
import me.cprox.practice.events.spleef.SpleefTask;
import me.cprox.practice.util.chat.CC;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

public class SpleefRoundStartTask extends SpleefTask {

	public SpleefRoundStartTask(Spleef spleef) {
		super(spleef, SpleefState.ROUND_STARTING);
	}
	static String EVENT_PREFIX = Practice.get().getEventMessages().getString("EVENT.SPLEEF.PREFIX");

	@Override
	public void onRun() {
		if (getTicks() >= 3) {
			this.getSpleef().broadcastMessage(Practice.get().getEventMessages().getString("EVENT.SPLEEF.STARTED"));
			this.getSpleef().setEventTask(null);
			this.getSpleef().setState(SpleefState.ROUND_FIGHTING);

			this.getSpleef().setRoundStart(System.currentTimeMillis());
		} else {
			int seconds = getSeconds();
			this.getSpleef().broadcastMessage(Practice.get().getEventMessages().getString("EVENT.SPLEEF.STARTING_IN").replace("<seconds>", String.valueOf(seconds)).replace("<brackets_prefix>", EVENT_PREFIX));
		}
	}

}
