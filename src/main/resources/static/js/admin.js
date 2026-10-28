'use strict';
let roles = [];
let currentUser;
let users = [];
let modalLoading = false;

function showTab(create) {
    document.getElementById('tablePanel').hidden = create;
    document.getElementById('newPanel').hidden = !create;
    document.getElementById('tableTab').classList.toggle('active', !create);
    document.getElementById('newTab').classList.toggle('active', create);
}

function showSection(admin) {
    document.getElementById('adminPanel').hidden = !admin;
    document.getElementById('profilePanel').hidden = admin;
    document.getElementById('adminLink').classList.toggle('active', admin);
    document.getElementById('userLink').classList.toggle('active', !admin);
}

function fillRoles(select, selected = []) {
    select.replaceChildren();
    roles.forEach(role => {
        const option = document.createElement('option');
        option.value = role.id;
        option.textContent = role.name.replace(/^ROLE_/, '');
        option.selected = selected.some(item => item.id === role.id);
        select.append(option);
    });
}

function renderUsers() {
    const body = document.getElementById('usersTableBody');
    body.replaceChildren();
    users.forEach(user => {
        const row = userRow(user);
        ['edit', 'delete'].forEach(mode => {
            const cell = document.createElement('td');
            const button = document.createElement('button');
            button.type = 'button';
            button.className = `btn btn-${mode === 'edit' ? 'info' : 'danger'} btn-sm`;
            button.textContent = mode === 'edit' ? 'Edit' : 'Delete';
            button.addEventListener('click', () => openModal(mode, user.id));
            cell.append(button);
            row.append(cell);
        });
        body.append(row);
    });
}

async function loadUsers() {
    users = await api('/api/admin/users');
    renderUsers();
    const freshProfile = users.find(user => user.id === currentUser.id);
    if (freshProfile) {
        currentUser = freshProfile;
        renderProfile(currentUser);
    }
}

async function openModal(mode, id) {
    if (modalLoading) return;
    modalLoading = true;
    showError('pageError', null);
    try {
        const user = await api(`/api/admin/users/${id}`);
        const form = document.getElementById(`${mode}Form`);
        form.reset();
        ['id', 'name', 'lastName', 'age', 'email'].forEach(key => {
            form.elements[key].value = user[key] ?? '';
        });
        fillRoles(form.elements.roleIds, user.roles);
        showError(`${mode}Error`, null);
        $(`#${mode}Modal`).modal('show');
    } catch (error) {
        showError('pageError', error);
    } finally {
        modalLoading = false;
    }
}

function readForm(form) {
    return {
        name: form.elements.name.value.trim(),
        lastName: form.elements.lastName.value.trim(),
        age: Number(form.elements.age.value),
        email: form.elements.email.value.trim(),
        password: form.elements.password.value,
        roleIds: Array.from(form.elements.roleIds.selectedOptions, option => Number(option.value))
    };
}

function bindForm(mode) {
    const form = document.getElementById(`${mode}Form`);
    form.addEventListener('submit', async event => {
        event.preventDefault();
        if (form.dataset.busy === 'true') return;
        if (!form.reportValidity()) return;
        form.dataset.busy = 'true';
        const submit = form.querySelector('[type="submit"]');
        submit.disabled = true;
        showError(`${mode}Error`, null);
        showError('pageError', null);
        try {
            const id = mode === 'create' ? '' : `/${form.elements.id.value}`;
            await api(`/api/admin/users${id}`, {
                method: {create: 'POST', edit: 'PUT', delete: 'DELETE'}[mode],
                ...(mode === 'delete' ? {} : {body: JSON.stringify(readForm(form))})
            });
            if (mode === 'create') {
                form.reset();
                fillRoles(form.elements.roleIds);
                showTab(false);
            } else {
                form.dataset.busy = 'false';
                $(`#${mode}Modal`).modal('hide');
            }
            // A successful mutation must not be resubmitted if refreshing the table fails.
            try { await loadUsers(); } catch (error) {
                showError('pageError', new Error(`Изменения сохранены, но таблица не обновилась: ${error.message}`));
            }
        } catch (error) {
            showError(`${mode}Error`, error);
        } finally {
            form.dataset.busy = 'false';
            submit.disabled = false;
        }
    });
}

['create', 'edit', 'delete'].forEach(bindForm);
['edit', 'delete'].forEach(mode => {
    $(`#${mode}Modal`).on('hide.bs.modal', event => {
        if (document.getElementById(`${mode}Form`).dataset.busy === 'true') {
            event.preventDefault();
        }
    });
});
document.getElementById('tableTab').addEventListener('click', () => showTab(false));
document.getElementById('newTab').addEventListener('click', () => showTab(true));
['admin', 'user'].forEach(section => {
    document.getElementById(`${section}Link`).addEventListener('click', event => {
        event.preventDefault();
        showSection(section === 'admin');
    });
});
showSection(true);
Promise.all([api('/api/user'), api('/api/admin/roles')]).then(async ([profile, allRoles]) => {
    currentUser = profile;
    roles = allRoles;
    renderProfile(profile);
    fillRoles(document.getElementById('createForm').elements.roleIds);
    await loadUsers();
}).catch(error => showError('pageError', error));
