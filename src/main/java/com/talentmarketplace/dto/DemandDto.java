package com.talentmarketplace.dto;

import com.talentmarketplace.entity.ProjectDemand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemandDto {
    private Long id;
    private String projectName;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private Integer allocationPercent;
    private ProjectDemand.Priority priority;
    private ProjectDemand.DemandStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DemandSkillDto> requiredSkills = new ArrayList<>();

    public DemandDto() {}

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

    public ProjectDemand.Priority getPriority() { return priority; }
    public void setPriority(ProjectDemand.Priority priority) { this.priority = priority; }

    public ProjectDemand.DemandStatus getStatus() { return status; }
    public void setStatus(ProjectDemand.DemandStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<DemandSkillDto> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<DemandSkillDto> requiredSkills) { this.requiredSkills = requiredSkills; }

    public static class CreateRequest {
        @NotBlank(message = "Project name cannot be blank")
        private String projectName;

        @NotBlank(message = "Role cannot be blank")
        private String role;

        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        @NotBlank(message = "Location cannot be blank")
        private String location;

        @NotNull(message = "Allocation percent is required")
        @Min(value = 1, message = "Allocation percent must be greater than 0")
        @Max(value = 100, message = "Allocation percent cannot exceed 100")
        private Integer allocationPercent;

        @NotNull(message = "Priority is required")
        private ProjectDemand.Priority priority = ProjectDemand.Priority.MEDIUM;

        private ProjectDemand.DemandStatus status = ProjectDemand.DemandStatus.OPEN;

        @Valid
        private List<DemandSkillDto.Request> requiredSkills = new ArrayList<>();

        public CreateRequest() {}

        @AssertTrue(message = "End date cannot precede start date")
        public boolean isDateRangeValid() {
            if (startDate == null || endDate == null) return true;
            return !endDate.isBefore(startDate);
        }

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

        public ProjectDemand.Priority getPriority() { return priority; }
        public void setPriority(ProjectDemand.Priority priority) { this.priority = priority; }

        public ProjectDemand.DemandStatus getStatus() { return status; }
        public void setStatus(ProjectDemand.DemandStatus status) { this.status = status; }

        public List<DemandSkillDto.Request> getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(List<DemandSkillDto.Request> requiredSkills) { this.requiredSkills = requiredSkills; }
    }
}
