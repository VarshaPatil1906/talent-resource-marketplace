package com.talentmarketplace.repository;

import com.talentmarketplace.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    List<Allocation> findByEmployeeId(Long employeeId);

    List<Allocation> findByDemandId(Long demandId);

    List<Allocation> findByStatus(Allocation.AllocationStatus status);

    @Query("SELECT a FROM Allocation a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.status = 'APPROVED' " +
           "AND a.startDate <= :endDate " +
           "AND a.endDate >= :startDate")
    List<Allocation> findOverlappingApprovedAllocations(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM Allocation a " +
           "WHERE a.status = 'APPROVED' " +
           "AND a.startDate <= :endDate " +
           "AND a.endDate >= :startDate")
    List<Allocation> findAllOverlappingApprovedAllocations(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM Allocation a JOIN FETCH a.employee JOIN FETCH a.demand ORDER BY a.createdAt DESC")
    List<Allocation> findAllWithDetails();
}
