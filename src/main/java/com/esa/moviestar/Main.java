package com.esa.moviestar;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

public class Main extends Application{

    public final static ResourceBundle resourceBundle = ResourceBundle.getBundle("com.esa.moviestar.images.svg-paths.general-svg");

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login/access.fxml"), resourceBundle);
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Moviestar");
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(720);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/esa/moviestar/images/logo.png"))));
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //This method is important for closing the application properly, without this the app remain in the background
    //Probably there is some thread that block the normal closing
    @Override
    public void stop() throws Exception {
        Platform.exit();
        System.exit(0);
        super.stop();
    }

    public static void main(String[] args){
        launch(args);

    }
}