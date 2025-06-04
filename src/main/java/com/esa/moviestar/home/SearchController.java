package com.esa.moviestar.home;

import com.esa.moviestar.libraries.TMDbApiManager;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
// import javafx.geometry.Insets; // Not strictly needed if FlowPane hgap/vgap is used
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
// import javafx.scene.layout.HBox; // No longer needed for label-like buttons
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SearchController {
    @FXML private FlowPane recommendations; // Top section for label-like buttons
    @FXML private Line separatorLine;
    @FXML private Button findOutButton; // "Altri titoli da scoprire:"
    @FXML private FlowPane filmSeriesRecommendations; // Bottom section for rich poster display

    private MainPagesController mainPagesController;
    private HeaderController headerController;
    private User user;
    private TMDbApiManager tmdbApiManager;

    public void initialize() {
        tmdbApiManager = TMDbApiManager.getInstance();
        if (separatorLine != null) {
            separatorLine.setStroke(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.TRANSPARENT), new Stop(0.5, Color.WHITE), new Stop(1.0, Color.TRANSPARENT)));
        } else System.err.println("SearchController: separatorLine is null.");

        recommendations.getChildren().add(new Label("Use the search bar above to find movies and TV shows."));
        filmSeriesRecommendations.getChildren().clear();
        recommendations.setHgap(8); // Spacing for label-like buttons
        recommendations.setVgap(4);
    }

    public void setParamController(HeaderController header, User user, MainPagesController mainPagesControllerInstance) {
        this.headerController = header;
        this.user = user;
        this.mainPagesController = mainPagesControllerInstance;

        if (headerController != null && headerController.getTbxSearch() != null) {
            String query = headerController.getTbxSearch().getText();
            if (query != null && !query.trim().isEmpty()) {
                performSearch(query);
            } else {
                Platform.runLater(() -> {
                    recommendations.getChildren().clear();
                    filmSeriesRecommendations.getChildren().clear();
                    recommendations.getChildren().add(new Label("Enter a search term to begin."));
                });
            }
        }
    }

    public void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            Platform.runLater(() -> {
                recommendations.getChildren().clear();
                filmSeriesRecommendations.getChildren().clear();
                recommendations.getChildren().add(new Label("Please enter a search term."));
            });
            return;
        }

        Platform.runLater(() -> {
            recommendations.getChildren().clear();
            filmSeriesRecommendations.getChildren().clear();
            Label searchingLabel = new Label("Searching for: " + query + "...");
            searchingLabel.setStyle("-fx-text-fill: white;");
            // Add to one pane, or a dedicated status area if you have one
            filmSeriesRecommendations.getChildren().add(searchingLabel);
        });

        tmdbApiManager.searchMultiContent(query, 1)
                .thenAcceptAsync(fullContentList -> {
                    Platform.runLater(() -> {
                        filmSeriesRecommendations.getChildren().clear(); // Clear "Searching..."
                        String lowerCaseQuery = query.toLowerCase();

                        List<Content> allMatchingContent = fullContentList.stream()
                                .filter(content -> content.getTitle() != null && !content.getTitle().trim().isEmpty() &&
                                        (content.getPosterUrl() != null || content.getImageUrl() != null) &&
                                        (content.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                                                (content.getOriginalTitle() != null && content.getOriginalTitle().toLowerCase().contains(lowerCaseQuery)))
                                )
                                .sorted(Comparator.comparingDouble(Content::getPopularity).reversed())
                                .collect(Collectors.toList());

                        if (allMatchingContent.isEmpty()) {
                            filmSeriesRecommendations.getChildren().add(new Label("No results found for '" + query + "'."));
                            findOutButton.setVisible(false);
                            findOutButton.setManaged(false);
                            return;
                        }

                        int topNCountForRichDisplay = 20;

                        List<Content> forRichDisplay = allMatchingContent.stream()
                                .limit(topNCountForRichDisplay)
                                .collect(Collectors.toList());

                        try {
                            if (!forRichDisplay.isEmpty()) {
                                if (mainPagesController == null) {
                                    System.err.println("SearchController: mainPagesController is null. Cannot create film nodes.");
                                    filmSeriesRecommendations.getChildren().add(new Label("Error: Cannot display rich results."));
                                } else {
                                    List<Node> filmNodes = mainPagesController.createFilmNodes(
                                            forRichDisplay,
                                            false,
                                            clickedContent -> mainPagesController.showFilmDetail(clickedContent)
                                    );
                                    filmSeriesRecommendations.getChildren().addAll(filmNodes);
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("SearchController: Error creating film nodes: " + e.getMessage());
                            filmSeriesRecommendations.getChildren().add(new Label("Error displaying results."));
                        }

                        List<Content> forLabelLikeDisplay;
                        if (allMatchingContent.size() > topNCountForRichDisplay) {
                            forLabelLikeDisplay = allMatchingContent.stream()
                                    .skip(topNCountForRichDisplay)
                                    .limit(10) // Max 10 text links
                                    .collect(Collectors.toList());
                        } else {
                            forLabelLikeDisplay = new ArrayList<>();
                        }

                        recommendations.getChildren().clear(); // Clear any previous
                        if (!forLabelLikeDisplay.isEmpty()) {
                            findOutButton.setVisible(true);
                            findOutButton.setManaged(true);
                            List<Node> labelButtons = createLabelLikeButtons(forLabelLikeDisplay);
                            recommendations.getChildren().addAll(labelButtons);
                        } else {
                            findOutButton.setVisible(false);
                            findOutButton.setManaged(false);
                        }

                        if (filmSeriesRecommendations.getChildren().isEmpty() && recommendations.getChildren().isEmpty()) {
                            filmSeriesRecommendations.getChildren().add(new Label("No suitable results found for '" + query + "'."));
                        }
                    });
                }, tmdbApiManager.getExecutor()) // Use API manager's executor for API call's continuation
                .exceptionally(ex -> {
                    System.err.println("SearchController: Error during search for '" + query + "': " + ex.getMessage());
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        recommendations.getChildren().clear();
                        filmSeriesRecommendations.getChildren().clear();
                        filmSeriesRecommendations.getChildren().add(new Label("Search failed. Please check connection or try again."));
                    });
                    return null;
                });
    }

    private List<Node> createLabelLikeButtons(List<Content> contentList) {
        List<Node> items = new ArrayList<>();
        if (contentList == null || contentList.isEmpty()) return items;

        for (Content content : contentList) {
            if (content.getTitle() == null || content.getTitle().trim().isEmpty()) continue;

            Button button = new Button(content.getTitle() + (content.getYear() > 0 ? " (" + content.getYear() + ")" : ""));
            button.getStyleClass().add("register-text-recommendations-mid"); // From access.css
            // Simpler styling, relying more on CSS class or FlowPane's hgap
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #B0B0B0; -fx-padding: 2 5; -fx-cursor: hand;");
            button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-underline: true; -fx-padding: 2 5; -fx-cursor: hand;"));
            button.setOnMouseExited(e -> button.setStyle("-fx-background-color: transparent; -fx-text-fill: #B0B0B0; -fx-padding: 2 5; -fx-cursor: hand;"));

            button.setOnAction(event -> {
                if (mainPagesController != null) {
                    mainPagesController.showFilmDetail(content);
                } else {
                    System.err.println("SearchController: mainPagesController is null. Cannot navigate.");
                }
            });
            items.add(button);
        }
        return items;
    }
}