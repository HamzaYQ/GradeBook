package com.gradebook.dao;

import com.gradebook.model.Periode;
import com.gradebook.model.ReleveDeNotes;

import java.util.List;
import java.util.Optional;

public interface IReleveDeNotesDao extends IDao<ReleveDeNotes> {
    List<ReleveDeNotes> findByEtudiant(int idEtudiant);

    Optional<ReleveDeNotes> findByEtudiantAndPeriode(int idEtudiant, Periode periode);

    List<ReleveDeNotes> findByClasse(int idClasse);
}
