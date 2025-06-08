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
//To facilitate understanding we've organized this class in a manner that allows information to be collapsed 
public class ContentDao{
    private final ContentCacheManager cacheManager;
    /// Constructor and Main Methods
    public ContentDao(){
        this.cacheManager = ContentCacheManager.getInstance();
        System.out.println("ContentDao: Initialized."  );
    }

    private void populateContentFromResultSet(Content content, ResultSet rs) throws SQLException{
        content.setTitle(rs.getString("Title"));
        content.setPlot(rs.getString( "Plot"));

        content.setImageUrl(rs.getString("Image_Url") );
        content.setPosterUrl( rs.getString( "Poster_Url"));
        content.setVideoUrl( rs.getString("Film_Url"));

        content.setYear(rs.getInt( "Year"));
        content.setRating(rs.getDouble("Rating"));
        content.setPopularity(rs.getInt("Popularity"));
        content.setReleaseDate(rs.getString("Release_Date"));
        content.setIsSeries(rs.getInt( "Episodes_Count")   > 0);
    }

    private Content createContentFromResultSet(ResultSet rs) throws SQLException{
        int contentId = rs.getInt("ID_Content"  );
        if (contentId == 0){
            System.err.println("ContentDao: createContentFromResultSet - Attempted to create content with ID 0. Skipping."  );
            return null;
        }

        Content content = cacheManager.get(contentId);
        if (content != null){
            populateContentFromResultSet(content, rs);
        } else{
            content = new Content();
            content.setId(contentId);
            populateContentFromResultSet(content, rs);
            cacheManager.put(content);
        }

        if (hasColumn(rs))
            content.setIsSeries(rs.getInt("Episodes_Count")  > 0);

        return content;
    }
    private void fetchAndSetGenresForContentList(List<Content> contents){
        if (contents == null || contents.isEmpty()){
            return;
        }
        List<Content> validContents = contents.stream().filter(Objects::nonNull).toList();
        if (validContents.isEmpty()){
            return;
        }


        String idsString = validContents.stream()
                .map(c -> String.valueOf(c.getId() ))
                .distinct()
                .collect(Collectors.joining(","));

        if (idsString.isEmpty()){
            System.out.println("ContentDao: fetchAndSetGenresForContentList - No content IDs to fetch genres for."  );
            return;
        }

        String genresQuery =  "SELECT ID_Content, ID_Genre FROM Content_Genre WHERE ID_Content IN (" + idsString + ")";
        Map<Integer,  List<Integer>> contentGenresMap =  new HashMap<>();

        try (Connection  conn = DataBaseManager.getConnection() ){
            if (conn == null){
                System.err.println( "ContentDao: fetchAndSetGenresForContentList - Database connection is not available."  );
                return;
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(genresQuery)){
                while ( rs.next()){
                    int contentId = rs.getInt("ID_Content"  );
                    int genreId = rs.getInt("ID_Genre" );
                    contentGenresMap.computeIfAbsent( contentId,  k -> new ArrayList<>()).add(genreId);
                }
            }
        } catch (SQLException e){
            System.err.println("ContentDao: Error fetching genres for multiple contents");
        }

        for (Content content : validContents ){
            List<Integer> genres = contentGenresMap.getOrDefault(content.getId(), Collections.emptyList( ));
            content.setCategories(genres.stream().distinct().collect(Collectors.toList( ))) ;
        }
        System.out.println( "ContentDao: Successfully fetched and set genres for content list."  );
    }
    private boolean hasColumn(ResultSet rs ) throws SQLException{
        ResultSetMetaData metadata = rs.getMetaData();
        int columns = metadata.getColumnCount();
        for (int x = 1; x <= columns; x++)
            if ("Episodes_Count".equals(metadata.getColumnName(x)))
                return true;

        return false;
    }

    
    
