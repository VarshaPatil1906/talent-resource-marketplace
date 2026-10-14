package com.talentmarketplace.controller;

import com.talentmarketplace.dto.AllocationDto;
import com.talentmarketplace.dto.AllocationHistoryDto;
import com.talentmarketplace.entity.Allocation;
import com.talentmarketplace.service.AllocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allocations")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping
    public ResponseEntity<AllocationDto> createAllocation(
            @Valid @RequestBody AllocationDto.Request request,
            Authentication authentication) {
        String user = (authentication != null) ? authentication.getName() : "recruiter";
        AllocationDto created = allocationService.createAllocationRequest(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<AllocationDto>> getAllAllocations(
            @RequestParam(required = false) Allocation.AllocationStatus status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long demandId) {
        List<AllocationDto> list = allocationService.getAllAllocations(status, employeeId, demandId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AllocationDto> getAllocationById(@PathVariable Long id) {
        AllocationDto allocation = allocationService.getAllocationById(id);
        return ResponseEntity.ok(allocation);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AllocationDto> approveAllocation(
            @PathVariable Long id,
            Authentication authentication) {
        String manager = (authentication != null) ? authentication.getName() : "manager";
        AllocationDto approved = allocationService.approveAllocation(id, manager);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AllocationDto> rejectAllocation(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        String manager = (authentication != null) ? authentication.getName() : "manager";
        String reason = (body != null) ? body.get("reason") : null;
        AllocationDto rejected = allocationService.rejectAllocation(id, manager, reason);
        return ResponseEntity.ok(rejected);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AllocationDto> cancelAllocation(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        String manager = (authentication != null) ? authentication.getName() : "manager";
        String reason = (body != null) ? body.get("reason") : null;
        AllocationDto cancelled = allocationService.cancelAllocation(id, manager, reason);
        return ResponseEntity.ok(cancelled);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<AllocationHistoryDto>> getAllocationHistory(@PathVariable Long id) {
        List<AllocationHistoryDto> history = allocationService.getAllocationHistory(id);
        return ResponseEntity.ok(history);
    }
}
