package com.esa.moviestar.movie_view;

import com.esa.moviestar.home.MainPagesController;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image; // Ensure this is used if you plan to load images
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font; // Ensure this is used if you customize fonts directly

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Placeholder/Stub classes for Season and Episode if you receive data in this format from an API
// before converting to Map structures.
class Episode {
    int number;
    String title;
    String description;
    String duration;
    String thumbnailUrl; // Example: if your API provides this

    public Episode(int number, String title, String description, String duration, String thumbnailUrl) {
        this.number = number;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("number", number);
        map.put("title", title);
        map.put("description", description);
        map.put("duration", duration);
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            map.put("thumbnailUrl", thumbnailUrl); // For future use in createEpisodeItem
        }
        return map;
    }
}

class Season {
    // int seasonNumber; // Could be useful for sorting or identification
    String seasonName;
    List<Episode> episodes;

    public Season(/*int seasonNumber,*/ String seasonName) {
        // this.seasonNumber = seasonNumber;
        this.seasonName = seasonName;
        this.episodes = new ArrayList<>();
    }

    public void addEpisode(Episode episode) {
        this.episodes.add(episode);
    }

    public String getSeasonName() {
        return seasonName;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }
}


public class FilmSceneController {

    MainPagesController mainPagesController;

    // Main containers
    @FXML
    public StackPane background;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public VBox episodesList; // This VBox should be a direct child of the VBox that will hold the seasonsContainer

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
    private Label episodesLabel; // Displays number of episodes in current season
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
    private Label showTypeLabel; // e.g., "SERIE TV", "FILM"
    @FXML
    private Label seriesTitleLabel; // Title specific for the episodes section header

    // Season selection components
    private HBox seasonsContainer;
    private Button seasonsDropdownButton;
    private VBox seasonsDropdownMenu;
    private boolean isDropdownOpen = false;

    // Data storage - using Maps and Lists
    private Map<String, List<Map<String, Object>>> seasonsData; // Key: Season Name, Value: List of episodes (as Maps)
    private List<String> seasonNames; // To maintain order and populate dropdown
    private int currentSeasonIndex = 0;
    private boolean isSeriesContent = false;

    public Error initialize() {
        if (background == null) {
            System.err.println("Error: background node is null. Check FXML injection.");
            return new Error("background node not initialized");
        }
        if (episodesList == null) {
            System.err.println("Error: episodesList node is null. Check FXML injection.");
            // Decide if this is critical enough to return an error, or if it can be handled
        }
        if (closeButton == null) {
            System.err.println("Error: closeButton is null. Check FXML file and controller fx:id.");
            // Potentially return error or handle gracefully
        }


        // Initialize data structures
        seasonsData = new HashMap<>();
        seasonNames = new ArrayList<>();

        // Set button actions
        if (closeButton != null) {
            closeButton.setOnMouseClicked(event -> closeView());
        } else {
            System.err.println("closeButton was not injected. Mouse click event cannot be set.");
        }
        if (playButton != null) {
            playButton.setOnAction(event -> playContent());
        }
        if (addButton != null) {
            addButton.setOnAction(event -> addToList());
        }
        if (infoButton != null) {
            infoButton.setOnAction(event -> showInfo());
        }


        // Initialize with placeholder data for testing
        // You would replace this with a call to load actual data, e.g., from an API
        initializeTestData(); // This will set up Evangelion series by default

        System.out.println("FilmSceneController initialized.");
        return null;
    }

