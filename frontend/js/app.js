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

    // Helper: Toast Notifications
    const showToast = (message, type = 'success') => {
        const container = document.getElementById('toastContainer');
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        container.appendChild(toast);
        setTimeout(() => toast.remove(), 4500);
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
        }
    };

    const populateSkillDropdowns = () => {
        const modalSkillSelect = document.getElementById('modalSkillSelect');
        const searchSkillSelect = document.getElementById('searchSkillSelect');

        if (modalSkillSelect) {
            modalSkillSelect.innerHTML = allSkills.map(s => 
                `<option value="${s.id}">${s.name} (${s.category})</option>`
            ).join('');
        }

        if (searchSkillSelect) {
            searchSkillSelect.innerHTML = '<option value="">Any Skill</option>' + 
                allSkills.map(s => `<option value="${s.id}">${s.name} (${s.category})</option>`).join('');
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

        document.getElementById('projectStartDate').value = nextMonth.toISOString().split('T')[0];
        document.getElementById('projectEndDate').value = nextQuarter.toISOString().split('T')[0];

        const addSkillRow = (skillName = '', mandatory = true, minProf = 3) => {
            const row = document.createElement('div');
            row.className = 'skill-spec-row';
            row.innerHTML = `
                <select class="form-control flex-2 skill-name-select" required>
                    ${allSkills.map(s => `<option value="${s.name}" ${s.name === skillName ? 'selected' : ''}>${s.name} (${s.category})</option>`).join('')}
                </select>
                <select class="form-control flex-1 skill-prof-select" required>
                    <option value="1" ${minProf === 1 ? 'selected' : ''}>Min L1</option>
                    <option value="2" ${minProf === 2 ? 'selected' : ''}>Min L2</option>
                    <option value="3" ${minProf === 3 ? 'selected' : ''}>Min L3</option>
                    <option value="4" ${minProf === 4 ? 'selected' : ''}>Min L4</option>
                    <option value="5" ${minProf === 5 ? 'selected' : ''}>Min L5</option>
                </select>
                <label style="display: flex; align-items: center; gap: 0.25rem; font-size: 0.8rem; cursor: pointer;">
                    <input type="checkbox" class="skill-mandatory-check" ${mandatory ? 'checked' : ''}>
                    Mandatory
                </label>
                <button type="button" class="btn btn-sm btn-danger btn-remove-skill">&times;</button>
            `;
            row.querySelector('.btn-remove-skill').addEventListener('click', () => row.remove());
            skillsList.appendChild(row);
        };

        btnAddSkill.addEventListener('click', () => addSkillRow());

        // Default with 2 sample skill rows
        skillsList.innerHTML = '';
        addSkillRow('Java', true, 4);
        addSkillRow('Spring Boot', true, 3);

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btnSubmit = document.getElementById('btnSubmitDemand');
            btnSubmit.disabled = true;
            btnSubmit.textContent = 'Submitting...';

            try {
                const requiredSkills = [];
                const rows = skillsList.querySelectorAll('.skill-spec-row');
                rows.forEach(row => {
                    const skillName = row.querySelector('.skill-name-select').value;
                    const minProficiency = parseInt(row.querySelector('.skill-prof-select').value, 10);
                    const mandatory = row.querySelector('.skill-mandatory-check').checked;
                    requiredSkills.push({ skillName, minProficiency, mandatory });
                });

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
                skillsList.innerHTML = '';
                addSkillRow('Java', true, 4);
                loadDemands();
            } catch (err) {
                showToast(err.message, 'error');
            } finally {
                btnSubmit.disabled = false;
                btnSubmit.textContent = 'Create Project Demand';
            }
        });

        btnRefresh?.addEventListener('click', loadDemands);
    };

    const loadDemands = async () => {
        const tbody = document.getElementById('demandsTableBody');
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4">Loading demands...</td></tr>';

        try {
            allDemands = await API.getDemands();
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
                            <strong>${escapeHtml(d.projectName)}</strong>
                            <div class="text-muted" style="font-size: 0.75rem;">${escapeHtml(d.role)}</div>
                        </td>
                        <td>${d.startDate} <span class="text-muted">to</span> ${d.endDate}</td>
                        <td>${d.location} (${d.allocationPercent}%)</td>
                        <td><span class="badge ${priorityBadge}">${d.priority}</span></td>
                        <td><span class="badge ${statusBadge}">${d.status}</span></td>
                        <td>
                            <button class="btn btn-sm btn-outline" onclick="App.evaluateDemand(${d.id})">Evaluate</button>
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

        btnOpenModal?.addEventListener('click', () => {
            populateModalEmployeeSelect();
            addSkillModal.classList.remove('hidden');
        });

        [btnCloseModal, btnCancelModal].forEach(btn => {
            btn?.addEventListener('click', () => addSkillModal.classList.add('hidden'));
        });

        addSkillForm?.addEventListener('submit', async (e) => {
            e.preventDefault();
            const empId = document.getElementById('modalEmployeeSelect').value;
            const skillId = document.getElementById('modalSkillSelect').value;
            const proficiencyLevel = parseInt(document.getElementById('modalProficiencySelect').value, 10);
            const lastValidatedAt = document.getElementById('modalValidationDate').value || null;

            try {
                await API.addEmployeeSkill(empId, { skillId: parseInt(skillId, 10), proficiencyLevel, lastValidatedAt });
                showToast('Skill assessment added/updated successfully!');
                addSkillModal.classList.add('hidden');
                loadSkillsMatrix();
            } catch (err) {
                showToast(err.message, 'error');
            }
        });
    };

    const populateModalEmployeeSelect = () => {
        const select = document.getElementById('modalEmployeeSelect');
        select.innerHTML = allEmployees.map(e => 
            `<option value="${e.id}">${e.name} (${e.employeeCode} - ${e.department})</option>`
        ).join('');
    };

    const loadSkillsMatrix = async () => {
        const tbody = document.getElementById('skillsMatrixTableBody');
        tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4">Loading skills matrix...</td></tr>';

        const dept = document.getElementById('matrixFilterDept')?.value || '';
        const status = document.getElementById('matrixFilterStatus')?.value || '';
        const search = (document.getElementById('matrixSearchInput')?.value || '').toLowerCase();

        try {
            allEmployees = await API.getEmployees({ department: dept, status });

            let filtered = allEmployees;
            if (search) {
                filtered = allEmployees.filter(e => 
                    e.name.toLowerCase().includes(search) ||
                    e.employeeCode.toLowerCase().includes(search) ||
                    e.skills.some(s => s.skillName.toLowerCase().includes(search))
                );
            }

            if (filtered.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No employees match filter criteria.</td></tr>';
                return;
            }

            tbody.innerHTML = filtered.map(emp => {
                const statusBadge = emp.employmentStatus === 'ACTIVE' ? 'badge-success' :
                                    emp.employmentStatus === 'ON_LEAVE' ? 'badge-warning' : 'badge-danger';

                const skillsBadges = emp.skills.map(s => {
                    const stars = s.proficiencyLevel ? '★'.repeat(s.proficiencyLevel) : 'Unvalidated';
                    const starClass = s.proficiencyLevel ? 'proficiency-stars' : 'text-muted';
                    const dateTooltip = s.lastValidatedAt ? `Validated: ${s.lastValidatedAt}` : 'Not validated';
                    return `
                        <span class="skill-pill" title="${dateTooltip}">
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
                            <button class="btn btn-sm btn-outline" onclick="App.openAddSkillForEmp(${emp.id})">+ Skill</button>
                        </td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger py-4">Error loading employees: ${escapeHtml(err.message)}</td></tr>`;
        }
    };

    const openAddSkillForEmp = (empId) => {
        populateModalEmployeeSelect();
        document.getElementById('modalEmployeeSelect').value = empId;
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

                        <div class="mt-3">
                            <button class="btn btn-sm btn-primary w-full" onclick="App.prepareAllocationForResource(${res.employeeId}, '${escapeHtml(res.name)}')">
                                Request Allocation
                            </button>
                        </div>
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

                        <div class="mt-4">
                            <button class="btn btn-sm ${c.eligible ? 'btn-primary' : 'btn-outline'} w-full"
                                    onclick="App.requestAllocationForDemand(${c.employeeId}, ${demandId})">
                                ${c.eligible ? 'Submit Allocation Request' : 'Propose with Waiver'}
                            </button>
                        </div>
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
                            <button class="btn btn-sm btn-success" onclick="App.approveAllocation(${a.id})">Approve (Lock & Commit)</button>
                            <button class="btn btn-sm btn-danger" onclick="App.rejectAllocation(${a.id})">Reject</button>
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
                                ${a.status === 'APPROVED' ? `<button class="btn btn-sm btn-outline" style="color: var(--danger);" onclick="App.cancelAllocation(${a.id})">Cancel Allocation</button>` : ''}
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
                                <button class="btn btn-primary" onclick="App.requestAllocationForDemand(${tc.employeeId}, ${response.demandId})">
                                    Submit for Manager Approval
                                </button>
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
        window.location.hash = 'screen-candidate-comparison';
        const select = document.getElementById('comparisonDemandSelect');
        if (select) select.value = demandId;
        renderCandidateComparison(demandId);
    };

    const requestAllocationForDemand = async (employeeId, demandId) => {
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
        init: () => {
            initNavigation();
            initDemandIntake();
            initSkillsMatrix();
            initResourceSearch();
            initCandidateComparison();
            initAllocations();
            initDashboard();
            initAiAssistant();
            loadGlobalData();
        },
        evaluateDemand,
        openAddSkillForEmp,
        requestAllocationForDemand,
        prepareAllocationForResource,
        approveAllocation,
        rejectAllocation,
        cancelAllocation,
        viewAuditHistory
    };
})();
