package com.esa.moviestar.settings;

import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.model.User;
import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;

import java.util.List;

public class WatchListController {
    @FXML
    private VBox vboxContainer;
    private SettingsViewController settingViewController;


    public void setScene(SettingsViewController container){this.settingViewController = container;}

    public void updateWatchList(User user){
        vboxContainer.getChildren().clear();
        ContentDao dao = new ContentDao();
        List<Node> contentList;
        try {
            contentList = settingViewController.createFilmNodes(dao.watchlistContent(user.getID()),this);
            for(int i = 0 ; i < contentList.size() ; i+= 4 ) {
                HBox row = new HBox();
                row.setSpacing(20);
                for (int j = 0 ; j < 4 && i+j<contentList.size(); j++)
                    row.getChildren().addAll(contentList.get(i+j));
                vboxContainer.getChildren().addAll(row);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
