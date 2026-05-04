package com.gradebook.dao;

import com.gradebook.model.Evaluation;

import java.util.List;

public interface IEvaluationDao extends IDao<Evaluation> {
    List<Evaluation> findByClasse(int idClasse);

    List<Evaluation> findByMatiere(int idMatiere);

    List<Evaluation> findByClasseAndMatiere(int idClasse, int idMatiere);

    List<Evaluation> findByEnseignant(int idEnseignant);
}
