package com.talentmarketplace;

import com.talentmarketplace.dto.AllocationDto;
import com.talentmarketplace.dto.DemandDto;
import com.talentmarketplace.dto.EmployeeDto;
import com.talentmarketplace.dto.StaffingAdviceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    @DisplayName("API: Employees list returns seeded records")
    void testGetEmployees() {
        ResponseEntity<List> response = restTemplate.getForEntity(getBaseUrl() + "/api/employees", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 10);
    }

    @Test
    @DisplayName("API: Demands list returns seeded demands")
    void testGetDemands() {
        ResponseEntity<List> response = restTemplate.getForEntity(getBaseUrl() + "/api/demands", List.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 5);
    }

    @Test
    @DisplayName("API: Reports utilization endpoint returns correct summary")
    void testGetUtilizationReport() {
        ResponseEntity<Map> response = restTemplate.getForEntity(getBaseUrl() + "/api/reports/utilization", Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("overallUtilizationPercent"));
        assertTrue(response.getBody().containsKey("employeeUtilizations"));
        assertTrue(response.getBody().containsKey("skillGaps"));
    }

    @Test
    @DisplayName("API: AI staffing advice endpoint returns simulated agent response")
    void testAiStaffingAdviceApi() {
        StaffingAdviceRequest req = new StaffingAdviceRequest(1L, "Find best engineer");
        ResponseEntity<Map> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/ai/staffing-advice", req, Map.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody().get("simulatedAgent"));
        assertNotNull(response.getBody().get("topCandidate"));
        assertNotNull(response.getBody().get("strategicRationale"));
    }

    @Test
    @DisplayName("API: Unauthorized approval attempt is rejected with 401 or 403")
    void testUnauthorizedApprovalIsRejected() {
        // Calling approve without manager credentials must be rejected by Spring Security
        HttpHeaders headers = new HttpHeaders();
        // Unauthenticated request
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/allocations/3/approve", request, String.class
        );

        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED || response.getStatusCode() == HttpStatus.FORBIDDEN,
                "Unauthenticated or non-manager approval must be rejected (got " + response.getStatusCode() + ")");
    }

    @Test
    @DisplayName("API: Manager authorized approval succeeds")
    void testManagerAuthorizedApproval() {
        // Calling with manager credentials
        TestRestTemplate managerRestTemplate = restTemplate.withBasicAuth("manager", "password123");

        // Submit new pending allocation for Alice on Demand 2
        AllocationDto.Request req = new AllocationDto.Request(
                1L, 2L, LocalDate.of(2027, 4, 1), LocalDate.of(2027, 4, 30), 20
        );
        ResponseEntity<AllocationDto> createResp = managerRestTemplate.postForEntity(
                getBaseUrl() + "/api/allocations", req, AllocationDto.class
        );
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
        Long allocId = createResp.getBody().getId();

        // Approve it with manager role
        ResponseEntity<AllocationDto> approveResp = managerRestTemplate.postForEntity(
                getBaseUrl() + "/api/allocations/" + allocId + "/approve", null, AllocationDto.class
        );
        assertEquals(HttpStatus.OK, approveResp.getStatusCode());
        assertEquals("APPROVED", approveResp.getBody().getStatus().name());
        assertEquals("manager", approveResp.getBody().getApprovedBy());
    }

    @Test
    @DisplayName("API: Validation error returns structured 400 with field errors")
    void testValidationErrorResponseStructure() {
        DemandDto.CreateRequest invalidReq = new DemandDto.CreateRequest();
        // Leave required fields blank

        ResponseEntity<Map> response = restTemplate.postForEntity(
                getBaseUrl() + "/api/demands", invalidReq, Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("fieldErrors"));
        List fieldErrors = (List) response.getBody().get("fieldErrors");
        assertFalse(fieldErrors.isEmpty(), "Field errors list must contain validation failures");
    }
}
