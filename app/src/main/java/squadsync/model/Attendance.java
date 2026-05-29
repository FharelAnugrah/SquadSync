package squadsync.model;

public class Attendance {
    private int id;
    private int playerId;
    private int trainingId;
    private String status;

    public Attendance(int id, int playerId, int trainingId, String status) {
        this.id = id;
        this.playerId = playerId;
        this.trainingId = trainingId;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    public int getTrainingId() { return trainingId; }
    public void setTrainingId(int trainingId) { this.trainingId = trainingId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
