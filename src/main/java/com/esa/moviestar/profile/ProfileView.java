package com.esa.moviestar.profile;

import com.esa.moviestar.Main;
import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.database.UserDao;
import com.esa.moviestar.home.MainPagesController;
import com.esa.moviestar.libraries.TMDbApiManager;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.User;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ProfileView {

    // FXML Components
    @FXML
    Label text;

    @FXML
    HBox grid;

    @FXML
    StackPane fatherContainer;

    @FXML
    Label warningText;


    private Account account;

    private static final int GRID_SPACING = 40;
    private static final int BOX_USER_SPACING = 10;
    private static final int BOX_USER_PADDING = 10;
    private static final double SVG_SIZE = 8;
    private static final double SVG_SIZE_HOVER = 8.5;
    private static final double PENCIL_SIZE = 0.5;
    private static final double PENCIL_HOVER = -2.5;
    private static final int PENCIL_POS = 0;
    private static final double CROSS_SIZE = 1.8 ;
    private static final double CROSS_CONTAINER_SIZE = 190;
    private static final double CROSS_CONTAINER_SIZE_HOVER = 200;




    public void initialize() {
        text.setText("Who wants to watch Moviestar?");
        grid.setSpacing(GRID_SPACING);

        Task<Void> updateDbTask = new Task<>() {
            @Override
            protected Void call() {updateMessage("Starting database content update...");
                System.out.println("ProfileView Task: Attempting to update all content in database.");

                TMDbApiManager tmdbApiManager = TMDbApiManager.getInstance();
                TMDbApiManager.getInstance().setContentDao(new ContentDao());
                try {
                    tmdbApiManager.updateAllContentInDatabase().join();
                } catch (Exception e) {
                    System.err.println("ProfileView Task: Exception during TMDb content update: " + e.getMessage());
                }
                return null;
            }
        };
        updateDbTask.setOnSucceeded(event -> System.out.println("ProfileView: Database update task succeeded."));
        updateDbTask.setOnFailed(event -> System.err.println("ProfileView: Database update task failed."));

        Thread taskThread = new Thread(updateDbTask);
        taskThread.setDaemon(true);
        taskThread.start();

    }

    public void setAccount(Account account) {
        this.account = account;
        System.out.println("ProfileView : email "+account.getEmail());
        loadUser();
    }


    private void loadUser() {
        grid.getChildren().clear();
        UserDao dao = new UserDao();
        List<User> users = dao.findAllUsers(account.getEmail());

        for (User u : users) {
            VBox userBox = createUserBox(u);
            grid.getChildren().add(userBox);
        }


        if (users.size() < 4) {
            VBox addUserBox = createAddUserBox();
            grid.getChildren().add(addUserBox);
        }
    }


    private VBox createUserBox(User user) {
        VBox box = new VBox();
        box.setSpacing(BOX_USER_SPACING);
        box.setPadding(new Insets(BOX_USER_PADDING));
        box.setAlignment(Pos.CENTER);

        Label name = new Label(user.getName());
        name.getStyleClass().addAll("on-primary", "bold-text", "large-text");

        Group icon = new Group(IconSVG.takeElement(user.getIDIcon()));
        icon.setScaleY(SVG_SIZE);
        icon.setScaleX(SVG_SIZE);

        StackPane iconBox = new StackPane(icon);
        StackPane.setAlignment(icon, Pos.CENTER);
        iconBox.setMinSize(204, 204);

        StackPane edit = createEditButton();

        // Event Handlers
        setupUserBoxEvents(box, icon, name);
        icon.setOnMouseClicked(e -> homePage(user));
        edit.setOnMouseClicked(e -> editPage(user));

        box.getChildren().addAll(iconBox, name, edit);
        return box;
    }

    private void setupUserBoxEvents(VBox box, Group icon, Label name) {
        box.setOnMouseEntered(event -> {
            icon.setScaleX(SVG_SIZE_HOVER);
            icon.setScaleY(SVG_SIZE_HOVER);
        });

        box.setOnMouseExited(event -> {
            icon.setScaleX(SVG_SIZE);
            icon.setScaleY(SVG_SIZE);
        });
    }

    private StackPane createEditButton() {
        StackPane editContainer = new StackPane();
        editContainer.setPrefWidth(100);

        SVGPath pencilModify = new SVGPath();
        pencilModify.setContent(Main.resourceBundle.getString("pencil"));
        pencilModify.setScaleY(PENCIL_SIZE);
        pencilModify.setScaleX(PENCIL_SIZE);
        pencilModify.setStyle("-fx-fill: #E6E3DC;");

        editContainer.getChildren().add(pencilModify);

        editContainer.setOnMouseEntered(event -> pencilModify.setTranslateY(PENCIL_HOVER));

        editContainer.setOnMouseExited(event -> pencilModify.setTranslateY(PENCIL_POS));
        return editContainer;
    }

    private VBox createAddUserBox() {
        StackPane crossContainer = getCrossContainerStyle();

        SVGPath cross = new SVGPath();
        cross.setContent(Main.resourceBundle.getString("plusButton"));
        cross.setScaleX(CROSS_SIZE);
        cross.setScaleY(CROSS_SIZE);
        cross.setStyle("-fx-fill: #F0ECFD;");


        crossContainer.getChildren().add(cross);

        VBox creationContainer = new VBox();
        Label plusText = new Label();
        plusText.setText("Add");
        plusText.setTranslateY(-18);
        plusText.getStyleClass().addAll("on-primary", "bold-text", "large-text");
        creationContainer.getChildren().addAll(crossContainer,plusText);
        creationContainer.setOnMouseEntered(event -> {
            cross.setScaleX(SVG_SIZE_HOVER);
            cross.setScaleY(SVG_SIZE_HOVER);
        });

        creationContainer.setOnMouseExited(event -> {
            cross.setScaleX(SVG_SIZE);
            cross.setScaleY(SVG_SIZE);
        });

        creationContainer.setSpacing(20);
        creationContainer.setAlignment(Pos.CENTER);

        creationContainer.setOnMouseEntered(event -> cross.setStyle("-fx-fill: #7341BE;"));

        creationContainer.setOnMouseExited(event -> cross.setStyle("-fx-fill: #F0ECFD;"));

        creationContainer.setOnMouseClicked(e -> userCreationPage());

        return creationContainer;
    }


    private static StackPane getCrossContainerStyle() {
        StackPane crossContainer = new StackPane();
        crossContainer.setMinSize(CROSS_CONTAINER_SIZE,CROSS_CONTAINER_SIZE);
        crossContainer.setTranslateY(-20);
        crossContainer.setStyle("-fx-background-color: #333333;" +
                "-fx-background-radius: 48px;" +
                "-fx-border-radius: 48px;");

        crossContainer.setOnMouseExited(event ->
                crossContainer.setMinSize(CROSS_CONTAINER_SIZE,CROSS_CONTAINER_SIZE)
        );

        crossContainer.setOnMouseEntered(event ->
                crossContainer.setMinSize(CROSS_CONTAINER_SIZE_HOVER,CROSS_CONTAINER_SIZE_HOVER)
        );
        return crossContainer;
    }



    //transition to the Home page
    private void homePage(User user) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/home/main.fxml"),Main.resourceBundle);
            Parent homeContent = loader.load();

            MainPagesController mainPagesController = loader.getController();
            mainPagesController.first_load(user,account);
            Scene currentScene = fatherContainer.getScene();
            Scene newScene = new Scene(homeContent, currentScene.getWidth(), currentScene.getHeight());
            Stage stage = (Stage) fatherContainer.getScene().getWindow();
            stage.setScene(newScene);

        }catch(IOException e){
            System.err.println("ProfileView: Error to load the home page"+e.getMessage());
        }
    }



    // Switch to edit page
    private void editPage(User user ) {
        if (grid.getChildren().size() > 1) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/modify-profile-view.fxml"),Main.resourceBundle);
                Parent modifyContent = loader.load();

                ModifyProfileController modifyProfileController = loader.getController();
                modifyProfileController.setAccount(account);
                modifyProfileController.setUser(user);
                modifyProfileController.setSource(ModifyProfileController.Origine.PROFILE);

                Scene currentScene = fatherContainer.getScene();
                Scene newScene = new Scene(modifyContent, currentScene.getWidth(), currentScene.getHeight());
                Stage stage = (Stage) fatherContainer.getScene().getWindow();
                stage.setScene(newScene);

            } catch (IOException e) {
                System.err.println("ProfileView : Error loading the edit page:"+e.getMessage());
            }
        } else {
            warningText.setText("No item has been selected for editing.");
        }
    }

    //  Switch to user creation page
    private void userCreationPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/create-profile-view.fxml"),Main.resourceBundle);
            Parent createContent = loader.load();
            CreateProfileController createProfileController = loader.getController();

            createProfileController.setAccount(account);
            createProfileController.setSource(CreateProfileController.Source.PROFILE);

            Scene currentScene = fatherContainer.getScene();
            Scene newScene = new Scene(createContent, currentScene.getWidth(), currentScene.getHeight());
            Stage stage = (Stage) fatherContainer.getScene().getWindow();
            stage.setScene(newScene);

        } catch (IOException e) {
            System.err.println("ProfileView : Error loading creation page."+e.getMessage());
        }
    }
}