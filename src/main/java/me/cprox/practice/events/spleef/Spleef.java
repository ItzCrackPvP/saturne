package me.cprox.practice.events.spleef;

import lombok.Getter;
import lombok.Setter;
import me.cprox.practice.Practice;
import me.cprox.practice.events.spleef.player.SpleefPlayer;
import me.cprox.practice.events.spleef.player.SpleefPlayerState;
import me.cprox.practice.events.spleef.task.SpleefRoundEndTask;
import me.cprox.practice.events.spleef.task.SpleefRoundStartTask;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.util.Circle;
import me.cprox.practice.util.PlayerSnapshot;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.chat.Clickable;
import me.cprox.practice.util.external.Cooldown;
import me.cprox.practice.util.external.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Getter
public class Spleef {

	protected static String EVENT_PREFIX = Practice.get().getEventMessages().getString("EVENT.SPLEEF.PREFIX");

	private final String name;
	@Setter private SpleefState state = SpleefState.WAITING;
	@Getter @Setter static private Kit kit = Kit.getByName("Spleef");
	private SpleefTask eventTask;
	private final PlayerSnapshot host;
	private final LinkedHashMap<UUID, SpleefPlayer> eventPlayers = new LinkedHashMap<>();
	@Getter private final List<UUID> spectators = new ArrayList<>();
	private final List<Location> placedBlocks = new ArrayList<>();
	private final List<BlockState> changedBlocks = new ArrayList<>();
	@Getter @Setter	public static int maxPlayers;
	@Getter @Setter private int totalPlayers;
	@Setter private Cooldown cooldown;
	@Setter private long roundStart;
	@Getter	@Setter	private static boolean enabled = true;


	public Spleef(Player player) {
		this.name = player.getName();
		this.host = new PlayerSnapshot(player.getUniqueId(), player.getName());
		maxPlayers = 100;
	}

	public List<String> getLore() {
		List<String> toReturn = new ArrayList<>();

		Spleef spleef = Practice.get().getSpleefManager().getActiveSpleef();

		toReturn.add(CC.MENU_BAR);
		toReturn.add(CC.translate("&4Host: &r" + spleef.getName()));

		if (spleef.isWaiting()) {
			toReturn.add("&4Players: &r" + spleef.getEventPlayers().size() + "/" + Spleef.getMaxPlayers());
			toReturn.add("");

			if (spleef.getCooldown() == null) {
				toReturn.add(CC.translate("&7Waiting for players..."));
			} else {
				String remaining = TimeUtil.millisToSeconds(spleef.getCooldown().getRemaining());

				if (remaining.startsWith("-")) {
					remaining = "0.0";
				}

				toReturn.add(CC.translate("&7Starting in " + remaining + "s"));
			}
		} else {
			toReturn.add("&4Players: &r" + spleef.getRemainingPlayers().size() + "/" + spleef.getTotalPlayers());
			toReturn.add("&4Duration: &r" + spleef.getRoundDuration());
		}
		toReturn.add(CC.MENU_BAR);

		return toReturn;
	}

	public void setEventTask(SpleefTask task) {
		if (eventTask != null) {
			eventTask.cancel();
		}

		eventTask = task;

		if (eventTask != null) {
			eventTask.runTaskTimer(Practice.get(), 0L, 20L);
		}
	}

	public boolean isWaiting() {
		return state == SpleefState.WAITING;
	}

	public boolean isFighting() {
		return state == SpleefState.ROUND_FIGHTING;
	}

	public boolean isFighting(Player player) {
		if (state.equals(SpleefState.ROUND_FIGHTING)) {
			return getRemainingPlayers().contains(player);
		} else {
			return false;
		}
	}

	public SpleefPlayer getEventPlayer(Player player) {
		return eventPlayers.get(player.getUniqueId());
	}

	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<>();

		for (SpleefPlayer spleefPlayer : eventPlayers.values()) {
			Player player = spleefPlayer.getPlayer();

			if (player != null) {
				players.add(player);
			}
		}

