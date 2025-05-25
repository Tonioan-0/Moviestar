package com.esa.moviestar.movie_view;

import com.esa.moviestar.model.Content;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Objects;

public class WindowCardController {
    @FXML
    private AnchorPane windowCardRoot;
    @FXML
    private HBox controlsContainer;
    @FXML
    private ImageView imgView;
    @FXML
    private HBox playButton;
    @FXML
    private HBox infoButton;
    @FXML
    private Text titleLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Text ratingLabel;
    private int _id;

    @FXML
    public void initialize(){
        windowCardRoot.viewOrderProperty().addListener((observable, oldValue, newValue) -> {
            if(Objects.equals(oldValue, newValue))
                return;
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(100), controlsContainer);

            if (newValue.doubleValue() == 0) {
                // Window card is at viewOrder 0, make controls appear
                controlsContainer.setOpacity(0);
                controlsContainer.setVisible(true);
                fadeTransition.setFromValue(0);
                fadeTransition.setToValue(1);
                fadeTransition.play();
            } else {
                // Window card is not at viewOrder 0, make controls disappear
                fadeTransition.setFromValue(controlsContainer.getOpacity());
                fadeTransition.setToValue(0);
                fadeTransition.setOnFinished(event -> controlsContainer.setVisible(false));
                fadeTransition.play();
            }
        });
    }

    public void setContent(Content film){
        _id= film.getId();
        titleLabel.setText(film.getTitle());
        descriptionLabel.setText(film.getPlot());
        ratingLabel.setText(String.valueOf(film.getRating()));
        if(!Objects.equals(film.getImageUrl(), "error"))
            imgView.setImage(new Image(film.getImageUrl(),true));
    }

    public int getCardId() {
        return _id;
    }

    public Node getPlayButton() {
        return playButton;
    }
    public Node getInfoButton() {
        return playButton;
    }
}

