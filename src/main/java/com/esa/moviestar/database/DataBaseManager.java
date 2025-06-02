package com.esa.moviestar.database;  // Dichiara il package in cui risiede questa classe

import java.net.URL;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseManager {

    private static final String DB_NAME = "com/esa/moviestar/DatabaseProjectUID.db";  //nome del file che contiente il database

    //Metodo vero e proprio di connessione
    public static Connection getConnection() throws SQLException {
        try {
            URL dbUrl = DataBaseManager.class.getResource("/" + DB_NAME);  //cerca il file nel classpath grazie a getResource , poi usiamo /+DB_NAME perche nella parte prima dello / si trova tutto il path e poi nella parte dopo il nome del file del database
            if (dbUrl == null) { //se il percorso è nullo
                throw new RuntimeException("Database not found in the classpath"); //manda questo errore
            }

            String jdbcUrl = "jdbc:sqlite:" + Paths.get(dbUrl.toURI()).toString();

            /*Converte l'URL del database in un percorso di file che può essere usato da SQLite.
            (dbUrl.toURI()) converte l'URL in un oggetto URI, che è una rappresentazione generica di un percorso.
            (Paths.get(...)) converte l'oggetto URI in un oggetto Path che rappresenta un file nel filesystem.
            (toString()) converte il Path in una stringa che può essere usata dal driver JDBC.
            Il risultato finale è una stringa che rappresenta il percorso assoluto del file del database, */

            return DriverManager.getConnection(jdbcUrl); //la vera e propria connessione avviene qui

        } catch (URISyntaxException e) { //errore nella conversione del percorso file da url a uri
            throw new RuntimeException("Error converting the DB path.", e);
        }
    }

    public static void main(String[] args) {

        try (Connection conn = getConnection()) {
            // Se conn non è null, significa che la connessione è riuscita
            if (conn != null) {
                System.out.println("Successfully connected to the database");
            }
        }
        catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            // Stampa un messaggio di errore con il dettaglio fornito dall’eccezione
        }
        // Fine del metodo main; la connessione è già chiusa dal try-with-resources
    }
}
