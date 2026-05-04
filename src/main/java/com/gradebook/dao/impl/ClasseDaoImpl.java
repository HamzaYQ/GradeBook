package com.gradebook.dao.impl;

import com.gradebook.config.DatabaseConnection;
import com.gradebook.dao.IClasseDao;
import com.gradebook.model.Classe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClasseDaoImpl implements IClasseDao {
    @Override
    public void create(Classe classe) {
        String sql = "INSERT INTO classe (nom, niveau, annee_academique) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, classe.getNom());
            stmt.setString(2, classe.getNiveau());
            stmt.setString(3, classe.getAnneeAcademique());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Classe> findById(int id) {
        String sql = "SELECT * FROM classe WHERE id_classe = ?";
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
    public List<Classe> findAll() {
        String sql = "SELECT * FROM classe";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Classe> result = new ArrayList<>();
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
    public void update(Classe classe) {
        String sql = "UPDATE classe SET nom = ?, niveau = ?, annee_academique = ? WHERE id_classe = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, classe.getNom());
            stmt.setString(2, classe.getNiveau());
            stmt.setString(3, classe.getAnneeAcademique());
            stmt.setInt(4, classe.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM classe WHERE id_classe = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Classe> findByAnneeAcademique(String annee) {
        String sql = "SELECT * FROM classe WHERE annee_academique = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Classe> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, annee);
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

    @Override
    public List<Classe> findByEnseignant(int idEnseignant) {
        String sql = "SELECT DISTINCT c.* FROM classe c " +
                "JOIN cours co ON c.id_classe = co.id_classe " +
                "WHERE co.id_enseignant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Classe> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEnseignant);
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

    private Classe mapResultSet(ResultSet rs) throws SQLException {
        Classe classe = new Classe();
        classe.setId(rs.getInt("id_classe"));
        classe.setNom(rs.getString("nom"));
        classe.setNiveau(rs.getString("niveau"));
        classe.setAnneeAcademique(rs.getString("annee_academique"));
        return classe;
    }
}
