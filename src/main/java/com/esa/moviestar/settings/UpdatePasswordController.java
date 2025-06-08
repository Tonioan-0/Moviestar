package com.esa.moviestar.settings;

import com.esa.moviestar.Main;
import com.esa.moviestar.database.AccountDao;
import com.esa.moviestar.login.Access;
import com.esa.moviestar.login.AnimationUtils;
import com.esa.moviestar.login.Register;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.User;
import com.esa.moviestar.libraries.CredentialCryptManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import java.util.regex.Pattern;


public class UpdatePasswordController {
    @FXML
    private PasswordField oldPasswordField;

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

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    private Account account;

    public void setAccount(Account account) {
        this.account = account;

    }


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
            updateButton.setOnAction(event -> validatePasswordUpdate());
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


    private void validatePasswordUpdate() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            warningText.setText("All fields are required.");
            AnimationUtils.shake(warningText);
            return;
        }

        AccountDao dao = new AccountDao();
        if (CredentialCryptManager.verifyPassword(account.getPassword(), oldPassword)) {
            boolean oldPasswordCorrect = dao.checkPassword(account.getEmail(), oldPassword);
            if (!oldPasswordCorrect) {
                warningText.setText("The old password is incorrect.");
                AnimationUtils.shake(warningText);
                return;
            }
        } else {
            System.out.println("error");
        }

        if (!newPassword.equals(confirmPassword)) {
            warningText.setText("Passwords do not match.");
            AnimationUtils.shake(warningText);
            return;
        }

        Register tempRegister = new Register();
        if (!Pattern.matches(tempRegister.get_regex(), newPassword)) {
            warningText.setText("The password does not meet the security requirements.");
            AnimationUtils.shake(warningText);
            return;
        }

        try {
            changePassword(account.getEmail(), newPassword);

            AnimationUtils.pulse(updateButton);

            PauseTransition pause = new PauseTransition(Duration.seconds(0.25));
            pause.setOnFinished(e -> navigateToAccess());
            pause.play();
        } catch (SQLException e) {
            System.err.println("Error while changing password " + e.getMessage());

        }
    }

    private void changePassword(String email, String newPassword) throws SQLException {
        AccountDao dao = new AccountDao();
        dao.updatePassword(email, CredentialCryptManager.hashPassword(newPassword));
    }

    private void navigateToAccess() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml") ,  Main.resourceBundle);
            Parent accountSettingContent = loader.load();
            Access access =  loader.getController();
            access.setAccount(account);

            Scene currentScene = parentContainer.getScene();

            Scene newScene = new Scene(accountSettingContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) parentContainer.getScene().getWindow();
            stage.setScene(newScene);

        } catch (IOException e) {
            System.err.println("Error while loading the page : access"+e.getMessage());

        }
    }

    private void navigateToSetting(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/settings-view.fxml" ), Main.resourceBundle);
            Parent accountSettingContent = loader.load();
            SettingsViewController settingsViewController = loader.getController();
            settingsViewController.setUser(user);
            settingsViewController.setAccount(account);

            Scene currentScene = parentContainer.getScene();

            Scene newScene = new Scene(accountSettingContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) parentContainer.getScene().getWindow();
            stage.setScene(newScene);

        } catch (IOException e) {
            System.err.println("Error while loading the page : settings"+e.getMessage());
        }
    }

}
