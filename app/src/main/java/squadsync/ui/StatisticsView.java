package squadsync.ui;


import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import squadsync.database.DatabaseManager;
import squadsync.model.Player;
import squadsync.model.Statistic;
import squadsync.model.UserRole;

public class StatisticsView {

    private ComboBox<Player> playerCombo = new ComboBox<>();
    private VBox statsContent = new VBox(25);
    private UserRole role;
    private String username;

    public StatisticsView(UserRole role, String username) {
        this.role = role;
        this.username = username;
    }

    public VBox getView() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");

        Label title = new Label("Analisis Performa");
        title.setFont(Font.font("System", FontWeight.BLACK, 32));
        title.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        playerCombo.setItems(FXCollections.observableArrayList(DatabaseManager.getInstance().getAllPlayers()));
        playerCombo.setPromptText("-- Pilih Pemain --");
        playerCombo.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-border-color: rgba(0,0,0,0.1); -fx-background-radius: 8; -fx-border-radius: 8;");
        playerCombo.setPrefWidth(300);
        playerCombo.setPrefHeight(45);
        playerCombo.setOnAction(e -> loadPlayerStats());

        Button btnEdit = UIUtils.createPrimaryButton("✎ EDIT STATISTIK");
        btnEdit.setOnAction(e -> showEditDialog());

        topBar.getChildren().addAll(playerCombo, btnEdit);

        statsContent.setAlignment(Pos.TOP_LEFT);
        statsContent.setPadding(new Insets(20, 0, 0, 0));

