package com.talentmarketplace.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_recommendations", indexes = {
    @Index(name = "idx_tr_employee", columnList = "employee_id"),
    @Index(name = "idx_tr_skill", columnList = "skill_id"),
    @Index(name = "idx_tr_status", columnList = "status")
})
public class TrainingRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "learning_path", nullable = false, length = 500)
    private String learningPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RecommendationStatus status = RecommendationStatus.RECOMMENDED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TrainingRecommendation() {}

    public TrainingRecommendation(Employee employee, Skill skill, String reason, String learningPath) {
        this.employee = employee;
        this.skill = skill;
        this.reason = reason;
        this.learningPath = learningPath;
        this.status = RecommendationStatus.RECOMMENDED;
        this.createdAt = LocalDateTime.now();
    }

    public enum RecommendationStatus {
        RECOMMENDED,
        ENROLLED,
        COMPLETED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getLearningPath() { return learningPath; }
    public void setLearningPath(String learningPath) { this.learningPath = learningPath; }

    public RecommendationStatus getStatus() { return status; }
    public void setStatus(RecommendationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
