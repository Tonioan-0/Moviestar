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
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;


public class UpdatePasswordController{

    @FXML private Button updateButton;
    @FXML private Button backToSettingButton;

    @FXML private VBox mainContainer;
    @FXML private StackPane parentContainer;
    @FXML private PasswordField oldPasswordField;

    //Password structure stackPane -> passwordField toggleButton
    //                             |- textField
    @FXML private StackPane newPasswordContainer;
    @FXML private StackPane confirmPasswordContainer;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField newPasswordTextField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordTextField;
    @FXML private Button toggleNewPasswordButton;
    @FXML private Button toggleConfirmPasswordButton;

    @FXML private Label statusMessage;
    @FXML private Label warningText;

    private String userEmail;

    private User user;
    private Account account;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    public void setUser(User user){
        this.user = user;
        System.out.println("UpdatePasswordController : user  : " + user.getName());
    }

    public void setAccount(Account account){
        this.account = account;
        System.out.println("UpdatePasswordController : email  : " + account.getEmail());
    }


    public void initialize(){
        if (statusMessage != null)
            statusMessage.setText("");

        if (warningText != null)
            warningText.setText("");


        if (oldPasswordField != null)
            oldPasswordField.setPromptText("Old Password");

        if (newPasswordField != null)
            newPasswordField.setPromptText("New Password");

        if (newPasswordTextField != null)
            newPasswordTextField.setPromptText("New Password");

        if (confirmPasswordField != null)
            confirmPasswordField.setPromptText("Confirm the new Password");

        if (confirmPasswordTextField != null)
            confirmPasswordTextField.setPromptText("Confirm the new Password");



        if (updateButton != null)
            updateButton.setOnAction(event -> validatePasswordUpdate());

        if (backToSettingButton != null)
            backToSettingButton.setOnAction(event -> navigateToSetting());


        if (toggleNewPasswordButton != null){
            toggleNewPasswordButton.setGraphic(new SVGPath(){{
                setContent(Main.resourceBundle.getString("passwordField.showPassword"));
                getStyleClass().add("on-primary");
            }});
        }
        if (toggleConfirmPasswordButton != null){
            toggleConfirmPasswordButton.setGraphic(new SVGPath(){{
                setContent(Main.resourceBundle.getString("passwordField.showPassword"));
                getStyleClass().add("on-primary");
            }});
        }

        setupPasswordToggle();

        if (updateButton != null && oldPasswordField != null &&
                newPasswordContainer != null && confirmPasswordContainer != null &&
                backToSettingButton != null &&
                toggleNewPasswordButton != null && toggleConfirmPasswordButton != null &&
                newPasswordTextField != null && confirmPasswordTextField != null){
            Node[] formElements ={
                    backToSettingButton, oldPasswordField,
                    newPasswordContainer, confirmPasswordContainer,
                    updateButton
            };
            AnimationUtils.animateSimultaneously(formElements);
        }
    }

    private void setupPasswordToggle(){
        // Setup for New Password
        if (newPasswordField != null && newPasswordTextField != null && toggleNewPasswordButton != null){
            // Sincronizza i testi tra i due campi
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

            //  toggle button
            toggleNewPasswordButton.setOnAction(event -> toggleNewPasswordVisibility());
            StackPane.setAlignment(toggleNewPasswordButton, Pos.CENTER_RIGHT);
            StackPane.setMargin(toggleNewPasswordButton, new Insets(0, 10, 0, 0));
        }

        // Setup for Confirm Password
        if (confirmPasswordField != null && confirmPasswordTextField != null && toggleConfirmPasswordButton != null){
            // Sincronizza i testi tra i due campi
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

            //  toggle button
            toggleConfirmPasswordButton.setOnAction(event -> toggleConfirmPasswordVisibility());
            StackPane.setAlignment(toggleConfirmPasswordButton, Pos.CENTER_RIGHT);
            StackPane.setMargin(toggleConfirmPasswordButton, new Insets(0, 10, 0, 0));
        }
    }

    private void toggleNewPasswordVisibility(){
        isNewPasswordVisible = !isNewPasswordVisible;
        String currentText = isNewPasswordVisible ? newPasswordField.getText() : newPasswordTextField.getText();

        if (isNewPasswordVisible) {
            // password like text
            newPasswordTextField.setText(currentText);
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
            newPasswordTextField.setVisible(true);
            newPasswordTextField.setManaged(true);
            newPasswordTextField.requestFocus();
            newPasswordTextField.positionCaret(newPasswordTextField.getText().length());

            // change icon
            toggleNewPasswordButton.setGraphic(new SVGPath(){{
                setContent(Main.resourceBundle.getString("passwordField.hidePassword"));
                getStyleClass().add("on-primary");
            }});
        } else {
            // hide the password
            newPasswordField.setText(currentText);
            newPasswordTextField.setVisible(false);
            newPasswordTextField.setManaged(false);
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            newPasswordField.requestFocus();
            newPasswordField.positionCaret(newPasswordField.getText().length());

            // change the icon
            toggleNewPasswordButton.setGraphic(new SVGPath(){{
                setContent(Main.resourceBundle.getString("passwordField.showPassword"));
                getStyleClass().add("on-primary");
            }});
        }
    }

