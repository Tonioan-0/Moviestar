package com.esa.moviestar.settings;

import com.esa.moviestar.Main;
import com.esa.moviestar.database.UserDao;
import com.esa.moviestar.home.MainPagesController;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.User;
import com.esa.moviestar.movie_view.FilmCardController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsViewController {

    @FXML
    private StackPane contentArea;
    @FXML
    private AnchorPane container;
    @FXML
    private HBox userContent;
    @FXML
    private HBox historyContent;
    @FXML
    private HBox privacyContent;
    @FXML
    private HBox favouritesContent;
    @FXML
    private HBox watchListContent;
    @FXML
    private HBox aboutContent;
    @FXML
    private StackPane backToHome;
    @FXML
    private HBox githubIcon;

    private User user;
    private Account account;

    public void setAccount(Account account) {
        this.account = account;
        loadView("/com/esa/moviestar/settings/account-setting-view.fxml");
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static final String PATH_CARD_VERTICAL = "/com/esa/moviestar/movie_view/FilmCard_Vertical.fxml";


    public void initialize() {
        backToHome();
        highlightMenu(userContent);
        menuClick();
        goToGithubPage();
    }

    private void backToHome() {
        // go back to home logic
        backToHome.setOnMouseClicked(event -> {
            if (account == null) {
                System.err.println("Account is NULL, unable to navigate to home");
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/home/main.fxml"), Main.resourceBundle);
                Parent backHomeView = loader.load();

                MainPagesController mainPagesController = loader.getController();
                mainPagesController.load(user, account);

                Scene currentScene = container.getScene();
                Scene newScene = new Scene(backHomeView, currentScene.getWidth(), currentScene.getHeight());

                Stage stage = (Stage) currentScene.getWindow();
                stage.setScene(newScene);
            } catch (IOException e) {
                System.err.println("SettingsViewController : Error returning to home" + e.getMessage());
            }
        });
    }

    private void menuClick() {
        userContent.setOnMouseClicked(event -> {
            highlightMenu(userContent);
            loadView("/com/esa/moviestar/settings/account-setting-view.fxml");
        });

        historyContent.setOnMouseClicked(event -> {
            highlightMenu(historyContent);
            loadView("/com/esa/moviestar/settings/history-setting-view.fxml");
        });

        privacyContent.setOnMouseClicked(event -> {
            highlightMenu(privacyContent);
            loadView("/com/esa/moviestar/settings/privacy-setting-view.fxml");
        });

        aboutContent.setOnMouseClicked(event -> {
            highlightMenu(aboutContent);
            loadView("/com/esa/moviestar/settings/about-setting-view.fxml");
        });

        watchListContent.setOnMouseClicked(event -> {
            highlightMenu(watchListContent);
            loadView("/com/esa/moviestar/settings/watchlist-setting-view.fxml");
        });

        favouritesContent.setOnMouseClicked(event -> {
            highlightMenu(favouritesContent);
            loadView("/com/esa/moviestar/settings/favourite-setting-view.fxml");
        });
    }

    private void highlightMenu(HBox selectedMenu) {
        // remove the selected class
        userContent.getStyleClass().remove("menu-button-selected");
        historyContent.getStyleClass().remove("menu-button-selected");
        privacyContent.getStyleClass().remove("menu-button-selected");
        aboutContent.getStyleClass().remove("menu-button-selected");
        watchListContent.getStyleClass().remove("menu-button-selected");
        favouritesContent.getStyleClass().remove("menu-button-selected");


        //Adds the selected class to the clicked one
        if (!selectedMenu.getStyleClass().contains("menu-button-selected")) {
            selectedMenu.getStyleClass().add("menu-button-selected");
        }
    }

    private void loadView(String pathFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(pathFXML), Main.resourceBundle);
            Parent view = loader.load();


            if (loader.getController() instanceof AccountSettingController controller) {
                controller.setAccount(account);
                controller.setUser(user);
                controller.setContainer(container);
            }

            if (loader.getController() instanceof HistorySettingController controller) {
                controller.setScene(this);
                controller.updateHistory(user);
            }

            if (loader.getController() instanceof WatchListController controller) {
                controller.setScene(this);
                controller.updateWatchList(user);
            }
            if (loader.getController() instanceof FavouriteSettingController controller) {
                controller.setScene(this);
                controller.updateFavourite(user);


            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading view: " + pathFXML);
        }
    }

    private void goToGithubPage() {
        githubIcon.setOnMouseClicked(event -> {
            try {
                URI uri = new URI("https://github.com/Tonioan-0/Moviestar");
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(uri);
                } else {
                    System.err.println("SettingsViewController: Can't open url");
                }
            } catch (Exception ex) {
                System.err.println("SettingsViewController: Error loading github web page"+ex.getMessage());
            }
        });
    }

    public java.util.List<Node> createFilmNodes(java.util.List<Content> contentList ,Object obt) throws IOException {
        List<Node> nodes = new ArrayList<>(contentList.size());
        String cardPath = PATH_CARD_VERTICAL;

        for (Content content : contentList) {
            if (content == null || content.getId() == 0) continue;

            FXMLLoader fxmlLoader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(cardPath), "FXML resource not found: " + cardPath),
                    Main.resourceBundle
            );
            StackPane nodeContainer = new StackPane();
            Node node = fxmlLoader.load();
            FilmCardController filmCardController = fxmlLoader.getController();
            nodeContainer.getChildren().add(node);

            HBox deleteButton = new HBox();
            deleteButton.getChildren().add(new SVGPath(){{setContent(Main.resourceBundle.getString("bin"));getStyleClass().add("on-primary");}});
            deleteButton.setMinSize(48,48);
            deleteButton.setMaxSize(48,48);
            StackPane.setMargin(deleteButton,new Insets(8));
            deleteButton.setAlignment(Pos.CENTER);
            deleteButton.getStyleClass().addAll("small-item","primary");

            StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);


            nodeContainer.getChildren().add(deleteButton);
            deleteButton.setOnMouseClicked(event -> {
                UserDao dao  = new UserDao();
                if(obt instanceof HistorySettingController){
                    dao.deleteHistory(user.getID(), content.getId());
                    ((HistorySettingController)obt).updateHistory(user);
                }

                else if (obt instanceof WatchListController) {
                    dao.deleteWatchlist(user.getID(), content.getId());
                    ((WatchListController)obt).updateWatchList(user);

                }

                else if (obt instanceof FavouriteSettingController){
                    dao.deleteFavourite(user.getID(), content.getId());
                    ((FavouriteSettingController)obt).updateFavourite(user);

                }

            });

            if (filmCardController != null) {
                filmCardController.setContent(content, true);
                node.setOnMouseClicked(e -> homePage(user , content.getId(), content.isSeries()));
                nodes.add(nodeContainer);
            } else {
                System.err.println("SettingsViewController: FilmCardController is null for " + cardPath);
            }
        }

        return nodes;
    }

    private void homePage(User user,int content,boolean series) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/home/main.fxml"),Main.resourceBundle);
            Parent homeContent = loader.load();

            MainPagesController mainPagesController = loader.getController();
            mainPagesController.loadForShowingFilmScene(user,account,content,series);
            Scene currentScene = container.getScene();
            Scene newScene = new Scene(homeContent, currentScene.getWidth(), currentScene.getHeight());
            Stage stage = (Stage) container.getScene().getWindow();
            stage.setScene(newScene);

        }catch(IOException e){
            System.err.println("SettingsViewController: Error to load the home page"+e.getMessage());
        }
    }


}