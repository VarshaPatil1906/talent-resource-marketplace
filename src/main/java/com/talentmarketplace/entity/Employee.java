package com.talentmarketplace.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employee_code", columnList = "employee_code", unique = true),
    @Index(name = "idx_employee_status", columnList = "employment_status"),
    @Index(name = "idx_employee_department", columnList = "department")
})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Employee code cannot be blank")
    @Column(name = "employee_code", nullable = false, unique = true, length = 50)
    private String employeeCode;

    @NotBlank(message = "Employee name cannot be blank")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Location cannot be blank")
    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @NotBlank(message = "Department cannot be blank")
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @NotNull(message = "Employment status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 30)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<EmployeeSkill> employeeSkills = new ArrayList<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Allocation> allocations = new ArrayList<>();

    public Employee() {}

    public Employee(String employeeCode, String name, String location, String department, EmploymentStatus employmentStatus) {
        this.employeeCode = employeeCode;
        this.name = name;
        this.location = location;
        this.department = department;
        this.employmentStatus = employmentStatus;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum EmploymentStatus {
        ACTIVE,
        INACTIVE,
        ON_LEAVE
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public EmploymentStatus getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(EmploymentStatus employmentStatus) { this.employmentStatus = employmentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<EmployeeSkill> getEmployeeSkills() { return employeeSkills; }
    public void setEmployeeSkills(List<EmployeeSkill> employeeSkills) { this.employeeSkills = employeeSkills; }

    public List<Allocation> getAllocations() { return allocations; }
    public void setAllocations(List<Allocation> allocations) { this.allocations = allocations; }
}
