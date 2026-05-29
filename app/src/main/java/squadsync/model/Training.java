package squadsync.model;

public class Training {
    private int id;
    private String date; // YYYY-MM-DD
    private String day;
    private String time;
    private String activity;
    private String location;

    public Training(int id, String date, String day, String time, String activity, String location) {
        this.id = id;
        this.date = date;
        this.day = day;
        this.time = time;
        this.activity = activity;
        this.location = location;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    @Override
    public String toString() {
        return date + " (" + time + ") - " + activity;
    }
}
