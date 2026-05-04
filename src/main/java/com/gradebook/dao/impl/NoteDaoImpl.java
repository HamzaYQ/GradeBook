package com.gradebook.dao.impl;

import com.gradebook.config.DatabaseConnection;
import com.gradebook.dao.INoteDao;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Etudiant;
import com.gradebook.model.Evaluation;
import com.gradebook.model.Note;
import com.gradebook.model.Session;
import com.gradebook.model.TypeEvaluation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NoteDaoImpl implements INoteDao {
    private static final String SELECT_WITH_JOINS =
            "SELECT n.*, et.nom as etudiant_nom, et.prenom as etudiant_prenom, et.matricule, " +
            "ev.libelle as eval_libelle, ev.type, ev.session, ev.coefficient as eval_coef, " +
            "ens.nom as ens_nom, ens.prenom as ens_prenom " +
            "FROM note n " +
            "JOIN etudiant et ON n.id_etudiant = et.id_etudiant " +
            "JOIN evaluation ev ON n.id_evaluation = ev.id_evaluation " +
            "JOIN enseignant ens ON n.id_enseignant = ens.id_enseignant ";

    @Override
    public void save(Note note) {
        String sql = "INSERT INTO note (id_etudiant, id_evaluation, valeur, id_enseignant) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, note.getEtudiant().getId());
            stmt.setInt(2, note.getEvaluation().getId());
            stmt.setDouble(3, note.getValeur());
            stmt.setInt(4, note.getSaisiPar().getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void update(Note note) {
        String sql = "UPDATE note SET valeur = ?, id_enseignant = ?, updated_at = NOW() " +
                "WHERE id_etudiant = ? AND id_evaluation = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, note.getValeur());
            stmt.setInt(2, note.getSaisiPar().getId());
            stmt.setInt(3, note.getEtudiant().getId());
            stmt.setInt(4, note.getEvaluation().getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Note> findByEtudiantAndEvaluation(int idEtudiant, int idEvaluation) {
        String sql = SELECT_WITH_JOINS + "WHERE n.id_etudiant = ? AND n.id_evaluation = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEtudiant);
            stmt.setInt(2, idEvaluation);
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
    public List<Note> findByEvaluation(int idEvaluation) {
        String sql = SELECT_WITH_JOINS + "WHERE n.id_evaluation = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Note> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEvaluation);
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
    public List<Note> findByEtudiant(int idEtudiant) {
        String sql = SELECT_WITH_JOINS + "WHERE n.id_etudiant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Note> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEtudiant);
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
    public List<Note> findByEtudiantAndMatiere(int idEtudiant, int idMatiere) {
        String sql = SELECT_WITH_JOINS + "WHERE n.id_etudiant = ? AND ev.id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Note> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEtudiant);
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

    @Override
    public boolean existsByEtudiantAndEvaluation(int idEtudiant, int idEvaluation) {
        String sql = "SELECT COUNT(*) FROM note WHERE id_etudiant = ? AND id_evaluation = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEtudiant);
            stmt.setInt(2, idEvaluation);
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

    @Override
    public void deleteByEtudiantAndEvaluation(int idEtudiant, int idEvaluation) {
        String sql = "DELETE FROM note WHERE id_etudiant = ? AND id_evaluation = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEtudiant);
            stmt.setInt(2, idEvaluation);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Note mapResultSet(ResultSet rs) throws SQLException {
        Etudiant etudiant = new Etudiant();
        etudiant.setId(rs.getInt("id_etudiant"));
        etudiant.setNom(rs.getString("etudiant_nom"));
        etudiant.setPrenom(rs.getString("etudiant_prenom"));
        etudiant.setMatricule(rs.getString("matricule"));

        Evaluation evaluation = new Evaluation();
        evaluation.setId(rs.getInt("id_evaluation"));
        evaluation.setLibelle(rs.getString("eval_libelle"));
        String typeValue = rs.getString("type");
        if (typeValue != null) {
            evaluation.setType(TypeEvaluation.valueOf(typeValue));
        }
        String sessionValue = rs.getString("session");
        if (sessionValue != null) {
            evaluation.setSession(Session.valueOf(sessionValue));
        }
        evaluation.setCoefficient(rs.getDouble("eval_coef"));

        Enseignant enseignant = new Enseignant();
        enseignant.setId(rs.getInt("id_enseignant"));
        enseignant.setNom(rs.getString("ens_nom"));
        enseignant.setPrenom(rs.getString("ens_prenom"));

        Note note = new Note();
        note.setEtudiant(etudiant);
        note.setEvaluation(evaluation);
        note.setSaisiPar(enseignant);
        note.setValeur(rs.getDouble("valeur"));

        Timestamp dateSaisie = rs.getTimestamp("date_saisie");
        if (dateSaisie != null) {
            note.setDateSaisie(dateSaisie.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            note.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return note;
    }
}
