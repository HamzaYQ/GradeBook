package com.gradebook.service;

import com.gradebook.model.ReleveDeNotes;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface IReleveService {
    ReleveDeNotes genererReleve(int idEtudiant, int semestre, String anneeAcademique, int idAdmin);

    List<ReleveDeNotes> genererRelevesPourClasse(int idClasse, int semestre, String anneeAcademique, int idAdmin);

    List<ReleveDeNotes> getRelevesByEtudiant(int idEtudiant);

    Optional<ReleveDeNotes> getReleveByEtudiantAndSemestre(int idEtudiant, int semestre);

    File exporterRelevePDF(ReleveDeNotes releve);
}
