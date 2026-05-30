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
import squadsync.model.UserRole;

public class LoginScreen {
    private Stage stage;

    public LoginScreen(Stage stage) {
        this.stage = stage;
    }

    private void handleLogin(String user, String pass, Label error) {
        squadsync.model.UserRole role = squadsync.database.DatabaseManager.getInstance().authenticate(user, pass);
        if (role != null) {
            new DashboardScreen(stage, role, user).show();
        } else {
            error.setText("❌ Nama pengguna atau kata sandi salah!");
        }
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

        Label subtitle = new Label("One Team. One Sync. One Goal.");
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

        Button loginBtn = UIUtils.createPrimaryButton("MASUK KE DASHBOARD");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(50);

        Button registerBtn = new Button("Belum punya akun? Daftar Gratis");
        registerBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + UIUtils.COLOR_ACCENT + "; -fx-font-weight: 800; -fx-cursor: hand;");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> new RegisterScreen(stage).show());
        
        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.web(UIUtils.COLOR_DANGER));
        errorLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        loginBtn.setOnAction(e -> handleLogin(userField.getText(), passField.getText(), errorLabel));

        form.getChildren().addAll(
            createFieldLabel("NAMA PENGGUNA"), userField, 
            createFieldLabel("KATA SANDI"), passField, 
            new Region() {{ setMinHeight(10); }},
            loginBtn, registerBtn, errorLabel
        );
        
        root.getChildren().addAll(header, form);

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.show();
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
