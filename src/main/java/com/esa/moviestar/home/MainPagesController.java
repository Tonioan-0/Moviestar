package com.esa.moviestar.home;

import com.esa.moviestar.Main;
import com.esa.moviestar.model.User;
import com.esa.moviestar.movie_view.FilmPlayer;
import com.esa.moviestar.profile.CreateProfileController;
import com.esa.moviestar.settings.SettingsViewController;
import com.esa.moviestar.components.BufferAnimation;
import com.esa.moviestar.model.Account;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.movie_view.FilmCardController;
import com.esa.moviestar.movie_view.FilmSceneController;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
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
// import java.util.concurrent.ExecutorService; // Not explicitly used in this version's relevant parts
// import java.util.concurrent.Executors;    // Not explicitly used in this version's relevant parts
import java.util.function.Supplier;


public class MainPagesController {


    // FXML elements
    @FXML private AnchorPane body;
    @FXML private StackPane headerContainer;
    @FXML private AnchorPane root;

    // Constants
    public static final String PATH_CARD_WINDOW = "/com/esa/moviestar/movie_view/WindowCard.fxml";
    public static final String PATH_CARD_VERTICAL = "/com/esa/moviestar/movie_view/FilmCard_Vertical.fxml";
    public static final String PATH_CARD_HORIZONTAL = "/com/esa/moviestar/movie_view/FilmCard_Horizontal.fxml";
    public static final String FILM_SCENE_PATH = "/com/esa/moviestar/movie_view/FilmScene.fxml";
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
    private PageData pageBeforeSearch;
    private PageData savedPageData;    // For restoring state after film scene

    //Timer setting
    private Timeline searchTimer;

    private boolean transitionInProgress = false;
    private BufferAnimation loadingSpinner;
    private StackPane loadingOverlay;
    private static final double FADE_DURATION_MS = 300;


    public void first_load(User user, Account account) {
        if (loadingOverlay == null && root != null) {
            createLoadingOverlay();
            if (!root.getChildren().contains(loadingOverlay)) {
                root.getChildren().add(loadingOverlay);
                AnchorPane.setTopAnchor(loadingOverlay, 0.0);
                AnchorPane.setBottomAnchor(loadingOverlay, 0.0);
                AnchorPane.setLeftAnchor(loadingOverlay, 0.0);
                AnchorPane.setRightAnchor(loadingOverlay, 0.0);
            }
        } else if (loadingOverlay == null && body != null) {
            createLoadingOverlay();
            if (!body.getChildren().contains(loadingOverlay)){
                body.getChildren().add(loadingOverlay);
            }
        }

        showLoadingSpinner();
        this.user = user;
        this.account = account;

        if (header == null)
            loadHeader();


        if (this.header == null || this.header.controller() == null) {
            System.err.println("MainPagesController: Critical error - Header or its controller failed to load. Aborting first_load.");
            hideLoadingSpinner();
            return;
        }

        if (this.header.controller() instanceof HeaderController headerCtrl) {
            headerCtrl.setProfileIcon(user.getIcon());
        } else {
            System.err.println("MainPagesController: Header controller is not of type HeaderController.");
            hideLoadingSpinner();
            return;
        }

        CompletableFuture.runAsync(() -> {
            PageData homeData = loadDynamicBody("home.fxml");
            if (homeData != null && homeData.controller() instanceof HomeController) {
                Platform.runLater(() -> {
                    ((HomeController) homeData.controller()).setRecommendations(user, MainPagesController.this);
                    this.home = homeData;
                    transitionToPage(homeData);
                });
            } else {
                Platform.runLater(() -> {
                    hideLoadingSpinner();
                    System.err.println("MainPagesController: Home page or its controller failed to load.");
                });
            }
        });
    }

    private void createLoadingOverlay() {
        loadingSpinner = new BufferAnimation(128);
        loadingOverlay = new StackPane(loadingSpinner);
        loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        loadingOverlay.setAlignment(Pos.CENTER);
        loadingOverlay.setVisible(false);
        loadingOverlay.setOpacity(0);
    }

