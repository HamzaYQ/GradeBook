package com.gradebook.dao;

import java.util.List;
import java.util.Optional;

public interface IDao<T> {
    void create(T t);

    Optional<T> findById(int id);

    List<T> findAll();

    void update(T t);

    void delete(int id);
}
