package com.esa.moviestar.database;

import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.Utente;

import java.sql.*;
import java.util.*;
import java.util.stream.*;
import java.util.stream.IntStream;

public class ContentDao {
    private static Connection connection;


    public ContentDao() {
        try {
            connection = DataBaseManager.getConnection();
        } catch (SQLException e) {
            System.err.println("Errore di connessione al database: " + e.getMessage());
        }
    }


    public List<List<Content>> getHomePageContents(Utente user) {
        List<Integer> gusti = user.getGustiComeLista();
        String top_genres = IntStream.range(0, gusti.size()).boxed().sorted(Comparator.comparing(gusti::get)).map(Object::toString).limit(3).collect(java.util.stream.Collectors.joining(","));
        List<Integer> list = IntStream.range(0, gusti.size()).boxed().sorted(Comparator.comparing(gusti::get).reversed()).limit(3).toList();
        String bottom_genres = list.stream().map(Object::toString).collect(java.util.stream.Collectors.joining(","));
        String query =
                // Popular content from top genres
                "SELECT * FROM(SELECT C.*, 0 AS Ordinamento " +
                        "FROM Contenuto C JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                        "WHERE CG.ID_Genere IN (" + top_genres + ") " +
                        "ORDER BY C.Click DESC LIMIT 5)" +

                        " UNION ALL " +
                        // Random top genres content
                        "SELECT * FROM(SELECT C.*, 1 AS Ordinamento FROM Contenuto C " +
                        "JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                        "WHERE CG.ID_Genere IN (" + top_genres + ") " +
                        "ORDER BY RANDOM() LIMIT 10)" +

                        " UNION ALL " +
                        // New releases
                        "SELECT * FROM(SELECT C.*, 2 AS Ordinamento FROM Contenuto C ORDER BY Data_di_pubblicazione DESC LIMIT 8)" +

                        " UNION ALL " +
                        // Favorite tag but not watched
                        "SELECT * FROM(SELECT C.*, 3 AS Ordinamento FROM Contenuto C " +
                        "JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                        "LEFT JOIN Cronologia CR ON C.ID_Contenuto = CR.ID_Contenuto AND CR.ID_Utente = " + user.getID() + " " +
                        "WHERE CG.ID_Genere = ( " +
                        "SELECT ID_Genere FROM Contenuti_Generi WHERE ID_Contenuto IN ( " +
                        "SELECT ID_Contenuto FROM Contenuti_Generi WHERE ID_Genere = " + list.getFirst() + " LIMIT 1 ) LIMIT 1" +
                        ") AND CR.ID_Contenuto IS NULL AND C.N_Episodi = 0 ORDER BY RANDOM() LIMIT 8)" +

                        " UNION ALL " +
                        // Similar to last watched
                        "SELECT * FROM(SELECT C2.*, 4 AS Ordinamento FROM Contenuti_Generi CG1 " +
                        "JOIN Contenuti_Generi CG2 ON CG1.ID_Genere = CG2.ID_Genere AND CG1.ID_Contenuto != CG2.ID_Contenuto " +
                        "JOIN Contenuto C2 ON CG2.ID_Contenuto = C2.ID_Contenuto " +
                        "WHERE CG1.ID_Contenuto = ( SELECT CR.ID_Contenuto FROM Cronologia CR WHERE CR.ID_Utente = " + user.getID() + " " +
                        "ORDER BY CR.DataVisione DESC LIMIT 1 ) ORDER BY RANDOM() LIMIT 7)" +
                        " UNION ALL " +

                        // Recently watched
                        "SELECT * FROM(SELECT C.*, 5 AS Ordinamento FROM Cronologia CR JOIN Contenuto C ON CR.ID_Contenuto = C.ID_Contenuto " +
                        "WHERE CR.ID_Utente = " + user.getID() + " ORDER BY CR.DataVisione DESC LIMIT 7)" +

                        " UNION ALL " +

                        // Recommended series
                        "SELECT * FROM(SELECT C.*, 6 AS Ordinamento FROM Contenuto C JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                        "WHERE CG.ID_Genere IN (" + top_genres + ") AND C.N_Episodi > 0 ORDER BY RANDOM() LIMIT 7)" +

