package com.esa.moviestar.home;

import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.User;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.util.List;

public class SearchController {
    //@FXML Objects
    Button searchLabel;
    @FXML
    Line separatorLine;
    @FXML
    Button findOutButton;
    Button genre;
    Button filmSeries;
    @FXML
    FlowPane filmSeriesRecommendations;
    @FXML
    FlowPane recommendations;

    //private db
    private String searchText;
    private MainPagesController setupController;
    private HeaderController headerController;
    private User user;
    private ContentDao dbSearch;
    private List<Node> tryRecommendationList;
    List<Content> suggestedContent;

    List<Content> content;
    private List<Node> tryList;

    public void initialize(){
        setupController = new MainPagesController();
        separatorLine.setStroke(
                new LinearGradient(
                        0, 0,
                        1, 0,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.TRANSPARENT),
                        new Stop(0.5, Color.WHITE),
                        new Stop(1.0, Color.TRANSPARENT)
                ));
    }

    public void setParamController(HeaderController header, User user, MainPagesController mainPagesController) throws IOException {
//        this.headerController = header;
//        this.user = user;
//        this.setupController = mainPagesController; // Use the passed instance
//        String searchText= headerController.getTbxSearch().getText();
//        if (dbSearch == null) {
//            dbSearch = new ContentDao();
//        }
//
//        content = dbSearch.take_film_series(searchText, user);
//        suggestedContent = dbSearch.takeRecommendations(searchText, user);
//        tryList = setupController.createFilmNodes(content, false);
//        tryRecommendationList = setupController.createFilmNodes(suggestedContent, false);
//        recommendedList();
//        recommendedSeriesFilms();
    }

    // Modifica al metodo recommendedList() per usare FlowPane esistente
    public void recommendedList() {
//        if (!headerController.getTbxSearch().getText().isEmpty()) {
//            recommendations.getChildren().clear();
//            for(int i = 0; i < tryRecommendationList.size(); i++) {
//                HBox itemContainer = new HBox();
//                itemContainer.setAlignment(javafx.geometry.Pos.CENTER);
//                itemContainer.setSpacing(2);
//
//                // Bottone con titolo del contenuto
//                Button dynamicButton = new Button(suggestedContent.get(i).getTitle());
//                dynamicButton.getStyleClass().add("register-text-recommendations-mid");
//                dynamicButton.setOnAction(event-> setupController.openFilmScene(1));
//                itemContainer.getChildren().add(dynamicButton);
//
//                if (i < tryRecommendationList.size() - 1) {
//                    Line verticalSeparator = new Line();
//                    verticalSeparator.setStartY(0);
//                    verticalSeparator.setEndY(30);
//                    verticalSeparator.setStrokeWidth(1.5);
//                    verticalSeparator.setStroke(Color.WHITE);
//                    HBox.setMargin(verticalSeparator, new javafx.geometry.Insets(0, 8, 0, 8));
//                    itemContainer.getChildren().add(verticalSeparator);
//                }
//                recommendations.getChildren().add(itemContainer);
//            }
//        }
    }
    public void recommendedSeriesFilms(){
        if (!headerController.getTbxSearch().getText().isEmpty()){

            for (Node dynamicContent : tryList) {
                filmSeriesRecommendations.getChildren().add(dynamicContent);
            }
        }
    }

}