package com.esa.moviestar.movie_view;

import com.esa.moviestar.Main;
import com.esa.moviestar.home.MainPagesController;
import com.esa.moviestar.libraries.TMDbApiManager;
import com.esa.moviestar.model.FilmSeriesDetails;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilmSceneController {

    public VBox episodesSectionVBox;
    public VBox backgroundVBox;
    public VBox mainVBox;
    public ImageView heroImageView;
    public Label runtimeOrSeasons;
    MainPagesController mainPagesController;
    private TMDbApiManager apiManager;
    private FilmSeriesDetails currentContent;

    // Main containers
    @FXML
    public StackPane background;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public VBox episodesList;

    // Buttons
    @FXML
    private StackPane closeButton;
    @FXML
    private HBox playButton;
    @FXML
    private HBox addToWatchListButton;
    @FXML
    private HBox addToFavouriteButton;

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

    // Season selection components
    private HBox seasonsContainer;
    private Button seasonsDropdownButton;
    private VBox seasonsDropdownMenu;
    private boolean isDropdownOpen = false;
    private int currentSeasonIndex = 0;
    private int currentSeriesId = 0; // Store series ID for loading additional seasons

    // Event filter for closing dropdown
    private javafx.event.EventHandler<javafx.scene.input.MouseEvent> closeDropdownFilter;


    public void initialize() {
        apiManager = TMDbApiManager.getInstance();

        // Set button actions
        if (closeButton != null) {
            closeButton.setOnMouseClicked(event -> closeView());
        }
        if (playButton != null) {
            playButton.setOnMouseClicked(event -> playContent());
        }
        if (addToWatchListButton != null) {
            addToWatchListButton.setOnMouseClicked(event -> addToList());
        }
        if (addToFavouriteButton != null) {
            addToFavouriteButton.setOnMouseClicked(event -> showInfo());
        }

        System.out.println("FilmSceneController initialized.");
    }

    /**
     * Main method to load content data from TMDb API
     *
     * @param contentId The TMDb ID of the content
     * @param isMovie   true for movie, false for TV series
     */
    public void loadContent(int contentId, boolean isMovie) {
        System.out.println("Loading " + (isMovie ? "movie" : "TV series") + " with ID: " + contentId);

        // Show loading state
        showLoadingState();

        if (isMovie) {
            loadMovieData(contentId);
        } else {
            currentSeriesId = contentId; // Store series ID
            loadTVSeriesData(contentId);
        }
    }

    private void loadMovieData(int movieId) {
        String endpoint = "/movie/" + movieId + "?append_to_response=credits";

        apiManager.makeRequestAsync(endpoint)
                .thenAccept(jsonString -> {
                    Platform.runLater(() -> {
                        try {
                            JsonObject movieJson = JsonParser.parseString(jsonString).getAsJsonObject();
                            currentContent = parseMovieData(movieJson);
                            displayMovieContent();
                        } catch (Exception e) {
                            System.err.println("Error parsing movie data: " + e.getMessage());
                            e.printStackTrace();
                            showErrorState();
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println("Error loading movie data: " + ex.getMessage());
                        ex.printStackTrace();
                        showErrorState();
                    });
                    return null;
                });
    }

    private void loadTVSeriesData(int seriesId) {
        String seriesEndpoint = "/tv/" + seriesId + "?append_to_response=credits";

        apiManager.makeRequestAsync(seriesEndpoint)
                .thenAccept(seriesJsonString -> {
                    Platform.runLater(() -> {
                        try {
                            JsonObject seriesJson = JsonParser.parseString(seriesJsonString).getAsJsonObject();
                            currentContent = parseSeriesData(seriesJson);
                            initializeAllSeasons();
                            displaySeriesContent();
                        } catch (Exception e) {
                            System.err.println("Error parsing series data: " + e.getMessage());
                            e.printStackTrace();
                            showErrorState();
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println("Error loading series data: " + ex.getMessage());
                        ex.printStackTrace();
                        showErrorState();
                    });
                    return null;
                });
    }

    private FilmSeriesDetails parseMovieData(JsonObject movieJson) {
        FilmSeriesDetails movie = new FilmSeriesDetails();

        movie.setId(getIntegerOrNull(movieJson, "id", 0));
        movie.setTitle(getStringOrNull(movieJson, "title"));
        movie.setPlot(getStringOrNull(movieJson, "overview"));
        movie.setPosterUrl(apiManager.getImageUrl(getStringOrNull(movieJson, "poster_path"), "w500"));
        movie.setBackdropUrl(apiManager.getImageUrl(getStringOrNull(movieJson, "backdrop_path"), "w1280"));
        movie.setReleaseDate(getStringOrNull(movieJson, "release_date"));
        movie.setMovieRuntime(getIntegerOrNull(movieJson, "runtime", 0));
        movie.setVideoUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4");

        if (movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4) {
            try {
                movie.setYear(Integer.parseInt(movie.getReleaseDate().substring(0, 4)));
            } catch (NumberFormatException e) {
                System.err.println("Could not parse year for movie.");
            }
        }

        movie.setSeries(false);

        List<String> genreNames = new ArrayList<>();
        if (movieJson.has("genres") && movieJson.get("genres").isJsonArray()) {
            for (JsonElement genreEl : movieJson.getAsJsonArray("genres")) {
                genreNames.add(getStringOrNull(genreEl.getAsJsonObject(), "name"));
            }
        }
        movie.setGenreNames(genreNames);

        if (movieJson.has("production_companies") && movieJson.get("production_companies").isJsonArray()) {
            JsonArray prodArray = movieJson.getAsJsonArray("production_companies");
            if (!prodArray.isEmpty()) {
                movie.setProductionName(getStringOrNull(prodArray.get(0).getAsJsonObject(), "name"));
            }
        }

        if (movieJson.has("credits") && movieJson.getAsJsonObject("credits").has("cast")) {
            JsonArray castArray = movieJson.getAsJsonObject("credits").getAsJsonArray("cast");
            List<String> castNames = new ArrayList<>();
            for (int i = 0; i < castArray.size() && i < 5; i++) {
                castNames.add(getStringOrNull(castArray.get(i).getAsJsonObject(), "name"));
            }
            movie.setCast(String.join(", ", castNames));
        }
        return movie;
    }

    private FilmSeriesDetails parseSeriesData(JsonObject seriesJson) {
        FilmSeriesDetails series = new FilmSeriesDetails();

        series.setId(getIntegerOrNull(seriesJson, "id", 0));
        series.setTitle(getStringOrNull(seriesJson, "name"));
        series.setPlot(getStringOrNull(seriesJson, "overview"));
        series.setPosterUrl(apiManager.getImageUrl(getStringOrNull(seriesJson, "poster_path"), "w500"));
        series.setBackdropUrl(apiManager.getImageUrl(getStringOrNull(seriesJson, "backdrop_path"), "w1280"));
        series.setReleaseDate(getStringOrNull(seriesJson, "first_air_date"));

        if (series.getReleaseDate() != null && series.getReleaseDate().length() >= 4) {
            try {
                series.setYear(Integer.parseInt(series.getReleaseDate().substring(0, 4)));
            } catch (NumberFormatException e) {
                System.err.println("Could not parse year for series.");
            }
        }

        series.setSeries(true);
        series.setNumberOfSeasons(getIntegerOrNull(seriesJson, "number_of_seasons", 0));

        List<String> genreNames = new ArrayList<>();
        if (seriesJson.has("genres") && seriesJson.get("genres").isJsonArray()) {
            for (JsonElement genreEl : seriesJson.getAsJsonArray("genres")) {
                genreNames.add(getStringOrNull(genreEl.getAsJsonObject(), "name"));
            }
        }
        series.setGenreNames(genreNames);

        if (seriesJson.has("production_companies") && seriesJson.get("production_companies").isJsonArray()) {
            JsonArray prodArray = seriesJson.getAsJsonArray("production_companies");
            if (!prodArray.isEmpty()) {
                series.setProductionName(getStringOrNull(prodArray.get(0).getAsJsonObject(), "name"));
            }
        }

        if (seriesJson.has("credits") && seriesJson.getAsJsonObject("credits").has("cast")) {
            JsonArray castArray = seriesJson.getAsJsonObject("credits").getAsJsonArray("cast");
            List<String> castNames = new ArrayList<>();
            for (int i = 0; i < castArray.size() && i < 5; i++) {
                castNames.add(getStringOrNull(castArray.get(i).getAsJsonObject(), "name"));
            }
            series.setCast(String.join(", ", castNames));
        }
        return series;
    }

    private FilmSeriesDetails.SeasonDetails parseSeasonData(JsonObject seasonJson) {
        FilmSeriesDetails.SeasonDetails season = new FilmSeriesDetails.SeasonDetails();

        season.setSeasonNumber(getIntegerOrNull(seasonJson, "season_number", 0));
        season.setName(getStringOrNull(seasonJson, "name"));
        season.setOverview(getStringOrNull(seasonJson, "overview"));
        season.setPosterUrl(apiManager.getImageUrl(getStringOrNull(seasonJson, "poster_path"), "w300"));
        season.setAirDate(getStringOrNull(seasonJson, "air_date"));

        if (seasonJson.has("episodes") && seasonJson.get("episodes").isJsonArray()) {
            for (JsonElement epEl : seasonJson.getAsJsonArray("episodes")) {
                JsonObject epJson = epEl.getAsJsonObject();
                FilmSeriesDetails.EpisodeDetails episode = new FilmSeriesDetails.EpisodeDetails();

                episode.setEpisodeNumber(getIntegerOrNull(epJson, "episode_number", 0));
                episode.setName(getStringOrNull(epJson, "name"));
                episode.setOverview(getStringOrNull(epJson, "overview"));
                episode.setStillUrl(apiManager.getImageUrl(getStringOrNull(epJson, "still_path"), "w300"));
                episode.setRuntimeMinutes(getIntegerOrNull(epJson, "runtime", 0));

                season.addEpisode(episode);
            }
        }
        return season;
    }

    private String truncateText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        // If "..." is already present, assume it's suitably truncated or meant to be displayed as is.
        if (text.contains("...")) {
            return text;
        }

        int firstPeriod = text.indexOf('.');
        if (firstPeriod == -1) {
            return text; // No periods, return original
        }

        int secondPeriod = text.indexOf('.', firstPeriod + 1);
        if (secondPeriod == -1) {
            return text; // Only one period, return original
        }
        // Return the substring up to and including the second period
        return text.substring(0, secondPeriod + 1);
    }

    private void displayMovieContent() {
        if (currentContent == null) return;

        setTitle(currentContent.getTitle());
        setYear(String.valueOf(currentContent.getYear()));
        setMovieRuntime(currentContent.getMovieRuntime());
        setDescription(truncateText(currentContent.getPlot()));
        setCast(currentContent.getCast());
        setGenres(String.join(", ", currentContent.getGenreNames()));

        if (showTypeLabel != null) showTypeLabel.setText("MOVIE");
        hideSeriesSpecificUI();
        loadBackdrop(currentContent.getBackdropUrl());
        System.out.println("Movie content displayed: " + currentContent.getTitle());
    }

    private void displaySeriesContent() {
        if (currentContent == null) return;

        setTitle(currentContent.getTitle());
        setYear(String.valueOf(currentContent.getYear()));
        setDescription(truncateText(currentContent.getPlot()));
        setCast(currentContent.getCast());
        setGenres(String.join(", ", currentContent.getGenreNames()));
        setSeasonsNumber(currentContent.getNumberOfSeasons());


        if (showTypeLabel != null) showTypeLabel.setText("TV SERIES");
        if (seriesTitleLabel != null) {
            seriesTitleLabel.setText(currentContent.getTitle());
            seriesTitleLabel.setVisible(true);
            seriesTitleLabel.setManaged(true);
        }

        showSeriesSpecificUI();
        loadBackdrop(currentContent.getBackdropUrl());
        System.out.println("Series content displayed: " + currentContent.getTitle());
    }

    private void showSeriesSpecificUI() {
        if (episodesSectionVBox != null) {
            episodesSectionVBox.setVisible(true);
            episodesSectionVBox.setManaged(true);
        }
        createSeasonsSelector();
        updateEpisodesDisplay();
    }

    private void hideSeriesSpecificUI() {
        removeCloseDropdownFilter();
        if (seasonsContainer != null) {
            seasonsContainer.setVisible(false);
            seasonsContainer.setManaged(false);
        }
        if (episodesList != null) {
            episodesList.getChildren().clear();
        }
        if (episodesSectionVBox != null) {
            episodesSectionVBox.setVisible(false);
            episodesSectionVBox.setManaged(false);
        }
        if (episodesLabel != null) {
            episodesLabel.setVisible(false);
            episodesLabel.setManaged(false);
        }
        if (seriesTitleLabel != null) {
            seriesTitleLabel.setVisible(false);
            seriesTitleLabel.setManaged(false);
        }
    }

    private void createSeasonsSelector() {
        if (episodesList == null || !(episodesList.getParent() instanceof VBox)) {
            System.err.println("Cannot create seasons selector: episodesList is null or its parent is not a VBox.");
            return;
        }

        VBox episodesSectionParent = episodesSectionVBox;
        if (episodesSectionParent == null) {
            System.err.println("Cannot create seasons selector: episodesSectionVBox is null.");
            return;
        }
        if (seasonsContainer == null) {
            seasonsContainer = new HBox();
            seasonsContainer.setAlignment(Pos.CENTER_LEFT);
            seasonsContainer.setSpacing(10);
            seasonsContainer.setStyle("-fx-padding: 0 0 15 0;");

            seasonsDropdownButton = new Button();
            seasonsDropdownButton.getStyleClass().addAll("seasons-dropdown-button","small-item","on-primary");
            seasonsDropdownButton.setPrefHeight(35);
            seasonsDropdownButton.setOnAction(e -> popUpSeasonContainer());
            if (currentContent.getSeasons() != null && !currentContent.getSeasons().isEmpty() && currentContent.getSeasons().get(0) != null) {
                FilmSeriesDetails.SeasonDetails season = currentContent.getSeasons().get(0);
                if (season.getName() != null && !season.getName().isEmpty()) {
                    seasonsDropdownButton.setText(season.getName());
                }
            } else {
                seasonsDropdownButton.setText("Season " + (1));
            }

            seasonsDropdownMenu = new VBox();
            seasonsDropdownMenu.getStyleClass().addAll("seasons-dropdown-menu","small-item","on-primary");
            seasonsDropdownMenu.setVisible(false);
            seasonsDropdownMenu.setManaged(false);
            seasonsDropdownMenu.setSpacing(2);
            seasonsDropdownMenu.setMaxHeight(200);

            StackPane dropdownWrapper = new StackPane(seasonsDropdownButton, seasonsDropdownMenu);
            StackPane.setAlignment(seasonsDropdownMenu, Pos.TOP_LEFT);
            seasonsDropdownMenu.setTranslateY(37);

            seasonsContainer.getChildren().add(dropdownWrapper);
        }

        if (!episodesSectionParent.getChildren().contains(seasonsContainer)) {
            int insertIndex = episodesSectionParent.getChildren().indexOf(episodesList);
            if (insertIndex == -1) insertIndex = 0;
            episodesSectionParent.getChildren().add(insertIndex, seasonsContainer);
        }

        seasonsContainer.setVisible(true);
        seasonsContainer.setManaged(true);
    }

    private void popUpSeasonContainer(){
        Popup popup = new Popup();
        StackPane stackPane = new StackPane();
        stackPane.getStylesheets().add(getClass().getResource("/com/esa/moviestar/styles/general.css").toExternalForm());
        stackPane.setPadding(new Insets(8));
        stackPane.getStyleClass().addAll("medium-item","surface-dim");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(200);
        VBox vbox = new VBox();

        for (int i = 0; i < currentContent.getNumberOfSeasons(); i++) {
            String seasonName = "Season " + (i + 1);
            if (currentContent.getSeasons() != null && i < currentContent.getSeasons().size() && currentContent.getSeasons().get(i) != null) {
                FilmSeriesDetails.SeasonDetails season = currentContent.getSeasons().get(i);
                if (season.getName() != null && !season.getName().isEmpty()) {
                    seasonName = season.getName();
                }
            } else {
                seasonName = "Season " + (i + 1);
            }

            Button seasonOptionButton = new Button(seasonName);
            seasonOptionButton.getStyleClass().addAll("season-option-button","small-item","on-primary");
            seasonOptionButton.setPrefWidth(256);
            seasonOptionButton.setPrefHeight(50);
            final int seasonIdx = i;
            seasonOptionButton.setOnAction(e -> {selectSeason(seasonIdx); popup.hide();});
            if (i == currentSeasonIndex) {
                seasonOptionButton.getStyleClass().addAll("selected-season");
            }
            vbox.getChildren().add(seasonOptionButton);
        }
        scroll.setContent(vbox);
        stackPane.getChildren().add(scroll);
        popup.getContent().add(stackPane);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        double anchorX = seasonsContainer.localToScreen(0, 0).getX();
        double anchorY = seasonsContainer.localToScreen(0, 8).getY();
        double posY = anchorY + seasonsContainer.getBoundsInLocal().getHeight();
        popup.show(seasonsContainer.getScene().getWindow(), anchorX, posY);

    }




    private void selectSeason(int seasonIndex) {
        if (currentContent == null || seasonIndex < 0 || seasonIndex >= currentContent.getNumberOfSeasons()) {
            return;
        }
        currentSeasonIndex = seasonIndex;
        updateEpisodesDisplay();
    }

    private void updateEpisodesDisplay() {
        if (episodesList == null) {
            System.err.println("episodesList VBox is null, cannot update display.");
            return;
        }
        episodesList.getChildren().clear();

        if (currentContent == null || !currentContent.isSeries()) {
            if (episodesLabel != null) episodesLabel.setText("No episodes to display.");
            return;
        }

        if (currentContent.getSeasons() == null ||
                currentSeasonIndex >= currentContent.getSeasons().size() ||
                currentContent.getSeasons().get(currentSeasonIndex) == null) {
            if (episodesLabel != null) episodesLabel.setText("Loading episodes...");
            return;
        }

        FilmSeriesDetails.SeasonDetails currentSeason = currentContent.getSeasons().get(currentSeasonIndex);
        List<FilmSeriesDetails.EpisodeDetails> episodes = currentSeason.getEpisodes();

        if (episodes != null && !episodes.isEmpty()) {
            for (FilmSeriesDetails.EpisodeDetails episode : episodes) {
                HBox episodeItem = createEpisodeItem(episode);
                episodesList.getChildren().add(episodeItem);
            }
            if (episodesLabel != null) {
                episodesLabel.setText(episodes.size() + (episodes.size() == 1 ? " episode" : " episodes"));
                episodesLabel.setVisible(true);
                episodesLabel.setManaged(true);
            }
        } else {
            if (episodesLabel != null) {
                episodesLabel.setText("No episodes in this season.");
                episodesLabel.setVisible(true);
                episodesLabel.setManaged(true);
            }
        }
    }


    private void loadEpisodeThumbnail(String episodeImageUrl, ImageView imageView) {
        if (episodeImageUrl != null && !episodeImageUrl.isEmpty() && !episodeImageUrl.endsWith("null")) {
            try {
                imageView.getStyleClass().add("episode-thumbnail-loading");
                Image image = new Image(episodeImageUrl, true);
                imageView.setImage(image);

                image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                    if (newProgress.doubleValue() == 1.0) {
                        imageView.getStyleClass().remove("episode-thumbnail-loading");
                        imageView.getStyleClass().add("episode-thumbnail-loaded");
                    }
                });

                image.errorProperty().addListener((obs, wasError, isError) -> {
                    if (isError) {
                        System.err.println("Failed to load episode image: " + episodeImageUrl);
                        imageView.getStyleClass().remove("episode-thumbnail-loading");
                        imageView.getStyleClass().add("episode-thumbnail-error");
                        // Fallback to main content poster on error
                        loadFallbackThumbnail(imageView);
                    }
                });
            } catch (Exception e) {
                System.err.println("Error creating image object for episode: " + episodeImageUrl + " - " + e.getMessage());
                imageView.getStyleClass().add("episode-thumbnail-error");
                // Fallback to main content poster on error
                loadFallbackThumbnail(imageView);
            }
        } else { // Episode still_path is null, empty, or invalid
            System.out.println("Episode image URL is null, empty, or invalid. Attempting to load main content poster as fallback.");
            imageView.getStyleClass().add("episode-thumbnail-missing");
            loadFallbackThumbnail(imageView);
        }
    }


    private void loadFallbackThumbnail(ImageView imageView) {
        if (currentContent != null && currentContent.getPosterUrl() != null && !currentContent.getPosterUrl().isEmpty() && !currentContent.getPosterUrl().endsWith("null")) {
            try {
                // Remove any previous state classes before adding fallback/loaded
                imageView.getStyleClass().remove("episode-thumbnail-loading");
                imageView.getStyleClass().remove("episode-thumbnail-error");
                imageView.getStyleClass().remove("episode-thumbnail-missing");
                // Optional: Add a specific class for fallback styling if you want different appearance
                // imageView.getStyleClass().add("episode-thumbnail-fallback");

                Image fallbackImage = new Image(currentContent.getPosterUrl(), true);
                imageView.setImage(fallbackImage);

                fallbackImage.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                    if (newProgress.doubleValue() == 1.0) {
                        // imageView.getStyleClass().remove("episode-thumbnail-fallback"); // Remove fallback class once loaded
                        imageView.getStyleClass().add("episode-thumbnail-loaded"); // Add loaded class
                    }
                });

                fallbackImage.errorProperty().addListener((obs, wasError, isError) -> {
                    if (isError) {
                        System.err.println("Failed to load fallback image (main poster): " + currentContent.getPosterUrl());
                        // imageView.getStyleClass().remove("episode-thumbnail-fallback");
                        imageView.getStyleClass().add("episode-thumbnail-error"); // Revert to error state if fallback fails
                    }
                });

            } catch (Exception e) {
                System.err.println("Error creating fallback image object: " + currentContent.getPosterUrl() + " - " + e.getMessage());
                imageView.getStyleClass().add("episode-thumbnail-error"); // Revert to error state if fallback creation fails
            }
        } else {
            System.err.println("Main content poster URL is also null, empty, or invalid. No fallback image available.");
            // Keep the "episode-thumbnail-missing" or "episode-thumbnail-error" class already set
        }
    }


    private HBox createEpisodeItem(FilmSeriesDetails.EpisodeDetails episode) {
        HBox episodeItem = new HBox();
        episodeItem.setAlignment(Pos.CENTER_LEFT);
        episodeItem.setSpacing(15);
        episodeItem.getStyleClass().add("episode-item");

        Label episodeNumber = new Label(String.valueOf(episode.getEpisodeNumber()));
        episodeNumber.getStyleClass().addAll("episode-number","on-primary");
        episodeNumber.setTextOverrun(OverrunStyle.CLIP);


        ImageView episodeThumbnail = new ImageView();
        episodeThumbnail.setFitWidth(120);
        episodeThumbnail.setFitHeight(70);
        episodeThumbnail.setPreserveRatio(true);
        episodeThumbnail.getStyleClass().add("episode-thumbnail-image");
        // Use the updated loadEpisodeThumbnail which includes fallback logic
        loadEpisodeThumbnail(episode.getStillUrl(), episodeThumbnail);

        VBox episodeDetails = new VBox(5);
        HBox.setHgrow(episodeDetails, Priority.ALWAYS);

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label episodeTitle = new Label(episode.getName() != null ? episode.getName() : "Title not available");
        episodeTitle.getStyleClass().addAll("section-title","on-primary");
        episodeTitle.setMaxWidth(Double.MAX_VALUE);

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        String duration = episode.getRuntimeMinutes() > 0 ? episode.getRuntimeMinutes() + "min" : "";
        Label episodeDuration = new Label(duration);
        episodeDuration.getStyleClass().add("on-primary");

        titleRow.getChildren().addAll(episodeTitle, titleSpacer, episodeDuration);

        String overview = episode.getOverview();
        String displayOverview = (overview != null && !overview.isEmpty()) ? truncateText(overview) : "";
        Label episodeDescription = new Label(displayOverview);
        episodeDescription.getStyleClass().addAll("episode-description","on-primary");
        episodeDescription.setWrapText(true);
        episodeDescription.setMaxHeight(40); // Keep existing max height

        episodeDetails.getChildren().addAll(titleRow, episodeDescription);
        episodeItem.setOnMouseClicked(e -> playEpisode(episode));

        episodeItem.getChildren().addAll(episodeNumber, episodeThumbnail, episodeDetails);

        return episodeItem;
    }

    private void playEpisode(FilmSeriesDetails.EpisodeDetails episode) {
        openPlayerScene();
    }

    private void loadBackdrop(String backdropUrl) {
        if (backdropUrl != null && !backdropUrl.isEmpty() && !backdropUrl.endsWith("null")) {
            try {
                // Background loading (true) is good for UI responsiveness
                Image image = new Image(backdropUrl, true);
                heroImageView.setImage(image);
                heroImageView.setPreserveRatio(false);
                heroImageView.setFitWidth(backgroundVBox.getWidth());
                heroImageView.setFitHeight(backgroundVBox.getPrefHeight());

                // The following lines are no longer needed due to FXML bindings:
                // heroImageView.setFitWidth(backgroundVBox.getWidth());
                // heroImageView.setFitHeight(backgroundVBox.getPrefHeight());

            } catch (IllegalArgumentException e) {
                // This can happen if the URL string is malformed
                System.err.println("Invalid image URL format for backdrop: " + backdropUrl + " - " + e.getMessage());
                // heroImageView.setImage(null); // or some default error image
            } catch (Exception e) {
                // Catch any other unexpected errors during image instantiation or setting
                System.err.println("Failed to load backdrop image: " + backdropUrl + " - " + e.getMessage());
                e.printStackTrace(); // Good for debugging
                // heroImageView.setImage(null); // or some default error image
            }
        } else {
            System.err.println("Backdrop URL is null, empty, or invalid: " + backdropUrl);
            // heroImageView.setImage(null); // Clear image or set a default placeholder
        }
    }

    private void showLoadingState() {

        if (episodesList != null) episodesList.getChildren().clear();
        hideSeriesSpecificUI();
    }

    private void showErrorState() {
        if (episodesList != null) episodesList.getChildren().clear();
        hideSeriesSpecificUI();
    }

    public void setProperties(ImageView newBackgroundImageView, MainPagesController mainPagesController) {
        if (newBackgroundImageView == null || background == null) {
            System.err.println("Error: Background ImageView or StackPane is null for setProperties.");
            return;
        }
        this.mainPagesController = mainPagesController;
        background.getChildren().removeIf(node -> node instanceof ImageView && "mainBlurredBackground".equals(node.getId()));

        newBackgroundImageView.setId("mainBlurredBackground");
        newBackgroundImageView.setEffect(setColorBackground());
        background.getChildren().addFirst(setImageBackground(newBackgroundImageView));
    }

    private ImageView setImageBackground(ImageView newImgView) {
        if (background != null) {
            newImgView.fitWidthProperty().bind(background.widthProperty());
            newImgView.fitHeightProperty().bind(background.heightProperty());
        }
        newImgView.setPreserveRatio(false);
        return newImgView;
    }

    private ColorAdjust setColorBackground() {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.65);
        colorAdjust.setSaturation(-0.3);
        GaussianBlur blur = new GaussianBlur(10);
        colorAdjust.setInput(blur);
        return colorAdjust;
    }

    @FXML
    private void closeView() {
        System.out.println("Close button clicked");
        removeCloseDropdownFilter();
        clearContent();

        if (mainPagesController != null) {
            mainPagesController.restorePreviousScene();
        }
    }

    @FXML
    private void playContent() {
        System.out.println("Play button clicked");
        if (currentContent != null) {
            if (currentContent.isSeries()) {
                if (currentContent.getSeasons() != null &&
                        currentSeasonIndex < currentContent.getSeasons().size() &&
                        currentContent.getSeasons().get(currentSeasonIndex) != null) {
                    FilmSeriesDetails.SeasonDetails currentSeason = currentContent.getSeasons().get(currentSeasonIndex);
                    if (currentSeason.getEpisodes() != null && !currentSeason.getEpisodes().isEmpty()) {
                        playEpisode(currentSeason.getEpisodes().getFirst());
                    } else {
                        System.out.println("No episodes found for the current season.");
                    }
                } else {
                    System.out.println("Current season data not loaded yet.");
                }
            } else {
                openPlayerScene();
            }
        }
    }

    private void openPlayerScene() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/movie_view/FilmPlayer.fxml"), Main.resourceBundle);
                Scene currentSceneNode = (background != null && background.getScene() != null) ? background.getScene() : null;
                if (currentSceneNode == null) return;
                Parent scene = loader.load();
                ((FilmPlayer)loader.getController()).initializePlayer(currentContent.getVideoUrl());
                ((FilmPlayer)loader.getController()).play();
                Scene newScene = new Scene(scene, currentSceneNode.getWidth(), currentSceneNode.getHeight());
                Stage stage = (Stage) currentSceneNode.getWindow();
                if (stage != null) stage.setScene(newScene);
                else System.err.println("FilmSceneController: Stage is null.");

            } catch (IOException e) {
                System.err.println("\"FilmSceneController: Error to load player, message: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void addToList() {
        System.out.println("Add to list button clicked");
        if (currentContent != null) {
            System.out.println("Adding to list: " + currentContent.getTitle());
            // TODO: Implement add to list functionality
        }
    }

    @FXML
    private void showInfo() {
        System.out.println("Info button clicked");
        if (currentContent != null) {
            System.out.println("Showing info for: " + currentContent.getTitle());
            // TODO: Implement info display
        }
    }

    public void setTitle(String title) {
        if (titleLabel != null) titleLabel.setText(title != null ? title : "Unknown Title");
    }

    public void setMovieRuntime(int movieRuntime){
        if(movieRuntime!=0){
            runtimeOrSeasons.setText(movieRuntime+"min");
        }
    }

    public void setSeasonsNumber(int seasonsNumber){
        if(seasonsNumber !=0){
            runtimeOrSeasons.setText(seasonsNumber +" Seasons");
        }
    }


    public void setDescription(String description) { // Already expects potentially truncated text
        if (descriptionLabel != null)
            descriptionLabel.setText(description != null && !description.isEmpty() ? description : "");
    }

    public void setYear(String year) {
        if (yearLabel != null) yearLabel.setText(year != null ? year : "Unknown Year");
    }

    public void setCast(String cast) {
        if (castLabel != null) castLabel.setText(cast != null && !cast.isEmpty() ? cast : "Not available");
    }

    public void setGenres(String genres) {
        if (genresLabel != null) genresLabel.setText(genres != null && !genres.isEmpty() ? genres : " Not available");
    }

    private String getStringOrNull(JsonObject jsonObject, String memberName) {
        if (jsonObject != null && jsonObject.has(memberName) && !jsonObject.get(memberName).isJsonNull()) {
            JsonElement element = jsonObject.get(memberName);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                return element.getAsString();
            }
        }
        return null;
    }

    private Integer getIntegerOrNull(JsonObject jsonObject, String memberName, int defaultValue) {
        if (jsonObject != null && jsonObject.has(memberName) && !jsonObject.get(memberName).isJsonNull()) {
            JsonElement element = jsonObject.get(memberName);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                return element.getAsInt();
            }
        }
        return defaultValue;
    }

    public void clearContent() {
        currentContent = null;
        currentSeasonIndex = 0;
        isDropdownOpen = false;
        removeCloseDropdownFilter();

        if (titleLabel != null) titleLabel.setText("");
        if (descriptionLabel != null) descriptionLabel.setText("");
        if (yearLabel != null) yearLabel.setText("");
        if (castLabel != null) castLabel.setText("");
        if (genresLabel != null) genresLabel.setText("");
        if (episodesLabel != null) episodesLabel.setText("");
        if (showTypeLabel != null) showTypeLabel.setText("");
        if (seriesTitleLabel != null) seriesTitleLabel.setText("");

        if (episodesList != null) episodesList.getChildren().clear();
        if (backgroundVBox != null) {
            backgroundVBox.getChildren().removeIf(node -> node instanceof ImageView && node.getId() == null);
        }
        hideSeriesSpecificUI();
    }

    private void initializeAllSeasons() {
        if (currentContent == null || !currentContent.isSeries()) return;
        int totalSeasons = currentContent.getNumberOfSeasons();
        if (totalSeasons <= 0) {
            System.out.println("No seasons reported for this series.");
            updateEpisodesDisplay();
            return;
        }
        currentContent.initializeSeasonsList(totalSeasons);
        System.out.println("Starting asynchronous loading for " + totalSeasons + " seasons.");

        for (int i = 1; i <= totalSeasons; i++) {
            final int seasonNumber = i;
            String seasonEndpoint = "/tv/" + currentSeriesId + "/season/" + seasonNumber;
            apiManager.makeRequestAsync(seasonEndpoint)
                    .thenAccept(seasonJsonString -> {
                        Platform.runLater(() -> {
                            try {
                                JsonObject seasonJson = JsonParser.parseString(seasonJsonString).getAsJsonObject();
                                FilmSeriesDetails.SeasonDetails seasonDetails = parseSeasonData(seasonJson);
                                currentContent.setSeasonAtIndex(seasonNumber - 1, seasonDetails);
                                System.out.println("Season " + seasonNumber + " ('" + seasonDetails.getName() + "') loaded.");

                                if (currentSeasonIndex == seasonNumber - 1 || isDropdownOpen) {
                                    updateEpisodesDisplay();
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing season " + seasonNumber + " data: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        System.err.println("Error loading season " + seasonNumber + ": " + ex.getMessage());
                        ex.printStackTrace();
                        if (currentSeasonIndex == seasonNumber - 1) {
                            Platform.runLater(this::updateEpisodesDisplay);
                        }
                        return null;
                    });
        }
    }

    private void removeCloseDropdownFilter() {
        if (background != null && closeDropdownFilter != null) {
            background.removeEventFilter(MouseEvent.MOUSE_CLICKED, closeDropdownFilter);
        }
    }

    private boolean isNodeInside(Node node, Region container) {
        if (node == null || container == null) return false;
        Node current = node;
        while (current != null) {
            if (current.equals(container)) return true;
            current = current.getParent();
        }
        return false;
    }

    public void setMainPagesController(MainPagesController controller) {
        this.mainPagesController = controller;
    }

    public FilmSeriesDetails getCurrentContent() {
        return currentContent;
    }

    public boolean isLoading() {
        return titleLabel != null && "Loading...".equals(titleLabel.getText());
    }
}