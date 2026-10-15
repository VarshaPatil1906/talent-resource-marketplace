package com.talentmarketplace;

import com.talentmarketplace.dto.AllocationDto;
import com.talentmarketplace.dto.DemandDto;
import com.talentmarketplace.dto.EmployeeDto;
import com.talentmarketplace.dto.EmployeeSkillDto;
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

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        restTemplate.getRestTemplate().setRequestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory());
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

        ResponseEntity<Map> response = restTemplate.withBasicAuth("recruiter", "password123").postForEntity(
                getBaseUrl() + "/api/demands", invalidReq, Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertTrue(response.getBody().containsKey("fieldErrors"));
        List fieldErrors = (List) response.getBody().get("fieldErrors");
        assertFalse(fieldErrors.isEmpty(), "Field errors list must contain validation failures");
    }

    @Test
    @DisplayName("API Filter: Filter employees by status INACTIVE returns only inactive employees")
    void testFilterEmployeesByStatusInactive() {
        ResponseEntity<List> response = restTemplate.getForEntity(
                getBaseUrl() + "/api/employees?status=INACTIVE", List.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty(), "Must return at least one inactive employee");
        for (Object item : response.getBody()) {
            Map map = (Map) item;
            assertEquals("INACTIVE", map.get("employmentStatus"),
                    "All employees returned with status=INACTIVE must be INACTIVE");
        }
    }

    @Test
    @DisplayName("API Filter: Filter employees by status ACTIVE returns only active employees")
    void testFilterEmployeesByStatusActive() {
        ResponseEntity<List> response = restTemplate.getForEntity(
                getBaseUrl() + "/api/employees?status=ACTIVE", List.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty(), "Must return active employees");
        for (Object item : response.getBody()) {
            Map map = (Map) item;
            assertEquals("ACTIVE", map.get("employmentStatus"),
                    "All employees returned with status=ACTIVE must be ACTIVE");
        }
    }

    @Test
    @DisplayName("API Filter: Filter employees by status ON_LEAVE returns only on-leave employees")
    void testFilterEmployeesByStatusOnLeave() {
        ResponseEntity<List> response = restTemplate.getForEntity(
                getBaseUrl() + "/api/employees?status=ON_LEAVE", List.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty(), "Must return at least one on-leave employee");
        for (Object item : response.getBody()) {
            Map map = (Map) item;
            assertEquals("ON_LEAVE", map.get("employmentStatus"),
                    "All employees returned with status=ON_LEAVE must be ON_LEAVE");
        }
    }

    @Test
    @DisplayName("API Filter: Filter employees by status ALL returns all employees regardless of status")
    void testFilterEmployeesByStatusAll() {
        ResponseEntity<List> response = restTemplate.getForEntity(
                getBaseUrl() + "/api/employees?status=ALL", List.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 10, "Status=ALL must return all employees");

        boolean hasActive = response.getBody().stream().anyMatch(e -> "ACTIVE".equals(((Map) e).get("employmentStatus")));
        boolean hasInactive = response.getBody().stream().anyMatch(e -> "INACTIVE".equals(((Map) e).get("employmentStatus")));
        boolean hasOnLeave = response.getBody().stream().anyMatch(e -> "ON_LEAVE".equals(((Map) e).get("employmentStatus")));

        assertTrue(hasActive, "Status=ALL must include ACTIVE employees");
        assertTrue(hasInactive, "Status=ALL must include INACTIVE employees");
        assertTrue(hasOnLeave, "Status=ALL must include ON_LEAVE employees");
    }

    @Test
    @DisplayName("API Filter: Combined filter by Department AND Status simultaneously")
    void testCombinedFilterDepartmentAndStatus() {
        // Engineering + INACTIVE -> Only Ian Wright
        ResponseEntity<List> engInactiveResp = restTemplate.getForEntity(
                getBaseUrl() + "/api/employees?department=Engineering&status=INACTIVE", List.class
        );
        assertEquals(HttpStatus.OK, engInactiveResp.getStatusCode());
        assertNotNull(engInactiveResp.getBody());
        assertEquals(1, engInactiveResp.getBody().size(), "Only Ian Wright is INACTIVE in Engineering");
        Map ian = (Map) engInactiveResp.getBody().get(0);
        assertEquals("Engineering", ian.get("department"));
        assertEquals("INACTIVE", ian.get("employmentStatus"));
        assertEquals("Ian Wright", ian.get("name"));

        // Engineering + ON_LEAVE -> Only Grace Hopper
        ResponseEntity<List> engLeaveResp = restTemplate.getForEntity(
                getBaseUrl() + "/api/employees?department=Engineering&status=ON_LEAVE", List.class
        );
        assertEquals(HttpStatus.OK, engLeaveResp.getStatusCode());
        assertNotNull(engLeaveResp.getBody());
        assertEquals(1, engLeaveResp.getBody().size(), "Only Grace Hopper is ON_LEAVE in Engineering");
        Map grace = (Map) engLeaveResp.getBody().get(0);
        assertEquals("Engineering", grace.get("department"));
        assertEquals("ON_LEAVE", grace.get("employmentStatus"));
        assertEquals("Grace Hopper", grace.get("name"));

        // Engineering + ACTIVE -> Alice, Bob, Elena, Frank (4 employees)
        ResponseEntity<List> engActiveResp = restTemplate.getForEntity(
                getBaseUrl() + "/api/employees?department=Engineering&status=ACTIVE", List.class
        );
        assertEquals(HttpStatus.OK, engActiveResp.getStatusCode());
        assertNotNull(engActiveResp.getBody());
        assertTrue(engActiveResp.getBody().size() >= 4, "Must return active engineering employees");
        for (Object item : engActiveResp.getBody()) {
            Map map = (Map) item;
            assertEquals("Engineering", map.get("department"));
            assertEquals("ACTIVE", map.get("employmentStatus"));
        }
    }

    @Test
    @DisplayName("Skills Matrix: Updating proficiency level only persists new level and preserves existing validation date")
    void testUpdateSkillAssessment_ProficiencyLevelOnly() {
        TestRestTemplate managerClient = restTemplate.withBasicAuth("manager", "password123");

        // 1. Fetch employee 1 (Alice Johnson) before update
        ResponseEntity<EmployeeDto> beforeResp = managerClient.getForEntity(
                getBaseUrl() + "/api/employees/1", EmployeeDto.class
        );
        assertEquals(HttpStatus.OK, beforeResp.getStatusCode());
        assertNotNull(beforeResp.getBody());
        EmployeeSkillDto initialJavaSkill = beforeResp.getBody().getSkills().stream()
                .filter(s -> "Java".equalsIgnoreCase(s.getSkillName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alice must have initial Java skill"));

        LocalDate initialDate = initialJavaSkill.getLastValidatedAt();
        assertNotNull(initialDate, "Alice's Java skill should have an initial validation date");

        // 2. Change proficiency level only (e.g. from 5 to 4 or 3)
        int newProficiency = (initialJavaSkill.getProficiencyLevel() != null && initialJavaSkill.getProficiencyLevel() == 4) ? 3 : 4;
        EmployeeSkillDto.AddRequest updateReq = new EmployeeSkillDto.AddRequest();
        updateReq.setSkillId(initialJavaSkill.getSkillId());
        updateReq.setProficiencyLevel(newProficiency);
        // lastValidatedAt is intentionally omitted / null

        ResponseEntity<EmployeeSkillDto> updateResp = managerClient.postForEntity(
                getBaseUrl() + "/api/employees/1/skills", updateReq, EmployeeSkillDto.class
        );
        assertEquals(HttpStatus.CREATED, updateResp.getStatusCode());
        assertNotNull(updateResp.getBody());
        assertEquals(newProficiency, updateResp.getBody().getProficiencyLevel());
        assertEquals(initialDate, updateResp.getBody().getLastValidatedAt(),
                "Updating proficiency only must preserve the existing validation date");

        // 3. Re-fetch employee 1 and verify persistence
        ResponseEntity<EmployeeDto> afterResp = managerClient.getForEntity(
                getBaseUrl() + "/api/employees/1", EmployeeDto.class
        );
        assertEquals(HttpStatus.OK, afterResp.getStatusCode());
        EmployeeSkillDto persistedJava = afterResp.getBody().getSkills().stream()
                .filter(s -> "Java".equalsIgnoreCase(s.getSkillName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alice must have Java skill"));

        assertEquals(newProficiency, persistedJava.getProficiencyLevel(),
                "Persisted proficiency level must match the updated value upon refetch");
        assertEquals(initialDate, persistedJava.getLastValidatedAt(),
                "Persisted validation date must remain unchanged upon refetch");
    }

    @Test
    @DisplayName("Skills Matrix: Updating assessment date only persists new date and preserves existing proficiency level")
    void testUpdateSkillAssessment_ValidationDateOnly() {
        TestRestTemplate managerClient = restTemplate.withBasicAuth("manager", "password123");

        // 1. Fetch employee 1 before update
        ResponseEntity<EmployeeDto> beforeResp = managerClient.getForEntity(
                getBaseUrl() + "/api/employees/1", EmployeeDto.class
        );
        assertEquals(HttpStatus.OK, beforeResp.getStatusCode());
        EmployeeSkillDto currentJava = beforeResp.getBody().getSkills().stream()
                .filter(s -> "Java".equalsIgnoreCase(s.getSkillName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alice must have Java skill"));

        Integer currentProficiency = currentJava.getProficiencyLevel();
        assertNotNull(currentProficiency, "Existing proficiency level must not be null before test");

        // 2. Change assessment validation date only
        LocalDate newDate = LocalDate.of(2026, 11, 28);
        EmployeeSkillDto.AddRequest updateReq = new EmployeeSkillDto.AddRequest();
        updateReq.setSkillId(currentJava.getSkillId());
        updateReq.setLastValidatedAt(newDate);
        // proficiencyLevel is intentionally omitted / null

        ResponseEntity<EmployeeSkillDto> updateResp = managerClient.postForEntity(
                getBaseUrl() + "/api/employees/1/skills", updateReq, EmployeeSkillDto.class
        );
        assertEquals(HttpStatus.CREATED, updateResp.getStatusCode());
        assertNotNull(updateResp.getBody());
        assertEquals(newDate, updateResp.getBody().getLastValidatedAt());
        assertEquals(currentProficiency, updateResp.getBody().getProficiencyLevel(),
                "Updating validation date only must preserve the existing proficiency level");

        // 3. Re-fetch employee 1 and verify persistence
        ResponseEntity<EmployeeDto> afterResp = managerClient.getForEntity(
                getBaseUrl() + "/api/employees/1", EmployeeDto.class
        );
        assertEquals(HttpStatus.OK, afterResp.getStatusCode());
        EmployeeSkillDto persistedJava = afterResp.getBody().getSkills().stream()
                .filter(s -> "Java".equalsIgnoreCase(s.getSkillName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alice must have Java skill"));

        assertEquals(newDate, persistedJava.getLastValidatedAt(),
                "Persisted validation date must match the updated value upon refetch");
        assertEquals(currentProficiency, persistedJava.getProficiencyLevel(),
                "Persisted proficiency level must remain unchanged upon refetch");
    }

    @Test
    @DisplayName("Skills Matrix: Updating both proficiency level and assessment date persists both values together without duplicate records")
    void testUpdateSkillAssessment_BothProficiencyAndDate() {
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");

        // 1. Fetch employee 1 before update
        ResponseEntity<EmployeeDto> beforeResp = adminClient.getForEntity(
                getBaseUrl() + "/api/employees/1", EmployeeDto.class
        );
        assertEquals(HttpStatus.OK, beforeResp.getStatusCode());
        int initialSkillCount = beforeResp.getBody().getSkills().size();
        EmployeeSkillDto currentJava = beforeResp.getBody().getSkills().stream()
                .filter(s -> "Java".equalsIgnoreCase(s.getSkillName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Alice must have Java skill"));

        // 2. Change both fields together
        int newProficiency = 5;
        LocalDate newDate = LocalDate.of(2026, 12, 15);
        EmployeeSkillDto.AddRequest updateReq = new EmployeeSkillDto.AddRequest();
        updateReq.setSkillId(currentJava.getSkillId());
        updateReq.setProficiencyLevel(newProficiency);
        updateReq.setLastValidatedAt(newDate);

        ResponseEntity<EmployeeSkillDto> updateResp = adminClient.postForEntity(
                getBaseUrl() + "/api/employees/1/skills", updateReq, EmployeeSkillDto.class
        );
        assertEquals(HttpStatus.CREATED, updateResp.getStatusCode());
        assertNotNull(updateResp.getBody());
        assertEquals(newProficiency, updateResp.getBody().getProficiencyLevel());
        assertEquals(newDate, updateResp.getBody().getLastValidatedAt());

        // 3. Re-fetch employee 1 and verify persistence
        ResponseEntity<EmployeeDto> afterResp = adminClient.getForEntity(
                getBaseUrl() + "/api/employees/1", EmployeeDto.class
        );
        assertEquals(HttpStatus.OK, afterResp.getStatusCode());
        assertEquals(initialSkillCount, afterResp.getBody().getSkills().size(),
                "Updating an existing skill assessment must not create duplicate records");

        List<EmployeeSkillDto> matchingJavaSkills = afterResp.getBody().getSkills().stream()
                .filter(s -> "Java".equalsIgnoreCase(s.getSkillName()))
                .toList();
        assertEquals(1, matchingJavaSkills.size(), "Must have exactly 1 Java assessment record");

        EmployeeSkillDto persistedJava = matchingJavaSkills.get(0);
        assertEquals(newProficiency, persistedJava.getProficiencyLevel(),
                "Persisted proficiency level must match new value");
        assertEquals(newDate, persistedJava.getLastValidatedAt(),
                "Persisted validation date must match new value");
    }
}
