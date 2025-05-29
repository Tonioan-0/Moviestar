package com.esa.moviestar.database;

import com.esa.moviestar.model.Utente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtenteDao {
    private Connection connection;

    public UtenteDao() {
        try {
            this.connection = DataBaseManager.getConnection();
        } catch (SQLException e) {
            System.err.println("utenteDao : errore di connessione con il database "+e.getMessage());
        }
    }

    // Inserimento di un nuovo utente
    public void inserisciUtente(Utente utente){
        String query = "INSERT INTO Utente (Nome, Gusti, Email, Icona) VALUES (?, ?, ?, ?);";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, utente.getNome());
            stmt.setString(2, utente.getGusti());
            stmt.setString(3, utente.getEmail());
            stmt.setInt(4, utente.getIDIcona());
            stmt.executeUpdate();
            System.out.println("UtenteDao : utente inserito : "+utente.getNome()+"id utente : "+utente.getID());
        } catch (SQLException e) {
            System.err.println("utenteDao : errore di inserimento dell'utente "+e.getMessage());
        }
    }

    // Rimozione utente tramite codice
    public boolean rimuoviUtente(int idUtente){
        String sql = "DELETE FROM Utente WHERE ID_Utente = ?;";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUtente);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Nessun utente trovato con codice utente = " + idUtente);
            }
            System.out.println("UtenteDao : utente eliminato : "+idUtente);
        } catch (SQLException e) {
            System.err.println("utenteDao : errore di rimozione dell'utente "+e.getMessage());
        }
        return false;
    }

    public boolean rimuoviUtenteEmail(String email){
        String sql = "DELETE FROM Utente WHERE Email = ?;";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Nessun utente trovato con email utente = " + email);
            }
            System.out.println("UtenteDao : utente eliminato : "+email);
        } catch (SQLException e) {
            System.err.println("utenteDao : errore di rimozione dell'utente "+e.getMessage());
        }
        return false;
    }

    // Ricerca utente tramite codice
    public Utente cercaUtente(int idUtente) {
        String query = "SELECT * FROM Utente WHERE ID_Utente = ?;";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idUtente);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Utente(
                        rs.getInt("ID_Utente"),
                        rs.getString("Nome"),
                        rs.getString("Gusti"),
                        rs.getInt("Icona"),
                        rs.getString("Email")
                );
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("utenteDao : errore di ricerca dell'utente "+e.getMessage());        }
        return null;
    }

    public int contaProfiliPerEmail(String email){
        String query = "SELECT COUNT(*) FROM Utente WHERE Email = ?;";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }catch(SQLException e){
            System.err.println("utenteDao : errore di conteggio del numero di utenti "+e.getMessage());        }
        return 0;
    }

    public Utente recuperoUtente(String email,int idUtente)  {
        String query = "SELECT * FROM Utente WHERE Email = ? AND ID_Utente = ?;";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1, email);
            stmt.setInt(2,idUtente);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Utente(
                        rs.getInt("ID_Utente"),
                        rs.getString("Nome"),
                        rs.getString("Gusti"),
                        rs.getInt("Icona"),
                        rs.getString("Email")
                );
            }else {return null;}

        }catch(SQLException e){
            System.err.println("utenteDao : errore nel recupero delle informazioni dell'utente "+e.getMessage());
        }
        return null;
    }

    public int recuperoCodiceUtente(String email) {
        String query = "SELECT ID_Utente FROM Utente WHERE Email = ?;";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1,email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID_Utente");
            }

        }catch(SQLException e){
            System.err.println("utenteDao : errore nel recupero del codice dell'utente "+e.getMessage());        }
        return -1;
    }

    // Metodo che recupera tutti gli utenti
    public List<Utente> recuperaTuttiGliUtenti(String email) {
        List<Utente> utenti = new ArrayList<>();
        String query = "SELECT * FROM Utente WHERE Email = ?;";  // Recupera tutti gli utenti in base all'email

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1,email);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // Crea un oggetto Utente per ogni record nel risultato della query
                Utente utente = new Utente(
                        rs.getInt("ID_Utente"),
                        rs.getString("Nome"),
                        rs.getString("Gusti"),
                        rs.getInt("Icona"),
                        email
                );
                utenti.add(utente);  // Aggiungi l'utente alla lista
            }
            System.out.println("UtenteDao : Numero di utenti recuperati: " + utenti.size());
        } catch (SQLException e) {
            System.err.println("utenteDao : errore di recupero lista utenti in base l'email dell'utente "+e.getMessage());}
        return utenti;  // Restituisci la lista di utenti
    }

    public boolean aggiornaUtente(Utente utente){
        String query = "UPDATE Utente SET Nome = ? , Icona = ? WHERE ID_Utente=?;";
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1,utente.getNome());
            stmt.setInt(2,utente.getIDIcona());
            stmt.setInt(3,utente.getID());
            stmt.executeUpdate();
            return true;
        }catch (SQLException e){
            System.err.println("utenteDao : errore di aggiornamento dell'utente "+e.getMessage());
            return false;
        }
    }
}


