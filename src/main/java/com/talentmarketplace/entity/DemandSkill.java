package com.talentmarketplace.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "demand_skills",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_demand_skill", columnNames = {"demand_id", "skill_id"})
    },
    indexes = {
        @Index(name = "idx_demand_skill_demand", columnList = "demand_id"),
        @Index(name = "idx_demand_skill_skill", columnList = "skill_id")
    }
)
public class DemandSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demand_id", nullable = false)
    @JsonIgnore
    private ProjectDemand demand;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "is_mandatory", nullable = false)
    private boolean mandatory = true;

    @NotNull(message = "Minimum proficiency level is required")
    @Min(value = 1, message = "Minimum proficiency level must be between 1 and 5")
    @Max(value = 5, message = "Minimum proficiency level must be between 1 and 5")
    @Column(name = "min_proficiency", nullable = false)
    private Integer minProficiency = 1;

    public DemandSkill() {}

    public DemandSkill(ProjectDemand demand, Skill skill, boolean mandatory, Integer minProficiency) {
        this.demand = demand;
        this.skill = skill;
        this.mandatory = mandatory;
        this.minProficiency = minProficiency != null ? minProficiency : 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProjectDemand getDemand() { return demand; }
    public void setDemand(ProjectDemand demand) { this.demand = demand; }

    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }

    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

    public Integer getMinProficiency() { return minProficiency; }
    public void setMinProficiency(Integer minProficiency) { this.minProficiency = minProficiency; }
}
