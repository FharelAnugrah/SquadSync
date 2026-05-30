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
import squadsync.model.Attendance;
import squadsync.model.Player;
import squadsync.model.Training;

import java.time.LocalDate;
import java.util.List;

public class AttendanceView {

    private ComboBox<Training> trainingCombo = new ComboBox<>();
    private TableView<AttendanceRow> table = new TableView<>();
    private ObservableList<AttendanceRow> rowList = FXCollections.observableArrayList();
    private Label summaryLabel = new Label("Ringkasan: 0 Hadir, 0 Alpa, 0 Izin, 0 Sakit");
    
    private BorderPane mainContainer = new BorderPane();
    
    // Style constants for toggle buttons - Unified with Light Theme
    private final String ACTIVE_STYLE = "-fx-background-color: " + UIUtils.COLOR_ACCENT + "; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 25; -fx-effect: dropshadow(three-pass-box, rgba(37,99,235,0.2), 10, 0, 0, 0);";
    private final String INACTIVE_STYLE = "-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_SECONDARY + "; -fx-font-weight: 700; -fx-background-radius: 10; -fx-padding: 10 25; -fx-border-color: rgba(0,0,0,0.05); -fx-border-radius: 10;";

    public VBox getView() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");

        Label title = new Label("Manajemen Absensi");
        title.setFont(Font.font("System", FontWeight.BLACK, 32));
        title.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));

        HBox menuBar = new HBox(15);
        Button btnInput = new Button("📥 INPUT ABSENSI");
        Button btnRecap = new Button("📊 REKAP BULANAN");
        
        btnInput.setStyle(ACTIVE_STYLE);
        btnRecap.setStyle(INACTIVE_STYLE);
        
        menuBar.getChildren().addAll(btnInput, btnRecap);

        btnInput.setOnAction(e -> {
            btnInput.setStyle(ACTIVE_STYLE);
            btnRecap.setStyle(INACTIVE_STYLE);
            mainContainer.setCenter(createInputView());
        });
        
        btnRecap.setOnAction(e -> {
            btnRecap.setStyle(ACTIVE_STYLE);
            btnInput.setStyle(INACTIVE_STYLE);
            mainContainer.setCenter(createRecapView());
        });

        mainContainer.setCenter(createInputView());

        root.getChildren().addAll(title, menuBar, mainContainer);
        return root;
    }

    private VBox createInputView() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(20, 0, 0, 0));
        content.setStyle("-fx-background-color: transparent;");

        HBox topBar = new HBox(15);
        topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label comboLabel = new Label("Pilih Jadwal:");
        comboLabel.setTextFill(Color.web(UIUtils.COLOR_TEXT_SECONDARY));
        comboLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        List<Training> trainings = DatabaseManager.getInstance().getAllTrainings();
        trainingCombo.setItems(FXCollections.observableArrayList(trainings));
        trainingCombo.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-border-color: rgba(0,0,0,0.1); -fx-background-radius: 8; -fx-border-radius: 8;");
        trainingCombo.setPromptText("-- Pilih Latihan --");
        trainingCombo.setPrefHeight(45);
        trainingCombo.setPrefWidth(300);
        trainingCombo.setOnAction(e -> loadData());

        Button btnSave = UIUtils.createPrimaryButton("💾 SIMPAN ABSENSI");
        btnSave.setOnAction(e -> saveAttendanceData());

        topBar.getChildren().addAll(comboLabel, trainingCombo, btnSave);

        HBox utilBar = new HBox(15);
        utilBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Cari pemain...");
        searchField.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(0,0,0,0.1);");
        searchField.setPrefWidth(250);
        searchField.setPrefHeight(40);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterPlayers(newVal));

        Button btnAllPresent = new Button("✅ HADIR SEMUA");
        btnAllPresent.setStyle("-fx-background-color: rgba(22, 163, 74, 0.08); -fx-text-fill: " + UIUtils.COLOR_SUCCESS + "; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 15;");
        btnAllPresent.setOnAction(e -> setAllStatus("Hadir"));

        Button btnAllAbsent = new Button("❌ ALPA SEMUA");
        btnAllAbsent.setStyle("-fx-background-color: rgba(220, 38, 38, 0.08); -fx-text-fill: " + UIUtils.COLOR_DANGER + "; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 15;");
        btnAllAbsent.setOnAction(e -> setAllStatus("Alpa"));

        summaryLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        summaryLabel.setTextFill(Color.web(UIUtils.COLOR_ACCENT));
        
        utilBar.getChildren().addAll(searchField, btnAllPresent, btnAllAbsent, new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, summaryLabel);

        table.setStyle(UIUtils.getTableStyle());
        
        TableColumn<AttendanceRow, String> nameCol = new TableColumn<>("NAMA PEMAIN");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setPrefWidth(300);

        TableColumn<AttendanceRow, String> statusCol = new TableColumn<>("STATUS KEHADIRAN");
        statusCol.setPrefWidth(250);
        statusCol.setCellFactory(col -> new TableCell<AttendanceRow, String>() {
            private final ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("Hadir", "Izin", "Sakit", "Alpa"));
            {
                statusCombo.setStyle("-fx-background-color: transparent; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-border-color: rgba(0,0,0,0.05); -fx-background-radius: 8; -fx-border-radius: 8;");
                statusCombo.setMaxWidth(Double.MAX_VALUE);
                statusCombo.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        AttendanceRow row = getTableRow().getItem();
                        row.setStatus(statusCombo.getValue());
                        updateSummary();
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    statusCombo.setValue(getTableRow().getItem().getStatus());
                    setGraphic(statusCombo);
                }
            }
        });

        table.getColumns().clear();
        table.getColumns().addAll(nameCol, statusCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(topBar, utilBar, table);
        return content;
    }

    private void loadData() {
        Training t = trainingCombo.getValue();
        if (t == null) return;
        List<Player> players = DatabaseManager.getInstance().getAllPlayers();
        List<Attendance> saved = DatabaseManager.getInstance().getAttendanceByTraining(t.getId());
        rowList.clear();
        for (Player p : players) {
            String status = saved.stream().filter(a -> a.getPlayerId() == p.getId()).findFirst().map(Attendance::getStatus).orElse("Alpa");
            rowList.add(new AttendanceRow(p.getId(), p.getName(), status));
        }
        table.setItems(rowList);
        updateSummary();
    }

    private void filterPlayers(String query) {
        if (query == null || query.isEmpty()) {
            table.setItems(rowList);
        } else {
            table.setItems(FXCollections.observableArrayList(rowList.stream()
                .filter(row -> row.nameProperty().get().toLowerCase().contains(query.toLowerCase()))
                .toList()));
        }
    }

    private void setAllStatus(String status) {
        rowList.forEach(row -> row.setStatus(status));
        table.refresh();
        updateSummary();
    }

    private void updateSummary() {
        int hadir = 0, alpa = 0, izin = 0, sakit = 0;
        for (AttendanceRow row : rowList) {
            switch (row.getStatus()) {
                case "Hadir" -> hadir++;
                case "Alpa" -> alpa++;
                case "Izin" -> izin++;
                case "Sakit" -> sakit++;
            }
        }
        summaryLabel.setText(String.format("Ringkasan: %d Hadir, %d Alpa, %d Izin, %d Sakit", hadir, alpa, izin, sakit));
    }

    private void saveAttendanceData() {
        Training t = trainingCombo.getValue();
        if (t == null) return;
        DatabaseManager.getInstance().clearAttendanceForTraining(t.getId());
        for (AttendanceRow row : rowList) {
            DatabaseManager.getInstance().saveAttendance(new Attendance(0, row.id, t.getId(), row.getStatus()));
        }
        updateSummary();
        new Alert(Alert.AlertType.INFORMATION, "Absensi berhasil disimpan!").show();
    }

    private VBox createRecapView() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(20, 0, 0, 0));
        content.setStyle("-fx-background-color: transparent;");

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> monthCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni", 
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        ));
        monthCombo.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-background-radius: 8; -fx-border-color: rgba(0,0,0,0.1);");
        monthCombo.setPrefHeight(40);
        
        LocalDate now = LocalDate.now();
        monthCombo.getSelectionModel().select(now.getMonthValue() - 1);

        ComboBox<Integer> yearCombo = new ComboBox<>();
        for (int y = 2020; y <= 2030; y++) yearCombo.getItems().add(y);
        yearCombo.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-background-radius: 8; -fx-border-color: rgba(0,0,0,0.1);");
        yearCombo.setPrefHeight(40);
        yearCombo.setValue(now.getYear());

        Button btnLoad = UIUtils.createPrimaryButton("🔍 MUAT REKAP");
        
        Label l1 = new Label("Pilih Bulan:"); l1.setTextFill(Color.web(UIUtils.COLOR_TEXT_SECONDARY));
        Label l2 = new Label("Tahun:"); l2.setTextFill(Color.web(UIUtils.COLOR_TEXT_SECONDARY));
        
        topBar.getChildren().addAll(l1, monthCombo, l2, yearCombo, btnLoad);

        TableView<RecapRow> recapTable = new TableView<>();
        recapTable.setStyle(UIUtils.getTableStyle());
        setupRecapTable(recapTable);

        btnLoad.setOnAction(e -> {
            int monthVal = monthCombo.getSelectionModel().getSelectedIndex() + 1;
            loadMonthlyRecap(recapTable, String.format("%02d", monthVal), String.valueOf(yearCombo.getValue()));
        });
        
        VBox.setVgrow(recapTable, Priority.ALWAYS);
        content.getChildren().addAll(topBar, recapTable);
        
        loadMonthlyRecap(recapTable, String.format("%02d", now.getMonthValue()), String.valueOf(now.getYear()));

        return content;
    }

    private void setupRecapTable(TableView<RecapRow> table) {
        TableColumn<RecapRow, String> nameCol = new TableColumn<>("NAMA PEMAIN");
        nameCol.setCellValueFactory(d -> d.getValue().name);
        nameCol.setPrefWidth(250);

        TableColumn<RecapRow, Integer> hadirCol = new TableColumn<>("HADIR");
        hadirCol.setCellValueFactory(d -> d.getValue().hadir.asObject());

        TableColumn<RecapRow, Integer> izinCol = new TableColumn<>("IZIN");
        izinCol.setCellValueFactory(d -> d.getValue().izin.asObject());

        TableColumn<RecapRow, Integer> sakitCol = new TableColumn<>("SAKIT");
        sakitCol.setCellValueFactory(d -> d.getValue().sakit.asObject());

        TableColumn<RecapRow, Integer> alpaCol = new TableColumn<>("ALPA");
        alpaCol.setCellValueFactory(d -> d.getValue().alpa.asObject());

        table.getColumns().addAll(nameCol, hadirCol, izinCol, sakitCol, alpaCol);
    }

    private void loadMonthlyRecap(TableView<RecapRow> table, String month, String year) {
        String filter = year + "-" + month;
        List<Training> monthlyTrainings = DatabaseManager.getInstance().getAllTrainings().stream()
                .filter(t -> t.getDate().startsWith(filter))
                .toList();

        List<Player> players = DatabaseManager.getInstance().getAllPlayers();
        ObservableList<RecapRow> rows = FXCollections.observableArrayList();

        for (Player p : players) {
            int hadir = 0, izin = 0, sakit = 0, alpa = 0;
            for (Training t : monthlyTrainings) {
                List<Attendance> attendances = DatabaseManager.getInstance().getAttendanceByTraining(t.getId());
                String status = attendances.stream()
                        .filter(a -> a.getPlayerId() == p.getId())
                        .findFirst()
                        .map(Attendance::getStatus)
                        .orElse("Alpa");
                switch (status) {
                    case "Hadir" -> hadir++;
                    case "Izin" -> izin++;
                    case "Sakit" -> sakit++;
                    default -> alpa++;
                }
            }
            rows.add(new RecapRow(p.getName(), hadir, izin, sakit, alpa));
        }
        table.setItems(rows);
    }

    public static class AttendanceRow {
        private final int id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty status;
        public AttendanceRow(int id, String name, String status) {
            this.id = id;
            this.name = new SimpleStringProperty(name);
            this.status = new SimpleStringProperty(status);
        }
        public SimpleStringProperty nameProperty() { return name; }
        public String getStatus() { return status.get(); }
        public void setStatus(String status) { this.status.set(status); }
    }

    public static class RecapRow {
        SimpleStringProperty name;
        SimpleIntegerProperty hadir, izin, sakit, alpa;
        public RecapRow(String n, int h, int i, int s, int a) {
            this.name = new SimpleStringProperty(n);
            this.hadir = new SimpleIntegerProperty(h);
            this.izin = new SimpleIntegerProperty(i);
            this.sakit = new SimpleIntegerProperty(s);
            this.alpa = new SimpleIntegerProperty(a);
        }
    }
}
