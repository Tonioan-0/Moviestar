package com.esa.moviestar.profile;

import java.io.IOException;
import java.util.ResourceBundle;

import com.esa.moviestar.database.UtenteDao;
import com.esa.moviestar.home.MainPagesController;
import com.esa.moviestar.login.AnimationUtils;
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

public class CreateProfileController {

    @FXML GridPane pageContainer;
    @FXML VBox ContenitorePaginaCreazione;
    @FXML VBox elementContainer;
    @FXML Group defaultImagine;
    @FXML Label creationTitle;
    @FXML TextField textName;
    @FXML HBox imageScroll1;
    @FXML HBox imageScroll2;
    @FXML HBox imageScroll3;
    @FXML HBox imageScroll4;
    @FXML VBox scrollContainer;
    @FXML Button saveButton;
    @FXML Button cancelButton;
    @FXML Label warningText;
    @FXML Label errorText;
    @FXML VBox imageContainer;
    @FXML HBox buttonContainer;

    private Group originalProfileImage;
    private int codImmagineCorrente;
    private Account account;
    private Origine origine;
    private Utente utente;
    private UtenteDao dao;

    public final ResourceBundle resourceBundle = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");

    public enum Origine{
        HOME,
        PROFILE,
        REGISTER;
    }

    public void setAccount(Account account) {
        this.account = account;
        System.out.println("CreateProfileController = email : " + account.getEmail());
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
        System.out.println("CreateProfileController = utente  : " + utente.getNome());
    }

    public void setOrigine(Origine origine) {
        this.origine = origine;
    }

    private boolean validaNome(String nome) {
        return !nome.isEmpty();
    }

    private Utente creaUtente(String nome, int immagine) {
        String gusto = "505050505050505050505050505050505050";
        return new Utente(nome, immagine, gusto, account.getEmail());
    }

    private boolean salvaUtente(Utente utente) {
        try {
            UtenteDao utentedao = new UtenteDao();
            utentedao.inserisciUtente(utente);
            return true;
        } catch (Exception e) {
            System.err.println("Errore salvataggio profilo: " + e.getMessage());
            return false;
        }
    }

    private void tornaAiProfili() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/profile-view.fxml"));
            Parent profileContent = loader.load();

            ProfileView profileView = loader.getController();
            profileView.setAccount(account);

            Scene currentScene = ContenitorePaginaCreazione.getScene();
            Scene newScene = new Scene(profileContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) ContenitorePaginaCreazione.getScene().getWindow();
            stage.setScene(newScene);
        }catch (IOException ex){
            System.out.println("CreateProfileController : errore nel ritornare nella visualizzazione dei profili"+ex.getMessage());
            warningText.setText("Errore durante il caricamento della pagina: " + ex.getMessage());
        }
    }

    private void tornaAllaHome(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/home/main.fxml"), resourceBundle);
            Parent homeContent = loader.load();

            MainPagesController mainPagesController = loader.getController();
            mainPagesController.first_load(utente,account);

            Scene currentScene = ContenitorePaginaCreazione.getScene();
            Scene newScene = new Scene(homeContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) ContenitorePaginaCreazione.getScene().getWindow();
            stage.setScene(newScene);

        } catch (IOException e) {
            System.err.println("CreateProfileController : Errore nel tornare alla home"+e.getMessage());
        }
    }

    public void initialize() {
        dao=new UtenteDao();
        impostaUI();
        creaGalleriaImmagini();
        impostaBottoni();
    }

    private void impostaUI() {
        errorText.setText("");

        codImmagineCorrente=0;
        defaultImagine.getChildren().add(IconSVG.takeElement(codImmagineCorrente));
        defaultImagine.setScaleX(10);
        defaultImagine.setScaleY(10);

        creationTitle.setText("Create Username:"); //label per sopra il textfield per farci capire che stiamo creando un nuovo utente
        textName.setPromptText("Name");// text field dove inserire il nome, (con all'interno trasparente la scritta "inserisci nome")

        //metto lo stile per ogni scroll di immagini
        scrollContainer.setSpacing(90);

        saveButton.setText("Save"); //setting del bottone di salvataggio
        cancelButton.setText("Cancel"); //setting del bottone di annullamento
    }

    private void creaGalleriaImmagini() {
        for (int i = 0; i <= 16; i++) {  // Aggiungi le 16 icone (da 1 a 16)
            Group g = new Group();
            g.setScaleX(3.8);
            g.setScaleY(3.8);

            g.setOnMouseEntered(event -> {
                g.setScaleY(4);
                g.setScaleX(4);
            });
            g.setOnMouseExited(event -> {
                g.setScaleX(3.8);
                g.setScaleY(3.8);
            });

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

        imageScroll1.setSpacing(130);
        imageScroll2.setSpacing(130);
        imageScroll3.setSpacing(130);
        imageScroll4.setSpacing(130);

        // Inizializza tutti gli HBox di immagini
        setupImageProfile(imageScroll1);
        setupImageProfile(imageScroll2);
        setupImageProfile(imageScroll3);
        setupImageProfile(imageScroll4);
    }

    private void impostaBottoni() {
        cancelButton.setOnMouseClicked(e -> {
            if(origine==Origine.HOME){
                tornaAllaHome();
            }else if (origine==Origine.PROFILE) {
                tornaAiProfili();
            }else if (origine==Origine.REGISTER || dao.contaProfiliPerEmail(account.getEmail())==0) {
                textName.setText("");
            }
        });

        saveButton.setOnMouseClicked(event -> {  //Se clicco sul bottone di salvataggio / dovrà poi ritornare alla pagina di scelta dei profili con il profilo creato
            gestisciSalvataggio();
        });
    }

    private void gestisciSalvataggio() {
        String name = textName.getText();

        if (validaNome(name)) {  //se ho messo un nome nel textfield e l'ho salvato allora ritorno alla pagina principale dei profili / oppure potrei far direttamente loggare / (modifiche da fare : controllare che abbia scelto anche un immagine, oppure se non l'ha scelta dare quella di default)
            //questo metodo cosi fa ritornare alla pagina dei profili, aggiungere poi il fatto che io abbia creato il panel nuovo con tutte le modifiche
            Utente nuovoUtente = creaUtente(name, codImmagineCorrente);

            if (salvaUtente(nuovoUtente)) {
                tornaAiProfili();
            } else {
                warningText.setText("Errore durante il salvataggio del profilo");
            }
        }else {
            errorText.setText("No name entered"); //se non ho inserito nessun nome mi da errore perchè per forza va settato un nome , oppure potrei dare il nome di default tipo utente 1
            AnimationUtils.shake(errorText);
        }
    }

    private void setupImageProfile(HBox imageScroll) {
        // Cicla attraverso tutti gli elementi dell'HBox (le immagini)
        for (int i = 0; i < imageScroll.getChildren().size(); i++) {
            // prendo un'immagine che ce all'interno dello scrollImage
            Node scrollImage = imageScroll.getChildren().get(i);

            // ogni volta che clicco un immagine all'interno di un imageScroll allora succede qualcosa
            scrollImage.setOnMouseClicked(event -> {
                gestisciSelezioneImmagine(scrollImage);
            });
        }
    }

    private void gestisciSelezioneImmagine(Node scrollImage) {
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
    }
}