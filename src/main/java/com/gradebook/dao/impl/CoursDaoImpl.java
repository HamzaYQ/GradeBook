package com.gradebook.dao.impl;

import com.gradebook.config.DatabaseConnection;
import com.gradebook.dao.ICoursDao;
import com.gradebook.model.Classe;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Matiere;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CoursDaoImpl implements ICoursDao {
    @Override
    public void addCours(int idEnseignant, int idClasse, int idMatiere) {
        String sql = "INSERT IGNORE INTO cours (id_enseignant, id_classe, id_matiere) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEnseignant);
            stmt.setInt(2, idClasse);
            stmt.setInt(3, idMatiere);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void removeCours(int idEnseignant, int idClasse, int idMatiere) {
        String sql = "DELETE FROM cours WHERE id_enseignant = ? AND id_classe = ? AND id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEnseignant);
            stmt.setInt(2, idClasse);
            stmt.setInt(3, idMatiere);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Matiere> findMatieresByEnseignantAndClasse(int idEnseignant, int idClasse) {
        String sql = "SELECT m.* FROM matiere m " +
                "JOIN cours c ON m.id_matiere = c.id_matiere " +
                "WHERE c.id_enseignant = ? AND c.id_classe = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Matiere> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEnseignant);
            stmt.setInt(2, idClasse);
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
    public List<Classe> findClassesByEnseignant(int idEnseignant) {
        String sql = "SELECT DISTINCT cl.* FROM classe cl " +
                "JOIN cours c ON cl.id_classe = c.id_classe " +
                "WHERE c.id_enseignant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Classe> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEnseignant);
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
    public List<Enseignant> findEnseignantsByClasseAndMatiere(int idClasse, int idMatiere) {
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
                    result.add(mapEnseignant(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Enseignant> findEnseignantsByClasse(int idClasse) {
        String sql = "SELECT DISTINCT e.* FROM enseignant e " +
                "JOIN cours c ON e.id_enseignant = c.id_enseignant " +
                "WHERE c.id_classe = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Enseignant> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idClasse);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapEnseignant(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean existsCours(int idEnseignant, int idClasse, int idMatiere) {
        String sql = "SELECT COUNT(*) FROM cours WHERE id_enseignant = ? AND id_classe = ? AND id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEnseignant);
            stmt.setInt(2, idClasse);
            stmt.setInt(3, idMatiere);
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

    private Matiere mapResultSet(ResultSet rs) throws SQLException {
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

    private Enseignant mapEnseignant(ResultSet rs) throws SQLException {
        return new Enseignant(
                rs.getInt("id_enseignant"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("email"),
                rs.getString("mot_de_passe")
        );
    }
}
