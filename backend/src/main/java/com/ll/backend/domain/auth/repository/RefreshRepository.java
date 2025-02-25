package com.ll.backend.domain.auth.repository;

import com.ll.backend.domain.auth.entity.RefreshEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshRepository extends CrudRepository<RefreshEntity, String> {
}