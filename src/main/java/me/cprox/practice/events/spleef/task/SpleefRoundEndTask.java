package me.cprox.practice.events.spleef.task;

import me.cprox.practice.events.spleef.Spleef;
import me.cprox.practice.events.spleef.SpleefState;
import me.cprox.practice.events.spleef.SpleefTask;

public class SpleefRoundEndTask extends SpleefTask {

	public SpleefRoundEndTask(Spleef spleef) {
		super(spleef, SpleefState.ROUND_ENDING);
	}

	@Override
	public void onRun() {
		if (getTicks() >= 3) {
			if (this.getSpleef().canEnd()) {
				this.getSpleef().end();
			}
		}
	}

}