    private void initializeTestData() {
        // This method simulates loading data for a series.
        // Clear any existing data first
        seasonNames.clear();
        seasonsData.clear();

        // Add season names
        seasonNames.add("Stagione 1");
        seasonNames.add("Stagione 2");

        // Season 1 episodes
        List<Map<String, Object>> season1Episodes = new ArrayList<>();
        Map<String, Object> ep1 = new HashMap<>();
        ep1.put("number", 1);
        ep1.put("title", "L'attacco dell'Angelo");
        ep1.put("description", "Nell'anno 2015 un Angelo torna ad attaccare Neo Tokyo-3...");
        ep1.put("duration", "23min");
        season1Episodes.add(ep1);

        Map<String, Object> ep2 = new HashMap<>();
        ep2.put("number", 2);
        ep2.put("title", "La bestia sconosciuta");
        ep2.put("description", "Shinji si sveglia in ospedale e incontra Rei Ayanami...");
        ep2.put("duration", "23min");
        season1Episodes.add(ep2);
        seasonsData.put("Stagione 1", season1Episodes);

        // Season 2 episodes
        List<Map<String, Object>> season2Episodes = new ArrayList<>();
        Map<String, Object> s2ep1 = new HashMap<>();
        s2ep1.put("number", 1);
        s2ep1.put("title", "Il ritorno degli Angeli");
        s2ep1.put("description", "Una nuova minaccia emerge...");
        s2ep1.put("duration", "24min");
        season2Episodes.add(s2ep1);
        seasonsData.put("Stagione 2", season2Episodes);

        // Set some dummy series info
        setTitle("Evangelion Dummy Series");
        setYear("1995");
        setDescription("Una serie anime classica su mecha e angeli.");
        setCast("Megumi Ogata, Kotono Mitsuishi, Megumi Hayashibara");
        setGenres("Anime, Mecha, Fantascienza, Drammatico");
        if (ratingLabel != null) ratingLabel.setText("TV-MA");
        if (maturityLabel != null) maturityLabel.setText("16+");
        if (violenceLabel != null) violenceLabel.setText("Violenza, Temi maturi");


        // Configure as series content
        setupAsSeriesContent(); // This will call createSeasonsSelector and updateEpisodesDisplay
    }

    public void setProperties(ImageView newBackgroundImageView, MainPagesController mainPagesController) {
        if (newBackgroundImageView == null) {
            System.err.println("Error: newBackgroundImageView is null in setProperties.");
            // Create a placeholder background if null to avoid NullPointerException later
            // For example, a simple colored Pane or a default image
            Pane placeholderBackground = new Pane();
            placeholderBackground.setStyle("-fx-background-color: #222;"); // Dark gray
            if (background != null) {
                background.getChildren().clear(); // Remove old ones
                background.getChildren().addFirst(placeholderBackground);
                // Bind its size, similar to an ImageView
                placeholderBackground.prefWidthProperty().bind(background.widthProperty());
                placeholderBackground.prefHeightProperty().bind(background.heightProperty());
            }
            return; // Or handle error more gracefully
        }
        if (background == null) {
            System.err.println("Error: Main background StackPane is null. Cannot set properties.");
            return;
        }

        this.mainPagesController = mainPagesController;

        // Remove any previous backgrounds
        background.getChildren().removeIf(node -> node instanceof ImageView || node.getStyle().contains("-fx-background-color"));


        newBackgroundImageView.setEffect(setColorBackground());
        background.getChildren().addFirst(setImageBackground(newBackgroundImageView)); // Add it behind other UI elements

        System.out.println("New background set successfully.");
    }

    // --- Content Setup Methods ---

    /**
     * Configures the view for series content.
     * It assumes seasonNames and seasonsData are already populated.
     */
    public void setupAsSeriesContent() {
        this.isSeriesContent = true;
        if (showTypeLabel != null) showTypeLabel.setText("SERIE TV");
        if (seriesTitleLabel != null && titleLabel != null) {
            seriesTitleLabel.setText(titleLabel.getText()); // Set title for episodes section
            seriesTitleLabel.setVisible(true);
            seriesTitleLabel.setManaged(true);
        }

        if (seasonNames != null && !seasonNames.isEmpty()) {
            createSeasonsSelector(); // Create or update dropdown
            updateEpisodesDisplay();   // Display episodes for the current season
        } else {
            System.out.println("No seasons data to display for series content.");
            // Optionally hide episode-related UI if no seasons
            if (episodesList != null) episodesList.getChildren().clear();
            if (episodesLabel != null) episodesLabel.setText("Nessun episodio disponibile");
            hideSeasonsSelector();
        }
    }

    /**
     * Configures the view for movie content.
     */
    public void setupAsMovieContent() {
        this.isSeriesContent = false;
        if (showTypeLabel != null) showTypeLabel.setText("FILM");
        hideSeasonsSelector();
        if (episodesList != null) episodesList.getChildren().clear(); // No episodes for movies
        if (episodesLabel != null) { // Hide or repurpose episodesLabel
            episodesLabel.setVisible(false);
            episodesLabel.setManaged(false);
        }
        if (seriesTitleLabel != null) {
            seriesTitleLabel.setVisible(false);
            seriesTitleLabel.setManaged(false);
        }
        // Potentially display movie-specific information if needed
    }


    // --- Season and Episode Management ---

