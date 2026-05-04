package com.gradebook.dao;

import com.gradebook.model.Note;

import java.util.List;
import java.util.Optional;

public interface INoteDao {
    void save(Note note);

    void update(Note note);

    Optional<Note> findByEtudiantAndEvaluation(int idEtudiant, int idEvaluation);

    List<Note> findByEvaluation(int idEvaluation);

    List<Note> findByEtudiant(int idEtudiant);

    List<Note> findByEtudiantAndMatiere(int idEtudiant, int idMatiere);

    boolean existsByEtudiantAndEvaluation(int idEtudiant, int idEvaluation);

    void deleteByEtudiantAndEvaluation(int idEtudiant, int idEvaluation);
}
