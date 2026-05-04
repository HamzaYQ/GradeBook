package com.gradebook.service;

import com.gradebook.model.Periode;
import com.gradebook.model.ReleveDeNotes;

import java.io.File;
import java.util.List;

public interface IReleveService {
    ReleveDeNotes genererReleve(int idEtudiant, Periode periode, String anneeAcademique, int idAdmin);

    List<ReleveDeNotes> genererRelevesPourClasse(int idClasse, Periode periode, String anneeAcademique, int idAdmin);

    List<ReleveDeNotes> getRelevesByEtudiant(int idEtudiant);

    File exporterRelevePDF(ReleveDeNotes releve);
}
