package com.gradebook.dao;

import com.gradebook.model.Administration;

import java.util.Optional;

public interface IAdministrationDao extends IDao<Administration> {
    Optional<Administration> findByEmail(String email);
}
