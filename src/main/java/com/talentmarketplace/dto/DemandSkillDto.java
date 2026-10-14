package com.talentmarketplace.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DemandSkillDto {
    private Long id;
    private Long skillId;
    private String skillName;
    private String category;
    private boolean mandatory;
    private Integer minProficiency;

    public DemandSkillDto() {}

    public DemandSkillDto(Long id, Long skillId, String skillName, String category, boolean mandatory, Integer minProficiency) {
        this.id = id;
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.mandatory = mandatory;
        this.minProficiency = minProficiency;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

    public Integer getMinProficiency() { return minProficiency; }
    public void setMinProficiency(Integer minProficiency) { this.minProficiency = minProficiency; }

    public static class Request {
        private Long skillId;
        private String skillName;
        private boolean mandatory = true;

        @NotNull(message = "Minimum proficiency is required")
        @Min(value = 1, message = "Minimum proficiency must be between 1 and 5")
        @Max(value = 5, message = "Minimum proficiency must be between 1 and 5")
        private Integer minProficiency = 1;

        public Request() {}

        public Long getSkillId() { return skillId; }
        public void setSkillId(Long skillId) { this.skillId = skillId; }

        public String getSkillName() { return skillName; }
        public void setSkillName(String skillName) { this.skillName = skillName; }

        public boolean isMandatory() { return mandatory; }
        public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

        public Integer getMinProficiency() { return minProficiency; }
        public void setMinProficiency(Integer minProficiency) { this.minProficiency = minProficiency; }
    }
}
