package com.esa.moviestar.settings;



import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.model.Account;

import com.esa.moviestar.model.User;
import javafx.fxml.FXML;

import javafx.scene.Node;


import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.io.IOException;

import java.util.List;



public class FavouriteSettingController {
    @FXML
    private VBox vboxContainer;


    private User user;
    private Account account;
    private SettingsViewController settingViewController;


    public void setAccount(Account account){
        this.account=account;
        System.out.println("FavouriteSettingController : email "+account.getEmail());
    }

    public void setScene(SettingsViewController container){this.settingViewController = container;}

    public void setUser(User user){
        vboxContainer.getChildren().clear();
        this.user = user;
        System.out.println("FavouriteSettingController : user : "+ user.getName()+" email user : "+ user.getEmail()+" id user : "+ user.getID());
        ContentDao dao = new ContentDao();
        List<Node> contentList;
        try {
            contentList = settingViewController.createFilmNodes(dao.favoriteListContent(user.getID()),this);
            for(int i = 0 ; i < contentList.size() ; i+= 4 ) {
                HBox row = new HBox();
                row.setSpacing(20);
                for (int j = 0 ; j < 4 && i+j<contentList.size(); j++) {
                    row.getChildren().addAll(contentList.get(i+j));
                }
                vboxContainer.getChildren().addAll(row);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