    private void createSeasonsSelector() {
        if (episodesList == null || !(episodesList.getParent() instanceof VBox)) {
            System.err.println("Cannot create seasons selector: episodesList is null or its parent is not a VBox.");
            return;
        }

        VBox episodesSectionParent = (VBox) episodesList.getParent();

        if (seasonsContainer == null) {
            seasonsContainer = new HBox();
            seasonsContainer.setAlignment(Pos.CENTER_LEFT);
            seasonsContainer.setSpacing(10);
            seasonsContainer.setStyle("-fx-padding: 0 0 15 0;"); // Standard padding for Netflix like UI

            seasonsDropdownButton = new Button();
            seasonsDropdownButton.getStyleClass().add("seasons-dropdown-button"); // Style in CSS
            seasonsDropdownButton.setPrefHeight(35);
            seasonsDropdownButton.setOnAction(e -> toggleSeasonsDropdown());

            seasonsDropdownMenu = new VBox();
            seasonsDropdownMenu.getStyleClass().add("seasons-dropdown-menu"); // Style in CSS
            seasonsDropdownMenu.setVisible(false);
            seasonsDropdownMenu.setManaged(false); // Doesn't take space when invisible
            seasonsDropdownMenu.setSpacing(2);     // Spacing between season buttons
            seasonsDropdownMenu.setMaxHeight(200); // Limit height, make it scrollable if needed via ScrollPane

            StackPane dropdownWrapper = new StackPane(seasonsDropdownButton, seasonsDropdownMenu);
            StackPane.setAlignment(seasonsDropdownMenu, Pos.TOP_LEFT);
            seasonsDropdownMenu.setTranslateY(37); // Position menu just below the button

            seasonsContainer.getChildren().add(dropdownWrapper);
        }

        updateSeasonsDropdown(); // Populate with current season names

        // Add to parent VBox if not already there
        // Ensure it's placed correctly, typically before the episodesList
        if (!episodesSectionParent.getChildren().contains(seasonsContainer)) {
            // Find the index of episodesLabel or a similar marker to insert before episodesList
            // Assuming episodesLabel is the "Episodi" title for the list of episodes
            int insertIndex = -1;
            Node episodesSectionTitle = null; // Such as 'seriesTitleLabel' or a specific "Episodi" label
            for(int i=0; i< episodesSectionParent.getChildren().size(); i++){
                if(episodesSectionParent.getChildren().get(i) == seriesTitleLabel){ // Or whatever you use as title
                    episodesSectionTitle = episodesSectionParent.getChildren().get(i);
                    break;
                }
            }

            if (episodesSectionTitle != null) {
                insertIndex = episodesSectionParent.getChildren().indexOf(episodesSectionTitle) + 1;
            } else {
                // If no title, maybe insert before episodesList if episodesList is already there
                insertIndex = episodesSectionParent.getChildren().indexOf(episodesList);
                if (insertIndex == -1) insertIndex = 0; // Fallback: add at the beginning
            }

            if (insertIndex != -1 && insertIndex < episodesSectionParent.getChildren().size()) {
                episodesSectionParent.getChildren().add(insertIndex, seasonsContainer);
            } else {
                episodesSectionParent.getChildren().add(seasonsContainer); // Add at the end if index is problematic
            }
        }
        seasonsContainer.setVisible(true);
        seasonsContainer.setManaged(true);
    }

    private void updateSeasonsDropdown() {
        if (seasonNames == null || seasonNames.isEmpty() || seasonsDropdownButton == null || seasonsDropdownMenu == null) {
            if (seasonsContainer != null) { // If no seasons, hide the whole thing
                seasonsContainer.setVisible(false);
                seasonsContainer.setManaged(false);
            }
            return;
        }

        seasonsContainer.setVisible(true);
        seasonsContainer.setManaged(true);

        String currentSeasonName = seasonNames.get(currentSeasonIndex);
        seasonsDropdownButton.setText(currentSeasonName + (isDropdownOpen ? " ▲" : " ▼")); // Up/Down arrows

        seasonsDropdownMenu.getChildren().clear();
        for (int i = 0; i < seasonNames.size(); i++) {
            String seasonName = seasonNames.get(i);
            Button seasonOptionButton = new Button(seasonName);
            seasonOptionButton.getStyleClass().add("season-option-button"); // Style in CSS
            seasonOptionButton.setPrefWidth(200); // Or bind to dropdownButton's width
            seasonOptionButton.setPrefHeight(30);
            final int seasonIdx = i;
            seasonOptionButton.setOnAction(e -> selectSeason(seasonIdx));

            if (i == currentSeasonIndex) {
                seasonOptionButton.getStyleClass().add("selected-season"); // For highlighting
            }
            seasonsDropdownMenu.getChildren().add(seasonOptionButton);
        }
    }

