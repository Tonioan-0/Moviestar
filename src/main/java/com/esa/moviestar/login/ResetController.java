package com.esa.moviestar.login;

import com.esa.moviestar.database.AccountDao;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

// BCrypt import
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ResetController {

    @FXML
    private TextField codeField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button resetButton;

    @FXML
    private Button backToLoginButton;

    @FXML
    private VBox mainContainer;

    @FXML
    private Label statusMessage;

    @FXML
    private StackPane parentContainer;

    // Attributi per il reset della password
    private String userEmail;
    private String verificationCode;

    // BCrypt work factor (cost parameter) - stesso valore utilizzato in Register
    private static final int BCRYPT_ROUNDS = 12;

    // Valori di riferimento per il layout responsivo
    private final double REFERENCE_WIDTH = 1720.0;
    private final double REFERENCE_HEIGHT = 980.0;
    private final double REFERENCE_CONTAINER_WIDTH = 500.0;
    private final double REFERENCE_CONTAINER_HEIGHT = 559.0;
    private final double COMPACT_MODE_THRESHOLD = 500.0;
    private final double MIN_VBOX_VISIBILITY_THRESHOLD = 400.0;

    public void initialize() {
        // Make sure statusMessage is empty at start
        if (statusMessage != null) {
            statusMessage.setText("");
        }

        // Configure UI components
        if (codeField != null) {
            codeField.setPromptText("Verifycation code");
        }

        if (newPasswordField != null) {
            newPasswordField.setPromptText("New password");
        }

        if (confirmPasswordField != null) {
            confirmPasswordField.setPromptText("Confirm new password");
        }

        // Setup resetButton
        if (resetButton != null) {
            resetButton.setOnAction(event -> validatePasswordReset());
        }

        // Setup backToLoginButton
        if (backToLoginButton != null) {
            backToLoginButton.setOnAction(event -> navigateToLogin());
        }

        // Add subtle animation to the elements when loaded
        if (resetButton != null && newPasswordField != null &&
                confirmPasswordField != null && codeField != null && backToLoginButton != null) {
            Node[] formElements = {backToLoginButton, codeField, newPasswordField, confirmPasswordField, resetButton};
            AnimationUtils.animateSimultaneously(formElements, 1);
        }

        // Setup responsive layout
        setupResponsiveLayout();
    }

    private void setupResponsiveLayout() {
        if (parentContainer != null) {
            parentContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.widthProperty().addListener((observable, oldValue, newValue) ->
                            adjustLayout(newValue.doubleValue(), newScene.getHeight()));
                    newScene.heightProperty().addListener((observable, oldValue, newValue) ->
                            adjustLayout(newScene.getWidth(), newValue.doubleValue()));
                    adjustLayout(newScene.getWidth(), newScene.getHeight()); // Initial adjustment
                }
            });
        }
    }

    private void adjustLayout(double width, double height) {
        // Scale factor based on the SMALLER dimension
        double rawScale = Math.min(width / REFERENCE_WIDTH, height / REFERENCE_HEIGHT);
        double scale = 1 - (1 - rawScale) * 0.5;

        // Handle main container
        if (mainContainer != null) {
            boolean showContainer = width > MIN_VBOX_VISIBILITY_THRESHOLD;
            mainContainer.setVisible(showContainer);
            mainContainer.setManaged(showContainer);

            if (showContainer) {
                boolean compactMode = width < COMPACT_MODE_THRESHOLD;

                double containerWidth = compactMode ? 280 : REFERENCE_CONTAINER_WIDTH * scale;
                double containerHeight = compactMode ? 300 : REFERENCE_CONTAINER_HEIGHT * scale;

                mainContainer.setPrefWidth(containerWidth);
                mainContainer.setPrefHeight(containerHeight);
                mainContainer.setMaxWidth(containerWidth);
                mainContainer.setMaxHeight(containerHeight);

                // Dynamic padding and spacing
                double padding = Math.max(10, 20 * scale);
                double spacing = Math.max(5, 10 * scale);
                mainContainer.setPadding(new Insets(padding));
                mainContainer.setSpacing(spacing);

                // Positioning
                StackPane.setAlignment(mainContainer, compactMode ? Pos.CENTER : Pos.CENTER);
                StackPane.setMargin(mainContainer, compactMode ? new Insets(0) : new Insets(0));

                // Dynamic font sizes
                double baseFontSize = 15 * scale;
                double buttonScale = Math.max(scale, 0.7); // Never below 70% of original size
                double statusTextScale = Math.max(scale, 0.7);

                if (resetButton != null) {
                    // Smaller font size for button
                    resetButton.setStyle("-fx-font-size: " + (Math.min(18 * buttonScale, 18)+5) + "px;");
                }

                if (backToLoginButton != null) {
                    backToLoginButton.setStyle("-fx-font-size: " + Math.min(14 * buttonScale, 14) + "px; -fx-alignment: CENTER_LEFT;");
                    backToLoginButton.setAlignment(Pos.CENTER_LEFT);
                }

                if (statusMessage != null) {
                    statusMessage.setStyle("-fx-font-size: " + (baseFontSize) + "px;");
                }

                double fieldWidth = (Math.min(containerWidth - padding * 2, containerWidth * 0.9) - 15);
                if (codeField != null) {
                    codeField.setPrefWidth(fieldWidth);
                    codeField.setMaxWidth(fieldWidth);
                }
                if (newPasswordField != null) {
                    newPasswordField.setPrefWidth(fieldWidth);
                    newPasswordField.setMaxWidth(fieldWidth);
                }
                if (confirmPasswordField != null) {
                    confirmPasswordField.setPrefWidth(fieldWidth);
                    confirmPasswordField.setMaxWidth(fieldWidth);
                }

                // Dynamic margins
                double verticalMargin = 10 * scale;

                if (codeField != null) {
                    VBox.setMargin(codeField, new Insets((verticalMargin + 25), 0, verticalMargin, 0));
                }
                if (newPasswordField != null) {
                    VBox.setMargin(newPasswordField, new Insets(verticalMargin, 0,0, 0));
                }
                if (confirmPasswordField != null) {
                    VBox.setMargin(confirmPasswordField, new Insets(0, 0, 0, 0));
                }
                if (resetButton != null) {
                    VBox.setMargin(resetButton, new Insets((verticalMargin + 10), 0, 0, 0));
                }
                if (backToLoginButton != null) {
                    VBox.setMargin(backToLoginButton, new Insets((verticalMargin), 0, verticalMargin - 30, 0));
                }

            }
        }
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public void setVerificationCode(String code) {
        this.verificationCode = code;
    }

    public void setupResetButton() {
        if (resetButton != null) {
            resetButton.setOnAction(event -> validatePasswordReset());
        }
    }

    private String hashPassword(String plainTextPassword) {

        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(BCRYPT_ROUNDS));

    }

    private void validatePasswordReset() {
        String inputCode = codeField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Controllo se i campi sono vuoti
        if (inputCode.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            updateStatus("I campi non possono essere vuoti");
            AnimationUtils.shake(statusMessage);
            return;
        }

        // Controllo se il codice di verifica corrisponde
        if (!inputCode.equals(verificationCode)) {
            updateStatus("Codice di verifica errato");
            AnimationUtils.shake(statusMessage);
            return;
        }

        // Controllo se le password corrispondono
        if (!newPassword.equals(confirmPassword)) {
            updateStatus("Le password non corrispondono");
            AnimationUtils.shake(statusMessage);
            return;
        }

        Register tempRegister = new Register();
        if (!Pattern.matches(tempRegister.get_regex(), newPassword)) {
            updateStatus("La password non rispetta i requisiti di sicurezza");
            AnimationUtils.shake(statusMessage);
            return;
        }

        try {
            String hashedPassword = hashPassword(newPassword);

            cambiaPassword(userEmail, hashedPassword);

            updateStatus("Password cambiata con successo");
            AnimationUtils.pulse(resetButton);

            PauseTransition pause = new PauseTransition(Duration.seconds(0.25));
            pause.setOnFinished(e -> navigateToLogin());
            pause.play();
        } catch (SQLException e) {
            e.printStackTrace();
            updateStatus("Errore durante il reset della password: " + e.getMessage());
        }
    }

    private void cambiaPassword(String email, String hashedPassword) throws SQLException {
        AccountDao dao = new AccountDao();
        dao.updatePassword(email, hashedPassword);
    }

    private void updateStatus(String message) {
        if (statusMessage != null) {
            statusMessage.setText(message);
        } else {
            System.out.println("Status message label not found: " + message);
        }
    }
    public final ResourceBundle resourceBundle = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");

    private void navigateToLogin() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"),resourceBundle);
            Parent loginContent = loader.load();
            Scene currentScene = parentContainer.getScene();

            Scene newScene = new Scene(loginContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) parentContainer.getScene().getWindow();
            stage.setScene(newScene);

        }
        catch (IOException e) {
            e.printStackTrace();
            updateStatus("An error occurred while navigating to login: " + e.getMessage());
        }
    }
}