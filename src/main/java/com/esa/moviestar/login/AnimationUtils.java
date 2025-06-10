package com.esa.moviestar.login;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtils {
    private static final double regularTime = 300;
    private static final double quickTime = 150;
    private static final double pulseTime = 70;



    private static void general_fade(Node node, double start, double end, double duration){
        FadeTransition fadeTransition= new FadeTransition();
        fadeTransition.setDuration(Duration.millis(duration));
        fadeTransition.setNode(node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }
    public static void slideInFromRight(Node node, double durationMs) {
        node.setOpacity(0);
        TranslateTransition slide = new TranslateTransition();
        FadeTransition fade = new FadeTransition();
        slide.setDuration(Duration.millis(durationMs+0.3));
        slide.setNode(node);
        slide.setFromX(50);
        slide.setToX(0);


        fade.setDuration(Duration.millis(durationMs+0.3));
        fade.setNode(node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        slide.play();

        fade.play();
    }
    public static void animateSimultaneously(Node[] nodes) {
        for (Node node : nodes) {
            slideInFromRight(node,50);
        }
    }

    public static void fadeIn(Node node){
        general_fade(node, 0.0, 1.0, regularTime);
    }
    public static void fadeOut(Node node) {
        general_fade(node, 1.0, 0.0, regularTime);
    }
    //Shake animations
    public static void shake(Node node) {
        TranslateTransition shake = new TranslateTransition();
        shake.setFromX(0);
        shake.setByX(10);
        shake.setAutoReverse(true);
        shake.setCycleCount(4);
        shake.setDuration(Duration.millis(pulseTime));
        shake.setNode(node);

        shake.play();
    }
    // PULSE ANIMATION
    public static void pulse(Node node){
        PauseTransition pause = new PauseTransition();
        double originalX = node.getScaleX();

        double originalY = node.getScaleY();

        node.setScaleX(originalX * 1.2);
        node.setScaleY(originalY * 1.2);


        pause.setDuration(Duration.millis(pulseTime));
        pause.setOnFinished(e -> {

            node.setScaleX(originalX);

            node.setScaleY(originalY);
        });
        pause.play();
    }
}