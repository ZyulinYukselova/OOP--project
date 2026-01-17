package com.transport.ticketing.repository;

import com.transport.ticketing.model.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCrudRepository<T extends BaseEntity> implements CrudRepository<T> {
    private final Map<String, T> store = new ConcurrentHashMap<>();

    @Override
    public T save(T entity) {
        store.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
