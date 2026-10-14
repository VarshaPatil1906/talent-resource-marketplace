package com.talentmarketplace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "allocations", indexes = {
    @Index(name = "idx_alloc_employee", columnList = "employee_id"),
    @Index(name = "idx_alloc_demand", columnList = "demand_id"),
    @Index(name = "idx_alloc_status", columnList = "status"),
    @Index(name = "idx_alloc_dates", columnList = "start_date, end_date")
})
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "demand_id", nullable = false)
    private ProjectDemand demand;

    @NotNull(message = "Start date cannot be null")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Allocation percent cannot be null")
    @Min(value = 1, message = "Allocation percent must be greater than 0")
    @Max(value = 100, message = "Allocation percent cannot exceed 100")
    @Column(name = "allocation_percent", nullable = false)
    private Integer allocationPercent;

    @NotNull(message = "Allocation status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AllocationStatus status = AllocationStatus.PENDING;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Allocation() {}

    public Allocation(Employee employee, ProjectDemand demand, LocalDate startDate, LocalDate endDate,
                      Integer allocationPercent, AllocationStatus status, String approvedBy) {
        this.employee = employee;
        this.demand = demand;
        this.startDate = startDate;
        this.endDate = endDate;
        this.allocationPercent = allocationPercent;
        this.status = status;
        this.approvedBy = approvedBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AllocationStatus {
        PENDING,
        APPROVED,
        CANCELLED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public ProjectDemand getDemand() { return demand; }
    public void setDemand(ProjectDemand demand) { this.demand = demand; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getAllocationPercent() { return allocationPercent; }
    public void setAllocationPercent(Integer allocationPercent) { this.allocationPercent = allocationPercent; }

    public AllocationStatus getStatus() { return status; }
    public void setStatus(AllocationStatus status) { this.status = status; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
