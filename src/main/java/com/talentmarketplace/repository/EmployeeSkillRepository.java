package com.talentmarketplace.repository;

import com.talentmarketplace.entity.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {
    List<EmployeeSkill> findByEmployeeId(Long employeeId);
    List<EmployeeSkill> findBySkillId(Long skillId);
    Optional<EmployeeSkill> findByEmployeeIdAndSkillId(Long employeeId, Long skillId);

    @Query("SELECT es FROM EmployeeSkill es JOIN FETCH es.skill WHERE es.employee.id = :employeeId")
    List<EmployeeSkill> findByEmployeeIdWithSkill(@Param("employeeId") Long employeeId);
}
