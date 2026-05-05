package com.gradebook.dao;

import com.gradebook.model.Classe;
import com.gradebook.model.Matiere;

import java.util.List;

public interface IClasseMatiereDao {
    void addMatiere(int idClasse, int idMatiere);

    void removeMatiere(int idClasse, int idMatiere);

    List<Matiere> findMatieresByClasse(int idClasse);

    List<Classe> findClassesByMatiere(int idMatiere);

    boolean existsClasseMatiere(int idClasse, int idMatiere);
}
