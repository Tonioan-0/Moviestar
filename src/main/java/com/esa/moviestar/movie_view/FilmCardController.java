package com.esa.moviestar.movie_view;

import com.esa.moviestar.model.Content;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import javafx.scene.image.PixelReader;

import java.util.Objects;
import java.util.ResourceBundle;


public class FilmCardController {
    @FXML
    ImageView imgView;
    @FXML
    Label titleLabel;
    @FXML
    Label descriptionLabel;
    @FXML
    HBox metadataPane;
    @FXML
    VBox contentPane;
    @FXML
    StackPane cardContainer;
    @FXML
    Region gradientOverlay;
    @FXML
    SVGPath durationIcon;
    @FXML
    Label timeLabel;
    @FXML
    Label ratingLabel;
    @FXML
    ResourceBundle resources;

    public int _id;
    private Color color;

    public void setContent(Content content) {
        // Clean up any previous shimmer if setContent is called again
//        removeShimmerOverlay();

        _id = content.getId();
        titleLabel.setText(content.getTitle());
        descriptionLabel.setText(content.getPlot());
        if(content.isSeasonDivided()){
            timeLabel.setText(content.getSeasonCount()+" Seasons");
            durationIcon.setContent(resources.getString("season"));
        }
        else if(content.isSeries()){
            timeLabel.setText(content.getEpisodeCount() +" Episodes");
            durationIcon.setContent(resources.getString("episodes"));
        }
        else{
            timeLabel.setText(((int)content.getDuration()/60)+"h "+((int)content.getDuration()%60)+"min");
            durationIcon.setContent(resources.getString("clock"));
        }
        ratingLabel.setText(String.valueOf(content.getRating()));


        try {
            if (content.getImageUrl() == null || content.getImageUrl().isEmpty() || Objects.equals(content.getImageUrl(), "error")) {
                // System.err.println("Error: Image URL is null or empty for content: " + content.getTitle());
                imgView.setImage(null); // Clear previous image
                //  Platform.runLater(this::displayErrorShimmer);

                return;
            }
            Image img = new Image(content.getImageUrl(), true);
            img.errorProperty().addListener((observable, oldValue, newValue) -> {
                Exception e = img.getException();
                if (e != null) {
                    System.err.println("Error loading image '" + content.getImageUrl());

                } else {
                    System.err.println("Error loading image '" + content.getImageUrl() + "': Unknown error.");
                }
                // Platform.runLater(this::displayErrorShimmer);
            });
            img.progressProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.doubleValue() == 1.0) {
                    imgView.setImage(img);
                    Platform.runLater(() -> setupGradientOverlay(img));
                }
            });

        } catch (Exception e) {
            System.err.println("FilmCardController: "+e.getMessage());
        } finally {
            Platform.runLater(this::setupHoverTransitions);
        }
    }

