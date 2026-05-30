package squadsync.main;

import javafx.application.Application;
import javafx.stage.Stage;
import squadsync.database.DatabaseManager;
import squadsync.ui.LoginScreen;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize Database
        DatabaseManager.getInstance();

        // Start with Login Screen
        LoginScreen loginScreen = new LoginScreen(primaryStage);
        loginScreen.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
