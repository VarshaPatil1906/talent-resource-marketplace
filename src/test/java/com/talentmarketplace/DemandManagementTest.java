package com.talentmarketplace;

import com.talentmarketplace.dto.DemandDto;
import com.talentmarketplace.dto.DemandSkillDto;
import com.talentmarketplace.entity.ProjectDemand;
import com.talentmarketplace.entity.Skill;
import com.talentmarketplace.repository.ProjectDemandRepository;
import com.talentmarketplace.repository.SkillRepository;
import com.talentmarketplace.service.DemandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DemandManagementTest {

    @Autowired
    private DemandService demandService;

    @Autowired
    private ProjectDemandRepository demandRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Test
    @DisplayName("TC-06-01: Valid demand creation succeeds")
    void tc_06_01_validDemandCreationSucceeds() {
        DemandDto.CreateRequest request = new DemandDto.CreateRequest();
        request.setProjectName("NextGen Banking Core");
        request.setRole("Lead Backend Architect");
        request.setStartDate(LocalDate.of(2027, 1, 1));
        request.setEndDate(LocalDate.of(2027, 6, 30));
        request.setLocation("London");
        request.setAllocationPercent(100);
        request.setPriority(ProjectDemand.Priority.CRITICAL);
        request.setStatus(ProjectDemand.DemandStatus.OPEN);

        DemandSkillDto.Request skillReq = new DemandSkillDto.Request();
        skillReq.setSkillName("Java");
        skillReq.setMandatory(true);
        skillReq.setMinProficiency(4);
        request.setRequiredSkills(List.of(skillReq));

        DemandDto created = demandService.createDemand(request);

        assertNotNull(created.getId());
        assertEquals("NextGen Banking Core", created.getProjectName());
        assertEquals("Lead Backend Architect", created.getRole());
        assertEquals(ProjectDemand.Priority.CRITICAL, created.getPriority());
        assertEquals(ProjectDemand.DemandStatus.OPEN, created.getStatus());
        assertEquals(1, created.getRequiredSkills().size());
        assertEquals("Java", created.getRequiredSkills().get(0).getSkillName());
        assertTrue(created.getRequiredSkills().get(0).isMandatory());
        assertEquals(4, created.getRequiredSkills().get(0).getMinProficiency());
    }

    @Test
    @DisplayName("TC-06-02: Missing role or invalid dates are rejected")
    void tc_06_02_missingRoleOrInvalidDatesAreRejected() {
        // Test invalid dates: End date precedes start date
        DemandDto.CreateRequest invalidDatesRequest = new DemandDto.CreateRequest();
        invalidDatesRequest.setProjectName("Test Project");
        invalidDatesRequest.setRole("Developer");
        invalidDatesRequest.setStartDate(LocalDate.of(2027, 6, 30));
        invalidDatesRequest.setEndDate(LocalDate.of(2027, 1, 1)); // Invalid: end before start!
        invalidDatesRequest.setLocation("London");
        invalidDatesRequest.setAllocationPercent(100);

        assertThrows(IllegalArgumentException.class, () -> demandService.createDemand(invalidDatesRequest),
                "Should reject demand when end date precedes start date");

        // Test invalid allocation percentage: 0% or >100%
        DemandDto.CreateRequest invalidPercentRequest = new DemandDto.CreateRequest();
        invalidPercentRequest.setProjectName("Test Project 2");
        invalidPercentRequest.setRole("Developer");
        invalidPercentRequest.setStartDate(LocalDate.of(2027, 1, 1));
        invalidPercentRequest.setEndDate(LocalDate.of(2027, 3, 31));
        invalidPercentRequest.setLocation("London");
        invalidPercentRequest.setAllocationPercent(150); // Invalid: > 100%

        assertThrows(IllegalArgumentException.class, () -> demandService.createDemand(invalidPercentRequest),
                "Should reject demand when allocation percentage is > 100");
    }
}
