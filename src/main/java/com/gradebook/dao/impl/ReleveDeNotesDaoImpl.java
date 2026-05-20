package com.gradebook.dao.impl;

import com.gradebook.config.DatabaseConnection;
import com.gradebook.dao.IReleveDeNotesDao;
import com.gradebook.model.Administration;
import com.gradebook.model.Classe;
import com.gradebook.model.Etudiant;
import com.gradebook.model.ReleveDeNotes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReleveDeNotesDaoImpl implements IReleveDeNotesDao {
    private static final String SELECT_WITH_JOINS =
            "SELECT r.*, e.nom as etudiant_nom, e.prenom as etudiant_prenom, e.cne, " +
            "c.id_classe as classe_id, c.nom as classe_nom, c.niveau as classe_niveau, " +
            "c.annee_academique as classe_annee, " +
            "a.nom as admin_nom, a.prenom as admin_prenom " +
            "FROM releve_de_notes r " +
            "JOIN etudiant e ON r.id_etudiant = e.id_etudiant " +
            "JOIN classe c ON e.id_classe = c.id_classe " +
            "JOIN administration a ON r.id_administration = a.id_administration ";

    @Override
    public void create(ReleveDeNotes releve) {
        String sql = "INSERT INTO releve_de_notes (semestre, session, annee_academique, moyenne_generale, resultat, " +
                "id_etudiant, id_administration) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, releve.getSemestre());
            stmt.setInt(2, releve.getSession());
            stmt.setString(3, releve.getAnneeAcademique());
            stmt.setDouble(4, releve.getMoyenneGenerale());
            stmt.setString(5, releve.getResultat());
            stmt.setInt(6, releve.getEtudiant().getId());
            stmt.setInt(7, releve.getGenereParAdmin().getId());
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
        String sql = "UPDATE releve_de_notes SET semestre = ?, session = ?, annee_academique = ?, " +
                "moyenne_generale = ?, resultat = ? WHERE id_releve = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, releve.getSemestre());
            stmt.setInt(2, releve.getSession());
            stmt.setString(3, releve.getAnneeAcademique());
            stmt.setDouble(4, releve.getMoyenneGenerale());
            stmt.setString(5, releve.getResultat());
            stmt.setInt(6, releve.getId());
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
    public Optional<ReleveDeNotes> findByEtudiantAndSemestre(int idEtudiant, int semestre) {
        String sql = SELECT_WITH_JOINS + "WHERE r.id_etudiant = ? AND r.semestre = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEtudiant);
            stmt.setInt(2, semestre);
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
    public Optional<ReleveDeNotes> findByEtudiantAndSemestreAndSession(int idEtudiant, int semestre, int session) {
        String sql = SELECT_WITH_JOINS + "WHERE r.id_etudiant = ? AND r.semestre = ? AND r.session = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEtudiant);
            stmt.setInt(2, semestre);
            stmt.setInt(3, session);
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

    @Override
    public List<ReleveDeNotes> findBySemestre(int semestre) {
        String sql = SELECT_WITH_JOINS + "WHERE r.semestre = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<ReleveDeNotes> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, semestre);
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

        releve.setSemestre(rs.getInt("semestre"));
        releve.setSession(rs.getInt("session"));

        releve.setAnneeAcademique(rs.getString("annee_academique"));
        releve.setMoyenneGenerale(rs.getDouble("moyenne_generale"));
        releve.setResultat(rs.getString("resultat"));

        Timestamp genereLe = rs.getTimestamp("genere_le");
        if (genereLe != null) {
            releve.setGenereLe(genereLe.toLocalDateTime());
        }

        Classe classe = new Classe();
        classe.setId(rs.getInt("classe_id"));
        classe.setNom(rs.getString("classe_nom"));
        classe.setNiveau(rs.getString("classe_niveau"));
        classe.setAnneeAcademique(rs.getString("classe_annee"));

        Etudiant etudiant = new Etudiant();
        etudiant.setId(rs.getInt("id_etudiant"));
        etudiant.setNom(rs.getString("etudiant_nom"));
        etudiant.setPrenom(rs.getString("etudiant_prenom"));
        etudiant.setCne(rs.getString("cne"));
        etudiant.setClasse(classe);
        releve.setEtudiant(etudiant);

        Administration admin = new Administration();
        admin.setId(rs.getInt("id_administration"));
        admin.setNom(rs.getString("admin_nom"));
        admin.setPrenom(rs.getString("admin_prenom"));
        releve.setGenereParAdmin(admin);

        return releve;
    }
}