		return players;
	}

	public List<Player> getRemainingPlayers() {
		List<Player> players = new ArrayList<>();

		for (SpleefPlayer spleefPlayer : eventPlayers.values()) {
			if (spleefPlayer.getState() == SpleefPlayerState.WAITING) {
				Player player = spleefPlayer.getPlayer();
				if (player != null) {
					players.add(player);
				}
			}
		}

		return players;
	}

	public void handleJoin(Player player) {
		if (this.eventPlayers.size() >= maxPlayers) {
			player.sendMessage(Practice.get().getEventMessages().getString("EVENT.FULL").replace("<prefix>", EVENT_PREFIX));
			return;
		}

		eventPlayers.put(player.getUniqueId(), new SpleefPlayer(player));

		for (String string : Practice.get().getEventMessages().getStringList("EVENT.SPLEEF.PLAYER_JOINED")) {
			final String message = string
					.replace("<spleef_prefix>", EVENT_PREFIX)
					.replace("<player>", player.getName())
					.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
					.replace("<maxPlayers>", String.valueOf(getMaxPlayers()));
			broadcastMessage(message);
		}
		onJoin(player);

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setSpleef(this);
		profile.setState(ProfileState.IN_EVENT);
		profile.refreshHotbar();

		player.teleport(Practice.get().getSpleefManager().getSpleefSpectator());

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player otherPlayer : getPlayers()) {
					Profile otherProfile = Profile.getByUuid(otherPlayer.getUniqueId());
					otherProfile.handleVisibility(otherPlayer, player);
					profile.handleVisibility(player, otherPlayer);
				}
			}
		}.runTaskAsynchronously(Practice.get());
	}

	public void handleLeave(Player player) {
		if (state != SpleefState.WAITING) {
			if (isFighting(player)) {
				handleDeath(player);
			}
		}

		eventPlayers.remove(player.getUniqueId());

		if (state == SpleefState.WAITING) {
			for (String string : Practice.get().getEventMessages().getStringList("EVENT.SPLEEF.PLAYER_LEFT")) {
				final String message = string
						.replace("<spleef_prefix>", EVENT_PREFIX)
						.replace("<player>", player.getName())
						.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
						.replace("<maxPlayers>", String.valueOf(getMaxPlayers()));
				broadcastMessage(message);
			}
		}

		onLeave(player);

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setState(ProfileState.IN_LOBBY);
		profile.setSpleef(null);
		profile.refreshHotbar();

		Practice.get().getEssentials().teleportToSpawn(player);

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player otherPlayer : getPlayers()) {
					Profile otherProfile = Profile.getByUuid(otherPlayer.getUniqueId());
					otherProfile.handleVisibility(otherPlayer, player);
					profile.handleVisibility(player, otherPlayer);
				}
			}
		}.runTaskAsynchronously(Practice.get());
	}

	protected List<Player> getSpectatorsList() {
		return PlayerUtil.convertUUIDListToPlayerList(spectators);
	}

	public void handleDeath(Player player) {
		SpleefPlayer loser = getEventPlayer(player);
		loser.setState(SpleefPlayerState.ELIMINATED);

		onDeath(player);
	}

	public void end() {
		Practice.get().getSpleefManager().setActiveSpleef(null);
		Practice.get().getSpleefManager().setCooldown(new Cooldown(60_000L * 10));

		setEventTask(null);

		new SpleefResetTask(this).runTask(Practice.get());

		Player winner = this.getWinner();

		if (winner == null) {
			Bukkit.broadcastMessage(Practice.get().getEventMessages().getString("EVENT.SPLEEF.CANCELED").replace("<prefix>", EVENT_PREFIX));
		} else {
			for (String string : Practice.get().getEventMessages().getStringList("EVENT.SPLEEF.WINNER_ANNOUNCEMENT")) {
				final String message = string
						.replace("<spleef_prefix>", EVENT_PREFIX)
						.replace("<player>", getWinner().getName())
						.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
						.replace("<maxPlayers>", String.valueOf(getMaxPlayers()));
				broadcastMessage(message);
			}
		}

		for (SpleefPlayer spleefPlayer : eventPlayers.values()) {
			Player player = spleefPlayer.getPlayer();

			if (player != null) {
				Profile profile = Profile.getByUuid(player.getUniqueId());
				profile.setState(ProfileState.IN_LOBBY);
				profile.setSpleef(null);
				profile.refreshHotbar();

				Practice.get().getEssentials().teleportToSpawn(player);
			}
		}

		getSpectatorsList().forEach(this::removeSpectator);

		for (Player player : getPlayers()) {
			Profile.getByUuid(player.getUniqueId()).handleVisibility();
		}
	}

	public boolean canEnd() {
		int remaining = 0;

		for (SpleefPlayer spleefPlayer : eventPlayers.values()) {
			if (spleefPlayer.getState() == SpleefPlayerState.WAITING) {
				remaining++;
			}
		}

		return remaining <= 1;
	}

	public Player getWinner() {
		for (SpleefPlayer spleefPlayer : eventPlayers.values()) {
			if (spleefPlayer.getState() != SpleefPlayerState.ELIMINATED) {
				return spleefPlayer.getPlayer();
			}
		}

		return null;
	}

	public void announce() {
		List<String> strings = new ArrayList<>();
		Spleef spleef = Practice.get().getSpleefManager().getActiveSpleef();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!eventPlayers.containsKey(player.getUniqueId())) {

				for (String string : Practice.get().getEventMessages().getStringList("EVENT.SPLEEF.STARTING")) {
					final String message = string
							.replace("<spleef_prefix>", EVENT_PREFIX)
							.replace("<player>", this.host.getUsername())
							.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
							.replace("<maxPlayers>", String.valueOf(getMaxPlayers()));
					player.sendMessage(CC.translate(message));
				}
			}
		}
		strings.add(CC.translate(Practice.get().getEventMessages().getStringList("EVENT.SPLEEF.CLICK_TO_JOIN").toString()));
		for (String string : strings) {
			Clickable message = new Clickable(string, Practice.get().getEventMessages().getString("EVENT.SPLEEF.HOVER_CLICK_TO_JOIN"), "/spleef join");
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (!eventPlayers.containsKey(player.getUniqueId())) {
					message.sendToPlayer(player);
				}
			}
		}
	}

	public void broadcastMessage(String message) {
		for (Player player : getPlayers()) {
			player.sendMessage(EVENT_PREFIX + CC.translate(message));
		}
	}

	public void onJoin(Player player) {
		}
	public void onLeave(Player player) {
		}

	public void onRound() {
		setState(SpleefState.ROUND_STARTING);

		int i = 0;
		for (Player player : this.getRemainingPlayers()) {
			if (player != null) {
				Location midSpawn = Practice.get().getSpleefManager().getSpleefSpectator();
				List<Location> circleLocations = Circle.getCircle(midSpawn, 7, this.getPlayers().size());
				Location center = midSpawn.clone();
				Location loc = circleLocations.get(i);
				Location target = loc.setDirection(center.subtract(loc).toVector());
				player.teleport(target.add(0, 0.5, 0));
				circleLocations.remove(i);
				i++;
				Profile profile = Profile.getByUuid(player.getUniqueId());
				if (profile.isInSpleef()) {
					profile.refreshHotbar();
				}
				PlayerUtil.reset(player);
			}

			assert player != null;
			//Profile.getByUuid(player.getUniqueId()).getStatisticsData().get(getKit()).getKitItems().forEach((integer, itemStack) -> player.getInventory().setItem(integer, itemStack));
		}
		setEventTask(new SpleefRoundStartTask(this));
	}

	public void onDeath(Player player) {
		Profile profile = Profile.getByUuid(player.getUniqueId());

		player.sendMessage(Practice.get().getEventMessages().getString("EVENT.ELIMINATED").replace("<spleef_prefix>", EVENT_PREFIX));

		broadcastMessage(Practice.get().getEventMessages().getString("EVENT.SPLEEF.PLAYER_DIED").replace("<spleef_prefix>", EVENT_PREFIX).replace("<player>", player.getName()));

		if (canEnd()) {
			setState(SpleefState.ROUND_ENDING);
			setEventTask(new SpleefRoundEndTask(this));
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player otherPlayer : getPlayers()) {
					Profile otherProfile = Profile.getByUuid(otherPlayer.getUniqueId());
					otherProfile.handleVisibility(otherPlayer, player);
					profile.handleVisibility(player, otherPlayer);
				}
			}
		}.runTaskAsynchronously(Practice.get());

		new BukkitRunnable() {
			@Override
			public void run() {
				profile.refreshHotbar();
			}
		}.runTask(Practice.get());
	}

	public String getRoundDuration() {
		if (getState() == SpleefState.ROUND_STARTING) {
			return "00:00";
		} else if (getState() == SpleefState.ROUND_FIGHTING) {
			return TimeUtil.millisToTimer(System.currentTimeMillis() - roundStart);
		} else {
			return "Ending";
		}
	}

	public void addSpectator(Player player) {
		spectators.add(player.getUniqueId());

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setSpleef(this);
		profile.setState(ProfileState.SPECTATE_MATCH);
		profile.refreshHotbar();
		profile.handleVisibility();
		player.setFlying(true);


		player.teleport(Practice.get().getSpleefManager().getSpleefSpectator());
	}

	public void removeSpectator(Player player) {
		spectators.remove(player.getUniqueId());

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setSpleef(null);
		profile.setState(ProfileState.IN_LOBBY);
		profile.refreshHotbar();
		profile.handleVisibility();

		Practice.get().getEssentials().teleportToSpawn(player);
	}
}
