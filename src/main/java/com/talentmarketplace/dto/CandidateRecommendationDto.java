package com.talentmarketplace.dto;

import java.util.ArrayList;
import java.util.List;

public class CandidateRecommendationDto {
    private Long employeeId;
    private String employeeCode;
    private String name;
    private String location;
    private String department;
    private boolean eligible;
    private double overallScore; // 0 to 100
    private ScoreBreakdownDto scoreBreakdown;
    private List<String> exclusionReasons = new ArrayList<>();
    private List<MatchedSkillDto> matchedSkills = new ArrayList<>();
    private List<MissingSkillDto> missingSkills = new ArrayList<>();
    private int availableCapacityPercent;

    public CandidateRecommendationDto() {}

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public ScoreBreakdownDto getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(ScoreBreakdownDto scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }

    public List<String> getExclusionReasons() { return exclusionReasons; }
    public void setExclusionReasons(List<String> exclusionReasons) { this.exclusionReasons = exclusionReasons; }

    public List<MatchedSkillDto> getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(List<MatchedSkillDto> matchedSkills) { this.matchedSkills = matchedSkills; }

    public List<MissingSkillDto> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<MissingSkillDto> missingSkills) { this.missingSkills = missingSkills; }

    public int getAvailableCapacityPercent() { return availableCapacityPercent; }
    public void setAvailableCapacityPercent(int availableCapacityPercent) { this.availableCapacityPercent = availableCapacityPercent; }

    public static class MatchedSkillDto {
        private String skillName;
        private int requiredProficiency;
        private Integer actualProficiency;
        private boolean mandatory;
        private String validatedDate;

        public MatchedSkillDto() {}

        public MatchedSkillDto(String skillName, int requiredProficiency, Integer actualProficiency, boolean mandatory, String validatedDate) {
            this.skillName = skillName;
            this.requiredProficiency = requiredProficiency;
            this.actualProficiency = actualProficiency;
            this.mandatory = mandatory;
            this.validatedDate = validatedDate;
        }

        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }

        public int getRequiredProficiency() { return requiredProficiency; }
        public void setRequiredProficiency(int requiredProficiency) { this.requiredProficiency = requiredProficiency; }

        public Integer getActualProficiency() { return actualProficiency; }
        public void setActualProficiency(Integer actualProficiency) { this.actualProficiency = actualProficiency; }

        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

        public String getValidatedDate() { return validatedDate; }
        public void setValidatedDate(String validatedDate) { this.validatedDate = validatedDate; }
    }

    public static class MissingSkillDto {
        private String skillName;
        private int requiredProficiency;
        private Integer actualProficiency; // null if completely missing
        private boolean mandatory;
        private String reason;

        public MissingSkillDto() {}

        public MissingSkillDto(String skillName, int requiredProficiency, Integer actualProficiency, boolean mandatory, String reason) {
            this.skillName = skillName;
            this.requiredProficiency = requiredProficiency;
            this.actualProficiency = actualProficiency;
            this.mandatory = mandatory;
            this.reason = reason;
        }

        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }

        public int getRequiredProficiency() { return requiredProficiency; }
        public void setRequiredProficiency(int requiredProficiency) { this.requiredProficiency = requiredProficiency; }

        public Integer getActualProficiency() { return actualProficiency; }
        public void setActualProficiency(Integer actualProficiency) { this.actualProficiency = actualProficiency; }

        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
