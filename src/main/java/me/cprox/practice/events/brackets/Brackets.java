package me.cprox.practice.events.brackets;

import lombok.Getter;
import lombok.Setter;
import me.cprox.practice.Practice;
import me.cprox.practice.events.brackets.player.BracketsPlayer;
import me.cprox.practice.events.brackets.player.BracketsPlayerState;
import me.cprox.practice.events.brackets.task.BracketsRoundEndTask;
import me.cprox.practice.events.brackets.task.BracketsRoundStartTask;
import me.cprox.practice.kit.Kit;
import me.cprox.practice.profile.Profile;
import me.cprox.practice.profile.enums.ProfileState;
import me.cprox.practice.util.PlayerSnapshot;
import me.cprox.practice.util.PlayerUtil;
import me.cprox.practice.util.chat.CC;
import me.cprox.practice.util.chat.Clickable;
import me.cprox.practice.util.external.Cooldown;
import me.cprox.practice.util.external.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Getter
public class Brackets {

	protected static String EVENT_PREFIX = Practice.get().getEventMessages().getString("EVENT.BRACKETS.PREFIX");
	private final String name;
	@Setter
	private BracketsState state = BracketsState.WAITING;
	@Getter
	@Setter
	private Kit kit;
	private BracketsTask eventTask;
	private final PlayerSnapshot host;
	private final LinkedHashMap<UUID, BracketsPlayer> eventPlayers = new LinkedHashMap<>();
	@Getter
	private final List<UUID> spectators = new ArrayList<>();
	@Getter
	@Setter
	public static int maxPlayers;
	@Getter
	@Setter
	private int totalPlayers;
	@Setter
	private Cooldown cooldown;
	private final List<Entity> entities = new ArrayList<>();
	private BracketsPlayer roundPlayerA;
	private BracketsPlayer roundPlayerB;
	@Setter
	private long roundStart;
	private int rounds = 1;
	@Getter
	@Setter
	private static boolean enabled = true;


	public Brackets(Player player, Kit kit) {
		this.name = player.getName();
		this.host = new PlayerSnapshot(player.getUniqueId(), player.getName());
		Brackets.maxPlayers = 100;
		this.kit = kit;
	}

	public List<String> getLore() {
		List<String> toReturn = new ArrayList<>();

		Brackets brackets = Practice.get().getBracketsManager().getActiveBrackets();

		toReturn.add(CC.MENU_BAR);
		toReturn.add(CC.translate("&4&lBrackets Event"));
		toReturn.add(CC.translate(""));
		toReturn.add(CC.translate("&7Host: &4" + brackets.getName()));

		if (brackets.isWaiting()) {
			toReturn.add("&7Players: &4" + brackets.getEventPlayers().size() + "&8/&4" + Brackets.getMaxPlayers());
			toReturn.add("");

			if (brackets.getCooldown() == null) {
				toReturn.add(CC.translate("&7Waiting for players..."));
				toReturn.add(CC.translate("&7Kit: &4" + kit.getName()));
			} else {
				String remaining = TimeUtil.millisToSeconds(brackets.getCooldown().getRemaining());

				if (remaining.startsWith("-")) {
					remaining = "0.0";
				}

				toReturn.add(CC.translate("&7Starting in: &4" + remaining));
				toReturn.add(CC.translate("&7Kit: &4" + kit.getName()));
			}
		} else {
			toReturn.add(CC.translate("&4&lBrackets Event"));
			toReturn.add(CC.translate(""));
			toReturn.add(CC.translate("&7Round Duration: &4" + brackets.getRoundDuration()));
			toReturn.add(CC.translate("&7Players: &4" + brackets.getRemainingPlayers().size() + "/" + brackets.getTotalPlayers()));
			toReturn.add(CC.translate(""));
			toReturn.add(CC.translate("&a" + brackets.getRoundPlayerA().getUsername() + " &8(&2" + PlayerUtil.getPing(brackets.getRoundPlayerA().getPlayer()) + "&7ms&8)"));
			toReturn.add(CC.translate("vs"));
			toReturn.add(CC.translate("&c" + brackets.getRoundPlayerB().getUsername() + " &8(&4" + PlayerUtil.getPing(brackets.getRoundPlayerB().getPlayer()) + "&7ms&8)"));
			toReturn.add(CC.translate(""));
		}
		toReturn.add(CC.MENU_BAR);

		return toReturn;
	}

	public void setEventTask(BracketsTask task) {
		if (eventTask != null) {
			eventTask.cancel();
		}

		eventTask = task;

		if (eventTask != null) {
			eventTask.runTaskTimer(Practice.get(), 0L, 20L);
		}
	}

	public boolean isWaiting() {
		return state == BracketsState.WAITING;
	}

	public boolean isFighting() {
		return state == BracketsState.ROUND_FIGHTING;
	}

	public BracketsPlayer getEventPlayer(Player player) {
		return eventPlayers.get(player.getUniqueId());
	}

	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<>();