    private void toggleSeasonsDropdown() {
        isDropdownOpen = !isDropdownOpen;
        if (seasonsDropdownMenu != null) {
            seasonsDropdownMenu.setVisible(isDropdownOpen);
            seasonsDropdownMenu.setManaged(isDropdownOpen);
        }
        // Update arrow on button
        if (seasonsDropdownButton != null && seasonNames != null && !seasonNames.isEmpty()) {
            String currentSeasonName = seasonNames.get(currentSeasonIndex);
            seasonsDropdownButton.setText(currentSeasonName + (isDropdownOpen ? " \u25B2" : " \u25BC"));
        }
    }

    private void selectSeason(int seasonIndex) {
        if (seasonNames == null || seasonIndex < 0 || seasonIndex >= seasonNames.size()) {
            return;
        }
        currentSeasonIndex = seasonIndex;
        System.out.println("Selected season: " + seasonNames.get(currentSeasonIndex));
        updateSeasonsDropdown();   // Update button text and menu highlighting
        updateEpisodesDisplay();   // Refresh episode list
        toggleSeasonsDropdown();   // Close dropdown
    }

    private void hideSeasonsSelector() {
        if (seasonsContainer != null) {
            seasonsContainer.setVisible(false);
            seasonsContainer.setManaged(false);
            // Optionally, remove it if it won't be shown again for this content view
            // if (episodesList != null && episodesList.getParent() instanceof VBox) {
            // ((VBox) episodesList.getParent()).getChildren().remove(seasonsContainer);
            // }
        }
        if (seriesTitleLabel != null) { // Also hide series title if selector is hidden
            seriesTitleLabel.setVisible(false);
            seriesTitleLabel.setManaged(false);
        }
    }

    private void updateEpisodesDisplay() {
        if (episodesList == null) {
            System.err.println("episodesList VBox is null, cannot update display.");
            return;
        }
        episodesList.getChildren().clear();

        if (!isSeriesContent || seasonNames == null || seasonNames.isEmpty() ||
                currentSeasonIndex < 0 || currentSeasonIndex >= seasonNames.size()) {
            if (episodesLabel != null) episodesLabel.setText("Nessun episodio da mostrare.");
            return;
        }

        String currentSeasonKey = seasonNames.get(currentSeasonIndex);
        List<Map<String, Object>> episodesForSeason = seasonsData.get(currentSeasonKey);

        if (episodesForSeason != null && !episodesForSeason.isEmpty()) {
            for (Map<String, Object> episodeData : episodesForSeason) {
                HBox episodeItemNode = createEpisodeItem(episodeData);
                episodesList.getChildren().add(episodeItemNode);
            }
            if (episodesLabel != null) {
                episodesLabel.setText(episodesForSeason.size() + (episodesForSeason.size() == 1 ? " episodio" : " episodi"));
                episodesLabel.setVisible(true);
                episodesLabel.setManaged(true);
            }
        } else {
            if (episodesLabel != null) {
                episodesLabel.setText("Nessun episodio in questa stagione.");
                episodesLabel.setVisible(true);
                episodesLabel.setManaged(true);
            }
            // Optionally display a message in episodesList itself
            Label noEpisodesMsg = new Label("Non ci sono episodi disponibili per " + currentSeasonKey + ".");
            noEpisodesMsg.setStyle("-fx-text-fill: #888; -fx-padding: 20;");
            episodesList.getChildren().add(noEpisodesMsg);
        }

        // Update the series title in the episodes section if it's different from main title
        if (seriesTitleLabel != null && titleLabel != null) {
            seriesTitleLabel.setText(titleLabel.getText()); // Or a specific series title for this section
            seriesTitleLabel.setVisible(true);
            seriesTitleLabel.setManaged(true);
        }
    }

