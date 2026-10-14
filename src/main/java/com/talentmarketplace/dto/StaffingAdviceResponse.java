package com.talentmarketplace.dto;

import java.util.ArrayList;
import java.util.List;

public class StaffingAdviceResponse {
    private Long demandId;
    private String projectName;
    private String role;
    private String goal;
    private boolean simulatedAgent = true;
    private String simulationDisclaimer = "Simulated AI Staffing Agent — no live LLM connected. All decisions grounded in deterministic database facts.";
    private CandidateRecommendationDto topCandidate;
    private List<CandidateRecommendationDto> candidateRecommendations = new ArrayList<>();
    private List<TrainingRecommendationDto> trainingRecommendations = new ArrayList<>();
    private String strategicRationale;
    private List<String> toolExecutionTrace = new ArrayList<>();

    public StaffingAdviceResponse() {}

    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public boolean isSimulatedAgent() { return simulatedAgent; }
    public void setSimulatedAgent(boolean simulatedAgent) { this.simulatedAgent = simulatedAgent; }

    public String getSimulationDisclaimer() { return simulationDisclaimer; }
    public void setSimulationDisclaimer(String simulationDisclaimer) { this.simulationDisclaimer = simulationDisclaimer; }

    public CandidateRecommendationDto getTopCandidate() { return topCandidate; }
    public void setTopCandidate(CandidateRecommendationDto topCandidate) { this.topCandidate = topCandidate; }

    public List<CandidateRecommendationDto> getCandidateRecommendations() { return candidateRecommendations; }
    public void setCandidateRecommendations(List<CandidateRecommendationDto> candidateRecommendations) { this.candidateRecommendations = candidateRecommendations; }

    public List<TrainingRecommendationDto> getTrainingRecommendations() { return trainingRecommendations; }
    public void setTrainingRecommendations(List<TrainingRecommendationDto> trainingRecommendations) { this.trainingRecommendations = trainingRecommendations; }

    public String getStrategicRationale() { return strategicRationale; }
    public void setStrategicRationale(String strategicRationale) { this.strategicRationale = strategicRationale; }

    public List<String> getToolExecutionTrace() { return toolExecutionTrace; }
    public void setToolExecutionTrace(List<String> toolExecutionTrace) { this.toolExecutionTrace = toolExecutionTrace; }
}
