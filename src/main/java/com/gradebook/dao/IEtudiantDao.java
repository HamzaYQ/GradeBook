package com.gradebook.dao;

import com.gradebook.model.Etudiant;

import java.util.List;
import java.util.Optional;

public interface IEtudiantDao extends IDao<Etudiant> {
    Optional<Etudiant> findByCne(String cne);

    List<Etudiant> findByClasse(int idClasse);

    Optional<Etudiant> findByEmail(String email);
}