    private HBox createEpisodeItem(Map<String, Object> episode) {
        HBox episodeItem = new HBox();
        episodeItem.setAlignment(Pos.CENTER_LEFT);
        episodeItem.setSpacing(15); // Slightly less spacing
        episodeItem.getStyleClass().add("episode-item"); // Define in CSS
        // Basic styling, enhance with CSS
        episodeItem.setStyle("-fx-padding: 15; -fx-background-radius: 6px; -fx-cursor: hand;");

        Label episodeNumber = new Label(String.valueOf(episode.getOrDefault("number", "-")));
        episodeNumber.getStyleClass().add("episode-number");
        episodeNumber.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 16px; -fx-font-weight: bold; -fx-min-width: 25px;");

        ImageView thumbnail = new ImageView();
        // TODO: Load actual thumbnail image if URL is provided in episode map
        // e.g., if (episode.containsKey("thumbnailUrl")) try { thumbnail.setImage(new Image((String)episode.get("thumbnailUrl"), true)); } catch (Exception e) { /* set placeholder */ }
        thumbnail.setFitHeight(70); // Adjusted size
        thumbnail.setFitWidth(120);
        thumbnail.setPreserveRatio(false); // Fill the space
        // Placeholder styling for thumbnail
        Pane thumbnailPlaceholder = new Pane();
        thumbnailPlaceholder.setPrefSize(120, 70);
        thumbnailPlaceholder.setStyle("-fx-background-color: #444; -fx-background-radius: 4px;");
        // You'd add the ImageView to a StackPane with this placeholder if loading fails or as default

        VBox episodeDetails = new VBox(5); // Spacing between title and description
        HBox.setHgrow(episodeDetails, Priority.ALWAYS);

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        // titleRow.setSpacing(10); // Spacer will handle this

        Label episodeTitle = new Label((String) episode.getOrDefault("title", "Titolo non disponibile"));
        episodeTitle.getStyleClass().add("episode-title");
        episodeTitle.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        episodeTitle.setMaxWidth(Double.MAX_VALUE); // Allow title to take space

        Region titleSpacer = new Region(); // Pushes duration to the right
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Label episodeDuration = new Label((String) episode.getOrDefault("duration", "--min"));
        episodeDuration.getStyleClass().add("episode-duration");
        episodeDuration.setStyle("-fx-text-fill: #a0a0a0; -fx-font-size: 13px; -fx-padding: 0 0 0 10px;"); // Padding for separation

        titleRow.getChildren().addAll(episodeTitle, titleSpacer, episodeDuration);

        Label episodeDescription = new Label((String) episode.getOrDefault("description", "Nessuna descrizione."));
        episodeDescription.getStyleClass().add("episode-description");
        episodeDescription.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 13px;");
        episodeDescription.setWrapText(true);
        episodeDescription.setMaxHeight(40); // Limit description lines, show more on click/hover if needed

        episodeDetails.getChildren().addAll(titleRow, episodeDescription);

        episodeItem.setOnMouseClicked(e -> playEpisode(episode));
        final String baseStyle = "-fx-padding: 15; -fx-background-radius: 6px; -fx-cursor: hand;";
        episodeItem.setOnMouseEntered(e -> episodeItem.setStyle(baseStyle + " -fx-background-color: rgba(255, 255, 255, 0.08);"));
        episodeItem.setOnMouseExited(e -> episodeItem.setStyle(baseStyle + " -fx-background-color: transparent;"));


        // episodeItem.getChildren().addAll(episodeNumber, thumbnail, episodeDetails); // if using actual ImageView
        episodeItem.getChildren().addAll(episodeNumber, thumbnailPlaceholder, episodeDetails); // Using placeholder for now

        return episodeItem;
    }

    private void playEpisode(Map<String, Object> episode) {
        System.out.println("Attempting to play episode: " + episode.get("title"));
        System.out.println("Season: " + (seasonNames.isEmpty() ? "N/A" : seasonNames.get(currentSeasonIndex)) +
                ", Episode number: " + episode.get("number") +
                ", Duration: " + episode.get("duration"));
        // TODO: Add actual episode playback logic here
        // For example, mainPagesController.navigateToMediaPlayer(episodeUrl);
    }

    // --- API Simulation & Data Loading ---

