package com.talentmarketplace.service;

import com.talentmarketplace.dto.CandidateRecommendationDto;
import com.talentmarketplace.dto.ResourceAvailabilityDto;
import com.talentmarketplace.dto.ScoreBreakdownDto;
import com.talentmarketplace.entity.*;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.AllocationRepository;
import com.talentmarketplace.repository.EmployeeRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StaffingEngineService {

    private final EmployeeRepository employeeRepository;
    private final ProjectDemandRepository demandRepository;
    private final AllocationRepository allocationRepository;

    public StaffingEngineService(EmployeeRepository employeeRepository,
                                 ProjectDemandRepository demandRepository,
                                 AllocationRepository allocationRepository) {
        this.employeeRepository = employeeRepository;
        this.demandRepository = demandRepository;
        this.allocationRepository = allocationRepository;
    }

    /**
     * Evaluates all employees against a demand, calculating mandatory eligibility,
     * inclusive-date capacity, and explainable 0-100 score breakdowns.
     */
    public List<CandidateRecommendationDto> getRecommendationsForDemand(Long demandId) {
        ProjectDemand demand = demandRepository.findByIdWithSkills(demandId)
                .orElseThrow(() -> new ResourceNotFoundException("Demand not found with id: " + demandId));

        List<Employee> allEmployees = employeeRepository.findAll();
        List<CandidateRecommendationDto> recommendations = new ArrayList<>();

        for (Employee employee : allEmployees) {
            CandidateRecommendationDto candidate = evaluateCandidateForDemand(employee, demand);
            recommendations.add(candidate);
        }

        // Deterministic ranking:
        // 1. Eligible candidates first
        // 2. Highest total score first
        // 3. Lowest employee ID for stable tie-breaking
        recommendations.sort(Comparator
                .comparing(CandidateRecommendationDto::isEligible).reversed()
                .thenComparing(CandidateRecommendationDto::getOverallScore, Comparator.reverseOrder())
                .thenComparing(CandidateRecommendationDto::getEmployeeId));

        return recommendations;
    }

    /**
     * Detailed evaluation of a single candidate against a demand.
     */
    public CandidateRecommendationDto evaluateCandidateForDemand(Employee employee, ProjectDemand demand) {
        CandidateRecommendationDto candidate = new CandidateRecommendationDto();
        candidate.setEmployeeId(employee.getId());
        candidate.setEmployeeCode(employee.getEmployeeCode());
        candidate.setName(employee.getName());
        candidate.setLocation(employee.getLocation());
        candidate.setDepartment(employee.getDepartment());

        List<String> exclusionReasons = new ArrayList<>();
        List<CandidateRecommendationDto.MatchedSkillDto> matchedSkills = new ArrayList<>();
        List<CandidateRecommendationDto.MissingSkillDto> missingSkills = new ArrayList<>();

        // 1. Employment Status Eligibility
        if (employee.getEmploymentStatus() != Employee.EmploymentStatus.ACTIVE) {
            exclusionReasons.add("Employment status is " + employee.getEmploymentStatus() + " (must be ACTIVE)");
        }

        // 2. Skill & Proficiency Evaluation
        Map<Long, EmployeeSkill> empSkillsBySkillId = new HashMap<>();
        if (employee.getEmployeeSkills() != null) {
            for (EmployeeSkill es : employee.getEmployeeSkills()) {
                empSkillsBySkillId.put(es.getSkill().getId(), es);
            }
        }

        List<DemandSkill> demandSkills = demand.getDemandSkills() != null ? demand.getDemandSkills() : Collections.emptyList();
        int mandatoryCount = 0;
        int mandatoryMatchedCount = 0;
        int optionalCount = 0;
        int optionalMatchedCount = 0;
        double totalProficiencyBonus = 0.0;
        int evaluableSkillCount = 0;

        for (DemandSkill ds : demandSkills) {
            Skill skill = ds.getSkill();
            boolean isMandatory = ds.isMandatory();
            int requiredProf = ds.getMinProficiency();

            if (isMandatory) {
                mandatoryCount++;
            } else {
                optionalCount++;
            }

            EmployeeSkill empSkill = empSkillsBySkillId.get(skill.getId());
            if (empSkill == null) {
                // Completely missing skill
                String reason = isMandatory
                        ? "Missing mandatory skill: " + skill.getName()
                        : "Missing optional skill: " + skill.getName();
                missingSkills.add(new CandidateRecommendationDto.MissingSkillDto(skill.getName(), requiredProf, null, isMandatory, reason));
                if (isMandatory) {
                    exclusionReasons.add(reason);
                }
            } else if (empSkill.getProficiencyLevel() == null) {
                // Unvalidated / unknown proficiency - NEVER invent proficiency!
                String reason = isMandatory
                        ? "Mandatory skill '" + skill.getName() + "' has unknown/unvalidated proficiency"
                        : "Optional skill '" + skill.getName() + "' has unknown/unvalidated proficiency";
                missingSkills.add(new CandidateRecommendationDto.MissingSkillDto(skill.getName(), requiredProf, null, isMandatory, reason));
                if (isMandatory) {
                    exclusionReasons.add(reason);
                }
            } else if (empSkill.getProficiencyLevel() < requiredProf) {
                // Below required minimum proficiency
                String reason = "Proficiency in " + skill.getName() + " (" + empSkill.getProficiencyLevel()
                        + ") is below required minimum (" + requiredProf + ")";
                missingSkills.add(new CandidateRecommendationDto.MissingSkillDto(skill.getName(), requiredProf, empSkill.getProficiencyLevel(), isMandatory, reason));
                if (isMandatory) {
                    exclusionReasons.add(reason);
                }
            } else {
                // Satisfies required proficiency!
                if (isMandatory) mandatoryMatchedCount++;
                else optionalMatchedCount++;

                matchedSkills.add(new CandidateRecommendationDto.MatchedSkillDto(
                        skill.getName(),
                        requiredProf,
                        empSkill.getProficiencyLevel(),
                        isMandatory,
                        empSkill.getLastValidatedAt() != null ? empSkill.getLastValidatedAt().toString() : "Validated"
                ));

                evaluableSkillCount++;
                // Bonus for exceeding proficiency (0 to 2 extra levels)
                int extra = Math.min(2, empSkill.getProficiencyLevel() - requiredProf);
                totalProficiencyBonus += extra;
            }
        }

        // 3. Availability and Capacity Evaluation
        ResourceAvailabilityDto availability = calculateAvailability(
                employee.getId(),
                demand.getStartDate(),
                demand.getEndDate(),
                demand.getAllocationPercent()
        );

        candidate.setAvailableCapacityPercent(availability.getMinAvailableCapacityPercent());

        if (!availability.isFullyAvailable()) {
            exclusionReasons.add("Insufficient capacity for requested period (" + demand.getStartDate() + " to "
                    + demand.getEndDate() + "): available " + availability.getMinAvailableCapacityPercent()
                    + "%, required " + demand.getAllocationPercent() + "%");
        }

        // Final Eligibility Flag
        boolean isEligible = exclusionReasons.isEmpty();
        candidate.setEligible(isEligible);
        candidate.setExclusionReasons(exclusionReasons);
        candidate.setMatchedSkills(matchedSkills);
        candidate.setMissingSkills(missingSkills);

        // 4. Scoring Breakdown (0 to 100)
        ScoreBreakdownDto scoreBreakdown = calculateScoreBreakdown(
                mandatoryCount, mandatoryMatchedCount,
                optionalCount, optionalMatchedCount,
                evaluableSkillCount, totalProficiencyBonus,
                availability.getMinAvailableCapacityPercent(),
                employee.getLocation(), demand.getLocation()
        );

        candidate.setScoreBreakdown(scoreBreakdown);
        candidate.setOverallScore(scoreBreakdown.getTotalScore());

        return candidate;
    }

    /**
     * Calculates the explainable 0-100 score breakdown with explicit weights:
     * - Skill Match: 40 points
     * - Proficiency Fit: 30 points
     * - Capacity / Availability: 20 points
     * - Location Fit: 10 points
     */
    public ScoreBreakdownDto calculateScoreBreakdown(int mandatoryCount, int mandatoryMatchedCount,
                                                    int optionalCount, int optionalMatchedCount,
                                                    int evaluableSkillCount, double totalProficiencyBonus,
                                                    int availableCapacityPercent,
                                                    String employeeLocation, String demandLocation) {
        // A. Skill Match Score (40 pts)
        double skillMatchScore;
        if (mandatoryCount == 0 && optionalCount == 0) {
            skillMatchScore = 40.0;
        } else if (optionalCount == 0) {
            skillMatchScore = ((double) mandatoryMatchedCount / mandatoryCount) * 40.0;
        } else if (mandatoryCount == 0) {
            skillMatchScore = ((double) optionalMatchedCount / optionalCount) * 40.0;
        } else {
            double mandatoryPart = ((double) mandatoryMatchedCount / mandatoryCount) * 28.0;
            double optionalPart = ((double) optionalMatchedCount / optionalCount) * 12.0;
            skillMatchScore = mandatoryPart + optionalPart;
        }

        // B. Proficiency Fit Score (30 pts)
        double proficiencyScore = 0.0;
        if (evaluableSkillCount > 0) {
            // Base 20 points for meeting all required proficiencies
            double baseProficiency = ((double) (mandatoryMatchedCount + optionalMatchedCount) / Math.max(1, mandatoryCount + optionalCount)) * 20.0;
            // Up to 10 bonus points for exceeding minimum proficiency
            double bonusProficiency = Math.min(10.0, (totalProficiencyBonus / (evaluableSkillCount * 2.0)) * 10.0);
            proficiencyScore = Math.min(30.0, baseProficiency + bonusProficiency);
        }

        // C. Capacity Score (20 pts)
        double capacityScore = (Math.max(0, Math.min(100, availableCapacityPercent)) / 100.0) * 20.0;

        // D. Location Fit Score (10 pts)
        double locationScore;
        boolean exactLocation = employeeLocation != null && demandLocation != null &&
                employeeLocation.trim().equalsIgnoreCase(demandLocation.trim());
        boolean remoteCompatible = (employeeLocation != null && employeeLocation.equalsIgnoreCase("Remote"))
                || (demandLocation != null && demandLocation.equalsIgnoreCase("Remote"));

        if (exactLocation) {
            locationScore = 10.0;
        } else if (remoteCompatible) {
            locationScore = 6.0;
        } else {
            locationScore = 0.0;
        }

        // Total (Clamped to 0-100, rounded to 1 decimal place)
        double total = Math.min(100.0, Math.max(0.0, skillMatchScore + proficiencyScore + capacityScore + locationScore));
        total = Math.round(total * 10.0) / 10.0;
        skillMatchScore = Math.round(skillMatchScore * 10.0) / 10.0;
        proficiencyScore = Math.round(proficiencyScore * 10.0) / 10.0;
        capacityScore = Math.round(capacityScore * 10.0) / 10.0;
        locationScore = Math.round(locationScore * 10.0) / 10.0;

        ScoreBreakdownDto breakdown = new ScoreBreakdownDto(skillMatchScore, proficiencyScore, capacityScore, locationScore, total);
        breakdown.addExplanation("SkillMatch", "Matched " + mandatoryMatchedCount + "/" + mandatoryCount + " mandatory skills (" + skillMatchScore + "/40 pts)");
        breakdown.addExplanation("Proficiency", "Proficiency compliance and expertise bonus (" + proficiencyScore + "/30 pts)");
        breakdown.addExplanation("Capacity", "Available concurrent capacity of " + availableCapacityPercent + "% (" + capacityScore + "/20 pts)");
        breakdown.addExplanation("Location", exactLocation ? "Exact location match: " + employeeLocation + " (" + locationScore + "/10 pts)"
                : (remoteCompatible ? "Remote compatible (" + locationScore + "/10 pts)" : "Location mismatch (" + locationScore + "/10 pts)"));

        return breakdown;
    }

    /**
     * Accurately calculates resource availability using inclusive-date semantics
     * and daily concurrent committed capacity.
     */
    public ResourceAvailabilityDto calculateAvailability(Long employeeId, LocalDate startDate, LocalDate endDate, int requestedPercent) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot precede start date");
        }

        ResourceAvailabilityDto result = new ResourceAvailabilityDto();
        result.setEmployeeId(employee.getId());
        result.setEmployeeCode(employee.getEmployeeCode());
        result.setName(employee.getName());
        result.setLocation(employee.getLocation());
        result.setDepartment(employee.getDepartment());
        result.setRequestedStartDate(startDate);
        result.setRequestedEndDate(endDate);
        result.setRequestedPercent(requestedPercent);

        // Fetch all APPROVED allocations that overlap with the requested period using inclusive dates
        List<Allocation> overlappingApproved = allocationRepository.findOverlappingApprovedAllocations(employeeId, startDate, endDate);

        List<ResourceAvailabilityDto.ConflictingAllocationDto> conflicts = new ArrayList<>();
        for (Allocation alloc : overlappingApproved) {
            conflicts.add(new ResourceAvailabilityDto.ConflictingAllocationDto(
                    alloc.getId(),
                    alloc.getDemand() != null ? alloc.getDemand().getProjectName() : "Allocated Project",
                    alloc.getStartDate(),
                    alloc.getEndDate(),
                    alloc.getAllocationPercent(),
                    alloc.getStatus().name()
            ));
        }
        result.setConflictingAllocations(conflicts);

        // Discretize into capacity segments across the date range
        // Gather all critical boundary dates
        TreeSet<LocalDate> boundaryDates = new TreeSet<>();
        boundaryDates.add(startDate);
        boundaryDates.add(endDate.plusDays(1)); // Day after end

        for (Allocation alloc : overlappingApproved) {
            if (!alloc.getStartDate().isBefore(startDate) && !alloc.getStartDate().isAfter(endDate)) {
                boundaryDates.add(alloc.getStartDate());
            }
            if (!alloc.getEndDate().plusDays(1).isBefore(startDate) && !alloc.getEndDate().plusDays(1).isAfter(endDate.plusDays(1))) {
                boundaryDates.add(alloc.getEndDate().plusDays(1));
            }
        }

        List<LocalDate> sortedDates = new ArrayList<>(boundaryDates);
        int maxCommittedPercent = 0;
        List<ResourceAvailabilityDto.CapacitySegmentDto> segments = new ArrayList<>();

        for (int i = 0; i < sortedDates.size() - 1; i++) {
            LocalDate segmentStart = sortedDates.get(i);
            LocalDate segmentEnd = sortedDates.get(i + 1).minusDays(1);

            if (segmentStart.isAfter(endDate) || segmentEnd.isBefore(startDate)) {
                continue;
            }

            // Calculate committed percent for this segment
            int committedOnSegment = 0;
            for (Allocation alloc : overlappingApproved) {
                // Inclusive check: alloc covers this day
                if (!alloc.getStartDate().isAfter(segmentStart) && !alloc.getEndDate().isBefore(segmentStart)) {
                    committedOnSegment += alloc.getAllocationPercent();
                }
            }

            maxCommittedPercent = Math.max(maxCommittedPercent, committedOnSegment);
            int availableOnSegment = Math.max(0, 100 - committedOnSegment);

            segments.add(new ResourceAvailabilityDto.CapacitySegmentDto(
                    segmentStart,
                    segmentEnd,
                    committedOnSegment,
                    availableOnSegment
            ));
        }

        result.setCapacitySegments(segments);
        result.setMaxConcurrentCommittedPercent(maxCommittedPercent);
        int minAvailable = Math.max(0, 100 - maxCommittedPercent);
        result.setMinAvailableCapacityPercent(minAvailable);

        boolean fullyAvailable = minAvailable >= requestedPercent;
        result.setFullyAvailable(fullyAvailable);
        result.setPartiallyAvailable(!fullyAvailable && (minAvailable > 0 || segments.stream().anyMatch(s -> s.getAvailablePercent() >= requestedPercent)));

        return result;
    }

    /**
     * Endpoint query for finding available resources with optional skill, date, and capacity filters.
     */
    public List<ResourceAvailabilityDto> findAvailableResources(LocalDate startDate, LocalDate endDate,
                                                               Integer minCapacityPercent,
                                                               Long skillId, Integer minProficiency,
                                                               String department, String location) {
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        LocalDate end = endDate != null ? endDate : start.plusMonths(3);
        int minCap = minCapacityPercent != null ? minCapacityPercent : 1;

        List<Employee> activeEmployees = employeeRepository.findByEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        List<ResourceAvailabilityDto> availableResources = new ArrayList<>();

        for (Employee emp : activeEmployees) {
            if (department != null && !department.isBlank() && !emp.getDepartment().equalsIgnoreCase(department.trim())) {
                continue;
            }
            if (location != null && !location.isBlank() && !emp.getLocation().equalsIgnoreCase(location.trim())) {
                continue;
            }

            if (skillId != null) {
                boolean hasSkill = false;
                if (emp.getEmployeeSkills() != null) {
                    for (EmployeeSkill es : emp.getEmployeeSkills()) {
                        if (es.getSkill().getId().equals(skillId)) {
                            int reqProf = minProficiency != null ? minProficiency : 1;
                            if (es.getProficiencyLevel() != null && es.getProficiencyLevel() >= reqProf) {
                                hasSkill = true;
                                break;
                            }
                        }
                    }
                }
                if (!hasSkill) continue;
            }

            ResourceAvailabilityDto avail = calculateAvailability(emp.getId(), start, end, minCap);
            if (avail.getMinAvailableCapacityPercent() >= minCap) {
                availableResources.add(avail);
            }
        }

        availableResources.sort(Comparator.comparing(ResourceAvailabilityDto::getMinAvailableCapacityPercent).reversed()
                .thenComparing(ResourceAvailabilityDto::getName));

        return availableResources;
    }
}
