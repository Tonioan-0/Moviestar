package com.esa.moviestar.settings;

import com.esa.moviestar.home.MainPagesController;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Utente;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public class SettingsViewController {

    @FXML
    private StackPane contentArea;
    @FXML
    private AnchorPane contenitore;
    @FXML
    private HBox accountContent;
    @FXML
    private HBox cronologia;
    @FXML
    private HBox privacy;
    @FXML
    private HBox accessibilità;
    @FXML
    private HBox about;
    @FXML
    private StackPane backToHome;
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
        menuClick();
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
        // Collegamenti ai pulsanti
        accountContent.setOnMouseClicked(event -> caricaVista("/com/esa/moviestar/settings/account-setting-view.fxml"));
        cronologia.setOnMouseClicked(event -> caricaVista("/com/esa/moviestar/settings/cronologia-setting-view.fxml"));
        privacy.setOnMouseClicked(event -> caricaVista("/com/esa/moviestar/settings/privacy-setting-view.fxml"));
        accessibilità.setOnMouseClicked(event -> caricaVista("/com/esa/moviestar/settings/accessibilità-setting-view.fxml"));
        about.setOnMouseClicked(event -> caricaVista("/com/esa/moviestar/settings/about-setting-view.fxml"));
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

            if (loader.getController() instanceof CronologiaSettingController controller) {
                controller.setAccount(account);
                controller.setUtente(utente);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Errore nel caricamento della vista: " + percorsoFXML);
        }
    }
}