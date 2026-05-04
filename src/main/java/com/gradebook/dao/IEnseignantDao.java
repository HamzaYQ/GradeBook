package com.gradebook.dao;

import com.gradebook.model.Enseignant;

import java.util.List;
import java.util.Optional;

public interface IEnseignantDao extends IDao<Enseignant> {
    Optional<Enseignant> findByEmail(String email);

    List<Enseignant> findByClasseAndMatiere(int idClasse, int idMatiere);
}
