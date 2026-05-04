package com.gradebook.dao;

import com.gradebook.model.Matiere;

import java.util.List;

public interface IMatiereDao extends IDao<Matiere> {
    List<Matiere> findByEnseignant(int idEnseignant);

    List<Matiere> findByClasse(int idClasse);
}
