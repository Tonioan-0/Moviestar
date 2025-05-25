package com.esa.moviestar;

import javafx.application.Application;
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
        //FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("home/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        //Account account = new Account("ss","asda");
        //((MainPagesController)fxmlLoader.getController()).first_load(new Utente(1,"genoveffo","01FF32763200112233445566778899AABB",1,"prova2@gmail.com"),account);
        primaryStage.setTitle("Moviestar");
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(720);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/esa/moviestar/images/logo.png"))));
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args){
        launch(args);

    }
}