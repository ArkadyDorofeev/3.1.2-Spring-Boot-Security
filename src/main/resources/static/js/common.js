'use strict';
const baseUrl = document.querySelector('meta[name="app-base"]').content.replace(/\/$/, '');

async function api(path, options = {}) {
    const headers = new Headers(options.headers);
    headers.set('Accept', 'application/json');
    if (options.body) headers.set('Content-Type', 'application/json');
    if (options.method && options.method !== 'GET') {
        headers.set(document.querySelector('meta[name="_csrf_header"]').content,
            document.querySelector('meta[name="_csrf"]').content);
    }
    const response = await fetch(baseUrl + path, {...options, headers, credentials: 'same-origin'});
    if (response.redirected || response.status === 401) {
        throw new Error('Сессия истекла. Войдите в систему заново.');
    }
    const text = await response.text();
    let data;
    try { data = text ? JSON.parse(text) : null; } catch (_) {
        throw new Error('Сервер вернул неожиданный ответ. Попробуйте снова.');
    }
    if (!response.ok) throw new Error(data?.message || `Ошибка запроса: ${response.status}`);
    return data;
}

function showError(id, error) {
    const element = document.getElementById(id);
    element.textContent = error ? error.message : '';
    element.hidden = !error;
}

function roleNames(user) {
    return user.roles.map(role => role.name.replace(/^ROLE_/, '')).join(' ');
}

function userRow(user) {
    const row = document.createElement('tr');
    [user.id, user.name, user.lastName, user.age, user.email, roleNames(user)].forEach(value => {
        const cell = document.createElement('td');
        cell.textContent = value ?? '';
        row.append(cell);
    });
    return row;
}

function renderProfile(user) {
    document.getElementById('currentEmail').textContent = user.email;
    document.getElementById('currentRoles').textContent = roleNames(user);
    document.getElementById('adminLink').hidden = !user.roles.some(role => role.name === 'ROLE_ADMIN');
    document.getElementById('profileTableBody').replaceChildren(userRow(user));
}
