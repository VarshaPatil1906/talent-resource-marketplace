package com.talentmarketplace.controller;

import com.talentmarketplace.dto.EmployeeDto;
import com.talentmarketplace.dto.EmployeeSkillDto;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto.CreateRequest request) {
        EmployeeDto created = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAllEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Employee.EmploymentStatus status) {
        List<EmployeeDto> employees = employeeService.getAllEmployees(department, status);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        EmployeeDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @PostMapping("/{id}/skills")
    public ResponseEntity<EmployeeSkillDto> addSkillToEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeSkillDto.AddRequest request) {
        EmployeeSkillDto added = employeeService.addSkillToEmployee(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<List<EmployeeSkillDto>> getEmployeeSkills(@PathVariable Long id) {
        List<EmployeeSkillDto> skills = employeeService.getEmployeeSkills(id);
        return ResponseEntity.ok(skills);
    }
}
