package com.talentmarketplace.controller;

import com.talentmarketplace.dto.SkillDto;
import com.talentmarketplace.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping
    public ResponseEntity<SkillDto> createSkill(@Valid @RequestBody SkillDto.CreateRequest request) {
        SkillDto created = skillService.createSkill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<SkillDto>> getAllSkills(@RequestParam(required = false) String category) {
        List<SkillDto> skills = skillService.getAllSkills(category);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillDto> getSkillById(@PathVariable Long id) {
        SkillDto skill = skillService.getSkillById(id);
        return ResponseEntity.ok(skill);
    }
}
