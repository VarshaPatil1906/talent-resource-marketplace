package com.talentmarketplace.dto;

import com.talentmarketplace.entity.Employee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDto {
    private Long id;
    private String employeeCode;
    private String name;
    private String location;
    private String department;
    private Employee.EmploymentStatus employmentStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<EmployeeSkillDto> skills = new ArrayList<>();

    public EmployeeDto() {}

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

    public Employee.EmploymentStatus getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(Employee.EmploymentStatus employmentStatus) { this.employmentStatus = employmentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<EmployeeSkillDto> getSkills() { return skills; }
    public void setSkills(List<EmployeeSkillDto> skills) { this.skills = skills; }

    public static class CreateRequest {
        @NotBlank(message = "Employee code is required")
        private String employeeCode;

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Location is required")
        private String location;

        @NotBlank(message = "Department is required")
        private String department;

        @NotNull(message = "Employment status is required")
        private Employee.EmploymentStatus employmentStatus = Employee.EmploymentStatus.ACTIVE;

        public CreateRequest() {}

        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public Employee.EmploymentStatus getEmploymentStatus() { return employmentStatus; }
        public void setEmploymentStatus(Employee.EmploymentStatus employmentStatus) { this.employmentStatus = employmentStatus; }
    }
}
