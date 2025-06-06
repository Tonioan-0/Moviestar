package com.esa.moviestar.home;

import com.esa.moviestar.libraries.TMDbApiManager;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
// import javafx.scene.control.Label; // Label import is not used directly in this snippet
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SearchController {
    @FXML
    private FlowPane recommendations;
    @FXML
    private Line separatorLine;
    @FXML
    private Button findOutButton;
    @FXML
    private FlowPane filmSeriesRecommendations;

    private MainPagesController setupController;
    private HeaderController headerController;
    private User user;
    private TMDbApiManager tmdbApiManager;

    private static final int MAX_PAGES_TO_FETCH_SEARCH = 3;
    private static final int TOP_N_COUNT_FOR_RICH_DISPLAY = 20;
    private static final int MAX_LABEL_LIKE_COUNT = 15;

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = new HashSet<>();
        return t -> {
            Object key = keyExtractor.apply(t);
            if (key == null) {
                return false;
            }
            return seen.add(key);
        };
    }

    public void initialize() {
        if (this.setupController == null) {
            this.setupController = new MainPagesController();
        }
        tmdbApiManager = TMDbApiManager.getInstance();

        if (separatorLine != null) {
            separatorLine.setStroke(
                    new LinearGradient(
                            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                            new Stop(0.0, Color.TRANSPARENT),
                            new Stop(0.5, Color.WHITE),
                            new Stop(1.0, Color.TRANSPARENT)
                    ));
        } else {
            System.err.println("SearchController: separatorLine is null. Check FXML linkage.");
        }
    }

    public void setParamController(HeaderController header, User user, MainPagesController mainPagesController) {
        this.headerController = header;
        this.user = user;
        this.setupController = mainPagesController;

        if (headerController != null && headerController.getTbxSearch() != null) {
            String query = headerController.getTbxSearch().getText();
            if (query != null && !query.trim().isEmpty()) {
                performSearch(query);
            } else {
                clearSearchResults();
            }
        }
    }

    private void clearSearchResults() {
        Platform.runLater(() -> {
            recommendations.getChildren().clear();
            filmSeriesRecommendations.getChildren().clear();
            if (findOutButton != null) findOutButton.setVisible(false);
        });
    }

    public void performSearch(String query) {
        Platform.runLater(() -> {
            recommendations.getChildren().clear();
            filmSeriesRecommendations.getChildren().clear();
            if (findOutButton != null) findOutButton.setVisible(false); // Hide initially
            // Optionally, show a loading indicator here
        });

        List<CompletableFuture<List<Content>>> pageFutures = new ArrayList<>();
        for (int i = 1; i <= MAX_PAGES_TO_FETCH_SEARCH; i++) {
            pageFutures.add(tmdbApiManager.searchMultiContent(query, i));
        }

        CompletableFuture<Void> allPagesFuture = CompletableFuture.allOf(pageFutures.toArray(new CompletableFuture[0]));

        allPagesFuture.thenAcceptAsync(v -> {
                    List<Content> allRawResultsFromApi = new ArrayList<>();
                    for (CompletableFuture<List<Content>> pageFuture : pageFutures) {
                        try {
                            List<Content> pageResult = pageFuture.join(); // .join() is safe here
                            if (pageResult != null) {
                                allRawResultsFromApi.addAll(pageResult);
                            }
                        } catch (Exception e) {
                            System.err.println("SearchController: Error fetching or joining one page of search results: " + e.getMessage());
                        }
                    }

                    // Process the combined list on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        if (allRawResultsFromApi.isEmpty() && pageFutures.stream().allMatch(CompletableFuture::isCompletedExceptionally)) {
                            System.err.println("SearchController: All page fetches failed for query '" + query + "'.");
                            return;
                        }

                        String lowerCaseQuery = query.toLowerCase();

                        List<Content> uniquePopularContent = allRawResultsFromApi.stream()
                                .filter(content -> content != null && content.getTitle() != null &&
                                        !content.getTitle().trim().isEmpty() &&
                                        content.getTitle().toLowerCase().contains(lowerCaseQuery))
                                .sorted(Comparator.comparingDouble(Content::getPopularity).reversed())
                                .filter(distinctByKey(content -> content.getTitle().toLowerCase()))
                                .toList();

                        List<Content> forRichDisplay = uniquePopularContent.stream()
                                .limit(TOP_N_COUNT_FOR_RICH_DISPLAY)
                                .collect(Collectors.toList());

                        if (!forRichDisplay.isEmpty()) {
                            try {
                                if (setupController == null) {
                                    System.err.println("SearchController: setupController is null. Cannot create film nodes.");
                                    return;
                                }
                                List<Node> filmNodes = setupController.createFilmNodes(forRichDisplay, false);
                                filmSeriesRecommendations.getChildren().addAll(filmNodes);
                            } catch (IOException e) {
                                System.err.println("SearchController: Error creating film nodes for filmSeriesRecommendations: " + e.getMessage());
                            } catch (NullPointerException e) {
                                System.err.println("SearchController: NullPointerException during film node creation. Check setupController: " + e.getMessage());
                            }
                        }

                        List<Content> forLabelLikeDisplay = uniquePopularContent.stream()
                                .skip(forRichDisplay.size())
                                .limit(MAX_LABEL_LIKE_COUNT)
                                .collect(Collectors.toList());

                        recommendations.getChildren().clear();
                        if (!forLabelLikeDisplay.isEmpty()) {
                            List<Node> labelButtons = createLabelLikeButtons(forLabelLikeDisplay);
                            recommendations.getChildren().addAll(labelButtons);
                            if (findOutButton != null) findOutButton.setVisible(true);
                        } else {
                            if (findOutButton != null) findOutButton.setVisible(false);
                        }

                        if (forRichDisplay.isEmpty() && forLabelLikeDisplay.isEmpty()) {
                            System.out.println("SearchController: No results found for query '" + query + "' after processing all pages.");
                        }
                    });
                }, tmdbApiManager.getExecutor())
                .exceptionally(ex -> {
                    System.err.println("SearchController: Error during multi-page search operation for query '" + query + "': " + ex.getMessage());
                    Platform.runLater(() -> {
                        recommendations.getChildren().clear();
                        filmSeriesRecommendations.getChildren().clear();
                        if (findOutButton != null) findOutButton.setVisible(false);
                    });
                    return null;
                });
    }

    private List<Node> createLabelLikeButtons(List<Content> contentList) {
        List<Node> buttons = new ArrayList<>();
        if (contentList == null || contentList.isEmpty()) return buttons;

        for (int i = 0; i < contentList.size(); i++) {
            Content content = contentList.get(i);
            if (content.getTitle() == null || content.getTitle().trim().isEmpty()) continue;

            HBox itemContainer = new HBox();
            itemContainer.setAlignment(javafx.geometry.Pos.CENTER);
            itemContainer.setSpacing(2);

            Button button = new Button(content.getTitle());
            button.getStyleClass().addAll("register-text-recommendation-mid", "on-primary", "button-big-text");
            button.setOnAction(event -> {
                if (setupController != null) {
                    setupController.openFilmScene(content.getId(), content.isSeries());
                } else {
                    System.err.println("SearchController: setupController is null. Cannot open film scene.");
                }
            });

            itemContainer.getChildren().add(button);

            if (i < contentList.size() - 1) {
                Line verticalSeparator = new Line(0, 0, 0, 20);
                verticalSeparator.setStrokeWidth(1.0);
                verticalSeparator.setStroke(Color.LIGHTGRAY);
                HBox.setMargin(verticalSeparator, new Insets(0, 8, 0, 8));
                itemContainer.getChildren().add(verticalSeparator);
            }
            buttons.add(itemContainer);
        }
        return buttons;
    }
}