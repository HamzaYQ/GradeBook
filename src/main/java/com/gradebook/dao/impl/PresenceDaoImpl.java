package com.gradebook.dao.impl;

import com.gradebook.config.DatabaseConnection;
import com.gradebook.dao.IPresenceDao;
import com.gradebook.model.DonneesPresence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PresenceDaoImpl implements IPresenceDao {
    private static final String SELECT_WITH_JOINS =
            "SELECT p.*, e.cne, m.intitule as matiere_intitule " +
            "FROM presence p " +
            "JOIN etudiant e ON p.id_etudiant = e.id_etudiant " +
            "JOIN matiere m ON p.id_matiere = m.id_matiere ";

    @Override
    public void save(DonneesPresence presence) {
        String sql = "INSERT INTO presence (id_etudiant, id_matiere, date_absence, statut, source_import) " +
            "VALUES ((SELECT id_etudiant FROM etudiant WHERE cne = ?), " +
                "(SELECT id_matiere FROM matiere WHERE intitule = ?), ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE statut = VALUES(statut)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, presence.getCneEtudiant());
            stmt.setString(2, presence.getMatiere());
            if (presence.getDateAbsence() != null) {
                stmt.setDate(3, Date.valueOf(presence.getDateAbsence()));
            } else {
                stmt.setDate(3, null);
            }
            stmt.setString(4, presence.getStatut());
            stmt.setString(5, presence.getSourceImport());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void saveAll(List<DonneesPresence> presences) {
        if (presences == null || presences.isEmpty()) {
            return;
        }
        Connection conn = DatabaseConnection.getInstance().getConnection();
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            for (DonneesPresence presence : presences) {
                save(presence);
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
            throw new RuntimeException(e.getMessage(), e);
        } catch (RuntimeException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(previousAutoCommit);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    public List<DonneesPresence> findByEtudiant(int idEtudiant) {
        String sql = SELECT_WITH_JOINS + "WHERE p.id_etudiant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<DonneesPresence> result = new ArrayList<>();
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
    public List<DonneesPresence> findByEtudiantAndMatiere(int idEtudiant, int idMatiere) {
        String sql = SELECT_WITH_JOINS + "WHERE p.id_etudiant = ? AND p.id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<DonneesPresence> result = new ArrayList<>();
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
    public List<DonneesPresence> findByMatiere(int idMatiere) {
        String sql = SELECT_WITH_JOINS + "WHERE p.id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<DonneesPresence> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idMatiere);
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
    public int countAbsencesByEtudiantAndMatiere(int idEtudiant, int idMatiere) {
        String sql = "SELECT COUNT(*) FROM presence WHERE id_etudiant = ? AND id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEtudiant);
            stmt.setInt(2, idMatiere);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteBySourceImport(String sourceImport) {
        String sql = "DELETE FROM presence WHERE source_import = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sourceImport);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private DonneesPresence mapResultSet(ResultSet rs) throws SQLException {
        DonneesPresence presence = new DonneesPresence();
        presence.setCneEtudiant(rs.getString("cne"));

        Date dateAbsence = rs.getDate("date_absence");
        if (dateAbsence != null) {
            presence.setDateAbsence(dateAbsence.toLocalDate());
        }

        presence.setStatut(rs.getString("statut"));
        presence.setMatiere(rs.getString("matiere_intitule"));
        presence.setSourceImport(rs.getString("source_import"));
        return presence;
    }
}
