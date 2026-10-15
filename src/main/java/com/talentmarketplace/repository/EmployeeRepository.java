package com.talentmarketplace.repository;

import com.talentmarketplace.entity.Employee;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    List<Employee> findByEmploymentStatus(Employee.EmploymentStatus employmentStatus);

    List<Employee> findByDepartment(String department);

    List<Employee> findByDepartmentAndEmploymentStatus(String department, Employee.EmploymentStatus employmentStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Employee e WHERE e.id = :id")
    Optional<Employee> findByIdWithPessimisticLock(@Param("id") Long id);

    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.employeeSkills es LEFT JOIN FETCH es.skill WHERE e.employmentStatus = 'ACTIVE'")
    List<Employee> findAllActiveWithSkills();
}
