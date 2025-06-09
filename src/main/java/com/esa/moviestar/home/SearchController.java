package com.esa.moviestar.home;

import com.esa.moviestar.database.ContentDao;
import com.esa.moviestar.libraries.TMDbApiManager;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.geometry.Pos;
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
import javafx.scene.shape.SVGPath;
import java.util.*;

//Samuele metti le sources grz

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
    //Since the api gives content with different id but with same info we need to check from the title
    private Map<String, Boolean> seen;

    private static final int MAX_PAGES_TO_FETCH_SEARCH = 10;
    private static final int TOP_N_COUNT_FOR_RICH_DISPLAY = 20;
    private static final int MAX_LABEL_LIKE_COUNT = 15;
    private static final int MIN_LABEL_LIKE_DISPLAY_COUNT = 5;
    private static final int MIN_CONTENT_RICH_DISPLAY = 8;

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor){
        Set<Object> seen = new HashSet<>();
        return t ->{
            Object key = keyExtractor.apply(t);
            if (key == null){
                return false;
            }
            return seen.add(key);
        };
    }

    public void initialize(){
        if (this.setupController == null)
            this.setupController = new MainPagesController();

        tmdbApiManager = TMDbApiManager.getInstance();
        this.seen = new HashMap<>();
        if (separatorLine != null){
            separatorLine.setStroke(
                    new LinearGradient(
                            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                            new Stop(0.0, Color.TRANSPARENT),
                            new Stop(0.5, Color.WHITE),
                            new Stop(1.0, Color.TRANSPARENT)
                    ));
        } else{
            System.err.println("SearchController: separatorLine is null. Check FXML linkage.");
        }
    }

    public void setParamController(HeaderController header, User user, MainPagesController mainPagesController) {
        this.headerController = header;
        this.user = user;
        this.setupController = mainPagesController;

        if (headerController != null && headerController.getTbxSearch() != null){
            String queryText = headerController.getTbxSearch().getText();
            if (queryText != null && !queryText.trim().isEmpty()){
                performSearch(queryText);
            } else{
                clearSearchResults();
            }
        }
    }

    private void clearSearchResults(){
        Platform.runLater(() ->{
            recommendations.getChildren().clear();
            filmSeriesRecommendations.getChildren().clear();
            if (findOutButton != null) findOutButton.setVisible(false);
        });
    }

    public void performSearch(String searchText) {
        Platform.runLater(() ->{
            recommendations.getChildren().clear();
            filmSeriesRecommendations.getChildren().clear();
            if (findOutButton != null) findOutButton.setVisible(false);
        });

        List<CompletableFuture<List<Content>>> pageFutures = new ArrayList<>();
        for (int i = 1; i <= MAX_PAGES_TO_FETCH_SEARCH; i++)
            pageFutures.add(tmdbApiManager.searchMultiContent(searchText, i));


        CompletableFuture<Void> allPagesFuture = CompletableFuture.allOf(pageFutures.toArray(new CompletableFuture[0]));

        allPagesFuture.thenAcceptAsync(v ->{
                    List<Content> allRawResultsFromApi = new ArrayList<>();
                    for (CompletableFuture<List<Content>> pageFuture : pageFutures){
                        try{
                            List<Content> pageResult = pageFuture.join();
                            if (pageResult != null){
                                allRawResultsFromApi.addAll(pageResult);
                            }
                        } catch (Exception e){
                            System.err.println("SearchController: Error fetching or joining one page of search results: " + e.getMessage());
                        }
                    }

                    // Process the combined list on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        if (allRawResultsFromApi.isEmpty() && pageFutures.stream().allMatch(CompletableFuture::isCompletedExceptionally)){
                            System.err.println("SearchController: All page fetches failed for query '" + searchText + "'.");
                            return;
                        }

                        String lowerCaseSearchText = searchText.toLowerCase();
                        seen.clear();

                        List<Content> uniquePopularContentFromApi = allRawResultsFromApi.stream()
                                .filter(content -> content != null && content.getTitle() != null &&
                                        !content.getTitle().trim().isEmpty() &&
                                        content.getTitle().toLowerCase().contains(lowerCaseSearchText))
                                .sorted(Comparator.comparingDouble(Content::getPopularity).reversed())
                                .filter(content ->{
                                    String titleKey = content.getTitle().toLowerCase();
                                    if (seen.containsKey(titleKey))
                                        return false;
                                    else{seen.put(titleKey, true);
                                        return true;}
                                })
                                .toList();

                        List<Content> forRichDisplay = uniquePopularContentFromApi.stream()
                                .limit(TOP_N_COUNT_FOR_RICH_DISPLAY)
                                .collect(Collectors.toCollection(ArrayList::new));

                        // Supplement forRichDisplay from DB if it's not full yet
                        if (forRichDisplay.size() < MIN_CONTENT_RICH_DISPLAY){
                            ContentDao contentDao = new ContentDao();
                            List<Content> dbContentList = contentDao.getContentFromQuery(searchText);

                            for (Content dbContent : dbContentList){
                                if (forRichDisplay.size() >= MIN_CONTENT_RICH_DISPLAY){
                                    break;
                                }
                                if (dbContent.getTitle() != null && !dbContent.getTitle().trim().isEmpty()){
                                    String titleKey = dbContent.getTitle().toLowerCase();
                                    if (!seen.containsKey(titleKey)) {
                                        forRichDisplay.add(dbContent);
                                        seen.put(titleKey, true);
                                    }
                                }
                            }
                        }

                        if (!forRichDisplay.isEmpty()){
                            try{
                                if (setupController == null)
                                    System.err.println("SearchController: setupController is null. Cannot create film nodes.");
                                else
                               {
                                    filmSeriesRecommendations.getChildren().clear();
                                    List<Node> filmNodes =  setupController.createFilmNodes(forRichDisplay, false);
                                    filmSeriesRecommendations.getChildren().addAll(filmNodes);
                               }
                            } catch (IOException e){
                                System.err.println("SearchController: Error creating film nodes for filmSeriesRecommendations: " + e.getMessage());
                            } catch (NullPointerException e) {
                                System.err.println("SearchController: NullPointerException during film node creation. Check setupController: " + e.getMessage());
                            }
                        } else{
                            SVGPath path = new SVGPath();
                            path.setContent("M 100 100 L 300 100 L 200 300 Z");
                            filmSeriesRecommendations.getChildren().add(path);
                        }

                        List<Content> forLabelLikeDisplay = uniquePopularContentFromApi.stream()
                                .filter(content -> !seen.containsKey(content.getTitle().toLowerCase()))
                                .limit(MAX_LABEL_LIKE_COUNT)
                                .collect(Collectors.toCollection(ArrayList::new));
                        if (forLabelLikeDisplay.size() < MIN_LABEL_LIKE_DISPLAY_COUNT){
                            ContentDao contentdao = new ContentDao();
                            String sanitizedSearchTermForDb = searchText.replace("'", "''");
                            List<Content> dbContentList = contentdao.getContentFromQuery(sanitizedSearchTermForDb);

                            for (Content dbContent : dbContentList){
                                if (forLabelLikeDisplay.size() >= MIN_LABEL_LIKE_DISPLAY_COUNT)
                                    break;
                                if (dbContent.getTitle() != null && !dbContent.getTitle().trim().isEmpty()){
                                    String titleKey = dbContent.getTitle().toLowerCase();
                                    if (!seen.containsKey(titleKey)){
                                        forLabelLikeDisplay.add(dbContent);
                                        seen.put(titleKey, true);
                                    }
                                }
                            }
                        }
                        if (forLabelLikeDisplay.size() > MAX_LABEL_LIKE_COUNT)
                            forLabelLikeDisplay = forLabelLikeDisplay.subList(0, MAX_LABEL_LIKE_COUNT);

                        recommendations.getChildren().clear();
                        if (!forLabelLikeDisplay.isEmpty()){
                            List<Node> labelButtons = createLabelLikeButtons(forLabelLikeDisplay);
                            recommendations.getChildren().addAll(labelButtons);
                            if (findOutButton != null)
                                findOutButton.setVisible(true);
                        } else
                            if (findOutButton != null)
                                findOutButton.setVisible(false);


                        if (forRichDisplay.isEmpty() && forLabelLikeDisplay.isEmpty()) {
                            System.out.println("SearchController: No results found for query '" + searchText + "' after processing all pages.");
                        }
                    });
                }, tmdbApiManager.getExecutor())
                .exceptionally(ex ->{
                    System.err.println("SearchController: Error during multi-page search operation for query '" + searchText + "': " + ex.getMessage());
                    Platform.runLater(() ->{
                        recommendations.getChildren().clear();
                        filmSeriesRecommendations.getChildren().clear();
                        if (findOutButton != null) findOutButton.setVisible(false);
                    });
                    return null;
                });
    }

    private List<Node> createLabelLikeButtons(List<Content> contentList){
        List<Node> buttons = new ArrayList<>();
        if (contentList == null || contentList.isEmpty()) return buttons;

        for (int i = 0; i < contentList.size(); i++){
            Content content = contentList.get(i);
            if (content.getTitle() == null || content.getTitle().trim().isEmpty()) continue;

            HBox itemContainer = new HBox();
            itemContainer.setAlignment(Pos.CENTER);
            itemContainer.setSpacing(2);

            Button button = new Button(content.getTitle());
            button.getStyleClass().addAll("register-text-recommendation-mid", "on-primary", "button-big-text");
            button.setOnAction(event ->{
                if (setupController != null) {
                    setupController.openFilmScene(content.getId(), content.isSeries());
                } else{
                    System.err.println("SearchController: setupController is null. Cannot open film scene.");
                }
            });

            itemContainer.getChildren().add(button);

            if (i < contentList.size() - 1){
                Line verticalSeparator = new Line(0, 0, 0, 40);
                verticalSeparator.setStrokeWidth(1.5);

                verticalSeparator.setStroke(
                        new LinearGradient(
                                0, 0,
                                0, 1,
                                true,
                                CycleMethod.NO_CYCLE,
                                new Stop(0.0, Color.TRANSPARENT),
                                new Stop(0.5, Color.WHITE),
                                new Stop(1.0, Color.TRANSPARENT)
                        ));
                HBox.setMargin(verticalSeparator, new Insets(0, 8, 0, 8));
                itemContainer.getChildren().add(verticalSeparator);
            }
            buttons.add(itemContainer);
        }
        return buttons;
    }
}