        root.getChildren().addAll(title, topBar, statsContent);
        return root;
    }

    private void loadPlayerStats() {
        Player p = playerCombo.getValue();
        if (p == null) return;
        Statistic s = DatabaseManager.getInstance().getStatisticByPlayer(p.getId());
        if (s == null) s = new Statistic(0, p.getId(), 0, 0, 0, 0);

        statsContent.getChildren().clear();

        // Dynamically get labels based on position
        String[] labels = getStatLabels(p.getPosition());

        // Add Overall Performance Progression Bar
        double average = (s.getStamina() + s.getPassing() + s.getSpeed() + s.getShooting()) / 4.0;
        VBox overallCard = createOverallProgressCard(average);
        
        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(20);
        grid.add(createStatCard(labels[0], s.getStamina(), "⚡"), 0, 0);
        grid.add(createStatCard(labels[1], s.getSpeed(), getIconForStat(labels[1])), 1, 0);
        grid.add(createStatCard(labels[2], s.getShooting(), getIconForStat(labels[2])), 0, 1);
        grid.add(createStatCard(labels[3], s.getPassing(), getIconForStat(labels[3])), 1, 1);

        statsContent.getChildren().addAll(overallCard, new Separator(), grid);
    }

    private String[] getStatLabels(String pos) {
        if (pos.startsWith("GK")) {
            return new String[]{"Stamina", "Refleks", "Diving", "Posisi"};
        } else if (pos.startsWith("CB") || pos.startsWith("LB") || pos.startsWith("RB") || pos.startsWith("DM")) {
            return new String[]{"Stamina", "Kecepatan", "Tackling", "Intersepsi"};
        } else {
            return new String[]{"Stamina", "Kecepatan", "Shooting", "Passing"};
        }
    }

    private String getIconForStat(String label) {
        switch (label) {
            case "Refleks": return "🧤";
            case "Diving": return "🛡️";
            case "Posisi": return "📍";
            case "Kecepatan": return "🏃";
            case "Tackling": return "🛑";
            case "Intersepsi": return "⚡";
            case "Shooting": return "⚽";
            case "Passing": return "🎯";
            default: return "📊";
        }
    }

    private VBox createOverallProgressCard(double average) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: linear-gradient(to right, #2563EB, #3B82F6); -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(37,99,235,0.3), 20, 0, 0, 10);");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox textLabel = new VBox(5);
        Label title = new Label("OVERALL RATING");
        title.setTextFill(Color.web("#BFDBFE"));
        title.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Label value = new Label(String.format("%.1f%%", average));
        value.setTextFill(Color.WHITE);
        value.setFont(Font.font("System", FontWeight.BLACK, 36));
        textLabel.getChildren().addAll(title, value);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label icon = new Label("🏆");
        icon.setFont(Font.font(48));
        
        header.getChildren().addAll(textLabel, spacer, icon);
        
        ProgressBar pb = new ProgressBar(average / 100.0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(15);
        pb.setStyle("-fx-accent: #FFFFFF; -fx-control-inner-background: rgba(255,255,255,0.2); -fx-background-radius: 10;");
        
        card.getChildren().addAll(header, pb);
        return card;
    }

    private VBox createStatCard(String label, int value, String icon) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: " + UIUtils.COLOR_CARD + "; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 15, 0, 0, 5); -fx-border-color: rgba(0,0,0,0.05); -fx-border-radius: 15;");
        
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font(24));
        iconLbl.setStyle("-fx-background-color: rgba(37, 99, 235, 0.08); -fx-padding: 8; -fx-background-radius: 10;");
        
        Label titleLbl = new Label(label);
        titleLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLbl.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label valLbl = new Label(value + "/100");
        valLbl.setFont(Font.font("System", FontWeight.BLACK, 16));
        valLbl.setTextFill(Color.web(UIUtils.COLOR_ACCENT));
        
        header.getChildren().addAll(iconLbl, titleLbl, spacer, valLbl);

        ProgressBar pb = new ProgressBar(value / 100.0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(12);
        
        pb.setStyle(UIUtils.getProgressBarStyle());

        card.getChildren().addAll(header, pb);
        return card;
    }

    private void showEditDialog() {
        Player p = playerCombo.getValue();
        if (p == null) {
            new Alert(Alert.AlertType.WARNING, "Silakan pilih pemain terlebih dahulu!").show();
            return;
        }

        Statistic s = DatabaseManager.getInstance().getStatisticByPlayer(p.getId());
        if (s == null) s = new Statistic(0, p.getId(), 50, 50, 50, 50);

        String[] labels = getStatLabels(p.getPosition());

        Dialog<Statistic> dialog = new Dialog<>();
        dialog.setTitle("Edit Statistik: " + p.getName());
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));

        Label header = new Label("Perbarui Performa (" + p.getPosition() + ")");
        header.setFont(Font.font("System", FontWeight.BOLD, 18));
        header.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(20);

        Spinner<Integer> stamSpin = createNumericSpinner(s.getStamina());
        Spinner<Integer> speedSpin = createNumericSpinner(s.getSpeed());
        Spinner<Integer> shootSpin = createNumericSpinner(s.getShooting());
        Spinner<Integer> passSpin = createNumericSpinner(s.getPassing());

        addSpinnerField(grid, labels[0] + ":", stamSpin, 0);
        addSpinnerField(grid, labels[1] + ":", speedSpin, 1);
        addSpinnerField(grid, labels[2] + ":", shootSpin, 2);
        addSpinnerField(grid, labels[3] + ":", passSpin, 3);

        layout.getChildren().addAll(header, grid);
        dialogPane.setContent(layout);

        Button okBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        okBtn.setStyle("-fx-background-color: " + UIUtils.COLOR_ACCENT + "; -fx-text-fill: white;");

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new Statistic(0, p.getId(), 
                    stamSpin.getValue(), 
                    passSpin.getValue(), 
                    speedSpin.getValue(), 
                    shootSpin.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newStats -> {
            DatabaseManager.getInstance().saveStatistic(newStats);
            loadPlayerStats();
        });
    }

    private void addSpinnerField(GridPane grid, String label, Spinner<Integer> spinner, int row) {
        Label l = new Label(label);
        l.setTextFill(Color.web(UIUtils.COLOR_TEXT_SECONDARY));
        l.setFont(Font.font("System", FontWeight.BOLD, 13));
        grid.add(l, 0, row);
        grid.add(spinner, 1, row);
    }

    private Spinner<Integer> createNumericSpinner(int initialValue) {
        Spinner<Integer> spinner = new Spinner<>(0, 100, initialValue);
        spinner.setEditable(true);
        spinner.setPrefWidth(120);
        spinner.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-border-color: rgba(0,0,0,0.1);");
        spinner.getEditor().setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + ";");
        
        spinner.getEditor().setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                String newText = change.getControlNewText();
                if (newText.isEmpty()) return change;
                int val = Integer.parseInt(newText);
                if (val <= 100) return change;
            }
            return null;
        }));
        
        return spinner;
    }
}
