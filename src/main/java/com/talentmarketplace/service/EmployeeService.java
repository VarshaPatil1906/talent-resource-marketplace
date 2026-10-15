package com.talentmarketplace.service;

import com.talentmarketplace.dto.EmployeeDto;
import com.talentmarketplace.dto.EmployeeSkillDto;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.EmployeeSkill;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.EmployeeSkillRepository;
import com.talentmarketplace.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           SkillRepository skillRepository,
                           EmployeeSkillRepository employeeSkillRepository) {
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
        this.employeeSkillRepository = employeeSkillRepository;
    }

    public EmployeeDto createEmployee(EmployeeDto.CreateRequest request) {
        String code = request.getEmployeeCode() != null ? request.getEmployeeCode().trim() : "";
        if (code.isEmpty()) {
            throw new IllegalArgumentException("Employee code cannot be blank");
        }
        if (employeeRepository.findByEmployeeCode(code).isPresent()) {
            throw new IllegalArgumentException("Employee with code " + code + " already exists");
        }

        Employee employee = new Employee(
                code,
                request.getName() != null ? request.getName().trim() : "",
                request.getLocation() != null ? request.getLocation().trim() : "",
                request.getDepartment() != null ? request.getDepartment().trim() : "",
                request.getEmploymentStatus() != null ? request.getEmploymentStatus() : Employee.EmploymentStatus.ACTIVE
        );

        Employee saved = employeeRepository.save(employee);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees(String department, Employee.EmploymentStatus status) {
        List<Employee> employees;
        boolean hasDept = department != null && !department.isBlank() && !department.equalsIgnoreCase("ALL");
        boolean hasStatus = status != null;

        if (hasDept && hasStatus) {
            employees = employeeRepository.findByDepartmentAndEmploymentStatus(department.trim(), status);
        } else if (hasDept) {
            employees = employeeRepository.findByDepartment(department.trim());
        } else if (hasStatus) {
            employees = employeeRepository.findByEmploymentStatus(status);
        } else {
            employees = employeeRepository.findAll();
        }
        return employees.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToDto(employee);
    }

    public EmployeeSkillDto addSkillToEmployee(Long employeeId, EmployeeSkillDto.AddRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        Skill skill;
        if (request.getSkillId() != null) {
            skill = skillRepository.findById(request.getSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + request.getSkillId()));
        } else if (request.getSkillName() != null && !request.getSkillName().isBlank()) {
            String skillName = request.getSkillName().trim();
            skill = skillRepository.findByNameIgnoreCase(skillName)
                    .orElseGet(() -> skillRepository.save(new Skill(skillName, request.getCategory() != null ? request.getCategory() : "GENERAL")));
        } else {
            throw new IllegalArgumentException("Either skillId or skillName must be provided");
        }

        Optional<EmployeeSkill> existingOpt = employeeSkillRepository.findByEmployeeIdAndSkillId(employeeId, skill.getId());
        EmployeeSkill employeeSkill;
        if (existingOpt.isPresent()) {
            employeeSkill = existingOpt.get();
            employeeSkill.setProficiencyLevel(request.getProficiencyLevel());
            employeeSkill.setLastValidatedAt(request.getLastValidatedAt() != null ? request.getLastValidatedAt() : LocalDate.now());
        } else {
            employeeSkill = new EmployeeSkill(
                    employee,
                    skill,
                    request.getProficiencyLevel(),
                    request.getLastValidatedAt() != null ? request.getLastValidatedAt() : (request.getProficiencyLevel() != null ? LocalDate.now() : null)
            );
        }

        EmployeeSkill saved = employeeSkillRepository.save(employeeSkill);
        return mapToSkillDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeSkillDto> getEmployeeSkills(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }
        return employeeSkillRepository.findByEmployeeIdWithSkill(employeeId).stream()
                .map(this::mapToSkillDto)
                .collect(Collectors.toList());
    }

    public EmployeeDto mapToDto(Employee employee) {
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setName(employee.getName());
        dto.setLocation(employee.getLocation());
        dto.setDepartment(employee.getDepartment());
        dto.setEmploymentStatus(employee.getEmploymentStatus());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());

        List<EmployeeSkill> skills = employeeSkillRepository.findByEmployeeIdWithSkill(employee.getId());
        dto.setSkills(skills.stream().map(this::mapToSkillDto).collect(Collectors.toList()));
        return dto;
    }

    public EmployeeSkillDto mapToSkillDto(EmployeeSkill es) {
        return new EmployeeSkillDto(
                es.getId(),
                es.getSkill().getId(),
                es.getSkill().getName(),
                es.getSkill().getCategory(),
                es.getProficiencyLevel(),
                es.getLastValidatedAt()
        );
    }
}
