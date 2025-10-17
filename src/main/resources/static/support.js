// support.js
const BASE_URL = 'http://localhost:8080'; // 后面可以换成 nginx 代理模式
window.BASE_URL = "http://localhost:8080"; //
function apiGet(path) {
    return fetch(`${BASE_URL}${path}`).then(r => r.json());
}
function apiPost(path, data) {
    return fetch(`${BASE_URL}${path}`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(data)
    }).then(r => r.json());
}

async function apiGet(url) {
    const res = await fetch(BASE_URL + url, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    });
    return res.json();
}

async function apiPost(url, data) {
    const res = await fetch(BASE_URL + url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
    return res.json();
}

async function apiPut(url, data) {
    const res = await fetch(BASE_URL + url, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
    return res.json();
}

async function apiDelete(url) {
    const res = await fetch(BASE_URL + url, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    });
    return res.json();
}

function approvePending(id) {
    fetch(`${BASE_URL}/pending/approve/${id}`, {
        method: "PUT"
    }).then(res => res.json())
        .then(data => {
            alert("审核通过成功");
            loadPendingList(); // 刷新表格
        });
}

function rejectPending(id) {
    fetch(`${BASE_URL}/pending/reject/${id}`, {
        method: "PUT"
    }).then(res => res.json())
        .then(data => {
            alert("已拒绝");
            loadPendingList();
        });
}

