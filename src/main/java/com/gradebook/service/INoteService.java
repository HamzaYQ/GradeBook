package com.gradebook.service;

import com.gradebook.model.Note;

import java.util.List;

public interface INoteService {
    void saisirNote(int idEtudiant, int idEvaluation, int idEnseignant, double valeur);

    void modifierNote(int idEtudiant, int idEvaluation, int idEnseignant, double nouvelleValeur);

    List<Note> getNotesByEvaluation(int idEvaluation);

    List<Note> getNotesByEtudiant(int idEtudiant);

    List<Note> getNotesByEtudiantAndMatiere(int idEtudiant, int idMatiere);

    List<Note> getNotesByEtudiantAndSemestre(int idEtudiant, int semestre);

    List<Note> getNotesByEtudiantAndMatiereAndSemestre(int idEtudiant, int idMatiere, int semestre);

    boolean noteExiste(int idEtudiant, int idEvaluation);
}
