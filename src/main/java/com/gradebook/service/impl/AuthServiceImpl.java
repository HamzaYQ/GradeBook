package com.gradebook.service.impl;

import com.gradebook.dao.IAdministrationDao;
import com.gradebook.dao.IEnseignantDao;
import com.gradebook.dao.IEtudiantDao;
import com.gradebook.model.Administration;
import com.gradebook.model.Enseignant;
import com.gradebook.model.Etudiant;
import com.gradebook.service.IAuthService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class AuthServiceImpl implements IAuthService {
    private static final String HASH_ALGORITHM = "SHA-256";

    private final IEtudiantDao etudiantDao;
    private final IEnseignantDao enseignantDao;
    private final IAdministrationDao administrationDao;

    public AuthServiceImpl(IEtudiantDao etudiantDao, IEnseignantDao enseignantDao, IAdministrationDao administrationDao) {
        this.etudiantDao = etudiantDao;
        this.enseignantDao = enseignantDao;
        this.administrationDao = administrationDao;
    }

    @Override
    public String hasherMotDePasse(String motDePasse) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(motDePasse.getBytes(StandardCharsets.UTF_8));
            return toHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Etudiant> connecterEtudiant(String email, String motDePasse) {
        Optional<Etudiant> etudiant = etudiantDao.findByEmail(email);
        if (etudiant.isPresent() && matches(motDePasse, etudiant.get().getMotDePasse())) {
            return etudiant;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Enseignant> connecterEnseignant(String email, String motDePasse) {
        Optional<Enseignant> enseignant = enseignantDao.findByEmail(email);
        if (enseignant.isPresent() && matches(motDePasse, enseignant.get().getMotDePasse())) {
            return enseignant;
        }
        return Optional.empty();
    }

    @Override
    public Optional<Administration> connecterAdministration(String email, String motDePasse) {
        Optional<Administration> administration = administrationDao.findByEmail(email);
        if (administration.isPresent() && matches(motDePasse, administration.get().getMotDePasse())) {
            return administration;
        }
        return Optional.empty();
    }

    private boolean matches(String motDePasse, String hash) {
        if (motDePasse == null || hash == null) {
            return false;
        }
        return hasherMotDePasse(motDePasse).equals(hash);
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            int v = value & 0xFF;
            sb.append(Character.forDigit(v >>> 4, 16));
            sb.append(Character.forDigit(v & 0x0F, 16));
        }
        return sb.toString();
    }
}
