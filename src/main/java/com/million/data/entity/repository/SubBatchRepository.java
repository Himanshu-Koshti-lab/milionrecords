package com.million.data.entity.repository;

import com.million.data.entity.SubBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubBatchRepository extends JpaRepository<SubBatch, Long> {
}
