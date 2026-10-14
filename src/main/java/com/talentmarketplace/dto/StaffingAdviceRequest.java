package com.talentmarketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class StaffingAdviceRequest {

    @NotNull(message = "Demand ID is required")
    private Long demandId;

    @NotBlank(message = "Goal cannot be blank")
    private String goal;

    public StaffingAdviceRequest() {}

    public StaffingAdviceRequest(Long demandId, String goal) {
        this.demandId = demandId;
        this.goal = goal;
    }

    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
}