    /// INSERT
    public void insertContents(List<Content> contents ){
        if (contents == null ||  contents.isEmpty()){
            return;
        }
        System.out.println( "ContentDao: Starting insertContents for " + contents.size() + " items."  );

        String insertContentSQL = "INSERT OR REPLACE INTO Content " +
                "(ID_Content, Title, Plot, Image_Url, Poster_Url, Film_Url, Year, Rating, Popularity, Release_date, Episodes_Count, Fetched_Date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertGenreSQL = "INSERT OR IGNORE INTO Content_Genre (ID_Content, ID_Genre) VALUES (?, ?)";

        int contentBatchCount  = 0;
        int genreBatchCount  = 0 ;

        try (Connection conn = DataBaseManager.getConnection() ){
            if (conn == null)
                throw new SQLException("Database connection is not available for insertContents." );


            conn.setAutoCommit(false);

            try (PreparedStatement stmtInsertContent = conn.prepareStatement(insertContentSQL);
                 PreparedStatement genreStmt = conn.prepareStatement(insertGenreSQL)){

                for (Content content : contents){
                    if (content.getId() == 0)
                        continue;


                    cacheManager.put(content);

                    stmtInsertContent.setInt( 1, content.getId( ));
                    stmtInsertContent.setString( 2, content.getTitle()) ;
                    stmtInsertContent.setString(3, content.getPlot()) ;
                    stmtInsertContent.setString(4, content.getImageUrl()  != null ? content.getImageUrl() : "error" );
                    stmtInsertContent.setString( 5 , content.getPosterUrl() != null ? content.getPosterUrl() : "error" );
                    stmtInsertContent.setString(6,  content.getVideoUrl() != null ? content.getVideoUrl() : "error" );
                    stmtInsertContent.setInt( 7, content.getYear() );
                    stmtInsertContent.setDouble(8, content.getRating());
                    stmtInsertContent.setInt(9, content.getPopularity());
                    stmtInsertContent.setString(10 , content.getReleaseDate() != null ? content.getReleaseDate() : ""  );
                    stmtInsertContent.setInt( 11, content.isSeries() ? 1 : 0);
                    stmtInsertContent.setString(12, OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    stmtInsertContent.addBatch();
                    contentBatchCount++ ;

                    if (content.getCategories() != null && !content.getCategories().isEmpty()){
                        for (Integer genreId : content.getCategories()){
                            genreStmt.setInt(1, content.getId());
                            genreStmt.setInt(2, genreId);
                            genreStmt.addBatch() ;
                            genreBatchCount++ ;
                        }
                    }
                }

                if (contentBatchCount   > 0)
                    stmtInsertContent.executeBatch();

                if (genreBatchCount  > 0 )
                    genreStmt.executeBatch();

                conn.commit();
                System.out.println("ContentDao: Successfully inserted/replaced  " +  contentBatchCount + " content items and " + genreBatchCount + " genre links." );

            } catch (SQLException  e){
                System.err.println("ContentDao: Error inserting contents. Attempting rollback. Error: " + e.getMessage() );
                try{
                    if (!conn.getAutoCommit()){
                        conn.rollback();
                        System.err.println("ContentDao: Rollback successful."  );
                    }
                } catch (SQLException ex){
                    System.err.println("ContentDao: Error rolling back transaction: " + ex.getMessage() );
                }
                throw new RuntimeException( "ContentDao: Error inserting contents", e);
            }
        } catch (SQLException e){
            System.err.println("ContentDao: Database operation failed during insertContents: " + e.getMessage());
            throw new RuntimeException("ContentDao: Database operation failed during insertContents", e);
        }
    }
    
    /// SELECT
    private List<List<Content>> getContentFromDB(String query, int numberOfLists){
        List<List<Content>> resultList = new Vector<>();
        for (int i = 0; i < numberOfLists; i++){
            resultList.add(new Vector<>());
        }

        try (Connection conn = DataBaseManager.getConnection() ){
            if (conn == null){
                return resultList; // Return empty initialized list
            }
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()){
                int count = 0;
                while (rs.next()){
                    Content content = createContentFromResultSet(rs);
                    if (content != null){
                        int listIndex = rs.getInt("List" );
                        if (listIndex  >= 0 && listIndex < numberOfLists){
                            resultList.get(listIndex).add(content);
                            count++;
                        } else{
                            System.err.println("ContentDao: getContentFromDB - Invalid list index  " + listIndex + " from query for content ID " + content.getId());
                        }
                    }
                }
                System.out.println("ContentDao: getContentFromDB - Successfully fetched " + count + "  content items into respective lists."  );
            }
        } catch (SQLException e){
            System.err.println( "ContentDao: Error in getContentFromDB. Query Error: " + e.getMessage());
        }
        return resultList;
    }
    
