package com.esa.moviestar.movie_view;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class FilmSceneController {

    public AnchorPane background;

    public VBox filmdetail;

    public Error initialize(){
        if(background.getBackground() == null){
            return new Error("background not loaded");
        }

        return null;
    }
}
