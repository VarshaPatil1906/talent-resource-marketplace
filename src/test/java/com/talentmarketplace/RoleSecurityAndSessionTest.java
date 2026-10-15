package com.talentmarketplace;

import com.talentmarketplace.dto.AllocationDto;
import com.talentmarketplace.dto.AuthDto;
import com.talentmarketplace.dto.DemandDto;
import com.talentmarketplace.dto.SkillDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoleSecurityAndSessionTest {

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
    @DisplayName("Security: Recruiter is rejected with 403 Forbidden when attempting to approve allocation")
    void testRecruiterCannotApproveAllocation() {
        TestRestTemplate recruiterClient = restTemplate.withBasicAuth("recruiter", "password123");

        ResponseEntity<String> response = recruiterClient.postForEntity(
                getBaseUrl() + "/api/allocations/1/approve", null, String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Recruiter must receive HTTP 403 FORBIDDEN when attempting to approve allocation");
    }

    @Test
    @DisplayName("Security: Recruiter is rejected with 403 Forbidden when attempting to reject allocation")
    void testRecruiterCannotRejectAllocation() {
        TestRestTemplate recruiterClient = restTemplate.withBasicAuth("recruiter", "password123");

        ResponseEntity<String> response = recruiterClient.postForEntity(
                getBaseUrl() + "/api/allocations/1/reject",
                Map.of("reason", "Recruiter unpermitted reject"),
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Recruiter must receive HTTP 403 FORBIDDEN when attempting to reject allocation");
    }

    @Test
    @DisplayName("Security: Recruiter is rejected with 403 Forbidden when attempting to cancel allocation")
    void testRecruiterCannotCancelAllocation() {
        TestRestTemplate recruiterClient = restTemplate.withBasicAuth("recruiter", "password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of("reason", "Recruiter attempt"), headers);

        ResponseEntity<String> response = recruiterClient.exchange(
                getBaseUrl() + "/api/allocations/1/cancel",
                HttpMethod.PUT,
                request,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Recruiter must receive HTTP 403 FORBIDDEN when attempting to cancel allocation");
    }

    @Test
    @DisplayName("Security: Recruiter is permitted to create demands, search resources, and submit allocation requests")
    void testRecruiterPermittedOperations() {
        TestRestTemplate recruiterClient = restTemplate.withBasicAuth("recruiter", "password123");

        // 1. Create demand (permitted for Recruiter)
        DemandDto.CreateRequest demandReq = new DemandDto.CreateRequest();
        demandReq.setProjectName("Recruiter Integration Project");
        demandReq.setRole("QA Automation Lead");
        demandReq.setLocation("Remote");
        demandReq.setStartDate(LocalDate.of(2027, 8, 1));
        demandReq.setEndDate(LocalDate.of(2027, 8, 31));
        demandReq.setAllocationPercent(25);
        demandReq.setPriority(com.talentmarketplace.entity.ProjectDemand.Priority.MEDIUM);

        ResponseEntity<DemandDto> demandResp = recruiterClient.postForEntity(
                getBaseUrl() + "/api/demands", demandReq, DemandDto.class
        );
        assertEquals(HttpStatus.CREATED, demandResp.getStatusCode());
        assertNotNull(demandResp.getBody());
        Long demandId = demandResp.getBody().getId();

        // 2. Search available resources
        ResponseEntity<List> searchResp = recruiterClient.getForEntity(
                getBaseUrl() + "/api/resources/available?startDate=2027-08-01&endDate=2027-08-31&requiredCapacity=25",
                List.class
        );
        assertEquals(HttpStatus.OK, searchResp.getStatusCode());

        // 3. Submit allocation request (creates PENDING)
        AllocationDto.Request allocReq = new AllocationDto.Request(
                1L, demandId, LocalDate.of(2027, 8, 1), LocalDate.of(2027, 8, 31), 25
        );
        ResponseEntity<AllocationDto> allocResp = recruiterClient.postForEntity(
                getBaseUrl() + "/api/allocations", allocReq, AllocationDto.class
        );
        assertEquals(HttpStatus.CREATED, allocResp.getStatusCode());
        assertEquals("PENDING", allocResp.getBody().getStatus().name());
    }

    @Test
    @DisplayName("Security: Manager is rejected with 403 Forbidden when attempting to create demand")
    void testManagerCannotCreateDemands() {
        TestRestTemplate managerClient = restTemplate.withBasicAuth("manager", "password123");

        DemandDto.CreateRequest demandReq = new DemandDto.CreateRequest();
        demandReq.setProjectName("Manager Unpermitted Project");
        demandReq.setRole("Lead Architect");
        demandReq.setLocation("London");
        demandReq.setStartDate(LocalDate.of(2027, 11, 1));
        demandReq.setEndDate(LocalDate.of(2027, 11, 30));
        demandReq.setAllocationPercent(100);
        demandReq.setPriority(com.talentmarketplace.entity.ProjectDemand.Priority.HIGH);

        ResponseEntity<String> response = managerClient.postForEntity(
                getBaseUrl() + "/api/demands", demandReq, String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Manager must receive HTTP 403 FORBIDDEN when attempting demand creation (Recruiter only)");
    }

    @Test
    @DisplayName("Security: Admin is rejected with 403 Forbidden when attempting to create demand")
    void testAdminCannotCreateDemands() {
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");

        DemandDto.CreateRequest demandReq = new DemandDto.CreateRequest();
        demandReq.setProjectName("Admin Unpermitted Project");
        demandReq.setRole("Platform Engineer");
        demandReq.setLocation("London");
        demandReq.setStartDate(LocalDate.of(2027, 12, 1));
        demandReq.setEndDate(LocalDate.of(2027, 12, 31));
        demandReq.setAllocationPercent(50);
        demandReq.setPriority(com.talentmarketplace.entity.ProjectDemand.Priority.LOW);

        ResponseEntity<String> response = adminClient.postForEntity(
                getBaseUrl() + "/api/demands", demandReq, String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Admin must receive HTTP 403 FORBIDDEN when attempting demand creation (Recruiter only)");
    }

    @Test
    @DisplayName("Security: Manager authorized approval and cancellation succeed")
    void testManagerAuthorizedApprovalAndCancel() {
        TestRestTemplate managerClient = restTemplate.withBasicAuth("manager", "password123");

        // Submit new allocation
        AllocationDto.Request allocReq = new AllocationDto.Request(
                2L, 1L, LocalDate.of(2027, 9, 1), LocalDate.of(2027, 9, 30), 20
        );
        ResponseEntity<AllocationDto> createResp = managerClient.postForEntity(
                getBaseUrl() + "/api/allocations", allocReq, AllocationDto.class
        );
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
        Long allocId = createResp.getBody().getId();

        // Approve it with Manager credentials
        ResponseEntity<AllocationDto> approveResp = managerClient.postForEntity(
                getBaseUrl() + "/api/allocations/" + allocId + "/approve", null, AllocationDto.class
        );
        assertEquals(HttpStatus.OK, approveResp.getStatusCode());
        assertEquals("APPROVED", approveResp.getBody().getStatus().name());
        assertEquals("manager", approveResp.getBody().getApprovedBy());

        // Cancel it with Manager credentials
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> cancelReq = new HttpEntity<>(Map.of("reason", "Project postponed"), headers);

        ResponseEntity<AllocationDto> cancelResp = managerClient.exchange(
                getBaseUrl() + "/api/allocations/" + allocId + "/cancel",
                HttpMethod.PUT,
                cancelReq,
                AllocationDto.class
        );
        assertEquals(HttpStatus.OK, cancelResp.getStatusCode());
        assertEquals("CANCELLED", cancelResp.getBody().getStatus().name());
    }

    @Test
    @DisplayName("Security: Manager cannot manage skill taxonomy (restricted to Admin)")
    void testManagerCannotManageSkillTaxonomy() {
        TestRestTemplate managerClient = restTemplate.withBasicAuth("manager", "password123");

        SkillDto.CreateRequest skillReq = new SkillDto.CreateRequest();
        skillReq.setName("GraphQL-" + System.currentTimeMillis());
        skillReq.setCategory("Backend");
        ResponseEntity<String> response = managerClient.postForEntity(
                getBaseUrl() + "/api/skills", skillReq, String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Manager must receive HTTP 403 FORBIDDEN when attempting admin-only skill taxonomy creation");
    }

    @Test
    @DisplayName("Security: Admin has administrative control over skills taxonomy and employee provisioning, but not allocation approvals")
    void testAdminPermissions() {
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");

        // Admin creates new skill in taxonomy (permitted)
        SkillDto.CreateRequest skillReq = new SkillDto.CreateRequest();
        skillReq.setName("Rust-" + System.currentTimeMillis());
        skillReq.setCategory("Systems");
        ResponseEntity<SkillDto> skillResp = adminClient.postForEntity(
                getBaseUrl() + "/api/skills", skillReq, SkillDto.class
        );
        assertEquals(HttpStatus.CREATED, skillResp.getStatusCode());
        assertNotNull(skillResp.getBody());

        // Admin is forbidden from approving allocations (Manager only)
        ResponseEntity<String> approveResp = adminClient.postForEntity(
                getBaseUrl() + "/api/allocations/1/approve", null, String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, approveResp.getStatusCode(),
                "Admin must receive HTTP 403 FORBIDDEN when attempting allocation approval (Manager only)");
    }

    @Test
    @DisplayName("Auth API: POST /api/auth/login validates credentials and returns exact role-specific permissions")
    void testAuthLoginRolePermissions() {
        // 1. Recruiter login
        AuthDto.LoginRequest recruiterReq = new AuthDto.LoginRequest("recruiter", "password123");
        ResponseEntity<AuthDto.AuthResponse> recResp = restTemplate.postForEntity(
                getBaseUrl() + "/api/auth/login", recruiterReq, AuthDto.AuthResponse.class
        );
        assertEquals(HttpStatus.OK, recResp.getStatusCode());
        assertNotNull(recResp.getBody());
        assertEquals("RECRUITER", recResp.getBody().getActiveRole());
        assertEquals(true, recResp.getBody().getPermissions().get("canCreateDemand"));
        assertEquals(true, recResp.getBody().getPermissions().get("canEvaluateDemands"));
        assertEquals(true, recResp.getBody().getPermissions().get("canSearchResources"));
        assertEquals(true, recResp.getBody().getPermissions().get("canCompareCandidates"));
        assertEquals(true, recResp.getBody().getPermissions().get("canRequestAllocation"));
        assertEquals(false, recResp.getBody().getPermissions().get("canApproveAllocation"));
        assertEquals(false, recResp.getBody().getPermissions().get("canRejectAllocation"));
        assertEquals(false, recResp.getBody().getPermissions().get("canCancelAllocation"));
        assertEquals(false, recResp.getBody().getPermissions().get("canManageSkills"));
        assertEquals(false, recResp.getBody().getPermissions().get("canManageEmployees"));

        // 2. Manager login
        AuthDto.LoginRequest managerReq = new AuthDto.LoginRequest("manager", "password123");
        ResponseEntity<AuthDto.AuthResponse> mgrResp = restTemplate.postForEntity(
                getBaseUrl() + "/api/auth/login", managerReq, AuthDto.AuthResponse.class
        );
        assertEquals(HttpStatus.OK, mgrResp.getStatusCode());
        assertNotNull(mgrResp.getBody());
        assertEquals("MANAGER", mgrResp.getBody().getActiveRole());
        assertEquals(false, mgrResp.getBody().getPermissions().get("canCreateDemand"), "Manager cannot create demands");
        assertEquals(true, mgrResp.getBody().getPermissions().get("canEvaluateDemands"));
        assertEquals(true, mgrResp.getBody().getPermissions().get("canSearchResources"));
        assertEquals(true, mgrResp.getBody().getPermissions().get("canCompareCandidates"));
        assertEquals(true, mgrResp.getBody().getPermissions().get("canRequestAllocation"));
        assertEquals(true, mgrResp.getBody().getPermissions().get("canApproveAllocation"));
        assertEquals(true, mgrResp.getBody().getPermissions().get("canRejectAllocation"));
        assertEquals(true, mgrResp.getBody().getPermissions().get("canCancelAllocation"));
        assertEquals(false, mgrResp.getBody().getPermissions().get("canManageSkills"));
        assertEquals(false, mgrResp.getBody().getPermissions().get("canManageEmployees"));

        // 3. Admin login
        AuthDto.LoginRequest adminReq = new AuthDto.LoginRequest("admin", "password123");
        ResponseEntity<AuthDto.AuthResponse> admResp = restTemplate.postForEntity(
                getBaseUrl() + "/api/auth/login", adminReq, AuthDto.AuthResponse.class
        );
        assertEquals(HttpStatus.OK, admResp.getStatusCode());
        assertNotNull(admResp.getBody());
        assertEquals("ADMIN", admResp.getBody().getActiveRole());
        assertEquals(false, admResp.getBody().getPermissions().get("canCreateDemand"), "Admin cannot create demands");
        assertEquals(false, admResp.getBody().getPermissions().get("canEvaluateDemands"), "Admin cannot evaluate demands");
        assertEquals(false, admResp.getBody().getPermissions().get("canApproveAllocation"), "Admin cannot approve allocations");
        assertEquals(false, admResp.getBody().getPermissions().get("canRejectAllocation"));
        assertEquals(false, admResp.getBody().getPermissions().get("canCancelAllocation"));
        assertEquals(true, admResp.getBody().getPermissions().get("canManageSkills"));
        assertEquals(true, admResp.getBody().getPermissions().get("canManageEmployees"));

        // 4. Bad credentials
        AuthDto.LoginRequest badReq = new AuthDto.LoginRequest("recruiter", "wrongPassword");
        ResponseEntity<Map> badResp = restTemplate.postForEntity(
                getBaseUrl() + "/api/auth/login", badReq, Map.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, badResp.getStatusCode());
    }

    @Test
    @DisplayName("Auth API: GET /api/auth/me returns current authenticated user and logout clears session")
    void testAuthMeAndLogout() {
        TestRestTemplate managerClient = restTemplate.withBasicAuth("manager", "password123");

        ResponseEntity<AuthDto.AuthResponse> meResp = managerClient.getForEntity(
                getBaseUrl() + "/api/auth/me", AuthDto.AuthResponse.class
        );
        assertEquals(HttpStatus.OK, meResp.getStatusCode());
        assertNotNull(meResp.getBody());
        assertEquals("manager", meResp.getBody().getUsername());
        assertEquals("MANAGER", meResp.getBody().getActiveRole());

        // Logout
        ResponseEntity<Map> logoutResp = managerClient.postForEntity(
                getBaseUrl() + "/api/auth/logout", null, Map.class
        );
        assertEquals(HttpStatus.OK, logoutResp.getStatusCode());
    }

    @Test
    @DisplayName("Skill Loading: GET /api/skills returns complete taxonomy list and supports category filtering")
    void testSkillLoadingAndTaxonomyListing() {
        ResponseEntity<List> response = restTemplate.getForEntity(
                getBaseUrl() + "/api/skills", List.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() >= 14, "Must return all seeded skills from database");

        // Filter by category
        ResponseEntity<List> filteredResp = restTemplate.getForEntity(
                getBaseUrl() + "/api/skills?category=BACKEND", List.class
        );
        assertEquals(HttpStatus.OK, filteredResp.getStatusCode());
        assertNotNull(filteredResp.getBody());
        assertFalse(filteredResp.getBody().isEmpty(), "Backend category filter must return backend skills");
    }

    @Test
    @DisplayName("Security: Employee provisioning (POST /api/employees) is restricted to Admin role")
    void testEmployeeCreationRestrictedToAdmin() {
        TestRestTemplate recruiterClient = restTemplate.withBasicAuth("recruiter", "password123");
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");

        com.talentmarketplace.dto.EmployeeDto.CreateRequest empReq = new com.talentmarketplace.dto.EmployeeDto.CreateRequest();
        empReq.setEmployeeCode("EMP-TEST-" + System.currentTimeMillis());
        empReq.setName("Test Provisioned Engineer");
        empReq.setDepartment("Engineering");
        empReq.setLocation("London");

        // Recruiter attempt must be rejected with 403 Forbidden
        ResponseEntity<String> recResp = recruiterClient.postForEntity(
                getBaseUrl() + "/api/employees", empReq, String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, recResp.getStatusCode(),
                "Recruiter must receive 403 Forbidden when attempting employee provisioning");

        // Admin attempt must succeed with 201 Created
        ResponseEntity<com.talentmarketplace.dto.EmployeeDto> admResp = adminClient.postForEntity(
                getBaseUrl() + "/api/employees", empReq, com.talentmarketplace.dto.EmployeeDto.class
        );
        assertEquals(HttpStatus.CREATED, admResp.getStatusCode(),
                "Admin must be permitted to provision employees");
        assertNotNull(admResp.getBody());
        assertNotNull(admResp.getBody().getId());
    }

    @Test
    @DisplayName("Security: Admin is rejected with 403 Forbidden when attempting to submit allocation request")
    void testAdminCannotSubmitAllocationRequest() {
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");

        AllocationDto.Request allocReq = new AllocationDto.Request(
                1L, 1L, LocalDate.of(2027, 10, 1), LocalDate.of(2027, 10, 31), 20
        );

        ResponseEntity<String> response = adminClient.postForEntity(
                getBaseUrl() + "/api/allocations", allocReq, String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Admin must receive HTTP 403 FORBIDDEN when attempting allocation request submission (Recruiter/Manager only)");
    }

    @Test
    @DisplayName("Security: Admin is rejected with 403 Forbidden when attempting to cancel committed allocation")
    void testAdminCannotCancelAllocation() {
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> cancelReq = new HttpEntity<>(Map.of("reason", "Admin unpermitted cancel"), headers);

        ResponseEntity<String> response = adminClient.exchange(
                getBaseUrl() + "/api/allocations/1/cancel",
                HttpMethod.PUT,
                cancelReq,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Admin must receive HTTP 403 FORBIDDEN when attempting allocation cancellation (Manager only)");
    }

    @Test
    @DisplayName("Security: Recruiter is rejected with 403 Forbidden when attempting to record employee skill assessment")
    void testRecruiterCannotAddEmployeeSkill() {
        TestRestTemplate recruiterClient = restTemplate.withBasicAuth("recruiter", "password123");

        com.talentmarketplace.dto.EmployeeSkillDto.AddRequest skillReq =
                new com.talentmarketplace.dto.EmployeeSkillDto.AddRequest();
        skillReq.setSkillId(1L);
        skillReq.setProficiencyLevel(4);
        skillReq.setLastValidatedAt(LocalDate.now());

        ResponseEntity<String> response = recruiterClient.postForEntity(
                getBaseUrl() + "/api/employees/1/skills", skillReq, String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Recruiter must receive HTTP 403 FORBIDDEN when attempting to record employee skill assessment (Manager/Admin only)");
    }

    @Test
    @DisplayName("Admin Inspection: Admin is permitted to inspect demand specifications and required skills (GET /api/demands/{id})")
    void testAdminCanInspectDemandSpecifications() {
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");

        ResponseEntity<DemandDto> response = adminClient.getForEntity(
                getBaseUrl() + "/api/demands/1", DemandDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Admin must be permitted to inspect demand specifications for audit view");
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertNotNull(response.getBody().getProjectName());
        assertNotNull(response.getBody().getRequiredSkills());
    }

    @Test
    @DisplayName("Employee Management: Admin can create new employee, persisted in database with no automatic allocations")
    void testAdminCreateEmployeeAndPersistence() {
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");

        com.talentmarketplace.dto.EmployeeDto.CreateRequest empReq = new com.talentmarketplace.dto.EmployeeDto.CreateRequest();
        String uniqueCode = "EMP-NEW-" + System.currentTimeMillis();
        empReq.setEmployeeCode(uniqueCode);
        empReq.setName("Devon Walsh");
        empReq.setDepartment("DevOps");
        empReq.setLocation("Berlin");
        empReq.setEmploymentStatus(com.talentmarketplace.entity.Employee.EmploymentStatus.ACTIVE);

        ResponseEntity<com.talentmarketplace.dto.EmployeeDto> createResp = adminClient.postForEntity(
                getBaseUrl() + "/api/employees", empReq, com.talentmarketplace.dto.EmployeeDto.class
        );

        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
        assertNotNull(createResp.getBody());
        Long empId = createResp.getBody().getId();
        assertNotNull(empId);
        assertEquals(uniqueCode, createResp.getBody().getEmployeeCode());
        assertEquals("Devon Walsh", createResp.getBody().getName());
        assertEquals("DevOps", createResp.getBody().getDepartment());
        assertEquals("Berlin", createResp.getBody().getLocation());
        assertEquals(com.talentmarketplace.entity.Employee.EmploymentStatus.ACTIVE, createResp.getBody().getEmploymentStatus());

        // Verify retrieval from backend
        ResponseEntity<com.talentmarketplace.dto.EmployeeDto> getResp = adminClient.getForEntity(
                getBaseUrl() + "/api/employees/" + empId, com.talentmarketplace.dto.EmployeeDto.class
        );
        assertEquals(HttpStatus.OK, getResp.getStatusCode());
        assertEquals("Devon Walsh", getResp.getBody().getName());

        // Verify that NO automatic allocations or requests were generated
        ResponseEntity<List> allocsResp = adminClient.getForEntity(
                getBaseUrl() + "/api/allocations?employeeId=" + empId, List.class
        );
        assertEquals(HttpStatus.OK, allocsResp.getStatusCode());
        assertNotNull(allocsResp.getBody());
        assertTrue(allocsResp.getBody().isEmpty(), "New employee must not have automatic allocations or requests");
    }

    @Test
    @DisplayName("Employee Management: Reject duplicate employee code with 400 Bad Request")
    void testDuplicateEmployeeCodeRejected() {
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");

        com.talentmarketplace.dto.EmployeeDto.CreateRequest empReq = new com.talentmarketplace.dto.EmployeeDto.CreateRequest();
        empReq.setEmployeeCode("EMP001"); // Pre-seeded employee code for Alice Johnson
        empReq.setName("Duplicate Alice");
        empReq.setDepartment("Engineering");
        empReq.setLocation("New York");

        ResponseEntity<Map> response = adminClient.postForEntity(
                getBaseUrl() + "/api/employees", empReq, Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Duplicate employee code must return HTTP 400 Bad Request");
        assertNotNull(response.getBody());
        String message = (String) response.getBody().get("message");
        assertTrue(message != null && message.contains("already exists"),
                "Error message should state that employee code already exists");
    }

    @Test
    @DisplayName("Employee Management: Validation rejects blank employee fields with 400 Bad Request")
    void testBlankEmployeeFieldsRejected() {
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");

        com.talentmarketplace.dto.EmployeeDto.CreateRequest invalidReq = new com.talentmarketplace.dto.EmployeeDto.CreateRequest();
        invalidReq.setEmployeeCode("");
        invalidReq.setName("   ");
        invalidReq.setDepartment("");
        invalidReq.setLocation("");

        ResponseEntity<Map> response = adminClient.postForEntity(
                getBaseUrl() + "/api/employees", invalidReq, Map.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
                "Blank required employee fields must return HTTP 400 Bad Request");
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("fieldErrors") || response.getBody().containsKey("message"));
    }

    @Test
    @DisplayName("Employee Management: Manager cannot create employee (403 Forbidden)")
    void testManagerCannotCreateEmployee() {
        TestRestTemplate managerClient = restTemplate.withBasicAuth("manager", "password123");

        com.talentmarketplace.dto.EmployeeDto.CreateRequest empReq = new com.talentmarketplace.dto.EmployeeDto.CreateRequest();
        empReq.setEmployeeCode("EMP-MGR-FORBIDDEN");
        empReq.setName("Unauthorized Manager Addition");
        empReq.setDepartment("Product");
        empReq.setLocation("London");

        ResponseEntity<String> response = managerClient.postForEntity(
                getBaseUrl() + "/api/employees", empReq, String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Manager must receive HTTP 403 Forbidden when attempting to create employee");
    }

    @Test
    @DisplayName("Skills & Search: New employee with assigned skill is discoverable in Resource Search")
    void testNewEmployeeSkillAssignmentAndResourceSearch() {
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");
        TestRestTemplate recruiterClient = restTemplate.withBasicAuth("recruiter", "password123");

        // 1. Create a new active employee
        String code = "EMP-SRCH-" + System.currentTimeMillis();
        com.talentmarketplace.dto.EmployeeDto.CreateRequest empReq = new com.talentmarketplace.dto.EmployeeDto.CreateRequest();
        empReq.setEmployeeCode(code);
        empReq.setName("Siddharth Patel");
        empReq.setDepartment("Architecture");
        empReq.setLocation("Remote");
        empReq.setEmploymentStatus(com.talentmarketplace.entity.Employee.EmploymentStatus.ACTIVE);

        ResponseEntity<com.talentmarketplace.dto.EmployeeDto> empResp = adminClient.postForEntity(
                getBaseUrl() + "/api/employees", empReq, com.talentmarketplace.dto.EmployeeDto.class
        );
        assertEquals(HttpStatus.CREATED, empResp.getStatusCode());
        Long empId = empResp.getBody().getId();

        // 2. Assign skill (Spring Boot - skillId 2) with proficiency 5
        com.talentmarketplace.dto.EmployeeSkillDto.AddRequest skillReq = new com.talentmarketplace.dto.EmployeeSkillDto.AddRequest();
        skillReq.setSkillId(2L);
        skillReq.setProficiencyLevel(5);
        skillReq.setLastValidatedAt(LocalDate.of(2026, 10, 1));

        ResponseEntity<com.talentmarketplace.dto.EmployeeSkillDto> skillResp = adminClient.postForEntity(
                getBaseUrl() + "/api/employees/" + empId + "/skills", skillReq, com.talentmarketplace.dto.EmployeeSkillDto.class
        );
        assertEquals(HttpStatus.CREATED, skillResp.getStatusCode());
        assertEquals(5, skillResp.getBody().getProficiencyLevel());

        // 3. Search available resources as Recruiter for Spring Boot (skillId 2)
        ResponseEntity<List> searchResp = recruiterClient.getForEntity(
                getBaseUrl() + "/api/resources/available?skillIds=2&minProficiency=4&startDate=2028-05-01&endDate=2028-05-31&requiredCapacity=20",
                List.class
        );
        assertEquals(HttpStatus.OK, searchResp.getStatusCode());
        assertNotNull(searchResp.getBody());

        // Verify the newly created employee is found in search results
        boolean foundInSearch = searchResp.getBody().stream().anyMatch(res -> {
            if (res instanceof Map map) {
                return code.equals(map.get("employeeCode")) || "Siddharth Patel".equals(map.get("name"));
            }
            return false;
        });
        assertTrue(foundInSearch, "Newly created employee with assigned skill must be returned in Resource Search");
    }

    @Test
    @DisplayName("Row-Specific Safety: Row-specific skill assignment endpoint affects only the target employee")
    void testRowSpecificSkillEndpointSafety() {
        TestRestTemplate adminClient = restTemplate.withBasicAuth("admin", "password123");

        // Employee 1 (Alice Johnson) vs Employee 2 (Bob Martinez)
        // Check Employee 2's initial skills
        ResponseEntity<com.talentmarketplace.dto.EmployeeDto> emp2Before = adminClient.getForEntity(
                getBaseUrl() + "/api/employees/2", com.talentmarketplace.dto.EmployeeDto.class
        );
        assertEquals(HttpStatus.OK, emp2Before.getStatusCode());
        int emp2SkillCountBefore = emp2Before.getBody().getSkills().size();

        // Assign skill to Employee 1
        com.talentmarketplace.dto.EmployeeSkillDto.AddRequest skillReq = new com.talentmarketplace.dto.EmployeeSkillDto.AddRequest();
        skillReq.setSkillId(7L); // Docker
        skillReq.setProficiencyLevel(4);
        skillReq.setLastValidatedAt(LocalDate.of(2026, 10, 10));

        ResponseEntity<com.talentmarketplace.dto.EmployeeSkillDto> assignResp = adminClient.postForEntity(
                getBaseUrl() + "/api/employees/1/skills", skillReq, com.talentmarketplace.dto.EmployeeSkillDto.class
        );
        assertEquals(HttpStatus.CREATED, assignResp.getStatusCode());

        // Verify Employee 2's skills remained completely untouched
        ResponseEntity<com.talentmarketplace.dto.EmployeeDto> emp2After = adminClient.getForEntity(
                getBaseUrl() + "/api/employees/2", com.talentmarketplace.dto.EmployeeDto.class
        );
        assertEquals(HttpStatus.OK, emp2After.getStatusCode());
        assertEquals(emp2SkillCountBefore, emp2After.getBody().getSkills().size(),
                "Targeting employee 1 must never alter employee 2's skill records");
    }
}