    public List<List<Content>> getHomePageContents(User user){
        int userId = user.getID();
        String topGenresString     = buildGenreFilter( getGenreIdsStringFromTaste(userId, "DESC", 3));
        String bottomGenresString  =   buildGenreFilter(getGenreIdsStringFromTaste(userId, "ASC", 3));
        String firstTopGenreString = buildGenreFilter( getGenreIdsStringFromTaste(userId, "DESC", 1));
        String query =
                "SELECT DISTINCT   * , 0 AS List FROM (SELECT C.* FROM Content C  ORDER BY C.Popularity DESC LIMIT 5) UNION ALL " +

                        // Random top genres content
                        "SELECT DISTINCT * , 1 AS List FROM (SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                        topGenresString + " ORDER BY RANDOM() LIMIT 10) UNION ALL " +
                        // New releases
                        "SELECT DISTINCT * , 2 AS List FROM (SELECT C.* FROM Content C ORDER BY Release_Date DESC LIMIT 8) UNION ALL " +
                        // Favorite tag(s) but not watched (uses first_top_genre_str which could be one or all)
                        "SELECT DISTINCT * , 3 AS List FROM (SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                        "LEFT JOIN History CR ON C.ID_Content = CR.ID_Content AND CR.ID_User = " + userId + " " +
                        firstTopGenreString + "AND CR.ID_Content IS NULL AND C.Episodes_Count = 0 ORDER BY RANDOM() LIMIT 8) UNION ALL " +

                        "SELECT DISTINCT * , 4 AS List FROM (SELECT C2.* FROM Content_Genre CG1 JOIN Content_Genre CG2 ON CG1.ID_Genre = CG2.ID_Genre AND CG1.ID_Content != CG2.ID_Content " +
                        "JOIN Content C2 ON CG2.ID_Content = C2.ID_Content WHERE CG1.ID_Content = ( SELECT CR.ID_Content FROM History CR WHERE CR.ID_User = " + userId +
                        " ORDER BY CR.Date DESC LIMIT 1 ) ORDER BY RANDOM() LIMIT 7) UNION ALL " +

                        // Recently watched
                        "SELECT DISTINCT * , 5 AS List FROM (SELECT C.* FROM History CR JOIN Content C ON CR.ID_Content = C.ID_Content " +
                        "WHERE CR.ID_User = " + userId + " ORDER BY CR.Date DESC LIMIT 7) UNION ALL " +

