package com.gradebook.dao.impl;

import com.gradebook.config.DatabaseConnection;
import com.gradebook.dao.IEtudiantDao;
import com.gradebook.model.Classe;
import com.gradebook.model.Etudiant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EtudiantDaoImpl implements IEtudiantDao {
    private static final String SELECT_WITH_CLASSE =
            "SELECT e.*, c.nom as classe_nom, c.niveau, c.annee_academique " +
            "FROM etudiant e JOIN classe c ON e.id_classe = c.id_classe ";

    @Override
    public void create(Etudiant etudiant) {
        String sql = "INSERT INTO etudiant (nom, prenom, email, mot_de_passe, matricule, id_classe) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, etudiant.getNom());
            stmt.setString(2, etudiant.getPrenom());
            stmt.setString(3, etudiant.getEmail());
            stmt.setString(4, etudiant.getMotDePasse());
            stmt.setString(5, etudiant.getMatricule());
            stmt.setInt(6, etudiant.getClasse().getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Etudiant> findById(int id) {
        String sql = SELECT_WITH_CLASSE + "WHERE e.id_etudiant = ?";
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
    public List<Etudiant> findAll() {
        String sql = SELECT_WITH_CLASSE;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Etudiant> result = new ArrayList<>();
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
    public void update(Etudiant etudiant) {
        String sql = "UPDATE etudiant SET nom = ?, prenom = ?, email = ?, mot_de_passe = ?, " +
                "matricule = ?, id_classe = ? WHERE id_etudiant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, etudiant.getNom());
            stmt.setString(2, etudiant.getPrenom());
            stmt.setString(3, etudiant.getEmail());
            stmt.setString(4, etudiant.getMotDePasse());
            stmt.setString(5, etudiant.getMatricule());
            stmt.setInt(6, etudiant.getClasse().getId());
            stmt.setInt(7, etudiant.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM etudiant WHERE id_etudiant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Etudiant> findByMatricule(String matricule) {
        String sql = SELECT_WITH_CLASSE + "WHERE e.matricule = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricule);
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
    public List<Etudiant> findByClasse(int idClasse) {
        String sql = SELECT_WITH_CLASSE + "WHERE e.id_classe = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Etudiant> result = new ArrayList<>();
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

    @Override
    public Optional<Etudiant> findByEmail(String email) {
        String sql = SELECT_WITH_CLASSE + "WHERE e.email = ?";
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

    private Etudiant mapResultSet(ResultSet rs) throws SQLException {
        Classe classe = new Classe();
        classe.setId(rs.getInt("id_classe"));
        classe.setNom(rs.getString("classe_nom"));
        classe.setNiveau(rs.getString("niveau"));
        classe.setAnneeAcademique(rs.getString("annee_academique"));

        Etudiant etudiant = new Etudiant();
        etudiant.setId(rs.getInt("id_etudiant"));
        etudiant.setNom(rs.getString("nom"));
        etudiant.setPrenom(rs.getString("prenom"));
        etudiant.setEmail(rs.getString("email"));
        etudiant.setMotDePasse(rs.getString("mot_de_passe"));
        etudiant.setMatricule(rs.getString("matricule"));
        etudiant.setClasse(classe);
        return etudiant;
    }
}
