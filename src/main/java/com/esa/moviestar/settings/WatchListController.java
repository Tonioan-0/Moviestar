package com.esa.moviestar.settings;

import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.User;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class WatchListController {
    @FXML
    private VBox vboxContainer;

    private Account account;
    private User user;

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setUtente(User user) {
        this.user = user;
    }


//    public void addContentToWatchList(Content content) throws IOException {
//        Node contentNode = createContentNodes(List.of(content)).get(0);
//        vboxContainer.getChildren().add(contentNode);
//    }
//
}