    private void loadHeader() {
        header = loadDynamicBody("header.fxml");
        if (header == null || header.controller() == null) {
            System.err.println("MainPagesController: Error loading header or its controller. Navigation and search may not work.");
            return;
        }

        if (headerContainer != null) {
            headerContainer.getChildren().clear();
            headerContainer.getChildren().add(header.node());
        } else {
            System.err.println("MainPagesController: headerContainer is null. Cannot display header.");
            return;
        }

        if (header.controller() instanceof HeaderController headerController) {
            if (user != null) {
                headerController.setUpPopUpMenu(this, user, account);
            }
            setupNavigationButtons(headerController);
            setupSearchListener(headerController);
        } else {
            System.err.println("MainPagesController: Loaded header controller is not of type HeaderController.");
        }
    }

    private void setupNavigationButtons(HeaderController headerController) {
        if (headerController.homeButton != null) {
            headerController.homeButton.setOnMouseClicked(e -> { // Changed to setOnAction
                if (currentScene == home || transitionInProgress) return;
                loadPageAsync("home", () -> {
                    if (home == null) {
                        home = loadDynamicBody("home.fxml");
                        if (home != null && home.controller() instanceof HomeController hc) {
                            hc.setRecommendations(user, this);
                        } else if (home != null) {
                            System.err.println("MainPagesController: Home page loaded but controller type mismatch.");
                        }
                    }
                    return home;
                });
            });
        } else {
            System.err.println("MainPagesController: homeButton in HeaderController is null.");
        }

        if (headerController.filmButton != null) {
            headerController.filmButton.setOnMouseClicked(e -> { // Changed to setOnAction
                if (currentScene == filter_film || transitionInProgress) return;
                loadPageAsync("film filter", () -> {
                    if (filter_film == null) {
                        filter_film = loadDynamicBody("filter.fxml");
                        if (filter_film != null && filter_film.controller() instanceof FilterController fc) {
                            fc.setContent(this, user, true); // true for film
                        } else if (filter_film != null) {
                            System.err.println("MainPagesController: Film filter page loaded but controller type mismatch.");
                        }
                    }
                    return filter_film;
                });
            });
        } else {
            System.err.println("MainPagesController: filmButton in HeaderController is null.");
        }

        if (headerController.seriesButton != null) {
            headerController.seriesButton.setOnMouseClicked(e -> { // Changed to setOnAction
                if (currentScene == filter_series || transitionInProgress) return;
                loadPageAsync("series filter", () -> {
                    if (filter_series == null) {
                        filter_series = loadDynamicBody("filter.fxml"); // Re-use filter.fxml
                        if (filter_series != null && filter_series.controller() instanceof FilterController fc) {
                            fc.setContent(this, user, false); // false for series
                        } else if (filter_series != null) {
                            System.err.println("MainPagesController: Series filter page loaded but controller type mismatch.");
                        }
                    }
                    return filter_series;
                });
            });
        } else {
            System.err.println("MainPagesController: seriesButton in HeaderController is null.");
        }
    }

