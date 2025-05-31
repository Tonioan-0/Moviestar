package com.esa.moviestar.settings;


import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.Utente;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;


public class HistorySettingController {
   @FXML
   private VBox vboxContainer;

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



//
//    public void addFilmToHistory(Content content) throws IOException {
//        Node filmNode = createFilmNodes(List.of(content)).get(0);
//        vboxContainer.getChildren().add(filmNode);
//    }
//



}
