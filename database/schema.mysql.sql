-- ====================================================================
-- Talent & Resource Allocation Marketplace - MySQL 8 DDL Schema
-- ====================================================================

CREATE DATABASE IF NOT EXISTS talent_marketplace CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE talent_marketplace;

-- 1. Employees Table
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    employment_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_employee_code (employee_code),
    INDEX idx_employee_status (employment_status),
    INDEX idx_employee_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Skills Table
CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    INDEX idx_skill_name (name),
    INDEX idx_skill_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Employee Skills Table (Proficiency 1-5, missing stays NULL, unique per employee & skill)
CREATE TABLE IF NOT EXISTS employee_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    proficiency_level INT NULL,
    last_validated_at DATE NULL,
    CONSTRAINT uk_employee_skill UNIQUE (employee_id, skill_id),
    CONSTRAINT fk_emp_skill_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_emp_skill_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    CONSTRAINT chk_proficiency_range CHECK (proficiency_level IS NULL OR (proficiency_level >= 1 AND proficiency_level <= 5)),
    INDEX idx_emp_skill_employee (employee_id),
    INDEX idx_emp_skill_skill (skill_id),
    INDEX idx_emp_skill_proficiency (proficiency_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Project Demands Table
CREATE TABLE IF NOT EXISTS project_demands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(150) NOT NULL,
    role VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    location VARCHAR(100) NOT NULL,
    allocation_percent INT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_demand_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_demand_allocation CHECK (allocation_percent > 0 AND allocation_percent <= 100),
    INDEX idx_demand_status (status),
    INDEX idx_demand_priority (priority),
    INDEX idx_demand_role (role),
    INDEX idx_demand_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Demand Skills Table (Mandatory flags & minimum proficiencies)
CREATE TABLE IF NOT EXISTS demand_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    demand_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    min_proficiency INT NOT NULL DEFAULT 1,
    CONSTRAINT uk_demand_skill UNIQUE (demand_id, skill_id),
    CONSTRAINT fk_demand_skill_demand FOREIGN KEY (demand_id) REFERENCES project_demands(id) ON DELETE CASCADE,
    CONSTRAINT fk_demand_skill_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    CONSTRAINT chk_min_proficiency_range CHECK (min_proficiency >= 1 AND min_proficiency <= 5),
    INDEX idx_demand_skill_demand (demand_id),
    INDEX idx_demand_skill_skill (skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Allocations Table (PENDING, APPROVED, CANCELLED)
CREATE TABLE IF NOT EXISTS allocations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    demand_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    allocation_percent INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by VARCHAR(100) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_alloc_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE RESTRICT,
    CONSTRAINT fk_alloc_demand FOREIGN KEY (demand_id) REFERENCES project_demands(id) ON DELETE RESTRICT,
    CONSTRAINT chk_alloc_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_alloc_percent CHECK (allocation_percent > 0 AND allocation_percent <= 100),
    INDEX idx_alloc_employee (employee_id),
    INDEX idx_alloc_demand (demand_id),
    INDEX idx_alloc_status (status),
    INDEX idx_alloc_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Allocation History Audit Table
CREATE TABLE IF NOT EXISTS allocation_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    allocation_id BIGINT NOT NULL,
    action VARCHAR(30) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details VARCHAR(1000) NULL,
    CONSTRAINT fk_alloc_hist_alloc FOREIGN KEY (allocation_id) REFERENCES allocations(id) ON DELETE CASCADE,
    INDEX idx_alloc_hist_alloc (allocation_id),
    INDEX idx_alloc_hist_action (action),
    INDEX idx_alloc_hist_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Training Recommendations Table
CREATE TABLE IF NOT EXISTS training_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    reason VARCHAR(500) NOT NULL,
    learning_path VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RECOMMENDED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tr_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_tr_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    INDEX idx_tr_employee (employee_id),
    INDEX idx_tr_skill (skill_id),
    INDEX idx_tr_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
