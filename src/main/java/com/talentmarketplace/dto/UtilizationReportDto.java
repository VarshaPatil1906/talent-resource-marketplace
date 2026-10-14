package com.talentmarketplace.dto;

import java.util.ArrayList;
import java.util.List;

public class UtilizationReportDto {
    private double overallUtilizationPercent;
    private int totalEmployees;
    private int activeEmployees;
    private int totalCommittedCapacityPercent;
    private int totalAvailableCapacityPercent;
    private int openDemandsCount;
    private int unstaffedDemandsCount;
    private List<EmployeeUtilizationDto> employeeUtilizations = new ArrayList<>();
    private List<DepartmentUtilizationDto> departmentUtilizations = new ArrayList<>();
    private List<SkillGapDto> skillGaps = new ArrayList<>();
    private List<DemandDto> unstaffedDemands = new ArrayList<>();

    public UtilizationReportDto() {}

    public double getOverallUtilizationPercent() { return overallUtilizationPercent; }
    public void setOverallUtilizationPercent(double overallUtilizationPercent) { this.overallUtilizationPercent = overallUtilizationPercent; }

    public int getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(int totalEmployees) { this.totalEmployees = totalEmployees; }

    public int getActiveEmployees() { return activeEmployees; }
    public void setActiveEmployees(int activeEmployees) { this.activeEmployees = activeEmployees; }

    public int getTotalCommittedCapacityPercent() { return totalCommittedCapacityPercent; }
    public void setTotalCommittedCapacityPercent(int totalCommittedCapacityPercent) { this.totalCommittedCapacityPercent = totalCommittedCapacityPercent; }

    public int getTotalAvailableCapacityPercent() { return totalAvailableCapacityPercent; }
    public void setTotalAvailableCapacityPercent(int totalAvailableCapacityPercent) { this.totalAvailableCapacityPercent = totalAvailableCapacityPercent; }

    public int getOpenDemandsCount() { return openDemandsCount; }
    public void setOpenDemandsCount(int openDemandsCount) { this.openDemandsCount = openDemandsCount; }

    public int getUnstaffedDemandsCount() { return unstaffedDemandsCount; }
    public void setUnstaffedDemandsCount(int unstaffedDemandsCount) { this.unstaffedDemandsCount = unstaffedDemandsCount; }

    public List<EmployeeUtilizationDto> getEmployeeUtilizations() { return employeeUtilizations; }
    public void setEmployeeUtilizations(List<EmployeeUtilizationDto> employeeUtilizations) { this.employeeUtilizations = employeeUtilizations; }

    public List<DepartmentUtilizationDto> getDepartmentUtilizations() { return departmentUtilizations; }
    public void setDepartmentUtilizations(List<DepartmentUtilizationDto> departmentUtilizations) { this.departmentUtilizations = departmentUtilizations; }

    public List<SkillGapDto> getSkillGaps() { return skillGaps; }
    public void setSkillGaps(List<SkillGapDto> skillGaps) { this.skillGaps = skillGaps; }

    public List<DemandDto> getUnstaffedDemands() { return unstaffedDemands; }
    public void setUnstaffedDemands(List<DemandDto> unstaffedDemands) { this.unstaffedDemands = unstaffedDemands; }

    public static class EmployeeUtilizationDto {
        private Long employeeId;
        private String employeeCode;
        private String name;
        private String department;
        private String location;
        private int committedPercent;
        private int remainingPercent;
        private int activeAllocationCount;

        public EmployeeUtilizationDto() {}

        public EmployeeUtilizationDto(Long employeeId, String employeeCode, String name, String department, String location, int committedPercent, int remainingPercent, int activeAllocationCount) {
            this.employeeId = employeeId;
            this.employeeCode = employeeCode;
            this.name = name;
            this.department = department;
            this.location = location;
            this.committedPercent = committedPercent;
            this.remainingPercent = remainingPercent;
            this.activeAllocationCount = activeAllocationCount;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public int getCommittedPercent() { return committedPercent; }
        public void setCommittedPercent(int committedPercent) { this.committedPercent = committedPercent; }

        public int getRemainingPercent() { return remainingPercent; }
        public void setRemainingPercent(int remainingPercent) { this.remainingPercent = remainingPercent; }

        public int getActiveAllocationCount() { return activeAllocationCount; }
        public void setActiveAllocationCount(int activeAllocationCount) { this.activeAllocationCount = activeAllocationCount; }
    }

    public static class DepartmentUtilizationDto {
        private String department;
        private int employeeCount;
        private double averageUtilizationPercent;

        public DepartmentUtilizationDto() {}

        public DepartmentUtilizationDto(String department, int employeeCount, double averageUtilizationPercent) {
            this.department = department;
            this.employeeCount = employeeCount;
            this.averageUtilizationPercent = averageUtilizationPercent;
        }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public int getEmployeeCount() { return employeeCount; }
        public void setEmployeeCount(int employeeCount) { this.employeeCount = employeeCount; }

        public double getAverageUtilizationPercent() { return averageUtilizationPercent; }
        public void setAverageUtilizationPercent(double averageUtilizationPercent) { this.averageUtilizationPercent = averageUtilizationPercent; }
    }

    public static class SkillGapDto {
        private String category;
        private String skillName;
        private long requiredCount;
        private long qualifiedEmployeeCount;
        private long gapCount;

        public SkillGapDto() {}

        public SkillGapDto(String category, String skillName, long requiredCount, long qualifiedEmployeeCount, long gapCount) {
            this.category = category;
            this.skillName = skillName;
            this.requiredCount = requiredCount;
            this.qualifiedEmployeeCount = qualifiedEmployeeCount;
            this.gapCount = gapCount;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }

        public long getRequiredCount() { return requiredCount; }
        public void setRequiredCount(long requiredCount) { this.requiredCount = requiredCount; }

        public long getQualifiedEmployeeCount() { return qualifiedEmployeeCount; }
        public void setQualifiedEmployeeCount(long qualifiedEmployeeCount) { this.qualifiedEmployeeCount = qualifiedEmployeeCount; }

        public long getGapCount() { return gapCount; }
        public void setGapCount(long gapCount) { this.gapCount = gapCount; }
    }
}
