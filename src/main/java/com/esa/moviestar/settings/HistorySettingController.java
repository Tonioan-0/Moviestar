package com.esa.moviestar.settings;


import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.User;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;


public class HistorySettingController {
   @FXML
   private VBox vboxContainer;

    private User user;
    private Account account;

    public void setAccount(Account account){
        this.account=account;
        System.out.println("CronologiaSettingController : email "+account.getEmail());
    }

    public void setUtente(User user){
        this.user = user;
        System.out.println("CronologiaSettingController : user : "+ user.getName()+" email dell'user : "+ user.getEmail()+" id user : "+ user.getID());
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
