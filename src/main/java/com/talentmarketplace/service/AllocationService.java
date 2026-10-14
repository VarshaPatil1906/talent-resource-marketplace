package com.talentmarketplace.service;

import com.talentmarketplace.dto.AllocationDto;
import com.talentmarketplace.dto.AllocationHistoryDto;
import com.talentmarketplace.dto.ResourceAvailabilityDto;
import com.talentmarketplace.entity.*;
import com.talentmarketplace.exception.CapacityExceededException;
import com.talentmarketplace.exception.InvalidAllocationStateException;
import com.talentmarketplace.exception.ResourceNotFoundException;
import com.talentmarketplace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final AllocationHistoryRepository allocationHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectDemandRepository demandRepository;
    private final StaffingEngineService staffingEngineService;

    public AllocationService(AllocationRepository allocationRepository,
                             AllocationHistoryRepository allocationHistoryRepository,
                             EmployeeRepository employeeRepository,
                             ProjectDemandRepository demandRepository,
                             StaffingEngineService staffingEngineService) {
        this.allocationRepository = allocationRepository;
        this.allocationHistoryRepository = allocationHistoryRepository;
        this.employeeRepository = employeeRepository;
        this.demandRepository = demandRepository;
        this.staffingEngineService = staffingEngineService;
    }

    /**
     * Creates a new allocation in PENDING status.
     * Validates employee and demand existence, dates, and allocation percentage.
     */
    @Transactional
    public AllocationDto createAllocationRequest(AllocationDto.Request request, String createdBy) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot precede start date");
        }
        if (request.getAllocationPercent() <= 0 || request.getAllocationPercent() > 100) {
            throw new IllegalArgumentException("Allocation percent must be between 1 and 100");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        ProjectDemand demand = demandRepository.findById(request.getDemandId())
                .orElseThrow(() -> new ResourceNotFoundException("Demand not found with id: " + request.getDemandId()));

        if (demand.getStatus() == ProjectDemand.DemandStatus.CANCELLED) {
            throw new InvalidAllocationStateException("Cannot allocate to a CANCELLED project demand");
        }

        Allocation allocation = new Allocation(
                employee,
                demand,
                request.getStartDate(),
                request.getEndDate(),
                request.getAllocationPercent(),
                Allocation.AllocationStatus.PENDING,
                null
        );

        Allocation saved = allocationRepository.save(allocation);

        // Record audit history
        String user = (createdBy != null && !createdBy.isBlank()) ? createdBy : "system";
        AllocationHistory history = new AllocationHistory(
                saved,
                AllocationHistory.ActionType.CREATED,
                user,
                "Allocation request created in PENDING status for " + saved.getAllocationPercent() + "%"
        );
        allocationHistoryRepository.save(history);

        return mapToDto(saved);
    }

    /**
     * Approves an allocation request.
     * PROTECTED BY TRANSACTION & PESSIMISTIC LOCKING:
     * Obtains a pessimistic write lock on the employee row to ensure no concurrent
     * transactions can commit overlapping allocations that exceed capacity.
     */
    @Transactional
    public AllocationDto approveAllocation(Long allocationId, String approvedBy) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + allocationId));

        if (allocation.getStatus() != Allocation.AllocationStatus.PENDING) {
            throw new InvalidAllocationStateException("Only PENDING allocations can be approved. Current status: " + allocation.getStatus());
        }

        // PESSIMISTIC LOCK: Lock the employee row for update to prevent concurrent allocation race conditions
        Long employeeId = allocation.getEmployee().getId();
        Employee lockedEmployee = employeeRepository.findByIdWithPessimisticLock(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        // Touch and flush locked employee row to ensure exclusive database lock across transactions
        lockedEmployee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.saveAndFlush(lockedEmployee);

        // Revalidate employee status
        if (lockedEmployee.getEmploymentStatus() != Employee.EmploymentStatus.ACTIVE) {
            throw new InvalidAllocationStateException("Employee " + lockedEmployee.getEmployeeCode() + " is " + lockedEmployee.getEmploymentStatus() + " and cannot be allocated.");
        }

        // Revalidate demand status
        ProjectDemand demand = demandRepository.findById(allocation.getDemand().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Demand not found with id: " + allocation.getDemand().getId()));

        if (demand.getStatus() != ProjectDemand.DemandStatus.OPEN) {
            throw new InvalidAllocationStateException("Demand is not OPEN. Current status: " + demand.getStatus());
        }

        // Revalidate capacity at commit time
        ResourceAvailabilityDto availability = staffingEngineService.calculateAvailability(
                employeeId,
                allocation.getStartDate(),
                allocation.getEndDate(),
                allocation.getAllocationPercent()
        );

        if (!availability.isFullyAvailable()) {
            throw new CapacityExceededException("Cannot approve allocation: Employee " + lockedEmployee.getEmployeeCode()
                    + " has only " + availability.getMinAvailableCapacityPercent() + "% available capacity during requested period, but "
                    + allocation.getAllocationPercent() + "% is required.");
        }

        // Commit approval
        String manager = (approvedBy != null && !approvedBy.isBlank()) ? approvedBy : "manager";
        allocation.setStatus(Allocation.AllocationStatus.APPROVED);
        allocation.setApprovedBy(manager);
        allocation.setUpdatedAt(LocalDateTime.now());
        Allocation saved = allocationRepository.saveAndFlush(allocation);

        // Update demand status to STAFFED if appropriate
        if (allocation.getAllocationPercent().equals(demand.getAllocationPercent())) {
            demand.setStatus(ProjectDemand.DemandStatus.STAFFED);
            demandRepository.save(demand);
        }

        // Record audit history
        AllocationHistory history = new AllocationHistory(
                saved,
                AllocationHistory.ActionType.APPROVED,
                manager,
                "Allocation approved by " + manager + " for " + saved.getAllocationPercent() + "% capacity."
        );
        allocationHistoryRepository.save(history);

        return mapToDto(saved);
    }

    /**
     * Rejects a pending allocation request.
     */
    @Transactional
    public AllocationDto rejectAllocation(Long allocationId, String rejectedBy, String reason) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + allocationId));

        if (allocation.getStatus() != Allocation.AllocationStatus.PENDING) {
            throw new InvalidAllocationStateException("Only PENDING allocations can be rejected. Current status: " + allocation.getStatus());
        }

        allocation.setStatus(Allocation.AllocationStatus.CANCELLED);
        allocation.setUpdatedAt(LocalDateTime.now());
        Allocation saved = allocationRepository.save(allocation);

        String user = (rejectedBy != null && !rejectedBy.isBlank()) ? rejectedBy : "manager";
        String details = "Allocation rejected by " + user + (reason != null ? ": " + reason : "");
        AllocationHistory history = new AllocationHistory(
                saved,
                AllocationHistory.ActionType.REJECTED,
                user,
                details
        );
        allocationHistoryRepository.save(history);

        return mapToDto(saved);
    }

    /**
     * Cancels an existing allocation (releasing capacity) and retains complete audit history.
     */
    @Transactional
    public AllocationDto cancelAllocation(Long allocationId, String cancelledBy, String reason) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + allocationId));

        if (allocation.getStatus() == Allocation.AllocationStatus.CANCELLED) {
            throw new InvalidAllocationStateException("Allocation is already CANCELLED");
        }

        Allocation.AllocationStatus previousStatus = allocation.getStatus();
        allocation.setStatus(Allocation.AllocationStatus.CANCELLED);
        allocation.setUpdatedAt(LocalDateTime.now());
        Allocation saved = allocationRepository.save(allocation);

        // If the demand was STAFFED, return it to OPEN
        ProjectDemand demand = saved.getDemand();
        if (demand != null && demand.getStatus() == ProjectDemand.DemandStatus.STAFFED) {
            demand.setStatus(ProjectDemand.DemandStatus.OPEN);
            demandRepository.save(demand);
        }

        // Record audit history
        String user = (cancelledBy != null && !cancelledBy.isBlank()) ? cancelledBy : "manager";
        String details = "Allocation cancelled from " + previousStatus + " by " + user
                + ". Capacity of " + saved.getAllocationPercent() + "% released back to pool."
                + (reason != null ? " Reason: " + reason : "");

        AllocationHistory history = new AllocationHistory(
                saved,
                AllocationHistory.ActionType.CANCELLED,
                user,
                details
        );
        allocationHistoryRepository.save(history);

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AllocationDto> getAllAllocations(Allocation.AllocationStatus status, Long employeeId, Long demandId) {
        List<Allocation> list = allocationRepository.findAllWithDetails();
        return list.stream()
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> employeeId == null || a.getEmployee().getId().equals(employeeId))
                .filter(a -> demandId == null || a.getDemand().getId().equals(demandId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AllocationDto getAllocationById(Long id) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + id));
        return mapToDto(allocation);
    }

    @Transactional(readOnly = true)
    public List<AllocationHistoryDto> getAllocationHistory(Long allocationId) {
        if (!allocationRepository.existsById(allocationId)) {
            throw new ResourceNotFoundException("Allocation not found with id: " + allocationId);
        }
        return allocationHistoryRepository.findByAllocationIdOrderByTimestampDesc(allocationId).stream()
                .map(h -> new AllocationHistoryDto(
                        h.getId(),
                        h.getAllocation().getId(),
                        h.getAction(),
                        h.getChangedBy(),
                        h.getTimestamp(),
                        h.getDetails()
                ))
                .collect(Collectors.toList());
    }

    public AllocationDto mapToDto(Allocation allocation) {
        AllocationDto dto = new AllocationDto();
        dto.setId(allocation.getId());
        dto.setEmployeeId(allocation.getEmployee().getId());
        dto.setEmployeeCode(allocation.getEmployee().getEmployeeCode());
        dto.setEmployeeName(allocation.getEmployee().getName());
        dto.setDemandId(allocation.getDemand().getId());
        dto.setProjectName(allocation.getDemand().getProjectName());
        dto.setRole(allocation.getDemand().getRole());
        dto.setStartDate(allocation.getStartDate());
        dto.setEndDate(allocation.getEndDate());
        dto.setAllocationPercent(allocation.getAllocationPercent());
        dto.setStatus(allocation.getStatus());
        dto.setApprovedBy(allocation.getApprovedBy());
        dto.setCreatedAt(allocation.getCreatedAt());
        dto.setUpdatedAt(allocation.getUpdatedAt());
        return dto;
    }
}
