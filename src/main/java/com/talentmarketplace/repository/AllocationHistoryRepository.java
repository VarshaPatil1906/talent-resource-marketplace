package com.talentmarketplace.repository;

import com.talentmarketplace.entity.AllocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllocationHistoryRepository extends JpaRepository<AllocationHistory, Long> {
    List<AllocationHistory> findByAllocationIdOrderByTimestampDesc(Long allocationId);
    List<AllocationHistory> findAllByOrderByTimestampDesc();
}
