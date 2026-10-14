package com.talentmarketplace.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

public class EmployeeSkillDto {
    private Long id;
    private Long skillId;
    private String skillName;
    private String category;
    private Integer proficiencyLevel; // Null when unknown / unvalidated
    private LocalDate lastValidatedAt;

    public EmployeeSkillDto() {}

    public EmployeeSkillDto(Long id, Long skillId, String skillName, String category, Integer proficiencyLevel, LocalDate lastValidatedAt) {
        this.id = id;
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.proficiencyLevel = proficiencyLevel;
        this.lastValidatedAt = lastValidatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(Integer proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public LocalDate getLastValidatedAt() { return lastValidatedAt; }
    public void setLastValidatedAt(LocalDate lastValidatedAt) { this.lastValidatedAt = lastValidatedAt; }

    public static class AddRequest {
        private Long skillId;
        private String skillName;
        private String category;

        @Min(value = 1, message = "Proficiency level must be between 1 and 5")
        @Max(value = 5, message = "Proficiency level must be between 1 and 5")
        private Integer proficiencyLevel; // Optional; null represents unvalidated

        private LocalDate lastValidatedAt;

        public AddRequest() {}

        public Long getSkillId() { return skillId; }
        public void setSkillId(Long skillId) { this.skillId = skillId; }

        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Integer getProficiencyLevel() { return proficiencyLevel; }
        public void setProficiencyLevel(Integer proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

        public LocalDate getLastValidatedAt() { return lastValidatedAt; }
        public void setLastValidatedAt(LocalDate lastValidatedAt) { this.lastValidatedAt = lastValidatedAt; }
    }
}
