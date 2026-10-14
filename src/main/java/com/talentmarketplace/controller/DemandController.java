package com.talentmarketplace.controller;

import com.talentmarketplace.dto.CandidateRecommendationDto;
import com.talentmarketplace.dto.DemandDto;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.service.DemandService;
import com.talentmarketplace.service.StaffingEngineService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/demands")
public class DemandController {

    private final DemandService demandService;
    private final StaffingEngineService staffingEngineService;

    public DemandController(DemandService demandService, StaffingEngineService staffingEngineService) {
        this.demandService = demandService;
        this.staffingEngineService = staffingEngineService;
    }

    @PostMapping
    public ResponseEntity<DemandDto> createDemand(@Valid @RequestBody DemandDto.CreateRequest request) {
        DemandDto created = demandService.createDemand(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<DemandDto>> getAllDemands(
            @RequestParam(required = false) ProjectDemand.DemandStatus status,
            @RequestParam(required = false) ProjectDemand.Priority priority,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DemandDto> demands = demandService.getAllDemands(status, priority, role, location, startDate, endDate);
        return ResponseEntity.ok(demands);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandDto> getDemandById(@PathVariable Long id) {
        DemandDto demand = demandService.getDemandById(id);
        return ResponseEntity.ok(demand);
    }

    @PostMapping("/{id}/recommendations")
    public ResponseEntity<List<CandidateRecommendationDto>> getRecommendationsPost(@PathVariable Long id) {
        List<CandidateRecommendationDto> recs = staffingEngineService.getRecommendationsForDemand(id);
        return ResponseEntity.ok(recs);
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<List<CandidateRecommendationDto>> getRecommendationsGet(@PathVariable Long id) {
        List<CandidateRecommendationDto> recs = staffingEngineService.getRecommendationsForDemand(id);
        return ResponseEntity.ok(recs);
    }
}
