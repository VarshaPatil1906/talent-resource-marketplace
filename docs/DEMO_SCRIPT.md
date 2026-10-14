# Comprehensive Demo Script & Walkthrough

This document outlines the step-by-step demonstration walkthrough for the **Talent and Resource Allocation Marketplace**, covering the primary Happy Path and three critical Governance & Failure Cases.

---

## Prerequisites & Launching the Application

1. Open a terminal in the project directory:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot"
   .\mvnw.cmd spring-boot:run
   ```
2. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```
   The application initializes with 10 synthetic employees, 14 standardized skills, 5 project demands, and active/cancelled allocations.

---

## Part 1: The Happy Path Walkthrough

### Step 1: Screen 1 — Demand Intake Form
1. Click **1. Demand Intake** on the sidebar.
2. Fill out the form:
   - **Project Name:** `Core Payment Engine Refactoring`
   - **Target Role:** `Senior Backend Engineer`
   - **Location:** `London`
   - **Start Date:** `2026-11-01`
   - **End Date:** `2027-02-28`
   - **Allocation Capacity (%):** `100`
   - **Priority:** `HIGH`
3. In the **Required Skills** section:
   - Row 1: `Java`, `Min L4`, `Mandatory` checked.
   - Row 2: `Spring Boot`, `Min L3`, `Mandatory` checked.
   - Row 3: `MySQL`, `Min L3`, `Mandatory` unchecked (optional skill).
4. Click **Create Project Demand**.
5. Observe the toast notification and see the new demand appear in the **Existing Project Demands** table.

### Step 2: Screen 2 — Employee Skills Matrix
1. Click **2. Skills Matrix** on the sidebar.
2. Filter by `Engineering` or type `Alice` in the search bar.
3. Observe `Alice Johnson (EMP001)`:
   - Verified Skills: `Java [★★★★★]` (Validated 2026-08-15), `Spring Boot [★★★★]`, `MySQL [★★★★]`, `React [★★★]`.
4. Click **+ Skill** next to an employee to add a new skill with proficiency 1-5 and validation date.

### Step 3: Screen 3 — Resource Search & Ranking
1. Click **3. Resource Search** on the sidebar.
2. Set **Start Date:** `2026-11-01`, **End Date:** `2027-02-28`, **Required Capacity (%):** `50`, **Skill:** `Java`, **Min Proficiency:** `Level 3+`.
3. Click **Search Resources**.
4. Observe the ranking cards showing available capacity percentages, peak committed allocations, and location.

### Step 4: Screen 4 — Candidate Comparison
1. Click **4. Candidate Comparison** on the sidebar.
2. Select `Core Payment Engine Refactoring (Senior Backend Engineer - London)` from the dropdown.
3. Click **Evaluate Candidates**.
4. Observe the side-by-side comparison cards:
   - **Alice Johnson (EMP001):** Top recommendation, **Score: 95/100**, **Status: ELIGIBLE**.
     - Skill Match: `40/40`
     - Proficiency Bonus: `25/30`
     - Capacity: `20/20` (100% free)
     - Location Fit: `10/10` (Exact London match)
   - **Bob Smith (EMP002):** **Score: 78/100**, **Status: ELIGIBLE**, Location mismatch (New York vs London).
   - **Frank Miller (EMP006):** **Score: 42/100**, **Status: INELIGIBLE** (Spring Boot proficiency level 2 is below required level 3; insufficient capacity).

### Step 5: Screen 7 — AI Staffing Assistant (Simulated AI)
1. Click **7. AI Staffing Agent** on the sidebar.
2. Note the prominent disclaimer banner: *"Simulated AI Staffing Agent — no live LLM connected. Grounded in deterministic backend engine data."*
3. Select `Core Payment Engine Refactoring` and enter the goal:
   `Find the best qualified engineer with full capacity and verified Java expertise`
