package com.esa.moviestar.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Duration;

public class  Carousel extends Control {

    private static final String DEFAULT_STYLE_CLASS = "carousel";

    /**
     * The items to display in the carousel.
     */
    private final ObservableList<Node> items = FXCollections.observableArrayList();
    public ObservableList<Node> getItems() { return items; }

    /**
     * The currently visible item index.
     */
    private final ObjectProperty<Integer> currentIndex = new SimpleObjectProperty<>(0);
    public final Integer getCurrentIndex() { return currentIndex.get(); }
    public final ObjectProperty<Integer> currentIndexProperty() { return currentIndex; }
    public final void setCurrentIndex(Integer value) { currentIndex.set(value); }

    /**
     * The duration of the transition animation.
     */
    private final ObjectProperty<Duration> animationDuration = new SimpleObjectProperty<>(Duration.millis(500));
    public final Duration getAnimationDuration() { return animationDuration.get(); }
    public final ObjectProperty<Duration> animationDurationProperty() { return animationDuration; }
    public final void setAnimationDuration(Duration value) { animationDuration.set(value); }

    /**
     * Indicates if the carousel should loop around.
     */
    private final ObjectProperty<Boolean> wrapAround = new SimpleObjectProperty<>(false);
    public final Boolean getWrapAround() { return wrapAround.get(); }
    public final ObjectProperty<Boolean> wrapAroundProperty() { return wrapAround; }
    public final void setWrapAround(Boolean value) { wrapAround.set(value); }

    public Carousel() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CarouselSkin(this);
    }

    /**
     * Moves to the next item in the carousel.
     */
    public void next() {
        int nextIndex = getCurrentIndex() + 1;
        if (nextIndex >= getItems().size()) {
            if (getWrapAround()) {
                setCurrentIndex(0);
            }
        } else {
            setCurrentIndex(nextIndex);
        }
    }

    /**
     * Moves to the previous item in the carousel.
     */
    public void previous() {
        int previousIndex = getCurrentIndex() - 1;
        if (previousIndex < 0) {
            if (getWrapAround()) {
                setCurrentIndex(getItems().size() - 1);
            }
        } else {
            setCurrentIndex(previousIndex);
        }
    }

    /**
     * Moves to the item at the specified index.
     * @param index The index of the item to display.
     */
    public void goTo(int index) {
        if (index >= 0 && index < getItems().size()) {
            setCurrentIndex(index);
        }
    }
    public void start(){
        ((CarouselSkin) getSkin()).start();
    }
    public void stop(){
        ((CarouselSkin) getSkin()).stop();
    }
}