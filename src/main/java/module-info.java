module com.esa.moviestar {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.mail;
    requires java.desktop;
    requires jbcrypt;
    requires javafx.graphics;
    requires javafx.media;
    requires java.net.http;
    requires okhttp3;
    requires com.google.gson;
    requires java.sql;
    requires annotations;

    opens com.esa.moviestar to javafx.fxml;
    exports com.esa.moviestar;

    opens com.esa.moviestar.database to javafx.fxml;
    exports com.esa.moviestar.database;

    opens com.esa.moviestar.profile to javafx.fxml;
    exports com.esa.moviestar.profile;


    opens com.esa.moviestar.login to javafx.fxml;
    exports com.esa.moviestar.login;

    opens com.esa.moviestar.movie_view to javafx.fxml;
    exports com.esa.moviestar.movie_view;

    opens com.esa.moviestar.home to javafx.fxml;
    exports com.esa.moviestar.home;

    opens com.esa.moviestar.model to javafx.fxml;
    exports com.esa.moviestar.model;

    opens com.esa.moviestar.components to javafx.fxml;
    exports com.esa.moviestar.components;

    opens com.esa.moviestar.settings to javafx.fxml;
    exports com.esa.moviestar.settings;

    exports com.esa.moviestar.libraries;
    opens com.esa.moviestar.libraries to javafx.fxml;
}