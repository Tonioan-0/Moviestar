package com.esa.moviestar.settings;

import com.esa.moviestar.Main;
import com.esa.moviestar.database.AccountDao;
import com.esa.moviestar.database.UserDao;
import com.esa.moviestar.login.AnimationUtils;
import com.esa.moviestar.model.User;
import com.esa.moviestar.profile.CreateProfileController;
import com.esa.moviestar.profile.IconSVG;
import com.esa.moviestar.profile.ModifyProfileController;
import com.esa.moviestar.profile.ProfileView;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.libraries.CredentialCryptManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;


public class AccountSettingController {
    @FXML
    private GridPane accountContentSetting;
    @FXML
    private Button modifyUserButton;
    @FXML
    private Button modifyPasswordButton;
    @FXML
    private Button deleteAccountButton;
    @FXML
    private Group profileImage;
    @FXML
    private Label userName;
    @FXML
    private Button deleteUserButton;
    @FXML
    private Label registrationDate;
    @FXML
    private Label Email;


    private User user;
    private AnchorPane container;
    private Account account;
    private Label wrongPassword;

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            int imageID = user.getIDIcon();
            profileImage.getChildren().clear();
            Group g = new Group(IconSVG.takeElement(imageID));
            profileImage.getChildren().add(g);
            userName.setText(user.getName());
            if (user.getRegistrationDate() != null) {
                registrationDate.setText(user.getRegistrationDate().toString());
            } else {
                registrationDate.setText("Not available");
            }
            Email.setText("Email : " + user.getEmail());
        }
    }

    public void setContainer(AnchorPane container) {
        this.container = container;
    }

    public void initialize() {
        modifyUser();
        deleteAccount();
        updatePassword();
        deleteUser();
        setPasswordWarning();
    }

    public void deleteUser() {
        deleteUserButton.setOnMouseClicked(event -> {
            DeletePopUp userPopUp = new DeletePopUp(false, account);

            AnchorPane.setBottomAnchor(userPopUp, 0.0);
            AnchorPane.setTopAnchor(userPopUp, 0.0);
            AnchorPane.setLeftAnchor(userPopUp, 0.0);
            AnchorPane.setRightAnchor(userPopUp, 0.0);

            container.getChildren().add(userPopUp);

            userPopUp.getDeleteButton().setOnMouseClicked(e -> {
                if (CredentialCryptManager.verifyPassword(userPopUp.getPasswordField().getText(), (account.getPassword()))) {
                    UserDao userDao = new UserDao();
                    userDao.deleteUser(user.getID());
                    if (userDao.countProfilesByEmail(account.getEmail()) > 0) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/profile-view.fxml"));
                            Parent profileView = loader.load();
                            ProfileView profileView1 = loader.getController();
                            profileView1.setAccount(account);

                            Scene currentScene = accountContentSetting.getScene();
                            Scene newScene = new Scene(profileView, currentScene.getWidth(), currentScene.getHeight());

                            Stage stage = (Stage) accountContentSetting.getScene().getWindow();
                            stage.setScene(newScene);

                        } catch (IOException m) {
                            System.err.println("AccountSettingController : Error returning to the profiles page " + m.getMessage());
                        }

                    } else if ((userDao.countProfilesByEmail(account.getEmail()) == 0)) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/create-profile-view.fxml"), Main.resourceBundle);
                            Parent createView = loader.load();
                            CreateProfileController createProfileController = loader.getController();
                            createProfileController.setAccount(account);

                            Scene currentScene = accountContentSetting.getScene();
                            Scene newScene = new Scene(createView, currentScene.getWidth(), currentScene.getHeight());

                            Stage stage = (Stage) accountContentSetting.getScene().getWindow();
                            stage.setScene(newScene);

                        } catch (IOException ex) {
                            System.err.println("AccountSettingController : Error returning to the profile creation page " + ex.getMessage());
                        }
                    }
                } else {
                    if (!userPopUp.getPasswordBox().getChildren().contains(wrongPassword)) {
                        userPopUp.getPasswordBox().getChildren().add(wrongPassword);
                    }
                    AnimationUtils.shake(wrongPassword);
                }
            });

            userPopUp.getCancelButton().setOnMouseClicked(event2 -> container.getChildren().remove(userPopUp));

        });
    }



    public void deleteAccount() {
        deleteAccountButton.setOnMouseClicked(event -> {
            DeletePopUp accountPopUp = new DeletePopUp(true, account);

            AnchorPane.setBottomAnchor(accountPopUp, 0.0);
            AnchorPane.setTopAnchor(accountPopUp, 0.0);
            AnchorPane.setLeftAnchor(accountPopUp, 0.0);
            AnchorPane.setRightAnchor(accountPopUp, 0.0);

            container.getChildren().add(accountPopUp);

            accountPopUp.getDeleteButton().setOnMouseClicked(e -> {
                if (CredentialCryptManager.verifyPassword(accountPopUp.getPasswordField().getText(), (account.getPassword()))) {
                    AccountDao accountDao = new AccountDao();
                    accountDao.deleteAccount(account.getEmail());
                    UserDao userDao = new UserDao();
                    userDao.deleteUserByEmail(account.getEmail());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"), Main.resourceBundle);
                        Parent accessContent = loader.load();

                        Scene currentScene = accountContentSetting.getScene();
                        Scene newScene = new Scene(accessContent, currentScene.getWidth(), currentScene.getHeight());

                        Stage stage = (Stage) accountContentSetting.getScene().getWindow();
                        stage.setScene(newScene);
                    } catch (IOException ex) {
                        System.err.println("AccountSettingController: Error loading the account login page: " + ex.getMessage());
                    }
                } else {
                    if (!accountPopUp.getPasswordBox().getChildren().contains(wrongPassword)) {
                        accountPopUp.getPasswordBox().getChildren().add(wrongPassword);
                    }
                    AnimationUtils.shake(wrongPassword);
                }
            });

            accountPopUp.getCancelButton().setOnMouseClicked(e -> {
                container.getChildren().remove(accountPopUp); // Rimuove il popup
            });
        });
    }


    public void modifyUser() {
        modifyUserButton.setOnMouseClicked(event -> {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/modify-profile-view.fxml"), Main.resourceBundle);
                Parent modifyContent = loader.load();
                ModifyProfileController modifyProfileController = loader.getController();
                modifyProfileController.setUser(user);
                modifyProfileController.setSource(ModifyProfileController.Origine.SETTINGS);
                modifyProfileController.setAccount(account);

                Scene currentScene = accountContentSetting.getScene();

                Scene newScene = new Scene(modifyContent, currentScene.getWidth(), currentScene.getHeight());

                Stage stage = (Stage) accountContentSetting.getScene().getWindow();
                stage.setScene(newScene);

            } catch (IOException e) {
                System.err.println("AccountSettingController: Error loading the user edit page" + e.getMessage());
            }
        });
    }

    public void updatePassword() {
        modifyPasswordButton.setOnMouseClicked(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/update-password-view.fxml"), Main.resourceBundle);
                Parent updateContent = loader.load();

                UpdatePasswordController updatePasswordController = loader.getController();
                updatePasswordController.setUser(user);
                updatePasswordController.setAccount(account);

                Scene currentScene = accountContentSetting.getScene();

                Scene newScene = new Scene(updateContent, currentScene.getWidth(), currentScene.getHeight());

                Stage stage = (Stage) accountContentSetting.getScene().getWindow();
                stage.setScene(newScene);

            } catch (IOException e) {
                System.err.println("AccountSettingController: Error loading the password update page" + e.getMessage());
            }
        });
    }


    private void setPasswordWarning() {
        wrongPassword = new Label();

        wrongPassword.setText("Wrong Password");

        wrongPassword.getStyleClass().addAll("warningText");

        VBox.setMargin(wrongPassword, new Insets(0, 0, 0, 40.0));
    }

}
