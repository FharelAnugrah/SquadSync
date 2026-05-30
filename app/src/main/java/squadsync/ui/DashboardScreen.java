package squadsync.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import squadsync.model.UserRole;

public class DashboardScreen {
    private Stage stage;
    private BorderPane root;
    private UserRole role;
    private String username;
    private List<Button> sidebarButtons = new ArrayList<>();

    public DashboardScreen(Stage stage, UserRole role, String username) {
        this.stage = stage;
        this.role = role;
        this.username = username;
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");
    }

    public void show() {
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // Show default view
        loadHomeView();

        Scene scene = new Scene(root, 1200, 800);
        
        // Responsive listener
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() < 800) sidebar.setPrefWidth(80);
            else sidebar.setPrefWidth(260);
        });

        stage.setTitle("SquadSync - Dashboard Utama");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-border-color: rgba(0,0,0,0.05); -fx-border-width: 0 1 0 0;");
        sidebar.setPadding(new Insets(40, 15, 40, 15));

        Label logo = new Label("⚽ SQUADSYNC");
        logo.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 22));
        logo.setTextFill(Color.web(UIUtils.COLOR_ACCENT));
        logo.setPadding(new Insets(0, 15, 45, 15));
        logo.setStyle("-fx-letter-spacing: 2px;");
        
        // Hide logo text on small sidebar
        sidebar.widthProperty().addListener((obs, old, newVal) -> {
            if (newVal.doubleValue() < 150) logo.setText("⚽");
            else logo.setText("⚽ SQUADSYNC");
        });

        sidebar.getChildren().add(logo);

        String[][] menus = {
            {"🏠 Beranda", "Dashboard"},
            {"👥 Pemain", "Players"},
            {"📅 Latihan", "Training"},
            {"✅ Absensi", "Attendance"},
            {"📊 Statistik", "Statistics"},
            {"🏆 Peringkat", "Ranking"}
        };

        for (String[] menuInfo : menus) {
            Button btn = UIUtils.createSidebarButton(menuInfo[0]);
            sidebarButtons.add(btn);
            btn.setOnAction(e -> handleMenuClick(btn, menuInfo[1]));
            
            // Icon only mode for small sidebar
            sidebar.widthProperty().addListener((obs, old, newVal) -> {
                if (newVal.doubleValue() < 150) btn.setText(menuInfo[0].substring(0, 2));
                else btn.setText(menuInfo[0]);
            });

            sidebar.getChildren().add(btn);
        }

        resetSidebarStyles();
        setActiveButton(sidebarButtons.get(0));

        Button logoutBtn = UIUtils.createSidebarButton("🚪 Keluar");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + UIUtils.COLOR_DANGER + "; -fx-font-size: 15; -fx-font-weight: bold; -fx-background-radius: 12;");
        logoutBtn.setOnAction(e -> new LoginScreen(stage).show());
        
        sidebar.widthProperty().addListener((obs, old, newVal) -> {
            if (newVal.doubleValue() < 150) logoutBtn.setText("🚪");
            else logoutBtn.setText("🚪 Keluar");
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        sidebar.getChildren().addAll(spacer, logoutBtn);

        return sidebar;
    }

    private void handleMenuClick(Button clickedBtn, String menuType) {
        setActiveButton(clickedBtn);
        
        VBox view = null;
        if (menuType.equals("Dashboard")) { loadHomeView(); return; }
        else if (menuType.equals("Players")) view = new PlayerView().getView();
        else if (menuType.equals("Training")) view = new TrainingView(role).getView();
        else if (menuType.equals("Attendance")) view = new AttendanceView().getView();
        else if (menuType.equals("Statistics")) view = new StatisticsView(role, username).getView();
        else if (menuType.equals("Ranking")) view = new RankingView().getView();
        
        if (view != null) {
            view.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");
            VBox.setVgrow(view, Priority.ALWAYS);
            root.setCenter(view);
        }
    }

    private void resetSidebarStyles() {
        for (Button btn : sidebarButtons) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + UIUtils.COLOR_TEXT_SECONDARY + "; -fx-font-size: 15; -fx-font-weight: 600; -fx-background-radius: 12;");
            btn.setTextFill(Color.web(UIUtils.COLOR_TEXT_SECONDARY));
        }
    }

    private void setActiveButton(Button activeBtn) {
        resetSidebarStyles();
        activeBtn.setStyle("-fx-background-color: " + UIUtils.COLOR_ACCENT + "; -fx-text-fill: #FFFFFF; -fx-font-size: 15; -fx-font-weight: 800; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(59, 130, 246, 0.4), 15, 0, 0, 0);");
        activeBtn.setTextFill(Color.WHITE);
    }

    private void loadHomeView() {
        VBox content = new VBox(40);
        content.setPadding(new Insets(50));
        content.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");
        VBox.setVgrow(content, Priority.ALWAYS);
        
        VBox header = new VBox(8);
        Label welcome = new Label("Selamat datang kembali, " + username);
        welcome.setFont(Font.font("System", 16));
        welcome.setTextFill(Color.web(UIUtils.COLOR_TEXT_SECONDARY));
        
        Label title = new Label("Dashboard Manajer");
        title.setFont(Font.font("System", FontWeight.BLACK, 36));
        title.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));
        header.getChildren().addAll(welcome, title);

        FlowPane cardsPane = new FlowPane(25, 25);
        cardsPane.setAlignment(Pos.TOP_LEFT);
        cardsPane.setPadding(new Insets(10, 0, 10, 0));
        
        // Responsive wrapping logic
        cardsPane.prefWrapLengthProperty().bind(content.widthProperty().subtract(100));
        
        int totalPlayers = squadsync.database.DatabaseManager.getInstance().getAllPlayers().size();
        
        java.util.List<squadsync.model.Player> players = squadsync.database.DatabaseManager.getInstance().getAllPlayers();
        String bestPlayerName = "-";
        int avgScore = 0;
        if (!players.isEmpty()) {
            squadsync.model.Player best = players.get(0);
            int totalScore = 0;
            for (squadsync.model.Player p : players) {
                totalScore += p.getScore();
                if (p.getScore() > best.getScore()) {
                    best = p;
                }
            }
            bestPlayerName = best.getName();
            avgScore = totalScore / players.size();
        }

        java.util.List<squadsync.model.Training> trainings = squadsync.database.DatabaseManager.getInstance().getAllTrainings();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        squadsync.model.Training nextTraining = trainings.stream()
            .filter(t -> {
                try {
                    java.time.LocalDate d = java.time.LocalDate.parse(t.getDate());
                    java.time.LocalTime time;
                    if (t.getTime().contains(":")) {
                        time = java.time.LocalTime.parse(t.getTime());
                    } else if (t.getTime().length() == 4) {
                        time = java.time.LocalTime.of(Integer.parseInt(t.getTime().substring(0, 2)), Integer.parseInt(t.getTime().substring(2)));
                    } else {
                        return false;
                    }
                    return !java.time.LocalDateTime.of(d, time).isBefore(now);
                } catch (Exception ex) {
                    return false;
                }
            })
            .min((t1, t2) -> {
                java.time.LocalDateTime ldt1 = java.time.LocalDateTime.of(java.time.LocalDate.parse(t1.getDate()), 
                    t1.getTime().contains(":") ? java.time.LocalTime.parse(t1.getTime()) : java.time.LocalTime.of(Integer.parseInt(t1.getTime().substring(0, 2)), Integer.parseInt(t1.getTime().substring(2))));
                java.time.LocalDateTime ldt2 = java.time.LocalDateTime.of(java.time.LocalDate.parse(t2.getDate()), 
                    t2.getTime().contains(":") ? java.time.LocalTime.parse(t2.getTime()) : java.time.LocalTime.of(Integer.parseInt(t2.getTime().substring(0, 2)), Integer.parseInt(t2.getTime().substring(2))));
                return ldt1.compareTo(ldt2);
            })
            .orElse(null);

        cardsPane.getChildren().addAll(
            UIUtils.createCard("Total Pemain", String.valueOf(totalPlayers), "👥"),
            createNextTrainingCard(nextTraining),
            UIUtils.createCard("Pemain Terbaik", bestPlayerName, "⭐"),
            UIUtils.createCard("Rata-rata Skor", String.valueOf(avgScore), "📈")
        );

        content.getChildren().addAll(header, cardsPane);
        root.setCenter(content);
    }

    private VBox createNextTrainingCard(squadsync.model.Training training) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.setPrefSize(260, 160);
        card.setStyle("-fx-background-color: " + UIUtils.COLOR_CARD + "; " +
                      "-fx-background-radius: 16; " +
                      "-fx-border-color: #E2E8F0; " +
                      "-fx-border-width: 1.5; " +
                      "-fx-border-radius: 16; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 25, 0, 0, 12);");

        Label iconLabel = new Label("📅");
        iconLabel.setFont(Font.font("System", 32));
        iconLabel.setStyle("-fx-background-color: rgba(37, 99, 235, 0.12); -fx-padding: 10; -fx-background-radius: 12;");

        Label titleLabel = new Label("LATIHAN BERIKUTNYA");
        titleLabel.setTextFill(Color.web(UIUtils.COLOR_TEXT_SECONDARY));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 11));

        if (training != null) {
            Label dateLabel = new Label(training.getDate());
            dateLabel.setTextFill(Color.web(UIUtils.COLOR_ACCENT));
            dateLabel.setFont(Font.font("System", FontWeight.BLACK, 24));

            Label subInfo = new Label(training.getTime() + " | " + training.getDay());
            subInfo.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));
            subInfo.setFont(Font.font("System", FontWeight.BOLD, 14));

            card.getChildren().addAll(iconLabel, titleLabel, dateLabel, subInfo);
        } else {
            Label noTraining = new Label("-");
            noTraining.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));
            noTraining.setFont(Font.font("System", FontWeight.BLACK, 28));
            card.getChildren().addAll(iconLabel, titleLabel, noTraining);
        }
        
        return card;
    }
}
