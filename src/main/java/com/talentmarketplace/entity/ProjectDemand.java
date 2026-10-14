package com.talentmarketplace.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_demands", indexes = {
    @Index(name = "idx_demand_status", columnList = "status"),
    @Index(name = "idx_demand_priority", columnList = "priority"),
    @Index(name = "idx_demand_role", columnList = "role"),
    @Index(name = "idx_demand_dates", columnList = "start_date, end_date")
})
public class ProjectDemand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name cannot be blank")
    @Column(name = "project_name", nullable = false, length = 150)
    private String projectName;

    @NotBlank(message = "Role cannot be blank")
    @Column(name = "role", nullable = false, length = 100)
    private String role;

    @NotNull(message = "Start date cannot be null")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotBlank(message = "Location cannot be blank")
    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @NotNull(message = "Allocation percent cannot be null")
    @Min(value = 1, message = "Allocation percent must be greater than 0")
    @Max(value = 100, message = "Allocation percent cannot exceed 100")
    @Column(name = "allocation_percent", nullable = false)
    private Integer allocationPercent;

    @NotNull(message = "Priority cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DemandStatus status = DemandStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "demand", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DemandSkill> demandSkills = new ArrayList<>();

    @OneToMany(mappedBy = "demand", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Allocation> allocations = new ArrayList<>();

    public ProjectDemand() {}

    public ProjectDemand(String projectName, String role, LocalDate startDate, LocalDate endDate,
                         String location, Integer allocationPercent, Priority priority, DemandStatus status) {
        this.projectName = projectName;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.allocationPercent = allocationPercent;
        this.priority = priority;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum DemandStatus {
        OPEN,
        STAFFED,
        CANCELLED
    }

    public void addDemandSkill(DemandSkill demandSkill) {
        demandSkills.add(demandSkill);
        demandSkill.setDemand(this);
    }

    public void removeDemandSkill(DemandSkill demandSkill) {
        demandSkills.remove(demandSkill);
        demandSkill.setDemand(null);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getAllocationPercent() { return allocationPercent; }
    public void setAllocationPercent(Integer allocationPercent) { this.allocationPercent = allocationPercent; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public DemandStatus getStatus() { return status; }
    public void setStatus(DemandStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<DemandSkill> getDemandSkills() { return demandSkills; }
    public void setDemandSkills(List<DemandSkill> demandSkills) { this.demandSkills = demandSkills; }

    public List<Allocation> getAllocations() { return allocations; }
    public void setAllocations(List<Allocation> allocations) { this.allocations = allocations; }
}
