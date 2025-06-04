package com.esa.moviestar.home;

import com.esa.moviestar.Main;
import com.esa.moviestar.model.User;
import com.esa.moviestar.profile.CreateProfileController;
import com.esa.moviestar.settings.SettingsViewController;
import com.esa.moviestar.components.BufferAnimation;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Content;
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


    // FXML elements
    @FXML private AnchorPane body;
    @FXML private StackPane headerContainer;
    @FXML private AnchorPane root;

    // Constants
    public static final String PATH_CARD_WINDOW = "/com/esa/moviestar/movie_view/WindowCard.fxml";
    public static final String PATH_CARD_VERTICAL = "/com/esa/moviestar/movie_view/FilmCard_Vertical.fxml";
    public static final String PATH_CARD_HORIZONTAL = "/com/esa/moviestar/movie_view/FilmCard_Horizontal.fxml";
    public static final String FILM_SCENE_PATH ="/com/esa/moviestar/movie_view/filmScene.fxml";
    // UI Colors
    public static final Color FORE_COLOR = Color.rgb(240, 236, 253);
    public static final Color BACKGROUND_COLOR = Color.rgb(16, 16, 16);

    // Instance variables
    private User user;
    private Account account;



    // Page data containers
    private record PageData(Node node, Object controller) {}
    private PageData header;
    private PageData home;
    private PageData filter_film;
    private PageData filter_series;
    private PageData currentScene;
    private PageData pageBeforeSearch; // For restoring state after search is cleared
    private PageData savedPageData;    // For restoring state after film scene

    private boolean transitionInProgress = false;
    private BufferAnimation loadingSpinner;
    private StackPane loadingOverlay;
    static final double FADE_DURATION = 300; // milliseconds




    public void first_load(User user, Account account) {
        if (loadingOverlay == null) {
            createLoadingOverlay();
            body.getChildren().add(loadingOverlay);
        }
        showLoadingSpinner();
        this.user = user;
        this.account = account;


        if (header == null)
            loadHeader();


        // After attempting to load header, check if it's usable
        if (this.header == null || this.header.controller() == null) {
            System.err.println("MainPagesController: Critical error - Header or its controller failed to load. Aborting first_load.");
            hideLoadingSpinner();
            return;
        }

        ((HeaderController) this.header.controller()).setProfileIcon(user.getIcon());

        CompletableFuture.runAsync(() -> {
            showLoadingSpinner(); // Show spinner for home page loading
            PageData homeData = loadDynamicBody("home.fxml");
            if (homeData != null) {
                Platform.runLater(() -> {
                    HomeController homeBodyController = (HomeController) homeData.controller();
                    homeBodyController.setRecommendations(user, MainPagesController.this);
                    this.home = homeData;
                    transitionToPage(homeData); // This will also call hideLoadingSpinner
                });
            } else {
                Platform.runLater(() -> {
                    hideLoadingSpinner();
                    System.err.println("MainPagesController: Home page failed to load.");
                    // Optionally, display an error message to the user
                });
            }
        });
    }

    private void createLoadingOverlay() {
        loadingSpinner = new BufferAnimation(128);
        loadingOverlay = new StackPane();
        loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        loadingOverlay.getChildren().add(loadingSpinner);
        loadingOverlay.setAlignment(Pos.CENTER);

        AnchorPane.setBottomAnchor(loadingOverlay, 0.0);
        AnchorPane.setTopAnchor(loadingOverlay, 0.0);
        AnchorPane.setLeftAnchor(loadingOverlay, 0.0);
        AnchorPane.setRightAnchor(loadingOverlay, 0.0);

        loadingOverlay.setVisible(false);
        loadingOverlay.setOpacity(0);
    }

    private void loadHeader() {
        header = loadDynamicBody("header.fxml");
        if (header == null || header.controller() == null) {
            System.err.println("MainPagesController: Error to load header or its controller");
            return;
        }

        headerContainer.getChildren().add(header.node());
        HeaderController headerController = (HeaderController) header.controller();

        if (user != null) {
            headerController.setUpPopUpMenu(this, user, account);
        }

        setupNavigationButtons(headerController);

        headerController.getTbxSearch().textProperty().addListener((observableValue, oldV, newV) -> {
            if (newV.isEmpty()) {
                if (currentScene != null && currentScene.controller() instanceof SearchController) {
                    PageData pageToReturnTo = null;
                    String pageNameForButtonActivation = null;

                    // Determine the page to return to and its corresponding name for button activation
                    if (this.pageBeforeSearch != null && this.pageBeforeSearch.controller() != null &&
                            !(this.pageBeforeSearch.controller() instanceof SearchController)) {
                        pageToReturnTo = this.pageBeforeSearch;
                        if (pageToReturnTo == this.home) pageNameForButtonActivation = "home";
                        else if (pageToReturnTo == this.filter_film) pageNameForButtonActivation = "film filter";
                        else if (pageToReturnTo == this.filter_series) pageNameForButtonActivation = "series filter";
                    }

                    // Fallback to home page if pageBeforeSearch is not suitable
                    if (pageToReturnTo == null && this.home != null) {
                        pageToReturnTo = this.home;
                        pageNameForButtonActivation = "home";
                    }

                    if (pageToReturnTo != null && pageNameForButtonActivation != null) {
                        final PageData finalPageToReturnTo = pageToReturnTo;

                        loadPageAsync(pageNameForButtonActivation, () -> {

                            return finalPageToReturnTo; // Return the already determined (and likely cached) page
                        });

                        if (this.pageBeforeSearch == pageToReturnTo) {
                            this.pageBeforeSearch = null; // Consumed
                        }
                    }
                }
                return; // Exit after handling empty search text
            }

            // User is typing in search or search text is not empty
            // Store the current page if we are navigating TO the search page from a non-search page
            if (currentScene != null && !(currentScene.controller() instanceof SearchController)) {
                // Only update pageBeforeSearch if it's not already the search page
                // and if it's different from the current pageBeforeSearch
                if (this.pageBeforeSearch == null || (this.pageBeforeSearch.node() != currentScene.node())) {
                    this.pageBeforeSearch = currentScene;
                }
            }

            // Load the search page results
            loadPageAsync("search", () -> {
                PageData searchResultPage = loadDynamicBody("search.fxml");
                // headerController.activeSearch(); // This is handled by loadPageAsync
                if (searchResultPage != null && searchResultPage.controller() instanceof SearchController) {
                    ((SearchController) searchResultPage.controller()).setParamController(
                            headerController, user,  this);
                } else if (searchResultPage == null) {
                    System.err.println("MainPagesController: Search page (search.fxml) failed to load.");
                } else {
                    System.err.println("MainPagesController: Search page loaded, but controller is not SearchController.");
                }
                return searchResultPage;
            });
        });
    }

    private void setupNavigationButtons(HeaderController headerController) {
        headerController.homeButton.setOnMouseClicked(e -> {
            if (currentScene == home || transitionInProgress) return;
            loadPageAsync("home", () -> {
                if (home == null) {
                    home = loadDynamicBody("home.fxml");
                    if (home != null) {
                        ((HomeController) home.controller()).setRecommendations(user, this);
                    }
                }
                return home;
            });
        });

        headerController.filmButton.setOnMouseClicked(e -> {
            if (currentScene == filter_film || transitionInProgress) return;
            loadPageAsync("film filter", () -> {
                if (filter_film == null) {
                    filter_film = loadDynamicBody("filter.fxml");
                    if (filter_film != null) {
                        ((FilterController) filter_film.controller()).setContent(this, user, true);
                    }
                }
                return filter_film;
            });
        });

        headerController.seriesButton.setOnMouseClicked(e -> {
            if (currentScene == filter_series || transitionInProgress) return;
            loadPageAsync("series filter", () -> {
                if (filter_series == null) {
                    filter_series = loadDynamicBody("filter.fxml");
                    if (filter_series != null) {
                        ((FilterController) filter_series.controller()).setContent(this, user, false);
                    }
                }
                return filter_series;
            });
        });
    }

    private void showLoadingSpinner() {
        Platform.runLater(() -> {
            if (loadingSpinner == null || loadingOverlay == null) createLoadingOverlay(); // Defensive
            loadingSpinner.startAnimation();
            loadingOverlay.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), loadingOverlay);
            fadeIn.setFromValue(loadingOverlay.getOpacity()); // Fade from current opacity
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
    }
    private void hideLoadingSpinner() {
        Platform.runLater(() -> {
            if (loadingSpinner == null || loadingOverlay == null) return; // Nothing to hide
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), loadingOverlay);
            fadeOut.setFromValue(loadingOverlay.getOpacity()); // Fade from current opacity
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                loadingOverlay.setVisible(false);
                loadingSpinner.stopAnimation();
            });
            fadeOut.play();
        });
    }

    private void loadPageAsync(String pageName, java.util.function.Supplier<PageData> pageSupplier) {
        if (header == null || header.controller() == null) {
            System.err.println("MainPagesController.loadPageAsync: Header or its controller is null. Cannot proceed.");
            hideLoadingSpinner(); // Ensure spinner is hidden if we can't proceed
            return;
        }
        HeaderController headerController = (HeaderController)header.controller();

        CompletableFuture.runAsync(() -> {
            try {
                // Activate button on the JavaFX Application Thread before showing spinner
                Platform.runLater(() -> {
                    switch(pageName){
                        case "home":
                            headerController.activeButton(headerController.homeButton);
                            break;
                        case "film filter":
                            headerController.activeButton(headerController.filmButton);
                            break;
                        case "series filter":
                            headerController.activeButton(headerController.seriesButton);
                            break;
                        case "search":
                            headerController.activeSearch();
                            break;
                    }
                });

                showLoadingSpinner(); // Now show spinner
                PageData page = pageSupplier.get(); // This can be time-consuming

                if (page != null) {
                    Platform.runLater(() -> transitionToPage(page));
                } else {
                    Platform.runLater(this::hideLoadingSpinner);
                    System.err.println("MainPagesController: Failed to load " + pageName + " page (supplier returned null)");
                }
            } catch (Exception e) {
                Platform.runLater(this::hideLoadingSpinner);
                System.err.println("MainPagesController: Error to load " + pageName + " page: " + e.getMessage());
                e.printStackTrace(); //suca no gemini aruqqq
            }
        });
    }

    private PageData loadDynamicBody(String bodySource) {
        try {
            var resource = getClass().getResource(bodySource);
            if (resource == null) {
                System.err.println("MainPagesController: Resource not found: " + bodySource + " (relative to " + getClass().getPackageName() + ")");
                return null;
            }
            FXMLLoader loader = new FXMLLoader(resource, Main.resourceBundle);
            Node pageNode = loader.load();
            AnchorPane.setBottomAnchor(pageNode, 0.0);
            AnchorPane.setTopAnchor(pageNode, 0.0);
            AnchorPane.setLeftAnchor(pageNode, 0.0);
            AnchorPane.setRightAnchor(pageNode, 0.0);
            return new PageData(pageNode, loader.getController());
        } catch (IOException e) {
            System.err.println("MainPagesController: IOException while loading " + bodySource + ". Details: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("MainPagesController: Unexpected error while loading " + bodySource + ". Details: " + e.getMessage());
            e.printStackTrace(); // Good for debugging
            return null;
        }
    }

    /**
     * Captures screenshot and prepares for film scene overlay.
     * @return ImageView containing the screenshot
     */
    private ImageView captureScreenshotView() {
        if (root.getScene() == null || root.getScene().getWindow() == null) {
            System.err.println("MainPagesController: Cannot capture screenshot, scene or window not available.");
            return new ImageView(); // Return empty image view to prevent NPE
        }
        Stage stage = (Stage) root.getScene().getWindow();
        Scene scene = stage.getScene();
        double width = scene.getWidth();
        double height = scene.getHeight();
        WritableImage screenshot = new WritableImage((int) width, (int) height);
        scene.snapshot(screenshot);
        return new ImageView(screenshot);
    }

    public void restorePreviousScene() {
        if (this.savedPageData != null) {
            System.out.println("Restoring previous scene"); // Removed "with slide animation" as it's a fade

            root.getChildren().clear();
            // Ensure body and headerContainer are added back in the correct order
            root.getChildren().add(body); // Body first
            root.getChildren().add(headerContainer); // Header on top of body (conceptually, layout handles actual position)


            body.getChildren().clear();
            Node pageNodeToRestore = savedPageData.node();
            pageNodeToRestore.setOpacity(1.0); // Ensure it's visible
            body.getChildren().add(pageNodeToRestore);

            if (loadingOverlay != null) {
                if (!body.getChildren().contains(loadingOverlay)) {
                    body.getChildren().add(loadingOverlay); // Add it if not present
                }
                // Ensure loading overlay is hidden
                loadingOverlay.setOpacity(0);
                loadingOverlay.setVisible(false);
                if (loadingSpinner != null) {
                    loadingSpinner.stopAnimation();
                }
            }

            this.currentScene = this.savedPageData;
            this.savedPageData = null; // Consumed
            this.transitionInProgress = false; // Reset transition flag
        } else {
            System.err.println("Cannot restore scene: savedPageData is null");
        }
    }

    // Updated to accept filmId
    public void openFilmScene(int filmId) {
        ImageView screenshotView = captureScreenshotView();
        PageData filmDetail = loadDynamicBody(FILM_SCENE_PATH);

        if (filmDetail != null && filmDetail.controller() instanceof FilmSceneController filmController) {
            filmController.setProperties(screenshotView, this);
            // filmController.loadFilmData(filmId); // This should be called by FilmSceneController itself
            this.savedPageData = this.currentScene; // Save current page before switching

            root.getChildren().clear();
            root.getChildren().add(filmDetail.node());
            // currentScene will be the filmDetail page implicitly after this
        } else {
            System.err.println("MainPagesController: Failed to load film scene");
        }
    }

    private void transitionToPage(PageData newPage) {
        if (newPage == null || newPage.node() == null || (currentScene == newPage && !body.getChildren().isEmpty() && body.getChildren().contains(newPage.node())) ) {
            if (newPage == null || newPage.node() == null) hideLoadingSpinner(); // Still hide if page is invalid
            if (currentScene == newPage && !transitionInProgress) hideLoadingSpinner(); // If already on page, just hide spinner
            if (transitionInProgress && (currentScene == newPage)) return; // Avoid re-transition to same page if already in progress
            if (newPage == null || newPage.node() == null) return;
        }
        if (transitionInProgress) return; // Prevent concurrent transitions


        transitionInProgress = true;

        Node currentNode = null;
        // Find the current visible page node, excluding the loading overlay
        for(Node child : body.getChildren()){
            if(child != loadingOverlay){ // Make sure not to fade out the loading overlay itself
                currentNode = child;
                break;
            }
        }


        if (currentNode != null && currentNode != newPage.node()) { // Only fade out if there's a different current node
            FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_DURATION), currentNode);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            Node finalCurrentNode = currentNode; // For use in lambda
            fadeOut.setOnFinished(e -> {
                body.getChildren().remove(finalCurrentNode);
                showNewPage(newPage);
            });
            fadeOut.play();
        } else { // No current node to fade out, or it's the same node (e.g., initial load)
            if (currentNode == null && newPage.node() != null && !body.getChildren().contains(newPage.node())) {
                // If no current node, directly add and show the new page
            } else if (body.getChildren().contains(newPage.node())) {
                // If the new page is already there (e.g. after a failed load then success), ensure it's visible
                newPage.node().setOpacity(1.0);
                currentScene = newPage;
                transitionInProgress = false;
                hideLoadingSpinner();
                return;
            }
            showNewPage(newPage);
        }
    }

    private void showNewPage(PageData newPage) {
        Node pageNode = newPage.node();
        if (pageNode == null) { // Guard against null pageNode
            transitionInProgress = false;
            hideLoadingSpinner();
            System.err.println("MainPagesController.showNewPage: newPage.node() is null.");
            return;
        }

        pageNode.setOpacity(0); // Start transparent for fade-in
        // Add the new page node if it's not already present and not the loading overlay
        if (!body.getChildren().contains(pageNode) && pageNode != loadingOverlay) {
            body.getChildren().addFirst(pageNode); // Add to the bottom of the stack (rendered first)
        }


        FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION), pageNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e -> {
            currentScene = newPage;
            transitionInProgress = false;
            hideLoadingSpinner();
        });
        fadeIn.play();
    }

    public  List<Node> createFilmNodes(List<Content> contentList, boolean isVertical) throws IOException {
        List<Node> nodes = new ArrayList<>(contentList.size());
        String cardPath = isVertical ? PATH_CARD_VERTICAL : PATH_CARD_HORIZONTAL;

        for (Content content : contentList) {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(cardPath), "FXML resource for card not found: " + cardPath),
                    Main.resourceBundle
            );
            Node node = fxmlLoader.load();
            FilmCardController filmCardController = fxmlLoader.getController();
            filmCardController.setContent(content,isVertical);
            node.setOnMouseClicked(e -> cardClicked(filmCardController.getCardId()));
            nodes.add(node);
        }
        return nodes;
    }

    public void cardClicked(int cardId) {
        openFilmScene(cardId);
    }

    public void cardClickedPlay(int cardId) {
        // Implement playback functionality
        System.out.println("Play card clicked (not implemented): " + cardId);
    }

    public void settingsClick(User user, Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/settings-view.fxml"), Main.resourceBundle);
            Parent settingContent = loader.load();
            SettingsViewController settingsViewController = loader.getController();
            settingsViewController.setUtente(user);
            settingsViewController.setAccount(account); // Pass account

            Scene currentSceneNode = body.getScene(); // Use body.getScene() for consistency
            if (currentSceneNode == null) {
                System.err.println("MainPagesController.settingsClick: body.getScene() is null.");
                return;
            }
            Scene newScene = new Scene(settingContent, currentSceneNode.getWidth(), currentSceneNode.getHeight());
            Stage stage = (Stage) currentSceneNode.getWindow();
            stage.setScene(newScene);
        } catch (IOException e) {
            System.err.println("MainPagesController: Errore caricamento pagina dei setting: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void emailClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"), Main.resourceBundle);
            Parent accessContent = loader.load();
            Scene currentSceneNode = body.getScene();
            if (currentSceneNode == null) {
                System.err.println("MainPagesController.emailClick: body.getScene() is null.");
                return;
            }
            Scene newScene = new Scene(accessContent, currentSceneNode.getWidth(), currentSceneNode.getHeight());
            Stage stage = (Stage) currentSceneNode.getWindow();
            stage.setScene(newScene);
        } catch (IOException ex) {
            System.err.println("MainPagesController: Errore caricamento pagina di accesso dell'account: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void profileClick(User newUser) {
        if (transitionInProgress) return;

        showLoadingSpinner(); // Show spinner before starting the reset

        // Clear main containers on JavaFX thread after current operations might finish
        Platform.runLater(() -> {
            if (loadingSpinner != null) loadingSpinner.stopAnimation(); // Stop any previous animation

            if (body != null) body.getChildren().clear();
            if (headerContainer != null) headerContainer.getChildren().clear();

            // Reset page data and state
            header = null;
            home = null;
            filter_film = null;
            filter_series = null;
            currentScene = null;
            pageBeforeSearch = null;
            savedPageData = null;
            // loadingOverlay will be recreated by first_load if needed, or re-added if createLoadingOverlay is called
            // transitionInProgress will be reset by the first_load -> transitionToPage flow

            this.user = newUser; // Update to the new user

            // Re-initialize the UI for the new user
            first_load(this.user, this.account);
        });
    }

    public void createProfileUser(Account account){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/create-profile-view.fxml"), Main.resourceBundle);
            Parent createContent = loader.load();
            CreateProfileController createProfileController = loader.getController();
            createProfileController.setSource(CreateProfileController.Origine.HOME);
            createProfileController.setAccount(account);
            createProfileController.setUser(this.user); // Pass current user context

            Scene currentSceneNode = body.getScene();
            if (currentSceneNode == null) {
                System.err.println("MainPagesController.createProfileUser: body.getScene() is null.");
                return;
            }
            Scene newScene = new Scene(createContent, currentSceneNode.getWidth(), currentSceneNode.getHeight());
            Stage stage = (Stage) currentSceneNode.getWindow();
            stage.setScene(newScene);
        }catch(IOException e){
            System.err.println("MainPagesController : errore caricamento pagina di creazione profili: " + e.getMessage());
            e.printStackTrace();
        }
    }
}