(function() {
    const THEMES = {
        neon: {
            '--bg': '#050414',
            '--panel': 'rgba(12, 17, 38, 0.9)',
            '--accent': '#ff3f8a',
            '--accent-strong': '#2563eb',
            '--accent-soft': '#22d3ee',
            '--text': '#f8fafc',
            '--muted': '#cdd5e0',
            '--danger': '#f973a1',
            '--success': '#34d399'
        },
        lime: {
            '--bg': '#f0fff4',
            '--panel': '#e6fffa',
            '--accent': '#10b981',
            '--accent-strong': '#059669',
            '--accent-soft': '#84cc16',
            '--text': '#032418',
            '--muted': '#4b5563',
            '--danger': '#ef4444',
            '--success': '#16a34a'
        },
        mono: {
            '--bg': '#000000',
            '--panel': '#0f0f0f',
            '--accent': '#ffffff',
            '--accent-strong': '#e5e7eb',
            '--accent-soft': '#9ca3af',
            '--text': '#ffffff',
            '--muted': '#d1d5db',
            '--danger': '#f87171',
            '--success': '#22c55e'
        }
    };

    function applyTheme(themeName, customColors = {}, persist = true) {
        const palette = { ...THEMES[themeName] || THEMES.neon, ...customColors };
        const root = document.documentElement;
        Object.entries(palette).forEach(([key, value]) => {
            root.style.setProperty(key, value);
        });
        if (persist) {
            localStorage.setItem('funfunctions_theme', themeName);
            localStorage.setItem('funfunctions_theme_custom', JSON.stringify(customColors));
        }

        if (document.body.classList.contains('inclusive-mode') && themeName !== 'mono') {
            applyTheme('mono', {}, false);
        }
    }

    function loadThemePreferences() {
        const theme = localStorage.getItem('funfunctions_theme') || 'neon';
        let custom = {};
        try {
            custom = JSON.parse(localStorage.getItem('funfunctions_theme_custom') || '{}') || {};
        } catch {
            custom = {};
        }
        applyTheme(theme, custom);
        return { theme, custom };
    }

    function setInclusiveMode(enabled) {
        document.body.classList.toggle('inclusive-mode', Boolean(enabled));
        localStorage.setItem('funfunctions_inclusive', enabled ? '1' : '0');

        if (enabled) {
            applyTheme('mono', {}, false);
        } else {
            let custom = {};
            try {
                custom = JSON.parse(localStorage.getItem('funfunctions_theme_custom') || '{}') || {};
            } catch {
                custom = {};
            }
            applyTheme(localStorage.getItem('funfunctions_theme') || 'neon', custom);
        }
    }

    function applyInclusivePreference() {
        const stored = localStorage.getItem('funfunctions_inclusive');
        setInclusiveMode(stored === '1');
    }

    function storeCredentials(username, password) {
        if (username) {
            localStorage.setItem('funfunctions_username', username);
        }
        if (password) {
            localStorage.setItem('funfunctions_password', password);
        }
    }

    function getCredentials() {
        return {
            username: localStorage.getItem('funfunctions_username') || '',
            password: localStorage.getItem('funfunctions_password') || ''
        };
    }

    function clearCredentials() {
        localStorage.removeItem('funfunctions_username');
        localStorage.removeItem('funfunctions_password');
    }

    function getThemePalette(themeName) {
        return { ...THEMES[themeName] || THEMES.neon };
    }

    function buildAuthHeader(credentialsOverride) {
        const source = credentialsOverride || getCredentials();
        const username = source?.username;
        const password = source?.password;
        if (!username || !password) return null;
        return 'Basic ' + toBase64(`${username}:${password}`);
    }

    function toBase64(value) {
        const encoder = new TextEncoder();
        const bytes = encoder.encode(value);
        let binary = '';
        bytes.forEach(byte => binary += String.fromCharCode(byte));
        return btoa(binary);
    }

    function withHttp(value) {
        if (!value) return value;
        if (/^https?:\/\//i.test(value)) return value;
        return `http://${value}`;
    }

    function normalizeBase(value) {
        if (!value) return '/';
        return value.replace(/\/$/, '');
    }

    function determineApiBase() {
        const stored = localStorage.getItem('funfunctions_api_base');
        if (stored) return normalizeBase(withHttp(stored));

        const safeProtocol = window.location.protocol && window.location.protocol.startsWith('http')
            ? window.location.protocol
            : 'http:';
        return `${safeProtocol}//localhost:3000`;
    }

    function buildUrl(endpoint) {
        const sanitizedEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
        return `${determineApiBase()}${sanitizedEndpoint}`;
    }

    function stripHtml(rawText = '') {
        const temp = document.createElement('div');
        temp.innerHTML = rawText;
        return (temp.textContent || temp.innerText || '').trim();
    }

    async function sendJson(endpoint, { method = 'GET', payload, withAuth = false, authOverride } = {}) {
        const url = buildUrl(endpoint);
        let response;
        const headers = { 'Content-Type': 'application/json' };
        if (withAuth) {
            const authHeader = buildAuthHeader(authOverride);
            if (authHeader) headers['Authorization'] = authHeader;
        }

        try {
            response = await fetch(url, {
                method,
                headers,
                body: payload ? JSON.stringify(payload) : undefined
            });
        } catch (networkError) {
            const cleanMessage = stripHtml(networkError.message || networkError.toString());
            const message = `Не удалось связаться с сервером: ${cleanMessage} Повторите попытку позже или обратитесь к администратору.`;
            throw new Error(message);
        }

        const text = await response.text();
        let data;
        try {
            data = text ? JSON.parse(text) : {};
        } catch {
            data = { message: stripHtml(text) || text };
        }

        if (!response.ok) {
            const message = stripHtml(data?.message)
                || stripHtml(data?.error)
                || response.statusText
                || `Ошибка ${response.status}`;
            throw new Error(message);
        }
        return data;
    }

    function applyStoredPreferences() {
        loadThemePreferences();
        applyInclusivePreference();
    }

    window.funFunctionsShared = {
        applyTheme,
        loadThemePreferences,
        setInclusiveMode,
        applyInclusivePreference,
        applyStoredPreferences,
        storeCredentials,
        getCredentials,
        clearCredentials,
        buildAuthHeader,
        getThemePalette,
        determineApiBase,
        buildUrl,
        sendJson,
        stripHtml
    };
})();