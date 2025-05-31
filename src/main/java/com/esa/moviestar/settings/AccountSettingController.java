package com.esa.moviestar.settings;

import com.esa.moviestar.database.AccountDao;
import com.esa.moviestar.database.UtenteDao;
import com.esa.moviestar.login.AnimationUtils;
import com.esa.moviestar.profile.CreateProfileController;
import com.esa.moviestar.profile.IconSVG;
import com.esa.moviestar.profile.ModifyProfileController;
import com.esa.moviestar.profile.ProfileView;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Utente;
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
import java.util.ResourceBundle;

import static com.esa.moviestar.login.Access.verifyPassword;

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


    private Utente utente;
    private AnchorPane contenitore;
    private Account account;
    private Label passwordErrata;

    public void setAccount(Account account){
        this.account=account;
        System.out.println("AccountViewController: email "+account.getEmail());
    }

    public void setUtente(Utente utente){
        this.utente=utente;
        if(utente!=null){
        int codImmagineCorrente = utente.getIDIcona();
        profileImage.getChildren().clear();
        Group g = new Group(IconSVG.takeElement(codImmagineCorrente));
        profileImage.getChildren().add(g);
        userName.setText(utente.getNome());
        registrationDate.setText(utente.getDataRegistrazione().toString());
        Email.setText("Email : "+utente.getEmail());
        System.out.println("AccountViewController : utente : "+utente.getNome()+" email dell'utente : "+utente.getEmail()+" id utente : "+utente.getID());
        }
    }

    public void setContenitore(AnchorPane contenitore) {
        this.contenitore = contenitore;
    }

    public final ResourceBundle resourceBundle = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");

    public void initialize(){
        modifyUser();
        deleteAccount();
        updatePassword();
        deleteUser();
        setPasswordWarning();
    }

    public void deleteUser(){
        deleteUserButton.setOnMouseClicked(event -> {
            DeletePopUp userPopUp = new DeletePopUp(false,account);

            AnchorPane.setBottomAnchor(userPopUp, 0.0);
            AnchorPane.setTopAnchor(userPopUp, 0.0);
            AnchorPane.setLeftAnchor(userPopUp, 0.0);
            AnchorPane.setRightAnchor(userPopUp, 0.0);

            contenitore.getChildren().add(userPopUp);

            userPopUp.getDeleteButton().setOnMouseClicked(e->{
                if(verifyPassword(userPopUp.getPasswordField().getText(),(account.getPassword()))){
                    UtenteDao utenteDao = new UtenteDao();
                    utenteDao.rimuoviUtente(utente.getID());
                    if(utenteDao.contaProfiliPerEmail(account.getEmail())>0){
                        System.out.println("hai eliminato un profilo , te ne restano "+utenteDao.contaProfiliPerEmail(account.getEmail()));
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/profile-view.fxml"));
                            Parent profileView = loader.load();
                            ProfileView profileView1 = loader.getController();
                            profileView1.setAccount(account);

                            Scene currentScene = accountContentSetting.getScene();
                            Scene newScene = new Scene(profileView, currentScene.getWidth(), currentScene.getHeight());

                            Stage stage = (Stage) accountContentSetting.getScene().getWindow();
                            stage.setScene(newScene);

                        }catch (IOException m ){
                            System.err.println("AccountSettingController : errore nel ritornare alla pagina dei profili "+m.getMessage());
                        }

                    }else if ((utenteDao.contaProfiliPerEmail(account.getEmail())==0)){
                        System.out.println("sei rimasto con 0 profili per questo account , la prova : "+utenteDao.contaProfiliPerEmail(account.getEmail()));
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/create-profile-view.fxml"),resourceBundle);
                            Parent createView = loader.load();
                            CreateProfileController createProfileController = loader.getController();
                            createProfileController.setAccount(account);

                            Scene currentScene = accountContentSetting.getScene();
                            Scene newScene = new Scene(createView, currentScene.getWidth(), currentScene.getHeight());

                            Stage stage = (Stage) accountContentSetting.getScene().getWindow();
                            stage.setScene(newScene);

                        } catch (IOException ex) {
                            System.err.println("AccountSettingController : errore nel ritornare alla pagina di creazione di un profilo "+ex.getMessage());
                        }
                    }
                }else {
                    if (!userPopUp.getPasswordBox().getChildren().contains(passwordErrata)) {
                        userPopUp.getPasswordBox().getChildren().add(passwordErrata);
                    }
                    AnimationUtils.shake(passwordErrata);
                    System.out.println("user debug "+account.getPassword());
                    System.out.println("user debug "+userPopUp.getPasswordField().getText());

                    System.out.println("Errore nell'eliminazione dell'account, password sbagliata.");
                }
            });

            userPopUp.getCancelButton().setOnMouseClicked(event2->{
                contenitore.getChildren().remove(userPopUp);
            });

        });
    };

    public void deleteAccount() {
        deleteAccountButton.setOnMouseClicked(event -> {
            DeletePopUp accountPopUp = new DeletePopUp(true,account);

            AnchorPane.setBottomAnchor(accountPopUp, 0.0);
            AnchorPane.setTopAnchor(accountPopUp, 0.0);
            AnchorPane.setLeftAnchor(accountPopUp, 0.0);
            AnchorPane.setRightAnchor(accountPopUp, 0.0);

            contenitore.getChildren().add(accountPopUp);

            accountPopUp.getDeleteButton().setOnMouseClicked(e -> {
                if (verifyPassword(accountPopUp.getPasswordField().getText(),(account.getPassword()))) {
                    AccountDao accountDao = new AccountDao();
                    accountDao.rimuoviAccount(account.getEmail());
                    UtenteDao utenteDao = new UtenteDao();
                    utenteDao.rimuoviUtenteEmail(account.getEmail());
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"), resourceBundle);
                            Parent accessContent = loader.load();

                            Scene currentScene = accountContentSetting.getScene();
                            Scene newScene = new Scene(accessContent, currentScene.getWidth(), currentScene.getHeight());

                            Stage stage = (Stage) accountContentSetting.getScene().getWindow();
                            stage.setScene(newScene);
                        } catch (IOException ex) {
                            System.err.println("AccountSettingController: Errore caricamento pagina di accesso dell'account: " + ex.getMessage());
                        }
                    } else {
                        if (!accountPopUp.getPasswordBox().getChildren().contains(passwordErrata)) {
                            accountPopUp.getPasswordBox().getChildren().add(passwordErrata);
                        }
                        AnimationUtils.shake(passwordErrata);
                        System.out.println("account debug "+account.getPassword());
                        System.out.println("account debug "+accountPopUp.getPasswordField().getText());
                        System.out.println("Errore nell'eliminazione dell'account, password sbagliata.");
                }
            });

            accountPopUp.getCancelButton().setOnMouseClicked(e -> {
                contenitore.getChildren().remove(accountPopUp); // Rimuove il popup
            });
        });
    }


    public void modifyUser(){
        modifyUserButton.setOnMouseClicked(event -> {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/modify-profile-view.fxml"),resourceBundle);
                Parent modifyContent = loader.load();
                ModifyProfileController modifyProfileController = loader.getController();
                modifyProfileController.setUtente(utente);
                modifyProfileController.setOrigine(ModifyProfileController.Origine.SETTINGS);
                modifyProfileController.setAccount(account);
                System.out.println("email passata alla pagina di modifica dai setting : "+account.getEmail());

                Scene currentScene = accountContentSetting.getScene();

                Scene newScene = new Scene(modifyContent, currentScene.getWidth(), currentScene.getHeight());

                Stage stage = (Stage) accountContentSetting.getScene().getWindow();
                stage.setScene(newScene);

            } catch (IOException e) {
                System.err.println("AccountSettingController: Errore caricamento pagina di modifica dell'utente"+e.getMessage());
            }
        });
    }

    public void updatePassword(){
        modifyPasswordButton.setOnMouseClicked(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/update-password-view.fxml"),resourceBundle);
                Parent updateContent = loader.load();

                UpdatePasswordController updatePasswordController = loader.getController();
                updatePasswordController.setUtente(utente);
                updatePasswordController.setAccount(account);

                Scene currentScene = accountContentSetting.getScene();

                Scene newScene = new Scene(updateContent, currentScene.getWidth(), currentScene.getHeight());

                Stage stage = (Stage) accountContentSetting.getScene().getWindow();
                stage.setScene(newScene);

            } catch (IOException e) {
                System.err.println("AccountSettingController: Errore caricamento pagina di aggiornamento della password"+e.getMessage());
            }
        });
    }


    private void setPasswordWarning(){
        passwordErrata  = new Label();

        passwordErrata.setText("Password Errata");

        passwordErrata.getStyleClass().addAll("warningText");

        VBox.setMargin(passwordErrata, new Insets(0, 0, 0, 40.0));
    }

}