    /**
     * Simulates loading content data. Replace with actual API calls.
     * @param contentId ID of the content (movie or series)
     * @param contentType "series" or "movie" (or your defined types)
     */
    public void loadContentData(String contentId, String contentType) {
        System.out.println("Loading data for ID: " + contentId + ", Type: " + contentType);
        // Clear previous data
        seasonNames.clear();
        seasonsData.clear();
        currentSeasonIndex = 0; // Reset season index

        if ("series_placeholder".equalsIgnoreCase(contentType)) { // Using a placeholder type
            // Simulate API call returning series data (like Evangelion from initializeTestData)
            // setTitle, setDescription etc. should be called here with API data
            initializeTestData(); // For now, just re-uses the hardcoded Evangelion
            // In a real scenario:
            // APIService.fetchSeriesDetails(contentId, (seriesDetails, seasons) -> {
            //    Platform.runLater(() -> {
            //      setTitle(seriesDetails.getTitle());
            //      setDescription(seriesDetails.getDescription());
            //      setYear(seriesDetails.getYear());
            //      // ... other details ...
            //      this.seasonNames.clear();
            //      this.seasonsData.clear();
            //      for (Season season : seasons) {
            //          addSeasonObject(season); // Use the new method
            //      }
            //      setupAsSeriesContent();
            //    });
            // });

        } else if ("movie_placeholder".equalsIgnoreCase(contentType)) { // Using a placeholder type
            // Simulate API call returning movie data
            setTitle("Dummy Movie Title");
            setYear("2024");
            setDescription("This is a great action movie with lots of placeholder scenes and CGI.");
            setCast("Actor 1, Actress 2");
            setGenres("Action, Adventure");
            if (ratingLabel != null) ratingLabel.setText("PG-13");
            if (maturityLabel != null) maturityLabel.setText("13+");
            if (violenceLabel != null) violenceLabel.setText("Azione moderata");
            if (episodesLabel != null) episodesLabel.setText(""); // No episodes for movies

            setupAsMovieContent();
            // In a real scenario:
            // APIService.fetchMovieDetails(contentId, (movieDetails) -> {
            //    Platform.runLater(() -> {
            //      setTitle(movieDetails.getTitle());
            //      // ... set other movie details ...
            //      setupAsMovieContent();
            //    });
            // });
        } else {
            System.err.println("Unknown content type: " + contentType);
            // Fallback to a default state or show an error
            setupAsMovieContent(); // Or some empty state
            setTitle("Contenuto non trovato");
            setDescription("Impossibile caricare i dettagli per il contenuto selezionato.");
        }
    }


    // --- Background Image Handling ---
    private ImageView setImageBackground(ImageView newImgView) {
        if (background != null) {
            newImgView.fitWidthProperty().bind(background.widthProperty());
            newImgView.fitHeightProperty().bind(background.heightProperty());
        }
        newImgView.setPreserveRatio(false); // Cover the entire area
        return newImgView;
    }

    private ColorAdjust setColorBackground() {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.65);
        colorAdjust.setSaturation(-0.3);