    private void toggleConfirmPasswordVisibility(){
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        String currentText = isConfirmPasswordVisible ? confirmPasswordField.getText() : confirmPasswordTextField.getText();

        if (isConfirmPasswordVisible) {

            confirmPasswordTextField.setText(currentText);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            confirmPasswordTextField.requestFocus();
            confirmPasswordTextField.positionCaret(confirmPasswordTextField.getText().length());

            // Cambia icona per nascondere
            toggleConfirmPasswordButton.setGraphic(new SVGPath(){{
                setContent(Main.resourceBundle.getString("passwordField.hidePassword"));
                getStyleClass().add("on-primary");
            }});
        } else {
            confirmPasswordField.setText(currentText);
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmPasswordField.requestFocus();
            confirmPasswordField.positionCaret(confirmPasswordField.getText().length());

            toggleConfirmPasswordButton.setGraphic(new SVGPath(){{
                setContent(Main.resourceBundle.getString("passwordField.showPassword"));
                getStyleClass().add("on-primary");
            }});
        }
    }

    private String getCurrentNewPassword(){
        return isNewPasswordVisible ? newPasswordTextField.getText() : newPasswordField.getText();
    }

    private String getCurrentConfirmPassword(){
        return isConfirmPasswordVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();
    }



    private void changePassword(String email, String newPassword) throws SQLException{
        AccountDao dao = new AccountDao();
        dao.updatePassword(email, CredentialCryptManager.hashPassword(newPassword));
    }

    private void navigateToAccess(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"), Main.resourceBundle);
            Parent accountSettingContent = loader.load();
            Access access = loader.getController();
            access.setAccount(account);

            Scene currentScene = parentContainer.getScene();
            Scene newScene = new Scene(accountSettingContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) parentContainer.getScene().getWindow();
            stage.setScene(newScene);

        } catch (IOException e){
            System.err.println("Error while loading the page : access"+e.getMessage());
            if(warningText != null){
                warningText.setText("Error loading login page.");
            }
        }
    }
    private void validatePasswordUpdate(){
        String oldPassword = oldPasswordField.getText();
        String newPassword = getCurrentNewPassword();
        String confirmPassword = getCurrentConfirmPassword();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()){
            warningText.setText("All fields are required.");
            AnimationUtils.shake(warningText);
            return;
        }

        if (!CredentialCryptManager.verifyPassword(oldPassword, account.getPassword() )){
            warningText.setText("The old password is incorrect.");
            AnimationUtils.shake(warningText);
            System.out.println("ERROR: Password verification failed");
            return;
        }

        if (!newPassword.equals(confirmPassword)){
            warningText.setText("Passwords do not match.");
            AnimationUtils.shake(warningText);
            return;
        }

        Register tempRegister = new Register();
        if (!Pattern.matches(tempRegister.get_regex(), newPassword)){
            warningText.setText("The password does not meet the security requirements.");
            AnimationUtils.shake(warningText);
            return;
        }

        try{
            changePassword(account.getEmail(), newPassword);
            AnimationUtils.pulse(updateButton);

            PauseTransition pause = new PauseTransition(Duration.seconds(0.25));
            pause.setOnFinished(e -> navigateToAccess());
            pause.play();
        } catch (SQLException e){
            warningText.setText("Error while changing password.");
            AnimationUtils.shake(warningText);
            System.err.println("Error while changing password " + e.getMessage());
        }
    }
    private void navigateToSetting(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/settings-view.fxml"), Main.resourceBundle);
            Parent accountSettingContent = loader.load();
            SettingsViewController settingsViewController = loader.getController();
            settingsViewController.setUser(user);
            settingsViewController.setAccount(account);

            Scene currentScene = parentContainer.getScene();
            Scene newScene = new Scene(accountSettingContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) parentContainer.getScene().getWindow();
            stage.setScene(newScene);

        } catch (IOException e){
            System.err.println("Error while loading the page : settings"+e.getMessage());
            if(warningText != null){
                warningText.setText("Error loading settings page.");
            }
        }
    }
}