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
        if (newPasswordField != null && newPasswordTextField != null && toggleNewPasswordButton != null){
            newPasswordField.textProperty().addListener((obs, oldText, newText) ->{
                if (!newPasswordTextField.isFocused())
                    newPasswordTextField.setText(newText);
            });

            newPasswordTextField.textProperty().addListener((obs, oldText, newText) ->{
                if (!newPasswordField.isFocused())
                    newPasswordField.setText(newText);
            });

            toggleNewPasswordButton.setOnAction(event -> togglePasswordVisibility( isNewPasswordVisible, newPasswordField, newPasswordTextField, toggleNewPasswordButton));
            StackPane.setAlignment(toggleNewPasswordButton, Pos.CENTER_RIGHT);
            StackPane.setMargin(toggleNewPasswordButton, new Insets(0, 10, 0, 0));
        }

        if (confirmPasswordField != null && confirmPasswordTextField != null && toggleConfirmPasswordButton != null){
            confirmPasswordField.textProperty().addListener((obs, oldText, newText) ->{
                if (!confirmPasswordTextField.isFocused())
                    confirmPasswordTextField.setText(newText);

            });

            confirmPasswordTextField.textProperty().addListener((obs, oldText, newText) ->{
                if (!confirmPasswordField.isFocused())
                    confirmPasswordField.setText(newText);
            });

            toggleConfirmPasswordButton.setOnAction(event -> togglePasswordVisibility(isConfirmPasswordVisible, confirmPasswordField, confirmPasswordTextField, toggleConfirmPasswordButton));
            StackPane.setAlignment(toggleConfirmPasswordButton, Pos.CENTER_RIGHT);
            StackPane.setMargin(toggleConfirmPasswordButton, new Insets(0, 10, 0, 0));
        }
    }
    private void togglePasswordVisibility(boolean variable, PasswordField passwordField, TextField textField, Button toggleButton){
        variable = !variable;
        String currentPassword = variable ? passwordField.getText() : textField.getText();

        if (variable){
            textField.setText(currentPassword);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            textField.setVisible(true);
            textField.setManaged(true);
            textField.requestFocus();
            textField.positionCaret(textField.getText().length());
            toggleButton.setGraphic(new SVGPath(){{
                setContent(Main.resourceBundle.getString("passwordField.hidePassword"));
                getStyleClass().add("on-primary");
            }});
        } else{
            passwordField.setText(currentPassword);
            textField.setVisible(false);
            textField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            toggleButton.setGraphic(new SVGPath(){{
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


    private void validatePasswordUpdate(){
        String oldPassword = oldPasswordField.getText();
        String newPassword = getCurrentNewPassword();
        String confirmPassword = getCurrentConfirmPassword();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()){
            warningText.setText("All fields are required.");
            AnimationUtils.shake(warningText);
            return;
        }

        AccountDao dao = new AccountDao();
        if (CredentialCryptManager.verifyPassword(account.getPassword(), oldPassword)){
            boolean oldPasswordCorrect = dao.checkPassword(account.getEmail(), oldPassword);
            if (!oldPasswordCorrect){
                warningText.setText("The old password is incorrect.");
                AnimationUtils.shake(warningText);
                return;
            }
        } else{
            warningText.setText("The old password is incorrect.");
            AnimationUtils.shake(warningText);
            System.out.println("error: CredentialCryptManager.verifyPassword returned false for old password");
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

    private void changePassword(String email, String newPassword) throws SQLException{
        AccountDao dao = new AccountDao();
        dao.updatePassword(email, CredentialCryptManager.hashPassword(newPassword));
    }

    private void navigateToAccess(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"), Main.resourceBundle);
            Parent accountSettingContent = loader.load();
            Access access = loader.getController();
            // access.setAccount(account);

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