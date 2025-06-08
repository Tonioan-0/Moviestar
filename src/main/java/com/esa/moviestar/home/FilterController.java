package com.esa.moviestar.home;

import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.components.ScrollView;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.User;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.List;

public class FilterController {

    @FXML
    VBox scrollViewContainer;

    public void setContent(MainPagesController mainPagesController,  User user, boolean isFilm) {
        try {
            List<List<Content>> contentList = new ContentDao().getFilterPageContents(user,isFilm);
            scrollViewContainer.getChildren().clear();
            ScrollView s1 = new ScrollView(isFilm?"New Releases":"Continue to watch:", Color.TRANSPARENT, MainPagesController.FORE_COLOR , MainPagesController.BACKGROUND_COLOR);
            s1.setContent(mainPagesController.createFilmNodes( contentList.get(0), false));

            ScrollView s2 = new ScrollView(isFilm?"Our selection for you":"New Releases:",  Color.TRANSPARENT, MainPagesController.FORE_COLOR , MainPagesController.BACKGROUND_COLOR);
            s2.setContent(mainPagesController.createFilmNodes( contentList.get(1), false));

            ScrollView s3 = new ScrollView((isFilm?"Film":"Program")+ "to watch", Color.TRANSPARENT, MainPagesController.FORE_COLOR , MainPagesController.BACKGROUND_COLOR);
            s3.setContent(mainPagesController.createFilmNodes( contentList.get(2), false));

            ScrollView s4 = new ScrollView("Explore:", Color.TRANSPARENT, MainPagesController.FORE_COLOR , MainPagesController.BACKGROUND_COLOR);
            s4.setContent(mainPagesController.createFilmNodes( contentList.get(3), false));

            ScrollView s5 = new ScrollView("Maybe you missed:", Color.TRANSPARENT, MainPagesController.FORE_COLOR , MainPagesController.BACKGROUND_COLOR);
            s5.setContent(mainPagesController.createFilmNodes( contentList.get(4), true ));

            scrollViewContainer.getChildren().addAll(s1,s2,s3,s4,s5 );
        }
        catch (IOException e){
            System.err.println("HomeController: Failed to load recommendations \n Error:"+e.getMessage());
        }
    }


}