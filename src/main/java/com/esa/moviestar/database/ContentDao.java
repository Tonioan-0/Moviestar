package com.esa.moviestar.database;

import com.esa.moviestar.model.ContentCacheManager;
import com.esa.moviestar.model.Content;
import com.esa.moviestar.model.User;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ContentDao {
    private static Connection connection;
    private final ContentCacheManager cacheManager;


    public ContentDao() {
        this.cacheManager = ContentCacheManager.getInstance();
        try {
            connection = DataBaseManager.getConnection();
        } catch (SQLException e) {
            System.err.println("ContentDao: " + e.getMessage());
        }
    }

    private void populateContentFromResultSet(Content content, ResultSet rs) throws SQLException {
        content.setTitle(rs.getString("Title"));
        content.setPlot(rs.getString("Plot"));
        content.setImageUrl(rs.getString("Image_Url"));
        content.setPosterUrl(rs.getString("Poster_Url"));
        content.setVideoUrl(rs.getString("Film_Url"));
        content.setYear(rs.getInt("Year"));
        content.setRating(rs.getDouble("Rating"));
        content.setPopularity(rs.getInt("Popularity"));
        content.setReleaseDate(rs.getString("Release_Date"));
    }

    // Refactored createContent to use the cache
    private Content createContentFromResultSet(ResultSet rs) throws SQLException {
        int contentId = rs.getInt("ID_Content");
        if (contentId == 0)
            return null;

        Content content = cacheManager.get(contentId);
        if (content != null) {
            // If content is from cache, its fields might be stale, repopulate them
            populateContentFromResultSet(content, rs);
        } else {
            content = new Content();
            content.setId(contentId);
            populateContentFromResultSet(content, rs);
            cacheManager.put(content); // Add newly created and populated content to cache
        }
        return content;
    }


    public void insertContents(List<Content> contents) {
        String insertContentSQL = "INSERT OR REPLACE INTO Content " +
                "(ID_Content, Title, Plot, Image_Url,Poster_Url, Film_Url,  Year, Rating, Popularity,  Release_date, Episodes_Count,  Fetched_Date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertGenreSQL = "INSERT OR IGNORE INTO Content_Genre (ID_Content, ID_Genre) VALUES (?, ?)";

        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("ContentDao: insertContents - Database connection is not available.");
                throw new SQLException("Database connection is not available.");
            }
            connection.setAutoCommit(false);
            try (PreparedStatement stmtInsertContent = connection.prepareStatement(insertContentSQL);
                 PreparedStatement genreStmt = connection.prepareStatement(insertGenreSQL)) {

                for (Content content : contents) {

                    if (content.getId() == 0)
                        continue;

                    cacheManager.put(content);

                    stmtInsertContent.setInt(1, content.getId());
                    stmtInsertContent.setString(2, content.getTitle());
                    stmtInsertContent.setString(3, content.getPlot());
                    stmtInsertContent.setString(4, content.getImageUrl() != null ? content.getImageUrl() : "error");
                    stmtInsertContent.setString(5, content.getPosterUrl() != null ? content.getPosterUrl() : "error");
                    stmtInsertContent.setString(6, content.getVideoUrl() != null ? content.getVideoUrl() : "error");
                    stmtInsertContent.setInt(8, content.getYear());
                    stmtInsertContent.setDouble(9, content.getRating());
                    stmtInsertContent.setInt(10, content.getPopularity());
                    stmtInsertContent.setString(12, content.getReleaseDate() != null ? content.getReleaseDate() : "");
                    stmtInsertContent.setInt(13, content.isSeries() ? 1 : 0);
                    stmtInsertContent.setString(15, OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    stmtInsertContent.addBatch();
                    if(content.getCategories()!=null && ! content.getCategories().isEmpty())
                        for (Integer genreId : content.getCategories()) {
                            genreStmt.setInt(1, content.getId());
                            genreStmt.setInt(2, genreId);
                            genreStmt.addBatch();
                        }
                }
                stmtInsertContent.executeBatch();
                genreStmt.executeBatch();
                connection.commit();
            }
        } catch (SQLException e) {
            System.err.println("ContentDao: Error inserting contents. Attempting rollback. Error: " + e.getMessage());
            try {
                if (connection != null && !connection.isClosed()) connection.rollback();
            } catch (SQLException ex) {
                System.err.println("ContentDao: Error rolling back transaction: " + ex.getMessage());
            }
            throw new RuntimeException("ContentDao: Error inserting contents", e);
        } finally {
            try {
                if (connection != null && !connection.isClosed()) connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("ContentDao: Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    public void deleteExpiredContent() {
        OffsetDateTime oneWeekAgo = OffsetDateTime.now(ZoneOffset.UTC).minusWeeks(1);
        String oneWeekAgoStr = oneWeekAgo.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        List<Integer> idsToDelete = new ArrayList<>();
        String selectExpiredSQL = "SELECT ID_Content FROM Content " +
                "WHERE Fetched_Date < ? " +
                "AND ID_Content NOT IN (SELECT DISTINCT ID_Content FROM History) " +
                "AND ID_Content NOT IN (SELECT DISTINCT ID_Content FROM WatchList)";
        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("ContentDao: Database connection is not available for selecting IDs.");
            } else {
                try (PreparedStatement selectStmt = connection.prepareStatement(selectExpiredSQL)) {
                    selectStmt.setString(1, oneWeekAgoStr);
                    ResultSet rs = selectStmt.executeQuery();
                    while (rs.next()) {
                        idsToDelete.add(rs.getInt("ID_Content"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ContentDao: Error selecting expired content IDs for cache removal: " + e.getMessage());
        }

        String deleteSQL = "DELETE FROM Content " +
                "WHERE Fetched_Date < ? " +
                "AND ID_Content NOT IN (SELECT DISTINCT ID_Content FROM History) " +
                "AND ID_Content NOT IN (SELECT DISTINCT ID_Content FROM WatchList)";

        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("ContentDao: deleteExpiredContent - Database connection is not available for deletion.");
                return;
            }
            try (PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {
                stmt.setString(1, oneWeekAgoStr);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    idsToDelete.forEach(cacheManager::remove); // Remove deleted items from cache
                }
                System.out.println("ContentDao: Deleted " + rowsAffected + " expired content items. Removed from cache: " + idsToDelete.size());
            }
        } catch (SQLException e) {
            System.err.println("ContentDao: Error deleting expired content: " + e.getMessage());
        }
    }

    /**
     * Fetches genre IDs. Tries user-specific tastes first from the Taste table.
     *
     * @param userId  The ID of the user.
     * @param orderBy SQL keyword for ordering user tastes (e.g., "DESC" or "ASC").
     * @param limit   The maximum number of genre IDs to return from user tastes. This limit
     *                is not applied when fetching all genres as a default.
     * @return A comma-separated string of genre IDs
     */
    private String getGenreIdsStringFromTaste(int userId, String orderBy, int limit) {
        List<String> genreIds = new ArrayList<>();
        String userTasteSql = "SELECT ID_Genre FROM Taste WHERE ID_User = ? ORDER BY Weight " + orderBy + " LIMIT ?";

        boolean userTastesFetched = false;
        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("ContentDao: getGenreIdsStringFromTaste - DB connection not available for user tastes. UserID: " + userId);
            } else {
                try (PreparedStatement stmt = connection.prepareStatement(userTasteSql)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, limit);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        genreIds.add(String.valueOf(rs.getInt("ID_Genre")));
                    }
                    userTastesFetched = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("ContentDao: Error fetching genre IDs from Taste table for user " + userId + " (orderBy=" + orderBy + ", limit=" + limit + "): " + e.getMessage() + ". Will try fetching all genres.");
        }

        // If no user-specific genres were found (either query returned empty or an error occurred, or connection was bad)
        if (genreIds.isEmpty()) {
            if (userTastesFetched) { // Only log this if we actually tried and found nothing for the user
                System.out.println("ContentDao: No specific tastes found for user " + userId + " (orderBy=" + orderBy + ", limit=" + limit + "). Fetching all genres as default.");
            } else if (! (connection == null) ) {
                System.out.println("ContentDao: Failed to fetch specific tastes for user " + userId + " (orderBy=" + orderBy + ", limit=" + limit + "). Fetching all genres as default.");
            }
            // SQL to fetch all genres.
            // IMPORTANT: Replace 'Genre' and 'ID_Genre' with your actual table and column names for all genres.
            String allGenresSql = "SELECT ID_Genre FROM Taste ORDER BY ID_Genre";
            try {
                if (connection == null || connection.isClosed()) {
                    System.err.println("ContentDao: getGenreIdsStringFromTaste - DB connection not available for fetching all genres.");
                    return "-1"; // Ultimate fallback
                }
                try (PreparedStatement stmt = connection.prepareStatement(allGenresSql)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        genreIds.add(String.valueOf(rs.getInt("ID_Genre")));
                    }
                }
            } catch (SQLException e) {
                return "-1";
            }

            if (genreIds.isEmpty()) {
                System.err.println("ContentDao: No genres found in the Genre table either (or an error occurred). Returning '-1'.");
                return "-1";
            }
        }

        return String.join(",", genreIds);
    }


    private List<List<Content>> getContentFromDB( String query,int numberOfLists) {
        List<List<Content>> list = new Vector<>();
        for(int i=0;i<numberOfLists;i++)
            list.add(new Vector<>());
        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("ContentDao: getContentFromDB - Database connection is not available.");
                return list;
            }
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Content content = createContentFromResultSet(rs);
                    if (content != null) {
                        list.get(rs.getInt("List")).add(content);
                    }
                }
            }
        } catch (Exception e) { // Catching generic Exception is broad; consider specific ones like SQLException
            System.err.println("ContentDao: Error in getContentFromDB. Query (first 100 chars): " +
                    (query != null ? query.substring(0, Math.min(100, query.length())) : "null") +
                    "... Error:" + e.getMessage());
        }
        return list;
    }


    public List<List<Content>> getHomePageContents(User user) {
        int userId = user.getID();
        String topGenresString = buildGenreFilter( getGenreIdsStringFromTaste(userId, "DESC", 3));
        String bottomGenresString =  buildGenreFilter(getGenreIdsStringFromTaste(userId, "ASC", 3));
        String firstTopGenreString = buildGenreFilter( getGenreIdsStringFromTaste(userId, "DESC", 1));
        String query =
                // Popular content from top genres
                "SELECT DISTINCT  * , 0 AS List FROM (SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                        topGenresString +" ORDER BY C.Popularity DESC LIMIT 5) UNION ALL " +

                        // Random top genres content
                        "SELECT * , 1 AS List FROM (SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                        topGenresString + " ORDER BY RANDOM() LIMIT 10) UNION ALL " +
                        // New releases
                        "SELECT * , 2 AS List FROM (SELECT C.* FROM Content C ORDER BY Release_Date DESC LIMIT 8) UNION ALL " +
                        // Favorite tag(s) but not watched (uses first_top_genre_str which could be one or all)
                        "SELECT * , 3 AS List FROM (SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                        "LEFT JOIN History CR ON C.ID_Content = CR.ID_Content AND CR.ID_User = " + userId + " " +
                        firstTopGenreString + "AND CR.ID_Content IS NULL AND C.Episodes_Count = 0 ORDER BY RANDOM() LIMIT 8) UNION ALL " +

                        "SELECT * , 4 AS List FROM (SELECT C2.* FROM Content_Genre CG1 JOIN Content_Genre CG2 ON CG1.ID_Genre = CG2.ID_Genre AND CG1.ID_Content != CG2.ID_Content " +
                        "JOIN Content C2 ON CG2.ID_Content = C2.ID_Content WHERE CG1.ID_Content = ( SELECT CR.ID_Content FROM History CR WHERE CR.ID_User = " + userId +
                        " ORDER BY CR.Date DESC LIMIT 1 ) ORDER BY RANDOM() LIMIT 7) UNION ALL " +

                        // Recently watched
                        "SELECT * , 5 AS List FROM (SELECT C.* FROM History CR JOIN Content C ON CR.ID_Content = C.ID_Content " +
                        "WHERE CR.ID_User = " + userId + " ORDER BY CR.Date DESC LIMIT 7) UNION ALL " +

                        // Recommended series
                        "SELECT * , 6 AS List FROM (SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                        topGenresString + " AND C.Episodes_Count > 0 ORDER BY RANDOM() LIMIT 7) UNION ALL " +
                        // User favorites (Assuming 'Favourite' table)
                        "SELECT *, 7 AS List FROM (SELECT C.* FROM Favourite P JOIN Content C ON P.ID_Content = C.ID_Content " +
                        "WHERE P.ID_User = " + userId + " ORDER BY RANDOM() LIMIT 7) UNION ALL " +
                        // Other categories (bottom genres - if Taste empty, same as top_genres_str)
                        "SELECT  * , 8 AS List FROM (SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                        bottomGenresString + " ORDER BY RANDOM() LIMIT 7);";

        List<List<Content>> results = getContentFromDB(query,9);
        results.forEach(this::fetchAndSetGenresForContentList);
        return results;
    }
    public String buildGenreFilter(String genreIdsString) {
        if (genreIdsString != null && !genreIdsString.equals("-1")) {
            return "WHERE CG.ID_Genre IN (" + genreIdsString + ") ";
        }
        return "";
    }
    public List<List<Content>> getFilterPageContents(User user, boolean isFilm) {
        // SECURITY_NOTE: user.getID() is directly concatenated into SQL strings below.
        // This is a SQL injection vulnerability. Use PreparedStatement placeholders instead.
        int userId = user.getID();
        String top_genres_str = getGenreIdsStringFromTaste(userId, "DESC", 3); // If Taste empty, will be all genres

        String query;
        String typeConditionSpecific = isFilm ? " AND C.Seasons = 0 AND C.Episodes_Count = 0 " : "AND (C.Seasons > 0 OR C.Episodes_Count > 0) ";
        String typeConditionGeneral = isFilm ? " C.Seasons = 0 AND C.Episodes_Count = 0 " : "(C.Seasons > 0 OR C.Episodes_Count > 0) ";


        if (isFilm) {
            query = "SELECT *, 0 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content  " +
                    "WHERE CG.ID_Genre IN (" + top_genres_str + ") " + typeConditionSpecific + "ORDER BY Release_Date DESC LIMIT 10 ) AS SubF0 " +
                    "UNION ALL  " +
                    "SELECT *, 1 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content  " +
                    "WHERE CG.ID_Genre IN (" + top_genres_str + ") " + typeConditionSpecific + "ORDER BY RANDOM() LIMIT 7 ) AS SubF1 " +
                    "UNION ALL " +
                    // Content from top genre(s) not watched
                    "SELECT *, 2 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content  " +
                    "LEFT JOIN History CR ON C.ID_Content = CR.ID_Content AND CR.ID_User = " + userId + " " +
                    "WHERE CG.ID_Genre IN (" + top_genres_str + ") AND CR.ID_Content IS NULL " + typeConditionSpecific + " ORDER BY RANDOM() LIMIT 8 ) AS SubF2  UNION ALL  " +
                    "SELECT *, 3 AS List FROM ( SELECT C.* FROM Content C WHERE " + typeConditionGeneral + "ORDER BY RANDOM() LIMIT 8 ) AS SubF3 " +
                    "UNION ALL " +
                    "SELECT *, 4 AS List FROM ( SELECT C.* FROM Content C WHERE " + typeConditionGeneral + "ORDER BY RANDOM() LIMIT 8 ) AS SubF4;";
        } else { // For Series
            query = "SELECT *, 0 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content WHERE CG.ID_Genre IN (" + top_genres_str + ") " +
                    typeConditionSpecific + "ORDER BY Release_Date DESC LIMIT 10 ) AS SubS0 UNION ALL " +
                    "SELECT *, 1 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                    "WHERE CG.ID_Genre IN (" + top_genres_str + ") " + typeConditionSpecific + "ORDER BY RANDOM() LIMIT 7 ) AS SubS1 UNION ALL " +
                    // Content from top genre(s) not watched
                    "SELECT *, 2 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                    "LEFT JOIN History CR ON C.ID_Content = CR.ID_Content AND CR.ID_User = " + userId + " WHERE CG.ID_Genre IN (" + top_genres_str + ") " +
                    "AND CR.ID_Content IS NULL " + typeConditionSpecific + "ORDER BY RANDOM() LIMIT 8 ) AS SubS2 UNION ALL " +
                    "SELECT *, 3 AS List FROM ( SELECT C.* FROM Content C WHERE " + typeConditionGeneral + " ORDER BY RANDOM() LIMIT 8 ) AS SubS3 UNION ALL " +
                    "SELECT *, 4 AS List FROM ( SELECT C.* FROM Content C WHERE " + typeConditionGeneral + " ORDER BY RANDOM() LIMIT 10 ) AS SubS4;";
        }
        List<List<Content>> results = getContentFromDB(query,5);
        results.forEach(this::fetchAndSetGenresForContentList);
        return results;
    }


    public Content getFiLmDetails(int id) {
        Content content = cacheManager.get(id); // Try cache first

        String contentQuery = "SELECT * FROM Content WHERE ID_Content = ?;";
        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("ContentDao: getFiLmDetails - Database connection is not available for content ID: " + id);
                return content;
            }
            try (PreparedStatement stmt = connection.prepareStatement(contentQuery)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    if (content == null) {
                        content = new Content();
                        content.setId(id);
                        populateContentFromResultSet(content, rs);
                        cacheManager.put(content);
                    } else {
                        populateContentFromResultSet(content, rs);
                    }
                } else {
                    if (content != null) {
                        cacheManager.remove(id);
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("ContentDao: failed to load main content data for ID: " + id + ". Error: " + e.getMessage());
            return content;
        }

        List<Integer> categoryIds = new ArrayList<>();
        String genreQuery = "SELECT ID_Genre FROM Content_Genre WHERE ID_Content = ?;";
        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("ContentDao: getFiLmDetails - Database connection is not available for genres for content ID: " + id);
                return content; // Content object exists, but categories might be incomplete/stale
            }
            try (PreparedStatement genreStmt = connection.prepareStatement(genreQuery)) {
                genreStmt.setInt(1, id);
                ResultSet genreRs = genreStmt.executeQuery();
                while (genreRs.next()) {
                    categoryIds.add(genreRs.getInt("ID_Genre"));
                }
                content.setCategories(categoryIds.stream().distinct().collect(Collectors.toList()));
            }
        } catch (SQLException e) {
            System.err.println("ContentDao: failed to load genres for content ID: " + id + ". Error: " + e.getMessage());
        }
        return content;
    }


    private void fetchAndSetGenresForContentList(List<Content> contents) {
        if (contents == null || contents.isEmpty()) {
            return;
        }
        List<Content> validContents = contents.stream().filter(Objects::nonNull).toList();
        if (validContents.isEmpty()) {
            return;
        }

        String idsString = validContents.stream()
                .map(c -> String.valueOf(c.getId()))
                .distinct()
                .collect(Collectors.joining(","));

        if (idsString.isEmpty()) return;

        String genresQuery = "SELECT ID_Content, ID_Genre FROM Content_Genre WHERE ID_Content IN (" + idsString + ")";
        Map<Integer, List<Integer>> contentGenresMap = new HashMap<>();

        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("ContentDao: fetchAndSetGenresForContentList - Database connection is not available.");
                return;
            }
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(genresQuery);
                while (rs.next()) {
                    int contentId = rs.getInt("ID_Content");
                    int genreId = rs.getInt("ID_Genre");
                    contentGenresMap.computeIfAbsent(contentId, k -> new ArrayList<>()).add(genreId);
                }
            }
        } catch (SQLException e) {
            System.err.println("ContentDao: Error fetching genres for multiple contents. IDs (first 100 chars): " +
                    (idsString.length() > 100 ? idsString.substring(0, 100) : idsString) +
                    "... Error: " + e.getMessage());
        }

        for (Content content : validContents) {
            List<Integer> genres = contentGenresMap.getOrDefault(content.getId(), Collections.emptyList());
            content.setCategories(genres.stream().distinct().collect(Collectors.toList()));
        }
    }

    public List<Content> getFavourites(int idUser, int limit) {
        return getListWithLimit(idUser, limit, "SELECT C.* FROM Content C JOIN Favourite Pr ON C.ID_Content = Pr.ID_Content WHERE Pr.ID_User = ?");
    }

    public List<Content> getWatched(int idUser, int limit) {
        return getListWithLimit(idUser, limit, "SELECT C.* FROM Content C JOIN History Cr ON C.ID_Content = Cr.ID_Content WHERE Cr.ID_User = ?");
    }

    private List<Content> getListWithLimit(int idUser, int limit, String queryWithoutLimitAndPlaceholder) {
        List<Content> resultContents = new ArrayList<>();
        String fullQuery = queryWithoutLimitAndPlaceholder.trim();
        if (fullQuery.endsWith(";")) {
            fullQuery = fullQuery.substring(0, fullQuery.length() -1);
        }
        fullQuery += " LIMIT ?;";

        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("ContentDao: getListWithLimit - Database connection is not available.");
                return resultContents;
            }
            try (PreparedStatement stmt = connection.prepareStatement(fullQuery)) {
                stmt.setInt(1, idUser);
                stmt.setInt(2, limit);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Content content = createContentFromResultSet(rs);
                    if (content != null) {
                        resultContents.add(content);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ContentDao: error executing query (first 50 chars): " +
                    queryWithoutLimitAndPlaceholder.substring(0, Math.min(50, queryWithoutLimitAndPlaceholder.length())) +
                    "... Error: " + e.getMessage());
        }
        fetchAndSetGenresForContentList(resultContents);
        return resultContents;
    }


