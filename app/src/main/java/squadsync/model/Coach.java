package squadsync.model;

public class Coach extends Person {
    private String specialty;

    public Coach(int id, String name, int age, String specialty) {
        super(id, name, age);
        this.specialty = specialty;
    }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}
