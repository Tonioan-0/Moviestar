package com.esa.moviestar.database;

import com.esa.moviestar.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserDao{
    private Connection connection;

    public UserDao(){
        try{
            this.connection = DataBaseManager.getConnection();
        } catch (SQLException e){
            System.err.println("UserDao : Database connection error "+e.getMessage());
        }
    }

    public void insertUser(User user){
        String query = "INSERT INTO User (Name, Email, Icon, RegisterDate) VALUES (?, ?, ?, ?);";

        try (PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getIDIcon());
            stmt.setString(4, user.getRegistrationDate().toString());
            stmt.executeUpdate();
            System.out.println("UserDao : user name  : "+ user.getName()+"id user : "+ user.getID());
        } catch (SQLException e){
            System.err.println("UserDao : User insertion error "+e.getMessage());
        }
    }

    public void deleteUser(int idUser){
        String query = "DELETE FROM User WHERE ID_User = ?;";

        try (PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setInt(1, idUser);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0){
                throw new SQLException("No user found with user ID = " + idUser);
            }
            System.out.println("UserDao : User deleted : "+idUser);
        } catch (SQLException e){
            System.err.println("UserDao :Error removing user "+e.getMessage());
        }
    }

    public void deleteUserByEmail(String email){
        String query = "DELETE FROM User WHERE Email = ?;";

        try (PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1, email);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0){
                throw new SQLException("No user found with user email = " + email);
            }
            System.out.println("UserDao : User deleted : "+email);
        } catch (SQLException e){
            System.err.println("UserDao :Error removing user "+e.getMessage());
        }
    }

    //Counts how many users the account has
    public int countProfilesByEmail(String email){
        String query = "SELECT COUNT(*) FROM User WHERE Email = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                return rs.getInt(1);
            }
        }catch(SQLException e){
            System.err.println("UserDao : User count error "+e.getMessage());
        }
        return 0;
    }


    // Gets all users in which have the same email
    public List<User> findAllUsers(String email){
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM User WHERE Email = ?;";

        try (PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString( 1, email);
            ResultSet rs  = stmt.executeQuery();

            while (rs.next()){
                String dataRegString = rs.getString("RegisterDate");
                LocalDate registrationDate = null;

                if (dataRegString != null && !dataRegString.isEmpty()){
                    try{
                        registrationDate = LocalDate.parse(dataRegString);

                    } catch (DateTimeParseException e){
                        System.err.println("Error parsing data: " + dataRegString + " - " + e.getMessage());
                    }
                }

                User user = new User(
                        rs.getInt("ID_User"),
                        rs.getString("Name"),
                        rs.getInt("Icon"),
                        email,
                        registrationDate
                );
                users.add(user);
            }
        } catch (SQLException e){
            System.err.println("UserDao : Error fetching user list by user email " + e.getMessage());
        }
        return users;
    }


    public boolean updateUser(User user){
        String query = "UPDATE User SET Name = ? , Icon = ? WHERE ID_User=?;";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1, user.getName());
            stmt.setInt(2, user.getIDIcon());
            stmt.setInt(3, user.getID());
            stmt.executeUpdate();
            return true;
        }catch (SQLException e){
            System.err.println("UserDao : update user error "+e.getMessage());
            return false;
        }
    }
    public void insertHistoryContent(int userId , int contentId){
        String query = "INSERT INTO History (ID_User,ID_Content,Date) Values (?,?,?);";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setInt( 1, userId);
            stmt.setInt(2, contentId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }catch (SQLException e){
            System.err.println("userDao : Failed to insert user content into history "+e.getMessage());
        }
    }

    public void insertWatchlistContent(int userId , int contentId){
        String query = "INSERT INTO Watchlist (ID_User,ID_Content,Date) Values (?,?,?);";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setInt(1, userId);
            stmt.setInt(2, contentId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }catch (SQLException e){
            System.err.println("userDao : Failed to insert user content into watchlist "+e.getMessage());
        }
    }
    public void deleteHistory(int idUser,int idContent){
        String query = "DELETE FROM History WHERE ID_User = ? AND ID_Content = ?;";
        try(PreparedStatement stmt = connection.prepareStatement(query) ){
            stmt.setInt(1,idUser);
            stmt.setInt(2,idContent);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0)
                throw new SQLException("deleteHistory : No content with id : "+idContent+" for the user :  "+idUser);
        }
        catch (SQLException e){
            System.err.println("userDao : error deleting history of the user "+e.getMessage() );
        }
    }

    public void deleteWatchlist( int idUser,int idContent ){
        String query = "DELETE FROM WatchList WHERE ID_User = ? AND ID_Content = ?;";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setInt( 1,idUser );
            stmt.setInt(2,idContent );
            int rowsAffected =  stmt.executeUpdate();
            if (rowsAffected == 0){
                throw new SQLException("deleteWatchlist : No content with id : "+idContent+" for the user :  "+idUser);
            }        }catch (SQLException e){
            System.err.println("userDao : error deleting watchlist of the user "+e.getMessage());
        }
    }

    public void insertFavouriteContent(int userId , int contentId){
        String query = "INSERT INTO Favourite (ID_User,ID_Content) Values (?,?);";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setInt( 1, userId);
            stmt.setInt(2, contentId);
            stmt.executeUpdate();
        }catch (SQLException e){
            System.err.println("userDao : Failed to insert user content into favorites "+e.getMessage());
        }
    }
    public void deleteFavourite(int idUser, int idContent){
        String query = "DELETE FROM Favourite WHERE ID_User = ? AND ID_Content = ?;";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setInt(1,idUser);
            stmt.setInt(2,idContent);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0){
                throw new SQLException("deleteFavourites : No content with id : "+idContent+" for the user :  "+idUser);
            }        }catch (SQLException e){
            System.err.println("userDao : error deleting favorite of the user "+e.getMessage());
        }
    }

    public List<Boolean> checkIfIsInFavouritesOrWatchList(int userID, int contentID){
        boolean isInFavourites = false;
        boolean isInWatchlist = false;
        String favouriteQuery = "SELECT 1 FROM Favourite WHERE ID_User = ? AND ID_Content = ? LIMIT 1;";
        String watchlistQuery = "SELECT 1 FROM Watchlist WHERE ID_User = ? AND ID_Content = ? LIMIT 1;";
        try{
            try (PreparedStatement favStmt = connection.prepareStatement(favouriteQuery)) {
                favStmt.setInt( 1, userID);
                favStmt.setInt(2, contentID);
                try (ResultSet rs = favStmt.executeQuery()) {
                    if (rs.next())
                        isInFavourites = true;
                }
            }
            try (PreparedStatement watchStmt = connection.prepareStatement(watchlistQuery)){
                watchStmt.setInt(1, userID);
                watchStmt.setInt(2, contentID);
                try (ResultSet rs  = watchStmt.executeQuery()) {
                    if (rs.next())
                        isInWatchlist = true;
                }
            }
        } catch (SQLException e){
            System.err.println("UserDao : Error checking favourites or watchlist: " + e.getMessage());
        }

        return Arrays.asList(isInFavourites, isInWatchlist);
    }
}


