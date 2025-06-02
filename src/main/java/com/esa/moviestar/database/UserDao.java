package com.esa.moviestar.database;

import com.esa.moviestar.model.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private Connection connection;

    public UserDao() {
        try {
            this.connection = DataBaseManager.getConnection();
        } catch (SQLException e) {
            System.err.println("utenteDao : Database connection error "+e.getMessage());
        }
    }

    // Inserimento di un nuovo user
    public void insertUser(User user){
        String query = "INSERT INTO Utente (Nome, Gusti, Email, Icona, DataRegistrazione) VALUES (?, ?, ?, ?, ?);";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getTastes());
            stmt.setString(3, user.getEmail());
            stmt.setInt(4, user.getIDIcona());
            stmt.setString(5, user.getRegistrationDate().toString());
            stmt.executeUpdate();
            System.out.println("UserDao : user name  : "+ user.getName()+"id user : "+ user.getID());
        } catch (SQLException e) {
            System.err.println("utenteDao : User insertion error "+e.getMessage());
        }
    }

    // Rimozione utente tramite codice
    public boolean deleteUser(int idUser){
        String query = "DELETE FROM Utente WHERE ID_Utente = ?;";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUser);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No user found with user ID = " + idUser);
            }
            System.out.println("UserDao : User deleted : "+idUser);
        } catch (SQLException e) {
            System.err.println("utenteDao :Error removing user "+e.getMessage());
        }
        return false;
    }

    public boolean deleteUserbyEmail(String email){
        String query = "DELETE FROM Utente WHERE Email = ?;";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No user found with user email = " + email);
            }
            System.out.println("UserDao : User deleted : "+email);
        } catch (SQLException e) {
            System.err.println("utenteDao :Error removing user "+e.getMessage());
        }
        return false;
    }

    // Ricerca utente tramite codice

    public int countProfilesbyEmail(String email){
        String query = "SELECT COUNT(*) FROM Utente WHERE Email = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }catch(SQLException e){
            System.err.println("utenteDao : User count error "+e.getMessage());        }
        return 0;
    }


    public int findUserCode(String email) {
        String query = "SELECT ID_Utente FROM User WHERE Email = ?;";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1,email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID_Utente");
            }

        }catch(SQLException e){
            System.err.println("utenteDao : User code retrieval error "+e.getMessage());        }
        return -1;
    }

    // Metodo che recupera tutti gli utenti
    public List<User> findAllUsers(String email) {
        List<User> utenti = new ArrayList<>();
        String query = "SELECT * FROM Utente WHERE Email = ?;";  // Recupera tutti gli utenti in base all'email

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String dataRegString = rs.getString("DataRegistrazione");
                LocalDate registrationDate = null;

                if (dataRegString != null && !dataRegString.isEmpty()) {
                    try {
                        registrationDate = LocalDate.parse(dataRegString);

                    } catch (DateTimeParseException e) {
                        System.err.println("Errore parsing data: " + dataRegString + " - " + e.getMessage());
                    }
                }

                User user = new User(
                        rs.getInt("ID_Utente"),
                        rs.getString("Nome"),
                        rs.getString("Gusti"),
                        rs.getInt("Icona"),
                        email,
                        registrationDate
                );
                utenti.add(user);
            }

            System.out.println("UserDao : Numero di utenti recuperati: " + utenti.size());
        } catch (SQLException e) {
            System.err.println("utenteDao : Error fetching user list by user email " + e.getMessage());
        }
        return utenti;
    }


    public boolean updateUser(User user){
        String query = "UPDATE Utente SET Nome = ? , Icona = ? WHERE ID_Utente=?;";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1, user.getName());
            stmt.setInt(2, user.getIDIcona());
            stmt.setInt(3, user.getID());
            stmt.executeUpdate();
            return true;
        }catch (SQLException e){
            System.err.println("utenteDao : errore di aggiornamento dell'user "+e.getMessage());
            return false;
        }
    }
}


