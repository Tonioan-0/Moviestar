package com.esa.moviestar.profile;

import java.io.IOException;
import java.util.ResourceBundle;

import com.esa.moviestar.database.UtenteDao;
import com.esa.moviestar.login.AnimationUtils;
import com.esa.moviestar.settings.SettingsViewController;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class ModifyProfileController {
    @FXML
    GridPane pageContainer;
    @FXML
    VBox ContenitorePaginaModifica;
    @FXML
    VBox elementContainer;
    @FXML
    Group defaultImagine;
    @FXML
    Label creationTitle;
    @FXML
    TextField textName;
    @FXML
    HBox imageScroll1;
    @FXML
    HBox imageScroll2;
    @FXML
    HBox imageScroll3;
    @FXML
    HBox imageScroll4;
    @FXML
    VBox scrollContainer;
    @FXML
    Button saveButton;
    @FXML
    Button cancelButton;
    @FXML
    Label warningText;
    @FXML
    private Label errorText;
    @FXML
    private VBox imageContainer;

    private Group originalProfileImage;
    private int codImmagineCorrente;
    private Account account;
    private Utente utente;
    public void setAccount(Account account) {
        this.account = account;
        System.out.println("ModifyProfileView : email "+account.getEmail());
    }
    public void setUtente(Utente utente){
        this.utente=utente;
        System.out.println("ModifyProfileView = utente : "+utente.getNome()+" id utente : "+utente.getID()+" email dell'utente : "+utente.getEmail());
        if (utente != null) { // da qui in poi serve per portarsi dietro le immagini e il nome da modificare
            textName.setText(utente.getNome());
            codImmagineCorrente = utente.getIDIcona();

            // Mostra l'icona corrente
            defaultImagine.getChildren().clear();
            Group g = new Group(IconSVG.takeElement(codImmagineCorrente));
            defaultImagine.getChildren().add(g);
        }
    }

    public enum Origine {
        SETTINGS,
        PROFILI,
    }

    private Origine origine;

    public void setOrigine(Origine origine) {
        this.origine = origine;
    }
    public final ResourceBundle resourceBundle = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");

    public void initialize() {

// DA MODIFICARE IN MODO CHE FACCIA LE MODIFICHE
        errorText.setText("");

        codImmagineCorrente=0;
        defaultImagine.getChildren().add(IconSVG.takeElement(codImmagineCorrente));
        defaultImagine.setScaleX(10);
        defaultImagine.setScaleY(10);

        creationTitle.setText("Edit your username:"); //label per sopra il textfield per farci capire che stiamo creando un nuovo utente

        textName.setPromptText("Name");// text field dove inserire il nome, (con all'interno trasparente la scritta "inserisci nome")

        //metto lo stile per ogni scroll di immagini

        scrollContainer.setSpacing(90);

        saveButton.setText("Save"); //setting del bottone di salvataggio

        cancelButton.setText("Cancel"); //setting del bottone di annullamento

        cancelButton.setOnMouseClicked(eventCancel -> {
            textName.setText("");
            try {
                FXMLLoader loader2;
                FXMLLoader loader3;
                if (origine == Origine.PROFILI) {
                    loader2 = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/profile-view.fxml"));
                    Parent profile = loader2.load();

                    ProfileView profileView = loader2.getController();
                    profileView.setAccount(account);

                    Scene currentScene = ContenitorePaginaModifica.getScene();
                    Scene newScene = new Scene(profile, currentScene.getWidth(), currentScene.getHeight());
                    Stage stage = (Stage) currentScene.getWindow();
                    stage.setScene(newScene);
                }else if (origine == Origine.SETTINGS) {
                loader3 = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/settings-view.fxml"),resourceBundle);
                Parent settings = loader3.load();

                SettingsViewController settingsViewController = loader3.getController();
                settingsViewController.setUtente(utente);
                settingsViewController.setAccount(account);

                Scene currentScene = ContenitorePaginaModifica.getScene();
                Scene newScene = new Scene(settings, currentScene.getWidth(), currentScene.getHeight());
                Stage stage = (Stage) currentScene.getWindow();
                stage.setScene(newScene);
            }
                else {
                    System.err.println("Origine non riconosciuta: " + origine);
                }

            } catch (IOException e) {
                System.err.println("ModifyController: non è possibile caricare la pagina " + e.getMessage());
            }
        });


        saveButton.setOnMouseClicked(eventSave -> {  //Se clicco sul bottone di salvataggio / dovrà poi ritornare alla pagina di scelta dei profili con il profilo creato
            String name = textName.getText();
            String gusto = "0";
            int immagine=codImmagineCorrente;

            if(name==null){
                errorText.setText("No name entered");
                AnimationUtils.shake(errorText);
                return;
            }
            utente.setNome(name);
            utente.setIcona(immagine);

            UtenteDao dao = new UtenteDao();
            boolean successo = dao.aggiornaUtente(utente); // chiamata al database per fare l'update

            if(!successo){
                System.out.println("errore nel salvataggio delle modifiche");
            }

            if (!textName.getText().isEmpty()) {  //se ho messo un nome nel textfield e l'ho salvato allora ritorno alla pagina principale dei profili / oppure potrei far direttamente loggare / (modifiche da fare : controllare che abbia scelto anche un immagine, oppure se non l'ha scelta dare quella di default)
                //questo metodo cosi fa ritornare alla pagina dei profili, aggiungere poi il fatto che io abbia creato il panel nuovo con tutte le modifiche
                try {
                    FXMLLoader loader2;
                    FXMLLoader loader3;
                    if (origine == Origine.PROFILI) {
                        loader2 = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/profile-view.fxml"));
                        Parent profile = loader2.load();

                        ProfileView profileView = loader2.getController();
                        profileView.setAccount(account);

                        Scene currentScene = ContenitorePaginaModifica.getScene();
                        Scene newScene = new Scene(profile, currentScene.getWidth(), currentScene.getHeight());
                        Stage stage = (Stage) currentScene.getWindow();
                        stage.setScene(newScene);
                    }
                    else if (origine == Origine.SETTINGS) {
                        loader3 = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/settings-view.fxml"),resourceBundle);
                        Parent settings = loader3.load();

                        SettingsViewController settingsViewController = loader3.getController();
                        settingsViewController.setUtente(utente);
                        settingsViewController.setAccount(account);

                        Scene currentScene = ContenitorePaginaModifica.getScene();
                        Scene newScene = new Scene(settings, currentScene.getWidth(), currentScene.getHeight());
                        Stage stage = (Stage) currentScene.getWindow();
                        stage.setScene(newScene);
                    }
                    else {
                        System.err.println("Origine non riconosciuta: " + origine);
                    }

                } catch (IOException e) {
                    System.err.println("ModifyController: non è possibile caricare la pagina " + e.getMessage());
                }
                System.out.println("Ritorni alla pagina dei profili");
            } else {
                errorText.setText("No name entered"); //se non ho inserito nessun nome mi da errore perchè per forza va settato un nome , oppure potrei dare il nome di default tipo utente 1
                AnimationUtils.shake(errorText);
            }


        });


        for (int i = 0; i <= 16; i++) {  // Aggiungi le 16 icone (da 1 a 16)
            Group g = new Group();
            g.setScaleX(3.8);
            g.setScaleY(3.8);

            // Usa IconSVG.takeElement(i + 1) per ottenere tutte le icone
            g.getChildren().add(IconSVG.takeElement(i));  // Aggiungi l'elemento SVG al gruppo g

            // Aggiungi il gruppo all'HBox, distribuendo le icone tra imageScroll1, imageScroll2, imageScroll3
            if (i <= 3) {
                imageScroll1.getChildren().add(g);  // Aggiungi il gruppo a imageScroll1
            } else if (i <= 7) {
                imageScroll2.getChildren().add(g);  // Aggiungi il gruppo a imageScroll2
            } else if (i<=11){
                imageScroll3.getChildren().add(g);  // Aggiungi il gruppo a imageScroll3
            }else if (i<=15) {
                imageScroll4.getChildren().add(g);
            }
        }
        imageScroll1.setSpacing(120);
        imageScroll2.setSpacing(120);
        imageScroll3.setSpacing(120);
        imageScroll4.setSpacing(120);
        // Inizializza tutti gli HBox di immagini
        setupImageProfile(imageScroll1);
        setupImageProfile(imageScroll2);
        setupImageProfile(imageScroll3);
        setupImageProfile(imageScroll4);
    }

    private void setupImageProfile(HBox imageScroll) {
        // Cicla attraverso tutti gli elementi dell'HBox (le immagini)
        for (int i = 0; i < imageScroll.getChildren().size(); i++) {
            // prendo un'immagine che ce all'interno dello scrollImage
            Node scrollImage = imageScroll.getChildren().get(i);

            // ogni volta che clicco un immagine all'interno di un imageScroll allora succede qualcosa
            scrollImage.setOnMouseClicked(event -> {
                // Copia l'immagine SVG dal gruppo selezionato
                Group originalGroup = (Group) scrollImage;

                // Crea un nuovo gruppo che contiene l'immagine SVG
                Group clonedGroup = IconSVG.copyGroup(originalGroup);  // Crea una copia del gruppo con l'immagine

                // Aggiungi il clone al container principale
                defaultImagine.getChildren().clear();  // Rimuovi la precedente immagine
                defaultImagine.getChildren().addFirst(clonedGroup);  // Aggiungi la nuova immagine

                // Salva il riferimento dell'immagine selezionata
                originalProfileImage = clonedGroup;  // Salva il clone come immagine originale

                for(int j = 0 ; j < 4 ; j++){
                    HBox c = (HBox) imageContainer.getChildren().get(j);
                    if (c.getChildren().contains(originalGroup)){
                        codImmagineCorrente = c.getChildren().indexOf(originalGroup)+j*4;
                    }
                }
            });

        }
    }
}
