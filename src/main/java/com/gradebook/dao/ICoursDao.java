package com.gradebook.dao;

import com.gradebook.model.Classe;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Matiere;

import java.util.List;

public interface ICoursDao {
    void addCours(int idEnseignant, int idClasse, int idMatiere);

    void removeCours(int idEnseignant, int idClasse, int idMatiere);

    List<Matiere> findMatieresByEnseignantAndClasse(int idEnseignant, int idClasse);

    List<Classe> findClassesByEnseignant(int idEnseignant);

    List<Enseignant> findEnseignantsByClasseAndMatiere(int idClasse, int idMatiere);

    List<Enseignant> findEnseignantsByClasse(int idClasse);

    boolean existsCours(int idEnseignant, int idClasse, int idMatiere);
}
