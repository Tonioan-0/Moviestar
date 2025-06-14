package com.esa.moviestar.movie_view;

import com.esa.moviestar.Main;
import com.esa.moviestar.database.UserDao;
import com.esa.moviestar.home.MainPagesController;
import com.esa.moviestar.libraries.TMDbApiManager;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.FilmSeriesDetails;
import com.esa.moviestar.model.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
    public StackPane styleFavouriteButton;
    public StackPane styleWatchListButton;
    MainPagesController mainPagesController;
    private TMDbApiManager apiManager;
    private FilmSeriesDetails currentContent;
    private User user;
    private Account account;
    // The api doesn't provide the entire film, to make owr app work we take this sample video
    private String VIDEO_PLACEHOLDER= "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4";
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
    private Button seasonsListButton;
    private VBox seasonsListMenu;
    private boolean isDropdownOpen = false;
    private int currentSeasonIndex = 0;
    private int currentSeriesId = 0; // Store series ID for loading additional seasons

    // Event filter for closing dropdown
    private javafx.event.EventHandler<javafx.scene.input.MouseEvent> closeDropdownFilter;
    private boolean isInFavourite= false;
    private boolean isInWatchList= false;


    public void initialize(){

        apiManager = TMDbApiManager.getInstance();

        closeButton.setOnMouseClicked(event -> closeView());
        playButton.setOnMouseClicked(event -> playContent());

        if (addToWatchListButton != null)

            addToWatchListButton.setOnMouseClicked(event ->{
                if(!isInWatchList){
                    addToWatchlist();

                    styleWatchListButton.getStyleClass().add( "watchlist-active");
                    styleWatchListButton.getStyleClass().remove( "surface-transparent");

                }
                else
                {
                    deleteFromWatchlist();
                    styleWatchListButton.getStyleClass().remove( "watchlist-active");
                    styleWatchListButton.getStyleClass().add( "surface-transparent");
                }
                isInWatchList = !isInWatchList;
            });
        if (addToFavouriteButton != null)
            addToFavouriteButton.setOnMouseClicked(event -> {
                if(!isInFavourite){

                    addToFavourites();
                    styleFavouriteButton.getStyleClass().add( "favourite-active");
                    styleFavouriteButton.getStyleClass().remove( "surface-transparent");
                }
                else
                {

                    deleteFromFavourites();
                    styleFavouriteButton.getStyleClass().remove( "favourite-active");
                    styleFavouriteButton.getStyleClass().add( "surface-transparent");


                }
                isInFavourite = !isInFavourite;
            });

    }

    //This function stores the data gotten from the json file given by the api
    private void loadMovieData(int idMovie){

        String endpoint = "/movie/" + idMovie + "?append_to_response=credits";
        //This point is crucial to get the actual information by the movie id in the apiManager
        apiManager.makeRequestAsync(endpoint)
                .thenAccept(jsonString ->{
                    Platform.runLater(() -> {

                        JsonObject movieJson = JsonParser.parseString(jsonString).getAsJsonObject();
                        currentContent = parseMovieData(movieJson);
                        displayMovieContent();

                        if(currentContent==null)
                            System.err.println("Error parsing movie data in loadMovieData");
                    });
                });
    }


    //Function to store the movie data we parsed into a real FilmSeriesDetail object
    private FilmSeriesDetails parseMovieData(JsonObject movieJsonInfo){

        FilmSeriesDetails movie = new FilmSeriesDetails();

        List<String> genreNames = new ArrayList<>();
        if(movieJsonInfo.has("genres") && movieJsonInfo.get("genres").isJsonArray()){
            for(JsonElement genreEl : movieJsonInfo.getAsJsonArray("genres"))
                genreNames.add(getStringOrNull(genreEl.getAsJsonObject(), "name"));
        }
        movie.setGenreNames(genreNames);


        if (movieJsonInfo.has("credits") && movieJsonInfo.getAsJsonObject("credits").has("cast")) {
            JsonArray castArray = movieJsonInfo.getAsJsonObject("credits").getAsJsonArray("cast");
            List<String> castNames = new ArrayList<>();
            for (int i = 0; i < castArray.size() && i < 5; i++) {
                castNames.add(getStringOrNull(castArray.get(i).getAsJsonObject(), "name"));
            }
            movie.setCast(String.join(", ", castNames));
        }
        movie.setContentId(getIntegerOrNull(movieJsonInfo, "id", 0));
        movie.setContentTitle(getStringOrNull(movieJsonInfo, "title"));
        movie.setPlot(getStringOrNull(movieJsonInfo, "overview"));

        movie.setPosterUrl(apiManager.getImageUrl(getStringOrNull(movieJsonInfo, "poster_path"), "w500"));
        movie.setBackdropUrl(apiManager.getImageUrl(getStringOrNull(movieJsonInfo, "backdrop_path"), "w1280"));

        movie.setReleaseDate(getStringOrNull(movieJsonInfo, "release_date"));
        if(movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4)
            movie.setYear(Integer.parseInt(movie.getReleaseDate().substring(0, 4)));
        movie.setMovieRuntime(getIntegerOrNull(movieJsonInfo, "runtime", 0));
        movie.setVideoUrl(VIDEO_PLACEHOLDER);

        /*Essential boolean setting so we can use a currentContent object and use this to see which obj
         is being displayed*/
        movie.setSeries(false);

        return movie;
    }


    //This function stores the data gotten from the json file given by the api
    private void loadTVSeriesData(int idSeries){

        String seriesEndpoint = "/tv/" + idSeries + "?append_to_response=credits";
        /*This point is crucial to get the actual information by the series id in the apiManager just like
        we do for movies*/
        apiManager.makeRequestAsync(seriesEndpoint)
                .thenAccept(seriesJsonString -> {
                    Platform.runLater(() -> {
                        JsonObject seriesJson = JsonParser.parseString(seriesJsonString).getAsJsonObject();
                        currentContent = parseSeriesData(seriesJson);
                        initializeAllSeasons();
                        displaySeriesContent();
                        if(currentContent==null)
                            System.err.println("Error parsing series data in loadSeriesData");
                    });
                });
    }


    //Function to store the movie data we parsed into a real FilmSeriesDetail object
    private FilmSeriesDetails parseSeriesData(JsonObject seriesJsonInfo){
        FilmSeriesDetails series = new FilmSeriesDetails();
        List<String> genreNames = new ArrayList<>();
        if (seriesJsonInfo.has("genres") && seriesJsonInfo.get("genres").isJsonArray()) {
            for (JsonElement genreEl : seriesJsonInfo.getAsJsonArray("genres")) {
                genreNames.add(getStringOrNull(genreEl.getAsJsonObject(), "name"));
            }
        }
        series.setGenreNames(genreNames);


        if (seriesJsonInfo.has("credits") && seriesJsonInfo.getAsJsonObject("credits").has("cast")) {
            JsonArray castArray = seriesJsonInfo.getAsJsonObject("credits").getAsJsonArray("cast");
            List<String> castNames = new ArrayList<>();
            for (int i = 0; i < castArray.size() && i < 5; i++) {
                castNames.add(getStringOrNull(castArray.get(i).getAsJsonObject(), "name"));
            }
            series.setCast(String.join(", ", castNames));
        }
        series.setContentId(getIntegerOrNull(seriesJsonInfo, "id", 0));
        series.setContentTitle(getStringOrNull(seriesJsonInfo, "name"));
        series.setPlot(getStringOrNull(seriesJsonInfo, "overview"));
        series.setPosterUrl(apiManager.getImageUrl(getStringOrNull(seriesJsonInfo, "poster_path"), "w500"));
        series.setBackdropUrl(apiManager.getImageUrl(getStringOrNull(seriesJsonInfo, "backdrop_path"), "w1280"));
        series.setReleaseDate(getStringOrNull(seriesJsonInfo, "first_air_date"));
        if (series.getReleaseDate() != null && series.getReleaseDate().length() >= 4)
            series.setYear(Integer.parseInt(series.getReleaseDate().substring(0, 4)));
        series.setVideoUrl(VIDEO_PLACEHOLDER);


        series.setSeries(true);
        series.setNumberOfSeasons(getIntegerOrNull(seriesJsonInfo, "number_of_seasons", 0));

        return series;
    }

    private FilmSeriesDetails.SeasonDetails parseSeasonData(JsonObject seasonJson){

        FilmSeriesDetails.SeasonDetails season = new FilmSeriesDetails.SeasonDetails();


        if (seasonJson.has("episodes") && seasonJson.get("episodes").isJsonArray()) {
            for (JsonElement epEl : seasonJson.getAsJsonArray("episodes")) {
                JsonObject epJson = epEl.getAsJsonObject();
                FilmSeriesDetails.EpisodeDetails episode = new FilmSeriesDetails.EpisodeDetails();

                episode.setEpisodeNumber(getIntegerOrNull(epJson, "episode_number", 0));
                episode.setName(getStringOrNull(epJson, "name"));
                episode.setOverview(getStringOrNull(epJson, "overview"));
                episode.setStillUrl(apiManager.getImageUrl(getStringOrNull(epJson, "still_path"), "w300"));
                episode.setRuntimeMinutes(getIntegerOrNull(epJson, "runtime", 0));
                episode.setVideoUrl(VIDEO_PLACEHOLDER);
                season.addEpisode(episode);
            }
        }
        season.setSeasonNumber(getIntegerOrNull(seasonJson, "season_number", 0));

        season.setAirDate(getStringOrNull(seasonJson, "air_date"));
        season.setName(getStringOrNull(seasonJson, "name"));
        season.setOverview(getStringOrNull(seasonJson, "overview"));

        season.setPosterUrl(apiManager.getImageUrl(getStringOrNull(seasonJson, "poster_path"), "w300"));

        return season;
    }

    //Some descriptions of episodes and general movie/series are too long so we need to truncate them properly
    private String truncateText(String text){
        if (text == null || text.isEmpty())return text;
        //Sometimes we hae multiple "..." basically this method truncates on the second "."
        if (text.contains("..."))return text;

        int firstPeriod = text.indexOf('.');
        if (firstPeriod == -1)return text;
        int secondPeriod = text.indexOf('.', firstPeriod + 1);
        if (secondPeriod == -1)return text;
        String subDescription = text.substring(0, secondPeriod + 1);

        return subDescription;
    }

    //Load initial content data depending on it's type
    public void loadContent(int idContent, boolean isMovie){
        if(!isMovie){
            currentSeriesId = idContent;
            loadTVSeriesData(idContent);

            System.out.println("TV Series content was loaded");
        }
        else
        {
            loadMovieData(idContent);
            System.out.println("Movie content was loaded");
        }
        showLoadingState();

        checkFavouriteAndWatchList(user, idContent);

    }

    private void setMovieContentUI(){
        setTitle(currentContent.getContentTitle());
        setYear(String.valueOf(currentContent.getYear()));
        setMovieRuntime(currentContent.getMovieRuntime());

        setCast(currentContent.getCast());
        setDescription(truncateText(currentContent.getPlot()));
        setGenres(String.join(", ", currentContent.getGenreNames()));

    }

    public void setSeriesContentUI(){
        seriesTitleLabel.setText(currentContent.getContentTitle());
        seriesTitleLabel.setVisible(true);
        seriesTitleLabel.setManaged(true);
        setTitle(currentContent.getContentTitle());
        setYear(String.valueOf(currentContent.getYear()));
        setDescription(truncateText(currentContent.getPlot()));
        setCast(currentContent.getCast());
        setGenres(String.join(", ", currentContent.getGenreNames()));
        setSeasonsNumber(currentContent.getNumberOfSeasons());

    }

    private void displayMovieContent() {
        if (currentContent == null) {
            System.err.println("Current content is null, cannot display movie content in (displayMovieContent)");
            return;
        }
        setMovieContentUI();

        showTypeLabel.setText("MOVIE");
        hideSeriesSpecificUI();
        loadBackdrop(currentContent.getBackdropUrl());
        System.out.println("Movie content displayed");
    }



    private void displaySeriesContent() {
        if (currentContent == null){
            System.err.println("Current content is null, cannot display series content in (displaySeriesContent)");
            return;
        }
        setSeriesContentUI();
        showTypeLabel.setText("TV SERIES");

        showSeriesSpecificUI();
        loadBackdrop(currentContent.getBackdropUrl());
        System.out.println("Series content displayed");
    }

    //Specific section for series it adds the bottom vbox by showing its seasons and episodes
    private void showSeriesSpecificUI(){
        episodesSectionVBox.setManaged(true);
        episodesSectionVBox.setVisible(true);

        createSeasonsSwitch();

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

    private void updateSeasonDropdownButtonText() {
        if (currentContent == null || !currentContent.isSeries()){
            System.err.println("Cannot update season dropdown button text");
            return;
        }
        String buttonText = "Season " + (currentSeasonIndex + 1);
        List<FilmSeriesDetails.SeasonDetails> seasons = currentContent.getSeasons();
        if (seasons != null && currentSeasonIndex >= 0 && currentSeasonIndex < seasons.size()) {
            FilmSeriesDetails.SeasonDetails season = seasons.get(currentSeasonIndex);
            if (season != null && season.getName() != null && !season.getName().isEmpty()) buttonText = season.getName();
        }

        seasonsListButton.setText(buttonText);
    }

    //Basic setting season objects
    private void setSeasonsContainer(){
        seasonsContainer.setAlignment(Pos.CENTER_LEFT);
        seasonsContainer.setSpacing(10);
        seasonsContainer.setStyle("-fx-padding: 0 0 15 0;");
    }
    private void setSeasonsListButton(){
        seasonsListButton.getStyleClass().addAll("seasons-dropdown-button","small-item","on-primary","primary-border");
        seasonsListButton.setPrefHeight(35);
        seasonsListButton.setOnAction(e -> popUpSeasonContainer());
    }
    private void setSeasonsListMenu(){
        seasonsListMenu.getStyleClass().addAll("seasons-dropdown-menu","small-item","on-primary");
        seasonsListMenu.setVisible(false);
        seasonsListMenu.setManaged(false);
        seasonsListMenu.setSpacing(2);
        seasonsListMenu.setMaxHeight(200);
    }

    private void createSeasonsSwitch() {

        VBox episodesSection = episodesSectionVBox;
        if(episodesSection == null){
            System.err.println("Cannot create seasons selector due to null section-vbox");
            return;
        }
        seasonsContainer = new HBox();
        setSeasonsContainer();

        seasonsListButton = new Button();
        setSeasonsListButton();

        seasonsListMenu = new VBox();
        setSeasonsListMenu();

        StackPane dropdownWrapper = new StackPane(seasonsListButton, seasonsListMenu);
        StackPane.setAlignment(seasonsListMenu, Pos.TOP_LEFT);
        seasonsListMenu.setTranslateY(37);

        seasonsContainer.getChildren().add(dropdownWrapper);


        if (!episodesSection.getChildren().contains(seasonsContainer)) {
            int insertIndex = episodesSection.getChildren().indexOf(episodesList);
            if (insertIndex == -1) insertIndex = 0;
            episodesSection.getChildren().add(insertIndex, seasonsContainer);
        }

        seasonsContainer.setVisible(true);
        seasonsContainer.setManaged(true);
        updateSeasonDropdownButtonText();
    }

    private void popUpSeasonContainer(){
        Popup popup = new Popup();
        StackPane stackPane = new StackPane();
        stackPane.getStylesheets().add(getClass().getResource("/com/esa/moviestar/styles/general.css").toExternalForm());
        stackPane.setPadding(new Insets(8));
        stackPane.getStyleClass().addAll("medium-item","surface-dim","primary-border");
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(200);
        VBox vbox = new VBox();
        vbox.setSpacing(2.0);
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
            seasonOptionButton.getStyleClass().addAll("surface-transparent","season-option-button","small-item","on-primary","transparent-border");
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
        if (currentContent == null || seasonIndex < 0 || seasonIndex >= currentContent.getNumberOfSeasons()){
            System.err.println("Cannot select season");
            return;
        }
        currentSeasonIndex = seasonIndex;

        updateSeasonDropdownButtonText();
        updateEpisodesDisplay();

    }

    private void checkAddingEpisode() {
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
        }
        else {
            if (episodesLabel != null) {

                episodesLabel.setText("No episodes in this season.");
                episodesLabel.setVisible(true);
                episodesLabel.setManaged(true);
            }
        }
    }

    private void updateEpisodesDisplay() {

        if (episodesList == null){
            System.err.println("episodesList is null, cannot update display.");
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

        checkAddingEpisode();
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


    private HBox createEpisodeItem(FilmSeriesDetails.EpisodeDetails episode){
        VBox episodeDetails = new VBox(5);

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        HBox episodeItem = new HBox();
        setEpisodeItem(episodeItem);

        HBox.setHgrow(episodeDetails, Priority.ALWAYS);

        Label episodeNumber = new Label(String.valueOf(episode.getEpisodeNumber()));
        Label episodeTitle = new Label(episode.getName() != null ? episode.getName() : "Title not available");
        ImageView episodeThumbnail = new ImageView();
        setEpisodeThumbnail(episodeThumbnail);


        episodeNumber.getStyleClass().addAll("episode-number","on-primary");
        episodeNumber.setTextOverrun(OverrunStyle.CLIP);


        // Use the updated loadEpisodeThumbnail which includes fallback logic
        loadEpisodeThumbnail(episode.getStillUrl(), episodeThumbnail);


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
        setEpisodeDescription(episodeDescription);

        episodeDetails.getChildren().addAll(titleRow, episodeDescription);
        episodeItem.setOnMouseClicked(e -> playEpisode());

        episodeItem.getChildren().addAll(episodeNumber, episodeThumbnail, episodeDetails);

        return episodeItem;
    }

    private void setEpisodeDescription(Label episodeDescription) {
        episodeDescription.getStyleClass().addAll("episode-description","on-primary");
        episodeDescription.setWrapText(true);
        episodeDescription.setMaxHeight(40);
    }

    private void setEpisodeThumbnail(ImageView episodeThumbnail) {
        episodeThumbnail.setFitWidth(120);
        episodeThumbnail.setFitHeight(70);
        episodeThumbnail.setPreserveRatio(true);
        episodeThumbnail.getStyleClass().add("episode-thumbnail-image");
    }

    private void setEpisodeItem(HBox episodeItem) {
        episodeItem.setAlignment(Pos.CENTER_LEFT);
        episodeItem.setSpacing(15);
        episodeItem.getStyleClass().addAll("episode-item","small-item");
    }

    private void playEpisode() {
        openPlayerScene();
    }

    private void loadBackdrop(String backdropUrl) {
        if (backdropUrl != null && !backdropUrl.isEmpty() && !backdropUrl.endsWith("null")){
            try{
                // Background loading (true) is good for UI responsiveness
                Image image = new Image(backdropUrl, true);
                settingBackdropImage(image);


            }
            catch(IllegalArgumentException e){
                // This can happen if the URL string is malformed
                System.err.println("Invalid image URL format for backdrop: " + backdropUrl + " - " + e.getMessage());
            }
            catch(Exception e){
                System.err.println("Failed to load backdrop image: " + backdropUrl + " - " + e.getMessage());
            }
        }
        else{
            System.err.println("Backdrop URL is null, empty, or invalid: " + backdropUrl);
        }
    }

    private void settingBackdropImage(Image image) {
        heroImageView.setImage(image);
        heroImageView.setPreserveRatio(false);

        heroImageView.setFitWidth(backgroundVBox.getWidth());
        heroImageView.setFitHeight(backgroundVBox.getPrefHeight());
    }

    private void showLoadingState() {
        if (episodesList != null) episodesList.getChildren().clear();
        hideSeriesSpecificUI();
    }

    private void showErrorState() {
        if (episodesList != null) episodesList.getChildren().clear();
        hideSeriesSpecificUI();
    }
    //Setting background behind the film scene
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
        UserDao dao = new UserDao();
        dao.insertHistoryContent(user.getID(), currentContent.getContentId());
        if(currentContent != null){
            if(currentContent.isSeries()){
                if(currentContent.getSeasons() != null &&
                        currentSeasonIndex < currentContent.getSeasons().size() &&
                        currentContent.getSeasons().get(currentSeasonIndex) != null) {
                    FilmSeriesDetails.SeasonDetails currentSeason = currentContent.getSeasons().get(currentSeasonIndex);
                    if(currentSeason.getEpisodes() != null && !currentSeason.getEpisodes().isEmpty()) {
                        playEpisode();
                    }
                    else{
                        System.out.println("No episodes found for the current season.");
                    }
                }
                else{
                    System.out.println("Current season data not loaded yet.");
                }
            }
            else{
                openPlayerScene();
            }
        }
    }
    //Open player scene when button play is clicked
    private void openPlayerScene() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/movie_view/FilmPlayer.fxml"), Main.resourceBundle);
                Scene currentSceneNode = (background != null && background.getScene() != null) ? background.getScene() : null;
                if (currentSceneNode == null) return;
                Parent scene = loader.load();
                ((FilmPlayer)loader.getController()).initializePlayer(currentContent.getVideoUrl(), user, account);
                ((FilmPlayer)loader.getController()).play();
                Scene newScene = new Scene(scene, currentSceneNode.getWidth(), currentSceneNode.getHeight());
                Stage stage = (Stage) currentSceneNode.getWindow();
                if (stage != null) stage.setScene(newScene);
                else System.err.println("FilmSceneController: Stage is null.");

            } catch (IOException e) {
                System.err.println("\"FilmSceneController: Error to load player, message: " + e.getMessage());
            }
        });
    }

    @FXML
    private void addToWatchlist() {
        System.out.println("Add to watchlist button clicked");
        UserDao dao = new UserDao();
        dao.insertWatchlistContent(user.getID(), currentContent.getContentId());
    }

    @FXML
    private void addToFavourites() {
        System.out.println("add to favourites button clicked");
        UserDao dao = new UserDao();
        dao.insertFavouriteContent(user.getID(),currentContent.getContentId());
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

                                if (currentSeasonIndex == seasonNumber - 1) { // Note: Original code had `|| isDropdownOpen` here, this version doesn't
                                    updateSeasonDropdownButtonText();
                                    updateEpisodesDisplay();
                                }
                            } catch (Exception e) {
                                System.err.println("Error parsing season " + seasonNumber + " data: " + e.getMessage());
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        System.err.println("Error loading season " + seasonNumber + ": " + ex.getMessage());
                        if (currentSeasonIndex == seasonNumber - 1) {
                            Platform.runLater(() -> { // Note: Original code had `this::updateEpisodesDisplay` and another Platform.runLater, this version is simpler
                                updateSeasonDropdownButtonText();
                                updateEpisodesDisplay();
                            });
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

    public void setMainPagesController(MainPagesController controller) {this.mainPagesController = controller;}
    @FXML
    private void deleteFromFavourites() {
        UserDao dao = new UserDao();
        dao.deleteFavourite(user.getID(),currentContent.getContentId());
    }
    @FXML
    private void deleteFromWatchlist() {
        UserDao dao = new UserDao();
        dao.deleteWatchlist(user.getID(), currentContent.getContentId());
    }

    public FilmSeriesDetails getCurrentContent() {return currentContent;}

    public boolean isLoading() {return titleLabel != null && "Loading...".equals(titleLabel.getText());}


    public void setUserAndAccount(User user,Account account){this.user=user;this.account=account;}

    // Check  if the content is in the favourites or in the watchList
    private void checkFavouriteAndWatchList(User user,int contentId) {
        UserDao userDao = new UserDao();
        List<Boolean> list= userDao.checkIfIsInFavouritesOrWatchList(user.getID(),contentId);
        if(list.get(0) ){
            styleFavouriteButton.getStyleClass().remove( "surface-transparent");
            styleFavouriteButton.getStyleClass().add("favourite-active");
            isInFavourite = true;
        }
        if(list.get(1)){
            styleWatchListButton.getStyleClass().remove( "surface-transparent");
            styleWatchListButton.getStyleClass().add( "watchlist-active");
            isInWatchList = true;
        }
    }
}