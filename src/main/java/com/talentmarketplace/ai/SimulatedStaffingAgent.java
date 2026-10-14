package com.talentmarketplace.ai;

import com.talentmarketplace.dto.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimulatedStaffingAgent implements StaffingAgent {

    private final StaffingToolsService toolsService;

    public SimulatedStaffingAgent(StaffingToolsService toolsService) {
        this.toolsService = toolsService;
    }

    @Override
    public StaffingAdviceResponse provideStaffingAdvice(StaffingAdviceRequest request) {
        if (request.getDemandId() == null) {
            throw new IllegalArgumentException("Demand ID cannot be null");
        }
        if (request.getGoal() == null || request.getGoal().isBlank()) {
            throw new IllegalArgumentException("Staffing goal cannot be blank");
        }

        List<String> trace = new ArrayList<>();
        trace.add("[Agent Init] Goal accepted: \"" + request.getGoal().trim() + "\" for Demand ID: " + request.getDemandId());

        // Step 1: Tool invocation - getDemand
        trace.add("[Tool 1/6] Invoking getDemand(" + request.getDemandId() + ")");
        DemandDto demand = toolsService.getDemand(request.getDemandId());
        trace.add("[Tool 1/6 Result] Retrieved demand '" + demand.getProjectName() + "' (Role: " + demand.getRole()
                + ", Window: " + demand.getStartDate() + " to " + demand.getEndDate() + ", Allocation: " + demand.getAllocationPercent() + "%)");

        // Step 2: Tool invocation - getCandidateRecommendations
        trace.add("[Tool 2/6] Invoking getCandidateRecommendations(" + demand.getId() + ")");
        List<CandidateRecommendationDto> candidates = toolsService.getCandidateRecommendations(demand.getId());
        trace.add("[Tool 2/6 Result] Evaluated " + candidates.size() + " total candidates against database rules.");

        // Step 3: Find top candidate
        CandidateRecommendationDto topCandidate = candidates.stream()
                .filter(CandidateRecommendationDto::isEligible)
                .findFirst()
                .orElse(candidates.isEmpty() ? null : candidates.get(0));

        List<TrainingRecommendationDto> trainingRecs = new ArrayList<>();

        if (topCandidate != null) {
            // Step 4: Tool invocation - getEmployeeSkillEvidence
            trace.add("[Tool 3/6] Invoking getEmployeeSkillEvidence(EmployeeID: " + topCandidate.getEmployeeId() + ")");
            List<EmployeeSkillDto> evidence = toolsService.getEmployeeSkillEvidence(topCandidate.getEmployeeId());
            trace.add("[Tool 3/6 Result] Found " + evidence.size() + " validated skill evidence records.");

            // Step 5: Tool invocation - checkAvailability
            trace.add("[Tool 4/6] Invoking checkAvailability(EmployeeID: " + topCandidate.getEmployeeId() + ", Start: "
                    + demand.getStartDate() + ", End: " + demand.getEndDate() + ", ReqPercent: " + demand.getAllocationPercent() + ")");
            ResourceAvailabilityDto avail = toolsService.checkAvailability(
                    topCandidate.getEmployeeId(), demand.getStartDate(), demand.getEndDate(), demand.getAllocationPercent());
            trace.add("[Tool 4/6 Result] Availability status: FullyAvailable=" + avail.isFullyAvailable()
                    + ", MinAvailable=" + avail.getMinAvailableCapacityPercent() + "%");

            // Step 6: Tool invocation - generateTrainingRecommendations for non-eligible or near-match candidates
            for (CandidateRecommendationDto candidate : candidates) {
                if (!candidate.isEligible() && !candidate.getMissingSkills().isEmpty() && trainingRecs.size() < 3) {
                    trace.add("[Tool 5/6] Invoking generateTrainingRecommendations(EmployeeID: " + candidate.getEmployeeId() + ")");
                    List<TrainingRecommendationDto> generated = toolsService.generateTrainingRecommendations(candidate.getEmployeeId(), demand.getId());
                    trainingRecs.addAll(generated);
                    trace.add("[Tool 5/6 Result] Generated " + generated.size() + " targeted training pathways.");
                }
            }
        }

        trace.add("[Agent Summary] Synthesis complete. Prepared recommendation for manager approval. Direct auto-approval prevented by governance policy.");

        // Synthesize strategic rationale
        StringBuilder rationale = new StringBuilder();
        if (topCandidate != null && topCandidate.isEligible()) {
            rationale.append("Recommended Candidate: **").append(topCandidate.getName()).append("** (").append(topCandidate.getEmployeeCode()).append(").\n\n");
            rationale.append("• **Overall Match Score:** ").append(topCandidate.getOverallScore()).append("/100 (Skill: ")
                    .append(topCandidate.getScoreBreakdown().getSkillMatchScore()).append("/40, Proficiency: ")
                    .append(topCandidate.getScoreBreakdown().getProficiencyScore()).append("/30, Capacity: ")
                    .append(topCandidate.getScoreBreakdown().getCapacityScore()).append("/20, Location: ")
                    .append(topCandidate.getScoreBreakdown().getLocationScore()).append("/10).\n");
            rationale.append("• **Skill Verification:** Satisfies all mandatory criteria: ");
            topCandidate.getMatchedSkills().forEach(s -> rationale.append(s.getSkillName()).append(" (Lvl ").append(s.getActualProficiency()).append("), "));
            rationale.append("\n• **Capacity Verification:** Guaranteed ").append(topCandidate.getAvailableCapacityPercent())
                    .append("% uncommitted capacity across the requested period (").append(demand.getStartDate()).append(" to ").append(demand.getEndDate()).append(").\n");
            rationale.append("• **Location Fit:** Candidate location is ").append(topCandidate.getLocation())
                    .append(" for project location ").append(demand.getLocation()).append(".\n\n");
            rationale.append("Governance Action: Please review the score breakdown and trigger the manager approval workflow to commit allocation.");
        } else {
            rationale.append("No fully eligible candidate currently exists in the talent pool for demand '")
                    .append(demand.getProjectName()).append("'.\n\n");
            if (topCandidate != null && !topCandidate.getExclusionReasons().isEmpty()) {
                rationale.append("Closest candidate **").append(topCandidate.getName()).append("** has the following deficits:\n");
                for (String reason : topCandidate.getExclusionReasons()) {
                    rationale.append("• ").append(reason).append("\n");
                }
            }
            rationale.append("\nSuggested Action: Enroll candidates in targeted upskilling pathways or adjust project demand criteria.");
        }

        StaffingAdviceResponse response = new StaffingAdviceResponse();
        response.setDemandId(demand.getId());
        response.setProjectName(demand.getProjectName());
        response.setRole(demand.getRole());
        response.setGoal(request.getGoal());
        response.setSimulatedAgent(true);
        response.setSimulationDisclaimer("Simulated AI Staffing Agent — no live LLM connected. Grounded in deterministic backend engine data.");
        response.setTopCandidate(topCandidate);
        response.setCandidateRecommendations(candidates);
        response.setTrainingRecommendations(trainingRecs);
        response.setStrategicRationale(rationale.toString());
        response.setToolExecutionTrace(trace);

        return response;
    }
}
