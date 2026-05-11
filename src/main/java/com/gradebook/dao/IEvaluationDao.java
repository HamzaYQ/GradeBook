package com.gradebook.dao;

import com.gradebook.model.Evaluation;

import java.util.List;

public interface IEvaluationDao extends IDao<Evaluation> {
    List<Evaluation> findByClasse(int idClasse);

    List<Evaluation> findByMatiere(int idMatiere);

    List<Evaluation> findByClasseAndMatiere(int idClasse, int idMatiere);

    List<Evaluation> findByClasseAndMatiereBySemestre(int idClasse, int idMatiere, int semestre);

    List<Evaluation> findByEnseignant(int idEnseignant);

    List<Evaluation> findByEnseignantAndSemestre(int idEnseignant, int semestre);

    List<Evaluation> findBySemestre(int semestre);
}
