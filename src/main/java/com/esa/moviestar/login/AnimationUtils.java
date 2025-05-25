package com.esa.moviestar.login;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Utility class providing reusable animations for JavaFX UI components.
 * This class contains static methods for common animation effects.
 */
public class AnimationUtils {

    public static void fadeIn(Node node, double durationMs) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(durationMs), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    public static void fadeIn(Node node) {
        fadeIn(node, 300);
    }

    public static void fadeOut(Node node, double durationMs) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(durationMs), node);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.play();
    }

    public static void fadeOut(Node node) {
        fadeOut(node, 300);
    }

    public static void slideInFromRight(Node node, double durationMs, double distance) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(durationMs), node);
        translateTransition.setFromX(distance);
        translateTransition.setToX(0);
        node.setOpacity(0);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(durationMs), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        translateTransition.play();
        fadeTransition.play();
    }

    public static void slideInFromRight(Node node) {
        slideInFromRight(node, 300, 50);
    }

    public static void quickSlideInFromRight(Node node) {
        slideInFromRight(node, 150, 30);
    }

    public static void slideInFromLeft(Node node, double durationMs, double distance) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(durationMs), node);
        translateTransition.setFromX(-distance);
        translateTransition.setToX(0);
        node.setOpacity(0);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(durationMs), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        translateTransition.play();
        fadeTransition.play();
    }

    public static void slideInFromLeft(Node node) {
        slideInFromLeft(node, 300, 50);
    }

    public static void quickSlideInFromLeft(Node node) {
        slideInFromLeft(node, 150, 30);
    }

    public static void shake(Node node, double durationMs, double distance, int cycles) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(durationMs), node);
        translateTransition.setCycleCount(cycles);
        translateTransition.setFromX(0);
        translateTransition.setByX(distance);
        translateTransition.setAutoReverse(true);
        translateTransition.play();
    }

    public static void shake(Node node) {
        shake(node, 50, 10, 4); // Reduced to 4 cycles for faster effect
    }

    public static void pulse(Node node) {
        // Save the original scale
        double originalScaleX = node.getScaleX();
        double originalScaleY = node.getScaleY();

        // Grow slightly
        node.setScaleX(originalScaleX * 1.2);
        node.setScaleY(originalScaleY * 1.2);

        // Return to original size after a short delay (faster: 70ms instead of 100ms)
        PauseTransition pause = new PauseTransition(Duration.millis(70));
        pause.setOnFinished(e -> {
            node.setScaleX(originalScaleX);
            node.setScaleY(originalScaleY);
        });
        pause.play();
    }

    public static void animateSequentially(Node[] nodes, int animationType, double delayBetweenMs) {
        for (int i = 0; i < nodes.length; i++) {
            PauseTransition delay = new PauseTransition(Duration.millis(i * delayBetweenMs));
            final int index = i;
            delay.setOnFinished(e -> {
                switch (animationType) {
                    case 0:
                        fadeIn(nodes[index]);
                        break;
                    case 1:
                        slideInFromRight(nodes[index]);
                        break;
                    case 2:
                        slideInFromLeft(nodes[index]);
                        break;
                    case 3: // Added quick option
                        quickSlideInFromRight(nodes[index]);
                        break;
                    case 4: // Added quick option
                        quickSlideInFromLeft(nodes[index]);
                        break;
                }
            });
            delay.play();
        }
    }

    public static void animateSequentiallyFast(Node[] nodes, int animationType) {
        // Use a much shorter delay between elements (30ms)
        animateSequentially(nodes, animationType, 30);
    }

    public static void showAllInstantly(Node[] nodes) {
        for (Node node : nodes) {
            node.setOpacity(1.0);
        }
    }

    public static void animateSimultaneously(Node[] nodes, int animationType, double durationMs) {
        for (Node node : nodes) {
            switch (animationType) {
                case 0:
                    fadeIn(node, durationMs);
                    break;
                case 1:
                    slideInFromRight(node, durationMs, 50);
                    break;
                case 2:
                    slideInFromLeft(node, durationMs, 50);
                    break;
                case 3:
                    slideInFromRight(node, durationMs / 2, 30); // Mantiene l'idea di "quick" ma con durata personalizzata
                    break;
                case 4:
                    slideInFromLeft(node, durationMs / 2, 30); // Mantiene l'idea di "quick" ma con durata personalizzata
                    break;
            }
        }
    }

    public static void animateSimultaneously(Node[] nodes, int animationType) {
        // Usa una durata predefinita di 300ms
        animateSimultaneously(nodes, animationType, 500);
    }

    public static void animateContainerWithChildren(javafx.scene.Parent container, int animationType, double durationMs) {
        // First animate the container itself
        switch (animationType) {
            case 0:
                fadeIn(container, durationMs);
                break;
            case 1:
                slideInFromRight(container, durationMs, 50);
                break;
            case 2:
                slideInFromLeft(container, durationMs, 50);
                break;
            case 3:
                slideInFromRight(container, durationMs / 2, 30);
                break;
            case 4:
                slideInFromLeft(container, durationMs / 2, 30);
                break;
        }

        // Make sure all children are fully visible
        for (Node child : container.getChildrenUnmodifiable()) {
            child.setOpacity(1.0);
        }
    }

    public static void animateContainerWithChildren(javafx.scene.Parent container, int animationType) {
        // Usa una durata predefinita di 300ms
        animateContainerWithChildren(container, animationType, 300);
    }
}