                        // Recommended series
                        "SELECT DISTINCT * , 6 AS List FROM (SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                        topGenresString + " AND C.Episodes_Count  > 0 ORDER BY RANDOM() LIMIT 7) UNION ALL " +
                        // User favorites (Assuming 'Favourite' table)
                        "SELECT DISTINCT *, 7 AS List FROM (SELECT C.* FROM Favourite P JOIN Content C ON P.ID_Content = C.ID_Content " +
                        "WHERE P.ID_User = " + userId + " ORDER BY RANDOM() LIMIT 7) UNION ALL " +
                        // Other categories (bottom genres -  if Taste empty, same as top_genres_str)
                        "SELECT DISTINCT * , 8 AS List FROM (SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                        bottomGenresString + " ORDER BY RANDOM() LIMIT 7);";

        List<List<Content>> results = getContentFromDB(query, 9);
        results.forEach(this::fetchAndSetGenresForContentList );  // fetchAndSetGenresForContentList will use its own connection
        System.out.println( "ContentDao: Successfully completed getHomePageContents for user ID: " + user.getID());
        return results;
    }
    public List<List<Content>> getFilterPageContents(User user, boolean isFilm){
        int userId = user.getID();
        String top_genres_str =  buildGenreFilter(getGenreIdsStringFromTaste(userId, "DESC", 3) );

        String query;
        String typeConditionSpecific = isFilm ? "  AND C.Episodes_Count = 0 " : " AND  C.Episodes_Count  > 0 ";
        String typeConditionGeneral = isFilm  ? "  C.Episodes_Count = 0 "     : "  C.Episodes_Count  > 0 "    ;

        if (isFilm){
            query = "SELECT DISTINCT *, 0 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content  " +
                    top_genres_str + " " + typeConditionSpecific + "ORDER BY Release_Date DESC LIMIT 10 ) AS SubF0 " +
                    "UNION ALL  " +
                    "SELECT DISTINCT *, 1 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content  " +
                    top_genres_str + " " + typeConditionSpecific + "ORDER BY RANDOM() LIMIT 7 ) AS SubF1 " +
                    "UNION ALL " +
                    // Content from top genre(s) not watched
                    "SELECT DISTINCT *, 2 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content  " +
                    "LEFT JOIN History CR ON C.ID_Content = CR.ID_Content AND CR.ID_User = " + userId + " " +
                    top_genres_str + " AND CR.ID_Content IS NULL " + typeConditionSpecific + " ORDER BY RANDOM() LIMIT 8 ) AS SubF2  UNION ALL  " +
                    "SELECT DISTINCT *, 3 AS List FROM ( SELECT C.* FROM Content C WHERE " + typeConditionGeneral + "ORDER BY RANDOM() LIMIT 8 ) AS SubF3 " +
                    "UNION ALL " +
                    "SELECT DISTINCT *, 4 AS List FROM ( SELECT C.* FROM Content C WHERE " + typeConditionGeneral + "ORDER BY RANDOM() LIMIT 8 ) AS SubF4;";
        } else{ // For Series
            query = "SELECT DISTINCT *, 0 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " + top_genres_str + " " +
                    typeConditionSpecific + "ORDER BY Release_Date DESC LIMIT 10 ) AS SubS0 UNION ALL " +
                    "SELECT DISTINCT*, 1 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                    top_genres_str + " " + typeConditionSpecific + "ORDER BY RANDOM() LIMIT 7 ) AS SubS1 UNION ALL " +
                    // Content from top genre(s) not watched
                    "SELECT  DISTINCT *, 2 AS List FROM ( SELECT C.* FROM Content C JOIN Content_Genre CG ON C.ID_Content = CG.ID_Content " +
                    "LEFT JOIN History CR ON C.ID_Content = CR.ID_Content AND CR.ID_User = " + userId + " " + top_genres_str + " " +
                    "AND CR.ID_Content IS NULL " + typeConditionSpecific + "ORDER BY RANDOM() LIMIT 8 ) AS SubS2 UNION ALL " +
                    "SELECT  DISTINCT *, 3 AS List FROM ( SELECT C.* FROM Content C WHERE " + typeConditionGeneral + " ORDER BY RANDOM() LIMIT 8 ) AS SubS3 UNION ALL " +
                    "SELECT DISTINCT *, 4 AS List FROM ( SELECT C.* FROM Content C WHERE " + typeConditionGeneral + " ORDER BY RANDOM() LIMIT 10 ) AS SubS4;";
        }
        List<List<Content>> results = getContentFromDB(query, 5);
        results.forEach(this::fetchAndSetGenresForContentList) ;
        System.out.println("ContentDao: Successfully completed getFilterPageContents for user ID: " + user.getID() + ", isFilm: " + isFilm);
        return results ;
    }
    public String buildGenreFilter(String genreIdsString){
        if (genreIdsString != null && !genreIdsString.isEmpty() && !genreIdsString.equals("-1"))
            return "WHERE CG.ID_Genre IN (" + genreIdsString + ") ";
        return "";
    }
    
    private  String getGenreIdsStringFromTaste( int userId, String orderBy , int limit){
        List<String> genreIds = new ArrayList<>();
        String userTasteSql = " SELECT ID_Genre FROM Taste WHERE ID_User = ? ORDER BY Weight " + orderBy + " LIMIT ?";

        try (Connection conn = DataBaseManager.getConnection()){
            if (conn == null){
                System.err.println(" ContentDao: getGenreIdsStringFromTaste - DB connection not available for user tastes. UserID: " + userId);
            } else{
                try (PreparedStatement stmt = conn.prepareStatement(userTasteSql)){
                    stmt.setInt(1, userId);
                    stmt.setInt(2, limit);
                    try (ResultSet rs = stmt.executeQuery()){
                        while (rs.next()){
                            genreIds.add(String.valueOf(rs.getInt( "ID_Genre") ));
                        }
                    }
                }
            }
        } catch (SQLException e){
            System.err.println( "ContentDao: Error fetching genre IDs from Taste table for user " + userId + ": " + e.getMessage() + ". Will try fetching all genres."  );
        }

        if (genreIds.isEmpty()){
            System.out.println( "ContentDao: No user-specific tastes found for user " + userId + " or error occurred. Fetching all genres as fallback."  );
            String allGenresSql = "SELECT ID_Genre FROM Taste ORDER BY ID_Genre";
            try ( Connection conn = DataBaseManager.getConnection()){
                if (conn == null)
                    return "-1" ;

                try ( PreparedStatement stmt = conn.prepareStatement(allGenresSql)){
                    try (ResultSet rs = stmt.executeQuery() ){
                        while (rs.next())
                            genreIds.add(String.valueOf(rs.getInt("ID_Genre")));
                    }
                }
            } catch (SQLException e){
                return "-1";
            }
            if (genreIds.isEmpty()){
                return "-1";
            }
        }
        return String.join(",", genreIds);
    }
    
    public List<Content> getFavourites(int idUser, int limit ){
        return  getListWithLimit(idUser , limit, "SELECT C.* FROM Content C JOIN Favourite Pr ON C.ID_Content = Pr.ID_Content WHERE Pr.ID_User = ?"  );
    }
    public List<Content> getWatched(int  idUser, int limit ){
        return getListWithLimit(idUser, limit, "SELECT C.* FROM Content C JOIN History Cr ON C.ID_Content  = Cr.ID_Content WHERE Cr.ID_User = ?"  );
    }
    private List<Content> getListWithLimit(int idUser, int limit, String queryWithoutLimitAndPlaceholder ){
        List<Content> resultContents = new ArrayList<>();
        String fullQuery = queryWithoutLimitAndPlaceholder.trim();
        if (fullQuery.endsWith(";")){
            fullQuery = fullQuery.substring(0, fullQuery.length()  - 1);
        }
        fullQuery += " ORDER BY C.Popularity DESC LIMIT ?; " ;

        try (Connection conn = DataBaseManager.getConnection()){

            if (conn == null){
                System.err.println("ContentDao: getListWithLimit - Database connection is not available."  );
                return resultContents;
            }

            try (PreparedStatement stmt = conn.prepareStatement(fullQuery)){
                stmt.setInt(1, idUser);
                stmt.setInt( 2, limit);

                try (ResultSet rs = stmt.executeQuery()){
                    while (rs.next()){
                        Content content = createContentFromResultSet(rs);
                        if (content != null){
                            resultContents.add(content);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ContentDao: Error executing getListWithLimit query: " + e.getMessage());
        }
        fetchAndSetGenresForContentList(resultContents);
        return resultContents;
    }
    
    public List<Content> historyContent(int userId ) {
        List<Content> contentList = new ArrayList<> ();
        String query = "SELECT C.* FROM History H JOIN Content C ON H.ID_Content=C.ID_Content WHERE H.ID_User=? ORDER BY H.Date DESC";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setInt(1, userId );
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    Content content = createContentFromResultSet(rs);
                    if (content != null){
                        contentList.add(content);
                    }
                }
            }
        } catch (SQLException e){
            System.err.println("ContentDao: Error selecting content from history for user " + userId + ": " + e.getMessage());
        }
        fetchAndSetGenresForContentList(contentList);
        return contentList;
    }
    public List<Content> watchlistContent(int userId){
        List<Content> contentList = new ArrayList<>();
        //  Watchlist table has a Date_Added or similar for ordering, if not, order by Title or Popularity
        String query = "SELECT C.* FROM Watchlist W JOIN Content C ON W.ID_Content=C.ID_Content WHERE W.ID_User=? ORDER BY C.Title ASC";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    Content content = createContentFromResultSet(rs);

                    if (content != null)
                        contentList.add(content);
                }
            }
        } catch (SQLException e){
            System.err.println("ContentDao: Error selecting content from watchlist for user " + userId + ": " + e.getMessage());
        }
        fetchAndSetGenresForContentList(contentList);
        return contentList;
    }
    public List<Content> favoriteListContent(int userId){
        List<Content> contentList = new ArrayList<>();
        String query = "SELECT C.* FROM Favourite F JOIN Content C ON F.ID_Content=C.ID_Content WHERE F.ID_User=? ORDER BY C.Title ASC";

        try (Connection conn = DataBaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    Content content = createContentFromResultSet(rs);
                    if (content != null)
                        contentList.add(content);

                }
            }
        } catch (SQLException e){
            System.err.println("ContentDao: Error selecting content from favourite list for user " + userId + ": " + e.getMessage());
        }
        fetchAndSetGenresForContentList(contentList);
        return contentList;
    }
    
    
    /// DELETE
    public void deleteExpiredContent(){
        OffsetDateTime oneDayAgo = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1); // Changed from minusWeeks(1)
        String oneDayAgoStr = oneDayAgo.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        List<Integer> idList = new ArrayList<>();
        String selectExpiredSQL = "SELECT ID_Content FROM Content " +
                "WHERE Fetched_Date < ? " +
                "AND ID_Content NOT IN (SELECT DISTINCT ID_Content FROM History)  " +
                "AND ID_Content NOT IN (SELECT DISTINCT ID_Content FROM WatchList)" +
                "AND ID_Content NOT IN (SELECT DISTINCT ID_Content FROM Favourite)";

        try (Connection connSelect = DataBaseManager.getConnection();
             PreparedStatement selectStmt = connSelect.prepareStatement(selectExpiredSQL)){
            selectStmt.setString(1, oneDayAgoStr);
            try (ResultSet rs = selectStmt.executeQuery()){
                while (rs.next())
                    idList.add(rs.getInt("ID_Content"));
            }
        } catch (SQLException e){
            System.err.println("ContentDao: Error selecting expired content IDs: " + e.getMessage());
            return;
        }

        if (idList.isEmpty()){//No content to delete :)
            return;
        }

        String deleteSQL = "DELETE FROM Content WHERE ID_Content = ?";


        try (Connection  connDelete = DataBaseManager.getConnection()){
            if (connDelete == null){
                System.err.println("ContentDao: deleteExpiredContent - Failed to acquire database connection for deleting content."  );
                return;
            }
            connDelete.setAutoCommit(false);
            try (PreparedStatement stmt = connDelete.prepareStatement(deleteSQL)){
                for (Integer  id : idList){
                    stmt.setInt(1, id);
                    stmt.addBatch();
                }
                int[] batchResults = stmt.executeBatch();
                connDelete.commit();


                idList.forEach(cacheManager::remove );

            } catch (SQLException e){
                System.err.println("ContentDao: Error deleting expired content. Attempting rollback. Error:  " +  e.getMessage());
                try{
                    if ( !connDelete.getAutoCommit()){
                        connDelete.rollback();
                        System.err.println("ContentDao: Rollback successful for delete operation." );
                    }
                } catch (SQLException ex){
                    System.err.println( "ContentDao: Error rolling back delete transaction: "  +  ex.getMessage());
                }
            }
        } catch (SQLException e){
             System.err.println( "ContentDao: deleteExpiredContent - Database connection error during delete phase: "  + e.getMessage());
        }
    }
    
}