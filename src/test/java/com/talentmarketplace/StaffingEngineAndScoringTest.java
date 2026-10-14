package com.talentmarketplace;

import com.talentmarketplace.dto.CandidateRecommendationDto;
import com.talentmarketplace.dto.ResourceAvailabilityDto;
import com.talentmarketplace.entity.*;
import com.talentmarketplace.repository.*;
import com.talentmarketplace.service.StaffingEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StaffingEngineAndScoringTest {

    @Autowired
    private StaffingEngineService staffingEngineService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepository;

    @Autowired
    private ProjectDemandRepository demandRepository;

    @Autowired
    private DemandSkillRepository demandSkillRepository;

    @Autowired
    private AllocationRepository allocationRepository;

    @Test
    @DisplayName("TC-06-03: Candidate search follows documented eligibility and scoring")
    void tc_06_03_candidateSearchFollowsDocumentedEligibilityAndScoring() {
        // Demand 1: Core Payment Engine Refactoring (Java min 4, Spring Boot min 3, London, 100%)
        ProjectDemand demand = demandRepository.findAll().stream()
                .filter(d -> d.getProjectName().contains("Payment"))
                .findFirst().orElseThrow();

        List<CandidateRecommendationDto> candidates = staffingEngineService.getRecommendationsForDemand(demand.getId());

        assertFalse(candidates.isEmpty(), "Should return evaluated candidates");

        // Top candidate should be Alice Johnson (EMP001) who meets all criteria in London
        CandidateRecommendationDto top = candidates.get(0);
        assertTrue(top.isEligible(), "Top candidate should be eligible");
        assertEquals("EMP001", top.getEmployeeCode());

        // Verify score components
        assertNotNull(top.getScoreBreakdown());
        assertTrue(top.getOverallScore() > 0 && top.getOverallScore() <= 100);
        assertEquals(40.0, top.getScoreBreakdown().getSkillMatchScore(), 0.1);
        assertTrue(top.getScoreBreakdown().getProficiencyScore() > 0);
        assertTrue(top.getScoreBreakdown().getCapacityScore() > 0);
        assertEquals(10.0, top.getScoreBreakdown().getLocationScore(), 0.1, "Exact London match receives 10 pts");

        // Verify deterministic ranking: eligible candidates come before ineligible
        boolean seenIneligible = false;
        for (CandidateRecommendationDto c : candidates) {
            if (!c.isEligible()) {
                seenIneligible = true;
            } else {
                assertFalse(seenIneligible, "Eligible candidates must always precede ineligible candidates");
            }
        }
    }

    @Test
    @DisplayName("TC-06-04: Missing mandatory skills are excluded or explicitly flagged according to policy")
    void tc_06_04_missingMandatorySkillsAreExcludedOrFlagged() {
        // Frank Miller (EMP006) has Spring Boot level 2 (required is level 3 for Demand 1)
        ProjectDemand demand = demandRepository.findAll().stream()
                .filter(d -> d.getProjectName().contains("Payment"))
                .findFirst().orElseThrow();

        List<CandidateRecommendationDto> candidates = staffingEngineService.getRecommendationsForDemand(demand.getId());

        CandidateRecommendationDto frank = candidates.stream()
                .filter(c -> c.getEmployeeCode().equals("EMP006"))
                .findFirst().orElseThrow();

        assertFalse(frank.isEligible(), "Frank Miller should be marked ineligible due to low proficiency and capacity");
        assertFalse(frank.getExclusionReasons().isEmpty(), "Exclusion reasons must not be empty");
        assertTrue(frank.getExclusionReasons().stream().anyMatch(r -> r.toLowerCase().contains("proficiency") || r.toLowerCase().contains("spring boot")),
                "Should explicitly flag proficiency deficit in Spring Boot");

        // Henry Zhao (EMP008) has unvalidated / NULL proficiency for Python in Demand 2
        ProjectDemand mlDemand = demandRepository.findAll().stream()
                .filter(d -> d.getProjectName().contains("Fraud"))
                .findFirst().orElseThrow();

        List<CandidateRecommendationDto> mlCandidates = staffingEngineService.getRecommendationsForDemand(mlDemand.getId());
        CandidateRecommendationDto henry = mlCandidates.stream()
                .filter(c -> c.getEmployeeCode().equals("EMP008"))
                .findFirst().orElseThrow();

        assertFalse(henry.isEligible(), "Candidate with unvalidated mandatory skill proficiency must be ineligible");
        assertTrue(henry.getExclusionReasons().stream().anyMatch(r -> r.toLowerCase().contains("unvalidated") || r.toLowerCase().contains("unknown")),
                "Should disclose unvalidated / unknown proficiency as exclusion reason");
    }

    @Test
    @DisplayName("TC-06-05: Full-period allocation makes the employee unavailable for conflicting requests")
    void tc_06_05_fullPeriodAllocationMakesEmployeeUnavailable() {
        Employee emp = employeeRepository.findByEmployeeCode("EMP002").orElseThrow(); // Bob Smith

        // Create an APPROVED 100% allocation for Bob Smith covering entire month of November 2026
        ProjectDemand demand = demandRepository.findAll().get(0);
        Allocation fullAlloc = new Allocation(
                emp, demand, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30),
                100, Allocation.AllocationStatus.APPROVED, "manager"
        );
        allocationRepository.save(fullAlloc);

        // Bob should now have 0% available capacity for any overlapping request in November
        ResourceAvailabilityDto availability = staffingEngineService.calculateAvailability(
                emp.getId(), LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 20), 50
        );

        assertEquals(100, availability.getMaxConcurrentCommittedPercent());
        assertEquals(0, availability.getMinAvailableCapacityPercent());
        assertFalse(availability.isFullyAvailable(), "Should not be fully available when 100% committed");
    }

    @Test
    @DisplayName("TC-06-06: Partial availability is accurately reported")
    void tc_06_06_partialAvailabilityIsAccuratelyReported() {
        // Frank Miller (EMP006) has an approved 50% allocation from 2026-11-01 to 2027-01-31
        Employee frank = employeeRepository.findByEmployeeCode("EMP006").orElseThrow();

        ResourceAvailabilityDto availability = staffingEngineService.calculateAvailability(
                frank.getId(), LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31), 100
        );

        assertEquals(50, availability.getMaxConcurrentCommittedPercent());
        assertEquals(50, availability.getMinAvailableCapacityPercent(), "Remaining capacity should be exactly 50%");
        assertFalse(availability.isFullyAvailable(), "Should not be fully available for a 100% request");
        assertTrue(availability.isPartiallyAvailable(), "Should be reported as partially available");
        assertFalse(availability.getCapacitySegments().isEmpty(), "Should report capacity segments");
    }
}
