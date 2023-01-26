package me.cprox.practice.events.brackets;

import lombok.Getter;
import me.cprox.practice.Practice;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public abstract class BracketsTask extends BukkitRunnable {

	private int ticks;
	private final Brackets brackets;
	private final BracketsState eventState;

	public BracketsTask(Brackets brackets, BracketsState eventState) {
		this.brackets = brackets;
		this.eventState = eventState;
	}

	@Override
	public void run() {
		if (Practice.get().getBracketsManager().getActiveBrackets() == null ||
		    !Practice.get().getBracketsManager().getActiveBrackets().equals(brackets) || brackets.getState() != eventState) {
			cancel();
			return;
		}

		onRun();

		ticks++;
	}

	public int getSeconds() {
		return 3 - ticks;
	}

	public abstract void onRun();

}
