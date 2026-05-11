package com.gradebook.dao;

import com.gradebook.model.Classe;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Matiere;

import java.util.List;

public interface ICoursDao {
    void addCours(int idEnseignant, int idClasse, int idMatiere, int semestre);

    void removeCours(int idEnseignant, int idClasse, int idMatiere, int semestre);

    List<Matiere> findMatieresByEnseignantAndClasse(int idEnseignant, int idClasse);

    List<Matiere> findMatieresByEnseignantAndClasseAndSemestre(int idEnseignant, int idClasse, int semestre);

    List<Classe> findClassesByEnseignant(int idEnseignant);

    List<Classe> findClassesByEnseignantAndSemestre(int idEnseignant, int semestre);

    List<Enseignant> findEnseignantsByClasseAndMatiere(int idClasse, int idMatiere);

    List<Enseignant> findEnseignantsByClasseAndMatiereAndSemestre(int idClasse, int idMatiere, int semestre);

    List<Enseignant> findEnseignantsByClasse(int idClasse);

    List<Integer> findSemestresByEnseignantAndClasse(int idEnseignant, int idClasse);

    boolean existsCours(int idEnseignant, int idClasse, int idMatiere, int semestre);
}
