package com.talentmarketplace.dto;

import com.talentmarketplace.entity.Allocation;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AllocationDto {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Long demandId;
    private String projectName;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer allocationPercent;
    private Allocation.AllocationStatus status;
    private String approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AllocationDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getAllocationPercent() { return allocationPercent; }
    public void setAllocationPercent(Integer allocationPercent) { this.allocationPercent = allocationPercent; }

    public Allocation.AllocationStatus getStatus() { return status; }
    public void setStatus(Allocation.AllocationStatus status) { this.status = status; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class Request {
        @NotNull(message = "Employee ID is required")
        private Long employeeId;

        @NotNull(message = "Demand ID is required")
        private Long demandId;

        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        @NotNull(message = "Allocation percent is required")
        @Min(value = 1, message = "Allocation percent must be greater than 0")
        @Max(value = 100, message = "Allocation percent cannot exceed 100")
        private Integer allocationPercent;

        public Request() {}

        public Request(Long employeeId, Long demandId, LocalDate startDate, LocalDate endDate, Integer allocationPercent) {
            this.employeeId = employeeId;
            this.demandId = demandId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.allocationPercent = allocationPercent;
        }

        @AssertTrue(message = "End date cannot precede start date")
        public boolean isDateRangeValid() {
            if (startDate == null || endDate == null) return true;
            return !endDate.isBefore(startDate);
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public Long getDemandId() { return demandId; }
        public void setDemandId(Long demandId) { this.demandId = demandId; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public Integer getAllocationPercent() { return allocationPercent; }
        public void setAllocationPercent(Integer allocationPercent) { this.allocationPercent = allocationPercent; }
    }
}
