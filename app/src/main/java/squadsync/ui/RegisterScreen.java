package squadsync.ui;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import squadsync.database.DatabaseManager;

public class RegisterScreen {
    private Stage stage;

    public RegisterScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox root = new VBox(40);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + ";");

        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        
        Label logo = new Label("⚽ SQUADSYNC");
        logo.setFont(Font.font("System", FontWeight.BLACK, 48));
        logo.setTextFill(Color.web(UIUtils.COLOR_ACCENT));
        logo.setStyle("-fx-letter-spacing: 3px;");

        Label subtitle = new Label("Mulai Perjalanan Tim Anda Hari Ini.");
        subtitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        subtitle.setTextFill(Color.web(UIUtils.COLOR_TEXT_SECONDARY));
        
        header.getChildren().addAll(logo, subtitle);

        VBox form = new VBox(15);
        form.setMaxWidth(380);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: " + UIUtils.COLOR_SIDEBAR + "; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 30, 0, 0, 10); -fx-border-color: rgba(0,0,0,0.05); -fx-border-radius: 20;");

        TextField userField = createStyledTextField("Nama Pengguna");
        PasswordField passField = createStyledPasswordField("Kata Sandi");

        Button registerBtn = UIUtils.createPrimaryButton("DAFTAR SEKARANG");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(50);

        Button backBtn = new Button("Sudah punya akun? Masuk di sini");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + UIUtils.COLOR_ACCENT + "; -fx-font-weight: 800; -fx-cursor: hand;");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> new LoginScreen(stage).show());
        
        Label statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        registerBtn.setOnAction(e -> handleRegister(userField.getText(), passField.getText(), statusLabel));

        form.getChildren().addAll(
            createFieldLabel("NAMA PENGGUNA"), userField, 
            createFieldLabel("KATA SANDI"), passField, 
            new Region() {{ setMinHeight(10); }},
            registerBtn, backBtn, statusLabel
        );
        
        root.getChildren().addAll(header, form);

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.show();
    }

    private void handleRegister(String user, String pass, Label status) {
        String u = user.trim();
        String p = pass.trim();
        if (u.isEmpty() || p.isEmpty()) {
            status.setText("⚠️ Data tidak boleh kosong!");
            status.setTextFill(Color.web(UIUtils.COLOR_DANGER));
        } else if (p.length() < 6) {
            status.setText("⚠️ Kata sandi minimal 6 karakter!");
            status.setTextFill(Color.web(UIUtils.COLOR_DANGER));
        } else {
            boolean success = DatabaseManager.getInstance().registerUser(u, p, "MANAGER");
            if (!success) {
                status.setText("❌ Nama pengguna sudah terdaftar!");
                status.setTextFill(Color.web(UIUtils.COLOR_DANGER));
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Pendaftaran berhasil! Silakan masuk menggunakan akun baru Anda.").showAndWait();
                new LoginScreen(stage).show();
            }
        }
    }

    private Label createFieldLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web(UIUtils.COLOR_TEXT_SECONDARY));
        l.setFont(Font.font("System", FontWeight.BOLD, 11));
        return l;
    }

    private TextField createStyledTextField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-border-color: rgba(0,0,0,0.1); -fx-background-radius: 10; -fx-border-radius: 10; -fx-prompt-text-fill: #94A3B8;");
        f.setPrefHeight(45);
        return f;
    }

    private PasswordField createStyledPasswordField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color: " + UIUtils.COLOR_BG + "; -fx-text-fill: " + UIUtils.COLOR_TEXT_PRIMARY + "; -fx-border-color: rgba(0,0,0,0.1); -fx-background-radius: 10; -fx-border-radius: 10; -fx-prompt-text-fill: #94A3B8;");
        f.setPrefHeight(45);
        return f;
    }
}
