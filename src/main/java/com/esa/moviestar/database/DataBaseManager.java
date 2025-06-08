package com.esa.moviestar.database;

import java.net.URL;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class  DataBaseManager {

    // Defines the resource path for the SQLite database file.
    // This path is relative to the classpath root.
    private static final String DB_NAME = "com/esa/moviestar/DatabaseProjectUID.db";

    public static Connection getConnection() throws SQLException {
        try {
            // Attempt to find the database file within the classpath.
            // The leading "/" ensures the path is resolved from the root of the classpath.
            URL dbUrl = DataBaseManager.class.getResource("/" + DB_NAME);

            // If dbUrl is null, the resource was not found.
            if (dbUrl == null) {
                throw new RuntimeException("Database file '" + DB_NAME + "' not found in the classpath.");
            }

            // Construct the JDBC URL for SQLite.
            // 1. dbUrl.toURI(): Converts the URL to a URI, which is a more general representation of a resource identifier.
            //    This is necessary because URLs can contain special characters that need to be handled correctly for file paths.
            // 2. Paths.get(dbUrl.toURI()): Converts the URI to a Path object, representing the file system path.
            // 3. .toString(): Converts the Path object to its string representation.
            // The resulting string is the absolute path to the database file, suitable for the JDBC SQLite driver.
            String jdbcUrl = "jdbc:sqlite:" + Paths.get(dbUrl.toURI()).toString();

            // Establish and return the database connection.
            return DriverManager.getConnection(jdbcUrl);

        } catch (URISyntaxException e) {
            // This exception occurs if the URL retrieved from the classpath
            // cannot be converted into a valid URI (e.g., due to illegal characters).
            throw new RuntimeException("Error converting the database URL to a URI. Ensure the path is valid.", e);
        }
    }


    public static void main(String[] args) {
        // Using try-with-resources to ensure the connection is automatically closed.
        try (Connection conn = getConnection()) {
            // If conn is not null, the connection was successful.
            if (conn != null) {
                System.out.println("Successfully connected to the database: " + DB_NAME);
            }
        }
        // Catches SQLException that might be thrown by getConnection() or during auto-closing.
        catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            // Prints an error message including details from the exception.
        }
        // The connection is automatically closed here by the try-with-resources statement.
    }
}