public List<Content> historyContent(int userId){
    List<Content> contentList = new ArrayList<>();
    String query = "SELECT Content.* FROM History JOIN Content ON History.ID_Content=Content.ID_Content WHERE History.ID_User=?;";
    try(PreparedStatement stmt = connection.prepareStatement(query)){
        stmt.setInt(1,userId);
        ResultSet rs = stmt.executeQuery();
        while(rs.next()){
            Content content = new Content();
            content.setId(rs.getInt("ID_Content"));
            content.setTitle(rs.getString("Title"));
            content.setPlot(rs.getString("Plot"));
            content.setImageUrl(rs.getString("Image_Url"));
            content.setPosterUrl(rs.getString("Poster_Url"));
            content.setVideoUrl(rs.getString("Film_Url"));
            content.setYear(rs.getInt("Year"));
            content.setRating(rs.getDouble("Rating"));
            content.setPopularity(rs.getInt("Popularity"));
            content.setReleaseDate(rs.getString("Release_Date"));
            content.setIsSeries(rs.getInt("Episodes_Count") > 0);
            contentList.add(content);
        }

    }catch (SQLException e) {
        System.err.println("ContentDao : error select content from history of the user "+e.getMessage());
    }

    return contentList;
}

    public List<Content> watchlistContent(int userId){
        List<Content> contentList = new ArrayList<>();
        String query = "SELECT Content.* FROM Watchlist JOIN Content ON Watchlist.ID_Content=Content.ID_Content WHERE Watchlist.ID_User=?;";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setInt(1,userId);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                Content content = new Content();
                content.setId(rs.getInt("ID_Content"));
                content.setTitle(rs.getString("Title"));
                content.setPlot(rs.getString("Plot"));
                content.setImageUrl(rs.getString("Image_Url"));
                content.setPosterUrl(rs.getString("Poster_Url"));
                content.setVideoUrl(rs.getString("Film_Url"));
                content.setYear(rs.getInt("Year"));
                content.setRating(rs.getDouble("Rating"));
                content.setPopularity(rs.getInt("Popularity"));
                content.setReleaseDate(rs.getString("Release_Date"));
                content.setIsSeries(rs.getInt("Episodes_Count") > 0);
                contentList.add(content);
            }

        }catch (SQLException e) {
            System.err.println("ContentDao : error select content from watchlist of the user "+e.getMessage());
        }
        return contentList;
    }

}
