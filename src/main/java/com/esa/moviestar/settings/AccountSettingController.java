package com.esa.moviestar.settings;

import com.esa.moviestar.database.AccountDao;
import com.esa.moviestar.database.UtenteDao;
import com.esa.moviestar.login.UpdatePasswordController;
import com.esa.moviestar.profile.CreateProfileController;
import com.esa.moviestar.profile.IconSVG;
import com.esa.moviestar.profile.ModifyProfileController;
import com.esa.moviestar.profile.ProfileView;
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
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

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


    private Utente utente;
    private AnchorPane contenitore;
    private Account account;

    public void setAccount(Account account){
        this.account=account;
    }

    public void setUtente(Utente utente){
        this.utente=utente;
        if(utente!=null){
        int codImmagineCorrente = utente.getIDIcona();
        profileImage.getChildren().clear();
        Group g = new Group(IconSVG.takeElement(codImmagineCorrente));
        profileImage.getChildren().add(g);
        userName.setText(utente.getNome());
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

    }

    public void deleteUser(){
        deleteUserButton.setOnMouseClicked(event -> {
            DeletePopUp userPopUp = new DeletePopUp(false);

            AnchorPane.setBottomAnchor(userPopUp, 0.0);
            AnchorPane.setTopAnchor(userPopUp, 0.0);
            AnchorPane.setLeftAnchor(userPopUp, 0.0);
            AnchorPane.setRightAnchor(userPopUp, 0.0);

            contenitore.getChildren().add(userPopUp);

            userPopUp.getDeleteButton().setOnMouseClicked(e->{
                if(userPopUp.getPasswordField().getText().equals(account.getPassword())){
                    UtenteDao utenteDao = new UtenteDao();
                    boolean deleteSuccess = utenteDao.rimuoviUtente(utente.getID());
                    System.out.println("numero di profili che hai all'interno del tuo account : "+utenteDao.contaProfiliPerEmail(account.getEmail()));
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
                }
            });

            userPopUp.getCancelButton().setOnMouseClicked(event2->{
                contenitore.getChildren().remove(userPopUp);
            });

        });
    };

    public void deleteAccount() {
        deleteAccountButton.setOnMouseClicked(event -> {
            DeletePopUp accountPopUp = new DeletePopUp(true);

            AnchorPane.setBottomAnchor(accountPopUp, 0.0);
            AnchorPane.setTopAnchor(accountPopUp, 0.0);
            AnchorPane.setLeftAnchor(accountPopUp, 0.0);
            AnchorPane.setRightAnchor(accountPopUp, 0.0);

            contenitore.getChildren().add(accountPopUp);

            accountPopUp.getDeleteButton().setOnMouseClicked(e -> {
                if (accountPopUp.getPasswordField().getText().equals(account.getPassword())) {
                    AccountDao accountDao = new AccountDao();
                    boolean deleteSuccess = accountDao.rimuoviAccount(account.getEmail());

                    if (deleteSuccess) {
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
                        System.out.println("Errore nell'eliminazione dell'account.");
                    }
                } else {
                    System.out.println("Password errata.");
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

                Scene currentScene = accountContentSetting.getScene();

                Scene newScene = new Scene(updateContent, currentScene.getWidth(), currentScene.getHeight());

                Stage stage = (Stage) accountContentSetting.getScene().getWindow();
                stage.setScene(newScene);

            } catch (IOException e) {
                System.err.println("AccountSettingController: Errore caricamento pagina di aggiornamento della password"+e.getMessage());
            }
        });
    }

}
