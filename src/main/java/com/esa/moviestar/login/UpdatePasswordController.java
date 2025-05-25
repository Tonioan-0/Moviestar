package com.esa.moviestar.login;

import com.esa.moviestar.database.AccountDao;
import com.esa.moviestar.settings.SettingsViewController;
import com.esa.moviestar.model.Utente;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class UpdatePasswordController {
    @FXML
    private PasswordField oldPasswordField;  // Qui vecchia password

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button updateButton;

    @FXML
    private Button backToSettingButton;

    @FXML
    private VBox mainContainer;

    @FXML
    private Label statusMessage;

    @FXML
    private StackPane parentContainer;

    private String userEmail;

    private Utente utente;
    public void setUtente(Utente utente){
        this.utente=utente;
    }


    private final double REFERENCE_WIDTH = 1720.0;
    private final double REFERENCE_HEIGHT = 980.0;
    private final double REFERENCE_CONTAINER_WIDTH = 500.0;
    private final double REFERENCE_CONTAINER_HEIGHT = 559.0;
    private final double COMPACT_MODE_THRESHOLD = 500.0;
    private final double MIN_VBOX_VISIBILITY_THRESHOLD = 400.0;

    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");


    public void initialize() {
        if (statusMessage != null) {
            statusMessage.setText("");
        }

        if (oldPasswordField != null) {
            oldPasswordField.setPromptText("Vecchia Password");
        }
        if (newPasswordField != null) {
            newPasswordField.setPromptText("Nuova Password");
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setPromptText("Conferma Nuova Password");
        }

        if (updateButton != null) {
            updateButton.setOnAction(event -> validatePasswordReset());
        }
        if (backToSettingButton != null) {
            backToSettingButton.setOnAction(event -> navigateToSetting());
        }

        if (updateButton != null && oldPasswordField != null &&
                newPasswordField != null && confirmPasswordField != null && backToSettingButton != null) {
            Node[] formElements = {backToSettingButton, oldPasswordField, newPasswordField, confirmPasswordField, updateButton};
            AnimationUtils.animateSimultaneously(formElements, 1);
        }

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
                    adjustLayout(newScene.getWidth(), newScene.getHeight());
                }
            });
        }
    }

    private void adjustLayout(double width, double height) {
        double rawScale = Math.min(width / REFERENCE_WIDTH, height / REFERENCE_HEIGHT);
        double scale = 1 - (1 - rawScale) * 0.5;

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

                StackPane.setAlignment(mainContainer, Pos.CENTER);
                StackPane.setMargin(mainContainer, new Insets(0));

                double baseFontSize = 15 * scale;
                double buttonScale = Math.max(scale, 0.7);

                if (updateButton != null) {
                    updateButton.setStyle("-fx-font-size: " + (Math.min(18 * buttonScale, 18)+5) + "px;");
                }
                if (backToSettingButton != null) {
                    backToSettingButton.setStyle("-fx-font-size: " + Math.min(14 * buttonScale, 14) + "px; -fx-alignment: CENTER_LEFT;");
                    backToSettingButton.setAlignment(Pos.CENTER_LEFT);
                }
                if (statusMessage != null) {
                    statusMessage.setStyle("-fx-font-size: " + baseFontSize + "px;");
                }

                double fieldWidth = (Math.min(containerWidth - padding * 2, containerWidth * 0.9) - 15);
                if (oldPasswordField != null) {
                    oldPasswordField.setPrefWidth(fieldWidth);
                    oldPasswordField.setMaxWidth(fieldWidth);
                }
                if (newPasswordField != null) {
                    newPasswordField.setPrefWidth(fieldWidth);
                    newPasswordField.setMaxWidth(fieldWidth);
                }
                if (confirmPasswordField != null) {
                    confirmPasswordField.setPrefWidth(fieldWidth);
                    confirmPasswordField.setMaxWidth(fieldWidth);
                }

                double verticalMargin = 10 * scale;
                if (oldPasswordField != null) {
                    VBox.setMargin(oldPasswordField, new Insets(verticalMargin + 25, 0, verticalMargin, 0));
                }
                if (newPasswordField != null) {
                    VBox.setMargin(newPasswordField, new Insets(verticalMargin, 0, 0, 0));
                }
                if (confirmPasswordField != null) {
                    VBox.setMargin(confirmPasswordField, new Insets(0, 0, 0, 0));
                }
                if (updateButton != null) {
                    VBox.setMargin(updateButton, new Insets(verticalMargin + 10, 0, 0, 0));
                }
                if (backToSettingButton != null) {
                    VBox.setMargin(backToSettingButton, new Insets(verticalMargin, 0, verticalMargin - 30, 0));
                }
            }
        }
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    private void validatePasswordReset() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            updateStatus("Tutti i campi sono obbligatori");
            AnimationUtils.shake(statusMessage);
            return;
        }

        AccountDao dao = new AccountDao();
        boolean oldPasswordCorrect = dao.checkPassword(userEmail, oldPassword);
        if (!oldPasswordCorrect) {
            updateStatus("La vecchia password è errata");
            AnimationUtils.shake(statusMessage);
            return;
        }

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
            cambiaPassword(userEmail, newPassword);
            updateStatus("Password cambiata con successo");
            AnimationUtils.pulse(updateButton);

            PauseTransition pause = new PauseTransition(Duration.seconds(0.25));
            pause.setOnFinished(e -> navigateToSetting());
            pause.play();
        } catch (SQLException e) {
            updateStatus("Errore durante il cambio password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cambiaPassword(String email, String newPassword) throws SQLException {
        AccountDao dao = new AccountDao();
        dao.updatePassword(email, newPassword);
    }

    private void updateStatus(String message) {
        if (statusMessage != null) {
            statusMessage.setText(message);
        } else {
            System.out.println("Status message label not found: " + message);
        }
    }

    private void navigateToSetting() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/settings-view.fxml"),resourceBundle);
            Parent accountSettingContent = loader.load();
            Scene currentScene = parentContainer.getScene();
            SettingsViewController settingsViewController = loader.getController();
            settingsViewController.setUtente(utente);

            Scene newScene = new Scene(accountSettingContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) parentContainer.getScene().getWindow();
            stage.setScene(newScene);

        } catch (IOException e) {
            e.printStackTrace();
            updateStatus("Errore durante il caricamento della pagina");
        }
    }
}
