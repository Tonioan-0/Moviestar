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

import static com.esa.moviestar.Main.resourceBundle;

public class WatchListController {
    @FXML
    private VBox vboxContainer;

    private Account account;
    private Utente utente;

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setUtente(Utente utente) {
        this.utente=utente;
    }


//    public void addContentToWatchList(Content content) throws IOException {
//        Node contentNode = createContentNodes(List.of(content)).get(0);
//        vboxContainer.getChildren().add(contentNode);
//    }
//
}

