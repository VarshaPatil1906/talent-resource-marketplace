package com.talentmarketplace.service;

import com.talentmarketplace.dto.DemandDto;
import com.talentmarketplace.dto.DemandSkillDto;
import com.talentmarketplace.entity.DemandSkill;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.DemandSkillRepository;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DemandService {

    private final ProjectDemandRepository demandRepository;
    private final SkillRepository skillRepository;
    private final DemandSkillRepository demandSkillRepository;

    public DemandService(ProjectDemandRepository demandRepository,
                         SkillRepository skillRepository,
                         DemandSkillRepository demandSkillRepository) {
        this.demandRepository = demandRepository;
        this.skillRepository = skillRepository;
        this.demandSkillRepository = demandSkillRepository;
    }

    public DemandDto createDemand(DemandDto.CreateRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot precede start date");
        }
        if (request.getAllocationPercent() <= 0 || request.getAllocationPercent() > 100) {
            throw new IllegalArgumentException("Allocation percent must be between 1 and 100");
        }

        ProjectDemand demand = new ProjectDemand(
                request.getProjectName().trim(),
                request.getRole().trim(),
                request.getStartDate(),
                request.getEndDate(),
                request.getLocation().trim(),
                request.getAllocationPercent(),
                request.getPriority() != null ? request.getPriority() : ProjectDemand.Priority.MEDIUM,
                request.getStatus() != null ? request.getStatus() : ProjectDemand.DemandStatus.OPEN
        );

        ProjectDemand saved = demandRepository.save(demand);

        if (request.getRequiredSkills() != null && !request.getRequiredSkills().isEmpty()) {
            for (DemandSkillDto.Request skillReq : request.getRequiredSkills()) {
                Skill skill;
                if (skillReq.getSkillId() != null) {
                    skill = skillRepository.findById(skillReq.getSkillId())
                            .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillReq.getSkillId()));
                } else if (skillReq.getSkillName() != null && !skillReq.getSkillName().isBlank()) {
                    String name = skillReq.getSkillName().trim();
                    skill = skillRepository.findByNameIgnoreCase(name)
                            .orElseGet(() -> skillRepository.save(new Skill(name, "GENERAL")));
                } else {
                    continue;
                }

                int minProf = (skillReq.getMinProficiency() != null && skillReq.getMinProficiency() >= 1 && skillReq.getMinProficiency() <= 5)
                        ? skillReq.getMinProficiency() : 1;

                DemandSkill demandSkill = new DemandSkill(saved, skill, skillReq.isMandatory(), minProf);
                demandSkillRepository.save(demandSkill);
                saved.addDemandSkill(demandSkill);
            }
        }

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DemandDto> getAllDemands(ProjectDemand.DemandStatus status,
                                        ProjectDemand.Priority priority,
                                        String role,
                                        String location,
                                        LocalDate startDate,
                                        LocalDate endDate) {
        List<ProjectDemand> demands;
        if (status != null || priority != null || role != null || location != null || startDate != null || endDate != null) {
            demands = demandRepository.findWithFilters(status, priority, role, location, startDate, endDate);
        } else {
            demands = demandRepository.findAllWithSkills();
        }
        return demands.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DemandDto getDemandById(Long id) {
        ProjectDemand demand = demandRepository.findByIdWithSkills(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project demand not found with id: " + id));
        return mapToDto(demand);
    }

    public DemandDto updateDemandStatus(Long id, ProjectDemand.DemandStatus status) {
        ProjectDemand demand = demandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project demand not found with id: " + id));
        demand.setStatus(status);
        ProjectDemand updated = demandRepository.save(demand);
        return mapToDto(updated);
    }

    public DemandDto mapToDto(ProjectDemand demand) {
        DemandDto dto = new DemandDto();
        dto.setId(demand.getId());
        dto.setProjectName(demand.getProjectName());
        dto.setRole(demand.getRole());
        dto.setStartDate(demand.getStartDate());
        dto.setEndDate(demand.getEndDate());
        dto.setLocation(demand.getLocation());
        dto.setAllocationPercent(demand.getAllocationPercent());
        dto.setPriority(demand.getPriority());
        dto.setStatus(demand.getStatus());
        dto.setCreatedAt(demand.getCreatedAt());
        dto.setUpdatedAt(demand.getUpdatedAt());

        List<DemandSkill> skills = demand.getDemandSkills();
        if (skills != null) {
            dto.setRequiredSkills(skills.stream().map(ds -> new DemandSkillDto(
                    ds.getId(),
                    ds.getSkill().getId(),
                    ds.getSkill().getName(),
                    ds.getSkill().getCategory(),
                    ds.isMandatory(),
                    ds.getMinProficiency()
            )).collect(Collectors.toList()));
        }
        return dto;
    }
}
