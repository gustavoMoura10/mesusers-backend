package com.br.mesusers.shared.interfaces;
import java.util.Optional;

public interface ServiceInt {
    Optional<?> save(Object object);
    Optional<?> update(Object object);
    Optional<?> delete(Long id);
    Optional<?> findById(Long id);
    Optional<?> findAll();
    Optional<?> findAllPagination(Integer page, Integer size);
    Optional<?> findByField(String field, String value);    
}
