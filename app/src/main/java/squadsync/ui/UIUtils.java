package squadsync.ui;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class UIUtils {

    // Modern Light Theme Palette - Optimized for Readability
    public static final String COLOR_BG = "#F8FAFC";       // Soft Gray White
    public static final String COLOR_SIDEBAR = "#FFFFFF";  // Pure White
    public static final String COLOR_ACCENT = "#2563EB";   // Primary Blue
    public static final String COLOR_TEXT_PRIMARY = "#1E293B"; // Dark Slate (Readable)
    public static final String COLOR_TEXT_SECONDARY = "#64748B"; // Medium Slate
    public static final String COLOR_CARD = "#FFFFFF";
    public static final String COLOR_SUCCESS = "#16A34A";
    public static final String COLOR_DANGER = "#DC2626";

    public static VBox createCard(String title, String value, String emoji) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.setPrefSize(230, 160);
        card.setStyle("-fx-background-color: " + COLOR_CARD + "; " +
                      "-fx-background-radius: 16; " +
                      "-fx-border-color: #E2E8F0; " +
                      "-fx-border-width: 1.5; " +
                      "-fx-border-radius: 16; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 25, 0, 0, 12);");

        Label iconLabel = new Label(emoji);
        iconLabel.setFont(Font.font("System", 32));
        iconLabel.setStyle("-fx-background-color: rgba(37, 99, 235, 0.12); -fx-padding: 10; -fx-background-radius: 12;");

        Label titleLabel = new Label(title.toUpperCase());
        titleLabel.setTextFill(Color.web(COLOR_TEXT_SECONDARY));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 11));

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(COLOR_ACCENT)); // Use accent color for values to make them pop
        valueLabel.setFont(Font.font("System", FontWeight.BLACK, 28));
        valueLabel.setWrapText(true);

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        return card;
    }

    public static Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(14, 25, 14, 25));
        
        String baseStyle = "-fx-background-color: transparent; -fx-text-fill: " + COLOR_TEXT_SECONDARY + "; " +
                           "-fx-font-size: 15; -fx-font-weight: 600; -fx-background-radius: 12;";
        String hoverStyle = "-fx-background-color: rgba(37, 99, 235, 0.05); -fx-text-fill: " + COLOR_ACCENT + "; " +
                            "-fx-font-size: 15; -fx-font-weight: 600; -fx-background-radius: 12;";
        
        btn.setStyle(baseStyle);
        
        btn.setOnMouseEntered(e -> {
            if (!COLOR_ACCENT.equals(((Color)btn.getTextFill()).toString().toUpperCase().substring(0, 7))) {
                btn.setStyle(hoverStyle);
            }
        });
        btn.setOnMouseExited(e -> {
            if (!"#FFFFFF".equals(((Color)btn.getTextFill()).toString().toUpperCase().substring(0, 7))) {
                btn.setStyle(baseStyle);
            }
        });

        return btn;
    }

    public static Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        String style = "-fx-background-color: " + COLOR_ACCENT + "; -fx-text-fill: white; -fx-font-weight: 800; " +
                       "-fx-background-radius: 10; -fx-padding: 12 25 12 25; -fx-cursor: hand;";
        btn.setStyle(style);
        btn.setOnMouseEntered(e -> btn.setStyle(style + "-fx-background-color: #1D4ED8; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        btn.setOnMouseExited(e -> btn.setStyle(style));
        return btn;
    }

    public static void setupNumericValidation(javafx.scene.control.TextField textField) {
        textField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
    }

    public static String getTableStyle() {
        return "-fx-background-color: " + COLOR_SIDEBAR + "; " +
               "-fx-control-inner-background: " + COLOR_SIDEBAR + "; " +
               "-fx-table-cell-border-color: rgba(0,0,0,0.05); " +
               "-fx-background-radius: 12; " +
               "-fx-border-radius: 12; " +
               "-fx-text-fill: " + COLOR_TEXT_PRIMARY + ";";
    }

    public static String getProgressBarStyle() {
        return "-fx-accent: " + COLOR_ACCENT + "; -fx-control-inner-background: #E2E8F0; -fx-text-box-border: transparent; -fx-background-radius: 10;";
    }
}
