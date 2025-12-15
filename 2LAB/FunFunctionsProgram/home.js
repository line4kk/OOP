const navButtons = document.querySelectorAll('.nav-link');

navButtons.forEach(button => {
    button.addEventListener('click', (event) => {
        const target = button.dataset.target || button.getAttribute('href');
        if (!target) return;
        const targetPage = buildNextPageUrl(target);
        if (event) event.preventDefault();
        window.location.href = targetPage;
    });
});

function buildNextPageUrl(target) {
    try {
        const current = new URL(window.location.href);
        return new URL(target, current).toString();
    } catch (e) {
        return target;
    }
}

const shared = window.funFunctionsShared;
if (shared) {
    shared.applyStoredPreferences();
}

const settingsBtn = document.querySelector('.settings-btn');
settingsBtn?.addEventListener('click', () => {
    window.location.href = 'settings.html';
});

const username = localStorage.getItem('funfunctions_username');
if (username) {
    const header = document.querySelector('.actions-header h2');
    if (header && !header.dataset.usernameAdded) {
        header.textContent = `FunFunctions | ${username}`;
        header.dataset.usernameAdded = 'true';
    }
}