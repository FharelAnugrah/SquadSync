package squadsync.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import squadsync.database.DatabaseManager;
import squadsync.model.Player;

import java.util.Comparator;
import java.util.List;

public class RankingView {

    private TableView<RankRow> table = new TableView<>();

    public VBox getView() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");

        Label title = new Label("Papan Peringkat Pemain 🏆");
        title.setFont(Font.font("System", FontWeight.BLACK, 32));
        title.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));

        setupTable();
        loadData();

        root.getChildren().addAll(title, table);
        return root;
    }

    private void setupTable() {
        table.setStyle(UIUtils.getTableStyle());

        TableColumn<RankRow, Integer> rankCol = new TableColumn<>("PERINGKAT");
        rankCol.setCellValueFactory(d -> d.getValue().rank.asObject());
        rankCol.setPrefWidth(120);
        rankCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        TableColumn<RankRow, String> nameCol = new TableColumn<>("NAMA PEMAIN");
        nameCol.setCellValueFactory(d -> d.getValue().name);
        nameCol.setPrefWidth(300);

        TableColumn<RankRow, String> posCol = new TableColumn<>("POSISI");
        posCol.setCellValueFactory(d -> d.getValue().position);
        posCol.setPrefWidth(200);

        TableColumn<RankRow, Integer> scoreCol = new TableColumn<>("SKOR");
        scoreCol.setCellValueFactory(d -> d.getValue().score.asObject());
        scoreCol.setPrefWidth(150);
        scoreCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: " + UIUtils.COLOR_ACCENT + ";");

        table.getColumns().clear();
        table.getColumns().addAll(rankCol, nameCol, posCol, scoreCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setRowFactory(tv -> {
            TableRow<RankRow> row = new TableRow<RankRow>() {
                @Override
                protected void updateItem(RankRow item, boolean empty) {
                    super.updateItem(item, empty);
                    updateRowStyle(this, item, empty);
                }
            };

            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                updateRowStyle(row, row.getItem(), row.isEmpty());
            });

            return row;
        });
    }

    private void updateRowStyle(TableRow<RankRow> row, RankRow item, boolean empty) {
        if (item == null || empty) {
            row.setStyle("");
        } else if (row.isSelected()) {
            row.setStyle("-fx-background-color: " + UIUtils.COLOR_ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            int rank = item.rank.get();
            if (rank == 1) {
                row.setStyle("-fx-background-color: rgba(251, 192, 45, 0.15); -fx-border-color: #FBC02D; -fx-border-width: 0 0 1 0;");
            } else if (rank == 2) {
                row.setStyle("-fx-background-color: rgba(158, 158, 158, 0.15); -fx-border-color: #9E9E9E; -fx-border-width: 0 0 1 0;");
            } else if (rank == 3) {
                row.setStyle("-fx-background-color: rgba(141, 110, 99, 0.15); -fx-border-color: #8D6E63; -fx-border-width: 0 0 1 0;");
            } else {
                row.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(0,0,0,0.05); -fx-border-width: 0 0 1 0;");
            }
        }
    }

    private void loadData() {
        List<Player> players = DatabaseManager.getInstance().getAllPlayers();
        List<squadsync.model.Statistic> statsList = DatabaseManager.getInstance().getAllStatistics();
        
        // Map stats by player_id for quick lookup
        java.util.Map<Integer, squadsync.model.Statistic> statsMap = new java.util.HashMap<>();
        for (squadsync.model.Statistic s : statsList) {
            statsMap.put(s.getPlayerId(), s);
        }

        // Custom sorting logic with tie-breakers: Score > Stamina > Speed > Shooting > Passing
        players.sort((p1, p2) -> {
            // 1. Primary: Overall Score (Average)
            if (p2.getScore() != p1.getScore()) {
                return Integer.compare(p2.getScore(), p1.getScore());
            }

            // Tie-breakers: fetch from map
            squadsync.model.Statistic s1 = statsMap.get(p1.getId());
            squadsync.model.Statistic s2 = statsMap.get(p2.getId());

            // Handle missing stats
            if (s1 == null && s2 == null) return 0;
            if (s1 == null) return 1;
            if (s2 == null) return -1;

            // 2. Factor: Stamina
            if (s2.getStamina() != s1.getStamina()) {
                return Integer.compare(s2.getStamina(), s1.getStamina());
            }
            // 3. Factor: Kecepatan (Speed)
            if (s2.getSpeed() != s1.getSpeed()) {
                return Integer.compare(s2.getSpeed(), s1.getSpeed());
            }
            // 4. Factor: Shooting
            if (s2.getShooting() != s1.getShooting()) {
                return Integer.compare(s2.getShooting(), s1.getShooting());
            }
            // 5. Factor: Passing
            if (s2.getPassing() != s1.getPassing()) {
                return Integer.compare(s2.getPassing(), s1.getPassing());
            }

            // 6. Final Tie-breaker: Alphabetical Name
            return p1.getName().compareToIgnoreCase(p2.getName());
        });
        
        ObservableList<RankRow> rows = FXCollections.observableArrayList();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            rows.add(new RankRow(i + 1, p.getName(), p.getPosition(), p.getScore()));
        }
        table.setItems(rows);
    }

    public static class RankRow {
        SimpleIntegerProperty rank, score;
        SimpleStringProperty name, position;
        public RankRow(int r, String n, String p, int s) {
            this.rank = new SimpleIntegerProperty(r);
            this.name = new SimpleStringProperty(n);
            this.position = new SimpleStringProperty(p);
            this.score = new SimpleIntegerProperty(s);
        }
    }
}
