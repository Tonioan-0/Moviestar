package com.esa.moviestar.home;

import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.components.Carousel;
import com.esa.moviestar.components.ScrollView;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.Utente;
import com.esa.moviestar.movie_view.WindowCardController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.util.*;

public class HomeController {
    @FXML
    private VBox body;
    @FXML
    private ScrollPane root;
    @FXML
    private VBox scrollViewContainer;


    /**
     * Set all recommendation lists for a user profile
     */
    public void setRecommendations(Utente user,  MainPagesController mainPagesController) {
        try {
            scrollViewContainer.getChildren().clear();
            List<List<Content>> contentList= new ContentDao().getHomePageContents(user);
            List<Node> carouselList= new Vector<>();
            for (Content c:contentList.getFirst()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(mainPagesController.PATH_WINDOW_CARD),mainPagesController.resourceBundle);
                Node body = loader.load();
                WindowCardController windowCardController  = loader.getController();
                windowCardController.setContent(c);
                windowCardController.getPlayButton().setOnMouseClicked(e->mainPagesController.cardClickedPlay(windowCardController.getCardId()));
                windowCardController.getInfoButton().setOnMouseClicked(e->mainPagesController.cardClicked(windowCardController.getCardId()));
                carouselList.add(body);
            }
            Carousel carousel = new Carousel();
            carousel.getItems().addAll(carouselList);
            carousel.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/esa/moviestar/styles/carousel.css")).toExternalForm());
            body.getChildren().add(1, carousel);

            ScrollView top10Scroll = new ScrollView("Chosen for you:", Color.TRANSPARENT, mainPagesController.FORE_COLOR, mainPagesController.BACKGROUND_COLOR);
            top10Scroll.setContent(mainPagesController.createFilmNodes(contentList.get(1), false));


            ScrollView latest10Scroll = new ScrollView("New Releases:", Color.rgb(228, 193, 42), mainPagesController.BACKGROUND_COLOR, null, 32.0);
            latest10Scroll.setContent(mainPagesController.createFilmNodes( contentList.get(2), true));


            ScrollView favouriteCategoryScroll = new ScrollView("We think you'll like them:", Color.TRANSPARENT, mainPagesController.FORE_COLOR, mainPagesController.BACKGROUND_COLOR);
            favouriteCategoryScroll.setContent(mainPagesController.createFilmNodes( contentList.get(3), false));


            if(!contentList.get(4).isEmpty()) {
                ScrollView similarToLastWatchedScroll = new ScrollView("Similar at " + contentList.get(4).getFirst().getTitle() + " :", Color.TRANSPARENT, mainPagesController.FORE_COLOR, mainPagesController.BACKGROUND_COLOR);
                similarToLastWatchedScroll.setContent(mainPagesController.createFilmNodes(contentList.get(4), false));
            }

            ScrollView recommendSeriesScroll = new ScrollView("Series that you may like:", Color.TRANSPARENT, mainPagesController.FORE_COLOR, mainPagesController.BACKGROUND_COLOR);
            recommendSeriesScroll.setContent(mainPagesController.createFilmNodes( contentList.get(6), true));



            ScrollView bottom7Scroll = new ScrollView("New Experiences:", Color.TRANSPARENT, mainPagesController.FORE_COLOR, mainPagesController.BACKGROUND_COLOR);
            bottom7Scroll.setContent(mainPagesController.createFilmNodes( contentList.get(8), false));


            scrollViewContainer.getChildren().addAll(top10Scroll, latest10Scroll, favouriteCategoryScroll, recommendSeriesScroll, bottom7Scroll);

            if (!contentList.get(5).isEmpty()) {
                ScrollView watchedScroll =new ScrollView("Continue to watch:", Color.TRANSPARENT, mainPagesController.FORE_COLOR, mainPagesController.BACKGROUND_COLOR, 32.0);
                watchedScroll.setContent(mainPagesController.createFilmNodes( contentList.get(5), true));
                scrollViewContainer.getChildren().add(3,watchedScroll);
            }

            if (!contentList.get(7).isEmpty()) {
                ScrollView favouriteScroll = new ScrollView("Favourites:", Color.rgb(155, 155, 155), mainPagesController.BACKGROUND_COLOR, null, 32.0);
                favouriteScroll.setContent(mainPagesController.createFilmNodes( contentList.get(7), true));
                scrollViewContainer.getChildren().add(5,favouriteScroll);
            }
            Platform.runLater(carousel::start);
        }
        catch (IOException e){
            System.err.println("HomeController: Failed to load recommendations \n Error:"+e.getMessage());
        }
    }



}