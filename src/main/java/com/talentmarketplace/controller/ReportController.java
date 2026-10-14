package com.talentmarketplace.controller;

import com.talentmarketplace.dto.TrainingRecommendationDto;
import com.talentmarketplace.dto.UtilizationReportDto;
import com.talentmarketplace.service.ReportingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportingService reportingService;

    public ReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/utilization")
    public ResponseEntity<UtilizationReportDto> getUtilization(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UtilizationReportDto report = reportingService.getUtilizationReport(employeeId, department, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/skill-gaps")
    public ResponseEntity<List<UtilizationReportDto.SkillGapDto>> getSkillGaps() {
        List<UtilizationReportDto.SkillGapDto> gaps = reportingService.calculateSkillGaps();
        return ResponseEntity.ok(gaps);
    }

    @GetMapping("/training-recommendations")
    public ResponseEntity<List<TrainingRecommendationDto>> getTrainingRecommendations() {
        List<TrainingRecommendationDto> list = reportingService.getAllTrainingRecommendations();
        return ResponseEntity.ok(list);
    }
}
