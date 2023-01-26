package me.cprox.practice.events.sumo;

import lombok.Getter;
import lombok.Setter;
import me.cprox.practice.Practice;
import me.cprox.practice.events.sumo.player.SumoPlayer;
import me.cprox.practice.events.sumo.player.SumoPlayerState;
import me.cprox.practice.events.sumo.task.SumoRoundEndTask;
import me.cprox.practice.events.sumo.task.SumoRoundStartTask;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.util.PlayerSnapshot;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.chat.Clickable;
import me.cprox.practice.util.external.Cooldown;
import me.cprox.practice.util.external.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Getter
public class Sumo {

	protected static String EVENT_PREFIX = Practice.get().getEventMessages().getString("EVENT.SUMO.PREFIX");

	private final String name;
	@Setter private SumoState state = SumoState.WAITING;
	private SumoTask eventTask;
	private final PlayerSnapshot host;
	private final LinkedHashMap<UUID, SumoPlayer> eventPlayers = new LinkedHashMap<>();
	@Getter final private List<UUID> spectators = new ArrayList<>();
	@Getter @Setter	public static int maxPlayers;
	@Getter @Setter private int totalPlayers;
	@Setter private Cooldown cooldown;
	private SumoPlayer roundPlayerA;
	private int rounds = 1;
	private SumoPlayer roundPlayerB;
	@Setter	private long roundStart;
	@Getter	@Setter	private static boolean enabled = true;

	public Sumo(Player player) {
		this.name = player.getName();
		this.host = new PlayerSnapshot(player.getUniqueId(), player.getName());
		maxPlayers = 100;
	}

	public List<String> getLore() {
		List<String> toReturn = new ArrayList<>();

		Sumo sumo = Practice.get().getSumoManager().getActiveSumo();

		toReturn.add(CC.MENU_BAR);
		toReturn.add("&4&lSumo Event");
		toReturn.add("");
		toReturn.add("&7Host: &4" + sumo.getName());

		if (sumo.isWaiting()) {
			toReturn.add("&7Players: &4" + sumo.getEventPlayers().size() + "&8/&4" + Sumo.getMaxPlayers());
			toReturn.add("");

			if (sumo.getCooldown() == null) {
				toReturn.add(CC.translate("&7Waiting for players..."));
			} else {
				String remaining = TimeUtil.millisToSeconds(sumo.getCooldown().getRemaining());

				if (remaining.startsWith("-")) {
					remaining = "0.0";
				}

				toReturn.add("&7Starting in: &4" + remaining);
			}
		} else {
			toReturn.add("&4&lSumo Event");
			toReturn.add("");
			toReturn.add("&7Round Duration: &4" + sumo.getRoundDuration());
			toReturn.add("&7Players: &4" + sumo.getRemainingPlayers().size() + "&8/&4" + sumo.getTotalPlayers());
			toReturn.add("");
			toReturn.add("&a" + sumo.getRoundPlayerA().getUsername() + " &8(&2" + PlayerUtil.getPing(sumo.getRoundPlayerA().getPlayer()) + "&7ms&8)");
			toReturn.add("vs");
			toReturn.add("&c" + sumo.getRoundPlayerB().getUsername() + " &8(&4" + PlayerUtil.getPing(sumo.getRoundPlayerB().getPlayer()) + "&7ms&8)");
			toReturn.add("");
		}
		toReturn.add(CC.MENU_BAR);

		return toReturn;
	}

	public void setEventTask(SumoTask task) {
		if (eventTask != null) {
			eventTask.cancel();
		}

		eventTask = task;

		if (eventTask != null) {
			eventTask.runTaskTimer(Practice.get(), 0L, 20L);
		}
	}

	public boolean isWaiting() {
		return state == SumoState.WAITING;
	}

	public boolean isFighting() {
		return state == SumoState.ROUND_FIGHTING;
	}

	public SumoPlayer getEventPlayer(Player player) {
		return eventPlayers.get(player.getUniqueId());
	}

	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<>();

		for (SumoPlayer sumoPlayer : eventPlayers.values()) {
			Player player = sumoPlayer.getPlayer();

			if (player != null) {
				players.add(player);
			}
		}

