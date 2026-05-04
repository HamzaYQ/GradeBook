package com.gradebook.service;

import com.gradebook.model.Administration;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Etudiant;

import java.util.Optional;

public interface IAuthService {
    Optional<Etudiant> connecterEtudiant(String email, String motDePasse);

    Optional<Enseignant> connecterEnseignant(String email, String motDePasse);

    Optional<Administration> connecterAdministration(String email, String motDePasse);

    String hasherMotDePasse(String motDePasse);
}
