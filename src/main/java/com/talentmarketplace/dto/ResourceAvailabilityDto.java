package com.talentmarketplace.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResourceAvailabilityDto {
    private Long employeeId;
    private String employeeCode;
    private String name;
    private String location;
    private String department;
    private LocalDate requestedStartDate;
    private LocalDate requestedEndDate;
    private int maxConcurrentCommittedPercent; // Peak committed percent across any date in range
    private int minAvailableCapacityPercent;   // Minimum available capacity across any date in range (100 - peak)
    private boolean fullyAvailable;            // True only if minAvailableCapacityPercent >= requested allocation percent
    private boolean partiallyAvailable;        // True if available capacity > 0 on at least one day or has reduced capacity
    private int requestedPercent;
    private List<CapacitySegmentDto> capacitySegments = new ArrayList<>();
    private List<ConflictingAllocationDto> conflictingAllocations = new ArrayList<>();

    public ResourceAvailabilityDto() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDate getRequestedStartDate() { return requestedStartDate; }
    public void setRequestedStartDate(LocalDate requestedStartDate) { this.requestedStartDate = requestedStartDate; }

    public LocalDate getRequestedEndDate() { return requestedEndDate; }
    public void setRequestedEndDate(LocalDate requestedEndDate) { this.requestedEndDate = requestedEndDate; }

    public int getMaxConcurrentCommittedPercent() { return maxConcurrentCommittedPercent; }
    public void setMaxConcurrentCommittedPercent(int maxConcurrentCommittedPercent) { this.maxConcurrentCommittedPercent = maxConcurrentCommittedPercent; }

    public int getMinAvailableCapacityPercent() { return minAvailableCapacityPercent; }
    public void setMinAvailableCapacityPercent(int minAvailableCapacityPercent) { this.minAvailableCapacityPercent = minAvailableCapacityPercent; }

    public boolean isFullyAvailable() { return fullyAvailable; }
    public void setFullyAvailable(boolean fullyAvailable) { this.fullyAvailable = fullyAvailable; }

    public boolean isPartiallyAvailable() { return partiallyAvailable; }
    public void setPartiallyAvailable(boolean partiallyAvailable) { this.partiallyAvailable = partiallyAvailable; }

    public int getRequestedPercent() { return requestedPercent; }
    public void setRequestedPercent(int requestedPercent) { this.requestedPercent = requestedPercent; }

    public List<CapacitySegmentDto> getCapacitySegments() { return capacitySegments; }
    public void setCapacitySegments(List<CapacitySegmentDto> capacitySegments) { this.capacitySegments = capacitySegments; }

    public List<ConflictingAllocationDto> getConflictingAllocations() { return conflictingAllocations; }
    public void setConflictingAllocations(List<ConflictingAllocationDto> conflictingAllocations) { this.conflictingAllocations = conflictingAllocations; }

    public static class CapacitySegmentDto {
        private LocalDate startDate;
        private LocalDate endDate;
        private int committedPercent;
        private int availablePercent;

        public CapacitySegmentDto() {}

        public CapacitySegmentDto(LocalDate startDate, LocalDate endDate, int committedPercent, int availablePercent) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.committedPercent = committedPercent;
            this.availablePercent = availablePercent;
        }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public int getCommittedPercent() { return committedPercent; }
        public void setCommittedPercent(int committedPercent) { this.committedPercent = committedPercent; }

        public int getAvailablePercent() { return availablePercent; }
        public void setAvailablePercent(int availablePercent) { this.availablePercent = availablePercent; }
    }

    public static class ConflictingAllocationDto {
        private Long allocationId;
        private String projectName;
        private LocalDate startDate;
        private LocalDate endDate;
        private int allocationPercent;
        private String status;

        public ConflictingAllocationDto() {}

        public ConflictingAllocationDto(Long allocationId, String projectName, LocalDate startDate, LocalDate endDate, int allocationPercent, String status) {
            this.allocationId = allocationId;
            this.projectName = projectName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.allocationPercent = allocationPercent;
            this.status = status;
        }

        public Long getAllocationId() { return allocationId; }
        public void setAllocationId(Long allocationId) { this.allocationId = allocationId; }

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public int getAllocationPercent() { return allocationPercent; }
        public void setAllocationPercent(int allocationPercent) { this.allocationPercent = allocationPercent; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
