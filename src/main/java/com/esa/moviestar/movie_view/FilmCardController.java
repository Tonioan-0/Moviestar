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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public int _id;
    private Color color; // This will store the calculated color


    private static final ExecutorService imageProcessingExecutor =
            Executors.newCachedThreadPool(r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            });


    public void setContent(Content content, boolean isVertical) {
        _id = content.getId();
        titleLabel.setText(content.getTitle());

        String plot = content.getPlot();
        if (plot != null)
            descriptionLabel.setText(plot.length() > 128 ? plot.substring(0, 128) + "..." : plot);
        else
            descriptionLabel.setText("");

        //In the first design (before the inclusion of the api)we have thought to be a modern and simple user
        // function to give at the final user a data like the duration of the movie, the number of episodes of a series  or the number of season
//        if(content.isSeasonDivided()){
//            timeLabel.setText(content.getSeasonCount()+" Seasons");
//            durationIcon.setContent(resources.getString("season"));
//        }
//        else if(content.isSeries()){
//            timeLabel.setText(content.getEpisodeCount() +" Episodes");
//            durationIcon.setContent(resources.getString("episodes"));
//        }
//        else{
//            timeLabel.setText(((int)content.getTime()/60)+"h "+((int)content.getTime()%60)+"min" );
//            durationIcon.setContent(resources.getString("clock"));}

        ratingLabel.setText(String.valueOf(content.getRating()).substring(0, 3));
        try {
            String imageUrl = isVertical ? content.getPosterUrl() : content.getImageUrl();

            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                System.err.println("FilmCardController: Image URL is null or empty for content ID: " + content.getId());
                // Optionally set a placeholder image here
                Platform.runLater(() -> setupGradientOverlay(null)); // Setup default gradient
                return;
            }

            Image img = new Image(imageUrl, true); // true for background loading
            img.errorProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue ) {
                    System.err.println( "FilmCardController: Error loading image '" + imageUrl + "' for content ID: " + content.getId() + ". Exception: " + img.getException());
                    Platform.runLater( () -> setupGradientOverlay(null) );
                }
            });

            img.progressProperty().addListener(( observable, oldValue,  newValue) -> {
                if (newValue.doubleValue() == 1.0 && !img.isError()) { // Image fully loaded and no error
                    imgView.setImage(img );
                    CompletableFuture.supplyAsync(() -> getMixedColorFromImage(img), imageProcessingExecutor)
                            .thenAcceptAsync(calculatedColor -> {
                                this.color = calculatedColor;
                                setupGradientOverlay(img);
                            }, Platform::runLater)
                            .exceptionally(ex -> {
                                System.err.println( "FilmCardController: Error processing image color: " + ex.getMessage());
                                Platform.runLater( () ->  {
                                    this.color = null;
                                    setupGradientOverlay(null);
                                });
                                return null;
                            });
                }
            });
        } catch (Exception e) {
            System.err.println("FilmCardController: Exception in setContent during image loading for content ID " + content.getId() + ": " + e.getMessage());
            Platform.runLater(() -> setupGradientOverlay(null));
        } finally {
            Platform.runLater(this::setupHoverTransitions);
        }
    }


    private void setupGradientOverlay(Image image) {
        Color effectiveColor = this.color;
        LinearGradient gradient;
        gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.6, Color.TRANSPARENT),
                new Stop(1, Objects.requireNonNullElseGet(effectiveColor, () -> Color.rgb(115, 65, 190))));
        gradientOverlay.setBackground(Background.fill(gradient));
    }

    private void setupHoverTransitions() {
        contentPane.setOpacity(0);
        contentPane.setTranslateY(50);


        if (gradientOverlay.getBackground() == null) {

            LinearGradient initialGradient;
            initialGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0.6, Color.TRANSPARENT),
                    new Stop(1, Objects.requireNonNullElseGet(this.color, () -> Color.rgb(115, 65, 190))));
            gradientOverlay.setBackground(Background.fill(initialGradient));
        }

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(cardContainer.widthProperty());
        clip.heightProperty().bind(cardContainer.heightProperty());
        //the cards are medium item so have a radius of 24px, to have the same radius for the clip we need arc at radius*2
        clip.setArcWidth(48);
        clip.setArcHeight(48);
        cardContainer.setClip(clip);

        Duration duration = Duration.millis(250);

        TranslateTransition contentEnterTransition = new TranslateTransition(duration, contentPane);
        contentEnterTransition.setToY(0);
        FadeTransition metadataFadeOut = new FadeTransition(duration, metadataPane);
        metadataFadeOut.setToValue(0);
        FadeTransition contentFadeIn = new FadeTransition(duration, contentPane);
        contentFadeIn.setToValue(1);

        TranslateTransition contentExitTransition = new TranslateTransition(duration, contentPane);
        contentExitTransition.setToY(50);
        FadeTransition metadataFadeIn = new FadeTransition(duration, metadataPane);
        metadataFadeIn.setToValue(1);
        FadeTransition contentFadeOut = new FadeTransition(duration, contentPane);
        contentFadeOut.setToValue(0);

        ParallelTransition onHover = new ParallelTransition(contentEnterTransition, metadataFadeOut, contentFadeIn);
        ParallelTransition onExit = new ParallelTransition(contentExitTransition, metadataFadeIn, contentFadeOut);


        cardContainer.hoverProperty().addListener((observable, oldValue, isHovering) -> {
            Color currentDominantColor = this.color; // Use the member variable

            if (isHovering) {
                onExit.stop();
                LinearGradient hoverGradient= new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.TRANSPARENT),
                        new Stop(0.4,
                                currentDominantColor==null  ?
                                        Color.rgb(115, 65, 190, 0.7) :
                                        currentDominantColor.deriveColor(0, 1, 1, 0.9)
                        ),
                        new Stop(1,
                                currentDominantColor==null  ?
                                        Color.rgb(115, 65, 190) :
                                        currentDominantColor
                        )
                );

                gradientOverlay.setBackground(Background.fill(hoverGradient));
                onHover.play();
            } else {
                onHover.stop(); // Stop hover animations if any
                LinearGradient normalGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0.6, Color.TRANSPARENT),
                        new Stop(1, Objects.requireNonNullElseGet(currentDominantColor, () -> Color.rgb(115, 65, 190))));
                gradientOverlay.setBackground(Background.fill(normalGradient));
                onExit.play();
            }
        });
    }

    // This method now runs on a background thread
    public Color getMixedColorFromImage(Image image) {
        if (image == null || image.isError() || image.getWidth() == 0 || image.getHeight() == 0)
            return null;

        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null)
            return null;

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        double totalRed = 0;
        double totalGreen = 0;
        double totalBlue = 0;


        int numPixels = width * height;
        int desiredSamples = Math.min(numPixels, 2000);
        int sampleStride = numPixels > 0 ? Math.max(1, (int) Math.sqrt((double)numPixels / desiredSamples)) : 1;

        int samplesCount = 0;

        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                try {
                    Color pixelColor = pixelReader.getColor(x, y);
                    totalRed += pixelColor.getRed();
                    totalGreen += pixelColor.getGreen();
                    totalBlue += pixelColor.getBlue();
                    samplesCount++;
                } catch (Exception e) {
                    System.err.println("FilmCardController: Error processing image color");
                }
            }
        }

        if (samplesCount == 0)
            return null;


        double avgRed = totalRed / samplesCount;
        double avgGreen = totalGreen / samplesCount;
        double avgBlue = totalBlue / samplesCount;

        double brightness = 0.299 * avgRed + 0.587 * avgGreen + 0.114 * avgBlue;

        if (brightness > 0.75) {
            double reductionFactor = 0.75 / brightness;
            avgRed *= reductionFactor;
            avgGreen *= reductionFactor;
            avgBlue *= reductionFactor;
        }
        avgRed = Math.max(0, Math.min(1, avgRed));
        avgGreen = Math.max(0, Math.min(1, avgGreen));
        avgBlue = Math.max(0, Math.min(1, avgBlue));

        return Color.color(avgRed, avgGreen, avgBlue);
    }

    public int getCardId() {
        return _id;
    }

    public static void shutdownExecutor() {
        imageProcessingExecutor.shutdown();
        try {
            if (!imageProcessingExecutor.awaitTermination(800, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                imageProcessingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            imageProcessingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}