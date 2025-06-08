package com.esa.moviestar.database;

import java.net.URL;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class  DataBaseManager {
    private static final String DB_NAME = "com/esa/moviestar/DatabaseProjectUID.db";

    public static Connection getConnection() throws SQLException {
        try {
            URL dbUrl = DataBaseManager.class.getResource("/" + DB_NAME);
            if (dbUrl == null) {
                throw new RuntimeException("Database file '" + DB_NAME + "' not found in the classpath.");
            }

            // Construct the JDBC URL for SQLite. The resulting string is the absolute path to the database file, suitable for the JDBC SQLite driver.
            // 1. dbUrl.toURI(): Converts the URL to a URI,  which is a more general representation of a resource identifier.
            //    This is necessary because URLs can contain special characters that need to be handled correctly for file paths.
            // 2. Paths.get(dbUrl.toURI()) : Converts the URI to a Path object, representing the file system  path.
            String jdbcUrl = "jdbc:sqlite:" + Paths.get(dbUrl.toURI());
            return DriverManager.getConnection(jdbcUrl);

        } catch (URISyntaxException e) {
            throw new RuntimeException("Error converting the database URL to a URI. Ensure the path is valid.", e);
        }
    }

}