4. Click **⚡ Execute Agentic Staffing Workflow**.
5. Observe the interactive output:
   - **Tool Execution Trace:** Step-by-step logs for all 6 backend tools (`getDemand`, `getCandidateRecommendations`, `getEmployeeSkillEvidence`, `checkAvailability`, `generateTrainingRecommendations`, `submitAllocationRequest`).
   - **Candidate Fact Sheet:** Complete evidence verification with zero hallucination.
   - **Strategic Rationale:** Clear textual explanation grounded in database facts.
   - **Targeted Training Pathways:** Prescribed training for deficit candidates.
6. Click **Submit for Manager Approval** on the candidate card.

### Step 6: Screen 5 — Allocation Approval Workflow
1. Click **5. Allocation Approval** on the sidebar.
2. Notice the **Pending Allocations** table containing the proposal submitted by the AI agent.
3. Note the top-right role switcher is set to `Manager`.
4. Click **Approve (Lock & Commit)**.
5. The backend acquires a pessimistic write lock on the employee row, revalidates open demand status, verifies skills, recalculates available capacity, commits the allocation, updates the demand status to `STAFFED`, and logs an audit record.
6. Click **History** on the approved allocation to view the immutable audit trail (`CREATED` by AI, `APPROVED` by manager).

### Step 7: Screen 6 — Utilization Dashboard
1. Click **6. Utilization Dashboard** on the sidebar.
2. Observe updated KPIs:
   - Average utilization percentage updated.
   - Engineering department utilization increased.
   - Alice Johnson's committed capacity updated to 100%.

---

## Part 2: Failure & Edge Case Scenarios

### Failure Case 1: Overcapacity Allocation Rejection (Concurrency & Capacity Integrity)
1. Go to **5. Allocation Approval**.
2. Notice `Frank Miller (EMP006)` already has an existing 50% approved allocation from `2026-11-01` to `2027-01-31`.
3. Go to **3. Resource Search** or submit an allocation request for Frank Miller for **60% capacity** covering November 2026.
4. When the request is submitted into pending and the manager clicks **Approve**:
5. **Expected Result:**
   - The backend throws `409 Conflict: CapacityExceededException`.
   - Error toast displays: *"Cannot approve allocation: Employee EMP006 has only 50% available capacity during requested period, but 60% is required."*
   - The allocation is NOT persisted, preventing over-allocation.

### Failure Case 2: Unvalidated Proficiency & Mandatory Skill Deficit
1. In **1. Demand Intake**, inspect `AI-Powered Fraud Detection` which requires:
   - `Python` (Mandatory, Min Level 4)
   - `Machine Learning` (Mandatory, Min Level 4)
2. In **4. Candidate Comparison**, select this demand.
3. Observe `Henry Zhao (EMP008)`:
   - Henry has Python in his profile, but his proficiency is `NULL` (unvalidated assessment).
4. **Expected Result:**
   - Henry is marked **INELIGIBLE**.
   - Exclusion reason clearly states: *"Mandatory skill 'Python' has unknown/unvalidated proficiency"*.
   - The system NEVER invents or assumes proficiency for unassessed candidates.

### Failure Case 3: Unauthorized Approval Attempt (Spring Security Enforcement)
1. In the top-right header, change **Active Role** from `Manager` to `Recruiter`.
2. Go to **5. Allocation Approval**.
3. Attempt to click **Approve** or **Cancel Allocation**.
4. **Expected Result:**
   - The backend returns `403 Forbidden` (`AccessDeniedException`).
   - Error toast displays: *"Access denied: manager authorization required for this action"*.
   - Only users with `ROLE_MANAGER` or `ROLE_ADMIN` can approve allocations.

### Failure Case 4: Demand with Zero Matching Candidates
1. In **4. Candidate Comparison**, select `Legacy Monolith Decommissioning` (requires Rust L5 and Erlang L5).
2. **Expected Result:**
   - Zero candidates are marked eligible.
   - In **6. Utilization Dashboard**, the demand is highlighted under **Unstaffed Demands (0 eligible candidates)**.
   - The AI Assistant automatically prescribes structured learning paths for candidates.
