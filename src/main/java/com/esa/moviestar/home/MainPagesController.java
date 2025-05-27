package com.esa.moviestar.home;

import com.esa.moviestar.profile.CreateProfileController;
import com.esa.moviestar.settings.SettingsViewController;
import com.esa.moviestar.components.BufferAnimation;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.Utente;
import com.esa.moviestar.movie_view.FilmCardController;
import com.esa.moviestar.movie_view.FilmSceneController;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MainPagesController {
    private static final double FADE_DURATION = 300; // milliseconds

    // FXML elements
    @FXML private AnchorPane body;
    @FXML private StackPane headerContainer;
    @FXML private AnchorPane root;

    // Constants
    public final ResourceBundle resourceBundle = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");
    public final String PATH_WINDOW_CARD = "/com/esa/moviestar/movie_view/WindowCard.fxml";
    public final String PATH_CARD_VERTICAL = "/com/esa/moviestar/movie_view/FilmCard_Vertical.fxml";
    public final String PATH_CARD_HORIZONTAL = "/com/esa/moviestar/movie_view/FilmCard_Horizontal.fxml";

    // UI Colors
    public final Color FORE_COLOR = Color.rgb(240, 236, 253);
    public final Color BACKGROUND_COLOR = Color.rgb(16, 16, 16);

    // Page data containers
    private record PageData(Node node, Object controller) {}

    // Instance variables
    private Utente user;
    private Account account;
    private PageData header;
    private Node savedSceneNode; // Missing in first file
    private PageData home;
    private PageData filter_film;
    private PageData filter_series;
    private PageData currentScene;
    private boolean transitionInProgress = false;
    private BufferAnimation loadingSpinner;
    private StackPane loadingOverlay;

    // Account setter - present in first file
    public void setAccount(Account account) {
        this.account = account;
    }

    public void initialize() {
        createLoadingOverlay();
        showLoadingSpinner();
    }

    /**
     * Initializes the main page with the user's data
     * @param user The current user
     * @param account The current account
     */
    public void first_load(Utente user, Account account) {
        this.user = user;
        this.account = account;
        if (loadingOverlay == null)
            createLoadingOverlay();

        // Load header if not already loaded
        if (header == null) {
            loadHeader();
        }

        // Set profile icon
        ((HeaderController) header.controller).setProfileIcon(user.getIcona());

        // Load home page asynchronously
        CompletableFuture.runAsync(() -> {
            showLoadingSpinner();
            PageData home = loadDynamicBody("home.fxml");
            if (home != null) {
                Platform.runLater(() -> {
                    HomeController homeBodyController = (HomeController) home.controller;
                    homeBodyController.setRecommendations(user, MainPagesController.this);
                    this.home = home;
                    transitionToPage(home);
                });
            }
        });
    }

    /**
     * Creates the loading overlay with spinner
     */
    private void createLoadingOverlay() {
        // Create loading spinner
        loadingSpinner = new BufferAnimation(128);

        // Create overlay container
        loadingOverlay = new StackPane();
        loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        loadingOverlay.getChildren().add(loadingSpinner);
        loadingOverlay.setAlignment(Pos.CENTER);

        // Set anchors for the overlay
        AnchorPane.setBottomAnchor(loadingOverlay, 0.0);
        AnchorPane.setTopAnchor(loadingOverlay, 0.0);
        AnchorPane.setLeftAnchor(loadingOverlay, 0.0);
        AnchorPane.setRightAnchor(loadingOverlay, 0.0);

        // Initially invisible
        loadingOverlay.setVisible(false);
        loadingOverlay.setOpacity(0);
    }

    /**
     * Loads the header and configures its event handlers
     */
    private void loadHeader() {
        header = loadDynamicBody("header.fxml");
        if (header == null) {
            System.err.println("MainPagesController: Error to load header");
            return;
        }

        headerContainer.getChildren().add(header.node);
        HeaderController headerController = (HeaderController) header.controller;

        if (user != null) {
            // Updated to include account parameter
            headerController.setUpPopUpMenu(this, user, account);
        }

        // Configure navigation buttons
        setupNavigationButtons(headerController);

        // Configure search functionality - Updated method name
        headerController.getTbxSearch().textProperty().addListener((observableValue, oldV, newV) -> {
            if (newV.isEmpty()) {
                try {
                    body.getChildren().clear();
                    body.getChildren().add(currentScene.node);
                } catch (Exception e) {
                    System.err.println("MainPagesController: tbxSearchListener error \n Error:" + e.getMessage());
                }
                return;
            }
            PageData search = loadDynamicBody("search.fxml");
            if (search != null) {
                try {
                    // Updated method name from second file
                    ((SearchController) search.controller).set_paramcontroller((HeaderController) header.controller, user, resourceBundle, this);
                    body.getChildren().clear();
                    body.getChildren().add(search.node);
                    currentScene = search;
                } catch (Exception e) {
                    System.err.println("MainPagesController: tbxSearchListener error \n Error:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        // Add the loading overlay to the body
        body.getChildren().add(loadingOverlay);
    }

    /**
     * Sets up the navigation buttons in the header
     * @param headerController The header controller
     */
    private void setupNavigationButtons(HeaderController headerController) {
        // Home button
        headerController.homeButton.setOnMouseClicked(e -> {
            if (currentScene == home || transitionInProgress) {
                return;
            }

            loadPageAsync("home", () -> {
                if (home == null) {
                    home = loadDynamicBody("home.fxml");
                    if (home != null) {
                        ((HomeController) home.controller).setRecommendations(user, this);
                    }
                }
                return home;
            });
        });

        // Film button
        headerController.filmButton.setOnMouseClicked(e -> {
            if (currentScene == filter_film || transitionInProgress) {
                return;
            }

            loadPageAsync("film filter", () -> {
                if (filter_film == null) {
                    filter_film = loadDynamicBody("filter.fxml");
                    if (filter_film != null) {
                        ((FilterController) filter_film.controller).loadWithFilter(this, user, true);
                    }
                }
                return filter_film;
            });
        });

        // Series button
        headerController.seriesButton.setOnMouseClicked(e -> {
            if (currentScene == filter_series || transitionInProgress) {
                return;
            }

            loadPageAsync("series filter", () -> {
                if (filter_series == null) {
                    filter_series = loadDynamicBody("filter.fxml");
                    if (filter_series != null) {
                        ((FilterController) filter_series.controller).loadWithFilter(this, user, false);
                    }
                }
                return filter_series;
            });
        });
    }

    /**
     * Shows the loading spinner with a fade-in effect
     */
    private void showLoadingSpinner() {
        Platform.runLater(() -> {
            loadingSpinner.startAnimation();
            loadingOverlay.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), loadingOverlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
    }

    /**
     * Hides the loading spinner with a fade-out effect
     */
    private void hideLoadingSpinner() {
        Platform.runLater(() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), loadingOverlay);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                loadingOverlay.setVisible(false);
                loadingSpinner.stopAnimation();
            });
            fadeOut.play();
        });
    }

    /**
     * Asynchronously loads a page
     * @param pageName The name of the page for logging
     * @param pageSupplier A supplier that returns the page data
     */
    private void loadPageAsync(String pageName, java.util.function.Supplier<PageData> pageSupplier) {
        CompletableFuture.runAsync(() -> {
            try {
                showLoadingSpinner();
                PageData page = pageSupplier.get();
                if (page != null) {
                    Platform.runLater(() -> transitionToPage(page));
                } else {
                    hideLoadingSpinner();
                    System.err.println("MainPagesController: Failed to load " + pageName + " page");
                }
            } catch (Exception e) {
                hideLoadingSpinner();
                System.err.println("MainPagesController: Error to load " + pageName + " page");
            }
        });
    }

    /**
     * Dynamically loads FXML content - Enhanced version from second file
     * @param bodySource The FXML file to load
     * @return A PageData object containing the node and controller
     */
    private PageData loadDynamicBody(String bodySource) {
        try {
            // Add more detailed logging
            System.out.println("MainPagesController: Attempting to load: " + bodySource);

            // Check if resource exists
            var resource = getClass().getResource(bodySource);
            if (resource == null) {
                System.err.println("MainPagesController: Resource not found: " + bodySource);
                System.err.println("MainPagesController: Looking in package: " + getClass().getPackageName());
                return null;
            }

            System.out.println("MainPagesController: Resource found at: " + resource.toString());

            FXMLLoader loader = new FXMLLoader(resource, resourceBundle);
            Node pageNode = loader.load();

            // Set anchors for proper layout
            AnchorPane.setBottomAnchor(pageNode, 0.0);
            AnchorPane.setTopAnchor(pageNode, 0.0);
            AnchorPane.setLeftAnchor(pageNode, 0.0);
            AnchorPane.setRightAnchor(pageNode, 0.0);

            System.out.println("MainPagesController: Successfully loaded: " + bodySource);
            return new PageData(pageNode, loader.getController());

        } catch (IOException e) {
            System.err.println("MainPagesController: IOException while loading " + bodySource);
            System.err.println("MainPagesController: Error details: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("MainPagesController: Unexpected error while loading " + bodySource);
            System.err.println("MainPagesController: Error details: " + e.getMessage());
            return null;
        }
    }

    /**
     * MISSING FUNCTION: Captures screenshot and prepares for film scene overlay
     * @return ImageView containing the screenshot
     */
    private ImageView deletedynamicbody() {
        // Store the current scene node for later restoration
        Node savedNode = root;

        // Add a new field to the class to store this for later
        this.savedSceneNode = savedNode;
        // Create a screenshot of the current scene
        Stage stage = (Stage) root.getScene().getWindow();
        Scene scene = stage.getScene();
        double width = scene.getWidth();
        double height = scene.getHeight();
        WritableImage screenshot = new WritableImage((int) width, (int) height);
        scene.snapshot(screenshot);
        ImageView screenshotView = new ImageView(screenshot);

        // Clear current contents to prepare for the new overlay
        body.getChildren().clear();

        return screenshotView;
    }

    /**
     * MISSING FUNCTION: Restores the previously saved scene
     */
    public void restorePreviousScene() {
        if (savedSceneNode != null) {
            System.out.println("Restoring previous scene");
            // Apply fade-out transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_DURATION), body);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {

                root = (AnchorPane) savedSceneNode;

                // Apply fade-in transition
                FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION), body);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        }
    }

    /**
     * MISSING FUNCTION: Opens film scene with background screenshot
     * @param filmFxmlPath Path to the film FXML file
     */
    public void openFilmScene(String filmFxmlPath) {
        // Capture screenshot and clean scene
        ImageView screenshotView = deletedynamicbody();

        // Load film detail

        PageData filmDetail = loadDynamicBody(filmFxmlPath);

        if (filmDetail != null) {
            FilmSceneController filmController = ((FilmSceneController) filmDetail.controller());
            filmController.setProperties(screenshotView, this);
            root.getChildren().clear();
            root.getChildren().add(filmDetail.node);
        }
    }



    /**
     * Transitions to a new page with a fade effect
     * @param newPage The page to transition to
     */
    private void transitionToPage(PageData newPage) {
        if (newPage == null || transitionInProgress) {
            return;
        }

        transitionInProgress = true;

        // If there's already content (besides the loading overlay), fade it out first
        if (body.getChildren().size() > 1) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_DURATION),
                    body.getChildren().get(0));
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                // Remove all children except the loading overlay
                body.getChildren().removeIf(node -> node != loadingOverlay);
                showNewPage(newPage);
            });
            fadeOut.play();
        } else {
            showNewPage(newPage);
        }
    }

    /**
     * Shows a new page with a fade-in effect
     * @param newPage The page to show
     */
    private void showNewPage(PageData newPage) {
        Node pageNode = newPage.node;
        pageNode.setOpacity(0);

        // Add the new page below the loading overlay
        body.getChildren().add(0, pageNode);

        // Create fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION), pageNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e -> {
            currentScene = newPage;
            transitionInProgress = false;
            // Hide the loading spinner after page transition is complete
            hideLoadingSpinner();
        });
        fadeIn.play();
    }

    /**
     * Creates film card nodes from a list of content
     * @param contentList List of content to create nodes for
     * @param isVertical Whether to use vertical or horizontal layout
     * @return List of content nodes
     * @throws IOException If loading fails
     */
    public List<Node> createFilmNodes(List<Content> contentList, boolean isVertical) throws IOException {
        List<Node> nodes = new ArrayList<>(contentList.size());
        String cardPath = isVertical ? PATH_CARD_VERTICAL : PATH_CARD_HORIZONTAL;

        for (Content content : contentList) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(
                        Objects.requireNonNull(getClass().getResource(cardPath)),
                        resourceBundle
                );

                Node node = fxmlLoader.load();
                FilmCardController filmCardController = fxmlLoader.getController();
                filmCardController.setContent(content);

                // Set click handler
                node.setOnMouseClicked(e -> cardClicked(filmCardController.getCardId()));
                nodes.add(node);
            } catch (IOException e) {
                System.err.println("MainPagesController: Failed to load content");
                throw e; // Re-throw to maintain original behavior
            }
        }

        return nodes;
    }

    /**
     * Handles card click events
     * @param cardId The ID of the clicked card
     */
    public void cardClicked(int cardId) {
        if (transitionInProgress) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            showLoadingSpinner();
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/esa/moviestar/movie_view/filmInterface.fxml"),
                        resourceBundle
                );

                Node filmInterface = loader.load();

                // Set anchors
                AnchorPane.setBottomAnchor(filmInterface, 0.0);
                AnchorPane.setTopAnchor(filmInterface, 0.0);
                AnchorPane.setLeftAnchor(filmInterface, 0.0);
                AnchorPane.setRightAnchor(filmInterface, 0.0);

                // Create film page data
                PageData filmPage = new PageData(filmInterface, loader.getController());

                Platform.runLater(() -> transitionToPage(filmPage));
            } catch (IOException e) {
                hideLoadingSpinner();
                System.err.println("Failed to load film interface for card ID: " + cardId + "\n" + e.getMessage());
            }
        });
    }

    /**
     * Plays the content associated with the card
     * @param cardId The ID of the card
     */
    public void cardClickedPlay(int cardId) {
        // Implement playback functionality
    }

    /**
     * Opens user settings - Updated to include account parameter
     * @param user The current user
     * @param account The current account
     */
    public void settingsClick(Utente user, Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/settings-view.fxml"), resourceBundle);
            Parent settingContent = loader.load();

            SettingsViewController settingsViewController = loader.getController();
            settingsViewController.setUtente(user);
            settingsViewController.setAccount(account);

            Scene currentScene = body.getScene();
            Scene newScene = new Scene(settingContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) body.getScene().getWindow();
            stage.setScene(newScene);

        } catch (IOException e) {
            System.err.println("MainPagesController: Errore caricamento pagina dei setting" + e.getMessage());
        }
    }

    /**
     * Handles email click events
     */
    public void emailClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"), resourceBundle);
            Parent accessContent = loader.load();

            Scene currentScene = body.getScene();
            Scene newScene = new Scene(accessContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) body.getScene().getWindow();
            stage.setScene(newScene);
        } catch (IOException ex) {
            System.err.println("MainPagesController: Errore caricamento pagina di accesso dell'account: " + ex.getMessage());
        }
    }

    /**
     * Changes the current profile
     * @param user The user to switch to
     */
    public void profileClick(Utente user) {
        // Evita problemi se già in transizione
        if (transitionInProgress) {
            return;
        }

        // Ferma il loading se attivo
        if (loadingSpinner != null) {
            loadingSpinner.stopAnimation();
        }

        // Pulisci tutto
        body.getChildren().clear();
        headerContainer.getChildren().clear();

        // Reset variabili - IMPORTANTE: resetta tutto
        header = null;
        savedSceneNode = null;
        home = null;
        filter_film = null;
        filter_series = null;
        currentScene = null;
        transitionInProgress = false;
        loadingOverlay = null;

        // Aggiorna utente PRIMA di ricaricare
        this.user = user;

        // Ricostruisci tutto da zero
        initialize(); // Ricrea il loadingOverlay
        first_load(user, account); // Questo ricaricherà header e home per il nuovo utente
    }

    public void createProfileUser(Account account){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/create-profile-view.fxml"), resourceBundle);
            Parent createContent = loader.load();

            CreateProfileController createProfileController = loader.getController();
            createProfileController.setOrigine(CreateProfileController.Origine.HOME);
            createProfileController.setAccount(account);
            createProfileController.setUtente(user);

            Scene currentScene = body.getScene();
            Scene newScene = new Scene(createContent, currentScene.getWidth(), currentScene.getHeight());

            Stage stage = (Stage) body.getScene().getWindow();
            stage.setScene(newScene);
        }catch(IOException e){
            System.err.println("MainPagesController : errore caricamento pagina di creazione profili"+e.getMessage());
        }
    }
}