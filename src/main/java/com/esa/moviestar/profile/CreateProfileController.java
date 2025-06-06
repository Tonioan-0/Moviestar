package com.esa.moviestar.profile;

import java.io.IOException;
import java.time.LocalDate;
import com.esa.moviestar.Main;
import com.esa.moviestar.database.UserDao;
import com.esa.moviestar.home.MainPagesController;
import com.esa.moviestar.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CreateProfileController extends BaseProfileController {

    @FXML
    private VBox createPageContainer;

    private User user;
    private UserDao dao;
    private Origine source;

    public enum Origine {
        HOME,
        PROFILE,
        REGISTER
    }

    public void setUser(User user) {
        this.user = user;
        System.out.println("CreateProfileController = user : " + user.getName());
    }

    public void setSource(Origine source) {
        this.source = source;
    }

    @Override
    protected String getTitleText() {
        return "Create Username:";
    }

    @Override
    protected int getInitialImageCode() {
        return 0;
    }

    @Override
    protected void onInitializeComplete() {
        dao = new UserDao();
    }

    @Override
    protected void handleCancel() {
        if (source == Origine.HOME) {
            goToHome();
        } else if (source == Origine.PROFILE) {
            backToProfiles();
        } else if (source == Origine.REGISTER || dao.countProfilesByEmail(account.getEmail()) == 0) {
            textName.setText("");
        }
    }

    @Override
    protected boolean performSave(String name, int imageCode) {
        User newUser = createUser(name, imageCode);
        return saveUser(newUser);
    }

    @Override
    protected void onSaveSuccess() {
        backToProfiles();
    }

    private User createUser(String name, int image) {
        LocalDate date = LocalDate.now();
        return new User(name, image, account.getEmail(), date);
    }

    private boolean saveUser(User user) {
        try {
            UserDao userDao= new UserDao();
            userDao.insertUser(user);
            return true;
        } catch (Exception e) {
            System.err.println("Profile save error:" + e.getMessage());
            return false;
        }
    }

    private void backToProfiles() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/profile-view.fxml"));
            Parent profileContent = loader.load();

            ProfileView profileView = loader.getController();
            profileView.setAccount(account);

            Scene currentScene = createPageContainer.getScene();
            Scene newScene = new Scene(profileContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) createPageContainer.getScene().getWindow();
            stage.setScene(newScene);
        } catch (IOException ex) {
            System.out.println("CreateProfileController : Couldn't load profile view ." + ex.getMessage());
            warningText.setText("Error loading the page. " + ex.getMessage());
        }
    }

    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/home/main.fxml"), Main.resourceBundle);
            Parent homeContent = loader.load();

            MainPagesController mainPagesController = loader.getController();
            mainPagesController.first_load(user, account);

            Scene currentScene = createPageContainer.getScene();
            Scene newScene = new Scene(homeContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) createPageContainer.getScene().getWindow();
            stage.setScene(newScene);

        } catch (IOException e) {
            System.err.println("CreateProfileController : Couldn't return to home screen." + e.getMessage());
        }
    }
}