		return players;
	}

	public List<Player> getRemainingPlayers() {
		List<Player> players = new ArrayList<>();

		for (SumoPlayer sumoPlayer : eventPlayers.values()) {
			if (sumoPlayer.getState() == SumoPlayerState.WAITING) {
				Player player = sumoPlayer.getPlayer();
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
		
		eventPlayers.put(player.getUniqueId(), new SumoPlayer(player));
		
		for (String string : Practice.get().getEventMessages().getStringList("EVENT.SUMO.PLAYER_JOINED")) {
			final String message = string
					.replace("<sumo_prefix>", EVENT_PREFIX)
					.replace("<player>", player.getName())
					.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
					.replace("<maxPlayers>", String.valueOf(getMaxPlayers()));
			broadcastMessage(message);
		}

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setSumo(this);
		profile.setState(ProfileState.IN_EVENT);
		profile.refreshHotbar();
		player.teleport(Practice.get().getSumoManager().getSumoSpectator());

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
		if (isFighting(player.getUniqueId())) {
			handleDeath(player);
		}

		eventPlayers.remove(player.getUniqueId());

		if (state == SumoState.WAITING) {
			for (String string : Practice.get().getEventMessages().getStringList("EVENT.SUMO.PLAYER_LEFT")) {
				final String message = string
						.replace("<sumo_prefix>", EVENT_PREFIX)
						.replace("<player>", player.getName())
						.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
						.replace("<maxPlayers>", String.valueOf(getMaxPlayers()));
				broadcastMessage(message);
			}
		}

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setState(ProfileState.IN_LOBBY);
		profile.setSumo(null);
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
		SumoPlayer loser = getEventPlayer(player);
		loser.setState(SumoPlayerState.ELIMINATED);

		onDeath(player);
	}

	public void end() {
		Practice.get().getSumoManager().setActiveSumo(null);
		Practice.get().getSumoManager().setCooldown(new Cooldown(60_000L * 10));

		setEventTask(null);

		Player winner = this.getWinner();

		if (winner == null) {
			Bukkit.broadcastMessage(Practice.get().getEventMessages().getString("EVENT.SUMO.CANCELED").replace("<prefix>", EVENT_PREFIX));
		} else {
			int rounds = (this.rounds - 1);
			for (String string : Practice.get().getEventMessages().getStringList("EVENT.SUMO.WINNER_ANNOUNCEMENT")) {
				final String message = string
						.replace("<sumo_prefix>", EVENT_PREFIX)
						.replace("<player>", getWinner().getName())
						.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
						.replace("<maxPlayers>", String.valueOf(getMaxPlayers()))
						.replace("<rounds>", String.valueOf(rounds));
				broadcastMessage(message);
			}
		}

		for (SumoPlayer sumoPlayer : eventPlayers.values()) {
			Player player = sumoPlayer.getPlayer();

			if (player != null) {
				Profile profile = Profile.getByUuid(player.getUniqueId());
				profile.setState(ProfileState.IN_LOBBY);
				profile.setSumo(null);
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

		for (SumoPlayer sumoPlayer : eventPlayers.values()) {
			if (sumoPlayer.getState() == SumoPlayerState.WAITING) {
				remaining++;
			}
		}

		return remaining == 1;
	}

	public Player getWinner() {
		for (SumoPlayer sumoPlayer : eventPlayers.values()) {
			if (sumoPlayer.getState() != SumoPlayerState.ELIMINATED) {
				return sumoPlayer.getPlayer();
			}
		}

		return null;
	}

	public void announce() {
		List<String> strings = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!eventPlayers.containsKey(player.getUniqueId())) {
				for (String string : Practice.get().getEventMessages().getStringList("EVENT.SUMO.STARTING")) {
					final String message = string
							.replace("<sumo_prefix>", EVENT_PREFIX)
							.replace("<player>", this.host.getUsername())
							.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
							.replace("<maxPlayers>", String.valueOf(getMaxPlayers()));
					player.sendMessage(CC.translate(message));
				}
			}
		}
		strings.add(CC.translate(Practice.get().getEventMessages().getString("EVENT.SUMO.CLICK_TO_JOIN")));
		for (String string : strings) {
			Clickable message = new Clickable(string, Practice.get().getEventMessages().getString("EVENT.SUMO.HOVER_CLICK_TO_JOIN"), "/sumo join");
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (!eventPlayers.containsKey(player.getUniqueId())) {
					message.sendToPlayer(player);
				}
			}
		}
	}

	public void broadcastMessage(String message) {
		for (Player player : getPlayers()) {
			player.sendMessage(CC.translate(message));
		}
	}

	public void onRound() {
		setState(SumoState.ROUND_STARTING);
		if (roundPlayerA != null) {
			Player player = roundPlayerA.getPlayer();

			if (player != null) {
				player.teleport(Practice.get().getSumoManager().getSumoSpectator());

				Profile profile = Profile.getByUuid(player.getUniqueId());

				if (profile.isInSumo()) {
					profile.refreshHotbar();
				}
			}

			roundPlayerA = null;
		}

		if (roundPlayerB != null) {
			Player player = roundPlayerB.getPlayer();

			if (player != null) {
				player.teleport(Practice.get().getSumoManager().getSumoSpectator());

				Profile profile = Profile.getByUuid(player.getUniqueId());

				if (profile.isInSumo()) {
					profile.refreshHotbar();
				}
			}

			roundPlayerB = null;
		}

		roundPlayerA = findRoundPlayer();
		roundPlayerB = findRoundPlayer();

		Player playerA = roundPlayerA.getPlayer();
		Player playerB = roundPlayerB.getPlayer();

		PlayerUtil.reset(playerA);
		PlayerUtil.reset(playerB);

		PlayerUtil.denyMovement(playerA);
		PlayerUtil.denyMovement(playerB);

		playerA.teleport(Practice.get().getSumoManager().getSumoSpawn1());
		playerB.teleport(Practice.get().getSumoManager().getSumoSpawn2());
		for (String string : Practice.get().getEventMessages().getStringList("EVENT.SUMO.ROUND_STARTING")) {
			final String message = string
					.replace("<round>", String.valueOf(this.rounds))
					.replace("<sumo_prefix>", EVENT_PREFIX)
					.replace("<playerA>", playerA.getName())
					.replace("<playerB>", playerB.getName());
			broadcastMessage(message);
		}
		setEventTask(new SumoRoundStartTask(this));
	}

	public void onDeath(Player player) {
		SumoPlayer winner = roundPlayerA.getUuid().equals(player.getUniqueId()) ? roundPlayerB : roundPlayerA;
		Sumo sumo = Practice.get().getSumoManager().getActiveSumo();
		winner.setState(SumoPlayerState.WAITING);
		winner.incrementRoundWins();
		winner.getPlayer().teleport(Practice.get().getSumoManager().getSumoSpectator());

		player.sendMessage(Practice.get().getEventMessages().getString("EVENT.ELIMINATED").replace("<sumo_prefix>", EVENT_PREFIX));

		if (player.getPlayer().getKiller() != winner.getPlayer()) {
			broadcastMessage(Practice.get().getEventMessages().getString("EVENT.SUMO.PLAYER_DIED").replace("<sumo_prefix>", EVENT_PREFIX).replace("<player>", player.getName()));
		} else {
			broadcastMessage(Practice.get().getEventMessages().getString("EVENT.SUMO.PLAYER_KILLED_PLAYER").replace("<sumo_prefix>", EVENT_PREFIX).replace("<killer>", winner.getPlayer().getName()).replace("<player>", player.getName()).replace("<remainingPlayers>", String.valueOf(sumo.getRemainingPlayers().size())).replace("<maxPlayers>", String.valueOf(getMaxPlayers())));
		}

		setState(SumoState.ROUND_ENDING);
		setEventTask(new SumoRoundEndTask(this));
		this.rounds++;
	}

	public String getRoundDuration() {
		if (getState() == SumoState.ROUND_STARTING) {
			return "00:00";
		} else if (getState() == SumoState.ROUND_FIGHTING) {
			return TimeUtil.millisToTimer(System.currentTimeMillis() - roundStart);
		} else {
			return "Ending";
		}
	}

	public boolean isFighting(UUID uuid) {
		return (roundPlayerA != null && roundPlayerA.getUuid().equals(uuid)) || (roundPlayerB != null && roundPlayerB.getUuid().equals(uuid));
	}

	private SumoPlayer findRoundPlayer() {
		SumoPlayer sumoPlayer = null;

		for (SumoPlayer check : getEventPlayers().values()) {
			if (!isFighting(check.getUuid()) && check.getState() == SumoPlayerState.WAITING) {
				if (sumoPlayer == null) {
					sumoPlayer = check;
					continue;
				}

				if (check.getRoundWins() == 0) {
					sumoPlayer = check;
					continue;
				}

				if (check.getRoundWins() <= sumoPlayer.getRoundWins()) {
					sumoPlayer = check;
				}
			}
		}

		if (sumoPlayer == null) {
			throw new RuntimeException("Could not find a new round player");
		}

		return sumoPlayer;
	}

	public void addSpectator(Player player) {
		spectators.add(player.getUniqueId());

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setSumo(this);
		player.setFlying(true);
		profile.setState(ProfileState.SPECTATE_MATCH);
		profile.refreshHotbar();
		profile.handleVisibility();

		player.teleport(Practice.get().getSumoManager().getSumoSpawn1());
	}

	public void removeSpectator(Player player) {
		spectators.remove(player.getUniqueId());

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setSumo(null);
		player.setFlying(false);
		profile.setState(ProfileState.IN_LOBBY);
		profile.refreshHotbar();
		profile.handleVisibility();

		Practice.get().getEssentials().teleportToSpawn(player);
	}

}
