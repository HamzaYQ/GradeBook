package com.gradebook.dao.impl;

import com.gradebook.config.DatabaseConnection;
import com.gradebook.dao.IClasseMatiereDao;
import com.gradebook.model.Classe;
import com.gradebook.model.Matiere;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClasseMatiereDaoImpl implements IClasseMatiereDao {
    @Override
    public void addMatiere(int idClasse, int idMatiere) {
        String sql = "INSERT IGNORE INTO classe_matiere (id_classe, id_matiere) VALUES (?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idClasse);
            stmt.setInt(2, idMatiere);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void removeMatiere(int idClasse, int idMatiere) {
        String sql = "DELETE FROM classe_matiere WHERE id_classe = ? AND id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idClasse);
            stmt.setInt(2, idMatiere);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Matiere> findMatieresByClasse(int idClasse) {
        String sql = "SELECT m.* FROM matiere m " +
                "JOIN classe_matiere cm ON m.id_matiere = cm.id_matiere " +
                "WHERE cm.id_classe = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Matiere> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idClasse);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapMatiere(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Classe> findClassesByMatiere(int idMatiere) {
        String sql = "SELECT c.* FROM classe c " +
                "JOIN classe_matiere cm ON c.id_classe = cm.id_classe " +
                "WHERE cm.id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Classe> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMatiere);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapClasse(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean existsClasseMatiere(int idClasse, int idMatiere) {
        String sql = "SELECT COUNT(*) FROM classe_matiere WHERE id_classe = ? AND id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idClasse);
            stmt.setInt(2, idMatiere);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Matiere mapMatiere(ResultSet rs) throws SQLException {
        Matiere matiere = new Matiere();
        matiere.setId(rs.getInt("id_matiere"));
        matiere.setIntitule(rs.getString("intitule"));
        matiere.setCoefficient(rs.getDouble("coefficient"));
        return matiere;
    }

    private Classe mapClasse(ResultSet rs) throws SQLException {
        Classe classe = new Classe();
        classe.setId(rs.getInt("id_classe"));
        classe.setNom(rs.getString("nom"));
        classe.setNiveau(rs.getString("niveau"));
        classe.setAnneeAcademique(rs.getString("annee_academique"));
        return classe;
    }
}
