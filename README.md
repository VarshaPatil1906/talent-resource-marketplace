# AI-Driven Talent and Resource Allocation Marketplace

[![Java](https://img.shields.io/badge/Java-17%20%7C%2025%20LTS-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-MySQL%208%20%2F%20H2%20Hybrid-blue.svg)](https://www.mysql.com/)
[![Build & Tests](https://img.shields.io/badge/Tests-22%2F22%20Passing%20(100%25)-success.svg)](target/surefire-reports)

An enterprise-grade, deterministic talent and resource marketplace combining database-grounded scoring, inclusive-date capacity tracking, pessimistic concurrency control, manager approval workflows, and an autonomous, goal-driven AI staffing agent.

---

## 1. Project Overview & Business Problem

Modern professional services and technology organizations face recurring staffing inefficiencies:
- **Over-allocation and Burnout:** Unsynchronized resource requests exceed 100% committed capacity.
- **Subjective & Opaque Matching:** Candidate selection often relies on unverified claims rather than formal assessment evidence.
- **Race Conditions:** Two hiring managers concurrently approving the same high-demand engineer, resulting in over-allocation.
- **Unchecked AI Hallucinations:** Generative AI tools inventing skills, ignoring capacity limits, or bypassing governance gates.

### The Solution
This platform enforces strict domain integrity:
1. **Deterministic Scoring (0–100):** Explicit mathematical formula combining skill match (40%), proficiency (30%), capacity (20%), and location fit (10%).
2. **Inclusive-Date Availability Engine:** Day-by-day concurrent committed capacity calculation, accurately reporting full vs partial availability.
3. **Pessimistic Concurrency Locking:** Row-level `PESSIMISTIC_WRITE` locks on employee records prevent concurrent conflicting commitments.
4. **Governed Approval Gate:** Only authorized managers can commit allocations. Revalidation is enforced at commit time.
5. **Agentic AI Staffing Assistant:** Uses 6 controlled backend tools to inspect facts, verify evidence, suggest learning paths, and submit proposals into a `PENDING` state. Direct AI self-approval is forbidden by policy.

---

## 2. Technology Choices

- **Backend:** Java 17/25 LTS, Spring Boot 3.3.4, Spring Data JPA, Jakarta Validation, Spring Security 6.
- **Persistence:** MySQL 8.0 (primary production database) with out-of-the-box H2 MySQL mode fallback for zero-dependency standalone execution.
- **Testing:** JUnit 5, Mockito, Spring Boot Test, Spring Security Test.
- **Frontend:** Vanilla HTML5, CSS3, and JavaScript (ES6+).
  - *Why Vanilla JS?* Eliminates heavy build tools, transpilers, npm vulnerabilities, and complex hydration cycles. Provides instant sub-second browser loads, 100% predictable DOM manipulation, and turnkey execution directly from the Spring Boot embedded server.
- **AI Integration:** Controlled Tool Adapter (`StaffingToolsService`) with `SimulatedStaffingAgent` fallback, designed for easy plug-and-play connection to live LLM APIs.

---

## 3. Architecture & Data Model

### Data Relationships
- **Employee** (1) $\leftrightarrow$ (N) **EmployeeSkill** (Proficiency 1–5; unassessed proficiency is stored as `NULL`, never invented).
- **Skill** (1) $\leftrightarrow$ (N) **EmployeeSkill** / **DemandSkill**.
- **ProjectDemand** (1) $\leftrightarrow$ (N) **DemandSkill** (Mandatory flag and minimum required proficiency).
- **Employee** (1) $\leftrightarrow$ (N) **Allocation** (`PENDING`, `APPROVED`, `CANCELLED`).
- **Allocation** (1) $\leftrightarrow$ (N) **AllocationHistory** (Immutable audit trail: `CREATED`, `APPROVED`, `REJECTED`, `CANCELLED`).
- **Employee** (1) $\leftrightarrow$ (N) **TrainingRecommendation** (Prescribed learning pathways for skill deficits).

Detailed diagrams and schemas are available in [docs/architecture.md](docs/architecture.md).

---

## 4. Prerequisites & Environment Configuration

### Prerequisites
- **Java:** JDK 17 or later (Verified with OpenJDK 25 LTS).
- **Maven:** Bundled via `./mvnw.cmd` (Windows) or `./mvnw` (Linux/macOS).
- **MySQL (Optional):** MySQL 8.0 or Docker Compose. (If MySQL is absent, the system defaults to in-memory MySQL-compatible storage).

### Environment Variables
Copy `.env.example` to `.env` to customize settings:
```bash
# Server Port
SERVER_PORT=8080

# Active Profile: 'default' (Turnkey H2 MySQL mode) or 'mysql' (External MySQL)
SPRING_PROFILES_ACTIVE=default

# MySQL Connection (When using profile 'mysql')
DB_HOST=localhost
DB_PORT=3306
DB_NAME=talent_marketplace
DB_USER=talent_user
DB_PASSWORD=talent_pass

# Security Credentials
SECURITY_MANAGER_PASSWORD=password123
SECURITY_RECRUITER_PASSWORD=password123
SECURITY_ADMIN_PASSWORD=password123
```

---

## 5. Setup & Running Instructions

### Quick Start (Turnkey Standalone Mode)
Run the application with zero external dependencies:
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot"
.\mvnw.cmd spring-boot:run
```
Once started, open your browser at:
```
http://localhost:8080
```

### Running with Docker Compose & MySQL 8
To run with a dedicated MySQL 8 database container:
```bash
docker compose up -d
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```

---

## 6. Accessing the Seven Frontend Screens

The application is an interactive Single-Page Application (SPA) accessible at `http://localhost:8080`:

| Screen # | Screen Name | Key Capabilities |
| :--- | :--- | :--- |
| **Screen 1** | **Demand Intake Form** | Submit project requirements with explicit mandatory skills and minimum proficiency levels. |
| **Screen 2** | **Employee Skills Matrix** | Inspect verified proficiencies (levels 1–5), validation dates, and record new skill assessments. |
| **Screen 3** | **Resource Search & Ranking** | Query talent by date range, required capacity %, and skills with real-time availability calculation. |
| **Screen 4** | **Candidate Comparison** | Side-by-side comparison of top candidates with 0–100 score breakdowns and exclusion reasons. |
| **Screen 5** | **Allocation Approval** | Manager-only workflow to review pending requests, approve with row-level locks, cancel, and view audit history. |
| **Screen 6** | **Utilization Dashboard** | Organization-wide utilization %, department averages, bench capacity, and talent deficit gaps. |
| **Screen 7** | **AI Staffing Agent** | Autonomous 6-step tool execution trace, evidence fact sheets, and strategic staffing proposals. |

---

## 7. Demo Credentials

The platform includes pre-configured demo credentials:

| Username | Password | Role | Permissions |
| :--- | :--- | :--- | :--- |
| `manager` | `password123` | `ROLE_MANAGER` | Full read, demand intake, skill management, **Allocation Approval & Cancellation**. |
| `recruiter`| `password123` | `ROLE_RECRUITER` | Read access, demand intake, candidate comparison, submitting allocation requests. |
| `admin` | `password123` | `ROLE_ADMIN` | Full administrative control. |

*You can switch roles dynamically in the top navigation bar header.*

---

## 8. REST API Endpoints & Examples

### Core Endpoints

#### 1. Employees & Skills
- `GET /api/employees` — List all employees (supports `?department=Engineering&status=ACTIVE`)
- `GET /api/employees/{id}` — Get employee by ID
- `POST /api/employees` — Create synthetic employee
- `POST /api/employees/{id}/skills` — Add or update employee skill assessment
- `GET /api/skills` — List standardized skill taxonomy

#### 2. Demands & Candidate Matching
- `GET /api/demands` — List demands (supports `?status=OPEN&priority=HIGH`)
- `POST /api/demands` — Create project demand with required skills
- `POST /api/demands/{id}/recommendations` — Compute deterministic candidate recommendations

#### 3. Allocations & Approvals
- `GET /api/allocations` — List allocations (supports `?status=PENDING`)
- `POST /api/allocations` — Submit allocation request (creates `PENDING` state)
- `POST /api/allocations/{id}/approve` — **Manager Only:** Revalidate and commit allocation
- `POST /api/allocations/{id}/reject` — **Manager Only:** Reject pending allocation
- `PUT /api/allocations/{id}/cancel` — **Manager Only:** Cancel allocation and release capacity
- `GET /api/allocations/{id}/history` — Get immutable audit trail

#### 4. Reports & AI
- `GET /api/reports/utilization` — Daily concurrent utilization analysis
- `GET /api/reports/skill-gaps` — Open demand skill deficits grouped by category
- `POST /api/ai/staffing-advice` — Execute autonomous 6-step staffing agent workflow

### Example Request: AI Staffing Advice
```http
POST /api/ai/staffing-advice
Content-Type: application/json

{
  "demandId": 1,
  "goal": "Find top lead engineer with verified Spring Boot proficiency and full capacity"
}
```

### Example Response:
```json
{
  "demandId": 1,
  "projectName": "Core Payment Engine Refactoring",
  "role": "Senior Backend Engineer",
  "goal": "Find top lead engineer with verified Spring Boot proficiency and full capacity",
  "simulatedAgent": true,
  "simulationDisclaimer": "Simulated AI Staffing Agent — no live LLM connected. Grounded in deterministic backend engine data.",
  "topCandidate": {
    "employeeId": 1,
    "employeeCode": "EMP001",
    "name": "Alice Johnson",
    "location": "London",
    "department": "Engineering",
    "eligible": true,
    "overallScore": 95.0,
    "scoreBreakdown": {
      "skillMatchScore": 40.0,
      "proficiencyScore": 25.0,
      "capacityScore": 20.0,
      "locationScore": 10.0,
      "totalScore": 95.0
    },
    "matchedSkills": [
      { "skillName": "Java", "requiredProficiency": 4, "actualProficiency": 5, "mandatory": true, "validatedDate": "2026-08-15" },
      { "skillName": "Spring Boot", "requiredProficiency": 3, "actualProficiency": 4, "mandatory": true, "validatedDate": "2026-08-15" }
    ],
    "availableCapacityPercent": 100
  },
  "toolExecutionTrace": [
    "[Tool 1/6] Invoking getDemand(1)",
    "[Tool 2/6] Invoking getCandidateRecommendations(1)",
    "[Tool 3/6] Invoking getEmployeeSkillEvidence(EmployeeID: 1)",
    "[Tool 4/6] Invoking checkAvailability(EmployeeID: 1, Start: 2026-11-01, End: 2027-02-28, ReqPercent: 100)",
    "[Tool 5/6] Invoking generateTrainingRecommendations(EmployeeID: 6)",
    "[Agent Summary] Synthesis complete. Prepared recommendation for manager approval."
  ]
}
```

---

## 9. Candidate Scoring & Eligibility Rules

### Deterministic Formula (0 to 100)
1. **Skill Match (Max 40 points):** Proportional match of mandatory and optional skills. Missing mandatory skill = ineligible.
2. **Proficiency Fit (Max 30 points):** Baseline points for meeting required levels + bonus points for exceeding required proficiency.
3. **Capacity & Availability (Max 20 points):** Calculated using inclusive dates. Scaled to uncommitted percentage.
4. **Location Fit (Max 10 points):** Exact city match awards 10 pts; Remote compatibility awards 6 pts; Mismatch awards 0 pts.

---

## 10. Automated Tests & Verification

Run the complete test suite:
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot"
.\mvnw.cmd test
```

### Verified Test Scenarios (All 22 Passing):
- **TC-06-01:** Valid demand creation succeeds.
- **TC-06-02:** Missing role or invalid dates are rejected.
- **TC-06-03:** Candidate search follows documented eligibility and scoring.
- **TC-06-04:** Missing mandatory skills are excluded or explicitly flagged according to policy.
- **TC-06-05:** Full-period allocation makes the employee unavailable for conflicting requests.
- **TC-06-06:** Partial availability is accurately reported.
- **TC-06-07:** Approved allocation is persisted and capacity is recalculated.
- **TC-06-08:** Allocation exceeding capacity is rejected.
- **TC-06-09:** AI recommendation includes evidence and scores matching database records.
- **TC-06-10:** AI cannot invent proficiency or availability when data is missing.
- **TC-06-11:** Concurrent conflicting allocation requests cannot both exceed capacity.
- **TC-06-12:** Cancellation releases capacity and preserves allocation history.
- **Plus 10 Integration & Security Tests:** Role-based access, unauthenticated 401/403 rejection, and input validation.

---

## 11. Six Required SQL Tasks (`database/queries.sql`)

The repository includes executable SQL queries in [database/queries.sql](database/queries.sql):
1. **Query 1:** Open demands by role and priority.
2. **Query 2:** Resources grouped by skill and proficiency level.
3. **Query 3:** Resources available for a specified date range taking overlapping approved allocations into account.
4. **Query 4:** Current utilization by employee and department.
5. **Query 5:** Demands with zero eligible candidates.
6. **Query 6:** Skill gaps and talent deficits grouped by skill category.

---

## 12. Troubleshooting & FAQ

- **Port 8080 in use?** Change `SERVER_PORT=8081` in `.env` or run with `-Dserver.port=8081`.
- **Java version warning?** The application uses release 17 bytecode compatibility, compiling cleanly under Java 17 and Java 25 LTS.
- **AI Agent Label:** The interface displays *"Simulated AI Staffing Agent — no live LLM connected"* by default, ensuring full transparency while keeping all 6 backend tools strictly active.
