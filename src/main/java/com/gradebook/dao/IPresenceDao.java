package com.gradebook.dao;

import com.gradebook.model.DonneesPresence;

import java.util.List;

public interface IPresenceDao {
    void save(DonneesPresence presence);

    void saveAll(List<DonneesPresence> presences);

    List<DonneesPresence> findByEtudiant(int idEtudiant);

    List<DonneesPresence> findByEtudiantAndMatiere(int idEtudiant, int idMatiere);

    List<DonneesPresence> findByMatiere(int idMatiere);

    int countAbsencesByEtudiantAndMatiere(int idEtudiant, int idMatiere);

    void deleteBySourceImport(String sourceImport);
}
