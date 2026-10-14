package com.talentmarketplace.dto;

import java.util.HashMap;
import java.util.Map;

public class ScoreBreakdownDto {
    private double skillMatchScore;     // Max 40
    private double proficiencyScore;    // Max 30
    private double capacityScore;       // Max 20
    private double locationScore;       // Max 10
    private double totalScore;          // Max 100
    private Map<String, String> scoreExplanations = new HashMap<>();

    public ScoreBreakdownDto() {}

    public ScoreBreakdownDto(double skillMatchScore, double proficiencyScore, double capacityScore, double locationScore, double totalScore) {
        this.skillMatchScore = skillMatchScore;
        this.proficiencyScore = proficiencyScore;
        this.capacityScore = capacityScore;
        this.locationScore = locationScore;
        this.totalScore = totalScore;
    }

    public double getSkillMatchScore() { return skillMatchScore; }
    public void setSkillMatchScore(double skillMatchScore) { this.skillMatchScore = skillMatchScore; }

    public double getProficiencyScore() { return proficiencyScore; }
    public void setProficiencyScore(double proficiencyScore) { this.proficiencyScore = proficiencyScore; }

    public double getCapacityScore() { return capacityScore; }
    public void setCapacityScore(double capacityScore) { this.capacityScore = capacityScore; }

    public double getLocationScore() { return locationScore; }
    public void setLocationScore(double locationScore) { this.locationScore = locationScore; }

    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

    public Map<String, String> getScoreExplanations() { return scoreExplanations; }
    public void setScoreExplanations(Map<String, String> scoreExplanations) { this.scoreExplanations = scoreExplanations; }

    public void addExplanation(String category, String explanation) {
        this.scoreExplanations.put(category, explanation);
    }
}
