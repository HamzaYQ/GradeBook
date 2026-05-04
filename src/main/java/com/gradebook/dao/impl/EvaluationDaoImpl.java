package com.gradebook.dao.impl;

import com.gradebook.config.DatabaseConnection;
import com.gradebook.dao.IEvaluationDao;
import com.gradebook.model.Classe;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Evaluation;
import com.gradebook.model.Matiere;
import com.gradebook.model.Session;
import com.gradebook.model.TypeEvaluation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EvaluationDaoImpl implements IEvaluationDao {
    private static final String SELECT_WITH_JOINS =
            "SELECT ev.*, m.intitule as matiere_intitule, m.coefficient as matiere_coefficient, " +
            "c.nom as classe_nom, c.niveau, c.annee_academique, " +
            "e.nom as ens_nom, e.prenom as ens_prenom " +
            "FROM evaluation ev " +
            "JOIN matiere m ON ev.id_matiere = m.id_matiere " +
            "JOIN classe c ON ev.id_classe = c.id_classe " +
            "JOIN enseignant e ON ev.id_enseignant = e.id_enseignant ";

    @Override
    public void create(Evaluation evaluation) {
        String sql = "INSERT INTO evaluation (libelle, type, session, coefficient, date_session, " +
                "id_matiere, id_classe, id_enseignant) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, evaluation.getLibelle());

            if (evaluation.getType() != null) {
                stmt.setString(2, evaluation.getType().name());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            if (evaluation.getSession() != null) {
                stmt.setString(3, evaluation.getSession().name());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            stmt.setDouble(4, evaluation.getCoefficient());

            if (evaluation.getDateSession() != null) {
                stmt.setDate(5, Date.valueOf(evaluation.getDateSession()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            if (evaluation.getMatiere() != null) {
                stmt.setInt(6, evaluation.getMatiere().getId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            if (evaluation.getClasse() != null) {
                stmt.setInt(7, evaluation.getClasse().getId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            if (evaluation.getEnseignant() != null) {
                stmt.setInt(8, evaluation.getEnseignant().getId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Evaluation> findById(int id) {
        String sql = SELECT_WITH_JOINS + "WHERE ev.id_evaluation = ?";
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
    public List<Evaluation> findAll() {
        String sql = SELECT_WITH_JOINS;
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Evaluation> result = new ArrayList<>();
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
    public void update(Evaluation evaluation) {
        String sql = "UPDATE evaluation SET libelle = ?, type = ?, session = ?, coefficient = ?, " +
                "date_session = ?, id_matiere = ?, id_classe = ?, id_enseignant = ? " +
                "WHERE id_evaluation = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, evaluation.getLibelle());

            if (evaluation.getType() != null) {
                stmt.setString(2, evaluation.getType().name());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            if (evaluation.getSession() != null) {
                stmt.setString(3, evaluation.getSession().name());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            stmt.setDouble(4, evaluation.getCoefficient());

            if (evaluation.getDateSession() != null) {
                stmt.setDate(5, Date.valueOf(evaluation.getDateSession()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            if (evaluation.getMatiere() != null) {
                stmt.setInt(6, evaluation.getMatiere().getId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            if (evaluation.getClasse() != null) {
                stmt.setInt(7, evaluation.getClasse().getId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            if (evaluation.getEnseignant() != null) {
                stmt.setInt(8, evaluation.getEnseignant().getId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.setInt(9, evaluation.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM evaluation WHERE id_evaluation = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public List<Evaluation> findByClasse(int idClasse) {
        String sql = SELECT_WITH_JOINS + "WHERE ev.id_classe = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Evaluation> result = new ArrayList<>();
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
    public List<Evaluation> findByMatiere(int idMatiere) {
        String sql = SELECT_WITH_JOINS + "WHERE ev.id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Evaluation> result = new ArrayList<>();
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
    public List<Evaluation> findByClasseAndMatiere(int idClasse, int idMatiere) {
        String sql = SELECT_WITH_JOINS + "WHERE ev.id_classe = ? AND ev.id_matiere = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Evaluation> result = new ArrayList<>();
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

    @Override
    public List<Evaluation> findByEnseignant(int idEnseignant) {
        String sql = SELECT_WITH_JOINS + "WHERE ev.id_enseignant = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Evaluation> result = new ArrayList<>();
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

    private Evaluation mapResultSet(ResultSet rs) throws SQLException {
        Evaluation evaluation = new Evaluation();
        evaluation.setId(rs.getInt("id_evaluation"));
        evaluation.setLibelle(rs.getString("libelle"));

        String typeValue = rs.getString("type");
        if (typeValue != null) {
            evaluation.setType(TypeEvaluation.valueOf(typeValue));
        }

        String sessionValue = rs.getString("session");
        if (sessionValue != null) {
            evaluation.setSession(Session.valueOf(sessionValue));
        }

        evaluation.setCoefficient(rs.getDouble("coefficient"));

        Date dateSession = rs.getDate("date_session");
        if (dateSession != null) {
            evaluation.setDateSession(dateSession.toLocalDate());
        }

        Matiere matiere = new Matiere();
        matiere.setId(rs.getInt("id_matiere"));
        matiere.setIntitule(rs.getString("matiere_intitule"));
        matiere.setCoefficient(rs.getDouble("matiere_coefficient"));

        Classe classe = new Classe();
        classe.setId(rs.getInt("id_classe"));
        classe.setNom(rs.getString("classe_nom"));
        classe.setNiveau(rs.getString("niveau"));
        classe.setAnneeAcademique(rs.getString("annee_academique"));

        Enseignant enseignant = new Enseignant();
        enseignant.setId(rs.getInt("id_enseignant"));
        enseignant.setNom(rs.getString("ens_nom"));
        enseignant.setPrenom(rs.getString("ens_prenom"));

        evaluation.setMatiere(matiere);
        evaluation.setClasse(classe);
        evaluation.setEnseignant(enseignant);

        return evaluation;
    }
}
