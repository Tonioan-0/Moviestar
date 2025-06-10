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
            connection = DataBaseManager.getConnection();
        }
        catch (SQLException e  ) {
            System.err.println("AccountDao : Database connection error "+e.getMessage());
        }
    }


    public boolean insertAccount(Account account) {
        String query = "INSERT INTO Account (Email,Password) Values (?,?);";

        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1, account.getEmail());
            stmt.setString(2, account.getPassword());
            stmt.execute();
            System.out.println("AccountDao : Account added : "+account.getEmail());
            return true;
        }catch (SQLException e) {
            System.err.println("accountDao :Account insertion error"+e.getMessage());
            return false;
        }
    }

    public void deleteAccount(String email){
        String query = "DELETE FROM Account WHERE Email = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1, email);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0)
                System.err.println("No account found with email =" + email);

            System.out.println("AccountDao : account deleted : "+email);
        } catch (SQLException e){
            System.err.println("accountDao: Error removing account – " + e.getMessage());

        }
    }


    public  Account searchAccount(String email){
        String query = "SELECT * FROM Account WHERE Email = ?;";

        try (PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1, email);
            ResultSet rs =  stmt.executeQuery();
            if (rs.next()){
                return new Account(
                        rs.getString("Email"),
                        rs.getString("Password")
                );
            }
            else
                return null;

        } catch (SQLException e ){
            System.err.println("accountDao : Account search error"+e.getMessage());
        }
        return null;
    }

    public void updatePassword(String email, String password ){
        String query = "UPDATE Account SET Password = ? WHERE Email = ?;" ;

        try (PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1,  password);
            stmt.setString(2, email);
            stmt.executeUpdate();
            System.out.println("AccountDao :account with updated password : "+email);
        } catch (SQLException e){
            System.err.println("accountDao : Account password update error"+e.getMessage());

        }
    }

    public boolean checkPassword(String email , String password){
        String query = "SELECT Password FROM Account WHERE Email = ?;" ;

        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1,email);
            try (ResultSet rs = stmt.executeQuery()){
                if (rs.next()){
                    String storedPassword = rs.getString("Password" );
                    return storedPassword.equals(password);
                }
            }
            System.out.println("AccountDao : account with verified password: " + email );
        }catch (SQLException e ){
            System.err.println("accountDao : Account password verification error"+e.getMessage() );
        }
        return false;
    }


}
