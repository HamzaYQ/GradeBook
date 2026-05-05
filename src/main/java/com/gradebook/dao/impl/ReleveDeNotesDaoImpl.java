package com.gradebook.dao.impl;

import com.gradebook.config.DatabaseConnection;
import com.gradebook.dao.IReleveDeNotesDao;
import com.gradebook.model.Administration;
import com.gradebook.model.Etudiant;
import com.gradebook.model.Periode;
import com.gradebook.model.ReleveDeNotes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReleveDeNotesDaoImpl implements IReleveDeNotesDao {
    private static final String SELECT_WITH_JOINS =
            "SELECT r.*, e.nom as etudiant_nom, e.prenom as etudiant_prenom, e.cne, " +
            "a.nom as admin_nom " +
            "FROM releve_de_notes r " +
            "JOIN etudiant e ON r.id_etudiant = e.id_etudiant " +
            "JOIN administration a ON r.id_administration = a.id_administration ";

    @Override
    public void create(ReleveDeNotes releve) {
        String sql = "INSERT INTO releve_de_notes (periode, annee_academique, moyenne_generale, " +
                "id_etudiant, id_administration) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (releve.getPeriode() != null) {
                stmt.setString(1, releve.getPeriode().name());
            } else {
                stmt.setNull(1, Types.VARCHAR);
            }
            stmt.setString(2, releve.getAnneeAcademique());
            stmt.setDouble(3, releve.getMoyenneGenerale());
            stmt.setInt(4, releve.getEtudiant().getId());
            stmt.setInt(5, releve.getGenereParAdmin().getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<ReleveDeNotes> findById(int id) {
        String sql = SELECT_WITH_JOINS + "WHERE r.id_releve = ?";
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
    public List<ReleveDeNotes> findAll() {
        String sql = SELECT_WITH_JOINS;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<ReleveDeNotes> result = new ArrayList<>();
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
    public void update(ReleveDeNotes releve) {
        String sql = "UPDATE releve_de_notes SET periode = ?, annee_academique = ?, moyenne_generale = ? " +
                "WHERE id_releve = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (releve.getPeriode() != null) {
                stmt.setString(1, releve.getPeriode().name());
            } else {
                stmt.setNull(1, Types.VARCHAR);
            }
            stmt.setString(2, releve.getAnneeAcademique());
            stmt.setDouble(3, releve.getMoyenneGenerale());
            stmt.setInt(4, releve.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM releve_de_notes WHERE id_releve = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<ReleveDeNotes> findByEtudiant(int idEtudiant) {
        String sql = SELECT_WITH_JOINS + "WHERE r.id_etudiant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<ReleveDeNotes> result = new ArrayList<>();
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
    public Optional<ReleveDeNotes> findByEtudiantAndPeriode(int idEtudiant, Periode periode) {
        String sql = SELECT_WITH_JOINS + "WHERE r.id_etudiant = ? AND r.periode = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEtudiant);
            stmt.setString(2, periode != null ? periode.name() : null);
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
    public List<ReleveDeNotes> findByClasse(int idClasse) {
        String sql = SELECT_WITH_JOINS + "WHERE e.id_classe = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<ReleveDeNotes> result = new ArrayList<>();
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

    private ReleveDeNotes mapResultSet(ResultSet rs) throws SQLException {
        ReleveDeNotes releve = new ReleveDeNotes();
        releve.setId(rs.getInt("id_releve"));

        String periodeValue = rs.getString("periode");
        if (periodeValue != null) {
            releve.setPeriode(Periode.valueOf(periodeValue));
        }

        releve.setAnneeAcademique(rs.getString("annee_academique"));
        releve.setMoyenneGenerale(rs.getDouble("moyenne_generale"));

        Timestamp genereLe = rs.getTimestamp("genere_le");
        if (genereLe != null) {
            releve.setGenereLe(genereLe.toLocalDateTime());
        }

        Etudiant etudiant = new Etudiant();
        etudiant.setId(rs.getInt("id_etudiant"));
        etudiant.setNom(rs.getString("etudiant_nom"));
        etudiant.setPrenom(rs.getString("etudiant_prenom"));
        etudiant.setCne(rs.getString("cne"));
        releve.setEtudiant(etudiant);

        Administration admin = new Administration();
        admin.setId(rs.getInt("id_administration"));
        admin.setNom(rs.getString("admin_nom"));
        releve.setGenereParAdmin(admin);

        return releve;
    }
}
