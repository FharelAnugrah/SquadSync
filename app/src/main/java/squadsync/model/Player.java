package squadsync.model;

public class Player extends Person {
    private String position;
    private int number;
    private int score;

    public Player(int id, String name, int age, String position, int number, int score) {
        super(id, name, age);
        this.position = position;
        this.number = number;
        this.score = score;
    }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
