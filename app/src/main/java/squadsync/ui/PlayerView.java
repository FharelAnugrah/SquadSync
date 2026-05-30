package squadsync.ui;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import squadsync.database.DatabaseManager;
import squadsync.model.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerView {

    private TableView<Player> table = new TableView<>();
    private ObservableList<Player> playerList = FXCollections.observableArrayList();

    public VBox getView() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(50));
        content.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");

        Label title = new Label("Manajemen Pemain");
        title.setFont(Font.font("System", FontWeight.BLACK, 32));
        title.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Cari Nama Pemain...");
        searchField.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-border-color: rgba(0,0,0,0.1); -fx-background-radius: 10; -fx-border-radius: 10;");
        searchField.setPrefWidth(300);
        searchField.setPrefHeight(45);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterData(newValue);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAdd = UIUtils.createPrimaryButton("+ TAMBAH PEMAIN");
        btnAdd.setOnAction(e -> showPlayerDialog(null));
        
        Button btnEdit = new Button("✎ EDIT");
        btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: " + UIUtils.COLOR_ACCENT + "; -fx-font-weight: 800; -fx-padding: 12 20; -fx-background-radius: 10; -fx-border-color: " + UIUtils.COLOR_ACCENT + "; -fx-border-radius: 10;");
        btnEdit.setOnAction(e -> {
            Player selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showPlayerDialog(selected);
        });

        Button btnDelete = new Button("🗑 HAPUS");
        btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: " + UIUtils.COLOR_DANGER + "; -fx-font-weight: 800; -fx-padding: 12 20; -fx-background-radius: 10; -fx-border-color: " + UIUtils.COLOR_DANGER + "; -fx-border-radius: 10;");
        btnDelete.setOnAction(e -> {
            Player selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                DatabaseManager.getInstance().deletePlayer(selected.getId());
                loadData();
            }
        });

        topBar.getChildren().addAll(searchField, spacer, btnAdd, btnEdit, btnDelete);

        // Table setup
        table.setStyle(UIUtils.getTableStyle());
        
        TableColumn<Player, String> nameCol = new TableColumn<>("NAMA");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<Player, Integer> ageCol = new TableColumn<>("USIA");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));

        TableColumn<Player, String> positionCol = new TableColumn<>("POSISI");
        positionCol.setCellValueFactory(new PropertyValueFactory<>("position"));
        positionCol.setPrefWidth(180);

        TableColumn<Player, Integer> numberCol = new TableColumn<>("NO");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));

        TableColumn<Player, Integer> scoreCol = new TableColumn<>("SKOR");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        table.getColumns().clear();
        table.getColumns().addAll(nameCol, ageCol, positionCol, numberCol, scoreCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        loadData();

        content.getChildren().addAll(title, topBar, table);
        return content;
    }

    private void loadData() {
        playerList.setAll(DatabaseManager.getInstance().getAllPlayers());
        table.setItems(playerList);
    }

    private void filterData(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            table.setItems(playerList);
        } else {
            ObservableList<Player> filteredList = FXCollections.observableArrayList(
                playerList.stream()
                          .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                          .collect(Collectors.toList())
            );
            table.setItems(filteredList);
        }
    }

    private void showPlayerDialog(Player player) {
        Dialog<Player> dialog = new Dialog<>();
        dialog.setTitle(player == null ? "Tambah Pemain" : "Edit Pemain");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");

        ButtonType saveButtonType = new ButtonType("SIMPAN", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setPrefWidth(450);

        Label header = new Label(player == null ? "Entry Pemain Baru" : "Update Data Pemain");
        header.setFont(Font.font("System", FontWeight.BOLD, 22));
        header.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        TextField nameField = createStyledTextField("Nama Pemain");
        
        Spinner<Integer> ageSpin = createNumericSpinner(player != null ? player.getAge() : 20, 1, 60);
        
        ComboBox<String> posCombo = new ComboBox<>(FXCollections.observableArrayList(
            "GK (Kiper)", "CB (Bek Tengah)", "LB (Bek Kiri)", "RB (Bek Kanan)",
            "DM (Gelandang Bertahan)", "CM (Gelandang Tengah)", "AM (Gelandang Serang)",
            "LW (Sayap Kiri)", "RW (Sayap Kanan)", "ST (Striker)", "CF (Penyerang Tengah)"
        ));
        posCombo.setValue(player != null ? player.getPosition() : "CM (Gelandang Tengah)");
        posCombo.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(posCombo);

        Spinner<Integer> numSpin = createNumericSpinner(player != null ? player.getNumber() : 10, 1, 99);
        Spinner<Integer> scoreSpin = createNumericSpinner(player != null ? player.getScore() : 1, 1, 100);

        if (player != null) {
            nameField.setText(player.getName());
        }

        addFormField(grid, "Nama:", nameField, 0);
        addFormField(grid, "Usia:", ageSpin, 1);
        addFormField(grid, "Posisi:", posCombo, 2);
        addFormField(grid, "No. Punggung:", numSpin, 3);
        addFormField(grid, "Skor Awal:", scoreSpin, 4);

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.web(UIUtils.COLOR_DANGER));
        errorLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        layout.getChildren().addAll(header, grid, errorLabel);
        dialogPane.setContent(layout);

        final Button saveButton = (Button) dialogPane.lookupButton(saveButtonType);
        saveButton.setStyle("-fx-background-color: " + UIUtils.COLOR_ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold;");
        
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            errorLabel.setText("");
            String nameInput = nameField.getText().trim();
            if (nameInput.isEmpty()) {
                errorLabel.setText("⚠️ Nama tidak boleh kosong!");
                event.consume();
                return;
            }

            // Duplicate Check
            List<Player> allPlayers = DatabaseManager.getInstance().getAllPlayers();
            int num = numSpin.getValue();
            for (Player p : allPlayers) {
                if (player != null && p.getId() == player.getId()) continue;
                if (p.getName().equalsIgnoreCase(nameInput)) {
                    errorLabel.setText("⚠️ Nama pemain sudah ada!");
                    event.consume();
                    return;
                }
                if (p.getNumber() == num) {
                    errorLabel.setText("⚠️ No. Punggung sudah digunakan!");
                    event.consume();
                    return;
                }
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (player == null) {
                    return new Player(0, nameField.getText().trim(), ageSpin.getValue(), posCombo.getValue(), numSpin.getValue(), scoreSpin.getValue());
                } else {
                    player.setName(nameField.getText().trim());
                    player.setAge(ageSpin.getValue());
                    player.setPosition(posCombo.getValue());
                    player.setNumber(numSpin.getValue());
                    player.setScore(scoreSpin.getValue());
                    return player;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (player == null) DatabaseManager.getInstance().addPlayer(result);
            else DatabaseManager.getInstance().updatePlayer(result);
            loadData();
        });
    }

    private Spinner<Integer> createNumericSpinner(int initial, int min, int max) {
        Spinner<Integer> spinner = new Spinner<>(min, max, initial);
        spinner.setEditable(true);
        spinner.setMaxWidth(Double.MAX_VALUE);
        spinner.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-border-color: rgba(0,0,0,0.1); -fx-background-radius: 8; -fx-border-radius: 8;");
        spinner.getEditor().setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + ";");
        
        spinner.getEditor().setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                String newText = change.getControlNewText();
                if (newText.isEmpty()) return change;
                try {
                    int val = Integer.parseInt(newText);
                    if (val >= min && val <= max) return change;
                } catch (NumberFormatException e) {}
            }
            return null;
        }));
        return spinner;
    }

    private TextField createStyledTextField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-border-color: rgba(0,0,0,0.1); -fx-background-radius: 8; -fx-border-radius: 8;");
        f.setPrefHeight(40);
        return f;
    }

    private void styleComboBox(ComboBox<?> combo) {
        combo.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-border-color: rgba(0,0,0,0.1); -fx-background-radius: 8; -fx-border-radius: 8;");
        combo.setPrefHeight(40);
    }

    private void addFormField(GridPane grid, String label, Control field, int row) {
        Label l = new Label(label);
        l.setTextFill(Color.web(UIUtils.COLOR_TEXT_SECONDARY));
        l.setFont(Font.font("System", FontWeight.BOLD, 13));
        grid.add(l, 0, row);
        grid.add(field, 1, row);
    }
}
