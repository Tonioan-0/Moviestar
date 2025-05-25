package com.esa.moviestar.components;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

public class CarouselSkin extends SkinBase<Carousel> {

    // --- Constants ---
    private static final double CENTER_SCALE = 1.5;
    private static final double SIDE_SCALE = 1.0;
    private static final double CARD_OVERLAP = 0.85;
    private static final Duration TRANSITION_DURATION = Duration.millis(500);
    private static final double SLIDE_DISTANCE = 0.65;
    private static final double SCREEN_HEIGHT_PERCENTAGE = 0.6;
    private static final Duration AUTO_ROTATION_INTERVAL = Duration.seconds(10);
    private static final double DEFAULT_SIZE = 16;
    // --- UI Components ---
    private final VBox mainContainer;
    private final HBox cardsContainer;
    private final HBox dotsContainer;
    private final Rectangle clipRect;

    // --- State & Properties ---
    private Timeline animationTimeline;
    private Timeline autoRotationTimeline;
    private final List<Node> cardWrappers;
    private int currentDisplayedIndex = 0;
    private boolean animationInProgress = false;
    private boolean autoRotationEnabled = false;
    private double dynamicSlideDistance = 450;

    /**
     * Constructor for the CarouselSkin.
     * @param control The Carousel control this skin is for.
     */
    public CarouselSkin(Carousel control) {
        super(control);
        cardWrappers = new ArrayList<>();
        cardsContainer = new HBox();
        cardsContainer.setAlignment(Pos.CENTER);
        VBox.setMargin(cardsContainer,new Insets(150,0,150,0));
        dotsContainer = new HBox(DEFAULT_SIZE);
        dotsContainer.setAlignment(Pos.BOTTOM_CENTER);
        dotsContainer.setPadding(new Insets(DEFAULT_SIZE, 0, DEFAULT_SIZE, 0));

        Region r = new Region();
        VBox.setVgrow(r, Priority.ALWAYS);
        mainContainer = new VBox(){{ setAlignment(Pos.CENTER); getChildren().addAll(cardsContainer,r,dotsContainer);}};

        dotsContainer.setViewOrder(-1);
        clipRect = new Rectangle(){{widthProperty().bind(mainContainer.widthProperty()); heightProperty().bind(mainContainer.heightProperty());}};

        mainContainer.setClip(clipRect);

        getChildren().add(mainContainer);

        // Initialize animation timeline
        animationTimeline = new Timeline();
        autoRotationTimeline = createAutoRotationTimeline();

        control.getItems().addListener((ListChangeListener<? super Node>) (c) ->  Platform.runLater(this::rebuildCarousel));

        control.currentIndexProperty().addListener((obs, oldVal, newVal) ->
                handleIndexChange(oldVal, newVal));

        control.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null)
                return;
            adjustHeightToScreenPercentage(control);
            Window window = newScene.getWindow();
            if (window == null)
                return;
            window.heightProperty().addListener((heightObs, heightOld, heightNew) ->
                    adjustHeightToScreenPercentage(control));
        });

        rebuildCarousel();
    }

    /**
     * Starts the automatic rotation of the carousel
     */
    public void start() {
        if (autoRotationEnabled || getSkinnable().getItems().size() <= 1)
            return;
        autoRotationEnabled = true;
        autoRotationTimeline.play();
    }

    /**
     * Stops the rotation of the carousel.
     */
    public void stop() {
        if (!autoRotationEnabled)
            return;
        autoRotationEnabled = false;
        autoRotationTimeline.stop();
    }

    /**
     * Creates a timeline for auto-rotation.
     */
    private Timeline createAutoRotationTimeline() {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        KeyFrame keyFrame = new KeyFrame(AUTO_ROTATION_INTERVAL, event -> {
            if (!animationInProgress && getSkinnable().getItems().size() > 1) {
                int nextIndex = normalizeIndex(currentDisplayedIndex + 1, getSkinnable().getItems().size());
                getSkinnable().goTo(nextIndex);
            }
        });

        timeline.getKeyFrames().add(keyFrame);
        return timeline;
    }


    private void adjustHeightToScreenPercentage(Carousel control) {
        Scene scene = control.getScene();
        if (scene == null)
            return;
        Window window = scene.getWindow();
        if (window == null)
            return;
        double screenHeight = window.getHeight();
        double carouselHeight = screenHeight * SCREEN_HEIGHT_PERCENTAGE;
        control.setPrefHeight(carouselHeight);
        control.setMinHeight(800);
        control.setMaxHeight(carouselHeight * 1.2);
    }


    private void updateDynamicLayoutValues() {
        double width = cardWrappers.stream().mapToDouble(node -> node.getBoundsInLocal().getWidth()).max().orElse(0);
        if (width <= 0) 
            return;
        double dynamicCardOverlap = width * CARD_OVERLAP;
        cardsContainer.setSpacing(-dynamicCardOverlap);
        dynamicSlideDistance = width * SLIDE_DISTANCE;
    }


    private void handleIndexChange(int oldIndex, int newIndex) {
        if (animationInProgress) {
            return;
        }

        int n = getSkinnable().getItems().size();
        if (n == 0) return;

        // Normalize indices to be within bounds [0, n-1]
        int normalizedOldIndex = normalizeIndex(oldIndex, n);
        int normalizedNewIndex = normalizeIndex(newIndex, n);

        if (normalizedOldIndex == normalizedNewIndex) {
            return;
        }

        // Determine if it's a jump (more than 1 step or initial load)
        boolean isJump = oldIndex == -1 || Math.abs(normalizedOldIndex - normalizedNewIndex) > 1;
        // Handle the wrap-around case as a non-jump animation
        if (Math.abs(normalizedOldIndex - normalizedNewIndex) == n - 1) {
            isJump = false;
        }

        if (isJump) {
            jumpToIndex(normalizedNewIndex, false);
        } else {
            animateToIndex(normalizedNewIndex);
        }
    }


    private int normalizeIndex(int index, int itemCount) {
        if (itemCount == 0) return 0;
        return ((index % itemCount) + itemCount) % itemCount;
    }


    private void rebuildCarousel() {
        if (animationTimeline != null) {
            animationTimeline.stop();
            animationInProgress = false;
        }

        cardsContainer.getChildren().clear();
        dotsContainer.getChildren().clear();
        cardWrappers.clear();
        Carousel carousel = getSkinnable();
        int itemCount = carousel.getItems().size();

        if (itemCount == 0) {
            currentDisplayedIndex = -1;
            return;
        }

        for (int i = 0; i < itemCount; i++) {
            Node item = carousel.getItems().get(i);

            Node cardWrapper = createCardWrapper(item);
            item.viewOrderProperty().bind(cardWrapper.viewOrderProperty());
            cardWrappers.add(cardWrapper);
        }

        for (int i = 0; i < itemCount; i++) {
            Button dot = createDotButton(i);
            dot.getStyleClass().add("carousel-dot");
            dotsContainer.getChildren().add(dot);
        }

        adjustHeightToScreenPercentage(getSkinnable());
        jumpToIndex(0, true);

    }

    private StackPane createCardWrapper(Node content) {
        StackPane cardWrapper = new StackPane(){{
            getStyleClass().add("carousel-card");
            getChildren().add(content);
        }};
        cardWrapper.setOnMouseClicked(e -> {

            if (!animationInProgress) {
                if (autoRotationEnabled) {
                    autoRotationTimeline.stop();
                    autoRotationTimeline.play();}
                try {
                    if(cardWrapper.getViewOrder()!=0){
                        animateToIndex(cardWrappers.indexOf(cardWrapper));}
                } catch (Exception ex) {
                    System.err.println("Error navigating to card: " + ex.getMessage());
                }
            }
        });
        cardWrapper.layoutBoundsProperty().addListener((obs, old, newBounds) -> {
            if ((Math.abs(old.getWidth() - newBounds.getWidth()) > 100 ||
                    Math.abs(old.getHeight() - newBounds.getHeight()) > 100) &&
                    old.getHeight()!=newBounds.getHeight()) {
                updateDynamicLayoutValues();
            }
        });
        return cardWrapper;
    }

    private Button createDotButton(int index) {
        Button dot = new Button(){{
            getStyleClass().add("carousel-dot");
            setMinSize(DEFAULT_SIZE, DEFAULT_SIZE);
            setPrefSize(DEFAULT_SIZE, DEFAULT_SIZE);
            setMaxSize(DEFAULT_SIZE, DEFAULT_SIZE);
        }};
        dot.setOnAction(event -> {
            if (!animationInProgress && getSkinnable().getCurrentIndex() != index) {
                try {
                    if (autoRotationEnabled) {
                        autoRotationTimeline.stop();
                        autoRotationTimeline.play();
                    }
                    getSkinnable().goTo(index);
                } catch (Exception e) {
                    System.err.println("CarouselSkin: Method createDotButton error when clicking dot: " + e.getMessage());
                }
            }
        });

        return dot;
    }

    /**
     * Animates the carousel transition to the target index.
     * @param targetIndex is the card of the user should move
     *  How does it work?
     *      1 calculate the direction, if is a jum of two it
     *      2 take the nodes
     *      3 for every node calculate the animation associated
     *      4 calculate the keyframes
     *      5 animation set on finished report the nodes at the final position
     *      See the documentation for more info
     *
     */
    private void animateToIndex(int targetIndex) {
        boolean goingRight;
        if (currentDisplayedIndex == cardWrappers.size() - 1 && targetIndex == 0) {
            goingRight = true;
        } else if (currentDisplayedIndex == 0 && targetIndex == cardWrappers.size() - 1) {
            goingRight = false;
        } else {
            goingRight = targetIndex > currentDisplayedIndex;
        }
        int itemCount = getSkinnable().getItems().size();
        if (itemCount <= 1) {
            jumpToIndex(targetIndex, false);
            return;
        }
        animationInProgress = true;

        if (animationTimeline != null) {
            animationTimeline.stop();
        }
        animationTimeline = new Timeline();

        int normalizedCurrentIndex = normalizeIndex(currentDisplayedIndex, itemCount);
        int normalizedTargetIndex = normalizeIndex(targetIndex, itemCount);

        Node currentCenterCard = cardWrappers.get(normalizedCurrentIndex);
        Node currentLeftCard =  cardWrappers.get(normalizeIndex(normalizedCurrentIndex - 1, itemCount)) ;
        Node currentRightCard = cardWrappers.get(normalizeIndex(normalizedCurrentIndex + 1, itemCount));

        Node targetCenterCard = cardWrappers.get(normalizedTargetIndex);
        Node targetLeftCard =  cardWrappers.get(normalizeIndex(normalizedTargetIndex - 1, itemCount));
        Node targetRightCard = cardWrappers.get(normalizeIndex(normalizedTargetIndex + 1, itemCount));

        Node enteringCard = goingRight ? targetRightCard : targetLeftCard;
        Node leavingCard = goingRight ? currentLeftCard : currentRightCard;


        List<Node> cardsToAnimate = new ArrayList<>();

        if (currentLeftCard != null)
            cardsToAnimate.add(currentLeftCard);

        cardsToAnimate.add(currentCenterCard);

        if (currentRightCard != null && currentRightCard != currentLeftCard)
            cardsToAnimate.add(currentRightCard);


        if (enteringCard != null && !cardsToAnimate.contains(enteringCard))
            cardsToAnimate.add(enteringCard);


        if (!cardsToAnimate.contains(targetCenterCard))
            cardsToAnimate.add(targetCenterCard);


        cardsContainer.getChildren().clear();
        cardsContainer.getChildren().addAll(cardsToAnimate);
        for (Node card : cardsToAnimate) {
            if (card == currentCenterCard) {
                card.setTranslateX(0);
                card.setScaleX(CENTER_SCALE); card.setScaleY(CENTER_SCALE);
                card.setViewOrder(0.1);
                card.setOpacity(1);
            }
            else if (card == currentLeftCard) {
                card.setTranslateX(-dynamicSlideDistance);
                card.setScaleX(SIDE_SCALE); card.setScaleY(SIDE_SCALE);
                card.setOpacity(1);
            }
            else if (card == currentRightCard) {
                double initialRightTranslate = itemCount == 2 ?
                        -dynamicSlideDistance / 1.5 : dynamicSlideDistance;
                card.setTranslateX(initialRightTranslate);
                card.setScaleX(SIDE_SCALE); card.setScaleY(SIDE_SCALE);
                card.setOpacity(1);
            }
            else if (card == enteringCard) {
                card.setTranslateX(goingRight ? dynamicSlideDistance * 1.5 : -dynamicSlideDistance * 2.5);
                card.setScaleX(SIDE_SCALE); card.setScaleY(SIDE_SCALE);
                card.setOpacity(0);
            }
            else {
                card.setOpacity(0);
                card.setTranslateX(0);
                card.setScaleX(SIDE_SCALE); card.setScaleY(SIDE_SCALE);
            }
        }

        List<KeyFrame> keyFrames = new ArrayList<>();

        for (Node card : cardsToAnimate) {double targetTranslateX = card.getTranslateX();
            double targetScale = card.getScaleX();
            double targetOpacity = card.getOpacity();

            Interpolator translateInterpolator = Interpolator.EASE_BOTH;
            Interpolator scaleInterpolator = Interpolator.EASE_BOTH;
            Interpolator opacityInterpolator = Interpolator.EASE_BOTH;


            if (card == targetCenterCard) {
                targetTranslateX = goingRight ? 0 : dynamicSlideDistance/3;
                targetScale = CENTER_SCALE;
                targetOpacity = 1;
               if(goingRight){
                   card.setViewOrder(0);
               }
            } else if (card == targetLeftCard) {
                targetTranslateX = goingRight ? -dynamicSlideDistance : -dynamicSlideDistance*1.5;
                targetScale = SIDE_SCALE;
                targetOpacity = 1;
                if(!goingRight){
                    card.setViewOrder(2);
                }
            } else if (card == targetRightCard) {
                targetTranslateX = goingRight ? dynamicSlideDistance : dynamicSlideDistance*1.5;
                targetScale = SIDE_SCALE;
                targetOpacity = 1;
                card.setViewOrder(2);

            } else if (card == leavingCard) {
                targetTranslateX = goingRight ? -dynamicSlideDistance * 1.5 : dynamicSlideDistance * 1.5;
                targetScale = SIDE_SCALE;
                targetOpacity = 0;
                opacityInterpolator = Interpolator.EASE_IN;
                if (!goingRight)
                    card.setViewOrder(3);
            }

            List<KeyValue> cardKeyValues = new ArrayList<>();

            if (Math.abs(card.getTranslateX() - targetTranslateX) > 0.1)
                cardKeyValues.add(new KeyValue(card.translateXProperty(), targetTranslateX, translateInterpolator));


            if (Math.abs(card.getScaleX() - targetScale) > 0.01) {
                cardKeyValues.add(new KeyValue(card.scaleXProperty(), targetScale, scaleInterpolator));
                cardKeyValues.add(new KeyValue(card.scaleYProperty(), targetScale, scaleInterpolator));
            }

            if (Math.abs(card.getOpacity() - targetOpacity) > 0.01)
                cardKeyValues.add(new KeyValue(card.opacityProperty(), targetOpacity, opacityInterpolator));

            if (!cardKeyValues.isEmpty())
                keyFrames.add(new KeyFrame(TRANSITION_DURATION, cardKeyValues.toArray(new KeyValue[0])));

        }

        animationTimeline.getKeyFrames().addAll(keyFrames);

        animationTimeline.setOnFinished(e -> {
            cardsContainer.getChildren().clear();
            List<Node> finalVisibleCards = new ArrayList<>();
            if (itemCount == 2) {
                if (targetCenterCard == cardWrappers.get(0)) {
                    finalVisibleCards.add(cardWrappers.get(1));
                } else {
                    finalVisibleCards.add(cardWrappers.getFirst());
                }
                finalVisibleCards.add(targetCenterCard);
            } else {
                if (targetLeftCard != null)
                    finalVisibleCards.add(targetLeftCard );

                finalVisibleCards.add(targetCenterCard );
                if (targetRightCard!=null)
                    finalVisibleCards.add(targetRightCard );
            }
            cardsContainer.getChildren().addAll(finalVisibleCards);
            for (Node card : finalVisibleCards) {
                boolean isCenter = (card == targetCenterCard);
                boolean isLeft = (card == targetLeftCard);
                double finalTranslateX = 0;
                double finalScale = SIDE_SCALE;
                double finalViewOrder = 1;
                if (isCenter) {
                    finalScale = CENTER_SCALE;
                    finalViewOrder = 0;
                }
                else {
                    if (itemCount == 2)
                        finalTranslateX = -dynamicSlideDistance / 1.5;
                    else
                        finalTranslateX = isLeft ? -dynamicSlideDistance : dynamicSlideDistance;

                }
                card.setTranslateX(finalTranslateX);
                card.setScaleX(finalScale);
                card.setScaleY(finalScale);
                card.setOpacity(1);
                card.setViewOrder(finalViewOrder);
            }
            currentDisplayedIndex = normalizedTargetIndex;
            animationInProgress = false;

        });
        updateDots(normalizedTargetIndex);

        animationTimeline.play();
    }


    private void jumpToIndex(int targetIndex, boolean forceRecalculateDynamics) {
        int itemCount = getSkinnable().getItems().size();
        if (itemCount == 0) return;

        if (forceRecalculateDynamics) {
            updateDynamicLayoutValues();
        }

        if (animationTimeline != null) {
            animationTimeline.stop();
            animationInProgress = false;
        }

        int normalizedIndex = normalizeIndex(targetIndex, itemCount);
        currentDisplayedIndex = normalizedIndex;

        int prevIndex = normalizeIndex(normalizedIndex - 1, itemCount );
        int nextIndex = normalizeIndex( normalizedIndex + 1 , itemCount );
        cardsContainer.getChildren().clear();
        List<Node> visibleCards = new ArrayList<>();
        Node centerCard = cardWrappers.get(normalizedIndex);
        Node leftCard = (itemCount > 1) ? cardWrappers.get(prevIndex ) : null;
        Node rightCard = (itemCount > 1 )? cardWrappers.get(nextIndex) : null;

        if (itemCount == 1 )
            visibleCards.add(centerCard);
        else if ( itemCount == 2) {
            if ( normalizedIndex == 0) {
                visibleCards.add(rightCard);
                visibleCards.add(centerCard);
            }
            else {
                visibleCards.add(leftCard);
                visibleCards.add(centerCard);
            }
        } else {
            if (leftCard != null)
                visibleCards.add(leftCard);

            visibleCards.add(centerCard);

            if (rightCard != null)
                visibleCards.add(rightCard);
        }

        cardsContainer.getChildren().addAll(visibleCards);

        for (Node card : visibleCards) {
            boolean isCenter = (card == centerCard);
            boolean isLeft = (card == leftCard);

            double targetTranslateX = 0;
            if (!isCenter) {
                if (itemCount == 2)
                    targetTranslateX = -dynamicSlideDistance / 1.5;
                else
                    targetTranslateX = isLeft ? -dynamicSlideDistance : dynamicSlideDistance;

            }
            card.setTranslateX(targetTranslateX);
            double scale = isCenter ? CENTER_SCALE : SIDE_SCALE;
            card.setScaleX(scale);
            card.setScaleY(scale);
            card.setOpacity(isCenter || itemCount > 1 ? 1 : 0);
            card.setViewOrder(isCenter ? 0 : 1);
        }

        updateDots(normalizedIndex);

        if (getSkinnable().getCurrentIndex() != normalizedIndex) {
            getSkinnable().setCurrentIndex(normalizedIndex);
        }
    }

    private void updateDots(int activeIndex) {
        int itemCount = getSkinnable().getItems().size();
        if (itemCount == 0) return;
        int normalizedActiveIndex = normalizeIndex(activeIndex, itemCount);

        for (int i = 0; i < dotsContainer.getChildren().size(); i++) {
            Node node = dotsContainer.getChildren().get(i);
            if (node instanceof Button dot) {

                if(i == normalizedActiveIndex)// is active
                    dot.getStyleClass().add("carousel-dot-active");
                else
                    dot.getStyleClass().remove("carousel-dot-active");
            }
        }
    }

    @Override
    public void dispose() {
        if (animationTimeline != null) {
            animationTimeline.stop();
            animationTimeline.getKeyFrames().clear();
            animationTimeline = null;
        }
        if (autoRotationTimeline != null) {
            autoRotationTimeline.stop();
            autoRotationTimeline.getKeyFrames().clear();
            autoRotationTimeline = null;
        }
        Carousel control = getSkinnable();
        if (control != null) {
            control.getItems().removeListener((ListChangeListener<? super Node>) c -> {} );
            control.currentIndexProperty().removeListener((obs, o, n) -> { });
            control.sceneProperty().removeListener((obs, o, n) ->{} );

            if (control.getScene() != null && control.getScene().getWindow() != null) {
                Window window = control.getScene().getWindow();
                window.heightProperty().removeListener((heightObs, o, n) -> { });
            }
        }
        for (Node cardWrapper : cardWrappers) {
            if (cardWrapper instanceof StackPane) {
                cardWrapper.layoutBoundsProperty().removeListener((obs, o, n) -> { });
                cardWrapper.setOnMouseClicked(null);
            }
        }
        for (Node node : dotsContainer.getChildren()) {
            if (node instanceof Button dot) {
                dot.setOnAction(null);
            }
        }
        cardWrappers.clear();
        cardsContainer.getChildren().clear();
        dotsContainer.getChildren().clear();
        mainContainer.getChildren().clear();
        getChildren().clear();
        clipRect.widthProperty().unbind();
        clipRect.heightProperty().unbind();
        mainContainer.setClip(null);
        super.dispose();
    }
}