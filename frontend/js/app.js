/**
 * Talent & Resource Allocation Marketplace - Application Controller
 */

document.addEventListener('DOMContentLoaded', () => {
    App.init();
});

const App = (() => {
    let allSkills = [];
    let allDemands = [];
    let allEmployees = [];
    let currentUser = null;

    // Helper: Toast Notifications
    const showToast = (message, type = 'success') => {
        const container = document.getElementById('toastContainer');
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        container.appendChild(toast);
        setTimeout(() => toast.remove(), 4500);
    };

    const updateDemandStatChips = (demands) => {
        if (!demands) return;
        const total = demands.length;
        const open = demands.filter(d => d.status === 'OPEN').length;
        const staffed = demands.filter(d => d.status === 'STAFFED').length;
        const elTotal = document.getElementById('statTotalDemands');
        const elOpen = document.getElementById('statOpenDemands');
        const elStaffed = document.getElementById('statStaffedDemands');
        if (elTotal) elTotal.textContent = total;
        if (elOpen) elOpen.textContent = open;
        if (elStaffed) elStaffed.textContent = staffed;
    };

    // Active Role UI updates and Backend Session Synchronization
    const updateRoleUI = (user) => {
        if (!user) return;

        // Top-right Active Role Badge
        const badge = document.getElementById('activeRoleBadge');
        if (badge) {
            badge.textContent = user.activeRole;
            badge.className = `badge badge-${user.activeRole.toLowerCase()}`;
        }

        const role = user.activeRole;

        // Subtitles across all 7 screens
        const s1Sub = document.getElementById('screen1Subtitle');
        const s2Sub = document.getElementById('screen2Subtitle');
        const s3Sub = document.getElementById('screen3Subtitle');
        const s4Sub = document.getElementById('screen4Subtitle');
        const s5Sub = document.getElementById('screen5Subtitle');
        const s6Sub = document.getElementById('screen6Subtitle');
        const s7Sub = document.getElementById('screen7Subtitle');

        if (role === 'RECRUITER') {
            if (s1Sub) s1Sub.textContent = 'Submit new talent requirements with explicit mandatory skills and minimum proficiency levels.';
            if (s2Sub) s2Sub.textContent = 'Inspect verified employee proficiencies (1 to 5) and search synthetic talent across departments.';
            if (s3Sub) s3Sub.textContent = 'Query talent by required capacity, skill proficiency, and dates. Request allocation for qualified candidates.';
            if (s4Sub) s4Sub.textContent = 'Side-by-side evaluation of candidates against a demand with deterministic score breakdowns (0 to 100).';
            if (s5Sub) s5Sub.textContent = 'Track requested allocation commitments and review approval progress in real time (Read-Only).';
            if (s6Sub) s6Sub.textContent = 'Real-time capacity tracking, unstaffed demand alerts, and bench availability for resource planning.';
            if (s7Sub) s7Sub.textContent = 'Direct the autonomous staffing workflow to discover candidates, verify evidence, and formulate actionable proposals.';
        } else if (role === 'MANAGER') {
            if (s1Sub) s1Sub.textContent = 'Inspect project demand requirements and staffing statuses before reviewing candidate matches and approvals.';
            if (s2Sub) s2Sub.textContent = 'Review employee proficiencies (1 to 5), track assessment validation dates, and record skill endorsements.';
            if (s3Sub) s3Sub.textContent = 'Query team capacity and talent availability across overlapping date ranges to plan resource commitments.';
            if (s4Sub) s4Sub.textContent = 'Review candidate fit scores (0 to 100), evaluate skill match evidence, and submit or review allocation proposals.';
            if (s5Sub) s5Sub.textContent = 'Manager-authorized commitment review. Revalidates demand status, skills, and committed capacity at approval time.';
            if (s6Sub) s6Sub.textContent = 'Departmental capacity tracking, team utilization percentages, and skill gap deficits across active projects.';
            if (s7Sub) s7Sub.textContent = 'Execute autonomous staffing workflows to inspect candidate reasoning, verify tool traces, and review proposed allocations.';
        } else if (role === 'ADMIN') {
            if (s1Sub) s1Sub.textContent = 'Audit talent requirements, skill criteria, and staffing parameters across all projects.';
            if (s2Sub) s2Sub.textContent = 'Manage organizational skill taxonomy, view employee proficiencies (1 to 5), and maintain skill assessments.';
            if (s3Sub) s3Sub.textContent = 'Audit talent availability across all departments and monitor capacity distribution (Read-Only).';
            if (s4Sub) s4Sub.textContent = 'Audit candidate scoring algorithms, formula weights (Skills 40% | Prof 30% | Cap 20% | Loc 10%), and match evidence.';
            if (s5Sub) s5Sub.textContent = 'Governance and compliance audit log. View allocation statuses, commit timestamps, and audit history (Audit View).';
            if (s6Sub) s6Sub.textContent = 'Organizational talent utilization, department-level distribution metrics, and enterprise talent deficit reports.';
            if (s7Sub) s7Sub.textContent = 'Audit autonomous staffing tool executions, deterministic reasoning traces, and training pathway recommendations.';
        }

        // Screen 1: Demand Intake Form & Overview Layout
        const demandGrid = document.getElementById('demandGridContainer');
        const demandCardTitle = document.getElementById('demandFormCardTitle');
        const demandReadOnlySummary = document.getElementById('demandReadOnlySummary');
        const demandRoleNotice = document.getElementById('demandRoleNotice');
        const demandRoleGuide = document.getElementById('demandRoleGuide');
        const demandForm = document.getElementById('demandIntakeForm');
        const btnSubmitDemand = document.getElementById('btnSubmitDemand');
        const btnAddSkill = document.getElementById('btnAddSkillRequirement');

        const canCreate = !!user.permissions?.canCreateDemand;
        if (canCreate) {
            if (demandGrid) demandGrid.classList.remove('admin-mode');
            if (demandCardTitle) demandCardTitle.textContent = 'New Demand Specification';
            if (demandReadOnlySummary) demandReadOnlySummary.classList.add('hidden');
            if (demandRoleNotice) {
                demandRoleNotice.classList.add('hidden');
                demandRoleNotice.innerHTML = '';
            }
            if (demandForm) {
                demandForm.style.display = 'block';
                demandForm.classList.remove('form-disabled-overlay');
                Array.from(demandForm.elements).forEach(el => el.disabled = false);
            }
            if (btnAddSkill) btnAddSkill.disabled = false;
            if (btnSubmitDemand) {
                btnSubmitDemand.disabled = false;
                btnSubmitDemand.textContent = 'Create Project Demand';
                btnSubmitDemand.className = 'btn btn-primary';
            }
        } else {
            if (demandGrid) demandGrid.classList.add('admin-mode');
            if (demandCardTitle) demandCardTitle.textContent = 'Project Demands Overview';
            if (demandForm) {
                demandForm.style.display = 'none';
                demandForm.classList.add('form-disabled-overlay');
            }
            if (demandReadOnlySummary) {
                demandReadOnlySummary.classList.remove('hidden');
            }
            if (demandRoleNotice) {
                demandRoleNotice.classList.remove('hidden');
                demandRoleNotice.className = 'role-restriction-notice info';
                if (role === 'ADMIN') {
                    demandRoleNotice.innerHTML = '<span>ℹ️ <strong>Demand creation is restricted for Admin.</strong> Recruiters are responsible for creating project demands.</span>';
                } else if (role === 'MANAGER') {
                    demandRoleNotice.innerHTML = '<span>ℹ️ <strong>Demand creation is restricted for Manager.</strong> Recruiters are responsible for creating project demands.</span>';
                } else {
                    demandRoleNotice.innerHTML = `<span>ℹ️ <strong>Demand creation is restricted.</strong> Recruiters are responsible for creating project demands.</span>`;
                }
            }
            if (demandRoleGuide) {
                if (role === 'ADMIN') {
                    demandRoleGuide.innerHTML = '💡 <strong>Admin Responsibilities:</strong> Click <strong>👁️ Admin View</strong> on any project demand in the table to inspect detailed specifications, required competencies, and criteria. System taxonomy and employee records are managed in <strong>Skills Matrix</strong>.';
                } else if (role === 'MANAGER') {
                    demandRoleGuide.innerHTML = '💡 <strong>Manager Responsibilities:</strong> Review project requirements in the table and click <strong>Evaluate</strong> to inspect ranked candidate matches. Navigate to <strong>Allocation Approval</strong> to review and commit pending requests.';
                }
            }
            if (allDemands && allDemands.length > 0) {
                updateDemandStatChips(allDemands);
            } else {
                API.getDemands().then(demands => {
                    allDemands = demands;
                    updateDemandStatChips(demands);
                }).catch(() => {});
            }
        }

        // Screen 2: Skills Matrix Actions
        const btnOpenAddEmployee = document.getElementById('btnOpenAddEmployeeModal');
        if (btnOpenAddEmployee) {
            btnOpenAddEmployee.style.display = user.permissions?.canManageEmployees ? 'inline-flex' : 'none';
        }
        const btnOpenTaxonomy = document.getElementById('btnOpenAddTaxonomyModal');
        if (btnOpenTaxonomy) {
            btnOpenTaxonomy.style.display = user.permissions?.canManageSkills ? 'inline-flex' : 'none';
        }
        const btnOpenAddSkill = document.getElementById('btnOpenAddSkillModal');
        if (btnOpenAddSkill) {
            btnOpenAddSkill.style.display = user.permissions?.canAddEmployeeSkill ? 'inline-flex' : 'none';
        }

        // Screen 4: Candidate Comparison Actions & Notice
        const compNotice = document.getElementById('comparisonRoleNotice');
        const btnRunComp = document.getElementById('btnRunComparison');
        const selectCompDemand = document.getElementById('comparisonDemandSelect');
        if (compNotice && btnRunComp) {
            if (role === 'ADMIN') {
                compNotice.classList.remove('hidden');
                compNotice.className = 'role-restriction-notice info';
                compNotice.innerHTML = '<span>ℹ️ <strong>Admin Audit Mode:</strong> You are inspecting deterministic candidate scoring formulas (Skills 40% | Prof 30% | Cap 20% | Loc 10%). Allocation requests are submitted by Recruiters and Managers.</span>';
                btnRunComp.textContent = '🔍 Inspect Candidate Scoring (Audit View)';
                btnRunComp.disabled = false;
                if (selectCompDemand) selectCompDemand.disabled = false;
            } else {
                compNotice.classList.add('hidden');
                compNotice.innerHTML = '';
                btnRunComp.textContent = 'Evaluate Candidates';
                btnRunComp.disabled = false;
                if (selectCompDemand) selectCompDemand.disabled = false;
            }
        }

        // Screen 5: Allocation Approval Auth Pill
        const screen5AuthText = document.getElementById('screen5AuthText');
        const screen5AuthPill = document.getElementById('screen5AuthPill');
        if (screen5AuthText) {
            if (user.permissions?.canApproveAllocation) {
                screen5AuthText.textContent = `${user.activeRole} Authorized (Commit & Reject)`;
                if (screen5AuthPill) screen5AuthPill.style.borderColor = 'rgba(16, 185, 129, 0.4)';
            } else if (role === 'RECRUITER') {
                screen5AuthText.textContent = 'Read-Only: Recruiter cannot approve/reject';
                if (screen5AuthPill) screen5AuthPill.style.borderColor = 'rgba(245, 158, 11, 0.4)';
            } else {
                screen5AuthText.textContent = 'Audit View: Manager authorization required';
                if (screen5AuthPill) screen5AuthPill.style.borderColor = 'rgba(244, 114, 182, 0.4)';
            }
        }

        // Screen 7: AI Staffing Agent Notice & Execute Button
        const aiRoleNotice = document.getElementById('aiRoleNotice');
        const btnExecuteAi = document.getElementById('btnExecuteAiWorkflow');
        const aiDemandSelect = document.getElementById('aiDemandSelect');
        const aiGoalInput = document.getElementById('aiGoalInput');
        if (aiRoleNotice && btnExecuteAi) {
            if (role === 'ADMIN') {
                aiRoleNotice.classList.remove('hidden');
                aiRoleNotice.className = 'role-restriction-notice info';
                aiRoleNotice.innerHTML = '<span>ℹ️ <strong>Admin Audit Mode:</strong> You are auditing the 6 deterministic agent tools and simulated reasoning paths. Allocation requests are submitted by Recruiters and Managers.</span>';
                btnExecuteAi.textContent = '🔍 Audit Tool Execution Trace';
                btnExecuteAi.disabled = false;
                if (aiDemandSelect) aiDemandSelect.disabled = false;
                if (aiGoalInput) aiGoalInput.disabled = false;
            } else {
                aiRoleNotice.classList.add('hidden');
                aiRoleNotice.innerHTML = '';
                btnExecuteAi.textContent = '⚡ Execute Agentic Staffing Workflow';
                btnExecuteAi.disabled = false;
                if (aiDemandSelect) aiDemandSelect.disabled = false;
                if (aiGoalInput) aiGoalInput.disabled = false;
            }
        }
    };

    const initAuth = async () => {
        const roleSelect = document.getElementById('userRoleSelect');
        if (!roleSelect) return;

        const handleRoleSwitch = async (roleName) => {
            let password = 'password123';
            try {
                const user = await API.login(roleName, password);
                currentUser = user;
                updateRoleUI(user);
                showToast(`Switched active identity to ${user.username.toUpperCase()} [${user.activeRole}]`, 'info');

                // Refresh current screen to reflect role-based permissions immediately
                const activeNav = document.querySelector('.nav-item.active');
                const screenId = activeNav ? activeNav.getAttribute('data-screen') : 'screen-demand-intake';
                if (screenId === 'screen-demand-intake') loadDemands();
                if (screenId === 'screen-skills-matrix') loadSkillsMatrix();
                if (screenId === 'screen-resource-search') loadResourceSearch();
                if (screenId === 'screen-candidate-comparison') loadComparisonDemands();
                if (screenId === 'screen-allocation-approval') loadAllocations();
                if (screenId === 'screen-dashboard') loadDashboard();
                if (screenId === 'screen-ai-assistant') loadAiAssistant();
            } catch (err) {
                showToast(`Failed to switch role to ${roleName}: ` + err.message, 'error');
            }
        };

        roleSelect.addEventListener('change', (e) => {
            handleRoleSwitch(e.target.value);
        });

        // Initialize and sync backend session with current dropdown value
        await handleRoleSwitch(roleSelect.value || 'manager');
    };

    // Navigation & Screen Switching
    const initNavigation = () => {
        const navItems = document.querySelectorAll('.nav-item');
        const screens = document.querySelectorAll('.screen-view');

        const switchScreen = (screenId) => {
            navItems.forEach(item => {
                if (item.getAttribute('data-screen') === screenId) {
                    item.classList.add('active');
                } else {
                    item.classList.remove('active');
                }
            });

            screens.forEach(screen => {
                if (screen.id === screenId) {
                    screen.classList.add('active');
                } else {
                    screen.classList.remove('active');
                }
            });

            // Trigger screen specific refresh
            if (screenId === 'screen-demand-intake') loadDemands();
            if (screenId === 'screen-skills-matrix') loadSkillsMatrix();
            if (screenId === 'screen-resource-search') loadResourceSearch();
            if (screenId === 'screen-candidate-comparison') loadComparisonDemands();
            if (screenId === 'screen-allocation-approval') loadAllocations();
            if (screenId === 'screen-dashboard') loadDashboard();
            if (screenId === 'screen-ai-assistant') loadAiAssistant();
        };

        navItems.forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                const screenId = item.getAttribute('data-screen');
                window.location.hash = screenId;
                switchScreen(screenId);
            });
        });

        // Handle initial hash routing
        const currentHash = window.location.hash.replace('#', '') || 'screen-demand-intake';
        switchScreen(currentHash);

        window.addEventListener('hashchange', () => {
            const hash = window.location.hash.replace('#', '') || 'screen-demand-intake';
            switchScreen(hash);
        });
    };

    // Global Data Loading
    const loadGlobalData = async () => {
        try {
            allSkills = await API.getSkills();
            populateSkillDropdowns();
        } catch (err) {
            console.error('Failed to load global skills:', err);
            const skillsList = document.getElementById('demandSkillsList');
            if (skillsList) {
                skillsList.innerHTML = `<div class="skill-empty-state text-danger">⚠️ Failed to load skills from database: ${escapeHtml(err.message || 'Server connection error')}. Please check backend.</div>`;
            }
        }
    };

    const populateSkillDropdowns = () => {
        const modalSkillSelect = document.getElementById('modalSkillSelect');
        const searchSkillSelect = document.getElementById('searchSkillSelect');

        if (modalSkillSelect) {
            modalSkillSelect.innerHTML = allSkills.map(s => 
                `<option value="${s.id}">${escapeHtml(s.name)} (${escapeHtml(s.category)})</option>`
            ).join('');
        }

        if (searchSkillSelect) {
            searchSkillSelect.innerHTML = '<option value="">Any Skill</option>' + 
                allSkills.map(s => `<option value="${s.id}">${escapeHtml(s.name)} (${escapeHtml(s.category)})</option>`).join('');
        }
    };

    // ====================================================================
    // SCREEN 1: Demand Intake Form & Demands List
    // ====================================================================
    const initDemandIntake = () => {
        const form = document.getElementById('demandIntakeForm');
        const btnAddSkill = document.getElementById('btnAddSkillRequirement');
        const skillsList = document.getElementById('demandSkillsList');
        const btnRefresh = document.getElementById('btnRefreshDemands');

        // Set default dates: next month
        const today = new Date();
        const nextMonth = new Date(today.getFullYear(), today.getMonth() + 1, 1);
        const nextQuarter = new Date(today.getFullYear(), today.getMonth() + 4, 0);

        const startDateInput = document.getElementById('projectStartDate');
        const endDateInput = document.getElementById('projectEndDate');
        if (startDateInput) startDateInput.value = nextMonth.toISOString().split('T')[0];
        if (endDateInput) endDateInput.value = nextQuarter.toISOString().split('T')[0];

        // Updates options across all rows to prevent duplicate selection of the same skill
        const updateSkillDropdownOptions = () => {
            const rows = skillsList.querySelectorAll('.skill-spec-row');
            if (rows.length === 0) return;

            // Collect all currently selected skills
            const selectedSkills = new Set();
            rows.forEach(r => {
                const val = r.querySelector('.skill-name-select')?.value;
                if (val) selectedSkills.add(val);
            });

            // Update each row's select options to disable already chosen skills
            rows.forEach(r => {
                const select = r.querySelector('.skill-name-select');
                if (!select) return;
                const currentVal = select.value;

                Array.from(select.options).forEach(opt => {
                    const skillName = opt.value;
                    const isSelectedElsewhere = selectedSkills.has(skillName) && skillName !== currentVal;
                    opt.disabled = isSelectedElsewhere;

                    const origSkill = allSkills.find(s => s.name === skillName);
                    const category = origSkill ? origSkill.category : '';
                    if (isSelectedElsewhere) {
                        opt.textContent = `${skillName} (${category}) — [already added]`;
                    } else {
                        opt.textContent = `${skillName} (${category})`;
                    }
                });
            });

            // If all catalog skills have been added, disable "+ Add Skill" button with explanatory tooltip
            if (btnAddSkill) {
                const allSelected = allSkills.length > 0 && selectedSkills.size >= allSkills.length;
                btnAddSkill.disabled = allSelected;
                btnAddSkill.title = allSelected ? 'All available skills from catalog have already been added' : 'Add another skill requirement';
            }
        };

        const addSkillRow = (defaultSkillName = '', mandatory = true, minProf = 3) => {
            if (!allSkills || allSkills.length === 0) {
                skillsList.innerHTML = `<div class="skill-empty-state text-warning">⚠️ No skills available from backend. Please refresh or verify database initialization.</div>`;
                return;
            }

            // Remove empty placeholder message if present
            const emptyMsg = skillsList.querySelector('.skill-empty-state');
            if (emptyMsg) emptyMsg.remove();

            // Find current selected skills to pick a unique unselected skill
            const existingSelects = Array.from(skillsList.querySelectorAll('.skill-name-select'));
            const alreadySelected = new Set(existingSelects.map(s => s.value));

            let skillToSelect = defaultSkillName;
            if (!skillToSelect || alreadySelected.has(skillToSelect)) {
                const unselected = allSkills.find(s => !alreadySelected.has(s.name));
                if (!unselected) {
                    showToast('All available catalog skills have already been added to this demand.', 'warning');
                    return;
                }
                skillToSelect = unselected.name;
            }

            const row = document.createElement('div');
            row.className = 'skill-spec-row';
            row.innerHTML = `
                <select class="form-control skill-name-select" required>
                    ${allSkills.map(s => `
                        <option value="${escapeHtml(s.name)}" ${s.name === skillToSelect ? 'selected' : ''}>
                            ${escapeHtml(s.name)} (${escapeHtml(s.category)})
                        </option>
                    `).join('')}
                </select>
                <select class="form-control skill-prof-select" required>
                    <option value="1" ${minProf === 1 ? 'selected' : ''}>Level 1 (Basic)</option>
                    <option value="2" ${minProf === 2 ? 'selected' : ''}>Level 2 (Novice)</option>
                    <option value="3" ${minProf === 3 ? 'selected' : ''}>Level 3 (Competent)</option>
                    <option value="4" ${minProf === 4 ? 'selected' : ''}>Level 4 (Advanced)</option>
                    <option value="5" ${minProf === 5 ? 'selected' : ''}>Level 5 (Expert)</option>
                </select>
                <label class="skill-mandatory-label">
                    <input type="checkbox" class="skill-mandatory-check" ${mandatory ? 'checked' : ''}>
                    Mandatory
                </label>
                <button type="button" class="btn btn-sm btn-danger btn-remove-skill" title="Remove skill">&times;</button>
            `;

            // On skill selection change, immediately enforce duplicate prevention
            const nameSelect = row.querySelector('.skill-name-select');
            nameSelect.addEventListener('change', () => {
                updateSkillDropdownOptions();
            });

            // On remove, remove row and update remaining rows
            row.querySelector('.btn-remove-skill').addEventListener('click', () => {
                row.remove();
                if (skillsList.querySelectorAll('.skill-spec-row').length === 0) {
                    skillsList.innerHTML = `<div class="skill-empty-state">No skill requirements added. Click "+ Add Skill" above to add required skills.</div>`;
                }
                updateSkillDropdownOptions();
            });

            skillsList.appendChild(row);
            updateSkillDropdownOptions();
        };

        btnAddSkill.addEventListener('click', () => addSkillRow());

        // Default with sample skill rows from loaded skills
        skillsList.innerHTML = '';
        if (allSkills && allSkills.length > 0) {
            const hasJava = allSkills.some(s => s.name === 'Java');
            const hasSpringBoot = allSkills.some(s => s.name === 'Spring Boot');
            addSkillRow(hasJava ? 'Java' : allSkills[0].name, true, 4);
            if (allSkills.length > 1) {
                addSkillRow(hasSpringBoot ? 'Spring Boot' : allSkills[1].name, true, 3);
            }
        } else {
            skillsList.innerHTML = `<div class="skill-empty-state">Loading skills from database...</div>`;
        }

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (!currentUser?.permissions?.canCreateDemand) {
                showToast('Demand creation is restricted to Recruiters.', 'error');
                return;
            }
            const btnSubmit = document.getElementById('btnSubmitDemand');
            btnSubmit.disabled = true;
            btnSubmit.textContent = 'Submitting...';

            try {
                const requiredSkills = [];
                const rows = skillsList.querySelectorAll('.skill-spec-row');
                
                if (rows.length === 0) {
                    throw new Error('Please add at least one required skill requirement for the demand.');
                }

                const seenSkills = new Set();
                let duplicateDetected = false;

                rows.forEach(row => {
                    const skillName = row.querySelector('.skill-name-select').value;
                    const minProficiency = parseInt(row.querySelector('.skill-prof-select').value, 10);
                    const mandatory = row.querySelector('.skill-mandatory-check').checked;

                    if (seenSkills.has(skillName)) {
                        duplicateDetected = true;
                    }
                    seenSkills.add(skillName);

                    requiredSkills.push({ skillName, minProficiency, mandatory });
                });

                if (duplicateDetected) {
                    throw new Error('Duplicate skill requirements detected. Each skill must be specified only once.');
                }

                const payload = {
                    projectName: document.getElementById('projectName').value.trim(),
                    role: document.getElementById('projectRole').value.trim(),
                    location: document.getElementById('projectLocation').value,
                    startDate: document.getElementById('projectStartDate').value,
                    endDate: document.getElementById('projectEndDate').value,
                    allocationPercent: parseInt(document.getElementById('projectAllocation').value, 10),
                    priority: document.getElementById('projectPriority').value,
                    status: 'OPEN',
                    requiredSkills
                };

                const created = await API.createDemand(payload);
                showToast(`Project demand "${created.projectName}" created successfully!`);
                form.reset();
                if (startDateInput) startDateInput.value = nextMonth.toISOString().split('T')[0];
                if (endDateInput) endDateInput.value = nextQuarter.toISOString().split('T')[0];
                skillsList.innerHTML = '';
                if (allSkills && allSkills.length > 0) {
                    const hasJava = allSkills.some(s => s.name === 'Java');
                    const hasSpringBoot = allSkills.some(s => s.name === 'Spring Boot');
                    addSkillRow(hasJava ? 'Java' : allSkills[0].name, true, 4);
                    if (allSkills.length > 1) addSkillRow(hasSpringBoot ? 'Spring Boot' : allSkills[1].name, true, 3);
                }
                loadDemands();
            } catch (err) {
                showToast(err.message, 'error');
            } finally {
                btnSubmit.disabled = false;
                btnSubmit.textContent = 'Create Project Demand';
            }
        });

        btnRefresh?.addEventListener('click', loadDemands);
        document.getElementById('btnCloseDemandModal')?.addEventListener('click', () => {
            document.getElementById('demandDetailsModal').classList.add('hidden');
        });
        document.getElementById('btnDismissDemandModal')?.addEventListener('click', () => {
            document.getElementById('demandDetailsModal').classList.add('hidden');
        });
    };

    const loadDemands = async () => {
        const tbody = document.getElementById('demandsTableBody');
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4">Loading demands...</td></tr>';

        try {
            allDemands = await API.getDemands();
            updateDemandStatChips(allDemands);

            if (allDemands.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No project demands found.</td></tr>';
                return;
            }

            tbody.innerHTML = allDemands.map(d => {
                const priorityBadge = d.priority === 'CRITICAL' ? 'badge-danger' :
                                      d.priority === 'HIGH' ? 'badge-warning' : 'badge-accent';
                const statusBadge = d.status === 'STAFFED' ? 'badge-success' :
                                    d.status === 'CANCELLED' ? 'badge-danger' : 'badge-warning';

                return `
                    <tr>
                        <td>
                            <strong style="display: block; font-size: 0.85rem;">${escapeHtml(d.projectName)}</strong>
                            <div class="text-muted" style="font-size: 0.75rem;">${escapeHtml(d.role)}</div>
                        </td>
                        <td>
                            <div style="white-space: nowrap; font-size: 0.8rem;">${d.startDate}</div>
                            <div class="text-muted" style="white-space: nowrap; font-size: 0.72rem;">to ${d.endDate}</div>
                        </td>
                        <td>
                            <span style="white-space: nowrap; font-size: 0.8rem;">${escapeHtml(d.location)}</span>
                            <div class="text-muted" style="font-size: 0.72rem;">${d.allocationPercent}% cap</div>
                        </td>
                        <td><span class="badge ${priorityBadge}">${d.priority}</span></td>
                        <td><span class="badge ${statusBadge}">${d.status}</span></td>
                        <td>
                            ${currentUser?.permissions?.canEvaluateDemands ? `
                                <button class="btn btn-sm btn-outline" onclick="App.evaluateDemand(${d.id})" style="white-space: nowrap;">Evaluate</button>
                            ` : `
                                <button class="btn btn-sm btn-outline admin-view-btn" onclick="App.openAdminViewDemand(${d.id})" style="white-space: nowrap;">👁️ Admin View</button>
                            `}
                        </td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">Failed to load demands: ${escapeHtml(err.message)}</td></tr>`;
        }
    };

    // ====================================================================
    // SCREEN 2: Employee Skills Matrix
    // ====================================================================
    const initSkillsMatrix = () => {
        const deptFilter = document.getElementById('matrixFilterDept');
        const statusFilter = document.getElementById('matrixFilterStatus');
        const searchInput = document.getElementById('matrixSearchInput');
        const btnOpenModal = document.getElementById('btnOpenAddSkillModal');
        const addSkillModal = document.getElementById('addSkillModal');
        const btnCloseModal = document.getElementById('btnCloseAddSkillModal');
        const btnCancelModal = document.getElementById('btnCancelAddSkillModal');
        const addSkillForm = document.getElementById('addSkillForm');

        const applyFilter = () => loadSkillsMatrix();

        deptFilter?.addEventListener('change', applyFilter);
        statusFilter?.addEventListener('change', applyFilter);
        searchInput?.addEventListener('input', debounce(applyFilter, 300));

        // Global Add Employee Skill Modal opener
        btnOpenModal?.addEventListener('click', () => {
            populateModalEmployeeSelect();
            document.getElementById('modalEmployeeSelectGroup')?.classList.remove('hidden');
            document.getElementById('modalEmployeeReadOnlyGroup')?.classList.add('hidden');
            const hiddenId = document.getElementById('modalEmployeeIdHidden');
            if (hiddenId) hiddenId.value = '';
            const modalTitle = document.getElementById('addSkillModalTitle');
            if (modalTitle) modalTitle.textContent = 'Add / Update Employee Skill';

            const skillSelect = document.getElementById('modalSkillSelect');
            if (skillSelect && skillSelect.options.length > 0) skillSelect.selectedIndex = 0;
            const profSelect = document.getElementById('modalProficiencySelect');
            if (profSelect) profSelect.value = '3';
            const dateInput = document.getElementById('modalValidationDate');
            if (dateInput) dateInput.value = new Date().toISOString().split('T')[0];

            addSkillModal.classList.remove('hidden');
        });

        [btnCloseModal, btnCancelModal].forEach(btn => {
            btn?.addEventListener('click', () => addSkillModal.classList.add('hidden'));
        });

        addSkillForm?.addEventListener('submit', async (e) => {
            e.preventDefault();
            // Determine employee ID: row-specific locked field takes precedence when active
            const hiddenEmpId = document.getElementById('modalEmployeeIdHidden')?.value;
            const isRowSpecific = !document.getElementById('modalEmployeeReadOnlyGroup')?.classList.contains('hidden') && hiddenEmpId;
            const empId = isRowSpecific ? parseInt(hiddenEmpId, 10) : parseInt(document.getElementById('modalEmployeeSelect')?.value, 10);

            if (!empId) {
                showToast('Please select a valid employee.', 'error');
                return;
            }

            const skillId = document.getElementById('modalSkillSelect')?.value;
            if (!skillId) {
                showToast('Please select a skill.', 'error');
                return;
            }
            const proficiencyLevel = parseInt(document.getElementById('modalProficiencySelect').value, 10);
            const lastValidatedAt = document.getElementById('modalValidationDate').value || null;

            try {
                await API.addEmployeeSkill(empId, { skillId: parseInt(skillId, 10), proficiencyLevel, lastValidatedAt });
                showToast('Skill assessment saved successfully!');
                addSkillModal.classList.add('hidden');
                loadSkillsMatrix();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });

        // Admin-only Add Employee Modal
        const btnOpenAddEmp = document.getElementById('btnOpenAddEmployeeModal');
        const addEmpModal = document.getElementById('addEmployeeModal');
        const btnCloseAddEmp = document.getElementById('btnCloseAddEmployeeModal');
        const btnCancelAddEmp = document.getElementById('btnCancelAddEmployeeModal');
        const addEmpForm = document.getElementById('addEmployeeForm');

        btnOpenAddEmp?.addEventListener('click', () => {
            addEmpForm?.reset();
            addEmpModal?.classList.remove('hidden');
            document.getElementById('empModalCode')?.focus();
        });

        [btnCloseAddEmp, btnCancelAddEmp].forEach(btn => {
            btn?.addEventListener('click', () => addEmpModal?.classList.add('hidden'));
        });

        addEmpForm?.addEventListener('submit', async (e) => {
            e.preventDefault();
            const employeeCode = document.getElementById('empModalCode').value.trim();
            const name = document.getElementById('empModalName').value.trim();
            const department = document.getElementById('empModalDept').value;
            const location = document.getElementById('empModalLocation').value;
            const employmentStatus = document.getElementById('empModalStatus').value;

            if (!employeeCode || !name || !department || !location) {
                showToast('Please fill out all required fields.', 'error');
                return;
            }

            const btnSubmit = document.getElementById('btnSubmitAddEmployee');
            if (btnSubmit) {
                btnSubmit.disabled = true;
                btnSubmit.textContent = 'Saving...';
            }

            try {
                const created = await API.createEmployee({
                    employeeCode,
                    name,
                    department,
                    location,
                    employmentStatus
                });
                showToast(`Employee "${created.name}" (${created.employeeCode}) created successfully!`, 'success');
                addEmpModal?.classList.add('hidden');
                addEmpForm.reset();
                await loadSkillsMatrix();
                populateModalEmployeeSelect();
            } catch (err) {
                showToast(err.message, 'error');
            } finally {
                if (btnSubmit) {
                    btnSubmit.disabled = false;
                    btnSubmit.textContent = 'Create Employee';
                }
            }
        });

        // Admin-only Skill Taxonomy Modal
        const btnOpenTaxonomy = document.getElementById('btnOpenAddTaxonomyModal');
        const addTaxonomyModal = document.getElementById('addTaxonomyModal');
        const btnCloseTaxonomy = document.getElementById('btnCloseAddTaxonomyModal');
        const btnCancelTaxonomy = document.getElementById('btnCancelAddTaxonomyModal');
        const addTaxonomyForm = document.getElementById('addTaxonomyForm');

        btnOpenTaxonomy?.addEventListener('click', () => {
            addTaxonomyModal?.classList.remove('hidden');
        });

        [btnCloseTaxonomy, btnCancelTaxonomy].forEach(btn => {
            btn?.addEventListener('click', () => addTaxonomyModal?.classList.add('hidden'));
        });

        addTaxonomyForm?.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('taxonomySkillName').value.trim();
            const category = document.getElementById('taxonomyCategorySelect').value;
            const description = document.getElementById('taxonomyDescription').value.trim();

            try {
                await API.createSkill({ name, category, description });
                showToast(`New skill '${name}' added to taxonomy by Admin!`);
                addTaxonomyModal?.classList.add('hidden');
                addTaxonomyForm.reset();
                await loadGlobalData();
                loadSkillsMatrix();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    };

    const populateModalEmployeeSelect = () => {
        const select = document.getElementById('modalEmployeeSelect');
        if (!select || !allEmployees) return;
        select.innerHTML = allEmployees.map(e => 
            `<option value="${e.id}">${escapeHtml(e.name)} (${e.employeeCode} - ${escapeHtml(e.department)})</option>`
        ).join('');
    };

    const loadSkillsMatrix = async () => {
        const tbody = document.getElementById('skillsMatrixTableBody');
        tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4">Loading skills matrix...</td></tr>';

        const dept = document.getElementById('matrixFilterDept')?.value || '';
        const status = document.getElementById('matrixFilterStatus')?.value || '';
        const search = (document.getElementById('matrixSearchInput')?.value || '').trim().toLowerCase();

        try {
            const params = {};
            if (dept && dept.toUpperCase() !== 'ALL') params.department = dept;
            if (status && status.toUpperCase() !== 'ALL') params.status = status;

            const fetchedEmployees = await API.getEmployees(params);

            // Maintain allEmployees with a full superset for modal lookups
            if (!params.department && !params.status) {
                allEmployees = fetchedEmployees;
            } else if (!allEmployees || allEmployees.length === 0) {
                API.getEmployees().then(all => { if (all) allEmployees = all; }).catch(() => {});
            } else {
                fetchedEmployees.forEach(fe => {
                    const idx = allEmployees.findIndex(e => e.id === fe.id);
                    if (idx >= 0) allEmployees[idx] = fe;
                    else allEmployees.push(fe);
                });
            }

            // Apply all selected filters simultaneously (Status + Department + Quick Search)
            let filtered = fetchedEmployees.filter(emp => {
                // 1. Status Filter: exact match against employmentStatus
                if (status && status !== '' && status.toUpperCase() !== 'ALL') {
                    if (!emp.employmentStatus || emp.employmentStatus.toUpperCase() !== status.toUpperCase()) {
                        return false;
                    }
                }

                // 2. Department Filter: match against department
                if (dept && dept !== '' && dept.toUpperCase() !== 'ALL') {
                    if (!emp.department || emp.department.toLowerCase() !== dept.toLowerCase()) {
                        return false;
                    }
                }

                // 3. Quick Search Filter: matches name, employee code, department, location, or skills
                if (search) {
                    const matchName = emp.name && emp.name.toLowerCase().includes(search);
                    const matchCode = emp.employeeCode && emp.employeeCode.toLowerCase().includes(search);
                    const matchDept = emp.department && emp.department.toLowerCase().includes(search);
                    const matchLocation = emp.location && emp.location.toLowerCase().includes(search);
                    const matchSkill = emp.skills && emp.skills.some(s => 
                        s.skillName && s.skillName.toLowerCase().includes(search)
                    );
                    if (!matchName && !matchCode && !matchDept && !matchLocation && !matchSkill) {
                        return false;
                    }
                }

                return true;
            });

            if (filtered.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No employees match filter criteria.</td></tr>';
                return;
            }

            const canEditSkills = !!currentUser?.permissions?.canAddEmployeeSkill;

            tbody.innerHTML = filtered.map(emp => {
                const statusBadge = emp.employmentStatus === 'ACTIVE' ? 'badge-success' :
                                    emp.employmentStatus === 'ON_LEAVE' ? 'badge-warning' : 'badge-danger';

                const skillsBadges = emp.skills.map(s => {
                    const stars = s.proficiencyLevel ? '★'.repeat(s.proficiencyLevel) : 'Unvalidated';
                    const starClass = s.proficiencyLevel ? 'proficiency-stars' : 'text-muted';
                    const dateTooltip = s.lastValidatedAt ? `Validated: ${s.lastValidatedAt}` : 'Not validated';
                    const clickAttr = canEditSkills 
                        ? `onclick="App.openEditSkillForEmp(${emp.id}, ${s.skillId}, ${s.proficiencyLevel || 3}, '${s.lastValidatedAt || ''}')"` 
                        : '';
                    const editHint = canEditSkills ? ' (Click to edit assessment)' : '';
                    return `
                        <span class="skill-pill ${canEditSkills ? 'clickable' : ''}" title="${dateTooltip}${editHint}" ${clickAttr}>
                            <strong>${escapeHtml(s.skillName)}</strong>
                            <span class="${starClass}">[${stars}]</span>
                        </span>
                    `;
                }).join(' ') || '<span class="text-muted">No recorded skills</span>';

                return `
                    <tr>
                        <td><code>${emp.employeeCode}</code></td>
                        <td><strong>${escapeHtml(emp.name)}</strong></td>
                        <td>${escapeHtml(emp.department)}</td>
                        <td>${escapeHtml(emp.location)}</td>
                        <td><span class="badge ${statusBadge}">${emp.employmentStatus}</span></td>
                        <td>${skillsBadges}</td>
                        <td>
                            ${canEditSkills ? `
                                <button class="btn btn-sm btn-outline" onclick="App.openAddSkillForEmp(${emp.id})">+ Skill</button>
                            ` : `
                                <span class="badge badge-subtle">Verified</span>
                            `}
                        </td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger py-4">Error loading employees: ${escapeHtml(err.message)}</td></tr>`;
        }
    };

    const openAddSkillForEmp = (empId) => {
        const emp = allEmployees && allEmployees.find(e => e.id === empId);
        const empName = emp ? emp.name : `Employee #${empId}`;
        const empCode = emp ? emp.employeeCode : '';
        const empDept = emp ? emp.department : '';

        // Row-specific locked display
        const hiddenId = document.getElementById('modalEmployeeIdHidden');
        if (hiddenId) hiddenId.value = empId;
        const readOnlyText = document.getElementById('modalEmployeeReadOnlyText');
        if (readOnlyText) {
            readOnlyText.innerHTML = `<strong>${escapeHtml(empName)}</strong> <span class="text-muted">(${escapeHtml(empCode)} - ${escapeHtml(empDept)})</span>`;
        }

        document.getElementById('modalEmployeeSelectGroup')?.classList.add('hidden');
        document.getElementById('modalEmployeeReadOnlyGroup')?.classList.remove('hidden');

        const modalTitle = document.getElementById('addSkillModalTitle');
        if (modalTitle) modalTitle.textContent = `Add Skill for ${empName}`;

        const skillSelect = document.getElementById('modalSkillSelect');
        if (skillSelect && skillSelect.options.length > 0) skillSelect.selectedIndex = 0;
        const profSelect = document.getElementById('modalProficiencySelect');
        if (profSelect) profSelect.value = '3';
        const dateInput = document.getElementById('modalValidationDate');
        if (dateInput) dateInput.value = new Date().toISOString().split('T')[0];

        document.getElementById('addSkillModal').classList.remove('hidden');
    };

    const openEditSkillForEmp = (empId, skillId, proficiency, lastValidatedAt) => {
        if (!currentUser?.permissions?.canAddEmployeeSkill) {
            showToast('You are not authorized to edit skill assessments.', 'warning');
            return;
        }
        const emp = allEmployees && allEmployees.find(e => e.id === empId);
        const empName = emp ? emp.name : `Employee #${empId}`;
        const empCode = emp ? emp.employeeCode : '';
        const empDept = emp ? emp.department : '';

        const hiddenId = document.getElementById('modalEmployeeIdHidden');
        if (hiddenId) hiddenId.value = empId;
        const readOnlyText = document.getElementById('modalEmployeeReadOnlyText');
        if (readOnlyText) {
            readOnlyText.innerHTML = `<strong>${escapeHtml(empName)}</strong> <span class="text-muted">(${escapeHtml(empCode)} - ${escapeHtml(empDept)})</span>`;
        }

        document.getElementById('modalEmployeeSelectGroup')?.classList.add('hidden');
        document.getElementById('modalEmployeeReadOnlyGroup')?.classList.remove('hidden');

        const skillObj = allSkills && allSkills.find(s => s.id === skillId);
        const skillName = skillObj ? skillObj.name : 'Skill';

        const modalTitle = document.getElementById('addSkillModalTitle');
        if (modalTitle) modalTitle.textContent = `Edit Skill: ${skillName} (${empName})`;

        const skillSelect = document.getElementById('modalSkillSelect');
        if (skillSelect) skillSelect.value = skillId;
        const profSelect = document.getElementById('modalProficiencySelect');
        if (profSelect) profSelect.value = proficiency || 3;
        const dateInput = document.getElementById('modalValidationDate');
        if (dateInput) dateInput.value = lastValidatedAt || '';

        document.getElementById('addSkillModal').classList.remove('hidden');
    };

    // ====================================================================
    // SCREEN 3: Resource Search and Ranking
    // ====================================================================
    const initResourceSearch = () => {
        const form = document.getElementById('resourceSearchForm');

        const today = new Date();
        const start = new Date(today.getFullYear(), today.getMonth() + 1, 1).toISOString().split('T')[0];
        const end = new Date(today.getFullYear(), today.getMonth() + 3, 0).toISOString().split('T')[0];

        document.getElementById('searchStartDate').value = start;
        document.getElementById('searchEndDate').value = end;

        form.addEventListener('submit', (e) => {
            e.preventDefault();
            loadResourceSearch();
        });
    };

    const loadResourceSearch = async () => {
        const container = document.getElementById('resourceSearchResults');
        container.innerHTML = '<div class="text-center py-4 text-muted w-full">Searching available resources...</div>';

        const params = {
            startDate: document.getElementById('searchStartDate').value,
            endDate: document.getElementById('searchEndDate').value,
            minCapacityPercent: document.getElementById('searchCapacity').value,
            skillId: document.getElementById('searchSkillSelect').value,
            minProficiency: document.getElementById('searchMinProf').value
        };

        try {
            const resources = await API.getAvailableResources(params);

            if (resources.length === 0) {
                container.innerHTML = `
                    <div class="empty-state w-full text-center py-4">
                        <div class="empty-icon">🔍</div>
                        <h3>No Resources Available</h3>
                        <p>No active resources meet the capacity and skill criteria for the requested dates.</p>
                    </div>
                `;
                return;
            }

            container.innerHTML = resources.map(res => {
                const availBadge = res.fullyAvailable ? 'badge-success' : (res.partiallyAvailable ? 'badge-warning' : 'badge-danger');
                const availText = res.fullyAvailable ? 'Fully Available' : (res.partiallyAvailable ? 'Partially Available' : 'Committed');

                return `
                    <div class="resource-card">
                        <div>
                            <div class="flex-between mb-2">
                                <div>
                                    <h4 style="font-size: 1.1rem; font-weight: 700;">${escapeHtml(res.name)}</h4>
                                    <div class="text-muted" style="font-size: 0.8rem;"><code>${res.employeeCode}</code> | ${escapeHtml(res.department)}</div>
                                </div>
                                <span class="badge ${availBadge}">${availText}</span>
                            </div>

                            <div style="font-size: 0.85rem; margin-bottom: 0.75rem;">
                                📍 <strong>Location:</strong> ${escapeHtml(res.location)}
                            </div>

                            <div class="score-breakdown-row">
                                <div class="score-breakdown-labels">
                                    <span>Available Capacity:</span>
                                    <strong>${res.minAvailableCapacityPercent}%</strong>
                                </div>
                                <div class="progress-track">
                                    <div class="progress-fill" style="width: ${res.minAvailableCapacityPercent}%; background-color: ${res.minAvailableCapacityPercent >= 50 ? 'var(--success)' : 'var(--warning)'};"></div>
                                </div>
                            </div>

                            <div class="score-breakdown-row">
                                <div class="score-breakdown-labels">
                                    <span>Committed Peak:</span>
                                    <span>${res.maxConcurrentCommittedPercent}%</span>
                                </div>
                            </div>
                        </div>

                        ${currentUser?.permissions?.canRequestAllocation ? `
                        <div class="mt-3">
                            <button class="btn btn-sm btn-primary w-full" onclick="App.prepareAllocationForResource(${res.employeeId}, '${escapeHtml(res.name)}')">
                                Request Allocation
                            </button>
                        </div>
                        ` : ''}
                    </div>
                `;
            }).join('');
        } catch (err) {
            container.innerHTML = `<div class="text-danger py-4">Error searching resources: ${escapeHtml(err.message)}</div>`;
        }
    };

    // ====================================================================
    // SCREEN 4: Candidate Comparison & Evaluation
    // ====================================================================
    const initCandidateComparison = () => {
        const select = document.getElementById('comparisonDemandSelect');
        const btnRun = document.getElementById('btnRunComparison');

        btnRun?.addEventListener('click', () => {
            const demandId = select.value;
            if (!demandId) {
                showToast('Please select a project demand first', 'error');
                return;
            }
            renderCandidateComparison(demandId);
        });
    };

    const loadComparisonDemands = async () => {
        const select = document.getElementById('comparisonDemandSelect');
        try {
            allDemands = await API.getDemands();
            select.innerHTML = '<option value="">Select an open project demand...</option>' +
                allDemands.map(d => `<option value="${d.id}">${escapeHtml(d.projectName)} (${d.role} - ${d.location})</option>`).join('');
        } catch (err) {
            console.error('Failed to load demands for comparison:', err);
        }
    };

    const renderCandidateComparison = async (demandId) => {
        const container = document.getElementById('comparisonContainer');
        container.innerHTML = '<div class="text-center py-4 text-muted w-full">Evaluating candidate pool against demand rules...</div>';

        try {
            const candidates = await API.getDemandRecommendations(demandId);

            if (candidates.length === 0) {
                container.innerHTML = '<div class="empty-state"><h3>No Candidates Found</h3></div>';
                return;
            }

            container.innerHTML = candidates.slice(0, 3).map((c, index) => {
                const isTop = index === 0 && c.eligible;
                const statusBadge = c.eligible ? 'badge-success' : 'badge-danger';
                const statusText = c.eligible ? 'ELIGIBLE' : 'INELIGIBLE';

                const matchedList = c.matchedSkills.map(m => 
                    `<li>✅ <strong>${escapeHtml(m.skillName)}</strong>: Level ${m.actualProficiency}/${m.requiredProficiency}</li>`
                ).join('') || '<li class="text-muted">None</li>';

                const missingList = c.missingSkills.map(m => 
                    `<li class="text-danger">❌ ${escapeHtml(m.reason)}</li>`
                ).join('') || '<li class="text-success">None</li>';

                const exclusionList = c.exclusionReasons.map(r => 
                    `<li class="text-danger" style="font-size: 0.8rem;">⚠️ ${escapeHtml(r)}</li>`
                ).join('');

                return `
                    <div class="comparison-card ${isTop ? 'top-pick' : ''}">
                        ${isTop ? '<div class="top-pick-ribbon">⭐ Top Recommendation</div>' : ''}
                        <div class="flex-between">
                            <div>
                                <h3 style="font-size: 1.2rem; font-weight: 700;">${escapeHtml(c.name)}</h3>
                                <div class="text-muted" style="font-size: 0.8rem;"><code>${c.employeeCode}</code> | ${escapeHtml(c.department)}</div>
                            </div>
                            <span class="badge ${statusBadge}">${statusText}</span>
                        </div>

                        <div class="flex-between mt-3 mb-3">
                            <div class="score-circle">
                                ${Math.round(c.overallScore)}
                                <span>/ 100</span>
                            </div>
                            <div style="font-size: 0.85rem; line-height: 1.6;">
                                📍 <strong>Location:</strong> ${escapeHtml(c.location)}<br>
                                ⚡ <strong>Available:</strong> ${c.availableCapacityPercent}%<br>
                                🎯 <strong>Skill Fit:</strong> ${c.scoreBreakdown.skillMatchScore}/40
                            </div>
                        </div>

                        <!-- Breakdown bars -->
                        <div class="score-breakdown-row">
                            <div class="score-breakdown-labels">
                                <span>Skill Match (40 pts):</span>
                                <span>${c.scoreBreakdown.skillMatchScore}/40</span>
                            </div>
                            <div class="progress-track"><div class="progress-fill" style="width: ${(c.scoreBreakdown.skillMatchScore/40)*100}%;"></div></div>
                        </div>
                        <div class="score-breakdown-row">
                            <div class="score-breakdown-labels">
                                <span>Proficiency Bonus (30 pts):</span>
                                <span>${c.scoreBreakdown.proficiencyScore}/30</span>
                            </div>
                            <div class="progress-track"><div class="progress-fill" style="width: ${(c.scoreBreakdown.proficiencyScore/30)*100}%;"></div></div>
                        </div>
                        <div class="score-breakdown-row">
                            <div class="score-breakdown-labels">
                                <span>Capacity Score (20 pts):</span>
                                <span>${c.scoreBreakdown.capacityScore}/20</span>
                            </div>
                            <div class="progress-track"><div class="progress-fill" style="width: ${(c.scoreBreakdown.capacityScore/20)*100}%;"></div></div>
                        </div>
                        <div class="score-breakdown-row">
                            <div class="score-breakdown-labels">
                                <span>Location Fit (10 pts):</span>
                                <span>${c.scoreBreakdown.locationScore}/10</span>
                            </div>
                            <div class="progress-track"><div class="progress-fill" style="width: ${(c.scoreBreakdown.locationScore/10)*100}%;"></div></div>
                        </div>

                        <!-- Matched & Missing details -->
                        <div style="margin-top: 1rem; border-top: 1px solid var(--border-color); padding-top: 0.75rem;">
                            <strong style="font-size: 0.8rem;">Skill Match Evidence:</strong>
                            <ul style="font-size: 0.8rem; list-style: none; margin-top: 0.35rem;">
                                ${matchedList}
                            </ul>
                        </div>

                        ${exclusionList ? `
                        <div style="margin-top: 0.75rem; background-color: var(--danger-bg); padding: 0.6rem; border-radius: var(--radius-sm);">
                            <strong style="font-size: 0.75rem; color: var(--danger);">Exclusion Factors:</strong>
                            <ul style="list-style: none; margin-top: 0.25rem;">
                                ${exclusionList}
                            </ul>
                        </div>
                        ` : ''}

                        ${currentUser?.permissions?.canRequestAllocation ? `
                        <div class="mt-4">
                            <button class="btn btn-sm ${c.eligible ? 'btn-primary' : 'btn-outline'} w-full"
                                    onclick="App.requestAllocationForDemand(${c.employeeId}, ${demandId})">
                                ${c.eligible ? 'Submit Allocation Request' : 'Propose with Waiver'}
                            </button>
                        </div>
                        ` : `
                        <div class="mt-4 text-center text-muted" style="font-size: 0.78rem;">
                            Allocation requests submitted by Recruiters / Managers
                        </div>
                        `}
                    </div>
                `;
            }).join('');
        } catch (err) {
            container.innerHTML = `<div class="text-danger py-4">Error comparing candidates: ${escapeHtml(err.message)}</div>`;
        }
    };

    // ====================================================================
    // SCREEN 5: Allocation Approval Workflow
    // ====================================================================
    const initAllocations = () => {
        document.getElementById('btnRefreshAllocations')?.addEventListener('click', loadAllocations);
        document.getElementById('btnCloseHistoryModal')?.addEventListener('click', () => {
            document.getElementById('historyModal').classList.add('hidden');
        });
    };

    const loadAllocations = async () => {
        const pendingTbody = document.getElementById('pendingAllocationsTableBody');
        const committedTbody = document.getElementById('committedAllocationsTableBody');
        const badge = document.getElementById('pendingBadge');

        pendingTbody.innerHTML = '<tr><td colspan="7" class="text-center py-4">Loading pending allocations...</td></tr>';
        committedTbody.innerHTML = '<tr><td colspan="8" class="text-center py-4">Loading committed allocations...</td></tr>';

        try {
            const allocations = await API.getAllocations();

            const pending = allocations.filter(a => a.status === 'PENDING');
            const committed = allocations.filter(a => a.status !== 'PENDING');

            badge.textContent = pending.length;
            badge.style.display = pending.length > 0 ? 'inline-block' : 'none';

            const canApprove = currentUser?.permissions?.canApproveAllocation;
            const canCancel = currentUser?.permissions?.canCancelAllocation;

            // Render Pending Table
            if (pending.length === 0) {
                pendingTbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No pending allocations awaiting approval.</td></tr>';
            } else {
                pendingTbody.innerHTML = pending.map(a => `
                    <tr>
                        <td><code>#${a.id}</code></td>
                        <td><strong>${escapeHtml(a.employeeName)}</strong> <span class="text-muted">(${a.employeeCode})</span></td>
                        <td>${escapeHtml(a.projectName)}</td>
                        <td>${a.startDate} to ${a.endDate}</td>
                        <td><strong>${a.allocationPercent}%</strong></td>
                        <td><span class="badge badge-warning">PENDING</span></td>
                        <td>
                            ${canApprove ? `
                                <button class="btn btn-sm btn-success" onclick="App.approveAllocation(${a.id})">Approve (Lock & Commit)</button>
                                <button class="btn btn-sm btn-danger" onclick="App.rejectAllocation(${a.id})">Reject</button>
                            ` : (currentUser?.activeRole === 'RECRUITER' ? `
                                <span class="badge badge-warning">Awaiting Manager</span>
                                <span class="text-muted" style="font-size: 0.75rem; margin-right: 0.5rem;">Read-only for Recruiter</span>
                            ` : `
                                <span class="badge badge-muted">Manager Authorization Required</span>
                                <span class="text-muted" style="font-size: 0.75rem; margin-right: 0.5rem;">Audit View</span>
                            `)}
                            <button class="btn btn-sm btn-outline" onclick="App.viewAuditHistory(${a.id})">History</button>
                        </td>
                    </tr>
                `).join('');
            }

            // Render Committed Table
            if (committed.length === 0) {
                committedTbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">No committed allocations found.</td></tr>';
            } else {
                committedTbody.innerHTML = committed.map(a => {
                    const statusBadge = a.status === 'APPROVED' ? 'badge-success' : 'badge-danger';
                    return `
                        <tr>
                            <td><code>#${a.id}</code></td>
                            <td><strong>${escapeHtml(a.employeeName)}</strong> <span class="text-muted">(${a.employeeCode})</span></td>
                            <td>${escapeHtml(a.projectName)}</td>
                            <td>${a.startDate} to ${a.endDate}</td>
                            <td><strong>${a.allocationPercent}%</strong></td>
                            <td><span class="badge ${statusBadge}">${a.status}</span></td>
                            <td>${escapeHtml(a.approvedBy || '-')}</td>
                            <td>
                                ${canCancel && a.status === 'APPROVED' ? `<button class="btn btn-sm btn-outline" style="color: var(--danger);" onclick="App.cancelAllocation(${a.id})">Cancel Allocation</button>` : ''}
                                <button class="btn btn-sm btn-outline" onclick="App.viewAuditHistory(${a.id})">History</button>
                            </td>
                        </tr>
                    `;
                }).join('');
            }
        } catch (err) {
            showToast('Failed to load allocations: ' + err.message, 'error');
        }
    };

    const approveAllocation = async (id) => {
        try {
            const approved = await API.approveAllocation(id);
            showToast(`Allocation #${id} approved! Capacity committed and revalidated against concurrency locks.`);
            loadAllocations();
        } catch (err) {
            showToast(err.message, 'error');
        }
    };

    const rejectAllocation = async (id) => {
        const reason = prompt('Please enter reason for rejection (optional):');
        if (reason === null) return;
        try {
            await API.rejectAllocation(id, reason);
            showToast(`Allocation #${id} rejected.`);
            loadAllocations();
        } catch (err) {
            showToast(err.message, 'error');
        }
    };

    const cancelAllocation = async (id) => {
        const reason = prompt('Please enter cancellation reason (capacity will be released back to the talent pool):');
        if (reason === null) return;
        try {
            await API.cancelAllocation(id, reason);
            showToast(`Allocation #${id} cancelled! Capacity released back to talent pool.`);
            loadAllocations();
        } catch (err) {
            showToast(err.message, 'error');
        }
    };

    const viewAuditHistory = async (id) => {
        const modal = document.getElementById('historyModal');
        const tbody = document.getElementById('historyTableBody');
        tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4">Loading audit history...</td></tr>';
        modal.classList.remove('hidden');

        try {
            const history = await API.getAllocationHistory(id);
            if (history.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4 text-muted">No history entries recorded.</td></tr>';
                return;
            }

            tbody.innerHTML = history.map(h => {
                const actionBadge = h.action === 'APPROVED' ? 'badge-success' :
                                    h.action === 'CANCELLED' ? 'badge-danger' :
                                    h.action === 'REJECTED' ? 'badge-danger' : 'badge-accent';
                return `
                    <tr>
                        <td>${new Date(h.timestamp).toLocaleString()}</td>
                        <td><span class="badge ${actionBadge}">${h.action}</span></td>
                        <td><strong>${escapeHtml(h.changedBy)}</strong></td>
                        <td>${escapeHtml(h.details)}</td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="4" class="text-center text-danger py-4">Failed to load history: ${escapeHtml(err.message)}</td></tr>`;
        }
    };

    // ====================================================================
    // SCREEN 6: Utilization and Skill-Gap Dashboard
    // ====================================================================
    const initDashboard = () => {
        document.getElementById('btnRefreshDashboard')?.addEventListener('click', loadDashboard);
    };

    const loadDashboard = async () => {
        try {
            const report = await API.getUtilizationReport();

            document.getElementById('kpiUtilization').textContent = `${report.overallUtilizationPercent}%`;
            document.getElementById('kpiActiveEmployees').textContent = report.activeEmployees;
            document.getElementById('kpiOpenDemands').textContent = report.openDemandsCount;
            document.getElementById('kpiUnstaffedDemands').textContent = report.unstaffedDemandsCount;

            // Render Department Utilization
            const deptContainer = document.getElementById('departmentUtilizationContainer');
            deptContainer.innerHTML = report.departmentUtilizations.map(d => `
                <div class="score-breakdown-row" style="margin-bottom: 1rem;">
                    <div class="score-breakdown-labels">
                        <strong>${escapeHtml(d.department)} (${d.employeeCount} engineers)</strong>
                        <strong>${d.averageUtilizationPercent}%</strong>
                    </div>
                    <div class="progress-track" style="height: 10px;">
                        <div class="progress-fill" style="width: ${d.averageUtilizationPercent}%; background-color: ${d.averageUtilizationPercent > 80 ? 'var(--warning)' : 'var(--primary)'};"></div>
                    </div>
                </div>
            `).join('');

            // Render Skill Gaps Table
            const gapsTbody = document.getElementById('skillGapsTableBody');
            if (report.skillGaps.length === 0) {
                gapsTbody.innerHTML = '<tr><td colspan="5" class="text-center py-4 text-muted">No talent deficits identified.</td></tr>';
            } else {
                gapsTbody.innerHTML = report.skillGaps.map(g => `
                    <tr>
                        <td><span class="badge badge-accent">${escapeHtml(g.category)}</span></td>
                        <td><strong>${escapeHtml(g.skillName)}</strong></td>
                        <td>${g.requiredCount}</td>
                        <td>${g.qualifiedEmployeeCount}</td>
                        <td><strong style="color: ${g.gapCount > 0 ? 'var(--danger)' : 'var(--success)'};">${g.gapCount}</strong></td>
                    </tr>
                `).join('');
            }

            // Render Employee Utilization Table
            const empTbody = document.getElementById('employeeUtilizationTableBody');
            empTbody.innerHTML = report.employeeUtilizations.map(eu => `
                <tr>
                    <td><code>${eu.employeeCode}</code></td>
                    <td><strong>${escapeHtml(eu.name)}</strong></td>
                    <td>${escapeHtml(eu.department)}</td>
                    <td>${escapeHtml(eu.location)}</td>
                    <td>
                        <div class="score-breakdown-labels" style="font-size: 0.75rem;">
                            <span>${eu.committedPercent}% committed</span>
                        </div>
                        <div class="progress-track">
                            <div class="progress-fill" style="width: ${eu.committedPercent}%; background-color: ${eu.committedPercent >= 80 ? 'var(--warning)' : 'var(--primary)'};"></div>
                        </div>
                    </td>
                    <td><strong style="color: var(--success);">${eu.remainingPercent}%</strong></td>
                    <td>${eu.activeAllocationCount}</td>
                </tr>
            `).join('');

        } catch (err) {
            showToast('Failed to load dashboard metrics: ' + err.message, 'error');
        }
    };

    // ====================================================================
    // SCREEN 7: AI Staffing Assistant (Simulated AI)
    // ====================================================================
    const initAiAssistant = () => {
        const form = document.getElementById('aiAgentForm');
        form?.addEventListener('submit', async (e) => {
            e.preventDefault();
            const demandId = document.getElementById('aiDemandSelect').value;
            const goal = document.getElementById('aiGoalInput').value.trim();

            if (!demandId) {
                showToast('Please select a target project demand', 'error');
                return;
            }

            const btn = document.getElementById('btnExecuteAiWorkflow');
            btn.disabled = true;
            btn.textContent = '⏳ Agent Running 6 Backend Tools...';

            const outputContainer = document.getElementById('aiOutputContainer');
            outputContainer.classList.remove('hidden');

            const traceList = document.getElementById('aiTraceList');
            traceList.innerHTML = '<li>Starting agentic workflow initialization...</li>';

            try {
                const response = await API.getAiStaffingAdvice(demandId, goal);

                // Render Trace
                traceList.innerHTML = response.toolExecutionTrace.map(t => 
                    `<li>${escapeHtml(t)}</li>`
                ).join('');

                // Render Top Recommendation
                const topCard = document.getElementById('aiTopRecommendationCard');
                if (response.topCandidate) {
                    const tc = response.topCandidate;
                    topCard.innerHTML = `
                        <div class="card-header flex-between bg-dark-subtle">
                            <h3>Recommended Candidate Fact Sheet</h3>
                            <span class="badge ${tc.eligible ? 'badge-success' : 'badge-danger'}">${tc.eligible ? 'ELIGIBLE' : 'DEFICIT FLAGGED'}</span>
                        </div>
                        <div class="card-body">
                            <div class="flex-between">
                                <div>
                                    <h4 style="font-size: 1.3rem; font-weight: 700;">${escapeHtml(tc.name)}</h4>
                                    <div class="text-muted"><code>${tc.employeeCode}</code> | ${escapeHtml(tc.department)} | 📍 ${escapeHtml(tc.location)}</div>
                                </div>
                                <div class="score-circle">
                                    ${Math.round(tc.overallScore)}
                                    <span>/ 100</span>
                                </div>
                            </div>

                            <div style="margin-top: 1rem; background-color: var(--bg-surface); padding: 1rem; border-radius: var(--radius-md); border-left: 3px solid var(--primary);">
                                <strong style="color: var(--primary);">Agent Strategic Rationale:</strong>
                                <p style="font-size: 0.9rem; margin-top: 0.5rem; white-space: pre-line;">${escapeHtml(response.strategicRationale)}</p>
                            </div>

                            <div class="mt-4 flex-between">
                                <span class="text-muted" style="font-size: 0.8rem;">Protected Governance Gate: Agent submits proposal as PENDING for manager commit.</span>
                                ${currentUser?.permissions?.canRequestAllocation ? `
                                <button class="btn btn-primary" onclick="App.requestAllocationForDemand(${tc.employeeId}, ${response.demandId})">
                                    Submit for Manager Approval
                                </button>
                                ` : `
                                <span class="badge badge-muted">Admin Oversight View</span>
                                `}
                            </div>
                        </div>
                    `;
                } else {
                    topCard.innerHTML = `
                        <div class="card-body text-center py-4">
                            <h4>No matching candidates could be determined for this demand.</h4>
                        </div>
                    `;
                }

                // Render Training Recommendations
                const trainingContainer = document.getElementById('aiTrainingContainer');
                if (response.trainingRecommendations && response.trainingRecommendations.length > 0) {
                    trainingContainer.innerHTML = `
                        <div class="card-header">
                            <h3>Identified Skill Gaps & Automated Training Pathways</h3>
                        </div>
                        <div class="card-body p-0">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Employee</th>
                                        <th>Skill</th>
                                        <th>Gap Reason</th>
                                        <th>Prescribed Learning Path</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${response.trainingRecommendations.map(tr => `
                                        <tr>
                                            <td><strong>${escapeHtml(tr.employeeName)}</strong></td>
                                            <td><span class="badge badge-accent">${escapeHtml(tr.skillName)}</span></td>
                                            <td>${escapeHtml(tr.reason)}</td>
                                            <td><code>${escapeHtml(tr.learningPath)}</code></td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                    `;
                } else {
                    trainingContainer.innerHTML = '';
                }

                showToast('Agentic workflow completed successfully!');
            } catch (err) {
                showToast('Agent workflow error: ' + err.message, 'error');
            } finally {
                btn.disabled = false;
                btn.textContent = '⚡ Execute Agentic Staffing Workflow';
            }
        });
    };

    const loadAiAssistant = async () => {
        const select = document.getElementById('aiDemandSelect');
        try {
            allDemands = await API.getDemands();
            select.innerHTML = '<option value="">Select open demand...</option>' +
                allDemands.map(d => `<option value="${d.id}">${escapeHtml(d.projectName)} (${d.role} - ${d.location})</option>`).join('');
        } catch (err) {
            console.error('Failed to load demands for AI assistant:', err);
        }
    };

    // Helper Actions
    const evaluateDemand = (demandId) => {
        if (!currentUser?.permissions?.canEvaluateDemands) {
            showToast('Demand evaluation is restricted for your current role.', 'warning');
            return;
        }
        window.location.hash = 'screen-candidate-comparison';
        const select = document.getElementById('comparisonDemandSelect');
        if (select) select.value = demandId;
        renderCandidateComparison(demandId);
    };

    const openAdminViewDemand = async (demandId) => {
        try {
            const d = (allDemands && allDemands.find(item => item.id === demandId)) || await API.getDemandById(demandId);
            document.getElementById('demandModalTitle').textContent = d.projectName;
            document.getElementById('demandModalSubtitle').textContent = `${d.role} (${d.location})`;
            document.getElementById('demandModalProjectName').textContent = d.projectName;
            document.getElementById('demandModalRole').textContent = d.role;
            document.getElementById('demandModalLocation').textContent = d.location;
            document.getElementById('demandModalCapacity').textContent = `${d.allocationPercent}%`;
            document.getElementById('demandModalStartDate').textContent = d.startDate;
            document.getElementById('demandModalEndDate').textContent = d.endDate;

            const priorityBadge = d.priority === 'CRITICAL' ? 'badge-danger' :
                                  d.priority === 'HIGH' ? 'badge-warning' : 'badge-accent';
            const statusBadge = d.status === 'STAFFED' ? 'badge-success' :
                                d.status === 'CANCELLED' ? 'badge-danger' : 'badge-warning';

            document.getElementById('demandModalPriority').innerHTML = `<span class="badge ${priorityBadge}">${d.priority}</span>`;
            document.getElementById('demandModalStatus').innerHTML = `<span class="badge ${statusBadge}">${d.status}</span>`;

            const skillsTbody = document.getElementById('demandModalSkillsTbody');
            if (!d.requiredSkills || d.requiredSkills.length === 0) {
                skillsTbody.innerHTML = '<tr><td colspan="3" class="text-center py-2 text-muted">No specific skills configured for this demand.</td></tr>';
            } else {
                skillsTbody.innerHTML = d.requiredSkills.map(s => {
                    const typeBadge = s.mandatory ? '<span class="badge badge-danger">⭐ Mandatory</span>' : '<span class="badge badge-muted">Optional</span>';
                    return `
                        <tr>
                            <td><strong>${escapeHtml(s.skillName)}</strong></td>
                            <td>${typeBadge}</td>
                            <td>Level ${s.minProficiencyLevel} (Proficiency 1-5)</td>
                        </tr>
                    `;
                }).join('');
            }

            document.getElementById('demandDetailsModal').classList.remove('hidden');
        } catch (err) {
            showToast('Failed to load demand details: ' + err.message, 'error');
        }
    };

    const requestAllocationForDemand = async (employeeId, demandId) => {
        if (!currentUser?.permissions?.canRequestAllocation) {
            showToast('Submitting allocation requests is restricted for your current role.', 'warning');
            return;
        }
        try {
            const demand = await API.getDemandById(demandId);
            const payload = {
                employeeId,
                demandId,
                startDate: demand.startDate,
                endDate: demand.endDate,
                allocationPercent: demand.allocationPercent
            };
            const alloc = await API.createAllocation(payload);
            showToast(`Allocation request #${alloc.id} submitted into PENDING state awaiting manager approval.`);
            window.location.hash = 'screen-allocation-approval';
        } catch (err) {
            showToast(err.message, 'error');
        }
    };

    const prepareAllocationForResource = async (employeeId, employeeName) => {
        const demandIdStr = prompt(`Enter Demand ID to allocate ${employeeName} (e.g. 1, 2, 3):`, '1');
        if (!demandIdStr) return;
        const demandId = parseInt(demandIdStr, 10);
        await requestAllocationForDemand(employeeId, demandId);
    };

    // Utilities
    const escapeHtml = (text) => {
        if (!text) return '';
        return String(text)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    };

    const debounce = (func, wait) => {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    };

    // Public API
    return {
        init: async () => {
            initNavigation();
            await loadGlobalData();
            initDemandIntake();
            initSkillsMatrix();
            initResourceSearch();
            initCandidateComparison();
            initAllocations();
            initDashboard();
            initAiAssistant();
            await initAuth();
        },
        evaluateDemand,
        openAdminViewDemand,
        openAddSkillForEmp,
        openEditSkillForEmp,
        requestAllocationForDemand,
        prepareAllocationForResource,
        approveAllocation,
        rejectAllocation,
        cancelAllocation,
        viewAuditHistory
    };
})();
