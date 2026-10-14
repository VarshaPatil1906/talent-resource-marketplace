package com.talentmarketplace;

import com.talentmarketplace.dto.AllocationDto;
import com.talentmarketplace.dto.AllocationHistoryDto;
import com.talentmarketplace.dto.ResourceAvailabilityDto;
import com.talentmarketplace.entity.*;
import com.talentmarketplace.exception.CapacityExceededException;
import com.talentmarketplace.exception.InvalidAllocationStateException;
import com.talentmarketplace.repository.*;
import com.talentmarketplace.service.AllocationService;
import com.talentmarketplace.service.StaffingEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AllocationApprovalAndConcurrencyTest {

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private StaffingEngineService staffingEngineService;

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private AllocationHistoryRepository allocationHistoryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectDemandRepository demandRepository;

    @Test
    @Transactional
    @DisplayName("TC-06-07: Approved allocation is persisted and capacity is recalculated")
    void tc_06_07_approvedAllocationIsPersistedAndCapacityRecalculated() {
        Employee emp = employeeRepository.findByEmployeeCode("EMP003").orElseThrow(); // Carlos Mendez
        ProjectDemand demand = demandRepository.findAll().get(0);

        // Verify initial available capacity is 100% (since Carlos's prior allocation was cancelled)
        ResourceAvailabilityDto initialAvail = staffingEngineService.calculateAvailability(
                emp.getId(), LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 28), 50
        );
        assertEquals(100, initialAvail.getMinAvailableCapacityPercent());

        // 1. Submit allocation request (creates PENDING)
        AllocationDto.Request request = new AllocationDto.Request(
                emp.getId(), demand.getId(),
                LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 28),
                60
        );
        AllocationDto pending = allocationService.createAllocationRequest(request, "recruiter");
        assertEquals(Allocation.AllocationStatus.PENDING, pending.getStatus());

        // PENDING allocation must NOT reduce committed capacity before approval
        ResourceAvailabilityDto pendingAvail = staffingEngineService.calculateAvailability(
                emp.getId(), LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 28), 50
        );
        assertEquals(100, pendingAvail.getMinAvailableCapacityPercent(), "Pending request must not reduce committed capacity");

        // 2. Approve allocation (Manager commits)
        AllocationDto approved = allocationService.approveAllocation(pending.getId(), "manager");
        assertEquals(Allocation.AllocationStatus.APPROVED, approved.getStatus());
        assertEquals("manager", approved.getApprovedBy());

        // 3. Verify capacity is now recalculated (100 - 60 = 40% remaining)
        ResourceAvailabilityDto postApproveAvail = staffingEngineService.calculateAvailability(
                emp.getId(), LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 28), 50
        );
        assertEquals(40, postApproveAvail.getMinAvailableCapacityPercent());
        assertEquals(60, postApproveAvail.getMaxConcurrentCommittedPercent());
    }

    @Test
    @Transactional
    @DisplayName("TC-06-08: Allocation exceeding capacity is rejected")
    void tc_06_08_allocationExceedingCapacityIsRejected() {
        // Frank Miller (EMP006) already has 50% approved allocation from 2026-11-01 to 2027-01-31
        Employee frank = employeeRepository.findByEmployeeCode("EMP006").orElseThrow();
        ProjectDemand demand = demandRepository.findAll().get(0);

        // Create a pending request asking for 60% (50% + 60% = 110% > 100%)
        AllocationDto.Request req = new AllocationDto.Request(
                frank.getId(), demand.getId(),
                LocalDate.of(2026, 11, 15), LocalDate.of(2026, 12, 15),
                60
        );
        AllocationDto pending = allocationService.createAllocationRequest(req, "recruiter");

        // When manager attempts to approve, commit-time validation MUST reject it
        assertThrows(CapacityExceededException.class, () ->
                allocationService.approveAllocation(pending.getId(), "manager"),
                "Should reject approval when concurrent allocation exceeds 100% capacity"
        );
    }

    @Test
    @DisplayName("TC-06-11: Concurrent conflicting allocation requests cannot both exceed capacity")
    void tc_06_11_concurrentConflictingAllocationRequestsCannotBothExceedCapacity() throws Exception {
        // Use Employee EMP010 (Julia Chen) who has 100% capacity in March 2027
        Employee julia = employeeRepository.findByEmployeeCode("EMP010").orElseThrow();
        ProjectDemand demand = demandRepository.findAll().get(0);

        LocalDate start = LocalDate.of(2027, 3, 1);
        LocalDate end = LocalDate.of(2027, 3, 31);

        // Create two PENDING allocation requests each asking for 70% (70 + 70 = 140% > 100%)
        AllocationDto.Request req1 = new AllocationDto.Request(julia.getId(), demand.getId(), start, end, 70);
        AllocationDto.Request req2 = new AllocationDto.Request(julia.getId(), demand.getId(), start, end, 70);

        AllocationDto pending1 = allocationService.createAllocationRequest(req1, "recruiter1");
        AllocationDto pending2 = allocationService.createAllocationRequest(req2, "recruiter2");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger capacityExceededCount = new AtomicInteger(0);

        Callable<Void> task1 = () -> {
            try {
                barrier.await();
                allocationService.approveAllocation(pending1.getId(), "manager1");
                successCount.incrementAndGet();
            } catch (CapacityExceededException e) {
                capacityExceededCount.incrementAndGet();
            } catch (Exception e) {
                // other exceptions if any
            }
            return null;
        };

        Callable<Void> task2 = () -> {
            try {
                barrier.await();
                allocationService.approveAllocation(pending2.getId(), "manager2");
                successCount.incrementAndGet();
            } catch (CapacityExceededException e) {
                capacityExceededCount.incrementAndGet();
            } catch (Exception e) {
                // other exceptions if any
            }
            return null;
        };

        List<Future<Void>> futures = executor.invokeAll(List.of(task1, task2));
        for (Future<Void> f : futures) {
            f.get();
        }
        executor.shutdown();

        // Exactly one should succeed, and the conflicting concurrent one MUST be rejected!
        assertEquals(1, successCount.get(), "Exactly one concurrent 70% request should be approved");
        assertEquals(1, capacityExceededCount.get(), "The concurrent conflicting request must be rejected with CapacityExceededException");

        // Verify that total committed capacity in that period does not exceed 100%
        ResourceAvailabilityDto finalAvail = staffingEngineService.calculateAvailability(julia.getId(), start, end, 10);
        assertTrue(finalAvail.getMaxConcurrentCommittedPercent() <= 100, "Committed capacity must never exceed 100%");
    }

    @Test
    @Transactional
    @DisplayName("TC-06-12: Cancellation releases capacity and preserves allocation history")
    void tc_06_12_cancellationReleasesCapacityAndPreservesHistory() {
        Employee emp = employeeRepository.findByEmployeeCode("EMP005").orElseThrow(); // Elena Rostova
        ProjectDemand demand = demandRepository.findAll().get(0);

        // Elena already has an approved 50% allocation (Allocation 1)
        Allocation alloc = allocationRepository.findAll().stream()
                .filter(a -> a.getEmployee().getId().equals(emp.getId()) && a.getStatus() == Allocation.AllocationStatus.APPROVED)
                .findFirst().orElseThrow();

        Long allocId = alloc.getId();
        int initialHistoryCount = allocationHistoryRepository.findByAllocationIdOrderByTimestampDesc(allocId).size();

        // Cancel the allocation
        AllocationDto cancelled = allocationService.cancelAllocation(allocId, "manager", "Client deferred feature release");
        assertEquals(Allocation.AllocationStatus.CANCELLED, cancelled.getStatus());

        // Verify capacity is released back to the talent pool
        ResourceAvailabilityDto availAfterCancel = staffingEngineService.calculateAvailability(
                emp.getId(), alloc.getStartDate(), alloc.getEndDate(), 100
        );
        assertEquals(100, availAfterCancel.getMinAvailableCapacityPercent(), "Cancelled allocation must release all capacity back to pool");

        // Verify history is preserved and contains CANCELLED event
        List<AllocationHistoryDto> history = allocationService.getAllocationHistory(allocId);
        assertTrue(history.size() > initialHistoryCount, "History audit log must be expanded with cancellation record");
        assertEquals(AllocationHistory.ActionType.CANCELLED, history.get(0).getAction());
        assertTrue(history.get(0).getDetails().contains("Capacity of 50% released back to pool"));
    }

    @Test
    @Transactional
    @DisplayName("Stale approval is rejected when demand is already staffed or cancelled")
    void staleApprovalIsRejected() {
        Employee emp = employeeRepository.findByEmployeeCode("EMP001").orElseThrow();
        ProjectDemand demand = demandRepository.findAll().get(0);

        AllocationDto pending = allocationService.createAllocationRequest(
                new AllocationDto.Request(emp.getId(), demand.getId(), LocalDate.of(2027, 5, 1), LocalDate.of(2027, 5, 31), 50),
                "recruiter"
        );

        // Mark demand as CANCELLED
        demand.setStatus(ProjectDemand.DemandStatus.CANCELLED);
        demandRepository.save(demand);

        // Attempting to approve must fail
        assertThrows(InvalidAllocationStateException.class, () ->
                allocationService.approveAllocation(pending.getId(), "manager"));
    }
}
