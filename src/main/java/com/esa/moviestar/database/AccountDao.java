package com.esa.moviestar.database;

import com.esa.moviestar.model.Account;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDao {

    private static Connection connection;


    public AccountDao(){
        try{
            this.connection = DataBaseManager.getConnection();
        }
        catch (SQLException e) {
            System.err.println("accountDao : errore di connessione con il database "+e.getMessage());
        }
    }




    //Metodo per inserire un Account
    public boolean inserisciAccount(Account account) {
        String sql = "INSERT INTO Account (Email,Password) Values (?,?);";

        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, account.getEmail());
            stmt.setString(2, account.getPassword());
            stmt.execute();
            System.out.println("AccountDao : account aggiunto : "+account.getEmail());
            return true;
        }catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                // Email già esistente
                return false;
            } else {
                System.err.println("accountDao : errore di inserimento dell'account"+e.getMessage());
                return false;
            }
        }
    }

    //Metodo per eliminare l'account
    public boolean rimuoviAccount(String email) {
        String query = "DELETE FROM Account WHERE Email = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                System.err.println("Nessun account trovato con email = " + email);

            }
            System.out.println("AccountDao : account rimosso : "+email);
        } catch (SQLException e) {
            System.err.println("accountDao: errore di rimozione dell'account - " + e.getMessage());

        }
        return false;
    }


    //Metodo per cercare l'account dall'email
    public  Account cercaAccount(String email) {
        String query = "SELECT * FROM Account WHERE Email = ?;";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Account(
                        rs.getString("Email"),
                        rs.getString("Password")
                );
            }
            else {
                return null;
            }

        } catch (SQLException e) {
            System.err.println("accountDao : errore di ricerca dell'account"+e.getMessage());
        }
        return null;
    }
    public boolean updatePassword(String email, String password) {
        String query = "UPDATE Account SET Password = ? WHERE Email = ?;";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, password);
            stmt.setString(2, email);
            int righeModificate = stmt.executeUpdate();
            System.out.println("AccountDao : account a cui è stata aggiornata la password : "+email);
            return righeModificate > 0;  // ritorna true se almeno una riga è stata modificata
        } catch (SQLException e) {
            System.err.println("accountDao : errore di aggiornamento della password dell'account"+e.getMessage());

        }
        return false;
    }

    public boolean checkPassword(String email , String password){
        String query = "SELECT Password FROM Account WHERE Email = ?;";

        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1,email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("Password");
                    return storedPassword.equals(password);
                }
            }
            System.out.println("AccountDao : account a cui è stata verificata la password : "+email);
        }catch (SQLException e){
            System.err.println("accountDao : errore di verifica della password dell'account"+e.getMessage());
        }
        return false;
    }


}
