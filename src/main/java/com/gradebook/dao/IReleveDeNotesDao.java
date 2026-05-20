package com.gradebook.dao;

import com.gradebook.model.ReleveDeNotes;

import java.util.List;
import java.util.Optional;

public interface IReleveDeNotesDao extends IDao<ReleveDeNotes> {
    List<ReleveDeNotes> findByEtudiant(int idEtudiant);

    Optional<ReleveDeNotes> findByEtudiantAndSemestre(int idEtudiant, int semestre);

    Optional<ReleveDeNotes> findByEtudiantAndSemestreAndSession(int idEtudiant, int semestre, int session);

    List<ReleveDeNotes> findBySemestre(int semestre);

    List<ReleveDeNotes> findByClasse(int idClasse);
}
