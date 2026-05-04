package com.gradebook.dao.impl;

import com.gradebook.config.DatabaseConnection;
import com.gradebook.dao.IEnseignantDao;
import com.gradebook.model.Enseignant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnseignantDaoImpl implements IEnseignantDao {
    @Override
    public void create(Enseignant enseignant) {
        String sql = "INSERT INTO enseignant (nom, prenom, email, mot_de_passe) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, enseignant.getNom());
            stmt.setString(2, enseignant.getPrenom());
            stmt.setString(3, enseignant.getEmail());
            stmt.setString(4, enseignant.getMotDePasse());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Enseignant> findById(int id) {
        String sql = "SELECT * FROM enseignant WHERE id_enseignant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Enseignant> findAll() {
        String sql = "SELECT * FROM enseignant";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Enseignant> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void update(Enseignant enseignant) {
        String sql = "UPDATE enseignant SET nom = ?, prenom = ?, email = ?, mot_de_passe = ? " +
                "WHERE id_enseignant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, enseignant.getNom());
            stmt.setString(2, enseignant.getPrenom());
            stmt.setString(3, enseignant.getEmail());
            stmt.setString(4, enseignant.getMotDePasse());
            stmt.setInt(5, enseignant.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM enseignant WHERE id_enseignant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Enseignant> findByEmail(String email) {
        String sql = "SELECT * FROM enseignant WHERE email = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Enseignant> findByClasseAndMatiere(int idClasse, int idMatiere) {
        String sql = "SELECT e.* FROM enseignant e " +
                "JOIN cours c ON e.id_enseignant = c.id_enseignant " +
                "WHERE c.id_classe = ? AND c.id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Enseignant> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idClasse);
            stmt.setInt(2, idMatiere);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSet(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Enseignant mapResultSet(ResultSet rs) throws SQLException {
        return new Enseignant(
                rs.getInt("id_enseignant"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("mot_de_passe")
        );
    }
}
