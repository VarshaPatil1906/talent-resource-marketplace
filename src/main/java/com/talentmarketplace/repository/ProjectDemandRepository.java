package com.talentmarketplace.repository;

import com.talentmarketplace.entity.ProjectDemand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectDemandRepository extends JpaRepository<ProjectDemand, Long> {

    List<ProjectDemand> findByStatus(ProjectDemand.DemandStatus status);

    List<ProjectDemand> findByPriority(ProjectDemand.Priority priority);

    List<ProjectDemand> findByRoleContainingIgnoreCase(String role);

    @Query("SELECT DISTINCT d FROM ProjectDemand d LEFT JOIN FETCH d.demandSkills ds LEFT JOIN FETCH ds.skill WHERE d.id = :id")
    Optional<ProjectDemand> findByIdWithSkills(@Param("id") Long id);

    @Query("SELECT DISTINCT d FROM ProjectDemand d LEFT JOIN FETCH d.demandSkills ds LEFT JOIN FETCH ds.skill ORDER BY d.createdAt DESC")
    List<ProjectDemand> findAllWithSkills();

    @Query("SELECT d FROM ProjectDemand d WHERE " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:priority IS NULL OR d.priority = :priority) AND " +
           "(:role IS NULL OR LOWER(d.role) LIKE LOWER(CONCAT('%', :role, '%'))) AND " +
           "(:location IS NULL OR LOWER(d.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:startDate IS NULL OR d.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR d.endDate <= :endDate)")
    List<ProjectDemand> findWithFilters(
            @Param("status") ProjectDemand.DemandStatus status,
            @Param("priority") ProjectDemand.Priority priority,
            @Param("role") String role,
            @Param("location") String location,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
