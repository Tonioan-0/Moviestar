package com.esa.moviestar.movie_view;

import com.esa.moviestar.home.MainPagesController;
import com.esa.moviestar.libraries.TMDbApiManager;
import com.esa.moviestar.model.Content;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class FilmSceneController {

    private MainPagesController mainPagesController;
    private TMDbApiManager tmdbApiManager;
    private Content currentContentDetails; // Stores the fully detailed content object

    @FXML public StackPane background;
    @FXML public ScrollPane scrollPane;
    @FXML public VBox episodesList; // This VBox is where individual episode items are added

    @FXML private Button closeButton;
    @FXML private Button playButton;
    @FXML private Button addButton;
    @FXML private Button infoButton;

    @FXML private Label titleLabel;
    @FXML private Label yearLabel;
    @FXML private Label episodesLabel; // For current season's episode count or movie runtime
    @FXML private Label ratingLabel;
    @FXML private Label maturityLabel;
    @FXML private Label violenceLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label castLabel;
    @FXML private Label genresLabel;
    @FXML private Label showTypeLabel;
    @FXML private Label seriesTitleLabel; // Title for episodes section, e.g., "Episodes of [Series Name]"

    // Season selection components
    private HBox seasonsContainer;
    private Button seasonsDropdownButton;
    private VBox seasonsDropdownMenu; // This is the menu itself, child of a ScrollPane
    private ScrollPane seasonsDropdownScrollPane; // ScrollPane for the dropdown menu
    private boolean isDropdownOpen = false;

    // Simplified data storage for seasons and episodes
    private List<TMDbApiManager.ApiSeasonDetails> allSeasonsDetails;
    private int currentSeasonIndex = 0;
    private boolean isSeriesView = false;


    public Error initialize() {
        if (background == null) return new Error("background node not initialized in FXML");
        // Other critical FXML null checks can be added if issues persist

        this.tmdbApiManager = TMDbApiManager.getInstance();
        this.allSeasonsDetails = new ArrayList<>();

        if (closeButton != null) closeButton.setOnMouseClicked(event -> closeView());
        if (playButton != null) playButton.setOnAction(event -> playContent());
        if (addButton != null) addButton.setOnAction(event -> addToList());
        if (infoButton != null) infoButton.setOnAction(event -> showInfo());

        if (scrollPane != null && scrollPane.getContent() instanceof Region) {
            ((Region) scrollPane.getContent()).prefWidthProperty().bind(scrollPane.widthProperty());
        }
        System.out.println("FilmSceneController initialized.");
        return null;
    }

    public void setMainPagesController(MainPagesController mainPagesController) {
        this.mainPagesController = mainPagesController;
    }

    public void loadContent(Content initialContent) {
        if (initialContent == null) {
            Platform.runLater(() -> {
                setTitle("Error");
                setDescription("Content not found.");
                setPlaceholderBackground();
            });
            return;
        }
        clearUIBeforeLoading();

        tmdbApiManager.fetchFullContentDetails(initialContent.getId(), initialContent.isSeries())
                .thenAcceptAsync(fullContent -> {
                    this.currentContentDetails = fullContent;
                    this.isSeriesView = fullContent.isSeries();

                    Platform.runLater(() -> {
                        updateBackground(fullContent.getImageUrl() != null ? fullContent.getImageUrl() : fullContent.getPosterUrl());
                        populateBaseUI(fullContent);

                        if (fullContent.isSeries()) {
                            setupAsSeriesViewType();
                            loadSeasonsAndEpisodesForSeries(fullContent);
                        } else {
                            setupAsMovieViewType();
                            if (episodesLabel != null) {
                                episodesLabel.setText(fullContent.getRuntimeMinutes() > 0 ? fullContent.getRuntimeMinutes() + " min" : "Runtime N/A");
                                episodesLabel.setVisible(true);
                                episodesLabel.setManaged(true);
                            }
                        }
                    });
                }, Platform::runLater)
                .exceptionally(ex -> {
                    System.err.println("FilmSceneController: Failed to fetch full content details for ID " + initialContent.getId() + ": " + ex.getMessage());
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        setTitle("Error Loading Details");
                        setDescription("Could not load details. Please try again.");
                        setPlaceholderBackground();
                    });
                    return null;
                });
    }

    private void clearUIBeforeLoading() {
        setTitle("Loading...");
        setYear("");
        setDescription("");
        setGenres("");
        setRating("");
        setMaturity("");
        setViolenceLabel("");
        setCast("");
        if (episodesLabel != null) episodesLabel.setText("");
        if (episodesList != null) episodesList.getChildren().clear();
        allSeasonsDetails.clear();
        currentSeasonIndex = 0;
        hideSeasonsSelectorVisuals(); // Hides the dropdown related UI
        if (seriesTitleLabel != null) {
            seriesTitleLabel.setVisible(false);
            seriesTitleLabel.setManaged(false);
        }
    }

    private void updateBackground(String imageUrl) {
        if (background == null) return;
        background.getChildren().removeIf(node -> node instanceof ImageView || node.getStyle().contains("-fx-background-color:"));

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            try {
                Image image = new Image(imageUrl, true);
                ImageView bgImageView = new ImageView(image);
                image.errorProperty().addListener((obs, oldE, newE) -> {
                    if (newE) {
                        System.err.println("FilmSceneController: Failed to load background image: " + imageUrl);
                        Platform.runLater(this::setPlaceholderBackground);
                    }
                });
                if (!image.isError()) {
                    bgImageView.setEffect(createBackgroundEffect());
                    background.getChildren().addFirst(configureBackgroundImage(bgImageView));
                } else {
                    setPlaceholderBackground();
                }
            } catch (Exception e) {
                System.err.println("FilmSceneController: Exception loading background image: " + imageUrl + " - " + e.getMessage());
                setPlaceholderBackground();
            }
        } else {
            setPlaceholderBackground();
        }
    }

    private void setPlaceholderBackground() {
        if (background == null) return;
        background.getChildren().removeIf(node -> node instanceof ImageView || node.getStyle().contains("-fx-background-color:"));
        Pane placeholder = new Pane();
        placeholder.setStyle("-fx-background-color: #1A1A1A;");
        background.getChildren().addFirst(placeholder);
        placeholder.prefWidthProperty().bind(background.widthProperty());
        placeholder.prefHeightProperty().bind(background.heightProperty());
    }

    private void populateBaseUI(Content content) {
        setTitle(content.getTitle());
        setYear(content.getYear() > 0 ? String.valueOf(content.getYear()) : "N/A");
        setDescription(content.getPlot());
        setGenres(content.getGenreNames() != null && !content.getGenreNames().isEmpty() ?
                String.join(", ", content.getGenreNames()) : "N/A");
        setRating(content.getRating() > 0 ? String.format("%.1f/10", content.getRating()) : "N/A");
        setMaturity("TV-MA"); // Placeholder
        setViolenceLabel("Mild Violence"); // Placeholder
        setCast("Cast not available"); // Placeholder
    }

    private void loadSeasonsAndEpisodesForSeries(Content seriesContent) {
        allSeasonsDetails.clear();
        currentSeasonIndex = 0;

        if (seriesContent.getNumberOfSeasons() == 0) {
            Platform.runLater(() -> {
                if (episodesLabel != null) episodesLabel.setText("No seasons available.");
                hideSeasonsSelectorVisuals();
                if (episodesList != null) episodesList.getChildren().clear();
            });
            return;
        }

        List<CompletableFuture<TMDbApiManager.ApiSeasonDetails>> seasonFutures = new ArrayList<>();
        for (int i = 1; i <= seriesContent.getNumberOfSeasons(); i++) { // Fetch seasons 1 to N
            seasonFutures.add(tmdbApiManager.fetchTvSeasonDetails(seriesContent.getId(), i));
        }
        // Optionally fetch season 0 (Specials) if needed, by adding another future.

        CompletableFuture.allOf(seasonFutures.toArray(new CompletableFuture[0]))
                .thenAcceptAsync(voidResult -> {
                    this.allSeasonsDetails = seasonFutures.stream()
                            .map(future -> {
                                try { return future.join(); }
                                catch (Exception e) {
                                    System.err.println("FilmSceneController: Error joining season future: " + e.getMessage());
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .filter(s -> s.episodes != null && !s.episodes.isEmpty()) // Only keep seasons with episodes
                            .sorted(Comparator.comparingInt(s -> s.seasonNumber))
                            .collect(Collectors.toList());

                    Platform.runLater(this::updateSeriesUIDisplay);
                }, Platform::runLater)
                .exceptionally(ex -> {
                    System.err.println("FilmSceneController: Error fetching all season details for " + seriesContent.getTitle() + ": " + ex.getMessage());
                    Platform.runLater(() -> {
                        if (episodesLabel != null) episodesLabel.setText("Could not load episodes.");
                        hideSeasonsSelectorVisuals();
                    });
                    return null;
                });
    }

    private void updateSeriesUIDisplay() {
        if (!allSeasonsDetails.isEmpty()) {
            currentSeasonIndex = 0; // Default to first season
            ensureSeasonsSelectorExists(); // Create if not present, update if present
            displayEpisodesForCurrentSeason();
        } else {
            if (episodesLabel != null) episodesLabel.setText("No episodes available for this series.");
            hideSeasonsSelectorVisuals();
            if (episodesList != null) episodesList.getChildren().clear();
        }
    }

    public void setupAsSeriesViewType() {
        this.isSeriesView = true;
        if (showTypeLabel != null) showTypeLabel.setText("SERIE TV");
        if (seriesTitleLabel != null && titleLabel != null) {
            seriesTitleLabel.setText(titleLabel.getText()); // Or "Episodes of " + titleLabel.getText()
            seriesTitleLabel.setVisible(true);
            seriesTitleLabel.setManaged(true);
        }
    }

    public void setupAsMovieViewType() {
        this.isSeriesView = false;
        if (showTypeLabel != null) showTypeLabel.setText("FILM");
        hideSeasonsSelectorVisuals();
        if (episodesList != null) episodesList.getChildren().clear();
        if (episodesLabel != null) {
            episodesLabel.setVisible(true); // Will be set to runtime by loadContent
            episodesLabel.setManaged(true);
        }
        if (seriesTitleLabel != null) {
            seriesTitleLabel.setVisible(false);
            seriesTitleLabel.setManaged(false);
        }
    }

    private void ensureSeasonsSelectorExists() {
        if (episodesList == null || !(episodesList.getParent() instanceof VBox)) {
            System.err.println("Cannot create seasons selector: episodesList issue or parent not VBox.");
            return;
        }
        VBox episodesSectionParent = (VBox) episodesList.getParent();

        if (seasonsContainer == null) {
            seasonsContainer = new HBox();
            seasonsContainer.setAlignment(Pos.CENTER_LEFT);
            seasonsContainer.setSpacing(10);
            seasonsContainer.setStyle("-fx-padding: 0 0 15 0;");

            seasonsDropdownButton = new Button();
            seasonsDropdownButton.getStyleClass().add("seasons-dropdown-button");
            seasonsDropdownButton.setPrefHeight(35);
            seasonsDropdownButton.setOnAction(e -> toggleSeasonsDropdownMenu());

            seasonsDropdownMenu = new VBox(); // This is the content of the ScrollPane
            seasonsDropdownMenu.getStyleClass().add("seasons-dropdown-menu");
            seasonsDropdownMenu.setSpacing(2);

            seasonsDropdownScrollPane = new ScrollPane(seasonsDropdownMenu);
            seasonsDropdownScrollPane.setFitToWidth(true);
            seasonsDropdownScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            seasonsDropdownScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            seasonsDropdownScrollPane.setMaxHeight(200);
            seasonsDropdownScrollPane.getStyleClass().add("transparent-scroll"); // From general.css
            seasonsDropdownScrollPane.setVisible(false); // Visibility controlled by isDropdownOpen
            seasonsDropdownScrollPane.setManaged(false);

            StackPane dropdownWrapper = new StackPane(seasonsDropdownButton, seasonsDropdownScrollPane);
            StackPane.setAlignment(seasonsDropdownScrollPane, Pos.TOP_LEFT);
            seasonsDropdownScrollPane.setTranslateY(seasonsDropdownButton.getPrefHeight() + 2); // Position below button

            seasonsContainer.getChildren().add(dropdownWrapper);

            // Add seasonsContainer to the parent VBox if not already present
            if (!episodesSectionParent.getChildren().contains(seasonsContainer)) {
                int insertIndex = 0; // Default to top
                Node targetNodeForInsertionBefore = episodesLabel != null ? episodesLabel : episodesList;
                if (targetNodeForInsertionBefore != null && episodesSectionParent.getChildren().contains(targetNodeForInsertionBefore)) {
                    insertIndex = episodesSectionParent.getChildren().indexOf(targetNodeForInsertionBefore);
                } else if (episodesList != null && episodesSectionParent.getChildren().contains(episodesList)) {
                    insertIndex = episodesSectionParent.getChildren().indexOf(episodesList);
                } else if (episodesSectionParent.getChildren().size() > 0) {
                    // Fallback: try to insert after seriesTitleLabel if it exists
                    if (seriesTitleLabel != null && episodesSectionParent.getChildren().contains(seriesTitleLabel)) {
                        insertIndex = episodesSectionParent.getChildren().indexOf(seriesTitleLabel) + 1;
                    } else {
                        // If seriesTitleLabel is not there or not found, add before the first element if list is not empty
                        // or at the end if all else fails or list is empty.
                        insertIndex = episodesSectionParent.getChildren().isEmpty() ? 0 : Math.max(0, insertIndex);
                    }
                }

                if (insertIndex >= 0 && insertIndex <= episodesSectionParent.getChildren().size()) {
                    episodesSectionParent.getChildren().add(insertIndex, seasonsContainer);
                } else {
                    episodesSectionParent.getChildren().add(seasonsContainer); // Fallback
                }
            }
        }

        populateSeasonsDropdownOptions(); // Populate/update options
        seasonsContainer.setVisible(true);
        seasonsContainer.setManaged(true);
    }

    private void populateSeasonsDropdownOptions() {
        if (allSeasonsDetails == null || allSeasonsDetails.isEmpty() || seasonsDropdownButton == null || seasonsDropdownMenu == null) {
            hideSeasonsSelectorVisuals();
            return;
        }
        seasonsContainer.setVisible(true);
        seasonsContainer.setManaged(true);

        TMDbApiManager.ApiSeasonDetails currentSeason = allSeasonsDetails.get(currentSeasonIndex);
        String buttonText = (currentSeason.name != null && !currentSeason.name.isEmpty() ? currentSeason.name : "Season " + currentSeason.seasonNumber);
        seasonsDropdownButton.setText(buttonText + (isDropdownOpen ? " \u25B2" : " \u25BC"));

        seasonsDropdownMenu.getChildren().clear();
        for (int i = 0; i < allSeasonsDetails.size(); i++) {
            TMDbApiManager.ApiSeasonDetails season = allSeasonsDetails.get(i);
            String optionName = (season.name != null && !season.name.isEmpty() ? season.name : "Season " + season.seasonNumber);
            if (season.seasonNumber == 0 && (optionName.equalsIgnoreCase("Season 0") || optionName.isEmpty())) {
                optionName = "Specials"; // Common naming for season 0
            }

            Button seasonOptionButton = new Button(optionName);
            seasonOptionButton.getStyleClass().add("season-option-button");
            seasonOptionButton.setPrefWidth(200);
            seasonOptionButton.setPrefHeight(30);
            final int seasonIdx = i;
            seasonOptionButton.setOnAction(e -> handleSeasonSelection(seasonIdx));
            if (i == currentSeasonIndex) seasonOptionButton.getStyleClass().add("selected-season");
            seasonsDropdownMenu.getChildren().add(seasonOptionButton);
        }
    }

    private void toggleSeasonsDropdownMenu() {
        isDropdownOpen = !isDropdownOpen;
        if (seasonsDropdownScrollPane != null) {
            seasonsDropdownScrollPane.setVisible(isDropdownOpen);
            seasonsDropdownScrollPane.setManaged(isDropdownOpen);
        }
        // Update arrow on button
        if (seasonsDropdownButton != null && allSeasonsDetails != null && !allSeasonsDetails.isEmpty()) {
            TMDbApiManager.ApiSeasonDetails currentSeason = allSeasonsDetails.get(currentSeasonIndex);
            String buttonText = (currentSeason.name != null && !currentSeason.name.isEmpty() ? currentSeason.name : "Season " + currentSeason.seasonNumber);
            seasonsDropdownButton.setText(buttonText + (isDropdownOpen ? " \u25B2" : " \u25BC"));
        }
    }

    private void handleSeasonSelection(int seasonIndex) {
        if (allSeasonsDetails == null || seasonIndex < 0 || seasonIndex >= allSeasonsDetails.size()) return;
        currentSeasonIndex = seasonIndex;
        populateSeasonsDropdownOptions(); // Update button text and menu highlighting
        displayEpisodesForCurrentSeason();
        if (isDropdownOpen) toggleSeasonsDropdownMenu(); // Close dropdown
    }

    private void hideSeasonsSelectorVisuals() {
        if (seasonsContainer != null) {
            seasonsContainer.setVisible(false);
            seasonsContainer.setManaged(false);
        }
        if (seasonsDropdownScrollPane != null) {
            seasonsDropdownScrollPane.setVisible(false);
            seasonsDropdownScrollPane.setManaged(false);
        }
        isDropdownOpen = false; // Reset state
    }

    private void displayEpisodesForCurrentSeason() {
        if (episodesList == null) return;
        episodesList.getChildren().clear();

        if (!isSeriesView || allSeasonsDetails == null || allSeasonsDetails.isEmpty() ||
                currentSeasonIndex < 0 || currentSeasonIndex >= allSeasonsDetails.size()) {
            if (episodesLabel != null) episodesLabel.setText("No episodes to show.");
            return;
        }

        TMDbApiManager.ApiSeasonDetails currentSeason = allSeasonsDetails.get(currentSeasonIndex);
        List<TMDbApiManager.ApiEpisodeDetails> episodesForSeason = currentSeason.episodes;

        if (episodesForSeason != null && !episodesForSeason.isEmpty()) {
            for (TMDbApiManager.ApiEpisodeDetails episodeData : episodesForSeason) {
                episodesList.getChildren().add(createEpisodeItemNode(episodeData));
            }
            if (episodesLabel != null) {
                episodesLabel.setText(episodesForSeason.size() + (episodesForSeason.size() == 1 ? " episodio" : " episodi"));
                episodesLabel.setVisible(true);
                episodesLabel.setManaged(true);
            }
        } else {
            String seasonNameText = currentSeason.name != null ? currentSeason.name : "Season " + currentSeason.seasonNumber;
            if (episodesLabel != null) {
                episodesLabel.setText("No episodes in " + seasonNameText + ".");
                episodesLabel.setVisible(true);
                episodesLabel.setManaged(true);
            }
            Label noEpisodesMsg = new Label("No episodes available for " + seasonNameText + ".");
            noEpisodesMsg.setStyle("-fx-text-fill: #888; -fx-padding: 20;");
            episodesList.getChildren().add(noEpisodesMsg);
        }
        if (seriesTitleLabel != null && titleLabel != null) {
            seriesTitleLabel.setText(titleLabel.getText());
            seriesTitleLabel.setVisible(true);
            seriesTitleLabel.setManaged(true);
        }
    }

    private HBox createEpisodeItemNode(TMDbApiManager.ApiEpisodeDetails episode) {
        HBox episodeItem = new HBox();
        episodeItem.setAlignment(Pos.CENTER_LEFT);
        episodeItem.setSpacing(15);
        episodeItem.getStyleClass().add("episode-item");

        Label episodeNumberLabel = new Label(String.valueOf(episode.episodeNumber));
        episodeNumberLabel.getStyleClass().add("episode-number");

        StackPane thumbnailContainer = new StackPane();
        thumbnailContainer.setPrefSize(120, 70);
        thumbnailContainer.getStyleClass().add("episode-thumbnail");

        String thumbnailUrl = episode.getFullStillUrl(TMDbApiManager.STILL_IMAGE_SIZE);
        if (thumbnailUrl != null) {
            try {
                Image thumbImg = new Image(thumbnailUrl, 120, 70, false, true, true);
                ImageView thumbnailView = new ImageView(thumbImg);
                thumbnailView.setFitHeight(70);
                thumbnailView.setFitWidth(120);
                thumbImg.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) System.err.println("Failed to load episode thumbnail: " + thumbnailUrl);
                });
                if (!thumbImg.isError()) thumbnailContainer.getChildren().add(thumbnailView);
            } catch (Exception e) {
                System.err.println("Error creating image for thumbnail: " + thumbnailUrl + " - " + e.getMessage());
            }
        }

        VBox episodeDetailsVBox = new VBox(5);
        HBox.setHgrow(episodeDetailsVBox, Priority.ALWAYS);

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label episodeTitleLabel = new Label(episode.name != null ? episode.name : "Episode " + episode.episodeNumber);
        episodeTitleLabel.getStyleClass().add("episode-title");
        episodeTitleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(episodeTitleLabel, Priority.SOMETIMES);

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Label episodeDurationLabel = new Label(episode.runtime > 0 ? episode.runtime + "min" : "--min");
        episodeDurationLabel.getStyleClass().add("episode-duration");
        titleRow.getChildren().addAll(episodeTitleLabel, titleSpacer, episodeDurationLabel);

        Label episodeDescriptionLabel = new Label(episode.overview != null ? episode.overview : "No description.");
        episodeDescriptionLabel.getStyleClass().add("episode-description");
        episodeDescriptionLabel.setWrapText(true);
        episodeDescriptionLabel.setMaxHeight(38); // Approx 2 lines

        episodeDetailsVBox.getChildren().addAll(titleRow, episodeDescriptionLabel);

        episodeItem.setOnMouseClicked(e -> handlePlayEpisode(episode));
        // Hover effects are handled by CSS .episode-item:hover

        episodeItem.getChildren().addAll(episodeNumberLabel, thumbnailContainer, episodeDetailsVBox);
        return episodeItem;
    }

    private void handlePlayEpisode(TMDbApiManager.ApiEpisodeDetails episode) {
        String seasonName = "N/A";
        if (allSeasonsDetails != null && !allSeasonsDetails.isEmpty() && currentSeasonIndex < allSeasonsDetails.size()) {
            seasonName = allSeasonsDetails.get(currentSeasonIndex).name;
        }
        System.out.println("Playing episode: " + episode.name + " (Ep. " + episode.episodeNumber + ") from season " + seasonName);
        // TODO: Implement actual playback: mainPagesController.playVideo(episode.getVideoStreamUrl(), episode.name);
    }

    private ImageView configureBackgroundImage(ImageView newImgView) {
        if (background != null) {
            newImgView.fitWidthProperty().bind(background.widthProperty());
            newImgView.fitHeightProperty().bind(background.heightProperty());
        }
        newImgView.setPreserveRatio(false);
        return newImgView;
    }

    private ColorAdjust createBackgroundEffect() {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.65);
        colorAdjust.setSaturation(-0.2);
        GaussianBlur blur = new GaussianBlur(15);
        colorAdjust.setInput(blur);
        return colorAdjust;
    }

    @FXML private void closeView() {
        if (mainPagesController != null) mainPagesController.restorePreviousScene();
        else System.err.println("mainPagesController is null. Cannot close view.");
    }

    @FXML private void playContent() {
        if (currentContentDetails == null) return;
        System.out.println("Play button clicked for: " + currentContentDetails.getTitle());
        if (isSeriesView) {
            if (allSeasonsDetails != null && !allSeasonsDetails.isEmpty() &&
                    currentSeasonIndex < allSeasonsDetails.size() && currentSeasonIndex >= 0) {
                TMDbApiManager.ApiSeasonDetails currentSeason = allSeasonsDetails.get(currentSeasonIndex);
                if (currentSeason.episodes != null && !currentSeason.episodes.isEmpty()) {
                    handlePlayEpisode(currentSeason.episodes.get(0)); // Play first episode
                } else System.out.println("No episodes in current season to play.");
            } else System.out.println("No seasons/episodes to play for this series.");
        } else { // Movie
            System.out.println("Playing movie: " + currentContentDetails.getTitle());
            // TODO: mainPagesController.playVideo(currentContentDetails.getVideoUrl(), currentContentDetails.getTitle());
        }
    }

    @FXML private void addToList() {
        if (currentContentDetails == null) return;
        System.out.println("Add to list: " + currentContentDetails.getTitle());
        // TODO: Implement add to list functionality
    }

    @FXML private void showInfo() {
        if (currentContentDetails == null) return;
        System.out.println("Show info for: " + currentContentDetails.getTitle());
        // TODO: Implement show more info functionality
    }

    public void setTitle(String title) { if (titleLabel != null) titleLabel.setText(title != null ? title : "N/A"); }
    public void setDescription(String description) { if (descriptionLabel != null) descriptionLabel.setText(description != null ? description : "No description available.");}
    public void setYear(String year) { if (yearLabel != null) yearLabel.setText(year != null ? year : "N/A"); }
    public void setCast(String cast) { if (castLabel != null) castLabel.setText(cast != null ? "Cast: " + cast : "Cast: N/A"); }
    public void setGenres(String genres) { if (genresLabel != null) genresLabel.setText(genres != null ? "Genres: " + genres : "Genres: N/A"); }
    public void setRating(String rating) { if (ratingLabel != null) ratingLabel.setText(rating != null ? rating : "N/A"); }
    public void setMaturity(String maturity) { if (maturityLabel != null) maturityLabel.setText(maturity != null ? maturity : ""); }
    public void setViolenceLabel(String violence) { if (violenceLabel != null) violenceLabel.setText(violence != null ? violence : ""); }
}