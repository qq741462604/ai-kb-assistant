import axios from 'axios';

export const BASE_URL = import.meta.env.VITE_BASE_URL || 'http://localhost:8080/field';

export const api = axios.create({
    baseURL: BASE_URL,
    timeout: 5000,
    withCredentials: true
});

export function getPendingList(page=1, size=10) {
    return api.get('/pending/list', { params: { pageNum, pageSize } });
}

export function approvePending(id) { return api.post('/pending/approve', null, { params: { id } }); }
export function rejectPending(id) { return api.post('/pending/reject', null, { params: { id } }); }
export function batchApprove(ids) { return api.post('/pending/batchApprove', ids); }
export function batchReject(ids) { return api.post('/pending/batchReject', ids); }
export function updatePending(pending) { return api.put('/pending/update', pending); }
export function approveWithEdits(pending) { return api.post('/pending/approveWithEdits', pending); }
