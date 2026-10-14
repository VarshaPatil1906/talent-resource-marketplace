-- ==========================================================================================
-- Talent & Resource Allocation Marketplace - 6 Documented Production SQL Queries
-- Database: MySQL 8 (Compatible with H2 MySQL Mode)
-- ==========================================================================================

-- ------------------------------------------------------------------------------------------
-- QUERY 1: Open Demands Grouped by Role and Priority
-- Description:
--   Lists all currently open project demands grouped by role and priority, with required
--   capacity and mandatory skills summary.
-- Assumptions:
--   - Filter: status = 'OPEN'
-- ------------------------------------------------------------------------------------------
SELECT 
    pd.role AS requested_role,
    pd.priority AS priority_level,
    COUNT(pd.id) AS total_open_demands,
    GROUP_CONCAT(DISTINCT pd.project_name ORDER BY pd.project_name SEPARATOR '; ') AS project_names,
    MIN(pd.start_date) AS earliest_start_date,
    MAX(pd.end_date) AS latest_end_date,
    SUM(pd.allocation_percent) AS total_allocation_demand_percent
FROM project_demands pd
WHERE pd.status = 'OPEN'
GROUP BY pd.role, pd.priority
ORDER BY 
    CASE pd.priority
        WHEN 'CRITICAL' THEN 1
        WHEN 'HIGH' THEN 2
        WHEN 'MEDIUM' THEN 3
        WHEN 'LOW' THEN 4
        ELSE 5
    END,
    pd.role ASC;


-- ------------------------------------------------------------------------------------------
-- QUERY 2: Resources Grouped by Skill and Proficiency Level
-- Description:
--   Provides an inventory of available skills and active talent depth, showing how many active
--   employees hold each proficiency level (1 to 5) or are currently unassessed (NULL).
-- Assumptions:
--   - Considers only employees with employment_status = 'ACTIVE'.
--   - Proficiency is on a documented 1 to 5 scale; NULL indicates unvalidated proficiency.
-- ------------------------------------------------------------------------------------------
SELECT 
    s.category AS skill_category,
    s.name AS skill_name,
    COALESCE(CAST(es.proficiency_level AS CHAR), 'UNVALIDATED') AS proficiency_tier,
    COUNT(DISTINCT e.id) AS qualified_employee_count,
    GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') AS employee_roster
FROM skills s
JOIN employee_skills es ON s.id = es.skill_id
JOIN employees e ON es.employee_id = e.id
WHERE e.employment_status = 'ACTIVE'
GROUP BY s.category, s.name, es.proficiency_level
ORDER BY s.category ASC, s.name ASC, es.proficiency_level DESC;


-- ------------------------------------------------------------------------------------------
-- QUERY 3: Resources Available for a Specified Date Range
-- Description:
--   Identifies active employees with their committed allocation and remaining capacity
--   within a target analysis date range, taking overlapping APPROVED allocations into account.
-- Parameters:
--   - @target_start: Start date of the target period (e.g., '2026-11-01')
--   - @target_end:   End date of the target period   (e.g., '2027-01-31')
-- Assumptions:
--   - Inclusive date overlap: (a.start_date <= @target_end AND a.end_date >= @target_start)
--   - Only APPROVED allocations commit capacity. CANCELLED and PENDING are excluded.
--   - Max base capacity defaults to 100%.
-- ------------------------------------------------------------------------------------------
SET @target_start = '2026-11-01';
SET @target_end   = '2027-01-31';

SELECT 
    e.id AS employee_id,
    e.employee_code,
    e.name,
    e.department,
    e.location,
    COALESCE(SUM(
        CASE 
            WHEN a.status = 'APPROVED' 
                 AND a.start_date <= @target_end 
                 AND a.end_date >= @target_start 
            THEN a.allocation_percent 
            ELSE 0 
        END
    ), 0) AS total_committed_percent,
    GREATEST(0, 100 - COALESCE(SUM(
        CASE 
            WHEN a.status = 'APPROVED' 
                 AND a.start_date <= @target_end 
                 AND a.end_date >= @target_start 
            THEN a.allocation_percent 
            ELSE 0 
        END
    ), 0)) AS available_capacity_percent,
    CASE 
        WHEN COALESCE(SUM(
            CASE 
                WHEN a.status = 'APPROVED' 
                     AND a.start_date <= @target_end 
                     AND a.end_date >= @target_start 
                THEN a.allocation_percent 
                ELSE 0 
            END
        ), 0) = 0 THEN 'FULLY_AVAILABLE'
        WHEN COALESCE(SUM(
            CASE 
                WHEN a.status = 'APPROVED' 
                     AND a.start_date <= @target_end 
                     AND a.end_date >= @target_start 
                THEN a.allocation_percent 
                ELSE 0 
            END
        ), 0) < 100 THEN 'PARTIALLY_AVAILABLE'
        ELSE 'FULLY_COMMITTED'
    END AS availability_status
FROM employees e
LEFT JOIN allocations a ON e.id = a.employee_id 
    AND a.status = 'APPROVED'
    AND a.start_date <= @target_end 
    AND a.end_date >= @target_start
WHERE e.employment_status = 'ACTIVE'
GROUP BY e.id, e.employee_code, e.name, e.department, e.location
ORDER BY available_capacity_percent DESC, e.name ASC;


