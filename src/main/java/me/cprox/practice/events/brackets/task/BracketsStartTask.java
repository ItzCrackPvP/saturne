package me.cprox.practice.events.brackets.task;

import me.cprox.practice.Practice;
import me.cprox.practice.events.brackets.Brackets;
import me.cprox.practice.events.brackets.BracketsState;
import me.cprox.practice.events.brackets.BracketsTask;
import me.cprox.practice.util.external.Cooldown;
import org.bukkit.entity.Player;

public class BracketsStartTask extends BracketsTask {

	public BracketsStartTask(Brackets brackets) {
		super(brackets, BracketsState.WAITING);
	}

	@Override
	public void onRun() {
		if (getTicks() >= 120) {
			this.getBrackets().end();
			return;
		}

		if (this.getBrackets().getPlayers().size() <= 1 && this.getBrackets().getCooldown() != null) {
			this.getBrackets().setCooldown(null);
			this.getBrackets().broadcastMessage("&cThere are not enough players for the brackets to start.");
		}

		if (this.getBrackets().getPlayers().size() == Brackets.getMaxPlayers() || (getTicks() >= 30 && this.getBrackets().getPlayers().size() >= 2)) {
			if (this.getBrackets().getCooldown() == null) {
				this.getBrackets().setCooldown(new Cooldown(11_000));
			} else {
				if (this.getBrackets().getCooldown().hasExpired()) {
					this.getBrackets().setState(BracketsState.ROUND_STARTING);
					this.getBrackets().onRound();
					this.getBrackets().setTotalPlayers(this.getBrackets().getPlayers().size());
					this.getBrackets().setEventTask(new BracketsRoundStartTask(this.getBrackets()));
				}
			}
		}

		if (getTicks() % 10 == 0) {
			this.getBrackets().announce();
		}
	}

}
