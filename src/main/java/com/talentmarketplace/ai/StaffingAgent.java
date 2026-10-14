package com.talentmarketplace.ai;

import com.talentmarketplace.dto.StaffingAdviceRequest;
import com.talentmarketplace.dto.StaffingAdviceResponse;

public interface StaffingAgent {
    StaffingAdviceResponse provideStaffingAdvice(StaffingAdviceRequest request);
}
