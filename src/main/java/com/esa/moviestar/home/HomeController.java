package com.esa.moviestar.home;

import com.esa.moviestar.Main;
import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.components.Carousel;
import com.esa.moviestar.components.ScrollView;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.User;
import com.esa.moviestar.movie_view.WindowCardController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.util.*;

public class HomeController {
    @FXML
    private VBox body;
    @FXML
    private VBox scrollViewContainer;


    /**
     * Set all recommendation lists for a user profile
     */
    public void setRecommendations(User user, MainPagesController mainPagesController) {
        try {
            scrollViewContainer.getChildren().clear();
            List<List<Content>> contentList= new ContentDao().getHomePageContents(user);
            List<Node> carouselList= new Vector<>();
            for (Content c:contentList.getFirst()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(MainPagesController.PATH_CARD_WINDOW), Main.resourceBundle);
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

            ScrollView top10Scroll = new ScrollView("Chosen for you:", Color.TRANSPARENT, MainPagesController.FORE_COLOR, MainPagesController.BACKGROUND_COLOR);
            top10Scroll.setContent(mainPagesController.createFilmNodes(contentList.get(1), false));


            ScrollView latest10Scroll = new ScrollView("New Releases:", Color.rgb(228, 193, 42), MainPagesController.BACKGROUND_COLOR, null, 32.0);
            latest10Scroll.setContent(mainPagesController.createFilmNodes( contentList.get(2), true));

            scrollViewContainer.getChildren().addAll(top10Scroll,latest10Scroll);

//            if(!contentList.get(4).isEmpty()) {
//                ScrollView similarToLastWatchedScroll = new ScrollView("Similar at " + contentList.get(4).getFirst().getTitle() + " :", Color.TRANSPARENT, MainPagesController.FORE_COLOR, MainPagesController.BACKGROUND_COLOR);
//                similarToLastWatchedScroll.setContent(mainPagesController.createFilmNodes(contentList.get(4), false));
//                scrollViewContainer.getChildren().add(similarToLastWatchedScroll);
//            }
//
//            if (!contentList.get(5).isEmpty()) {
//                ScrollView watchedScroll =new ScrollView("Continue to watch:", Color.TRANSPARENT, MainPagesController.FORE_COLOR, MainPagesController.BACKGROUND_COLOR, 32.0);
//                watchedScroll.setContent(mainPagesController.createFilmNodes( contentList.get(5), true));
//                scrollViewContainer.getChildren().add(watchedScroll);
//            }
//
//            if(!contentList.get(3).isEmpty()) {
//                ScrollView favouriteCategoryScroll = new ScrollView("We think you'll like them:", Color.TRANSPARENT, MainPagesController.FORE_COLOR, MainPagesController.BACKGROUND_COLOR);
//                favouriteCategoryScroll.setContent(mainPagesController.createFilmNodes(contentList.get(3), false));
//                scrollViewContainer.getChildren().add(favouriteCategoryScroll);
//            }
//
//            if (!contentList.get(6).isEmpty()) {
//                ScrollView recommendSeriesScroll = new ScrollView("Series that you may like:", Color.TRANSPARENT, MainPagesController.FORE_COLOR, MainPagesController.BACKGROUND_COLOR);
//                recommendSeriesScroll.setContent(mainPagesController.createFilmNodes(contentList.get(6), true));
//                scrollViewContainer.getChildren().add(recommendSeriesScroll);
//            }
//
//            if (!contentList.get(7).isEmpty()) {
//                ScrollView favouriteScroll = new ScrollView("Favourites:", Color.rgb(155, 155, 155), MainPagesController.BACKGROUND_COLOR, null, 32.0);
//                favouriteScroll.setContent(mainPagesController.createFilmNodes( contentList.get(7), true));
//                scrollViewContainer.getChildren().add(favouriteScroll);
//            }
//
//            ScrollView bottom7Scroll = new ScrollView("New Experiences:", Color.TRANSPARENT, MainPagesController.FORE_COLOR, MainPagesController.BACKGROUND_COLOR);
//            bottom7Scroll.setContent(mainPagesController.createFilmNodes( contentList.get(8), false));
//            scrollViewContainer.getChildren().addAll(bottom7Scroll);
//
//



        }
        catch (IOException e){
            System.err.println("HomeController: Failed to load recommendations \n Error:"+e.getMessage());
        }
    }
}