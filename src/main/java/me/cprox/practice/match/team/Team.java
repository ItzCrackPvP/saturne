package me.cprox.practice.match.team;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Team {

    @Getter
    @Setter
    private TeamPlayer leader;
    private final List<TeamPlayer> teamPlayers;
    @Getter @Setter  private int kothPoints;
    @Getter @Setter private int sumoRounds = 0;

    public Team(TeamPlayer leader) {
        this.leader = leader;
        this.teamPlayers = new ArrayList<>();
        this.teamPlayers.add(this.leader);
    }

    public boolean isLeader(UUID uuid) {
        return this.leader.getUuid().equals(uuid);
    }

    public boolean containsPlayer(Player player) {
        for (TeamPlayer playerInfo : this.teamPlayers) {
            if (playerInfo.getUuid().equals(player.getUniqueId())) {
                return true;
            }
        }

        return false;
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();

        this.teamPlayers.forEach(matchPlayer -> {
            Player player = matchPlayer.getPlayer();

            if (player != null) {
                players.add(player);
            }
        });

        return players;
    }

    public int getTotalHits() {
        int toReturn = 0;
        for (TeamPlayer teamPlayer : this.teamPlayers) {
            toReturn += teamPlayer.getHits();
        }
        return toReturn;
    }

    /**
     * Returns a list of objects that extend {@link TeamPlayer} whose {@link TeamPlayer#isAlive()} returns true.
     *
     * @return A list of team players that are alive.
     */
    public List<TeamPlayer> getAliveTeamPlayers() {
        List<TeamPlayer> alive = new ArrayList<>();

        this.teamPlayers.forEach(teamPlayer -> {
            if (teamPlayer.isAlive()) {
                alive.add(teamPlayer);
            }
        });

        return alive;
    }

    /**
     * Returns an integer that is incremented for each {@link TeamPlayer} element in the {@code teamPlayers} list whose
     * {@link TeamPlayer#isAlive()} returns true.
     * <p>
     * Use this method rather than calling {@link List#size()} on the result of {@code getAliveTeamPlayers}.
     *
     * @return The count of team players that are alive.
     */
    public int getAliveCount() {
        if (this.teamPlayers.size() == 1) {
            return this.leader.isAlive() ? 1 : 0;
        } else {
            int alive = 0;

            for (TeamPlayer teamPlayer : this.teamPlayers) {
                if (teamPlayer.isAlive()) {
                    alive++;
                }
            }

            return alive;
        }
    }

    public void broadcast(String messages) {
        this.getPlayers().forEach(player -> player.sendMessage(messages));
    }

    public void broadcast(List<String> messages) {
        this.getPlayers().forEach(player -> messages.forEach(player::sendMessage));
    }


}
