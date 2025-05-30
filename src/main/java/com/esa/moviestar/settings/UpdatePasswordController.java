package com.esa.moviestar.settings;

import com.esa.moviestar.database.AccountDao;
import com.esa.moviestar.login.Access;
import com.esa.moviestar.login.AnimationUtils;
import com.esa.moviestar.login.Register;
import com.esa.moviestar.model.Account;
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

import static com.esa.moviestar.login.Access.verifyPassword;
import static com.esa.moviestar.login.Register.hashPassword;

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
    private Label warningText;

    @FXML
    private StackPane parentContainer;

    private String userEmail;

    private Utente utente;
    public void setUtente(Utente utente){
        this.utente=utente;
        System.out.println("UpdatePasswordController : utente passato : "+utente.getNome());
    }
    private Account account;
    public void setAccount(Account account) {
        this.account=account;
        System.out.println("UpdatePasswordController : email passata : "+account.getEmail());
    }


    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");


    public void initialize() {
        if (statusMessage != null) {
            statusMessage.setText("");
        }

        if (oldPasswordField != null) {
            oldPasswordField.setPromptText("Old Password");
        }
        if (newPasswordField != null) {
            newPasswordField.setPromptText("New Password");
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setPromptText("Confirm the new Password");
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

    }



    private void validatePasswordReset() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            updateStatus("All fields are required.");
            warningText.setText("All fields are required.");
            AnimationUtils.shake(warningText);
            return;
        }

        AccountDao dao = new AccountDao();
        if(verifyPassword(account.getPassword(), oldPassword)){
            boolean oldPasswordCorrect = dao.checkPassword(account.getEmail(), oldPassword);
            if (!oldPasswordCorrect) {
                updateStatus("The old password is incorrect.");
                warningText.setText("The old password is incorrect.");
                AnimationUtils.shake(warningText);
                return;
            }
        }else{
            System.out.println("errore");
        }

        if (!newPassword.equals(confirmPassword)) {
            updateStatus("Passwords do not match.");
            warningText.setText("Passwords do not match.");
            AnimationUtils.shake(warningText);
            return;
        }

        Register tempRegister = new Register();
        if (!Pattern.matches(tempRegister.get_regex(), newPassword)) {
            updateStatus("The password does not meet the security requirements.");
            warningText.setText("The password does not meet the security requirements.");
            AnimationUtils.shake(warningText);
            return;
        }

        try {
            cambiaPassword(account.getEmail(), newPassword);
            updateStatus("Password cambiata con successo");
            AnimationUtils.pulse(updateButton);

            PauseTransition pause = new PauseTransition(Duration.seconds(0.25));
            pause.setOnFinished(e -> navigateToAccess());
            pause.play();
        } catch (SQLException e) {
            updateStatus("Errore durante il cambio password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cambiaPassword(String email, String newPassword) throws SQLException {
        AccountDao dao = new AccountDao();
        dao.updatePassword(email, hashPassword(newPassword));
    }

    private void updateStatus(String message) {
        if (statusMessage != null) {
            statusMessage.setText(message);
        } else {
            System.out.println("Status message label not found: " + message);
        }
    }

    private void navigateToAccess(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"),resourceBundle);
            Parent accountSettingContent = loader.load();
            Access access = loader.getController();
            access.setAccount(account);

            Scene currentScene = parentContainer.getScene();

            Scene newScene = new Scene(accountSettingContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) parentContainer.getScene().getWindow();
            stage.setScene(newScene);

        } catch (IOException e) {
            e.printStackTrace();
            updateStatus("Errore durante il caricamento della pagina");
        }
    }

    private void navigateToSetting() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/settings-view.fxml"),resourceBundle);
            Parent accountSettingContent = loader.load();
            SettingsViewController settingsViewController = loader.getController();
            settingsViewController.setUtente(utente);
            settingsViewController.setAccount(account);

            Scene currentScene = parentContainer.getScene();

            Scene newScene = new Scene(accountSettingContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) parentContainer.getScene().getWindow();
            stage.setScene(newScene);

        } catch (IOException e) {
            e.printStackTrace();
            updateStatus("Errore durante il caricamento della pagina");
        }
    }

}
