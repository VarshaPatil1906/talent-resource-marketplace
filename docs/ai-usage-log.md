# AI Usage Log & Engineering Audit

**Project:** Talent and Resource Allocation Marketplace  
**Engine:** Antigravity AI Assistant  
**Date:** 2026-10-14  

---

## 1. System Prompts & Tool Orchestration Record

| Timestamp | Phase / Workflow Step | Tool Calls / Actions Executed | Outcome |
| :--- | :--- | :--- | :--- |
| 10:49 UTC | Workspace & Toolchain Inspection | `run_command` (`java -version`, `mvn -v`, `git status`) | Detected Java 25 LTS; Maven wrapper located in VSCode extensions and configured. |
| 10:54 UTC | Project Initialization & Domain Entities | `write_to_file` (`pom.xml`, `.gitignore`, `docker-compose.yml`, 8 JPA Entities) | Entities created with validation, foreign keys, and indexes. |
| 11:00 UTC | DTOs, Repositories & Controllers | `write_to_file` (8 Repositories, DTOs, Exception handlers, Controllers) | Compiles with 54 source files in 2.8s. |
| 11:06 UTC | Engine & Database Scripts | `write_to_file` (`StaffingEngineService`, `database/queries.sql`, `seed.sql`) | 6 production SQL queries and 0-100 deterministic scoring engine built. |
| 11:11 UTC | Automated Test Implementation | `write_to_file` (6 Test classes covering TC-06-01 through TC-06-12) | 22 automated test scenarios authored. |
| 11:12 UTC | Initial Test Run & Failure Analysis | `run_command` (`.\mvnw.cmd test`) | 21 passed, 1 failed (TC-06-11 concurrency isolation). |
| 11:14 UTC | Defect Remediation & Verification | `replace_file_content` (`AllocationService.java`), re-ran `mvnw test` | 22/22 tests passed (100% pass rate). |
| 11:16 UTC | Frontend Implementation | `write_to_file` (`index.html`, `styles.css`, `api.js`, `app.js`) | 7 functional screens implemented with vanilla JS and CSS. |

---

## 2. Genuine Defect Discovered and Resolved

### Defect Description
- **Test Case:** `TC-06-11: Concurrent conflicting allocation requests cannot both exceed capacity`
- **Symptom:** During concurrent execution of two simultaneous 70% allocation approval requests on Julia Chen (EMP010), both threads succeeded (`expected: <1> but was: <2>`), resulting in an over-capacity allocation of 140%.
- **Root Cause Analysis:**
  The `approveAllocation` method in `AllocationService` was annotated with `@Transactional(isolation = Isolation.REPEATABLE_READ)`.
  Under `REPEATABLE_READ`, Thread 2's transaction established its MVCC snapshot when it opened. Even though Thread 1 acquired the pessimistic write lock on `Employee`, committed its 70% allocation, and released the lock, Thread 2's subsequent capacity calculation query executed against its initial transaction snapshot, where Thread 1's newly committed row was not visible. Additionally, `lockedEmployee` was read with `PESSIMISTIC_WRITE` but was never modified or flushed, so the lock was released without updating the parent row.
- **Remediation Applied:**
  1. Removed `isolation = Isolation.REPEATABLE_READ` and used default `READ_COMMITTED` transaction semantics. Under `READ_COMMITTED`, once Thread 1 commits and releases the lock, Thread 2's capacity query immediately observes the latest committed data.
  2. Added an explicit touch and flush on the locked employee:
     ```java
     lockedEmployee.setUpdatedAt(LocalDateTime.now());
     employeeRepository.saveAndFlush(lockedEmployee);
     ```
  3. Added `allocationRepository.saveAndFlush(allocation)` so the committed row is immediately persisted to the database prior to transaction completion.
- **Verification:**
  Re-ran `AllocationApprovalAndConcurrencyTest#tc_06_11_concurrentConflictingAllocationRequestsCannotBothExceedCapacity` and the entire test suite. Exactly 1 thread succeeded and 1 thread failed with `CapacityExceededException`. 22/22 tests passed.

---

## 3. Validation Approach & Testing Strategy

- **Isolated Unit & Transactional Testing:** Used `@SpringBootTest` with `@Transactional` rollbacks for domain logic, and multi-threaded `ExecutorService` with `CyclicBarrier` for concurrency tests.
- **Inclusive Date Range Semantics:** Rigorously tested boundaries where allocations end on the exact start date of a new demand to ensure no off-by-one errors.
- **Security & Authorization Enforcement:** Verified with `TestRestTemplate` that unauthenticated and non-manager roles receive `401 Unauthorized` or `403 Forbidden` when attempting allocation approvals.
- **No Inventions Policy:** Verified in `TC-06-10` that missing skills (e.g. Rust, Erlang) result in 0 matching candidates and automatic generation of training recommendations rather than synthetic hallucinated proficiencies.

---

## 4. Known Limitations & Review Notes

- **AI Agent Fallback:** Connects to the deterministic backend engine via the `SimulatedStaffingAgent` adapter since no live LLM API credentials (`AI_API_KEY`) were provided in the environment. The interface `StaffingAgent` is ready for a real Gemini/OpenAI adapter.
- **Database Deployment:** Runs by default in turnkey standalone mode using H2 in MySQL compatibility mode (`MODE=MySQL`), and supports full production MySQL 8.0 via `docker compose up -d` with profile `mysql`.
