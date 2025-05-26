package com.esa.moviestar.settings;


import com.esa.moviestar.components.ScrollView;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Utente;
import com.esa.moviestar.profile.IconSVG;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.awt.*;
import java.util.ResourceBundle;

public class CronologiaSettingController {
    @FXML
    private GridPane gridPaneContent;
    @FXML
    private AnchorPane anchorPane1;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private AnchorPane anchorPane2;
    @FXML
    private Label label;
    @FXML
    private Text text;

    private Utente utente;
    private Account account;

    public void setAccount(Account account){
        this.account=account;
        System.out.println("CronologiaSettingController : email "+account.getEmail());
    }

    public void setUtente(Utente utente){
        this.utente=utente;
        System.out.println("CronologiaSettingController : utente : "+utente.getNome()+" email dell'utente : "+utente.getEmail()+" id utente : "+utente.getID());
    }

    public final ResourceBundle resourceBundle = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");

    public void initialize(){

    }


}
