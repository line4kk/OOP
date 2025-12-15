const shared = window.funFunctionsShared;
if (shared) {
    shared.applyStoredPreferences();
}

const factoryForm = document.getElementById('factoryForm');
const factoryFeedback = document.getElementById('factory-feedback');
const themeForm = document.getElementById('themeForm');
const themeFeedback = document.getElementById('theme-feedback');
const inclusiveToggle = document.getElementById('inclusiveToggle');
const logoutBtn = document.getElementById('logoutBtn');
const logoutFeedback = document.getElementById('logout-feedback');
const userBadge = document.querySelector('.user-badge');

function setFeedback(el, message, state = 'info') {
    if (!el) return;
    el.textContent = message || '';
    el.className = `feedback ${state !== 'info' ? state : ''}`.trim();
}

function renderUser() {
    const username = localStorage.getItem('funfunctions_username');
    if (username && userBadge) {
        userBadge.textContent = `Вы вошли как ${username}`;
    }
}

renderUser();

function setPreset(theme) {
    const currentPreset = themeForm.querySelector(`input[name="theme"][value="${theme}"]`);
    const fallbackPreset = themeForm.querySelector('input[name="theme"][value="neon"]');
    if (currentPreset) {
        currentPreset.checked = true;
    } else if (fallbackPreset) {
        fallbackPreset.checked = true;
    }
}

function setPaletteInputs(palette = {}) {
    const mapping = {
        bg: palette['--bg'],
        panel: palette['--panel'],
        accent: palette['--accent'],
        accentStrong: palette['--accent-strong'],
        accentSoft: palette['--accent-soft'],
        text: palette['--text'],
        muted: palette['--muted']
    };

    Object.entries(mapping).forEach(([name, value]) => {
        const input = themeForm.querySelector(`input[name="${name}"]`);
        if (input && value) {
            input.value = value;
        }
    });
}

function collectCustomPalette() {
    return {
        '--bg': themeForm.bg.value,
        '--panel': themeForm.panel.value,
        '--accent': themeForm.accent.value,
        '--accent-strong': themeForm.accentStrong.value,
        '--accent-soft': themeForm.accentSoft.value,
        '--text': themeForm.text.value,
        '--muted': themeForm.muted.value
    };
}

function loadThemeControls() {
    const { theme, custom } = shared ? shared.loadThemePreferences() : { theme: 'neon', custom: {} };
    setPreset(theme);
    const basePalette = shared?.getThemePalette ? shared.getThemePalette(theme) : {};
    const paletteToRender = Object.keys(custom || {}).length ? { ...basePalette, ...custom } : basePalette;
    setPaletteInputs(paletteToRender);
}

if (themeForm) {
    loadThemeControls();
    themeForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const preset = themeForm.theme.value || 'neon';
        shared?.applyTheme(preset, collectCustomPalette());
        setFeedback(themeFeedback, 'Тема сохранена и применена.', 'success');
    });

    themeForm.querySelectorAll('input[name="theme"]').forEach(input => {
        input.addEventListener('change', () => {
            themeFeedback.textContent = '';
            const preset = themeForm.theme.value || 'neon';
            const base = shared?.getThemePalette ? shared.getThemePalette(preset) : {};
            setPaletteInputs(base);
            shared?.applyTheme(preset, {});
        });
    });

    ['bg', 'panel', 'accent', 'accentStrong', 'accentSoft', 'text', 'muted'].forEach(name => {
        const input = themeForm.querySelector(`input[name="${name}"]`);
        input?.addEventListener('input', () => {
            const preset = themeForm.theme.value || 'neon';
            shared?.applyTheme(preset, collectCustomPalette());
        });
    });
}

function preloadCredentials(targetPasswordInput) {
    if (!shared || !targetPasswordInput) return;
    const { password } = shared.getCredentials();
    if (password) {
        targetPasswordInput.value = password;
    }
}

preloadCredentials(document.getElementById('factory-password'));

factoryForm?.addEventListener('submit', async (event) => {
    event.preventDefault();
    setFeedback(factoryFeedback, '');

    const factoryType = factoryForm.factory_type.value;
    const password = factoryForm.password.value;
    const { username } = shared?.getCredentials() || {};

    if (!username || !password) {
        setFeedback(factoryFeedback, 'Введите пароль или войдите заново.', 'error');
        return;
    }

    if (shared) {
        shared.storeCredentials(username, password);
    }

    try {
        await shared?.sendJson('/users/settings/factory_types', {
            method: 'PUT',
            payload: { factory_type: factoryType },
            withAuth: true,
            authOverride: { username, password }
        });
        setFeedback(factoryFeedback, 'Тип фабрики успешно обновлён.', 'success');
    } catch (error) {
        setFeedback(factoryFeedback, error.message, 'error');
    }
});

inclusiveToggle?.addEventListener('change', (event) => {
    shared?.setInclusiveMode(event.target.checked);
});

if (inclusiveToggle && shared) {
    inclusiveToggle.checked = localStorage.getItem('funfunctions_inclusive') === '1';
}

logoutBtn?.addEventListener('click', () => {
    setFeedback(logoutFeedback, 'Выходим из аккаунта...', 'info');
    shared?.clearCredentials();
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 400);
});