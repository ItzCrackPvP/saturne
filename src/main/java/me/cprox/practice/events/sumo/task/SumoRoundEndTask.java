package me.cprox.practice.events.sumo.task;

import me.cprox.practice.events.sumo.Sumo;
import me.cprox.practice.events.sumo.SumoState;
import me.cprox.practice.events.sumo.SumoTask;

public class SumoRoundEndTask extends SumoTask {

	public SumoRoundEndTask(Sumo sumo) {
		super(sumo, SumoState.ROUND_ENDING);
	}

	@Override
	public void onRun() {
		if (this.getSumo().canEnd()) {
			this.getSumo().end();
		} else {
			if (getTicks() >= 3) {
				this.getSumo().onRound();
			}
		}
	}

}
