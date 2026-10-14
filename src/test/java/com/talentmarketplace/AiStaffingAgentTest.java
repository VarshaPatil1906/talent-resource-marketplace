package com.talentmarketplace;

import com.talentmarketplace.ai.StaffingAgent;
import com.talentmarketplace.ai.StaffingToolsService;
import com.talentmarketplace.dto.CandidateRecommendationDto;
import com.talentmarketplace.dto.StaffingAdviceRequest;
import com.talentmarketplace.dto.StaffingAdviceResponse;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.repository.ProjectDemandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AiStaffingAgentTest {

    @Autowired
    private StaffingAgent staffingAgent;

    @Autowired
    private StaffingToolsService toolsService;

    @Autowired
    private ProjectDemandRepository demandRepository;

    @Test
    @DisplayName("TC-06-09: AI recommendation includes evidence and scores matching database records")
    void tc_06_09_aiRecommendationIncludesEvidenceAndScoresMatchingDatabase() {
        ProjectDemand demand = demandRepository.findAll().stream()
                .filter(d -> d.getProjectName().contains("Payment"))
                .findFirst().orElseThrow();

        StaffingAdviceRequest request = new StaffingAdviceRequest(
                demand.getId(),
                "Identify top candidate for Core Payment Engine and prepare staffing advice"
        );

        StaffingAdviceResponse response = staffingAgent.provideStaffingAdvice(request);

        assertNotNull(response);
        assertEquals(demand.getId(), response.getDemandId());
        assertTrue(response.isSimulatedAgent());
        assertTrue(response.getSimulationDisclaimer().contains("Simulated AI Staffing Agent"));
        assertNotNull(response.getTopCandidate());

        // Verify top candidate matches database record
        CandidateRecommendationDto top = response.getTopCandidate();
        assertEquals("EMP001", top.getEmployeeCode());
        assertEquals("Alice Johnson", top.getName());
        assertTrue(top.isEligible());
        assertEquals(40.0, top.getScoreBreakdown().getSkillMatchScore(), 0.1);

        // Verify execution trace has tool invocations
        assertNotNull(response.getToolExecutionTrace());
        assertFalse(response.getToolExecutionTrace().isEmpty());
        assertTrue(response.getToolExecutionTrace().stream().anyMatch(t -> t.contains("getDemand")));
        assertTrue(response.getToolExecutionTrace().stream().anyMatch(t -> t.contains("getCandidateRecommendations")));
        assertTrue(response.getToolExecutionTrace().stream().anyMatch(t -> t.contains("getEmployeeSkillEvidence")));
        assertTrue(response.getToolExecutionTrace().stream().anyMatch(t -> t.contains("checkAvailability")));
    }

    @Test
    @DisplayName("TC-06-10: AI cannot invent proficiency or availability when data is missing")
    void tc_06_10_aiCannotInventProficiencyOrAvailabilityWhenDataIsMissing() {
        // Demand 5: Legacy Monolith Decommissioning requires Rust 5 and Erlang 5
        // No employee in the database possesses Rust or Erlang
        ProjectDemand demand = demandRepository.findAll().stream()
                .filter(d -> d.getProjectName().contains("Monolith"))
                .findFirst().orElseThrow();

        StaffingAdviceRequest request = new StaffingAdviceRequest(
                demand.getId(),
                "Find an engineer with distributed systems Rust and Erlang skills"
        );

        StaffingAdviceResponse response = staffingAgent.provideStaffingAdvice(request);

        assertNotNull(response);
        // None of the candidates should be marked eligible
        boolean anyEligible = response.getCandidateRecommendations().stream()
                .anyMatch(CandidateRecommendationDto::isEligible);
        assertFalse(anyEligible, "AI must not invent skills: zero candidates should be marked eligible");

        // The top/closest candidate must clearly list missing skills in exclusion reasons
        if (response.getTopCandidate() != null) {
            assertFalse(response.getTopCandidate().isEligible());
            assertTrue(response.getTopCandidate().getExclusionReasons().stream()
                    .anyMatch(r -> r.toLowerCase().contains("missing mandatory skill") || r.toLowerCase().contains("rust") || r.toLowerCase().contains("erlang")));
        }

        // Training recommendations should be suggested for missing skills rather than fabricating skills
        assertFalse(response.getTrainingRecommendations().isEmpty(), "AI should suggest training recommendations for unstaffed demands");
        assertTrue(response.getStrategicRationale().contains("No fully eligible candidate"));
    }

    @Test
    @DisplayName("AI tools reject invalid inputs and enforce governance boundaries")
    void aiToolsRejectInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> toolsService.getDemand(null));
        assertThrows(IllegalArgumentException.class, () -> toolsService.getCandidateRecommendations(null));
        assertThrows(IllegalArgumentException.class, () -> toolsService.getEmployeeSkillEvidence(null));
        assertThrows(IllegalArgumentException.class, () -> toolsService.checkAvailability(null, null, null, 100));
    }
}
