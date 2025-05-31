package com.esa.moviestar.settings;

import com.esa.moviestar.home.MainPagesController;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Utente;
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
import java.util.ResourceBundle;

public class SettingsViewController {

    @FXML
    private StackPane contentArea;
    @FXML
    private AnchorPane contenitore;
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
    private HBox privacy;
    @FXML
    private HBox watchList;
    @FXML
    private HBox about;
    @FXML
    private StackPane backToHome;
    @FXML
    private HBox githubIcon;
    @FXML
    private Label userName;
    @FXML
    private Group profileImage;

    private Utente utente;
    private Account account;
    public void setAccount(Account account){
        this.account=account;
        caricaVista("/com/esa/moviestar/settings/account-setting-view.fxml");
        System.out.println("SettingsViewController: email "+account.getEmail());
    }

    public void setUtente(Utente utente){
        this.utente=utente;
        System.out.println("SettingsViewController = utente : "+utente.getNome()+" email dell'utente : "+utente.getEmail()+" id utente : "+utente.getID());
    }

    public final ResourceBundle resourceBundle = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");

    public void initialize() {
        backToHome();
        evidenziaMenu(userContent);
        menuClick();
        goToGithubPage();
    }

    private void backToHome() {
        // Gestione ritorno alla home
        backToHome.setOnMouseClicked(event -> {
            // Controllo sicurezza per dati NULL
            if (account == null) {
                System.err.println("Account è NULL, impossibile navigare alla home");
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/home/main.fxml"), resourceBundle);
                Parent backHomeView = loader.load();

                MainPagesController mainPagesController = loader.getController();
                mainPagesController.first_load(utente, account);

                Scene currentScene = contenitore.getScene();
                Scene newScene = new Scene(backHomeView, currentScene.getWidth(), currentScene.getHeight());

                Stage stage = (Stage) currentScene.getWindow();
                stage.setScene(newScene);
            } catch (IOException e) {
                System.err.println("SettingsViewController : Errore nel tornare alla home"+e.getMessage());
            }
        });
    }

    private void menuClick() {
        userContent.setOnMouseClicked(event -> {
            evidenziaMenu(userContent);
            caricaVista("/com/esa/moviestar/settings/account-setting-view.fxml");
        });

        historyContent.setOnMouseClicked(event -> {
            evidenziaMenu(historyContent);
            caricaVista("/com/esa/moviestar/settings/history-setting-view.fxml");
        });

        privacy.setOnMouseClicked(event -> {
            evidenziaMenu(privacy);
            caricaVista("/com/esa/moviestar/settings/privacy-setting-view.fxml");
        });

        about.setOnMouseClicked(event -> {
            evidenziaMenu(about);
            caricaVista("/com/esa/moviestar/settings/about-setting-view.fxml");
        });

        watchList.setOnMouseClicked(event -> {
            evidenziaMenu(watchList);
            caricaVista("/com/esa/moviestar/settings/watchlist-setting-view.fxml");
        });
    }

    private void evidenziaMenu(HBox selezionato) {
        // Rimuove la classe selezionata da tutti
        userContent.getStyleClass().remove("menu-button-selected");
        historyContent.getStyleClass().remove("menu-button-selected");
        privacy.getStyleClass().remove("menu-button-selected");
        about.getStyleClass().remove("menu-button-selected");
        watchList.getStyleClass().remove("menu-button-selected");

        // Aggiunge la classe selezionata a quello cliccato
        if (!selezionato.getStyleClass().contains("menu-button-selected")) {
            selezionato.getStyleClass().add("menu-button-selected");
        }
    }

    private void caricaVista(String percorsoFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(percorsoFXML), resourceBundle);
            Parent view = loader.load();

            // Passa l'utente solo alla vista account
            if (loader.getController() instanceof AccountSettingController controller) {
                controller.setAccount(account);
                controller.setUtente(utente);
                controller.setContenitore(contenitore);
            }

            if (loader.getController() instanceof HistorySettingController controller) {
                controller.setAccount(account);
                controller.setUtente(utente);
            }

            if (loader.getController() instanceof WatchListController controller) {
                controller.setAccount(account);
                controller.setUtente(utente);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della vista: " + percorsoFXML);
        }
    }

    private void goToGithubPage(){
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