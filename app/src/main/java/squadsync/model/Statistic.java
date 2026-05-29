package squadsync.model;

public class Statistic {
    private int id;
    private int playerId;
    private int stamina;
    private int passing;
    private int speed;
    private int shooting;

    public Statistic(int id, int playerId, int stamina, int passing, int speed, int shooting) {
        this.id = id;
        this.playerId = playerId;
        this.stamina = stamina;
        this.passing = passing;
        this.speed = speed;
        this.shooting = shooting;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public int getStamina() { return stamina; }
    public void setStamina(int stamina) { this.stamina = stamina; }

    public int getPassing() { return passing; }
    public void setPassing(int passing) { this.passing = passing; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public int getShooting() { return shooting; }
    public void setShooting(int shooting) { this.shooting = shooting; }
}
