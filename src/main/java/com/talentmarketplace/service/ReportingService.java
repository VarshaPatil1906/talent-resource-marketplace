package com.talentmarketplace.service;

import com.talentmarketplace.dto.CandidateRecommendationDto;
import com.talentmarketplace.dto.DemandDto;
import com.talentmarketplace.dto.TrainingRecommendationDto;
import com.talentmarketplace.dto.UtilizationReportDto;
import com.talentmarketplace.entity.*;
import com.talentmarketplace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportingService {

    private final EmployeeRepository employeeRepository;
    private final ProjectDemandRepository demandRepository;
    private final AllocationRepository allocationRepository;
    private final SkillRepository skillRepository;
    private final TrainingRecommendationRepository trainingRepository;
    private final StaffingEngineService staffingEngineService;
    private final DemandService demandService;

    public ReportingService(EmployeeRepository employeeRepository,
                            ProjectDemandRepository demandRepository,
                            AllocationRepository allocationRepository,
                            SkillRepository skillRepository,
                            TrainingRecommendationRepository trainingRepository,
                            StaffingEngineService staffingEngineService,
                            DemandService demandService) {
        this.employeeRepository = employeeRepository;
        this.demandRepository = demandRepository;
        this.allocationRepository = allocationRepository;
        this.skillRepository = skillRepository;
        this.trainingRepository = trainingRepository;
        this.staffingEngineService = staffingEngineService;
        this.demandService = demandService;
    }

    /**
     * Generates a comprehensive utilization report taking concurrent daily allocation into account.
     */
    public UtilizationReportDto getUtilizationReport(Long employeeId, String department, LocalDate startDate, LocalDate endDate) {
        LocalDate windowStart = startDate != null ? startDate : LocalDate.now();
        LocalDate windowEnd = endDate != null ? endDate : windowStart.plusDays(30);

        if (windowEnd.isBefore(windowStart)) {
            throw new IllegalArgumentException("End date cannot precede start date");
        }

        long totalDays = ChronoUnit.DAYS.between(windowStart, windowEnd) + 1;

        List<Employee> allEmployees = employeeRepository.findAll();
        List<Employee> activeEmployees = allEmployees.stream()
                .filter(e -> e.getEmploymentStatus() == Employee.EmploymentStatus.ACTIVE)
                .filter(e -> employeeId == null || e.getId().equals(employeeId))
                .filter(e -> department == null || department.isBlank() || e.getDepartment().equalsIgnoreCase(department.trim()))
                .collect(Collectors.toList());

        List<Allocation> approvedAllocations = allocationRepository.findAllOverlappingApprovedAllocations(windowStart, windowEnd);

        // Group allocations by employee
        Map<Long, List<Allocation>> allocsByEmp = approvedAllocations.stream()
                .collect(Collectors.groupingBy(a -> a.getEmployee().getId()));

        List<UtilizationReportDto.EmployeeUtilizationDto> empUtilList = new ArrayList<>();
        Map<String, List<Double>> deptUtils = new HashMap<>();
        double totalUtilizationSum = 0.0;

        for (Employee emp : activeEmployees) {
            List<Allocation> empAllocs = allocsByEmp.getOrDefault(emp.getId(), Collections.emptyList());

            // Compute concurrent daily utilization across window
            double dailySum = 0.0;
            int peakCommitted = 0;

            for (LocalDate d = windowStart; !d.isAfter(windowEnd); d = d.plusDays(1)) {
                int dailyCommitted = 0;
                for (Allocation a : empAllocs) {
                    if (!a.getStartDate().isAfter(d) && !a.getEndDate().isBefore(d)) {
                        dailyCommitted += a.getAllocationPercent();
                    }
                }
                dailySum += dailyCommitted;
                peakCommitted = Math.max(peakCommitted, dailyCommitted);
            }

            int avgCommittedPercent = (int) Math.round(dailySum / totalDays);
            int remainingPercent = Math.max(0, 100 - peakCommitted);

            empUtilList.add(new UtilizationReportDto.EmployeeUtilizationDto(
                    emp.getId(),
                    emp.getEmployeeCode(),
                    emp.getName(),
                    emp.getDepartment(),
                    emp.getLocation(),
                    avgCommittedPercent,
                    remainingPercent,
                    empAllocs.size()
            ));

            totalUtilizationSum += avgCommittedPercent;
            deptUtils.computeIfAbsent(emp.getDepartment(), k -> new ArrayList<>()).add((double) avgCommittedPercent);
        }

        // Aggregate Department Utilization
        List<UtilizationReportDto.DepartmentUtilizationDto> deptList = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : deptUtils.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            deptList.add(new UtilizationReportDto.DepartmentUtilizationDto(
                    entry.getKey(),
                    entry.getValue().size(),
                    Math.round(avg * 10.0) / 10.0
            ));
        }

        // Open Demands and Unstaffed Demands
        List<ProjectDemand> openDemands = demandRepository.findByStatus(ProjectDemand.DemandStatus.OPEN);
        List<DemandDto> unstaffedDemands = new ArrayList<>();

        for (ProjectDemand dem : openDemands) {
            List<CandidateRecommendationDto> recs = staffingEngineService.getRecommendationsForDemand(dem.getId());
            boolean hasEligible = recs.stream().anyMatch(CandidateRecommendationDto::isEligible);
            if (!hasEligible) {
                unstaffedDemands.add(demandService.mapToDto(dem));
            }
        }

        // Skill Gaps grouped by category
        List<UtilizationReportDto.SkillGapDto> skillGaps = calculateSkillGaps();

        UtilizationReportDto report = new UtilizationReportDto();
        report.setTotalEmployees(allEmployees.size());
        report.setActiveEmployees(activeEmployees.size());
        double overallAvg = activeEmployees.isEmpty() ? 0.0 : totalUtilizationSum / activeEmployees.size();
        report.setOverallUtilizationPercent(Math.round(overallAvg * 10.0) / 10.0);
        report.setTotalCommittedCapacityPercent((int) Math.round(totalUtilizationSum));
        report.setTotalAvailableCapacityPercent(Math.max(0, (activeEmployees.size() * 100) - (int) Math.round(totalUtilizationSum)));
        report.setOpenDemandsCount(openDemands.size());
        report.setUnstaffedDemandsCount(unstaffedDemands.size());
        report.setEmployeeUtilizations(empUtilList);
        report.setDepartmentUtilizations(deptList);
        report.setUnstaffedDemands(unstaffedDemands);
        report.setSkillGaps(skillGaps);

        return report;
    }

    /**
     * Calculates skill gaps across open demands vs active employees.
     */
    public List<UtilizationReportDto.SkillGapDto> calculateSkillGaps() {
        List<ProjectDemand> openDemands = demandRepository.findByStatus(ProjectDemand.DemandStatus.OPEN);
        List<Employee> activeEmployees = employeeRepository.findAllActiveWithSkills();

        Map<Long, Integer> requiredBySkill = new HashMap<>();
        Map<Long, Skill> skillMap = new HashMap<>();

        for (ProjectDemand d : openDemands) {
            if (d.getDemandSkills() != null) {
                for (DemandSkill ds : d.getDemandSkills()) {
                    if (ds.isMandatory()) {
                        requiredBySkill.merge(ds.getSkill().getId(), 1, Integer::sum);
                        skillMap.put(ds.getSkill().getId(), ds.getSkill());
                    }
                }
            }
        }

        List<UtilizationReportDto.SkillGapDto> gaps = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : requiredBySkill.entrySet()) {
            Long skillId = entry.getKey();
            int requiredCount = entry.getValue();
            Skill skill = skillMap.get(skillId);

            long qualifiedCount = activeEmployees.stream().filter(e -> {
                if (e.getEmployeeSkills() == null) return false;
                return e.getEmployeeSkills().stream().anyMatch(es ->
                        es.getSkill().getId().equals(skillId) &&
                        es.getProficiencyLevel() != null &&
                        es.getProficiencyLevel() >= 3);
            }).count();

            long gap = Math.max(0, requiredCount - qualifiedCount);

            gaps.add(new UtilizationReportDto.SkillGapDto(
                    skill.getCategory(),
                    skill.getName(),
                    requiredCount,
                    qualifiedCount,
                    gap
            ));
        }

        gaps.sort(Comparator.comparing(UtilizationReportDto.SkillGapDto::getGapCount).reversed()
                .thenComparing(UtilizationReportDto.SkillGapDto::getSkillName));

        return gaps;
    }

    @Transactional(readOnly = true)
    public List<TrainingRecommendationDto> getAllTrainingRecommendations() {
        return trainingRepository.findAllWithDetails().stream().map(tr -> new TrainingRecommendationDto(
                tr.getId(),
                tr.getEmployee().getId(),
                tr.getEmployee().getName(),
                tr.getSkill().getId(),
                tr.getSkill().getName(),
                tr.getReason(),
                tr.getLearningPath(),
                tr.getStatus(),
                tr.getCreatedAt()
        )).collect(Collectors.toList());
    }
}