    private void setupSearchListener(HeaderController headerController) {
        if (headerController.getTbxSearch() != null ) {

            headerController.getTbxSearch().textProperty().addListener((observableValue, oldV, newV) -> {

                if(searchTimer != null ){
                    searchTimer.stop();
                    searchTimer = null;
                }
                String query = newV.trim();
                if (query.isEmpty()) {
                    if (currentScene != null && currentScene.controller() instanceof SearchController ) {
                        PageData pageToReturnTo = (this.pageBeforeSearch != null && !(this.pageBeforeSearch.controller() instanceof SearchController))
                                ? this.pageBeforeSearch : this.home;
                        if (pageToReturnTo != null ) {
                            String pageName = getPageName(pageToReturnTo);
                            loadPageAsync(pageName, () -> pageToReturnTo);
                            if (this.pageBeforeSearch == pageToReturnTo) this.pageBeforeSearch = null;
                        } else {
                            SearchController sc = (SearchController) currentScene.controller();
                            sc.performSearch(""); // Tell search controller to clear/show default
                        }
                    }
                    return;
                }

                if (currentScene != null && !(currentScene.controller() instanceof SearchController)) {
                    if (this.pageBeforeSearch == null || (this.pageBeforeSearch.node() != currentScene.node())) {
                        this.pageBeforeSearch = currentScene;
                    }
                }

                loadPageAsync("search", () -> {
                    PageData searchResultPage = loadDynamicBody("search.fxml");
                    if (searchResultPage != null && searchResultPage.controller() instanceof SearchController sc) {
                        sc.setParamController(headerController, user, this);
                        searchTimer = new Timeline(new KeyFrame(Duration.millis(500), e -> {
                            sc.performSearch(query);
                        }));
                        searchTimer.play();
                        sc.performSearch(query);
                    }
                    else if (searchResultPage == null) {
                        System.err.println("MainPagesController: Search page (search.fxml) failed to load.");
                    } else {
                        System.err.println("MainPagesController: Search page loaded, but controller is not SearchController.");
                    }
                    return searchResultPage;
                });
            });
        } else {
            System.err.println("MainPagesController: Search text field (tbxSearch) is null in HeaderController.");
        }
    }

    private String getPageName(PageData pageData) {

        if (pageData == home) return "home";
        if (pageData == filter_film) return "film filter";
        if (pageData == filter_series) return "series filter";
        return "unknown";

    }


    private void showLoadingSpinner() {

        Platform.runLater(() -> {
            if (loadingSpinner == null || loadingOverlay == null) {
                if (root != null) createLoadingOverlay(); else return;
                if (root != null && !root.getChildren().contains(loadingOverlay)) {
                    root.getChildren().add(loadingOverlay);
                    AnchorPane.setTopAnchor(loadingOverlay, 0.0);
                    AnchorPane.setBottomAnchor(loadingOverlay, 0.0);
                    AnchorPane.setLeftAnchor(loadingOverlay, 0.0);
                    AnchorPane.setRightAnchor(loadingOverlay, 0.0);
                } else if (body != null && !body.getChildren().contains(loadingOverlay)){
                    body.getChildren().add(loadingOverlay);
                } else {
                    return;
                }
            }
            loadingSpinner.startAnimation();
            loadingOverlay.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), loadingOverlay);
            fadeIn.setFromValue(loadingOverlay.getOpacity());
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

    }

