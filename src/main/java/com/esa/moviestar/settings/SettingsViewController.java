package com.esa.moviestar.settings;

import com.esa.moviestar.Main;
import com.esa.moviestar.home.MainPagesController;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class SettingsViewController {

    @FXML
    private StackPane contentArea;
    @FXML
    private AnchorPane container;
    @FXML
    private HBox userContent;
    @FXML
    private Label userText;
    @FXML
    private SVGPath userSVG;
    @FXML
    private HBox historyContent;
    @FXML
    private SVGPath historySVG;
    @FXML
    private Label historyText;
    @FXML
    private HBox privacyContent;
    @FXML
    private HBox watchListContent;
    @FXML
    private HBox aboutContent;
    @FXML
    private StackPane backToHome;
    @FXML
    private HBox githubIcon;
    @FXML
    private Label userName;
    @FXML
    private Group profileImage;

    private User user;
    private Account account;

    public void setAccount(Account account) {
        this.account = account;
        loadView("/com/esa/moviestar/settings/account-setting-view.fxml");
        System.out.println("SettingsViewController: email " + account.getEmail());
    }

    public void setUtente(User user) {
        this.user = user;
        System.out.println("SettingsViewController = user : " + user.getName() + " email user : " + user.getEmail() + " id user : " + user.getID());
    }


    public void initialize() {
        backToHome();
        highlightMenu(userContent);
        menuClick();
        goToGithubPage();
    }

    private void backToHome() {
        // Gestione ritorno alla home
        backToHome.setOnMouseClicked(event -> {
            // Controllo sicurezza per dati NULL
            if (account == null) {
                System.err.println("Account is NULL, unable to navigate to home");
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/home/main.fxml"), Main.resourceBundle);
                Parent backHomeView = loader.load();

                MainPagesController mainPagesController = loader.getController();
                mainPagesController.first_load(user, account);

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
    }

    private void highlightMenu(HBox selectedMenu) {
        // Rimuove la classe selezionata da tutti
        userContent.getStyleClass().remove("menu-button-selected");
        historyContent.getStyleClass().remove("menu-button-selected");
        privacyContent.getStyleClass().remove("menu-button-selected");
        aboutContent.getStyleClass().remove("menu-button-selected");
        watchListContent.getStyleClass().remove("menu-button-selected");

        // Aggiunge la classe selezionata a quello cliccato
        if (!selectedMenu.getStyleClass().contains("menu-button-selected")) {
            selectedMenu.getStyleClass().add("menu-button-selected");
        }
    }

    private void loadView(String pathFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(pathFXML), Main.resourceBundle);
            Parent view = loader.load();

            // Passa l'user solo alla vista account
            if (loader.getController() instanceof AccountSettingController controller) {
                controller.setAccount(account);
                controller.setUtente(user);
                controller.setContainer(container);
            }

            if (loader.getController() instanceof HistorySettingController controller) {
                controller.setAccount(account);
                controller.setUtente(user);
            }

            if (loader.getController() instanceof WatchListController controller) {
                controller.setAccount(account);
                controller.setUtente(user);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della vista: " + pathFXML);
        }
    }

    private void goToGithubPage() {
        githubIcon.setOnMouseClicked(event -> {
            try {
                URI uri = new URI("https://github.com/Tonioan-0/Moviestar");
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(uri);
                } else {
                    System.err.println("Apertura browser non supportata");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


}