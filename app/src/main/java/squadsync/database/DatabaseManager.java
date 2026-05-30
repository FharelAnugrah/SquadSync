package squadsync.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import squadsync.model.Player;
import squadsync.model.Training;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;
    private static final String URL = "jdbc:sqlite:squadsync.db";

    private DatabaseManager() {
        connect();
        createTables();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(URL);
            System.out.println("Connected to SQLite database.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
        }
        return connection;
    }

    private void createTables() {
        String createPlayers = "CREATE TABLE IF NOT EXISTS Players (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "age INTEGER," +
                "position TEXT," +
                "number INTEGER," +
                "score INTEGER" +
                ")";

        String createTraining = "CREATE TABLE IF NOT EXISTS Training (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT," +
                "day TEXT," +
                "time TEXT," +
                "activity TEXT," +
                "location TEXT" +
                ")";

        String createAttendance = "CREATE TABLE IF NOT EXISTS Attendance (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_id INTEGER," +
                "training_id INTEGER," +
                "status TEXT," +
                "FOREIGN KEY(player_id) REFERENCES Players(id)," +
                "FOREIGN KEY(training_id) REFERENCES Training(id)" +
                ")";

        String createStatistics = "CREATE TABLE IF NOT EXISTS Statistics (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_id INTEGER," +
                "stamina INTEGER," +
                "passing INTEGER," +
                "speed INTEGER," +
                "shooting INTEGER," +
                "FOREIGN KEY(player_id) REFERENCES Players(id)" +
                ")";

        String createUsers = "CREATE TABLE IF NOT EXISTS Users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayers);
            stmt.execute(createTraining);
            stmt.execute(createAttendance);
            stmt.execute(createStatistics);
            stmt.execute(createUsers);
            
            // Migration: Add 'date' column to Training if missing
            try {
                stmt.execute("ALTER TABLE Training ADD COLUMN date TEXT");
                System.out.println("Migration: Added 'date' column to Training table.");
            } catch (SQLException e) {
                // Column probably already exists, ignore
            }
            
            // Add default admin if not exists
            String checkAdmin = "SELECT COUNT(*) FROM Users WHERE username='admin'";
            ResultSet rs = stmt.executeQuery(checkAdmin);
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO Users(username, password, role) VALUES('admin', 'admin123', 'MANAGER')");
            }
            
            System.out.println("Tables created or verified.");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    // --- User Management ---

    public boolean registerUser(String username, String password, String role) {
        String sql = "INSERT INTO Users(username, password, role) VALUES(?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, "MANAGER"); // Always Manager
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public squadsync.model.UserRole authenticate(String username, String password) {
        String sql = "SELECT role FROM Users WHERE username=? AND password=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return squadsync.model.UserRole.valueOf(rs.getString("role"));
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating: " + e.getMessage());
        }
        return null;
    }

    // --- Player CRUD ---

    public void addPlayer(Player player) {
        String sql = "INSERT INTO Players(name, age, position, number, score) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, capitalize(player.getName()));
            pstmt.setInt(2, player.getAge());
            pstmt.setString(3, player.getPosition());
            pstmt.setInt(4, player.getNumber());
            pstmt.setInt(5, player.getScore());
            pstmt.executeUpdate();
            
            // Get the generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int newId = rs.getInt(1);
                // Initialize stats to the initial score
                saveStatistic(new squadsync.model.Statistic(0, newId, player.getScore(), player.getScore(), player.getScore(), player.getScore()));
            }
        } catch (SQLException e) {
            System.err.println("Error adding player: " + e.getMessage());
        }
    }

    public Player getPlayerByName(String name) {
        String sql = "SELECT * FROM Players WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("position"),
                        rs.getInt("number"),
                        rs.getInt("score")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting player by name: " + e.getMessage());
        }
        return null;
    }

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM Players";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                players.add(new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("position"),
                        rs.getInt("number"),
                        rs.getInt("score")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all players: " + e.getMessage());
        }
        return players;
    }

    public void updatePlayer(Player player) {
        String sql = "UPDATE Players SET name=?, age=?, position=?, number=?, score=? WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, capitalize(player.getName()));
            pstmt.setInt(2, player.getAge());
            pstmt.setString(3, player.getPosition());
            pstmt.setInt(4, player.getNumber());
            pstmt.setInt(5, player.getScore());
            pstmt.setInt(6, player.getId());
            pstmt.executeUpdate();
            
            // Sync: If score is updated in Player list, set all stats to that value
            saveStatistic(new squadsync.model.Statistic(0, player.getId(), player.getScore(), player.getScore(), player.getScore(), player.getScore()));
            
        } catch (SQLException e) {
            System.err.println("Error updating player: " + e.getMessage());
        }
    }

    public void deletePlayer(int id) {
        String sql = "DELETE FROM Players WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            
            // Also delete stats and attendance
            try (Statement st = connection.createStatement()) {
                st.execute("DELETE FROM Statistics WHERE player_id=" + id);
                st.execute("DELETE FROM Attendance WHERE player_id=" + id);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting player: " + e.getMessage());
        }
    }

    // --- Training CRUD ---

    public void addTraining(Training training) {
        String sql = "INSERT INTO Training(date, day, time, activity, location) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, training.getDate());
            pstmt.setString(2, training.getDay());
            pstmt.setString(3, training.getTime());
            pstmt.setString(4, training.getActivity());
            pstmt.setString(5, training.getLocation());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding training: " + e.getMessage());
        }
    }

    public List<Training> getAllTrainings() {
        List<Training> trainings = new ArrayList<>();
        String sql = "SELECT * FROM Training ORDER BY date DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                trainings.add(new Training(
                        rs.getInt("id"),
                        rs.getString("date"),
                        rs.getString("day"),
                        rs.getString("time"),
                        rs.getString("activity"),
                        rs.getString("location")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting trainings: " + e.getMessage());
        }
        return trainings;
    }

    public void updateTraining(Training training) {
        String sql = "UPDATE Training SET date=?, day=?, time=?, activity=?, location=? WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, training.getDate());
            pstmt.setString(2, training.getDay());
            pstmt.setString(3, training.getTime());
            pstmt.setString(4, training.getActivity());
            pstmt.setString(5, training.getLocation());
            pstmt.setInt(6, training.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating training: " + e.getMessage());
        }
    }

    public void deleteTraining(int id) {
        String sql = "DELETE FROM Training WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting training: " + e.getMessage());
        }
    }

    // --- Attendance CRUD ---

    public void saveAttendance(squadsync.model.Attendance attendance) {
        String sql = "INSERT INTO Attendance(player_id, training_id, status) VALUES(?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, attendance.getPlayerId());
            pstmt.setInt(2, attendance.getTrainingId());
            pstmt.setString(3, attendance.getStatus());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving attendance: " + e.getMessage());
        }
    }

    public List<squadsync.model.Attendance> getAttendanceByTraining(int trainingId) {
        List<squadsync.model.Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE training_id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, trainingId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new squadsync.model.Attendance(
                        rs.getInt("id"),
                        rs.getInt("player_id"),
                        rs.getInt("training_id"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting attendance: " + e.getMessage());
        }
        return list;
    }

    public void clearAttendanceForTraining(int trainingId) {
        String sql = "DELETE FROM Attendance WHERE training_id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, trainingId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing attendance: " + e.getMessage());
        }
    }

    // --- Statistics CRUD ---

    public squadsync.model.Statistic getStatisticByPlayer(int playerId) {
        String sql = "SELECT * FROM Statistics WHERE player_id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new squadsync.model.Statistic(
                        rs.getInt("id"),
                        rs.getInt("player_id"),
                        rs.getInt("stamina"),
                        rs.getInt("passing"),
                        rs.getInt("speed"),
                        rs.getInt("shooting")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error getting statistic: " + e.getMessage());
        }
        return null;
    }

    public List<squadsync.model.Statistic> getAllStatistics() {
        List<squadsync.model.Statistic> stats = new ArrayList<>();
        String sql = "SELECT * FROM Statistics";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.add(new squadsync.model.Statistic(
                        rs.getInt("id"),
                        rs.getInt("player_id"),
                        rs.getInt("stamina"),
                        rs.getInt("passing"),
                        rs.getInt("speed"),
                        rs.getInt("shooting")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all statistics: " + e.getMessage());
        }
        return stats;
    }

    public void saveStatistic(squadsync.model.Statistic stat) {
        if (getStatisticByPlayer(stat.getPlayerId()) != null) {
            String sql = "UPDATE Statistics SET stamina=?, passing=?, speed=?, shooting=? WHERE player_id=?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, stat.getStamina());
                pstmt.setInt(2, stat.getPassing());
                pstmt.setInt(3, stat.getSpeed());
                pstmt.setInt(4, stat.getShooting());
                pstmt.setInt(5, stat.getPlayerId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error updating statistic: " + e.getMessage());
            }
        } else {
            String sql = "INSERT INTO Statistics(player_id, stamina, passing, speed, shooting) VALUES(?,?,?,?,?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, stat.getPlayerId());
                pstmt.setInt(2, stat.getStamina());
                pstmt.setInt(3, stat.getPassing());
                pstmt.setInt(4, stat.getSpeed());
                pstmt.setInt(5, stat.getShooting());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Error inserting statistic: " + e.getMessage());
            }
        }

        // Sync: Calculate average score and update Players table
        int avgScore = (stat.getStamina() + stat.getPassing() + stat.getSpeed() + stat.getShooting()) / 4;
        String updatePlayerSql = "UPDATE Players SET score=? WHERE id=?";
        try (PreparedStatement pstmt = connection.prepareStatement(updatePlayerSql)) {
            pstmt.setInt(1, avgScore);
            pstmt.setInt(2, stat.getPlayerId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error syncing score back to player: " + e.getMessage());
        }
    }
}
