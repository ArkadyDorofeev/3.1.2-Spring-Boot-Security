'use strict';
document.getElementById('userLink').classList.add('active');
api('/api/user').then(renderProfile).catch(error => showError('pageError', error));