-- ------------------------------------------------------------------------------------------
-- QUERY 4: Current Utilization by Employee and Department
-- Description:
--   Calculates committed allocation capacity and average department utilization
--   based on active approved allocations for the current date.
-- Assumptions:
--   - Evaluated as of CURRENT_DATE.
--   - Grouped by department and employee.
-- ------------------------------------------------------------------------------------------
SELECT 
    e.department,
    e.employee_code,
    e.name AS employee_name,
    COALESCE(SUM(a.allocation_percent), 0) AS committed_utilization_percent,
    GREATEST(0, 100 - COALESCE(SUM(a.allocation_percent), 0)) AS bench_capacity_percent,
    COUNT(a.id) AS active_project_assignments
FROM employees e
LEFT JOIN allocations a ON e.id = a.employee_id 
    AND a.status = 'APPROVED'
    AND a.start_date <= CURRENT_DATE 
    AND a.end_date >= CURRENT_DATE
WHERE e.employment_status = 'ACTIVE'
GROUP BY e.department, e.employee_code, e.name
ORDER BY e.department ASC, committed_utilization_percent DESC;


-- ------------------------------------------------------------------------------------------
-- QUERY 5: Demands with No Eligible Candidate
-- Description:
--   Detects all open project demands where zero active employees satisfy every mandatory
--   skill requirement at or above the minimum required proficiency.
-- Assumptions:
--   - Demand must be OPEN.
--   - Mandatory skills require proficiency_level >= ds.min_proficiency AND NOT NULL.
--   - Active employees only.
-- ------------------------------------------------------------------------------------------
SELECT 
    pd.id AS demand_id,
    pd.project_name,
    pd.role,
    pd.priority,
    pd.start_date,
    pd.end_date,
    pd.allocation_percent,
    GROUP_CONCAT(CONCAT(s.name, ' (Min ', ds.min_proficiency, ')') SEPARATOR ', ') AS mandatory_skills_required
FROM project_demands pd
JOIN demand_skills ds ON pd.id = ds.demand_id AND ds.is_mandatory = TRUE
JOIN skills s ON ds.skill_id = s.id
WHERE pd.status = 'OPEN'
  AND pd.id NOT IN (
      -- Demands that have at least one active candidate meeting ALL mandatory skills:
      SELECT matched.demand_id
      FROM (
          SELECT 
              d_sub.id AS demand_id,
              e_sub.id AS employee_id,
              COUNT(DISTINCT ds_sub.skill_id) AS matched_mandatory_skills
          FROM project_demands d_sub
          JOIN demand_skills ds_sub ON d_sub.id = ds_sub.demand_id AND ds_sub.is_mandatory = TRUE
          JOIN employees e_sub ON e_sub.employment_status = 'ACTIVE'
          JOIN employee_skills es_sub ON e_sub.id = es_sub.employee_id 
              AND es_sub.skill_id = ds_sub.skill_id
              AND es_sub.proficiency_level IS NOT NULL
              AND es_sub.proficiency_level >= ds_sub.min_proficiency
          WHERE d_sub.status = 'OPEN'
          GROUP BY d_sub.id, e_sub.id
      ) matched
      JOIN (
          SELECT demand_id, COUNT(skill_id) AS total_mandatory
          FROM demand_skills
          WHERE is_mandatory = TRUE
          GROUP BY demand_id
      ) req ON matched.demand_id = req.demand_id AND matched.matched_mandatory_skills = req.total_mandatory
  )
GROUP BY pd.id, pd.project_name, pd.role, pd.priority, pd.start_date, pd.end_date, pd.allocation_percent
ORDER BY pd.id ASC;


-- ------------------------------------------------------------------------------------------
-- QUERY 6: Skill Gaps Grouped by Skill Category
-- Description:
--   Quantifies talent deficits by comparing the number of open demands requiring each skill
--   against the number of active employees possessing that skill at level 3 or higher.
-- Assumptions:
--   - Only considers mandatory skill requirements from OPEN project demands.
--   - Qualified threshold set to proficiency_level >= 3.
-- ------------------------------------------------------------------------------------------
SELECT 
    s.category AS skill_category,
    s.name AS skill_name,
    COUNT(DISTINCT ds.demand_id) AS total_open_demands_requiring,
    COUNT(DISTINCT CASE 
        WHEN es.proficiency_level >= 3 AND e.employment_status = 'ACTIVE' 
        THEN es.employee_id 
        ELSE NULL 
    END) AS active_qualified_resources,
    GREATEST(0, COUNT(DISTINCT ds.demand_id) - COUNT(DISTINCT CASE 
        WHEN es.proficiency_level >= 3 AND e.employment_status = 'ACTIVE' 
        THEN es.employee_id 
        ELSE NULL 
    END)) AS deficit_headcount_gap
FROM skills s
JOIN demand_skills ds ON s.id = ds.skill_id AND ds.is_mandatory = TRUE
JOIN project_demands pd ON ds.demand_id = pd.id AND pd.status = 'OPEN'
LEFT JOIN employee_skills es ON s.id = es.skill_id
LEFT JOIN employees e ON es.employee_id = e.id AND e.employment_status = 'ACTIVE'
GROUP BY s.category, s.name
HAVING total_open_demands_requiring > 0
ORDER BY deficit_headcount_gap DESC, total_open_demands_requiring DESC, s.name ASC;
