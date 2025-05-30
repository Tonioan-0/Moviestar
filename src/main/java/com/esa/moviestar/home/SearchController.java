package com.esa.moviestar.home;
import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.components.ScrollView;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.Utente;
import com.esa.moviestar.components.ScrollViewSkin;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.util.*;
import java.util.List;

public class SearchController {
    @FXML
    public Button searchlabel;
    @FXML
    public Line separatorline;
    @FXML
    private Button findout;
    @FXML
    private Button genre;
    @FXML
    private Button filmSeries;
    @FXML
    private FlowPane filmseriesRaccomendations;
    @FXML
    private Pane pnl_separator1;
    @FXML
    private FlowPane raccomendations;
    //private db
    private String searchText;

    private MainPagesController setupController;

    private HeaderController headerController;

    private Utente p;

    private ContentDao dbSearch;

    public ResourceBundle resourcebundlesearch;

    private List<Node> tryraccomendationlist;

    List<Content> suggestedContent;

    private ScrollViewSkin separator;

    private ScrollView useless;

    List<Content> content;

    private List<Node> trylist;

    public void initialize(){
        useless = new ScrollView();
        separator = new ScrollViewSkin(useless);
        setupController = new MainPagesController();
        if (separatorline != null) {
            separatorline.setStroke(separator.getLinearGradient(Color.WHITE));
        }
    }

    public void set_paramcontroller(HeaderController h, Utente u, ResourceBundle bundle, MainPagesController mainPagesController) throws IOException {
        this.headerController = h;
        this.p = u;
        this.resourcebundlesearch = bundle;
        this.setupController = mainPagesController; // Use the passed instance
        String searchText= headerController.getTbxSearch().getText();
        if (dbSearch == null) {
            dbSearch = new ContentDao();
        }

        content = dbSearch.take_film_tvseries(searchText, p);
        suggestedContent = dbSearch.take_reccomendations(searchText, p);
        trylist = setupController.createFilmNodes(content, false);
        tryraccomendationlist = setupController.createFilmNodes(suggestedContent, false);
        reccomendedList();
        raccomendedSeriesFilms();
    }

    // Modifica al metodo reccomendedList() per usare FlowPane esistente
    public void reccomendedList() {
        if (!headerController.getTbxSearch().getText().isEmpty()) {

            raccomendations.getChildren().clear();

            for(int i = 0; i < tryraccomendationlist.size(); i++) {
                HBox itemContainer = new HBox();
                itemContainer.setAlignment(javafx.geometry.Pos.CENTER);
                itemContainer.setSpacing(2);

                // Bottone con titolo del contenuto
                Button dynamicButton = new Button(suggestedContent.get(i).getTitle());
                dynamicButton.getStyleClass().addAll("register-text-raccomendations-mid");
                dynamicButton.setOnAction(event-> setupController.openFilmScene(1));///////////////////////////////////////////////////////////////////////////////////////////////////////////qiua serve l'id del contenuto

                itemContainer.getChildren().add(dynamicButton);

                if (i < tryraccomendationlist.size() - 1) {

                    Line verticalSeparator = new Line();

                    verticalSeparator.setStartY(0);
                    verticalSeparator.setEndY(30);
                    verticalSeparator.setStrokeWidth(1.5);

                    verticalSeparator.setStroke(separator.getVerticalLinearGradient(Color.WHITE));

                    HBox.setMargin(verticalSeparator, new javafx.geometry.Insets(0, 8, 0, 8));

                    itemContainer.getChildren().add(verticalSeparator);
                }
                raccomendations.getChildren().add(itemContainer);
            }
        }
    }
    public void raccomendedSeriesFilms(){
        if (!headerController.getTbxSearch().getText().isEmpty()){

            for(int i = 0;i<trylist.size() ; i++){

                Node dynamicContent = trylist.get(i);
                filmseriesRaccomendations.getChildren().add(dynamicContent);
            }
        }
    }

}