		for (BracketsPlayer bracketsPlayer : eventPlayers.values()) {
			Player player = bracketsPlayer.getPlayer();

			if (player != null) {
				players.add(player);
			}
		}

		return players;
	}

	public List<Player> getRemainingPlayers() {
		List<Player> players = new ArrayList<>();

		for (BracketsPlayer bracketsPlayer : eventPlayers.values()) {
			if (bracketsPlayer.getState() == BracketsPlayerState.WAITING) {
				Player player = bracketsPlayer.getPlayer();
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

		eventPlayers.put(player.getUniqueId(), new BracketsPlayer(player));
		
		for (String string : Practice.get().getEventMessages().getStringList("EVENT.BRACKETS.PLAYER_JOINED")) {
			final String message = string
					.replace("<brackets_prefix>", EVENT_PREFIX)
					.replace("<player>", player.getName())
					.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
					.replace("<maxPlayers>", String.valueOf(getMaxPlayers()));
			broadcastMessage(message);
		}

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setBrackets(this);
		profile.setState(ProfileState.IN_EVENT);
		profile.refreshHotbar();
		player.teleport(Practice.get().getBracketsManager().getBracketsSpectator());

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
		
		if (state == BracketsState.WAITING) {
			for (String string : Practice.get().getEventMessages().getStringList("EVENT.BRACKETS.PLAYER_LEFT")) {
				final String message = string
						.replace("<brackets_prefix>", EVENT_PREFIX)
						.replace("<player>", player.getName())
						.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
						.replace("<maxPlayers>", String.valueOf(getMaxPlayers()));
				broadcastMessage(message);
			}
		}

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setState(ProfileState.IN_LOBBY);
		profile.setBrackets(null);
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
		BracketsPlayer loser = getEventPlayer(player);
		loser.setState(BracketsPlayerState.ELIMINATED);
		loser.getPlayer().setFireTicks(0);
		onDeath(player);
	}

	public void end() {
		Practice.get().getBracketsManager().setActiveBrackets(null);
		Practice.get().getBracketsManager().setCooldown(new Cooldown(60_000L * 10));

		setEventTask(null);

		Player winner = this.getWinner();

		if (winner == null) {
			Bukkit.broadcastMessage(Practice.get().getEventMessages().getString("EVENT.BRACKETS.CANCELED").replace("<prefix>", EVENT_PREFIX));
		} else {
			int rounds = (this.rounds - 1);
			for (String string : Practice.get().getEventMessages().getStringList("EVENT.BRACKETS.WINNER_ANNOUNCEMENT")) {
				final String message = string
						.replace("<brackets_prefix>", EVENT_PREFIX)
						.replace("<player>", getWinner().getName())
						.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
						.replace("<maxPlayers>", String.valueOf(getMaxPlayers()))
						.replace("<rounds>", String.valueOf(rounds));
				broadcastMessage(message);
			}
		}

		for (BracketsPlayer bracketsPlayer : eventPlayers.values()) {
			Player player = bracketsPlayer.getPlayer();

			if (player != null) {
				Profile profile = Profile.getByUuid(player.getUniqueId());
				profile.setState(ProfileState.IN_LOBBY);
				profile.setBrackets(null);
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

		for (BracketsPlayer bracketsPlayer : eventPlayers.values()) {
			if (bracketsPlayer.getState() == BracketsPlayerState.WAITING) {
				remaining++;
			}
		}

		return remaining == 1;
	}

	public Player getWinner() {
		for (BracketsPlayer bracketsPlayer : eventPlayers.values()) {
			if (bracketsPlayer.getState() != BracketsPlayerState.ELIMINATED) {
				return bracketsPlayer.getPlayer();
			}
		}

		return null;
	}

	public void announce() {

		List<String> strings = new ArrayList<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!eventPlayers.containsKey(player.getUniqueId())) {
				for (String string : Practice.get().getEventMessages().getStringList("EVENT.BRACKETS.STARTING")) {
					final String message = string
							.replace("<brackets_prefix>", EVENT_PREFIX)
							.replace("<player>", this.host.getUsername())
							.replace("<remainingPlayers>", String.valueOf(getRemainingPlayers().size()))
							.replace("<maxPlayers>", String.valueOf(getMaxPlayers()));
					player.sendMessage(CC.translate(message));
				}
			}
		}
		strings.add(CC.translate(Practice.get().getEventMessages().getString("EVENT.BRACKETS.CLICK_TO_JOIN")));
		for (String string : strings) {
			Clickable message = new Clickable(string, Practice.get().getEventMessages().getString("EVENT.BRACKETS.HOVER_CLICK_TO_JOIN"), "/brackets join");
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
		setState(BracketsState.ROUND_STARTING);

		if (roundPlayerA != null) {
			Player player = roundPlayerA.getPlayer();

			if (player != null) {
				player.teleport(Practice.get().getBracketsManager().getBracketsSpectator());

				Profile profile = Profile.getByUuid(player.getUniqueId());

				if (profile.isInBrackets()) {
					profile.refreshHotbar();
				}
			}

			roundPlayerA = null;
		}

		if (roundPlayerB != null) {
			Player player = roundPlayerB.getPlayer();

			if (player != null) {
				player.teleport(Practice.get().getBracketsManager().getBracketsSpectator());

				Profile profile = Profile.getByUuid(player.getUniqueId());

				if (profile.isInBrackets()) {
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

		playerA.teleport(Practice.get().getBracketsManager().getBracketsSpawn1());
		playerA.getInventory().setContents(getKit().getKitInventory().getContents());
		playerA.getInventory().setArmorContents(getKit().getKitInventory().getArmor());
		playerB.teleport(Practice.get().getBracketsManager().getBracketsSpawn2());
		playerB.getInventory().setContents(getKit().getKitInventory().getContents());
		playerB.getInventory().setArmorContents(getKit().getKitInventory().getArmor());
		for (String string : Practice.get().getEventMessages().getStringList("EVENT.BRACKETS.ROUND_STARTING")) {
			final String message = string
					.replace("<round>", String.valueOf(this.rounds))
					.replace("<brackets_prefix>", EVENT_PREFIX)
					.replace("<playerA>", playerA.getName())
					.replace("<playerB>", playerB.getName());
			broadcastMessage(message);
		}
		setEventTask(new BracketsRoundStartTask(this));
		this.rounds++;
	}

	public void onDeath(Player player) {
		BracketsPlayer winner = roundPlayerA.getUuid().equals(player.getUniqueId()) ? roundPlayerB : roundPlayerA;
		Brackets brackets = Practice.get().getBracketsManager().getActiveBrackets();
		winner.setState(BracketsPlayerState.WAITING);
		winner.incrementRoundWins();

		player.sendMessage(Practice.get().getEventMessages().getString("EVENT.ELIMINATED").replace("<brackets_prefix>", EVENT_PREFIX));

		if (player.getPlayer().getKiller() != winner.getPlayer()) {
			broadcastMessage(Practice.get().getEventMessages().getString("EVENT.BRACKETS.PLAYER_DIED").replace("<brackets_prefix>", EVENT_PREFIX).replace("<player>", player.getName()));
		} else {
			broadcastMessage(Practice.get().getEventMessages().getString("EVENT.BRACKETS.PLAYER_KILLED_PLAYER").replace("<brackets_prefix>", EVENT_PREFIX).replace("<killer>", winner.getPlayer().getName()).replace("<player>", player.getName()).replace("<remainingPlayers>", String.valueOf(brackets.getRemainingPlayers().size())).replace("<maxPlayers>", String.valueOf(getMaxPlayers())));
		}


		PlayerUtil.reset(player);
		player.setAllowFlight(true);
		player.setFlying(true);

		winner.getPlayer().hidePlayer(player);
		setState(BracketsState.ROUND_ENDING);
		setEventTask(new BracketsRoundEndTask(this));
	}

	public String getRoundDuration() {
		if (getState() == BracketsState.ROUND_STARTING) {
			return "00:00";
		} else if (getState() == BracketsState.ROUND_FIGHTING) {
			return TimeUtil.millisToTimer(System.currentTimeMillis() - roundStart);
		} else {
			return "Ending";
		}
	}

	public boolean isFighting(UUID uuid) {
		return (roundPlayerA != null && roundPlayerA.getUuid().equals(uuid)) || (roundPlayerB != null && roundPlayerB.getUuid().equals(uuid));
	}

	private BracketsPlayer findRoundPlayer() {
		BracketsPlayer bracketsPlayer = null;

		for (BracketsPlayer check : getEventPlayers().values()) {
			if (!isFighting(check.getUuid()) && check.getState() == BracketsPlayerState.WAITING) {
				if (bracketsPlayer == null) {
					bracketsPlayer = check;
					continue;
				}

				if (check.getRoundWins() == 0) {
					bracketsPlayer = check;
					continue;
				}

				if (check.getRoundWins() <= bracketsPlayer.getRoundWins()) {
					bracketsPlayer = check;
				}
			}
		}

		if (bracketsPlayer == null) {
			throw new RuntimeException("Could not find a new round player");
		}

		return bracketsPlayer;
	}

	public void addSpectator(Player player) {
		spectators.add(player.getUniqueId());

		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setBrackets(this);
		profile.setState(ProfileState.SPECTATE_MATCH);
		profile.refreshHotbar();
		profile.handleVisibility();
		PlayerUtil.spectator(player);
		player.setFlying(true);

		player.teleport(Practice.get().getBracketsManager().getBracketsSpectator());
	}

	public void removeSpectator(Player player) {
		spectators.remove(player.getUniqueId());
		PlayerUtil.reset(player);
		Profile profile = Profile.getByUuid(player.getUniqueId());
		profile.setBrackets(null);
		profile.setState(ProfileState.IN_LOBBY);
		profile.refreshHotbar();
		profile.handleVisibility();

		Practice.get().getEssentials().teleportToSpawn(player);
	}
}