package com.talentmarketplace.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
@Table(name = "skills", indexes = {
    @Index(name = "idx_skill_name", columnList = "name", unique = true),
    @Index(name = "idx_skill_category", columnList = "category")
})
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Skill name cannot be blank")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Skill category cannot be blank")
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    public Skill() {}

    public Skill(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public Skill(Long id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skill skill = (Skill) o;
        return Objects.equals(id, skill.id) || (name != null && name.equalsIgnoreCase(skill.name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name != null ? name.toLowerCase() : null);
    }
}
