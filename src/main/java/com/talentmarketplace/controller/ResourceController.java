package com.talentmarketplace.controller;

import com.talentmarketplace.dto.ResourceAvailabilityDto;
import com.talentmarketplace.service.StaffingEngineService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final StaffingEngineService staffingEngineService;

    public ResourceController(StaffingEngineService staffingEngineService) {
        this.staffingEngineService = staffingEngineService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<ResourceAvailabilityDto>> getAvailableResources(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "1") Integer minCapacityPercent,
            @RequestParam(required = false) Long skillId,
            @RequestParam(required = false) Integer minProficiency,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String location) {
        List<ResourceAvailabilityDto> available = staffingEngineService.findAvailableResources(
                startDate, endDate, minCapacityPercent, skillId, minProficiency, department, location);
        return ResponseEntity.ok(available);
    }
}
