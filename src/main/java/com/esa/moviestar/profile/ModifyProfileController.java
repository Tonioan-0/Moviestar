package com.esa.moviestar.profile;

import java.io.IOException;
import com.esa.moviestar.Main;
import com.esa.moviestar.database.UserDao;
import com.esa.moviestar.model.User;
import com.esa.moviestar.settings.SettingsViewController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ModifyProfileController extends BaseProfileController {

    @FXML
    private VBox modifyPageContainer;

    private User user;
    private Origine source;

    public enum Origine {
        SETTINGS,
        PROFILE,
    }

    public void setUser(User user) {
        this.user = user;
        System.out.println("ModifyProfileView = user : " + user.getName() +
                " id user : " + user.getID() +
                " email user : " + user.getEmail());

        if (user != null) {
            textName.setText(user.getName());
            codCurrentImage = user.getIDIcona();

            // Mostra l'icona corrente
            defaultImagine.getChildren().clear();
            Group g = new Group(IconSVG.takeElement(codCurrentImage));
            defaultImagine.getChildren().add(g);
        }
    }

    public void setSource(Origine source) {
        this.source = source;
    }

    @Override
    protected String getTitleText() {
        return "Edit your username:";
    }

    @Override
    protected int getInitialImageCode() {
        int idIcona;
        if (user != null) {
            idIcona = user.getIDIcona();
        } else {
            idIcona = 0;
        }
        return idIcona;
    }

    @Override
    protected void onInitializeComplete() {
    }

    @Override
    protected void handleCancel() {
        textName.setText("");
        navigateToDestination();
    }

    @Override
    protected boolean performSave(String name, int imageCode) {
        return saveChanges(name, imageCode);
    }

    @Override
    protected void onSaveSuccess() {
        navigateToDestination();
    }

    private boolean saveChanges(String name, int image) {
        user.setName(name);
        user.setIcona(image);

        UserDao dao = new UserDao();
        boolean success = dao.updateUser(user);

        if (!success) {
            System.out.println("Error saving changes.");
        }
        return success;
    }

    private void navigateToDestination() {
        try {
            if (source == Origine.PROFILE) {
                loadProfilesPage();
            } else if (source == Origine.SETTINGS) {
                loadSettingsPage();
            } else {
                System.err.println("Unrecognized source:" + source);
            }
        } catch (IOException e) {
            System.err.println("ModifyController: The page could not be loaded. " + e.getMessage());
        }
    }

    private void loadProfilesPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/profile-view.fxml"));
        Parent profile = loader.load();

        ProfileView profileView = loader.getController();
        profileView.setAccount(account);

        Scene currentScene = modifyPageContainer.getScene();
        Scene newScene = new Scene(profile, currentScene.getWidth(), currentScene.getHeight());
        Stage stage = (Stage) currentScene.getWindow();
        stage.setScene(newScene);
    }

    private void loadSettingsPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/settings-view.fxml"), Main.resourceBundle);
        Parent settings = loader.load();

        SettingsViewController settingsViewController = loader.getController();
        settingsViewController.setUtente(user);
        settingsViewController.setAccount(account);

        Scene currentScene = modifyPageContainer.getScene();
        Scene newScene = new Scene(settings, currentScene.getWidth(), currentScene.getHeight());
        Stage stage = (Stage) currentScene.getWindow();
        stage.setScene(newScene);
    }
}