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
import squadsync.model.Training;
import squadsync.model.UserRole;

import java.time.LocalDate;

public class TrainingView {

    private TableView<Training> table = new TableView<>();
    private ObservableList<Training> trainingList = FXCollections.observableArrayList();
    private UserRole role;

    public TrainingView(UserRole role) {
        this.role = role;
    }

    public VBox getView() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(50));
        content.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");

        Label title = new Label("Jadwal Latihan");
        title.setFont(Font.font("System", FontWeight.BLACK, 32));
        title.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));

        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button btnAdd = UIUtils.createPrimaryButton("+ TAMBAH JADWAL");
        btnAdd.setOnAction(e -> showTrainingDialog(null));

        Button btnEdit = new Button("✎ EDIT");
        btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: " + UIUtils.COLOR_ACCENT + "; -fx-font-weight: 800; -fx-padding: 12 20; -fx-background-radius: 10; -fx-border-color: " + UIUtils.COLOR_ACCENT + "; -fx-border-radius: 10;");
        btnEdit.setOnAction(e -> {
            Training selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showTrainingDialog(selected);
        });

        Button btnDelete = new Button("🗑 HAPUS");
        btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: " + UIUtils.COLOR_DANGER + "; -fx-font-weight: 800; -fx-padding: 12 20; -fx-background-radius: 10; -fx-border-color: " + UIUtils.COLOR_DANGER + "; -fx-border-radius: 10;");
        btnDelete.setOnAction(e -> {
            Training selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                DatabaseManager.getInstance().deleteTraining(selected.getId());
                loadData();
            }
        });

        topBar.getChildren().addAll(btnAdd, btnEdit, btnDelete);

        setupTable();
        loadData();

        content.getChildren().addAll(title, topBar, table);
        return content;
    }

    private void setupTable() {
        table.setStyle(UIUtils.getTableStyle());

        TableColumn<Training, String> dayCol = new TableColumn<>("HARI");
        dayCol.setCellValueFactory(new PropertyValueFactory<>("day"));
        dayCol.setPrefWidth(120);

        TableColumn<Training, String> dateCol = new TableColumn<>("TANGGAL");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(120);

        TableColumn<Training, String> timeCol = new TableColumn<>("WAKTU");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));

        TableColumn<Training, String> activityCol = new TableColumn<>("KEGIATAN");
        activityCol.setCellValueFactory(new PropertyValueFactory<>("activity"));
        activityCol.setPrefWidth(250);

        TableColumn<Training, String> locCol = new TableColumn<>("LOKASI");
        locCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locCol.setPrefWidth(200);

        table.getColumns().setAll(dayCol, dateCol, timeCol, activityCol, locCol);
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private void loadData() {
        trainingList.setAll(DatabaseManager.getInstance().getAllTrainings());
        table.setItems(trainingList);
    }

    private void showTrainingDialog(Training training) {
        Dialog<Training> dialog = new Dialog<>();
        dialog.setTitle(training == null ? "Tambah Jadwal" : "Edit Jadwal");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");

        ButtonType saveBtnType = new ButtonType("SIMPAN", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setPrefWidth(450);

        Label header = new Label(training == null ? "Entry Jadwal Baru" : "Update Jadwal Latihan");
        header.setFont(Font.font("System", FontWeight.BOLD, 22));
        header.setTextFill(Color.web(UIUtils.COLOR_TEXT_PRIMARY));

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(20);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-control-inner-background: " + UIUtils.COLOR_SIDEBAR + ";");
        datePicker.setMaxWidth(Double.MAX_VALUE);
        
        ComboBox<String> timeCombo = new ComboBox<>();
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m += 30) {
                timeCombo.getItems().add(String.format("%02d:%02d", h, m));
            }
        }
        timeCombo.setValue("16:00");
        timeCombo.setMaxWidth(Double.MAX_VALUE);
        styleComboBox(timeCombo);
        
        ComboBox<String> activityCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Latihan Fisik dan Stamina", "Strategi Taktis", "Latihan Teknik", "Uji Coba (Friendly)", "Sesi Pemulihan", "Latihan Gym"
        ));
        activityCombo.setMaxWidth(Double.MAX_VALUE);
        activityCombo.setPromptText("-- Pilih Kegiatan --");
        styleComboBox(activityCombo);
        
        TextField locField = new TextField();
        locField.setPromptText("Nama Lapangan / Lokasi");
        locField.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-border-color: rgba(0,0,0,0.1); -fx-background-radius: 8; -fx-border-radius: 8;");
        locField.setPrefHeight(40);

        if (training != null) {
            datePicker.setValue(LocalDate.parse(training.getDate()));
            String time = training.getTime();
            if (time.length() == 4 && !time.contains(":")) {
                time = time.substring(0, 2) + ":" + time.substring(2);
            }
            timeCombo.setValue(time);
            activityCombo.setValue(training.getActivity());
            locField.setText(training.getLocation());
        } else {
            activityCombo.setValue("Latihan Fisik dan Stamina");
        }

        addFormField(grid, "Tanggal:", datePicker, 0);
        addFormField(grid, "Waktu:", timeCombo, 1);
        addFormField(grid, "Kegiatan:", activityCombo, 2);
        addFormField(grid, "Lokasi:", locField, 3);

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.web(UIUtils.COLOR_DANGER));
        errorLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        layout.getChildren().addAll(header, grid, errorLabel);
        dialogPane.setContent(layout);

        Button saveBtn = (Button) dialogPane.lookupButton(saveBtnType);
        saveBtn.setStyle("-fx-background-color: " + UIUtils.COLOR_ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold;");

        saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            errorLabel.setText("");
            if (activityCombo.getValue() == null) {
                errorLabel.setText("⚠️ Silakan pilih jenis kegiatan!");
                e.consume();
            } else if (locField.getText().trim().isEmpty()) {
                errorLabel.setText("⚠️ Lokasi latihan tidak boleh kosong!");
                e.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                LocalDate date = datePicker.getValue();
                String[] hariIndo = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"};
                String day = hariIndo[date.getDayOfWeek().getValue() - 1];
                String timeStr = timeCombo.getValue();
                
                if (training == null) return new Training(0, date.toString(), day, timeStr, activityCombo.getValue(), locField.getText());
                training.setDay(day); training.setDate(date.toString()); training.setTime(timeStr); 
                training.setActivity(activityCombo.getValue()); training.setLocation(locField.getText());
                return training;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(res -> {
            if (training == null) DatabaseManager.getInstance().addTraining(res);
            else DatabaseManager.getInstance().updateTraining(res);
            loadData();
        });
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
