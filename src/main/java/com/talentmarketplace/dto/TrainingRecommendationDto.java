package com.talentmarketplace.dto;

import com.talentmarketplace.entity.TrainingRecommendation;
import java.time.LocalDateTime;

public class TrainingRecommendationDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long skillId;
    private String skillName;
    private String reason;
    private String learningPath;
    private TrainingRecommendation.RecommendationStatus status;
    private LocalDateTime createdAt;

    public TrainingRecommendationDto() {}

    public TrainingRecommendationDto(Long id, Long employeeId, String employeeName, Long skillId,
                                     String skillName, String reason, String learningPath,
                                     TrainingRecommendation.RecommendationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.skillId = skillId;
        this.skillName = skillName;
        this.reason = reason;
        this.learningPath = learningPath;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getLearningPath() { return learningPath; }
    public void setLearningPath(String learningPath) { this.learningPath = learningPath; }

    public TrainingRecommendation.RecommendationStatus getStatus() { return status; }
    public void setStatus(TrainingRecommendation.RecommendationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
