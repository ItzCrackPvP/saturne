package me.cprox.practice.events.spleef.task;

import me.cprox.practice.events.spleef.Spleef;
import me.cprox.practice.events.spleef.SpleefState;
import me.cprox.practice.events.spleef.SpleefTask;
import me.cprox.practice.util.external.Cooldown;

public class SpleefStartTask extends SpleefTask {

	public SpleefStartTask(Spleef spleef) {
		super(spleef, SpleefState.WAITING);
	}

	@Override
	public void onRun() {
		if (getTicks() >= 120) {
			this.getSpleef().end();
			return;
		}

		if (this.getSpleef().getPlayers().size() <= 1 && this.getSpleef().getCooldown() != null) {
			this.getSpleef().setCooldown(null);
			this.getSpleef().broadcastMessage("&cThere are not enough players for the spleef to start.");
		}

		if (this.getSpleef().getPlayers().size() == Spleef.getMaxPlayers() || (getTicks() >= 30 && this.getSpleef().getPlayers().size() >= 2)) {
			if (this.getSpleef().getCooldown() == null) {
				this.getSpleef().setCooldown(new Cooldown(11_000));
			} else {
				if (this.getSpleef().getCooldown().hasExpired()) {
					this.getSpleef().setState(SpleefState.ROUND_STARTING);
					this.getSpleef().onRound();
					this.getSpleef().setTotalPlayers(this.getSpleef().getPlayers().size());
					this.getSpleef().setEventTask(new SpleefRoundStartTask(this.getSpleef()));
				}
			}
		}

		if (getTicks() % 10 == 0) {
			this.getSpleef().announce();
		}
	}

}
