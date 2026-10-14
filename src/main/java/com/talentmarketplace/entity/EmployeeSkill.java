package com.talentmarketplace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

@Entity
@Table(name = "employee_skills",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_skill", columnNames = {"employee_id", "skill_id"})
    },
    indexes = {
        @Index(name = "idx_emp_skill_employee", columnList = "employee_id"),
        @Index(name = "idx_emp_skill_skill", columnList = "skill_id"),
        @Index(name = "idx_emp_skill_proficiency", columnList = "proficiency_level")
    }
)
public class EmployeeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Min(value = 1, message = "Proficiency level must be between 1 and 5")
    @Max(value = 5, message = "Proficiency level must be between 1 and 5")
    @Column(name = "proficiency_level", nullable = true)
    private Integer proficiencyLevel; // Null represents unknown/unvalidated; never defaults to an invented level

    @Column(name = "last_validated_at", nullable = true)
    private LocalDate lastValidatedAt;

    public EmployeeSkill() {}

    public EmployeeSkill(Employee employee, Skill skill, Integer proficiencyLevel, LocalDate lastValidatedAt) {
        this.employee = employee;
        this.skill = skill;
        this.proficiencyLevel = proficiencyLevel;
        this.lastValidatedAt = lastValidatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public Integer getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(Integer proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public LocalDate getLastValidatedAt() { return lastValidatedAt; }
    public void setLastValidatedAt(LocalDate lastValidatedAt) { this.lastValidatedAt = lastValidatedAt; }
}
