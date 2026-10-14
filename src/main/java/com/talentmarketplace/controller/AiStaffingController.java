package com.talentmarketplace.controller;

import com.talentmarketplace.ai.StaffingAgent;
import com.talentmarketplace.dto.StaffingAdviceRequest;
import com.talentmarketplace.dto.StaffingAdviceResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiStaffingController {

    private final StaffingAgent staffingAgent;

    public AiStaffingController(StaffingAgent staffingAgent) {
        this.staffingAgent = staffingAgent;
    }

    @PostMapping("/staffing-advice")
    public ResponseEntity<StaffingAdviceResponse> getStaffingAdvice(@Valid @RequestBody StaffingAdviceRequest request) {
        StaffingAdviceResponse response = staffingAgent.provideStaffingAdvice(request);
        return ResponseEntity.ok(response);
    }
}
