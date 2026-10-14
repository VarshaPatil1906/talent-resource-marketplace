package com.talentmarketplace.dto;

import com.talentmarketplace.entity.AllocationHistory;
import java.time.LocalDateTime;

public class AllocationHistoryDto {
    private Long id;
    private Long allocationId;
    private AllocationHistory.ActionType action;
    private String changedBy;
    private LocalDateTime timestamp;
    private String details;

    public AllocationHistoryDto() {}

    public AllocationHistoryDto(Long id, Long allocationId, AllocationHistory.ActionType action, String changedBy, LocalDateTime timestamp, String details) {
        this.id = id;
        this.allocationId = allocationId;
        this.action = action;
        this.changedBy = changedBy;
        this.timestamp = timestamp;
        this.details = details;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAllocationId() { return allocationId; }
    public void setAllocationId(Long allocationId) { this.allocationId = allocationId; }

    public AllocationHistory.ActionType getAction() { return action; }
    public void setAction(AllocationHistory.ActionType action) { this.action = action; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
