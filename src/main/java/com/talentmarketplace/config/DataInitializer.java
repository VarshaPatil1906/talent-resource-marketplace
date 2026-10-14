package com.talentmarketplace.config;

import com.talentmarketplace.entity.*;
import com.talentmarketplace.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SkillRepository skillRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final ProjectDemandRepository demandRepository;
    private final DemandSkillRepository demandSkillRepository;
    private final AllocationRepository allocationRepository;
    private final AllocationHistoryRepository allocationHistoryRepository;
    private final TrainingRecommendationRepository trainingRepository;

    public DataInitializer(SkillRepository skillRepository,
                           EmployeeRepository employeeRepository,
                           EmployeeSkillRepository employeeSkillRepository,
                           ProjectDemandRepository demandRepository,
                           DemandSkillRepository demandSkillRepository,
                           AllocationRepository allocationRepository,
                           AllocationHistoryRepository allocationHistoryRepository,
                           TrainingRecommendationRepository trainingRepository) {
        this.skillRepository = skillRepository;
        this.employeeRepository = employeeRepository;
        this.employeeSkillRepository = employeeSkillRepository;
        this.demandRepository = demandRepository;
        this.demandSkillRepository = demandSkillRepository;
        this.allocationRepository = allocationRepository;
        this.allocationHistoryRepository = allocationHistoryRepository;
        this.trainingRepository = trainingRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            log.info("Database already initialized with {} employees. Skipping synthetic seeding.", employeeRepository.count());
            return;
        }

        log.info("Starting safe synthetic data initialization for Talent & Resource Marketplace...");

        // 1. Seed Skills
        Map<String, Skill> skills = new HashMap<>();
        String[][] skillData = {
                {"Java", "BACKEND"},
                {"Spring Boot", "BACKEND"},
                {"MySQL", "DATABASE"},
                {"Python", "DATA_AI"},
                {"Machine Learning", "DATA_AI"},
                {"React", "FRONTEND"},
                {"TypeScript", "FRONTEND"},
                {"AWS", "CLOUD"},
                {"Kubernetes", "DEVOPS"},
                {"Docker", "DEVOPS"},
                {"BigQuery", "DATABASE"},
                {"Terraform", "CLOUD"},
                {"Rust", "SYSTEMS"},
                {"Erlang", "SYSTEMS"}
        };

        for (String[] s : skillData) {
            Skill skill = skillRepository.save(new Skill(s[0], s[1]));
            skills.put(s[0], skill);
        }

        // 2. Seed Employees
        Employee emp1 = employeeRepository.save(new Employee("EMP001", "Alice Johnson", "London", "Engineering", Employee.EmploymentStatus.ACTIVE));
        Employee emp2 = employeeRepository.save(new Employee("EMP002", "Bob Smith", "New York", "Engineering", Employee.EmploymentStatus.ACTIVE));
        Employee emp3 = employeeRepository.save(new Employee("EMP003", "Carlos Mendez", "Bangalore", "Cloud & Platform", Employee.EmploymentStatus.ACTIVE));
        Employee emp4 = employeeRepository.save(new Employee("EMP004", "Divya Nair", "Bangalore", "Data & AI", Employee.EmploymentStatus.ACTIVE));
        Employee emp5 = employeeRepository.save(new Employee("EMP005", "Elena Rostova", "London", "Engineering", Employee.EmploymentStatus.ACTIVE));
        Employee emp6 = employeeRepository.save(new Employee("EMP006", "Frank Miller", "New York", "Engineering", Employee.EmploymentStatus.ACTIVE));
        Employee emp7 = employeeRepository.save(new Employee("EMP007", "Grace Hopper", "San Francisco", "Engineering", Employee.EmploymentStatus.ON_LEAVE));
        Employee emp8 = employeeRepository.save(new Employee("EMP008", "Henry Zhao", "New York", "Data & AI", Employee.EmploymentStatus.ACTIVE));
        Employee emp9 = employeeRepository.save(new Employee("EMP009", "Ian Wright", "London", "Engineering", Employee.EmploymentStatus.INACTIVE));
        Employee emp10 = employeeRepository.save(new Employee("EMP010", "Julia Chen", "San Francisco", "Cloud & Platform", Employee.EmploymentStatus.ACTIVE));

        // 3. Seed Employee Skills (with proficiency and validation evidence, or null where unvalidated)
        // Alice Johnson: Strong Java 5, Spring Boot 4, MySQL 4, React 3
        employeeSkillRepository.save(new EmployeeSkill(emp1, skills.get("Java"), 5, LocalDate.of(2026, 8, 15)));
        employeeSkillRepository.save(new EmployeeSkill(emp1, skills.get("Spring Boot"), 4, LocalDate.of(2026, 8, 15)));
        employeeSkillRepository.save(new EmployeeSkill(emp1, skills.get("MySQL"), 4, LocalDate.of(2026, 7, 20)));
        employeeSkillRepository.save(new EmployeeSkill(emp1, skills.get("React"), 3, LocalDate.of(2026, 5, 10)));

        // Bob Smith: Java 4, Spring Boot 4, AWS 3
        employeeSkillRepository.save(new EmployeeSkill(emp2, skills.get("Java"), 4, LocalDate.of(2026, 9, 1)));
        employeeSkillRepository.save(new EmployeeSkill(emp2, skills.get("Spring Boot"), 4, LocalDate.of(2026, 9, 1)));
        employeeSkillRepository.save(new EmployeeSkill(emp2, skills.get("AWS"), 3, LocalDate.of(2026, 6, 15)));

        // Carlos Mendez: AWS 5, Kubernetes 4, Docker 4, Terraform 3
        employeeSkillRepository.save(new EmployeeSkill(emp3, skills.get("AWS"), 5, LocalDate.of(2026, 8, 20)));
        employeeSkillRepository.save(new EmployeeSkill(emp3, skills.get("Kubernetes"), 4, LocalDate.of(2026, 8, 20)));
        employeeSkillRepository.save(new EmployeeSkill(emp3, skills.get("Docker"), 4, LocalDate.of(2026, 7, 10)));
        employeeSkillRepository.save(new EmployeeSkill(emp3, skills.get("Terraform"), 3, LocalDate.of(2026, 6, 1)));

        // Divya Nair: Python 5, Machine Learning 4, BigQuery 4
        employeeSkillRepository.save(new EmployeeSkill(emp4, skills.get("Python"), 5, LocalDate.of(2026, 9, 10)));
        employeeSkillRepository.save(new EmployeeSkill(emp4, skills.get("Machine Learning"), 4, LocalDate.of(2026, 9, 10)));
        employeeSkillRepository.save(new EmployeeSkill(emp4, skills.get("BigQuery"), 4, LocalDate.of(2026, 8, 5)));

        // Elena Rostova: React 5, TypeScript 4, Docker 3
        employeeSkillRepository.save(new EmployeeSkill(emp5, skills.get("React"), 5, LocalDate.of(2026, 9, 1)));
        employeeSkillRepository.save(new EmployeeSkill(emp5, skills.get("TypeScript"), 4, LocalDate.of(2026, 9, 1)));
        employeeSkillRepository.save(new EmployeeSkill(emp5, skills.get("Docker"), 3, LocalDate.of(2026, 4, 12)));

        // Frank Miller: Java 3, Spring Boot 2 (low proficiency), MySQL 3
        employeeSkillRepository.save(new EmployeeSkill(emp6, skills.get("Java"), 3, LocalDate.of(2026, 7, 1)));
        employeeSkillRepository.save(new EmployeeSkill(emp6, skills.get("Spring Boot"), 2, LocalDate.of(2026, 7, 1)));
        employeeSkillRepository.save(new EmployeeSkill(emp6, skills.get("MySQL"), 3, LocalDate.of(2026, 6, 15)));

        // Grace Hopper: Java 5, AWS 5 (ON_LEAVE status)
        employeeSkillRepository.save(new EmployeeSkill(emp7, skills.get("Java"), 5, LocalDate.of(2026, 5, 1)));
        employeeSkillRepository.save(new EmployeeSkill(emp7, skills.get("AWS"), 5, LocalDate.of(2026, 5, 1)));

        // Henry Zhao: Python (NULL proficiency - unvalidated!), BigQuery 3
        employeeSkillRepository.save(new EmployeeSkill(emp8, skills.get("Python"), null, null));
        employeeSkillRepository.save(new EmployeeSkill(emp8, skills.get("BigQuery"), 3, LocalDate.of(2026, 6, 10)));

        // Julia Chen: AWS 5, Kubernetes 4, Terraform 4
        employeeSkillRepository.save(new EmployeeSkill(emp10, skills.get("AWS"), 5, LocalDate.of(2026, 8, 10)));
        employeeSkillRepository.save(new EmployeeSkill(emp10, skills.get("Kubernetes"), 4, LocalDate.of(2026, 8, 10)));
        employeeSkillRepository.save(new EmployeeSkill(emp10, skills.get("Terraform"), 4, LocalDate.of(2026, 7, 15)));

        // 4. Seed Demands
        // Demand 1: Core Payment Engine Refactoring
        ProjectDemand dem1 = demandRepository.save(new ProjectDemand(
                "Core Payment Engine Refactoring", "Senior Backend Engineer",
                LocalDate.of(2026, 11, 1), LocalDate.of(2027, 2, 28),
                "London", 100, ProjectDemand.Priority.HIGH, ProjectDemand.DemandStatus.OPEN
        ));
        demandSkillRepository.save(new DemandSkill(dem1, skills.get("Java"), true, 4));
        demandSkillRepository.save(new DemandSkill(dem1, skills.get("Spring Boot"), true, 3));
        demandSkillRepository.save(new DemandSkill(dem1, skills.get("MySQL"), false, 3));

        // Demand 2: AI-Powered Fraud Detection
        ProjectDemand dem2 = demandRepository.save(new ProjectDemand(
                "AI-Powered Fraud Detection", "Machine Learning Engineer",
                LocalDate.of(2026, 11, 15), LocalDate.of(2027, 3, 31),
                "Bangalore", 50, ProjectDemand.Priority.CRITICAL, ProjectDemand.DemandStatus.OPEN
        ));
        demandSkillRepository.save(new DemandSkill(dem2, skills.get("Python"), true, 4));
        demandSkillRepository.save(new DemandSkill(dem2, skills.get("Machine Learning"), true, 4));
        demandSkillRepository.save(new DemandSkill(dem2, skills.get("BigQuery"), false, 3));

        // Demand 3: Cloud Infrastructure Modernization
        ProjectDemand dem3 = demandRepository.save(new ProjectDemand(
                "Cloud Infrastructure Modernization", "Cloud Architect",
                LocalDate.of(2026, 12, 1), LocalDate.of(2027, 4, 30),
                "San Francisco", 100, ProjectDemand.Priority.HIGH, ProjectDemand.DemandStatus.OPEN
        ));
        demandSkillRepository.save(new DemandSkill(dem3, skills.get("AWS"), true, 4));
        demandSkillRepository.save(new DemandSkill(dem3, skills.get("Kubernetes"), true, 3));
        demandSkillRepository.save(new DemandSkill(dem3, skills.get("Terraform"), true, 3));

        // Demand 4: Customer Portal Redesign
        ProjectDemand dem4 = demandRepository.save(new ProjectDemand(
                "Customer Portal Redesign", "Senior Frontend Engineer",
                LocalDate.of(2026, 10, 15), LocalDate.of(2027, 1, 15),
                "London", 50, ProjectDemand.Priority.MEDIUM, ProjectDemand.DemandStatus.STAFFED
        ));
        demandSkillRepository.save(new DemandSkill(dem4, skills.get("React"), true, 4));
        demandSkillRepository.save(new DemandSkill(dem4, skills.get("TypeScript"), true, 3));

        // Demand 5: Legacy Monolith Decommissioning (Unstaffed demand - no matching candidate)
        ProjectDemand dem5 = demandRepository.save(new ProjectDemand(
                "Legacy Monolith Decommissioning", "Distributed Systems Specialist",
                LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 31),
                "New York", 100, ProjectDemand.Priority.LOW, ProjectDemand.DemandStatus.OPEN
        ));
        demandSkillRepository.save(new DemandSkill(dem5, skills.get("Rust"), true, 5));
        demandSkillRepository.save(new DemandSkill(dem5, skills.get("Erlang"), true, 5));

        // 5. Seed Allocations & Histories
        // Alloc 1: Elena Rostova on Demand 4 (APPROVED, 50%)
        Allocation alloc1 = allocationRepository.save(new Allocation(
                emp5, dem4, LocalDate.of(2026, 10, 15), LocalDate.of(2027, 1, 15),
                50, Allocation.AllocationStatus.APPROVED, "manager"
        ));
        allocationHistoryRepository.save(new AllocationHistory(alloc1, AllocationHistory.ActionType.CREATED, "recruiter", "Initial staffing proposal for Customer Portal"));
        allocationHistoryRepository.save(new AllocationHistory(alloc1, AllocationHistory.ActionType.APPROVED, "manager", "Manager approved 50% commitment"));

        // Alloc 2: Frank Miller on Demand 1 (APPROVED, 50%)
        Allocation alloc2 = allocationRepository.save(new Allocation(
                emp6, dem1, LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 31),
                50, Allocation.AllocationStatus.APPROVED, "manager"
        ));
        allocationHistoryRepository.save(new AllocationHistory(alloc2, AllocationHistory.ActionType.CREATED, "recruiter", "Initial staffing proposal for Frank Miller (50%)"));
        allocationHistoryRepository.save(new AllocationHistory(alloc2, AllocationHistory.ActionType.APPROVED, "manager", "Approved 50% allocation"));

        // Alloc 3: Alice Johnson on Demand 1 (PENDING, 50%) - Should NOT count against committed capacity!
        Allocation alloc3 = allocationRepository.save(new Allocation(
                emp1, dem1, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31),
                50, Allocation.AllocationStatus.PENDING, null
        ));
        allocationHistoryRepository.save(new AllocationHistory(alloc3, AllocationHistory.ActionType.CREATED, "recruiter", "Pending manager approval for Alice Johnson"));

        // Alloc 4: Carlos Mendez on Demand 3 (CANCELLED, 100%) - Capacity released back to pool!
        Allocation alloc4 = allocationRepository.save(new Allocation(
                emp3, dem3, LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 31),
                100, Allocation.AllocationStatus.CANCELLED, "manager"
        ));
        allocationHistoryRepository.save(new AllocationHistory(alloc4, AllocationHistory.ActionType.CREATED, "recruiter", "Initial proposal"));
        allocationHistoryRepository.save(new AllocationHistory(alloc4, AllocationHistory.ActionType.CANCELLED, "manager", "Cancelled by manager. 100% capacity released."));

        // 6. Seed Training Recommendations
        trainingRepository.save(new TrainingRecommendation(
                emp6, skills.get("Spring Boot"),
                "Spring Boot proficiency level 2 is below required level 3 for Senior Backend roles",
                "Advanced Spring Boot 3 & Microservices Architecture (12 hours)"
        ));
        trainingRepository.save(new TrainingRecommendation(
                emp8, skills.get("Python"),
                "Python proficiency is currently unvalidated and missing formal evidence",
                "Enterprise Python Assessment & Data Engineering Certification"
        ));
        trainingRepository.save(new TrainingRecommendation(
                emp2, skills.get("AWS"),
                "AWS proficiency level 3 can be enhanced to level 4 for Cloud demands",
                "AWS Solutions Architect Professional Learning Pathway"
        ));

        log.info("Synthetic data initialization complete. {} employees, {} skills, {} demands seeded.",
                employeeRepository.count(), skillRepository.count(), demandRepository.count());
    }
}
