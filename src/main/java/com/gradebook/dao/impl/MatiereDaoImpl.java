package com.gradebook.dao.impl;

import com.gradebook.config.DatabaseConnection;
import com.gradebook.dao.IMatiereDao;
import com.gradebook.model.Matiere;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatiereDaoImpl implements IMatiereDao {
    @Override
    public void create(Matiere matiere) {
        String sql = "INSERT INTO matiere (intitule, coefficient) VALUES (?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matiere.getIntitule());
            stmt.setDouble(2, matiere.getCoefficient());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Matiere> findById(int id) {
        String sql = "SELECT * FROM matiere WHERE id_matiere = ?";
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
    public List<Matiere> findAll() {
        String sql = "SELECT * FROM matiere";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Matiere> result = new ArrayList<>();
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
    public void update(Matiere matiere) {
        String sql = "UPDATE matiere SET intitule = ?, coefficient = ? WHERE id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matiere.getIntitule());
            stmt.setDouble(2, matiere.getCoefficient());
            stmt.setInt(3, matiere.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM matiere WHERE id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Matiere> findByEnseignant(int idEnseignant) {
        String sql = "SELECT DISTINCT m.* FROM matiere m " +
                "JOIN cours c ON m.id_matiere = c.id_matiere " +
                "WHERE c.id_enseignant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Matiere> result = new ArrayList<>();
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

    @Override
    public List<Matiere> findByClasse(int idClasse) {
        String sql = "SELECT DISTINCT m.* FROM matiere m " +
                "JOIN cours c ON m.id_matiere = c.id_matiere " +
                "WHERE c.id_classe = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Matiere> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idClasse);
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

    private Matiere mapResultSet(ResultSet rs) throws SQLException {
        Matiere matiere = new Matiere();
        matiere.setId(rs.getInt("id_matiere"));
        matiere.setIntitule(rs.getString("intitule"));
        matiere.setCoefficient(rs.getDouble("coefficient"));
        return matiere;
    }
}