    private void hideLoadingSpinner() {

        Platform.runLater(() -> {
            if (loadingOverlay == null) return;
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), loadingOverlay);
            fadeOut.setFromValue(loadingOverlay.getOpacity());
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                loadingOverlay.setVisible(false);
                if (loadingSpinner != null) loadingSpinner.stopAnimation();
            });
            fadeOut.play();
        });

    }

    private void loadPageAsync(String pageName, Supplier<PageData> pageSupplier) {
        if (header == null || !(header.controller() instanceof HeaderController headerController)) {
            System.err.println("MainPagesController.loadPageAsync: Header or its controller is invalid. Cannot load " + pageName);
            hideLoadingSpinner();
            return;
        }

        showLoadingSpinner();

        Platform.runLater(() -> {
            switch (pageName) {
                case "home" -> { if (headerController.homeButton != null) headerController.activeButton(headerController.homeButton); }
                case "film filter" -> { if (headerController.filmButton != null) headerController.activeButton(headerController.filmButton); }
                case "series filter" -> { if (headerController.seriesButton != null) headerController.activeButton(headerController.seriesButton); }
                case "search" -> headerController.activeSearch();
                default -> System.err.println("MainPagesController.loadPageAsync: Unknown page name for button activation: " + pageName);
            }
        });

        CompletableFuture.supplyAsync(pageSupplier)
                .thenAcceptAsync(page -> {
                    if (page != null && page.node() != null) {
                        transitionToPage(page);
                    } else {
                        hideLoadingSpinner();
                        System.err.println("MainPagesController: Failed to load " + pageName + " page (supplier returned null or node is null).");
                    }
                }, Platform::runLater)
                .exceptionally(ex -> {
                    System.err.println("MainPagesController: Exception during async page loading for " + pageName + ": " + ex.getMessage());
                    ex.printStackTrace();
                    Platform.runLater(this::hideLoadingSpinner);
                    return null;
                });
    }

    private PageData loadDynamicBody(String fxmlFile) {
        try {
            String pathToLoad;
            if (fxmlFile.startsWith("/"))
                pathToLoad = fxmlFile;
             else
                pathToLoad =  "/com/esa/moviestar/home/"+fxmlFile;
            if (fxmlFile.equals(FILM_SCENE_PATH.substring(1))) {
                pathToLoad = FILM_SCENE_PATH;
            }
            var resource = getClass().getResource(pathToLoad);
            if (resource == null) {
                System.err.println("MainPagesController: Resource not found: " + pathToLoad + " (Original: " + fxmlFile + ")");
                return null;
            }
            FXMLLoader loader = new FXMLLoader(resource, Main.resourceBundle);
            Node pageNode = loader.load();

            if (!pathToLoad.equals(FILM_SCENE_PATH)) {
                AnchorPane.setBottomAnchor(pageNode, 0.0);
                AnchorPane.setTopAnchor(pageNode, 0.0);
                AnchorPane.setLeftAnchor(pageNode, 0.0);
                AnchorPane.setRightAnchor(pageNode, 0.0);
            }
            return new PageData(pageNode, loader.getController());
        } catch (Exception e) {
            System.err.println("MainPagesController: Unexpected error while loading " + fxmlFile + ". Details: " + e.getMessage());
            return null;
        }
    }

    private ImageView captureScreenshotView() {
        if (root == null || root.getScene() == null || root.getScene().getWindow() == null) {
            System.err.println("MainPagesController: Cannot capture screenshot, root, scene or window not available.");
            return new ImageView();
        }
        WritableImage screenshot = root.snapshot(null, null);
        return new ImageView(screenshot);
    }

    public void restorePreviousScene() {
        Platform.runLater(() -> {
            if (this.savedPageData != null && this.savedPageData.node() != null) {
                System.out.println("Restoring previous scene");
                if (root == null || body == null || headerContainer == null) {
                    System.err.println("Cannot restore: root, body, or headerContainer is null.");
                    transitionInProgress = false;
                    return;
                }

                root.getChildren().clear();
                root.getChildren().addAll(body, headerContainer);

                body.getChildren().clear();
                Node pageNodeToRestore = savedPageData.node();
                pageNodeToRestore.setOpacity(1.0);
                body.getChildren().add(pageNodeToRestore);
                AnchorPane.setTopAnchor(pageNodeToRestore, 0.0);
                AnchorPane.setBottomAnchor(pageNodeToRestore, 0.0);
                AnchorPane.setLeftAnchor(pageNodeToRestore, 0.0);
                AnchorPane.setRightAnchor(pageNodeToRestore, 0.0);

                if (loadingOverlay != null) {
                    if (!root.getChildren().contains(loadingOverlay)) {
                        root.getChildren().add(loadingOverlay);
                        AnchorPane.setTopAnchor(loadingOverlay, 0.0);
                        AnchorPane.setBottomAnchor(loadingOverlay, 0.0);
                        AnchorPane.setLeftAnchor(loadingOverlay, 0.0);
                        AnchorPane.setRightAnchor(loadingOverlay, 0.0);
                    }
                    loadingOverlay.setOpacity(0);
                    loadingOverlay.setVisible(false);
                    if (loadingSpinner != null) loadingSpinner.stopAnimation();
                }

                this.currentScene = this.savedPageData;
                this.transitionInProgress = false;
            } else {
                System.err.println("Cannot restore scene: savedPageData is null or node is null.");
                transitionInProgress = false;
                if (home != null) {
                    transitionToPage(home);
                }
            }
        });
    }

    public void openFilmScene(int contentId, boolean isSeries) {
        if (transitionInProgress) {
            System.out.println("MainPagesController: Transition already in progress. Cannot open film scene.");
            return;
        }
        transitionInProgress = true;
        showLoadingSpinner();

        ImageView screenshotView = captureScreenshotView();
        PageData filmDetailPd = loadDynamicBody(FILM_SCENE_PATH);

        if (filmDetailPd != null && filmDetailPd.controller() instanceof FilmSceneController filmController) {
            this.savedPageData = this.currentScene;

            if (root != null) {
                root.getChildren().clear();
                Node filmNode = filmDetailPd.node();
                root.getChildren().add(filmNode);
                AnchorPane.setTopAnchor(filmNode, 0.0);
                AnchorPane.setBottomAnchor(filmNode, 0.0);
                AnchorPane.setLeftAnchor(filmNode, 0.0);
                AnchorPane.setRightAnchor(filmNode, 0.0);
            } else {
                System.err.println("MainPagesController: Root pane is null. Cannot display film scene.");
                hideLoadingSpinner();
                transitionInProgress = false;
                return;
            }

            filmController.setMainPagesController(this);
            filmController.setProperties(screenshotView, this);
            filmController.loadContent(contentId, !isSeries);
            filmController.setUserAndAccount(user,account);

            transitionInProgress = false;

        } else {
            System.err.println("MainPagesController: Failed to load film scene FXML or its controller is not FilmSceneController.");
            hideLoadingSpinner();
            transitionInProgress = false;
            if (savedPageData != null) {
                restorePreviousScene();
            }
        }
    }

    private void transitionToPage(PageData newPageData) {
        Platform.runLater(() -> {
            if (newPageData == null || newPageData.node() == null) {
                System.err.println("MainPagesController.transitionToPage: newPageData or its node is null.");
                hideLoadingSpinner();
                transitionInProgress = false;
                return;
            }
            if (currentScene == newPageData && body.getChildren().contains(newPageData.node()) && !transitionInProgress) {
                hideLoadingSpinner();
                return;
            }
            if (transitionInProgress) {
                System.out.println("Transition already in progress. Requested page: " + newPageData.controller().getClass().getSimpleName());
                return;
            }
            transitionInProgress = true;

            Node oldNode = body.getChildren().isEmpty() ? null : body.getChildren().getFirst();

            if (oldNode != null && oldNode != newPageData.node()) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(FADE_DURATION_MS), oldNode);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    body.getChildren().remove(oldNode);
                    addNewPageNode(newPageData);
                });
                fadeOut.play();
            } else {
                if (oldNode == null && newPageData.node() != null && !body.getChildren().contains(newPageData.node())) {
                    body.getChildren().clear();
                    addNewPageNode(newPageData);
                } else if (body.getChildren().contains(newPageData.node())) {
                    assert newPageData.node() != null;
                    newPageData.node().setOpacity(1.0);
                    currentScene = newPageData;
                    transitionInProgress = false;
                    hideLoadingSpinner();
                } else {
                    body.getChildren().clear();
                    addNewPageNode(newPageData);
                }
            }
        });
    }

    private void addNewPageNode(PageData newPageData) {
        Node pageNode = newPageData.node();
        pageNode.setOpacity(0.0);
        if (!body.getChildren().contains(pageNode) && pageNode != loadingOverlay) {
            body.getChildren().addFirst(pageNode);
        }

        FadeTransition fadeIn = new FadeTransition(Duration.millis(FADE_DURATION_MS), pageNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e -> {
            currentScene = newPageData;
            transitionInProgress = false;
            hideLoadingSpinner();
        });
        fadeIn.play();
    }

    public List<Node> createFilmNodes(List<Content> contentList, boolean isVertical) throws IOException {
        List<Node> nodes = new ArrayList<>(contentList.size());
        String cardPath = isVertical ? PATH_CARD_VERTICAL : PATH_CARD_HORIZONTAL;

        for (Content content : contentList) {
            if (content == null || content.getId() == 0) continue;

            FXMLLoader fxmlLoader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(cardPath), "FXML resource for card not found: " + cardPath),
                    Main.resourceBundle
            );
            Node node = fxmlLoader.load();
            FilmCardController filmCardController = fxmlLoader.getController();

            if (filmCardController != null) {
                filmCardController.setContent(content, isVertical);
                node.setOnMouseClicked(e -> openFilmScene(content.getId(), content.isSeries()));
                nodes.add(node);
            } else {
                System.err.println("MainPagesController: FilmCardController is null for " + cardPath);
            }
        }
        return nodes;
    }

    public void cardClickedPlay(String urlVideo) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/movie_view/FilmPlayer.fxml"), Main.resourceBundle);
                Scene currentSceneNode = getCurrentAppScene();
                if (currentSceneNode == null) return;
                Parent scene = loader.load();
                ((FilmPlayer)loader.getController()).initializePlayer(urlVideo,user,account);
                ((FilmPlayer)loader.getController()).play();
                Scene newScene = new Scene(scene, currentSceneNode.getWidth(), currentSceneNode.getHeight());
                Stage stage = (Stage) currentSceneNode.getWindow();
                if (stage != null) stage.setScene(newScene);
                else System.err.println("MainPagesController.settingsClick: Stage is null.");

            } catch (IOException e) {
                System.err.println("MainPagesController: Errore caricamento pagina dei setting: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void settingsClick(User user, Account account) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/settings/settings-view.fxml"), Main.resourceBundle);
                Parent settingContent = loader.load();
                SettingsViewController settingsViewController = loader.getController();
                if (settingsViewController != null) {
                    settingsViewController.setUser(user);
                    settingsViewController.setAccount(account);
                } else {
                    System.err.println("MainPagesController.settingsClick: SettingsViewController is null.");
                }

                Scene currentSceneNode = getCurrentAppScene();
                if (currentSceneNode == null) return;

                Scene newScene = new Scene(settingContent, currentSceneNode.getWidth(), currentSceneNode.getHeight());
                Stage stage = (Stage) currentSceneNode.getWindow();
                if (stage != null) stage.setScene(newScene);
                else System.err.println("MainPagesController.settingsClick: Stage is null.");

            } catch (IOException e) {
                System.err.println("MainPagesController: Error loading settings page: " + e.getMessage());}
        });
    }

    public void emailClick() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/login/access.fxml"), Main.resourceBundle);
                Parent accessContent = loader.load();
                Scene currentSceneNode = getCurrentAppScene();
                if (currentSceneNode == null) return;

                Scene newScene = new Scene(accessContent, currentSceneNode.getWidth(), currentSceneNode.getHeight());
                Stage stage = (Stage) currentSceneNode.getWindow();
                if (stage != null) stage.setScene(newScene);
                else System.err.println("MainPagesController.emailClick: Stage is null.");

            } catch (IOException ex) {
                System.err.println("MainPagesController: Error loading access page: " + ex.getMessage());
            }
        });
    }

    public void profileClick(User newUser) {
        if (transitionInProgress) {
            System.out.println("Transition in progress, cannot switch profile.");
            return;
        }
        showLoadingSpinner();

        Platform.runLater(() -> {
            if (loadingSpinner != null) loadingSpinner.stopAnimation();
            if (body != null) body.getChildren().clear();
            if (headerContainer != null) headerContainer.getChildren().clear();

            header = null; home = null; filter_film = null; filter_series = null;
            currentScene = null; pageBeforeSearch = null; savedPageData = null;

            this.user = newUser;
            first_load(this.user, this.account);
        });
    }

    public void createProfileUser(Account account){
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esa/moviestar/profile/create-profile-view.fxml"), Main.resourceBundle);
                Parent createContent = loader.load();
                CreateProfileController createProfileController = loader.getController();
                if (createProfileController != null) {
                    createProfileController.setSource(CreateProfileController.Origine.HOME);
                    createProfileController.setAccount(account);
                    createProfileController.setUser(this.user);
                } else {
                    System.err.println("MainPagesController.createProfileUser: CreateProfileController is null.");
                }

                Scene currentSceneNode = getCurrentAppScene();
                if (currentSceneNode == null) return;

                Scene newScene = new Scene(createContent, currentSceneNode.getWidth(), currentSceneNode.getHeight());
                Stage stage = (Stage) currentSceneNode.getWindow();
                if (stage != null) stage.setScene(newScene);
                else System.err.println("MainPagesController.createProfileUser: Stage is null.");

            }catch(IOException e){
                System.err.println("MainPagesController : Error to switch profile: " + e.getMessage());
            }
        });
    }

    private Scene getCurrentAppScene() {
        Scene scene = (body != null && body.getScene() != null) ? body.getScene() :
                (root != null && root.getScene() != null) ? root.getScene() : null;
        if (scene == null) {
            System.err.println("MainPagesController.getCurrentAppScene: Cannot get current scene from body or root.");
        }
        return scene;
    }

    public void filmClicked(User user , Account account,int content,boolean series){
        if (loadingOverlay == null && root != null) {
            createLoadingOverlay();
            if (!root.getChildren().contains(loadingOverlay)) {
                root.getChildren().add(loadingOverlay);
                AnchorPane.setTopAnchor(loadingOverlay, 0.0);
                AnchorPane.setBottomAnchor(loadingOverlay, 0.0);
                AnchorPane.setLeftAnchor(loadingOverlay, 0.0);
                AnchorPane.setRightAnchor(loadingOverlay, 0.0);
            }
        } else if (loadingOverlay == null && body != null) {
            createLoadingOverlay();
            if (!body.getChildren().contains(loadingOverlay)){
                body.getChildren().add(loadingOverlay);
            }
        }

        showLoadingSpinner();
        this.user = user;
        this.account = account;

        if (header == null)
            loadHeader();


        if (this.header == null || this.header.controller() == null) {
            System.err.println("MainPagesController: Critical error - Header or its controller failed to load. Aborting first_load.");
            hideLoadingSpinner();
            return;
        }

        if (this.header.controller() instanceof HeaderController headerCtrl) {
            headerCtrl.setProfileIcon(user.getIcon());
        } else {
            System.err.println("MainPagesController: Header controller is not of type HeaderController.");
            hideLoadingSpinner();
            return;
        }

        CompletableFuture.runAsync(() -> {
            PageData homeData = loadDynamicBody("home.fxml");
            if (homeData != null && homeData.controller() instanceof HomeController) {
                Platform.runLater(() -> {
                    ((HomeController) homeData.controller()).setRecommendations(user, MainPagesController.this);
                    this.home = homeData;

                    if (homeData.node() == null)
                        return;

                    if (currentScene == homeData && body.getChildren().contains(homeData.node()) && !transitionInProgress) {
                        hideLoadingSpinner();
                        return;
                    }

                    Node oldNode = body.getChildren().isEmpty() ? null : body.getChildren().getFirst();

                    if (oldNode == null && !body.getChildren().contains(homeData.node())) {
                        body.getChildren().clear();
                        addNewPageNode(homeData);
                    } else if (body.getChildren().contains(homeData.node())) {
                        homeData.node().setOpacity(1.0);
                        currentScene = homeData;
                        transitionInProgress = false;
                        hideLoadingSpinner();
                    } else {
                        body.getChildren().clear();
                        addNewPageNode(homeData);
                    }
                    openFilmScene(content, series);
                });
            } else {
                Platform.runLater(() -> {
                    hideLoadingSpinner();
                    System.err.println("MainPagesController: Home page or its controller failed to load.");
                });
            }
        });

    }
}