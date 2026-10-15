/**
 * Talent & Resource Allocation Marketplace - API Client
 * Manages authenticated HTTP requests, server-side session sync, and role permissions.
 */

const API = (() => {
    const BASE_URL = '/api';

    let currentUsername = 'manager';
    let currentPassword = 'password123';
    let currentUser = null;

    const getAuthHeaders = () => {
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        if (currentUsername && currentPassword) {
            headers['Authorization'] = 'Basic ' + btoa(`${currentUsername}:${currentPassword}`);
        }

        return headers;
    };

    const request = async (endpoint, options = {}) => {
        const url = `${BASE_URL}${endpoint}`;
        const headers = { ...getAuthHeaders(), ...(options.headers || {}) };

        try {
            const response = await fetch(url, {
                ...options,
                headers,
                credentials: 'same-origin' // Send and receive session cookies (JSESSIONID)
            });
            
            // Handle non-JSON or empty responses
            if (response.status === 204) return null;
            
            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const message = data?.message || data?.error || `HTTP ${response.status}: ${response.statusText}`;
                const error = new Error(message);
                error.status = response.status;
                error.data = data;
                throw error;
            }

            return data;
        } catch (err) {
            console.error(`API Error [${options.method || 'GET'} ${endpoint}]:`, err);
            throw err;
        }
    };

    return {
        // Authentication & Session
        login: async (username, password) => {
            currentUsername = username;
            currentPassword = password;
            const res = await request('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ username, password })
            });
            currentUser = res;
            return res;
        },
        getCurrentUser: async () => {
            const res = await request('/auth/me');
            currentUser = res;
            return res;
        },
        logout: async () => {
            try {
                await request('/auth/logout', { method: 'POST' });
            } finally {
                currentUser = null;
                currentUsername = null;
                currentPassword = null;
            }
        },
        getCurrentUserProfile: () => currentUser,
        setCredentials: (username, password) => {
            currentUsername = username;
            currentPassword = password;
        },

        // Demands
        getDemands: (params = {}) => {
            const query = new URLSearchParams(params).toString();
            return request(`/demands${query ? '?' + query : ''}`);
        },
        getDemandById: (id) => request(`/demands/${id}`),
        createDemand: (demand) => request('/demands', {
            method: 'POST',
            body: JSON.stringify(demand)
        }),
        getDemandRecommendations: (demandId) => request(`/demands/${demandId}/recommendations`, {
            method: 'POST'
        }),

        // Employees & Skills
        getEmployees: (params = {}) => {
            const queryParams = new URLSearchParams();
            if (params.department && params.department.trim() && params.department.trim().toUpperCase() !== 'ALL') {
                queryParams.set('department', params.department.trim());
            }
            if (params.status && params.status.trim() && params.status.trim().toUpperCase() !== 'ALL') {
                queryParams.set('status', params.status.trim());
            }
            const query = queryParams.toString();
            return request(`/employees${query ? '?' + query : ''}`);
        },
        getEmployeeById: (id) => request(`/employees/${id}`),
        createEmployee: (employee) => request('/employees', {
            method: 'POST',
            body: JSON.stringify(employee)
        }),
        addEmployeeSkill: (employeeId, skillData) => request(`/employees/${employeeId}/skills`, {
            method: 'POST',
            body: JSON.stringify(skillData)
        }),
        getEmployeeSkills: (employeeId) => request(`/employees/${employeeId}/skills`),
        getSkills: (category = '') => request(`/skills${category ? '?category=' + category : ''}`),
        createSkill: (skill) => request('/skills', {
            method: 'POST',
            body: JSON.stringify(skill)
        }),

        // Availability Search
        getAvailableResources: (params = {}) => {
            const cleanParams = Object.fromEntries(Object.entries(params).filter(([_, v]) => v !== '' && v !== null && v !== undefined));
            const query = new URLSearchParams(cleanParams).toString();
            return request(`/resources/available${query ? '?' + query : ''}`);
        },

        // Allocations & Approval Workflow
        getAllocations: (params = {}) => {
            const cleanParams = Object.fromEntries(Object.entries(params).filter(([_, v]) => v !== '' && v !== null && v !== undefined));
            const query = new URLSearchParams(cleanParams).toString();
            return request(`/allocations${query ? '?' + query : ''}`);
        },
        createAllocation: (allocation) => request('/allocations', {
            method: 'POST',
            body: JSON.stringify(allocation)
        }),
        approveAllocation: (id) => request(`/allocations/${id}/approve`, {
            method: 'POST'
        }),
        rejectAllocation: (id, reason = '') => request(`/allocations/${id}/reject`, {
            method: 'POST',
            body: JSON.stringify({ reason })
        }),
        cancelAllocation: (id, reason = '') => request(`/allocations/${id}/cancel`, {
            method: 'PUT',
            body: JSON.stringify({ reason })
        }),
        getAllocationHistory: (id) => request(`/allocations/${id}/history`),

        // Reports & Analytics
        getUtilizationReport: (params = {}) => {
            const cleanParams = Object.fromEntries(Object.entries(params).filter(([_, v]) => v !== '' && v !== null && v !== undefined));
            const query = new URLSearchParams(cleanParams).toString();
            return request(`/reports/utilization${query ? '?' + query : ''}`);
        },
        getSkillGaps: () => request('/reports/skill-gaps'),

        // Agentic AI Staffing Advice
        getAiStaffingAdvice: (demandId, goal) => request('/ai/staffing-advice', {
            method: 'POST',
            body: JSON.stringify({ demandId, goal })
        })
    };
})();
