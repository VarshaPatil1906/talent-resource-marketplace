package com.talentmarketplace.service;

import com.talentmarketplace.dto.SkillDto;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public SkillDto createSkill(SkillDto.CreateRequest request) {
        String name = request.getName().trim();
        if (skillRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Skill '" + name + "' already exists");
        }
        Skill skill = new Skill(name, request.getCategory().trim().toUpperCase());
        Skill saved = skillRepository.save(skill);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SkillDto> getAllSkills(String category) {
        List<Skill> skills = (category != null && !category.isBlank())
                ? skillRepository.findByCategoryIgnoreCase(category.trim())
                : skillRepository.findAll();
        return skills.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SkillDto getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
        return mapToDto(skill);
    }

    public SkillDto mapToDto(Skill skill) {
        return new SkillDto(skill.getId(), skill.getName(), skill.getCategory());
    }
}