                        " UNION ALL " +
                        // User favorites
                        "SELECT * FROM(SELECT C.*, 7 AS Ordinamento FROM Preferiti P JOIN Contenuto C ON P.ID_Contenuto = C.ID_Contenuto " +
                        "WHERE P.ID_Utente = " + user.getID() + " ORDER BY RANDOM() LIMIT 7)" +

                        " UNION ALL " +
                        // Other categories (bottom genres)
                        "SELECT * FROM(SELECT C.*, 8 AS Ordinamento FROM Contenuto C JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                        "WHERE CG.ID_Genere IN (" + bottom_genres + ") ORDER BY RANDOM() LIMIT 7)";

        return getPagesContents(user, query);
    }

    public List<List<Content>> getFilterPageContents(Utente user, boolean isFilm) {
        List<Integer> gusti = user.getGustiComeLista();
        String top_genres = IntStream.range(0, gusti.size()).boxed().sorted(Comparator.comparing(gusti::get)).map(Object::toString).limit(3).collect(java.util.stream.Collectors.joining(","));
        String query;
        if (isFilm) {
            // Popular content from top genres
            query = "SELECT * FROM ( SELECT C.*, 0 AS Ordinamento  FROM Contenuto C JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto  " +
                    "WHERE CG.ID_Genere IN (" + top_genres + ") AND C.Stagioni = 0 AND C.N_Episodi = 0 ORDER BY Data_di_pubblicazione DESC LIMIT 10 ) " +
                    "UNION ALL  " +
                    "SELECT * FROM ( SELECT C.*, 1 AS Ordinamento  FROM Contenuto C JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto  " +
                    "WHERE CG.ID_Genere IN (" + top_genres + ")  AND C.Stagioni = 0 AND C.N_Episodi = 0 ORDER BY RANDOM() LIMIT 7 ) " +
                    "UNION ALL " +
                    "SELECT * FROM ( SELECT C.*, 2 AS Ordinamento FROM Contenuto C JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto  " +
                    "    LEFT JOIN Cronologia CR ON C.ID_Contenuto = CR.ID_Contenuto AND CR.ID_Utente = " + user.getID() + " WHERE CG.ID_Genere = ( SELECT ID_Genere  " +
                    "        FROM Contenuti_Generi  " +
                    "        WHERE ID_Contenuto IN ( SELECT ID_Contenuto FROM Contenuti_Generi " +
                    "            WHERE ID_Genere IN (" + top_genres +
                    ") LIMIT 1 ) LIMIT 1 ) AND CR.ID_Contenuto IS NULL  AND C.Stagioni = 0 AND C.N_Episodi = 0 ORDER BY RANDOM() LIMIT 8 ) " +
                    "UNION ALL  " +
                    "SELECT * FROM ( " +
                    "    SELECT C.*, 3 AS Ordinamento FROM Contenuto C JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto  " +
                    "    WHERE C.Stagioni = 0 AND C.N_Episodi = 0 ORDER BY RANDOM() LIMIT 8 ) " +
                    " UNION ALL " +
                    "SELECT * FROM ( SELECT C.*, 4 AS Ordinamento " +
                    "    FROM Contenuto C JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                    "    WHERE C.Stagioni = 0 AND C.N_Episodi = 0 ORDER BY RANDOM() LIMIT 8" +
                    ");";
        } else {
            query = "SELECT * FROM (" +
                    "SELECT C.*, 0 AS Ordinamento " +
                    "FROM Contenuto C " +
                    "JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                    "WHERE CG.ID_Genere IN (" + top_genres + ") " +
                    "AND (C.Stagioni > 0 OR C.N_Episodi > 0) " +
                    "ORDER BY Data_di_pubblicazione DESC " +
                    "LIMIT 10 " +
                    ") " +
                    "UNION ALL " +
                    "SELECT * FROM (" +
                    "SELECT C.*, 1 AS Ordinamento " +
                    "FROM Contenuto C " +
                    "JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                    "WHERE CG.ID_Genere IN (" + top_genres + ") " +
                    "AND (C.Stagioni > 0 OR C.N_Episodi > 0) " +
                    "ORDER BY RANDOM() " +
                    "LIMIT 7 " +
                    ") " +
                    "UNION ALL " +
                    "SELECT * FROM (" +
                    "SELECT C.*, 2 AS Ordinamento " +
                    "FROM Contenuto C " +
                    "JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                    "LEFT JOIN Cronologia CR ON C.ID_Contenuto = CR.ID_Contenuto AND CR.ID_Utente = " + user.getID() +
                    " WHERE CG.ID_Genere = (" +
                    "SELECT ID_Genere " +
                    "FROM Contenuti_Generi " +
                    "WHERE ID_Contenuto IN (" +
                    "SELECT ID_Contenuto " +
                    "FROM Contenuti_Generi " +
                    "WHERE ID_Genere IN (" + top_genres + ") " +
                    "LIMIT 1 " +
                    ") " +
                    "LIMIT 1 " +
                    ") " +
                    "AND CR.ID_Contenuto IS NULL " +
                    "AND (C.Stagioni > 0 OR C.N_Episodi > 0) " +
                    "ORDER BY RANDOM() " +
                    "LIMIT 8 " +
                    ") " +
                    "UNION ALL " +
                    "SELECT * FROM (" +
                    "SELECT C.*, 4 AS Ordinamento " +
                    "FROM Contenuto C " +
                    "JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                    "WHERE C.Stagioni > 0 OR C.N_Episodi > 0 " +
                    "ORDER BY RANDOM() " +
                    "LIMIT 8 " +
                    ") " +
                    "UNION ALL " +
                    "SELECT * FROM (" +
                    "SELECT C.*, 4 AS Ordinamento " +
                    "FROM Contenuto C " +
                    "JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                    "WHERE C.Stagioni > 0 OR C.N_Episodi > 0 " +
                    "ORDER BY RANDOM() " +
                    "LIMIT 10 " +
                    ");";
        }

        return getPagesContents(user, query);
    }

    private List<List<Content>> getPagesContents(Utente user, String query) {
        List<List<Content>> list = new Vector<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                while (rs.getInt("Ordinamento") >= list.size()) {
                    list.add(new Vector<>());
                }
                list.get(rs.getInt("Ordinamento")).add(createContent(rs));
            }
        } catch (Exception e) {
            System.err.println("ContentDao: Error to create content list in take_home_contents \n Error:" + e.getMessage());
        }
        return list;
    }

    public Content getFiLmDetails(int id) {
        String query = "SELECT C.* FROM Contenuto C WHERE ID_Contenuto = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            Content content = createContent(rs);
            while (rs.next()) {
                content.addCategory(rs.getInt("ID_Genere"));
            }
            return content;
        } catch (SQLException e) {
            System.err.println("ContentDao: failed to load content: " + id + "\n Error:" + e.getMessage());
        }
        return null;
    }

    public List<Content> take_film_tvseries(String title, Utente u) {
        // Ottieni i top 5 generi preferiti dell'utente utilizzando il metodo funzionale
        List<Integer> gusti = u.getGustiComeLista();
        String top5Genres = IntStream.range(0, gusti.size())
                .boxed()
                .sorted(Comparator.comparing(gusti::get).reversed()) // Ordina per peso decrescente
                .map(Object::toString)
                .limit(5) // Prendi i primi 5 generi
                .collect(Collectors.joining(","));

        if (top5Genres.isEmpty()) {
            return new ArrayList<>();
        }

        List<Content> resultContents = new ArrayList<>();
        Set<Integer> addedContentIds = new HashSet<>();

        try {
            // Utilizziamo una UNION ALL con ordinamento per priorità come nella funzione getFilterPageContents
            String query =
                    // Prima parte: contenuti che INIZIANO con il titolo specificato (priorità 0)
                    "SELECT * FROM ( " +
                            "    SELECT C.*, CG.ID_Genere, 0 AS Ordinamento " +
                            "    FROM Contenuto C " +
                            "    JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                            "    WHERE C.Titolo LIKE '" + title + "%' AND CG.ID_Genere IN (" + top5Genres + ") " +
                            "    GROUP BY C.ID_Contenuto " +
                            "    ORDER BY C.Valutazione DESC " +
                            "    LIMIT 30 " +
                            ") " +
                            "UNION ALL " +
                            // Seconda parte: contenuti che CONTENGONO il titolo ma non iniziano con esso (priorità 1)
                            "SELECT * FROM ( " +
                            "    SELECT C.*, CG.ID_Genere, 1 AS Ordinamento " +
                            "    FROM Contenuto C " +
                            "    JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                            "    WHERE C.Titolo LIKE '%" + title + "%' AND C.Titolo NOT LIKE '" + title + "%' " +
                            "    AND CG.ID_Genere IN (" + top5Genres + ") " +
                            "    GROUP BY C.ID_Contenuto " +
                            "    ORDER BY C.Valutazione DESC " +
                            "    LIMIT 30 " +
                            ") " +
                            "ORDER BY Ordinamento, Valutazione DESC " +
                            "LIMIT 30";

            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    int contentId = rs.getInt("ID_Contenuto");

                    // Evita duplicati
                    if (!addedContentIds.contains(contentId)) {
                        Content content = createContent(rs);

                        // Aggiungi l'ID del genere corrente come categoria iniziale
                        List<Integer> categories = new ArrayList<>();
                        categories.add(rs.getInt("ID_Genere"));
                        content.setCategories(categories);

                        resultContents.add(content);
                        addedContentIds.add(contentId);
                    }
                }
            }

            // Fetch dei generi per tutti i contenuti in un'unica query
            if (!resultContents.isEmpty()) {
                String idsString = resultContents.stream()
                        .map(c -> String.valueOf(c.getId()))
                        .collect(Collectors.joining(","));

                String genresQuery = "SELECT ID_Contenuto, ID_Genere FROM Contenuti_Generi WHERE ID_Contenuto IN (" + idsString + ")";
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery(genresQuery);

                    // Mappa temporanea per memorizzare tutti i generi per ogni contenuto
                    Map<Integer, List<Integer>> contentGenres = new HashMap<>();

                    while (rs.next()) {
                        int contentId = rs.getInt("ID_Contenuto");
                        int genreId = rs.getInt("ID_Genere");

                        contentGenres.computeIfAbsent(contentId, k -> new ArrayList<>()).add(genreId);
                    }

                    // Aggiorna le categorie di ogni contenuto
                    for (Content content : resultContents) {
                        contentGenres.computeIfPresent(content.getId(), (k, v) -> {
                            content.setCategories(v);
                            return v;
                        });
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero dei contenuti: " + e.getMessage(), e);
        }

        return resultContents;
    }

    public List<Content> take_reccomendations(String title, Utente u) {
        // Ottieni i generi preferiti dell'utente
        List<Integer> gusti = u.getGustiComeLista();
        String topGenres = IntStream.range(0, gusti.size())
                .boxed()
                .sorted(Comparator.comparing(gusti::get).reversed()) // Ordina per peso decrescente
                .map(Object::toString)
                .collect(Collectors.joining(","));

        if (topGenres.isEmpty()) {
            return new ArrayList<>();
        }

        List<Content> resultContents = new ArrayList<>();
        Set<Integer> addedContentIds = new HashSet<>();

        try {
            // Ottieni prima i 5 contenuti preferiti dell'utente per escluderli
            String topPreferredQuery =
                    "SELECT C.ID_Contenuto " +
                            "FROM Contenuto C " +
                            "JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                            "WHERE CG.ID_Genere IN (" + topGenres + ") " +
                            "GROUP BY C.ID_Contenuto " +
                            "ORDER BY C.Valutazione DESC " +
                            "LIMIT 5";

            Set<Integer> topPreferredIds = new HashSet<>();
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(topPreferredQuery);
                while (rs.next()) {
                    topPreferredIds.add(rs.getInt("ID_Contenuto"));
                }
            }

            // Costruisci la condizione di esclusione
            String exclusionCondition = "";
            if (!topPreferredIds.isEmpty()) {
                String excludeIds = topPreferredIds.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","));
                exclusionCondition = " AND C.ID_Contenuto NOT IN (" + excludeIds + ") ";
            }

            // Consulta principale che combina titoli che contengono o iniziano con la stringa specificata
            String query =
                    "SELECT C.*, CG.ID_Genere " +
                            "FROM Contenuto C " +
                            "JOIN Contenuti_Generi CG ON C.ID_Contenuto = CG.ID_Contenuto " +
                            "WHERE (C.Titolo LIKE '%" + title + "%') " + exclusionCondition +
                            "AND CG.ID_Genere IN (" + topGenres + ") " +
                            "GROUP BY C.ID_Contenuto " +
                            "ORDER BY RANDOM() " +  // Ordine casuale
                            "LIMIT 15";

            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                    int contentId = rs.getInt("ID_Contenuto");

                    // Evita duplicati
                    if (!addedContentIds.contains(contentId)) {
                        Content content = createContent(rs);

                        // Aggiungi l'ID del genere corrente come categoria iniziale
                        List<Integer> categories = new ArrayList<>();
                        categories.add(rs.getInt("ID_Genere"));
                        content.setCategories(categories);

                        resultContents.add(content);
                        addedContentIds.add(contentId);
                    }
                }
            }

            // Fetch dei generi per tutti i contenuti in un'unica query
            if (!resultContents.isEmpty()) {
                String idsString = resultContents.stream()
                        .map(c -> String.valueOf(c.getId()))
                        .collect(Collectors.joining(","));

                String genresQuery = "SELECT ID_Contenuto, ID_Genere FROM Contenuti_Generi WHERE ID_Contenuto IN (" + idsString + ")";
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery(genresQuery);

                    // Mappa temporanea per memorizzare tutti i generi per ogni contenuto
                    Map<Integer, List<Integer>> contentGenres = new HashMap<>();

                    while (rs.next()) {
                        int contentId = rs.getInt("ID_Contenuto");
                        int genreId = rs.getInt("ID_Genere");

                        contentGenres.computeIfAbsent(contentId, k -> new ArrayList<>()).add(genreId);
                    }

                    // Aggiorna le categorie di ogni contenuto
                    for (Content content : resultContents) {
                        contentGenres.computeIfPresent(content.getId(), (k, v) -> {
                            content.setCategories(v);
                            return v;
                        });
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle raccomandazioni: " + e.getMessage(), e);
        }

        return resultContents;
    }

    public List<Content> getWatched(int idUser,int limit) {
        return getList(idUser,idUser,"SELECT C.* FROM Contenuto C JOIN Cronologia Cr ON C.ID_Contenuto = Cr.ID_Contenuto WHERE Cr.ID_Utente = ? LIMIT ?;");
    }

    public List<Content> getFavourites(int idUser,int limit) {
        return getList(idUser,idUser,"SELECT C.* FROM Contenuto C JOIN Preferiti Pr ON C.ID_Contenuto = Pr.ID_Contenuto WHERE Pr.ID_Utente = ? LIMIT ?;");
    }

    private List<Content> getList(int idUser,int limit,String query){
        List<Content> resultContents = new ArrayList<>();
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setInt(1, idUser);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                resultContents.add(createContent(rs));
            }
        }catch(SQLException e){
            System.err.println("ContentDao: error to take execute query: "+query+ "\n Error:"+e.getMessage());
        }
        return resultContents;
    }

    private Content createContent(ResultSet rs) throws SQLException {
        Content content = new Content();
        content.setId(rs.getInt("ID_Contenuto"));
        content.setTitle(rs.getString("Titolo"));
        content.setPlot(rs.getString("Trama"));
        content.setImageUrl(rs.getString("Link_immagine"));
        content.setVideoUrl(rs.getString("Link_film"));
        content.setDuration(rs.getDouble("Durata"));
        content.setYear(rs.getInt("Anno"));
        content.setRating(rs.getDouble("Valutazione"));
        content.setClicks(rs.getInt("Click"));
        content.setCountry(rs.getString("Nazione"));
        content.setReleaseDate(rs.getString("Data_di_pubblicazione"));
        content.seasonDivided(rs.getInt("Stagioni") > 0);
        content.setSeasonCount(rs.getInt("Stagioni"));
        content.Series(rs.getInt("N_Episodi")>0);
        content.setEpisodeCount(rs.getInt("N_Episodi"));
        return content;
    }


}
