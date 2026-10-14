package com.talentmarketplace.ai;

import com.talentmarketplace.dto.*;
import com.talentmarketplace.entity.Employee;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.entity.TrainingRecommendation;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.SkillRepository;
import com.talentmarketplace.repository.TrainingRecommendationRepository;
import com.talentmarketplace.service.AllocationService;
import com.talentmarketplace.service.DemandService;
import com.talentmarketplace.service.StaffingEngineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlled backend tools service for the Staffing AI Agent.
 * Exposes strictly validated, backend-enforced tools.
 */
@Service
public class StaffingToolsService {

    private final ProjectDemandRepository demandRepository;
    private final DemandService demandService;
    private final StaffingEngineService staffingEngineService;
    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final TrainingRecommendationRepository trainingRepository;
    private final AllocationService allocationService;

    public StaffingToolsService(ProjectDemandRepository demandRepository,
                                DemandService demandService,
                                StaffingEngineService staffingEngineService,
                                EmployeeRepository employeeRepository,
                                SkillRepository skillRepository,
                                TrainingRecommendationRepository trainingRepository,
                                AllocationService allocationService) {
        this.demandRepository = demandRepository;
        this.demandService = demandService;
        this.staffingEngineService = staffingEngineService;
        this.employeeRepository = employeeRepository;
        this.skillRepository = skillRepository;
        this.trainingRepository = trainingRepository;
        this.allocationService = allocationService;
    }

    /**
     * Tool 1: Retrieve Project Demand
     */
    @Transactional(readOnly = true)
    public DemandDto getDemand(Long demandId) {
        if (demandId == null) {
            throw new IllegalArgumentException("Demand ID cannot be null");
        }
        return demandService.getDemandById(demandId);
    }

    /**
     * Tool 2: Retrieve Backend-Generated Candidates and Score Breakdowns
     */
    @Transactional(readOnly = true)
    public List<CandidateRecommendationDto> getCandidateRecommendations(Long demandId) {
        if (demandId == null) {
            throw new IllegalArgumentException("Demand ID cannot be null");
        }
        return staffingEngineService.getRecommendationsForDemand(demandId);
    }

    /**
     * Tool 3: Retrieve Employee Skill Evidence (Audit validation dates & assessment records)
     */
    @Transactional(readOnly = true)
    public List<EmployeeSkillDto> getEmployeeSkillEvidence(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        if (employee.getEmployeeSkills() == null) {
            return new ArrayList<>();
        }

        return employee.getEmployeeSkills().stream().map(es -> new EmployeeSkillDto(
                es.getId(),
                es.getSkill().getId(),
                es.getSkill().getName(),
                es.getSkill().getCategory(),
                es.getProficiencyLevel(), // Never invented
                es.getLastValidatedAt()
        )).collect(Collectors.toList());
    }

    /**
     * Tool 4: Check Availability for specified dates and capacity percentage
     */
    @Transactional(readOnly = true)
    public ResourceAvailabilityDto checkAvailability(Long employeeId, LocalDate startDate, LocalDate endDate, int requestedPercent) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID cannot be null");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        return staffingEngineService.calculateAvailability(employeeId, startDate, endDate, requestedPercent);
    }

    /**
     * Tool 5: Generate Training Recommendations for missing skills
     */
    @Transactional
    public List<TrainingRecommendationDto> generateTrainingRecommendations(Long employeeId, Long demandId) {
        if (employeeId == null || demandId == null) {
            throw new IllegalArgumentException("Employee ID and Demand ID cannot be null");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
        ProjectDemand demand = demandRepository.findByIdWithSkills(demandId)
                .orElseThrow(() -> new ResourceNotFoundException("Demand not found with id: " + demandId));

        CandidateRecommendationDto eval = staffingEngineService.evaluateCandidateForDemand(employee, demand);
        List<TrainingRecommendationDto> generated = new ArrayList<>();

        for (CandidateRecommendationDto.MissingSkillDto missing : eval.getMissingSkills()) {
            Skill skill = skillRepository.findByNameIgnoreCase(missing.getSkillName()).orElse(null);
            if (skill == null) continue;

            String reason = missing.getReason() != null ? missing.getReason() : "Required for demand: " + demand.getProjectName();
            String path = "Structured Learning Path: " + missing.getSkillName() + " Professional Training & Assessment (Target: Level " + missing.getRequiredProficiency() + ")";

            TrainingRecommendation tr = new TrainingRecommendation(employee, skill, reason, path);
            TrainingRecommendation saved = trainingRepository.save(tr);

            generated.add(new TrainingRecommendationDto(
                    saved.getId(),
                    employee.getId(),
                    employee.getName(),
                    skill.getId(),
                    skill.getName(),
                    saved.getReason(),
                    saved.getLearningPath(),
                    saved.getStatus(),
                    saved.getCreatedAt()
            ));
        }

        return generated;
    }

    /**
     * Tool 6: Submit an Allocation Request into the approval workflow (creates PENDING state).
     * The AI agent NEVER approves or commits an allocation directly.
     */
    @Transactional
    public AllocationDto submitAllocationRequest(AllocationDto.Request request) {
        return allocationService.createAllocationRequest(request, "AI_Staffing_Assistant");
    }
}
