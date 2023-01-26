package me.cprox.practice.events.brackets.task;

import me.cprox.practice.events.brackets.Brackets;
import me.cprox.practice.events.brackets.BracketsState;
import me.cprox.practice.events.brackets.BracketsTask;

public class BracketsRoundEndTask extends BracketsTask {

	public BracketsRoundEndTask(Brackets brackets) {
		super(brackets, BracketsState.ROUND_ENDING);
	}

	@Override
	public void onRun() {
		if (getTicks() >= 3) {
			if (this.getBrackets().canEnd()) {
				this.getBrackets().end();
			} else {
				this.getBrackets().onRound();
			}
		}
	}

}
