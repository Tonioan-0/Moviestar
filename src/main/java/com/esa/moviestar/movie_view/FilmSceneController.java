package com.esa.moviestar.movie_view;

import com.esa.moviestar.home.MainPagesController;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class FilmSceneController {

    MainPagesController mainPagesController;
    // Main containers
    @FXML
    public StackPane background;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public VBox episodesList;

    // Buttons
    @FXML
    private Button closeButton;
    @FXML
    private Button playButton;
    @FXML
    private Button addButton;
    @FXML
    private Button infoButton;

    // Labels for content info
    @FXML
    private Label titleLabel;
    @FXML
    private Label yearLabel;
    @FXML
    private Label episodesLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label maturityLabel;
    @FXML
    private Label violenceLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label castLabel;
    @FXML
    private Label genresLabel;
    @FXML
    private Label showTypeLabel;
    @FXML
    private Label seriesTitleLabel;

    // Episode elements
    @FXML
    private ImageView episode1Thumb;
    @FXML
    private Label episode1Title;
    @FXML
    private Label episode1Duration;
    @FXML
    private Label episode1Description;

    public Error initialize() {
        if (background == null) {
            System.err.println("Errore: il nodo background non è stato inizializzato!");
            return new Error("background node not initialized");
        }
        // set on actions for buttons
        closeButton.setOnMouseClicked(event -> closeView());

        System.out.println("FilmSceneController inizializzato correttamente con un background valido.");
        return null;
    }

    public void setProperties(ImageView newBackground, MainPagesController mainPagesController) {
        if (newBackground == null) {
            System.err.println("Errore: il nuovo sfondo fornito è null.");
            return;
        }

        this.mainPagesController = mainPagesController;

        // Remove any previous backgrounds
        background.getChildren().removeIf(node -> node instanceof ImageView);

        newBackground.setEffect( setColorBackground());

        background.getChildren().addFirst(setImageBackground(newBackground));

        System.out.println("Nuovo sfondo con effetto scuro impostato correttamente.");
    }

    private ImageView setImageBackground(ImageView newBackground){
        newBackground.fitWidthProperty().bind(background.widthProperty());
        newBackground.fitHeightProperty().bind(background.heightProperty());
        newBackground.setPreserveRatio(false);
        return newBackground;
    }

    private ColorAdjust setColorBackground(){
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.6);
        colorAdjust.setSaturation(-0.2);

        GaussianBlur blur = new GaussianBlur(5);
        colorAdjust.setInput(blur);

        return colorAdjust;
    }

    // Action handlers for buttons
    @FXML
    private void closeView() {
        System.out.println("Close button clicked");
        background.getChildren().clear();
        mainPagesController.restorePreviousScene();
    }

    @FXML
    private void playContent() {
        System.out.println("Play button clicked");
        // Add logic to play the next episode
    }

    @FXML
    private void addToList() {
        System.out.println("Add to list button clicked");
        // Add logic to add content to user's list
    }

    @FXML
    private void showInfo() {
        System.out.println("Info button clicked");
        // Add logic to show more information
    }

    // Methods to update content dynamically
    public void setTitle(String title) {
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
    }

    public void setDescription(String description) {
        if (descriptionLabel != null) {
            descriptionLabel.setText(description);
        }
    }

    public void setYear(String year) {
        if (yearLabel != null) {
            yearLabel.setText(year);
        }
    }

    public void setEpisodeCount(String episodes) {
        if (episodesLabel != null) {
            episodesLabel.setText(episodes);
        }
    }

    public void setCast(String cast) {
        if (castLabel != null) {
            castLabel.setText(cast);
        }
    }

    public void setGenres(String genres) {
        if (genresLabel != null) {
            genresLabel.setText(genres);
        }
    }

    public void setEpisodeThumbnail(Image image) {
        if (episode1Thumb != null && image != null) {
            episode1Thumb.setImage(image);
        }
    }

    public void setEpisodeTitle(String title) {
        if (episode1Title != null) {
            episode1Title.setText(title);
        }
    }

    public void setEpisodeDescription(String description) {
        if (episode1Description != null) {
            episode1Description.setText(description);
        }
    }

    public void setEpisodeDuration(String duration) {
        if (episode1Duration != null) {
            episode1Duration.setText(duration);
        }
    }
}