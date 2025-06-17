package com.million.data.entity.repository;

import com.million.data.entity.MasterBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterBatchRepository extends JpaRepository<MasterBatch, Long> {
    // Example custom query if needed:
    // Optional<MasterBatch> findTopByOrderByStartTimeDesc();
}
