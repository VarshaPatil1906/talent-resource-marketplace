-- ====================================================================
-- Talent & Resource Allocation Marketplace - Synthetic Demo & Seed Data
-- ====================================================================

USE talent_marketplace;

-- 1. Insert Skills
INSERT INTO skills (id, name, category) VALUES
(1, 'Java', 'BACKEND'),
(2, 'Spring Boot', 'BACKEND'),
(3, 'MySQL', 'DATABASE'),
(4, 'Python', 'DATA_AI'),
(5, 'Machine Learning', 'DATA_AI'),
(6, 'React', 'FRONTEND'),
(7, 'TypeScript', 'FRONTEND'),
(8, 'AWS', 'CLOUD'),
(9, 'Kubernetes', 'DEVOPS'),
(10, 'Docker', 'DEVOPS'),
(11, 'BigQuery', 'DATABASE'),
(12, 'Terraform', 'CLOUD'),
(13, 'Rust', 'SYSTEMS'),
(14, 'Erlang', 'SYSTEMS')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Insert Synthetic Employees
INSERT INTO employees (id, employee_code, name, location, department, employment_status, created_at, updated_at) VALUES
(1, 'EMP001', 'Alice Johnson', 'London', 'Engineering', 'ACTIVE', '2026-01-10 09:00:00', '2026-01-10 09:00:00'),
(2, 'EMP002', 'Bob Smith', 'New York', 'Engineering', 'ACTIVE', '2026-01-12 09:00:00', '2026-01-12 09:00:00'),
(3, 'EMP003', 'Carlos Mendez', 'Bangalore', 'Cloud & Platform', 'ACTIVE', '2026-02-01 09:00:00', '2026-02-01 09:00:00'),
(4, 'EMP004', 'Divya Nair', 'Bangalore', 'Data & AI', 'ACTIVE', '2026-02-15 09:00:00', '2026-02-15 09:00:00'),
(5, 'EMP005', 'Elena Rostova', 'London', 'Engineering', 'ACTIVE', '2026-03-01 09:00:00', '2026-03-01 09:00:00'),
(6, 'EMP006', 'Frank Miller', 'New York', 'Engineering', 'ACTIVE', '2026-03-10 09:00:00', '2026-03-10 09:00:00'),
(7, 'EMP007', 'Grace Hopper', 'San Francisco', 'Engineering', 'ON_LEAVE', '2026-03-15 09:00:00', '2026-03-15 09:00:00'),
(8, 'EMP008', 'Henry Zhao', 'New York', 'Data & AI', 'ACTIVE', '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(9, 'EMP009', 'Ian Wright', 'London', 'Engineering', 'INACTIVE', '2026-04-10 09:00:00', '2026-04-10 09:00:00'),
(10, 'EMP010', 'Julia Chen', 'San Francisco', 'Cloud & Platform', 'ACTIVE', '2026-05-01 09:00:00', '2026-05-01 09:00:00')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Insert Employee Skills (Proficiency 1-5, null for unvalidated/missing evidence)
INSERT INTO employee_skills (id, employee_id, skill_id, proficiency_level, last_validated_at) VALUES
-- Alice Johnson (EMP001): Strong Java, Spring Boot, MySQL, React
(1, 1, 1, 5, '2026-08-15'),
(2, 1, 2, 4, '2026-08-15'),
(3, 1, 3, 4, '2026-07-20'),
(4, 1, 6, 3, '2026-05-10'),

-- Bob Smith (EMP002): Java, Spring Boot, AWS
(5, 2, 1, 4, '2026-09-01'),
(6, 2, 2, 4, '2026-09-01'),
(7, 2, 8, 3, '2026-06-15'),

-- Carlos Mendez (EMP003): AWS, Kubernetes, Docker, Terraform
(8, 3, 8, 5, '2026-08-20'),
(9, 3, 9, 4, '2026-08-20'),
(10, 3, 10, 4, '2026-07-10'),
(11, 3, 12, 3, '2026-06-01'),

-- Divya Nair (EMP004): Python, Machine Learning, BigQuery
(12, 4, 4, 5, '2026-09-10'),
(13, 4, 5, 4, '2026-09-10'),
(14, 4, 11, 4, '2026-08-05'),

-- Elena Rostova (EMP005): React, TypeScript, Docker
(15, 5, 6, 5, '2026-09-01'),
(16, 5, 7, 4, '2026-09-01'),
(17, 5, 10, 3, '2026-04-12'),

-- Frank Miller (EMP006): Java, Spring Boot (Proficiency 2, below high requirement), MySQL
(18, 6, 1, 3, '2026-07-01'),
(19, 6, 2, 2, '2026-07-01'),
(20, 6, 3, 3, '2026-06-15'),

-- Grace Hopper (EMP007): Java 5, Cloud 5 (ON_LEAVE)
(21, 7, 1, 5, '2026-05-01'),
(22, 7, 8, 5, '2026-05-01'),

-- Henry Zhao (EMP008): Python (Unvalidated proficiency - null!), BigQuery 3
(23, 8, 4, NULL, NULL),
(24, 8, 11, 3, '2026-06-10'),

-- Julia Chen (EMP010): AWS 5, Kubernetes 4, Terraform 4
(25, 10, 8, 5, '2026-08-10'),
(26, 10, 9, 4, '2026-08-10'),
(27, 10, 12, 4, '2026-07-15')
ON DUPLICATE KEY UPDATE proficiency_level=VALUES(proficiency_level);

-- 4. Insert Project Demands
INSERT INTO project_demands (id, project_name, role, start_date, end_date, location, allocation_percent, priority, status, created_at, updated_at) VALUES
(1, 'Core Payment Engine Refactoring', 'Senior Backend Engineer', '2026-11-01', '2027-02-28', 'London', 100, 'HIGH', 'OPEN', '2026-10-01 10:00:00', '2026-10-01 10:00:00'),
(2, 'AI-Powered Fraud Detection', 'Machine Learning Engineer', '2026-11-15', '2027-03-31', 'Bangalore', 50, 'CRITICAL', 'OPEN', '2026-10-02 11:30:00', '2026-10-02 11:30:00'),
(3, 'Cloud Infrastructure Modernization', 'Cloud Architect', '2026-12-01', '2027-04-30', 'San Francisco', 100, 'HIGH', 'OPEN', '2026-10-03 14:00:00', '2026-10-03 14:00:00'),
(4, 'Customer Portal Redesign', 'Senior Frontend Engineer', '2026-10-15', '2027-01-15', 'London', 50, 'MEDIUM', 'STAFFED', '2026-09-25 09:00:00', '2026-10-10 16:00:00'),
(5, 'Legacy Monolith Decommissioning', 'Distributed Systems Specialist', '2026-11-01', '2027-01-31', 'New York', 100, 'LOW', 'OPEN', '2026-10-05 16:30:00', '2026-10-05 16:30:00')
ON DUPLICATE KEY UPDATE project_name=VALUES(project_name);

-- 5. Insert Demand Skills (Mandatory vs Optional, Min Proficiency)
INSERT INTO demand_skills (id, demand_id, skill_id, is_mandatory, min_proficiency) VALUES
-- Demand 1 (Core Payment): Java (Mandatory, Min 4), Spring Boot (Mandatory, Min 3), MySQL (Optional, Min 3)
(1, 1, 1, TRUE, 4),
(2, 1, 2, TRUE, 3),
(3, 1, 3, FALSE, 3),

-- Demand 2 (AI Fraud): Python (Mandatory, Min 4), Machine Learning (Mandatory, Min 4), BigQuery (Optional, Min 3)
(4, 2, 4, TRUE, 4),
(5, 2, 5, TRUE, 4),
(6, 2, 11, FALSE, 3),

-- Demand 3 (Cloud Mod): AWS (Mandatory, Min 4), Kubernetes (Mandatory, Min 3), Terraform (Mandatory, Min 3)
(7, 3, 8, TRUE, 4),
(8, 3, 9, TRUE, 3),
(9, 3, 12, TRUE, 3),

-- Demand 4 (Customer Portal): React (Mandatory, Min 4), TypeScript (Mandatory, Min 3)
(10, 4, 6, TRUE, 4),
(11, 4, 7, TRUE, 3),

-- Demand 5 (Legacy Monolith): Rust (Mandatory, Min 5), Erlang (Mandatory, Min 5) -> No candidate matches!
(12, 5, 13, TRUE, 5),
(13, 5, 14, TRUE, 5)
ON DUPLICATE KEY UPDATE min_proficiency=VALUES(min_proficiency);

-- 6. Insert Allocations (APPROVED, PENDING, CANCELLED)
INSERT INTO allocations (id, employee_id, demand_id, start_date, end_date, allocation_percent, status, approved_by, created_at, updated_at) VALUES
-- Allocation 1: Elena Rostova on Demand 4 (50% approved allocation)
(1, 5, 4, '2026-10-15', '2027-01-15', 50, 'APPROVED', 'manager', '2026-10-10 16:00:00', '2026-10-10 16:00:00'),

-- Allocation 2: Frank Miller has an existing 50% approved commitment
(2, 6, 1, '2026-11-01', '2027-01-31', 50, 'APPROVED', 'manager', '2026-10-05 11:00:00', '2026-10-05 11:00:00'),

-- Allocation 3: Alice Johnson has a PENDING request (50%) - should NOT reduce committed capacity until approved!
(3, 1, 1, '2026-11-01', '2026-12-31', 50, 'PENDING', NULL, '2026-10-12 14:00:00', '2026-10-12 14:00:00'),

-- Allocation 4: Carlos Mendez had an allocation that was CANCELLED - released capacity!
(4, 3, 3, '2026-11-01', '2027-01-31', 100, 'CANCELLED', 'manager', '2026-10-02 09:00:00', '2026-10-08 17:00:00')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- 7. Insert Allocation History
INSERT INTO allocation_history (id, allocation_id, action, changed_by, timestamp, details) VALUES
(1, 1, 'CREATED', 'recruiter', '2026-10-10 15:30:00', 'Initial allocation request submitted for Elena Rostova (50%)'),
(2, 1, 'APPROVED', 'manager', '2026-10-10 16:00:00', 'Manager approved 50% allocation on Customer Portal Redesign'),
(3, 2, 'CREATED', 'recruiter', '2026-10-05 10:30:00', 'Initial allocation request submitted for Frank Miller (50%)'),
(4, 2, 'APPROVED', 'manager', '2026-10-05 11:00:00', 'Manager approved 50% allocation on Core Payment Engine Refactoring'),
(5, 3, 'CREATED', 'recruiter', '2026-10-12 14:00:00', 'Initial allocation request submitted for Alice Johnson (50%) awaiting approval'),
(6, 4, 'CREATED', 'recruiter', '2026-10-02 09:00:00', 'Initial allocation request for Carlos Mendez'),
(7, 4, 'CANCELLED', 'manager', '2026-10-08 17:00:00', 'Project re-scoped. Allocation cancelled and 100% capacity returned to pool.')
ON DUPLICATE KEY UPDATE action=VALUES(action);

-- 8. Insert Training Recommendations
INSERT INTO training_recommendations (id, employee_id, skill_id, reason, learningPath, status, created_at) VALUES
(1, 6, 2, 'Spring Boot proficiency level 2 is below the required level 3 for Senior Backend demands.', 'Advanced Spring Boot 3 & Microservices Architecture (12 hours)', 'RECOMMENDED', '2026-10-11 10:00:00'),
(2, 8, 4, 'Python proficiency is currently unvalidated and missing assessment evidence.', 'Enterprise Python Certification & Data Science Assessment', 'RECOMMENDED', '2026-10-12 11:00:00'),
(3, 2, 8, 'AWS proficiency level 3 can be leveled up to level 4 for Cloud demands.', 'AWS Certified Solutions Architect Professional Track', 'ENROLLED', '2026-10-13 09:30:00')
ON DUPLICATE KEY UPDATE status=VALUES(status);
