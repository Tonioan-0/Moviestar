package com.esa.moviestar.components;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
/**
    Buffer animation is the main animation of the program, it used in mainPagesController to switch by the children
    Sources: Same to CarouselSkin
*/
public class BufferAnimation extends  StackPane{

    private final Rectangle largeSquare;
    private final Rectangle smallSquare;
    private final Timeline animation;


    public BufferAnimation(){
        this(64 );
    }

    public BufferAnimation(double size){
        setPrefSize(size, size);
        setMaxSize(size, size);
        setMinSize(size, size);

        Pane container = new Pane(){{
            setPrefSize(size, size);
        }};

        // Create large cube
        largeSquare = new Rectangle(size, size ){{
            setFill(Color.rgb(115, 65, 190));
            setArcWidth(32);
            setArcHeight(32);
            setLayoutX(0);
            setLayoutY(0);
        }};

        // Create a small  cube
        double smallSize = size * 0.5 ;
        smallSquare = new Rectangle(smallSize , smallSize){{
            setFill(Color.rgb( 31,31, 31));
            setArcHeight(24);
            setArcWidth(24);
            setLayoutX((size - smallSize) / 2);
            setLayoutY((size - smallSize) / 2 );
        }};

        // Add the shapes to the container
        container.getChildren().addAll(largeSquare,  smallSquare);
        getChildren().add(container );

        animation = createAnimation();
        animation.play();
    }

    private Timeline createAnimation(){
        Duration duration = Duration.seconds(2);
        Timeline timeline = new Timeline();
        KeyValue kv1Start =new KeyValue( largeSquare.rotateProperty(),  0, Interpolator.EASE_BOTH);
        KeyValue kv1Mid =  new KeyValue(largeSquare.rotateProperty(), 180, Interpolator.EASE_BOTH);
        KeyValue kv1End =  new KeyValue(largeSquare.rotateProperty(), 180, Interpolator.EASE_BOTH);

        KeyValue kv2Start =  new KeyValue(  smallSquare.rotateProperty(),  0, Interpolator.EASE_BOTH);
        KeyValue kv2Mid   =  new KeyValue(smallSquare.rotateProperty(), -180, Interpolator.EASE_BOTH);
        KeyValue kv2End   =  new KeyValue(smallSquare.rotateProperty(), -180, Interpolator.EASE_BOTH);

        KeyFrame kf0 = new KeyFrame(Duration.ZERO, kv1Start, kv2Start);
        KeyFrame kf1 = new KeyFrame(duration.divide(2),  kv1Mid, kv2Mid);
        KeyFrame kf2 = new KeyFrame(duration, kv1End, kv2End);

        timeline.getKeyFrames().addAll(kf0, kf1,  kf2);
        timeline.setCycleCount(Timeline.INDEFINITE);

        return timeline;
    }


    public void startAnimation(){
        animation.play();
    }
    public void stopAnimation(){
        animation.pause();
    }
}
