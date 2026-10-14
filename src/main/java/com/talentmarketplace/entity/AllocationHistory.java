package com.talentmarketplace.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "allocation_history", indexes = {
    @Index(name = "idx_alloc_hist_alloc", columnList = "allocation_id"),
    @Index(name = "idx_alloc_hist_action", columnList = "action"),
    @Index(name = "idx_alloc_hist_timestamp", columnList = "timestamp")
})
public class AllocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "allocation_id", nullable = false)
    private Allocation allocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private ActionType action;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "details", length = 1000)
    private String details;

    public AllocationHistory() {}

    public AllocationHistory(Allocation allocation, ActionType action, String changedBy, String details) {
        this.allocation = allocation;
        this.action = action;
        this.changedBy = changedBy;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    public enum ActionType {
        CREATED,
        APPROVED,
        REJECTED,
        CANCELLED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Allocation getAllocation() { return allocation; }
    public void setAllocation(Allocation allocation) { this.allocation = allocation; }

    public ActionType getAction() { return action; }
    public void setAction(ActionType action) { this.action = action; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
