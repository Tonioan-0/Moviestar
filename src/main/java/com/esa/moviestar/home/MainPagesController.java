package com.esa.moviestar.home;

import com.esa.moviestar.Main;
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
    private Utente user;
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




    public void first_load(Utente user, Account account) {
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

        ((HeaderController) this.header.controller()).setProfileIcon(user.getIcona());

        CompletableFuture.runAsync(() -> {
            showLoadingSpinner(); // Show spinner for home page loading
            PageData homeData = loadDynamicBody("home.fxml");
            if (homeData != null) {
                Platform.runLater(() -> {
                    HomeController homeBodyController = (HomeController) homeData.controller();
                    homeBodyController.setRecommendations(user, MainPagesController.this);
                    this.home = homeData;
                    transitionToPage(homeData);
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
                    if (pageBeforeSearch != null && pageBeforeSearch != currentScene) {
                        transitionToPage(pageBeforeSearch);
                        pageBeforeSearch = null; // Consumed
                    } else if (home != null && currentScene != home) { // Fallback to home
                        transitionToPage(home);
                    }
                }
                return;
            }
            if (currentScene == null || !(currentScene.controller() instanceof SearchController)) {
                if (currentScene != null && (pageBeforeSearch == null || (pageBeforeSearch.node() != currentScene.node()))) {
                    pageBeforeSearch = currentScene;
                }
            }

            loadPageAsync("search", () -> {
                PageData searchResultPage = loadDynamicBody("search.fxml");
                headerController.activeSearch();
                if (searchResultPage != null) {
                    try {
                        ((SearchController) searchResultPage.controller()).setParamController(
                                headerController, user,  this);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
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
        CompletableFuture.runAsync(() -> {
            HeaderController headerController = (HeaderController)header.controller();
            try {
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
                showLoadingSpinner();
                PageData page = pageSupplier.get();
                if (page != null) {

                    Platform.runLater(() -> transitionToPage(page));
                } else {
                    Platform.runLater(this::hideLoadingSpinner);
                    System.err.println("MainPagesController: Failed to load " + pageName + " page (supplier returned null)");
                }
            } catch (Exception e) {
                Platform.runLater(this::hideLoadingSpinner);
                System.err.println("MainPagesController: Error to load " + pageName + " page: " + e.getMessage());
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
            System.out.println("Restoring previous scene with slide animation");

            root.getChildren().clear();
            root.getChildren().add(body);
            root.getChildren().add(headerContainer);

            // Clear body and add the saved page node
            body.getChildren().clear();
            Node pageNodeToRestore = savedPageData.node();
            pageNodeToRestore.setOpacity(1.0);
            body.getChildren().add(pageNodeToRestore);

            if (loadingOverlay != null) {
                if (!body.getChildren().contains(loadingOverlay))
                    body.getChildren().add(loadingOverlay);
                loadingOverlay.setOpacity(0);
                loadingOverlay.setVisible(false);
                if (loadingSpinner != null)
                    loadingSpinner.stopAnimation();
            }

            this.currentScene = this.savedPageData;
            this.savedPageData = null;
            this.transitionInProgress = false;
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
            //filmController.loadFilmData(filmId);
            this.savedPageData = this.currentScene;

            root.getChildren().clear();
            root.getChildren().add(filmDetail.node());
        } else {
            System.err.println("MainPagesController: Failed to load film scene");
        }
    }

    private void transitionToPage(PageData newPage) {
        if (newPage == null || newPage.node() == null || transitionInProgress) {
            if (newPage == null || newPage.node() == null) hideLoadingSpinner();
            return;
        }

        transitionInProgress = true;

        Node currentNode = null;
        for(Node child : body.getChildren()){
            if(child != loadingOverlay){
                currentNode = child;
                break;
            }
        }

        if (currentNode != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_DURATION), currentNode);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            Node finalCurrentNode = currentNode;
            fadeOut.setOnFinished(e -> {
                body.getChildren().remove(finalCurrentNode);
                showNewPage(newPage);
            });
            fadeOut.play();
        } else {
            showNewPage(newPage);
        }
    }

    private void showNewPage(PageData newPage) {
        Node pageNode = newPage.node();
        pageNode.setOpacity(0);
        if (pageNode != loadingOverlay)
            body.getChildren().addFirst(pageNode);



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

    public void settingsClick(Utente user, Account account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/settings-view.fxml"), Main.resourceBundle);
            Parent settingContent = loader.load();
            SettingsViewController settingsViewController = loader.getController();
            settingsViewController.setUtente(user);
            settingsViewController.setAccount(account); // Pass account

            Scene currentSceneNode = body.getScene(); // Use body.getScene() for consistency
            Scene newScene = new Scene(settingContent, currentSceneNode.getWidth(), currentSceneNode.getHeight());
            Stage stage = (Stage) currentSceneNode.getWindow();
            stage.setScene(newScene);
        } catch (IOException e) {
            System.err.println("MainPagesController: Errore caricamento pagina dei setting: " + e.getMessage());
        }
    }

    public void emailClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"), Main.resourceBundle);
            Parent accessContent = loader.load();
            Scene currentSceneNode = body.getScene();
            Scene newScene = new Scene(accessContent, currentSceneNode.getWidth(), currentSceneNode.getHeight());
            Stage stage = (Stage) currentSceneNode.getWindow();
            stage.setScene(newScene);
        } catch (IOException ex) {
            System.err.println("MainPagesController: Errore caricamento pagina di accesso dell'account: " + ex.getMessage());
        }
    }

    public void profileClick(Utente newUser) {
        if (transitionInProgress) return;

        if (loadingSpinner != null) loadingSpinner.stopAnimation();

        // Clear main containers
        if (body != null) body.getChildren().clear();
        if (headerContainer != null) headerContainer.getChildren().clear();

        // Reset page data and state
        header = null;
        home = null;
        filter_film = null;
        filter_series = null;
        currentScene = null;
        pageBeforeSearch = null;
        savedPageData = null; // Reset this too
        loadingOverlay = null; // Will be recreated by initialize
        // transitionInProgress will be reset by initialize->showLoadingSpinner->transitionToPage flow

        this.user = newUser; // Update to the new user

        first_load(this.user, this.account); // Reloads header and home for the new user
    }

    public void createProfileUser(Account account){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/create-profile-view.fxml"), Main.resourceBundle);
            Parent createContent = loader.load();
            CreateProfileController createProfileController = loader.getController();
            createProfileController.setOrigine(CreateProfileController.Origine.HOME);
            createProfileController.setAccount(account);
            createProfileController.setUtente(this.user); // Pass current user context

            Scene currentSceneNode = body.getScene();
            Scene newScene = new Scene(createContent, currentSceneNode.getWidth(), currentSceneNode.getHeight());
            Stage stage = (Stage) currentSceneNode.getWindow();
            stage.setScene(newScene);
        }catch(IOException e){
            System.err.println("MainPagesController : errore caricamento pagina di creazione profili: " + e.getMessage());
        }
    }
}