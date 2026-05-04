package com.gradebook.dao;

import com.gradebook.model.Classe;

import java.util.List;

public interface IClasseDao extends IDao<Classe> {
    List<Classe> findByAnneeAcademique(String annee);

    List<Classe> findByEnseignant(int idEnseignant);
}