        GaussianBlur blur = new GaussianBlur(10); // More blur
        colorAdjust.setInput(blur);
        return colorAdjust;
    }

    // --- Action Handlers for Buttons ---
    @FXML
    private void closeView() {
        System.out.println("Close button clicked");
        if (background != null) background.getChildren().clear(); // Clear current view's background
        if (mainPagesController != null) {
            mainPagesController.restorePreviousScene();
        } else {
            System.err.println("mainPagesController is null. Cannot restore previous scene.");
        }
    }

    @FXML
    private void playContent() {
        System.out.println("Play button clicked");
        if (isSeriesContent) {
            if (seasonNames != null && !seasonNames.isEmpty() &&
                    currentSeasonIndex < seasonNames.size() && currentSeasonIndex >= 0) {
                String currentSeasonKey = seasonNames.get(currentSeasonIndex);
                List<Map<String, Object>> episodes = seasonsData.get(currentSeasonKey);
                if (episodes != null && !episodes.isEmpty()) {
                    playEpisode(episodes.get(0)); // Play the first episode of the current season
                } else {
                    System.out.println("No episodes in the current season to play.");
                    // Optionally show a message to the user
                }
            } else {
                System.out.println("No seasons or episodes available to play for this series.");
            }
        } else {
            // This is a movie
            System.out.println("Playing movie: " + (titleLabel != null ? titleLabel.getText() : "N/A"));
            // TODO: Add movie playback logic here
            // For example, mainPagesController.navigateToMediaPlayer(movieUrl);
        }
    }

    @FXML
    private void addToList() {
        System.out.println("Add to list button clicked for: " + (titleLabel != null ? titleLabel.getText() : "N/A"));
        // TODO: Add logic to add content to user's list
    }

    @FXML
    private void showInfo() {
        System.out.println("Info button clicked for: " + (titleLabel != null ? titleLabel.getText() : "N/A"));
        // TODO: Add logic to show more information (e.g., a more detailed popup or view)
    }


    // --- Setters for Content Details ---
    public void setTitle(String title) {
        if (titleLabel != null) titleLabel.setText(title != null ? title : "Titolo Sconosciuto");
    }

    public void setDescription(String description) {
        if (descriptionLabel != null) descriptionLabel.setText(description != null ? description : "Nessuna descrizione disponibile.");
    }

    public void setYear(String year) {
        if (yearLabel != null) yearLabel.setText(year != null ? year : "Anno Sconosciuto");
    }

    // This now refers to the episode count label for the current season
    public void setEpisodeCountForCurrentSeason(String count) {
        if (episodesLabel != null) episodesLabel.setText(count != null ? count : "");
    }

    public void setCast(String cast) {
        if (castLabel != null) castLabel.setText(cast != null ? "Cast: " + cast : "Cast: Non disponibile");
    }

    public void setGenres(String genres) {
        if (genresLabel != null) genresLabel.setText(genres != null ? "Generi: " + genres : "Generi: Non disponibili");
    }

    public void setRating(String rating) {
        if (ratingLabel != null) ratingLabel.setText(rating != null ? rating : "");
    }

    public void setMaturity(String maturity) {
        if (maturityLabel != null) maturityLabel.setText(maturity != null ? maturity : "");
    }
    public void setViolenceLabel(String violence) {
        if (violenceLabel != null) violenceLabel.setText(violence != null ? violence : "");
    }


    // --- Programmatic Data Addition (Example: if loading from API) ---

    /**
     * Adds a season and its episodes to the controller's data.
     * This is useful if your API returns data structured as Season/Episode objects.
     * @param seasonObject The Season object to add.
     */
    public void addSeasonObject(Season seasonObject) {
        if (seasonObject == null || seasonObject.getSeasonName() == null || seasonObject.getSeasonName().isEmpty()) {
            System.err.println("Cannot add null season or season with no name.");
            return;
        }

        String sName = seasonObject.getSeasonName();
        if (!seasonNames.contains(sName)) {
            seasonNames.add(sName);
        }

        List<Map<String, Object>> episodeMapList = new ArrayList<>();
        if (seasonObject.getEpisodes() != null) {
            for (Episode ep : seasonObject.getEpisodes()) {
                episodeMapList.add(ep.toMap()); // Convert Episode object to Map
            }
        }
        seasonsData.put(sName, episodeMapList);

        // If this is the first season being added, set it as current
        if (seasonNames.size() == 1) {
            currentSeasonIndex = 0;
        }

        // If the view is already set up for series, refresh UI components
        // This should ideally be called after all data is loaded, not per season.
        // Consider a bulk update method or refresh after all seasons are added.
        // if (isSeriesContent && seasonsContainer != null) {
        //     updateSeasonsDropdown();
        //     updateEpisodesDisplay();
        // }
    }

    /**
     * To be called after all seasons (from API for example) have been added via addSeasonObject.
     */
    public void refreshSeriesDisplayAfterDataLoad() {
        if (isSeriesContent) {
            if (seasonNames != null && !seasonNames.isEmpty()) {
                currentSeasonIndex = 0; // Default to first season
                if(seasonsContainer == null && episodesList != null && episodesList.getParent() instanceof VBox) {
                    // If selector was never created because data wasn't there initially
                    createSeasonsSelector();
                } else {
                    updateSeasonsDropdown();
                }
                updateEpisodesDisplay();
            } else {
                // Handle case where there are no seasons for a series.
                hideSeasonsSelector();
                if(episodesList != null) episodesList.getChildren().clear();
                if(episodesLabel != null) episodesLabel.setText("Nessuna stagione disponibile per questa serie.");
            }
        }
    }


    /**
     * Gets the data for the currently selected season.
     * @return A List of Maps, where each map represents an episode. Returns null if not a series or no data.
     */
    public List<Map<String, Object>> getCurrentSeasonEpisodes() {
        if (isSeriesContent && seasonNames != null && !seasonNames.isEmpty() &&
                currentSeasonIndex >= 0 && currentSeasonIndex < seasonNames.size()) {
            return seasonsData.get(seasonNames.get(currentSeasonIndex));
        }
        return null;
    }
}