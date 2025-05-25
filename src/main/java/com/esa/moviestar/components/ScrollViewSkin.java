package com.esa.moviestar.components;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ScrollViewSkin extends SkinBase<ScrollView> {

    // header
    private StackPane titleBox;
    private Text titleLabel;
    private Line separator;
    // footer
    private ScrollPane scrollPane;
    private HBox container;
    private Button leftButton;
    private Button rightButton;
    private AnchorPane sliderContainer; // Reference
    private Region leftGradient; // overlay
    private Region rightGradient; // overlay
    // Animation
    private Timeline scrollAnimation;

    // Data
    private final int SPACE = 24;

    // Listeners
    private boolean isHovering = false; // Track hover state
    private ChangeListener<Bounds> layoutBoundsListener;
    private ChangeListener<Number> widthListener;
    private ChangeListener<Paint> backgroundColorListener;
    private ListChangeListener<Node> itemsListener;
    private ChangeListener<Paint> foreColorListener;
    private ChangeListener<Paint> edgeColorListener;

    /**
     * Constructor for the ScrollViewSkin.
     *
     * @param control   The ScrollView control this skin is for.
     *
     */
    public ScrollViewSkin(ScrollView control) {
        super(control);
        // Initialize UI components
        titleBox = createTitleBox();
        sliderContainer = createSlider();
        
        setupHoverBehavior();

        VBox root = new VBox();
        root.getChildren().addAll(titleBox, sliderContainer);

        // Setup bindings
        setupBindings();

        // Initialize buttons to be hidden
        leftButton.setOpacity(0);
        rightButton.setOpacity(0);

        // Add to scene graph
        getChildren().add(root);
    }

    /**
     * Sets up the mouse enter/exit listeners on the slider container
     * to control the visibility of the navigation buttons.
     */
    private void setupHoverBehavior() {
        // Show buttons when mouse enters the container
        sliderContainer.setOnMouseEntered(e -> {
            if(!isHovering&& container.getWidth()>scrollPane.getWidth()){
            isHovering = true;
            updateButtonVisibility(scrollPane.getHvalue());
            }
        });

        // Hide buttons when mouse exits the container
        sliderContainer.setOnMouseExited(e -> {
            if(isHovering){
                isHovering = false;
                hideButton(leftButton, true);
                hideButton(rightButton, false);
            }
        });
    }

    /**
     * Creates the header section containing the title, separator line, and action button.
     *
     * @return The StackPane containing the header elements.
     */
    private StackPane createTitleBox() {
        // Create StackPane container instead of HBox
        StackPane box = new StackPane(){{setMinHeight(SPACE*3); setPrefHeight(SPACE*3); }};
        Paint foreColor = getSkinnable().getForeColor();
        box.setPadding(new Insets(0, SPACE, 0, SPACE));

        // Create label
        titleLabel = new Text();
        titleLabel.setFill(foreColor);
        titleLabel.setFont(Font.font(null, FontWeight.BOLD, SPACE));

        // Create line
        separator = new Line();
        separator.setStrokeWidth(1);
        separator.setStroke(getLinearGradient((Color)foreColor));
        separator.setOpacity(0.9);

        StackPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
        box.getChildren().addAll(separator, titleLabel);
        return box;
    }

    /**
     * Creates the main slider area including the ScrollPane, item container,
     * navigation buttons, and gradient overlays.
     *
     * @return An AnchorPane containing the slider components.
     */
    private AnchorPane createSlider() {
        ScrollView control = getSkinnable();

        // Create scroll pane and container for items
        scrollPane = new ScrollPane();

        // Configure the
        container = new HBox(control.getSpacing());
        container.setAlignment(Pos.CENTER_LEFT);
        container.setSpacing(control.getSpacing());
        container.spacingProperty().bind(control.spacingProperty());
        container.setPadding(new Insets(0,SPACE,0,SPACE));

        // Configure ScrollPane
        scrollPane.setContent(container);
        scrollPane.setVmax(0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPadding(new Insets(0,SPACE,0,SPACE));
        scrollPane.setPannable(true);
        scrollPane.setFitToHeight(true); // Ensure content fits the height
        HBox.setHgrow(scrollPane, Priority.ALWAYS); // Make scroll pane take up all available space

        // Create navigation buttons
        leftButton = createNavButton(true);
        rightButton = createNavButton(false);

        // Create gradient overlays
        leftGradient = new Pane();
        leftGradient.setPrefSize(80,SPACE);
        rightGradient = new Pane();
        rightGradient.setPrefSize(80,SPACE);

        leftGradient.setBackground(getOverlayGradientBackgroundFill(control.getEdgeColor()==null ? (Color)control.getBackgroundColor(): (Color)control.getEdgeColor(), true));
        rightGradient.setBackground(getOverlayGradientBackgroundFill(control.getEdgeColor()==null ? (Color)control.getBackgroundColor(): (Color)control.getEdgeColor(), false));



        AnchorPane containerStack = new AnchorPane();
        final double distance =0.0;
        // Set alignment for all components
        AnchorPane.setBottomAnchor(scrollPane,distance);
        AnchorPane.setTopAnchor(scrollPane,distance);
        AnchorPane.setLeftAnchor(scrollPane,distance);
        AnchorPane.setRightAnchor(scrollPane,distance);

        AnchorPane.setBottomAnchor(rightButton,distance);
        AnchorPane.setTopAnchor(rightButton,distance);
        AnchorPane.setRightAnchor(rightButton,distance);

        AnchorPane.setBottomAnchor(leftButton,distance);
        AnchorPane.setTopAnchor(leftButton,distance);
        AnchorPane.setLeftAnchor(leftButton,distance);

        AnchorPane.setBottomAnchor(leftGradient,distance);
        AnchorPane.setTopAnchor(leftGradient,distance);
        AnchorPane.setLeftAnchor(leftGradient,distance);

        AnchorPane.setBottomAnchor(rightGradient,distance);
        AnchorPane.setTopAnchor(rightGradient,distance);
        AnchorPane.setRightAnchor(rightGradient,distance);

        containerStack.getChildren().addAll(scrollPane,rightGradient,leftGradient, leftButton, rightButton);

        return containerStack;
    }



    /**
     * Creates a navigation button (left or right).
     *
     * @param isLeft True to create the left button, false for the right.
     * @return The configured Button.
     */
    private Button createNavButton(boolean isLeft) {
        Button button = new Button();
        button.setBackground(Background.EMPTY);
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(getSkinnable().getArrowIcon());
        svgPath.setFill(getSkinnable().getForeColor());

        if (isLeft) svgPath.setScaleX(-1);
        button.setGraphic(svgPath);

        button.setOnAction(event -> {
            double currentHValue = scrollPane.getHvalue();
            double visibleItems = calculateVisibleItems();
            int itemCount = getSkinnable().getItems().size();

            double hValueChange = calculateHValueChange(visibleItems, itemCount);

            double targetValue;
            if (isLeft)
                targetValue = Math.max(0.0, currentHValue - hValueChange);
             else
                targetValue = Math.min(1.0, currentHValue + hValueChange);

            if (Math.abs(targetValue - currentHValue) > 0.001)
                animateScroll(targetValue);

            updateButtonVisibility(targetValue);
        });

        button.setMinHeight(80);
        button.setPrefWidth(40);
        button.setMinWidth(40);

        return button;
    }

    /**
     * Estimates the width of a single item in the ScrollView.
     * Assumes all items have roughly the same width. Uses the first item.
     *
     * @return The estimated width of an item, or 0 if no items exist.
     */
    private double estimateItemWidth() {
        if (!getSkinnable().getItems().isEmpty()) {
            Node firstItem = getSkinnable().getItems().getFirst();
            return firstItem.getLayoutBounds().getWidth();
        }
        return 0;
    }

    /**
     * Calculates the approximate number of items that are fully visible within the ScrollPane's viewport.
     *
     * @return The number of visible items (at least 1 if the viewport has width).
     */
    private double calculateVisibleItems() {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        if (viewportWidth <= 0) return 0;

        double itemWidth = estimateItemWidth();
        if (itemWidth <= 0) return 0;

        double spacing = getSkinnable().getSpacing();
        double itemWidthWithSpacing = itemWidth + spacing;

        if (itemWidthWithSpacing <= 0) {
            // Avoid division by zero
            return itemWidth > 0 ? 1 : 0;
        }

        return Math.max(1.0, Math.floor(viewportWidth / itemWidthWithSpacing));
    }


    /**
     * Calculates the required change in the ScrollPane's height value to scroll
     * by approximately (visibleItems - 1) items.
     *
     * @param visibleItems The number of items currently visible (can be fractional).
     * @param itemCount    The total number of items in the ScrollView.
     * @return The proportional change (0.0 to 1.0) in height value needed for the scroll.
     */
    private double calculateHValueChange(double visibleItems, int itemCount) {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double itemWidth = estimateItemWidth();
        double spacing = getSkinnable().getSpacing();

        if (itemCount <= 0 || itemWidth <= 0 || viewportWidth <= 0 || visibleItems <= 0) {
            return 0.0;
        }
        double totalContentWidth = (itemCount * itemWidth) + (itemCount > 1 ? (itemCount - 1) * spacing : 0);

        double totalScrollableWidth = Math.max(1.0, totalContentWidth - viewportWidth);

        if (totalScrollableWidth <= 1.0)
            return 0.0;

        // Determine the number of items to scroll by (at least 1)
        // Calculate the proportional change in height value
        // Calculate the pixel distance to scroll
        double itemsToScroll = Math.max(1.0, Math.floor(visibleItems));
        double itemWidthWithSpacing = itemWidth + spacing;
        double pixelScrollDistance = itemsToScroll * itemWidthWithSpacing;
        double hValueChange = pixelScrollDistance / totalScrollableWidth;
        return Math.max(0.0, Math.min(1.0, hValueChange));
    }

    /**
     * Animates the ScrollPane's horizontal scroll position (height value) to a target value.
     * @param targetValue The target height value (between 0.0 and 1.0).
     */
    private void animateScroll(double targetValue) {
        if (scrollAnimation != null && scrollAnimation.getStatus() == Timeline.Status.RUNNING) {
            scrollAnimation.stop();
        }
        scrollAnimation = new Timeline();

        // Add initial and final keyframe for smooth animation
        KeyValue keyValue = new KeyValue(
                scrollPane.hvalueProperty(),
                targetValue,
                Interpolator.SPLINE(0.4, 0.0, 0.2, 1.0) // Easing curve for natural movement
        );

        KeyFrame keyFrame = new KeyFrame(Duration.millis(1000), keyValue);
        scrollAnimation.getKeyFrames().add(keyFrame );

        scrollAnimation.play();
    }

    /**
     * Updates the visibility (opacity) of the navigation buttons based on the current
     * scroll position (height value) and whether the mouse is hovering over the slider area.
     *
     * @param scrollValue The current height value of the ScrollPane (0.0 to 1.0).
     */
    private void updateButtonVisibility(double scrollValue) {
        if (!isHovering) {
            hideButton(leftButton, true);
            hideButton(rightButton, false);
            return;
        }

        if (scrollValue <= 0.01)
            hideButton(leftButton, true);
        else
            showButton(leftButton, true);

        if (scrollValue >= 0.99)
            hideButton(rightButton, false);
        else
            showButton(rightButton, false);
    }

    /**
     * Animates a button to become visible (fade in and slide in).
     *
     * @param button The button to show.
     * @param isLeft True if it's the left button (affects slide direction).
     */
    private void showButton(Node button, boolean isLeft) {
        Duration duration = Duration.millis(250);
        Interpolator easing = Interpolator.EASE_OUT;
        button.setOpacity(1);

        FadeTransition fadeIn = new FadeTransition(duration, button);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(easing);

        TranslateTransition slideIn = new TranslateTransition(duration, button);
        slideIn.setFromX(isLeft ? -10 : 10);
        slideIn.setToX(0);
        slideIn.setInterpolator(easing);

        ParallelTransition showTransition = new ParallelTransition(button, fadeIn, slideIn);
        showTransition.play();
    }

    /**
     * Animates a button to become hidden (fade out and slide out).
     * Makes the button unmanaged after the animation finishes.
     *
     * @param button The object to hide.
     * @param isLeft True if it's the left button (affects slide direction).
     */
    private void hideButton(Node button, boolean isLeft) {
        if (button.isVisible() && button.isManaged()) {
            Duration duration = Duration.millis(200);
            ParallelTransition hideTransition = getParallelTransition(button, isLeft, duration);
            hideTransition.setOnFinished(event -> button.setOpacity(0));
            hideTransition.play();
        } else {
            button.setOpacity(0);
        }
    }

    /**
     * Transition of the buttons
     *
     * @param button the object
     * @param isLeft the orientation
     * @param duration of the effect
     *
     */
    private static ParallelTransition getParallelTransition(Node button, boolean isLeft, Duration duration) {
        Interpolator easing = Interpolator.EASE_IN;

        FadeTransition fadeOut = new FadeTransition(duration, button);
        fadeOut.setFromValue(button.getOpacity());
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(easing);

        TranslateTransition slideOut = new TranslateTransition(duration, button);
        slideOut.setFromX(0);
        slideOut.setToX(isLeft ? -10 : 10);
        slideOut.setInterpolator(easing);

        return new ParallelTransition(button, fadeOut, slideOut);
    }

    /**
     * Bind the children of the object
     */
    private void setupBindings() {
        ScrollView control = getSkinnable();

        // Listener for layout bounds (clipping)
        layoutBoundsListener = (observable, oldValue, newValue) -> {
            if (control.getRadius() > 0) {
                Rectangle clipRect = new Rectangle(newValue.getWidth(), newValue.getHeight());
                clipRect.setArcWidth(control.getRadius() * 2);
                clipRect.setArcHeight(control.getRadius() * 2);
                control.setClip(clipRect);
            } else {
                control.setClip(null);
            }
        };
        control.layoutBoundsProperty().addListener(layoutBoundsListener);
        titleLabel.textProperty().bind(control.titleProperty());

        // Listener for width changes (separator visibility)
        widthListener = (obs, oldVal, newVal) -> {
            if (separator != null) { // Check separator as it might be nullified during dispose
                if (newVal.doubleValue() < 720) {
                    separator.setVisible(false);
                } else {
                    separator.setVisible(true);
                    separator.setStartX(newVal.doubleValue() * 0.3);
                    separator.setEndX(newVal.doubleValue() * 0.8);
                }
            }
        };
        control.widthProperty().addListener(widthListener);

        // Initialize SVG paths
        updateSVGContent(leftButton, control.getArrowIcon());
        updateSVGContent(rightButton, control.getArrowIcon());
        leftButton.prefHeightProperty().bind(container.heightProperty());
        rightButton.prefHeightProperty().bind(container.heightProperty());

        // Bind background color
        getSkinnable().setBackground(Background.fill(control.getBackgroundColor()));
        backgroundColorListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (control.getEdgeColor() == null) {
                    if (leftGradient != null) leftGradient.setBackground(getOverlayGradientBackgroundFill((Color)newVal, true));
                    if (rightGradient != null) rightGradient.setBackground(getOverlayGradientBackgroundFill((Color)newVal, false));
                }

                ScrollView skin = getSkinnable();
                if (skin != null)
                    skin.setBackground(Background.fill( skin.getBackgroundColor() ));
            }
        };
        control.backgroundColorProperty().addListener(backgroundColorListener);

        Bindings.bindContent(container.getChildren(), control.getItems());
        itemsListener = this::onChanged; // Store the method reference
        control.getItems().addListener(itemsListener);

        // Bind foreground color
        foreColorListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateForeColor((Color)newVal);
            }
        };
        control.foreColorProperty().addListener(foreColorListener);

        // Bind edge color
        edgeColorListener = (obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (leftGradient != null) leftGradient.setBackground(getOverlayGradientBackgroundFill((Color) newVal, true));
                if (rightGradient != null) rightGradient.setBackground(getOverlayGradientBackgroundFill((Color)newVal, false));
            }
            // Ensure components are not null before calling toFront()
            if (sliderContainer != null) {
                if (leftGradient != null && sliderContainer.getChildren().contains(leftGradient)) {
                    leftGradient.toFront();
                }
                if (rightGradient != null && sliderContainer.getChildren().contains(rightGradient)) {
                    rightGradient.toFront();
                }
                if (leftButton != null && sliderContainer.getChildren().contains(leftButton)) {
                    leftButton.toFront();
                }
                if (rightButton != null && sliderContainer.getChildren().contains(rightButton)) {
                    rightButton.toFront();
                }
            }
        };
        control.edgeColorProperty().addListener(edgeColorListener);
    }

    /**
     * Update the height of the control
     * it controls the height of the items and add a minimum space
     */
    private void updateContainerHeight() {
        double maxHeight = 0;
        for (Node item : getSkinnable().getItems()) {
            double itemHeight = item.getLayoutBounds().getHeight();
            if (itemHeight <= 0)
                itemHeight = item.prefHeight(-1);
            maxHeight = Math.max(maxHeight, itemHeight);
        }
        maxHeight+= SPACE;
        if (maxHeight > 0) {
            container.setMinHeight(maxHeight);
            container.setPrefHeight(maxHeight);
        }
    }

    /**
     * Updates the SVG content and fill color for a button's graphic.
     * Assumes the graphic is an SVGPath.
     * @param button     The button whose graphic needs updating.
     * @param svgContent The new SVG path data string.
     */
    private void updateSVGContent(Button button, String svgContent) {
        if (button == null || button.getGraphic() == null)
            return;
        SVGPath svgPath = (SVGPath) button.getGraphic();
        svgPath.setContent(svgContent);
        svgPath.setFill(this.getSkinnable().getForeColor());
        if (button == leftButton)
            svgPath.setScaleX(-1);
    }

    /**
     * Applies the foreground color to all relevant UI elements in the skin.
     * @param color The new foreground color.
     */
    public void updateForeColor (Color color) {
        if (color == null) return;
        titleLabel.setFill(color);
        separator.setStroke(getLinearGradient(color));
        // Navigation Button Icons (assuming they exist and are SVG)
        updateSVGContent(leftButton, getSkinnable().getArrowIcon());
        updateSVGContent(rightButton, getSkinnable().getArrowIcon());

    }
    /**
     * Return the color of the separator
     * @param color the foreground color
     */
    public LinearGradient getLinearGradient (Color color) {
        return new LinearGradient(
                0, 0,      // start X,Y (left edge)
                1, 0,      // end X,Y (right edge)
                true,      // proportional
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.TRANSPARENT),
                new Stop(0.5, color),
                new Stop(1.0, Color.TRANSPARENT)
        );
    }
    //The same but for vertical, used in the search.fxml
    public LinearGradient getVerticalLinearGradient (Color color) {
        return new LinearGradient(
                0, 0,      // start X,Y (left edge)
                0, 1,      // end X,Y (right edge)
                true,      // proportional
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.TRANSPARENT),
                new Stop(0.5, color),
                new Stop(1.0, Color.TRANSPARENT)
        );
    }
    /**
     * Return the color of the overlays
     * @param color the edge color, if null the background color
     */
    public Background getOverlayGradientBackgroundFill(Color color, boolean isLeft) {
        return Background.fill(
                new LinearGradient(isLeft? 0:1,0,
                isLeft? 1:0, 0,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, color),
                new Stop(0.4, color),
                new Stop(1, Color.TRANSPARENT)));
    }

    /**
     * Cleans up resources, listeners, and bindings when the skin is disposed.
     */
    @Override
    public void dispose() {
        ScrollView control = getSkinnable();
        if (scrollAnimation != null) {
            scrollAnimation.stop();
        }
        if (control != null) {
            if (layoutBoundsListener != null)
                control.layoutBoundsProperty().removeListener(layoutBoundsListener);
            if (widthListener != null)
                control.widthProperty().removeListener(widthListener);
            if (backgroundColorListener != null)
                control.backgroundColorProperty().removeListener(backgroundColorListener);
            if (itemsListener != null)
                control.getItems().removeListener(itemsListener);
            if (foreColorListener != null)
                control.foreColorProperty().removeListener(foreColorListener);
            if (edgeColorListener != null)
                control.edgeColorProperty().removeListener(edgeColorListener);
        }
        if (sliderContainer != null) {
            sliderContainer.setOnMouseEntered(null);
            sliderContainer.setOnMouseExited(null);
        }
        if (titleLabel != null) titleLabel.textProperty().unbind();
        if (container != null) {
            container.spacingProperty().unbind();
            if (control != null && control.getItems() != null) {
                Bindings.unbindContent(container.getChildren(), control.getItems());
            }
        }
        if (leftButton != null)
            leftButton.prefHeightProperty().unbind();
        if (rightButton != null)
            rightButton.prefHeightProperty().unbind();

        titleBox = null;
        titleLabel = null;
        separator = null;
        scrollPane = null;
        container = null;
        leftButton = null;
        rightButton = null;
        sliderContainer = null;
        leftGradient = null;
        rightGradient = null;
        scrollAnimation = null;
        layoutBoundsListener = null;
        widthListener = null;
        backgroundColorListener = null;
        itemsListener = null;
        foreColorListener = null;
        edgeColorListener = null;

        super.dispose();
    }

    private void onChanged(ListChangeListener.Change<? extends Node> change) {
        updateContainerHeight();
    }
}