//    private void displayErrorShimmer() {
//        removeShimmerOverlay();
//        double overlayWidth = imgView.getFitWidth();
//        double overlayHeight = imgView.getFitHeight();
//
//        if (overlayWidth <= 0 && cardContainer.getWidth() > 0) overlayWidth = cardContainer.getWidth();
//        if (overlayHeight <= 0 && cardContainer.getHeight() > 0) overlayHeight = cardContainer.getHeight();
//
//        // Default sizes if everything is uninitialized
//        if (overlayWidth <= 0|| overlayHeight <= 0)
//            return;
//        Region shimmerOverlay = new Region();
//        shimmerOverlay.setBackground(Background.fill(Color.rgb(62,62,62)));
//        shimmerOverlay.setPrefSize(cardContainer.getWidth(), cardContainer.getHeight());
//        shimmerOverlay.setClip(new Rectangle(cardContainer.getWidth(), cardContainer.getHeight()) {{
//            setArcWidth(48);
//            setArcHeight(48);
//        }});
//        SequentialTransition completeAnimation = getSequentialTransition(shimmerOverlay);
//        cardContainer.getChildren().add(1, shimmerOverlay);
//        completeAnimation.play();
//    }
//
//    private static SequentialTransition getSequentialTransition(Region shimmerOverlay) {
//        FadeTransition shimmerFadeIn = new FadeTransition(Duration.seconds(10), shimmerOverlay);
//        shimmerFadeIn.setFromValue(0);
//        shimmerFadeIn.setToValue(1);
//
//        FadeTransition shimmerFadeOut = new FadeTransition(Duration.seconds(10), shimmerOverlay);
//        shimmerFadeOut.setFromValue(1);
//        shimmerFadeOut.setToValue(0);
//        SequentialTransition completeAnimation = new SequentialTransition(shimmerFadeIn, shimmerFadeOut);
//        completeAnimation.setCycleCount(10); // Repeat the entire fade-in and fade-out sequence
//        return completeAnimation;
//    }
//
//    private void removeShimmerOverlay() {
//        cardContainer.getChildren().removeIf(node -> node instanceof Rectangle);
//    }

    private void setupGradientOverlay(Image image) {
        // Extract dominant color from image if available
        if (image != null) {
            color = getMixedColorFromImage(image);
        }

        // Set default gradient overlay
        if (color != null) {
            gradientOverlay.setBackground(Background.fill(
                    new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                            new Stop(0.6, Color.TRANSPARENT),
                            new Stop(1, color))));
        } else {
            gradientOverlay.setBackground(Background.fill(
                    new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                            new Stop(0.6, Color.TRANSPARENT),
                            new Stop(1, Color.rgb(115, 65, 190)))));
        }
    }

    private void setupHoverTransitions() {
        // Set initial positions and states
        contentPane.setOpacity(0);

        // Set default gradient if not already set
        if (gradientOverlay.getBackground() == null) {
            gradientOverlay.setBackground(Background.fill(
                    new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                            new Stop(0.6, Color.TRANSPARENT),
                            new Stop(1, Color.rgb(115, 65, 190)))));
        }

        // Create a clip for the card to ensure animations stay within bounds
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(cardContainer.widthProperty());
        clip.heightProperty().bind(cardContainer.heightProperty());
        clip.setArcWidth(48);
        clip.setArcHeight(48);
        cardContainer.setClip(clip);

        // Create transitions for hover animation
        Duration duration = Duration.millis(250);
        TranslateTransition contentEnterTransition = new TranslateTransition(duration, contentPane);
        contentEnterTransition.setToY(0); // Move up into view
        FadeTransition metadataFadeOut = new FadeTransition(duration, metadataPane);
        metadataFadeOut.setToValue(0);
        FadeTransition contentFadeIn = new FadeTransition(duration, contentPane);
        contentFadeIn.setToValue(1);

        // Define transitions for mouse exit
        TranslateTransition metadataReturnTransition = new TranslateTransition(duration, metadataPane);
        metadataReturnTransition.setToY(0); // Return to original position
        TranslateTransition contentExitTransition = new TranslateTransition(duration, contentPane);
        contentExitTransition.setToY(50); // Move down out of view
        FadeTransition metadataFadeIn = new FadeTransition(duration, metadataPane);
        metadataFadeIn.setToValue(1);
        FadeTransition contentFadeOut = new FadeTransition(duration, contentPane);
        contentFadeOut.setToValue(0);

        // Apply hover listener
        cardContainer.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Mouse entered - stop any running animations and play enter transition
                contentEnterTransition.stop();
                metadataFadeOut.stop();
                contentFadeIn.stop();

                // Update gradient on hover
                if (color != null) {
                    gradientOverlay.setBackground(Background.fill(
                            new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                                    new Stop(0, Color.TRANSPARENT),
                                    new Stop(0.4, color))));
                }

                // Play animations
                contentEnterTransition.play();
                metadataFadeOut.play();
                contentFadeIn.play();
            } else {
                // Mouse exited - stop any running animations and play exit transition
                metadataReturnTransition.stop();
                contentExitTransition.stop();
                metadataFadeIn.stop();
                contentFadeOut.stop();

                // Return gradient to original state
                if (color != null) {
                    gradientOverlay.setBackground(Background.fill(
                            new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                                    new Stop(0.6, Color.TRANSPARENT),
                                    new Stop(1, color))));
                }

                // Play animations
                metadataReturnTransition.play();
                contentExitTransition.play();
                metadataFadeIn.play();
                contentFadeOut.play();
            }
        });
    }

    public Color getMixedColorFromImage(Image image) {
        // Get pixel reader for the image
        PixelReader pixelReader = image.getPixelReader();

        // Image dimensions
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        // Variables to store total RGB values
        double totalRed = 0;
        double totalGreen = 0;
        double totalBlue = 0;
        int sampleStride = Math.max(1, (width * height) / 5000);
        int samplesCount = 0;

        // Iterate through pixels with stride
        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                Color pixelColor = pixelReader.getColor(x, y);
                totalRed +=  pixelColor.getRed();
                totalGreen +=  pixelColor.getGreen();
                totalBlue +=  pixelColor.getBlue();
                samplesCount++;
            }
        }

        // Calculate average color
        double avgRed = totalRed / samplesCount;
        double avgGreen = totalGreen / samplesCount;
        double avgBlue = totalBlue / samplesCount;

        // Calculate brightness using a common formula
        double brightness = 0.299 * avgRed + 0.587 * avgGreen + 0.114 * avgBlue;

        // Adjust brightness if it's too high (above 0.7 on a 0-1 scale)
        if (brightness > 0.7) {
            double reductionFactor = 0.7 / brightness;
            avgRed *=  reductionFactor;
            avgGreen *= reductionFactor;
            avgBlue *= reductionFactor;
        }

        // Return the balanced color
        return Color.color(avgRed, avgGreen, avgBlue);
    }

    public int getCardId() {
        return _id;
    }
}