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
    private TextField newPasswordTextField; // For password visibility toggle

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField confirmPasswordTextField; // For password visibility toggle

    @FXML
    private Button resetButton;

    @FXML
    private StackPane backToLoginButton;

    @FXML
    private VBox mainContainer;

    @FXML
    private Label statusMessage;

    @FXML
    private StackPane parentContainer;

    @FXML
    private StackPane newPasswordContainer; // Container for new password field with toggle

    @FXML
    private StackPane confirmPasswordContainer; // Container for confirm password field with toggle

    @FXML
    private Button toggleNewPasswordButton; // Toggle button for new password

    @FXML
    private Button toggleConfirmPasswordButton; // Toggle button for confirm password

    // Attributi per il reset della password
    private String userEmail;
    private String verificationCode;

    // Password visibility states
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private static final int BCRYPT_ROUNDS = 12;

    private final double REFERENCE_WIDTH = 1720.0;
    private final double REFERENCE_HEIGHT = 980.0;
    private final double REFERENCE_CONTAINER_WIDTH = 500.0;
    private final double REFERENCE_CONTAINER_HEIGHT = 559.0;
    private final double COMPACT_MODE_THRESHOLD = 500.0;
    private final double MIN_VBOX_VISIBILITY_THRESHOLD = 400.0;

    public void initialize() {

        if (statusMessage != null) {
            statusMessage.setText("");
        }

        // Configure UI components
        if (codeField != null) {
            codeField.setPromptText("Verification code");
        }

        if (newPasswordField != null) {
            newPasswordField.setPromptText("New password");
        }

        if (newPasswordTextField != null) {
            newPasswordTextField.setPromptText("New password");
        }

        if (confirmPasswordField != null) {
            confirmPasswordField.setPromptText("Confirm new password");
        }

        if (confirmPasswordTextField != null) {
            confirmPasswordTextField.setPromptText("Confirm new password");
        }

        if (resetButton != null) {
            resetButton.setOnAction(event -> validatePasswordReset());
        }

        if (backToLoginButton != null) {
            backToLoginButton.setOnMouseClicked(event -> navigateToLogin());
        }

        // Setup password toggle functionality
        setupPasswordToggle();

        if (resetButton != null && newPasswordField != null &&
                confirmPasswordField != null && codeField != null && backToLoginButton != null) {
            Node[] formElements = {backToLoginButton, codeField, newPasswordContainer, confirmPasswordContainer, resetButton};
            AnimationUtils.animateSimultaneously(formElements, 1, 0.3);
        }

        setupResponsiveLayout();
    }

    // Password toggle setup similar to Access class
    private void setupPasswordToggle() {
        // Setup new password toggle
        if (newPasswordField != null && newPasswordTextField != null && toggleNewPasswordButton != null) {
            newPasswordField.textProperty().addListener((obs, oldText, newText) -> {
                if (!newPasswordTextField.isFocused()) {
                    newPasswordTextField.setText(newText);
                }
            });

            newPasswordTextField.textProperty().addListener((obs, oldText, newText) -> {
                if (!newPasswordField.isFocused()) {
                    newPasswordField.setText(newText);
                }
            });

            toggleNewPasswordButton.setOnAction(event -> toggleNewPasswordVisibility());
            StackPane.setAlignment(toggleNewPasswordButton, Pos.CENTER_RIGHT);
            StackPane.setMargin(toggleNewPasswordButton, new Insets(0, 10, 0, 0));
        }

        // Setup confirm password toggle
        if (confirmPasswordField != null && confirmPasswordTextField != null && toggleConfirmPasswordButton != null) {
            confirmPasswordField.textProperty().addListener((obs, oldText, newText) -> {
                if (!confirmPasswordTextField.isFocused()) {
                    confirmPasswordTextField.setText(newText);
                }
            });

            confirmPasswordTextField.textProperty().addListener((obs, oldText, newText) -> {
                if (!confirmPasswordField.isFocused()) {
                    confirmPasswordField.setText(newText);
                }
            });

            toggleConfirmPasswordButton.setOnAction(event -> toggleConfirmPasswordVisibility());
            StackPane.setAlignment(toggleConfirmPasswordButton, Pos.CENTER_RIGHT);
            StackPane.setMargin(toggleConfirmPasswordButton, new Insets(0, 10, 0, 0));
        }

    }


    private void toggleNewPasswordVisibility() {
        isNewPasswordVisible = !isNewPasswordVisible;

        if (isNewPasswordVisible) {
            newPasswordTextField.setText(newPasswordField.getText());
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
            newPasswordTextField.setVisible(true);
            newPasswordTextField.setManaged(true);
            newPasswordTextField.requestFocus();
            newPasswordTextField.positionCaret(newPasswordTextField.getText().length());
            toggleNewPasswordButton.setText("🙈");
        } else {
            newPasswordField.setText(newPasswordTextField.getText());
            newPasswordTextField.setVisible(false);
            newPasswordTextField.setManaged(false);
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            newPasswordField.requestFocus();
            newPasswordField.positionCaret(newPasswordField.getText().length());
            toggleNewPasswordButton.setText("👁");
        }
    }

    private void toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        if (isConfirmPasswordVisible) {
            confirmPasswordTextField.setText(confirmPasswordField.getText());
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            confirmPasswordTextField.requestFocus();
            confirmPasswordTextField.positionCaret(confirmPasswordTextField.getText().length());
            toggleConfirmPasswordButton.setText("🙈");
        } else {
            confirmPasswordField.setText(confirmPasswordTextField.getText());
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordField.requestFocus();
            confirmPasswordField.positionCaret(confirmPasswordField.getText().length());
            toggleConfirmPasswordButton.setText("👁");
        }
    }

    // Get current password values considering visibility state
    private String getCurrentNewPassword() {
        return isNewPasswordVisible ? newPasswordTextField.getText() : newPasswordField.getText();
    }

    private String getCurrentConfirmPassword() {
        return isConfirmPasswordVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();
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

                // Update password containers and fields
                if (newPasswordContainer != null) {
                    newPasswordContainer.setPrefWidth(fieldWidth);
                    newPasswordContainer.setMaxWidth(fieldWidth);
                }
                if (newPasswordField != null) {
                    newPasswordField.setPrefWidth(fieldWidth);
                    newPasswordField.setMaxWidth(fieldWidth);
                }
                if (newPasswordTextField != null) {
                    newPasswordTextField.setPrefWidth(fieldWidth);
                    newPasswordTextField.setMaxWidth(fieldWidth);
                }

                if (confirmPasswordContainer != null) {
                    confirmPasswordContainer.setPrefWidth(fieldWidth);
                    confirmPasswordContainer.setMaxWidth(fieldWidth);
                }
                if (confirmPasswordField != null) {
                    confirmPasswordField.setPrefWidth(fieldWidth);
                    confirmPasswordField.setMaxWidth(fieldWidth);
                }
                if (confirmPasswordTextField != null) {
                    confirmPasswordTextField.setPrefWidth(fieldWidth);
                    confirmPasswordTextField.setMaxWidth(fieldWidth);
                }

                // Dynamic margins
                double verticalMargin = 10 * scale;

                if (codeField != null) {
                    VBox.setMargin(codeField, new Insets((verticalMargin + 25), 0, verticalMargin, 0));
                }
                if (newPasswordContainer != null) {
                    VBox.setMargin(newPasswordContainer, new Insets(verticalMargin, 0,0, 0));
                }
                if (confirmPasswordContainer != null) {
                    VBox.setMargin(confirmPasswordContainer, new Insets(0, 0, 0, 0));
                }
                if (resetButton != null) {
                    VBox.setMargin(resetButton, new Insets((verticalMargin + 10), 0, 0, 0));
                }
                if (backToLoginButton != null) {
                    VBox.setMargin(backToLoginButton, new Insets((verticalMargin), 0, verticalMargin - 30, verticalMargin-200));
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
        String newPassword = getCurrentNewPassword();
        String confirmPassword = getCurrentConfirmPassword();

        if (inputCode.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            updateStatus("Fields cannot be empty");
            AnimationUtils.shake(statusMessage);
            return;
        }
        if (!inputCode.equals(verificationCode)) {
            updateStatus("Incorrect verification code");
            AnimationUtils.shake(statusMessage);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            updateStatus("Passwords do not match");
            AnimationUtils.shake(statusMessage);
            return;
        }

        Register tempRegister = new Register();
        if (!Pattern.matches(tempRegister.get_regex(), newPassword)) {
            updateStatus("Password does not meet security requirements");
            AnimationUtils.shake(statusMessage);
            return;
        }

        try {
            String hashedPassword = hashPassword(newPassword);

            cambiaPassword(userEmail, hashedPassword);
            updateStatus("Password changed successfully");
            AnimationUtils.pulse(resetButton);

            PauseTransition pause = new PauseTransition(Duration.seconds(0.25));
            pause.setOnFinished(e -> navigateToLogin());

            pause.play();

        } catch (SQLException e) {
            e.printStackTrace();
            updateStatus("Error during password reset: " + e.getMessage());
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