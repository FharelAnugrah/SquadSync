package squadsync.model;

import java.util.List;

public class Team {
    private List<Player> players;
    private Coach coach;

    public Team(List<Player> players, Coach coach) {
        this.players = players;
        this.coach = coach;
    }

    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }

    public Coach getCoach() { return coach; }
    public void setCoach(Coach coach) { this.coach = coach; }
}
