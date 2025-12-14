const functionsStatus = document.getElementById('functionsStatus');
const functionsList = document.getElementById('functionsList');
const refreshButton = document.getElementById('refreshList');
const createButton = document.getElementById('createFunction');
const chartsButton = document.getElementById('openCharts');
const backButton = document.getElementById('backButton');

const apiBase = determineApiBase();
const BASIC_AUTH_KEY = 'funfunctions_basic_credentials';

attachActions();
loadFunctions();

function attachActions() {
    if (refreshButton) {
        refreshButton.addEventListener('click', () => loadFunctions(true));
    }
    if (createButton) {
        createButton.addEventListener('click', () => informComingSoon('Создание функций скоро будет доступно.'));
    }
    if (chartsButton) {
        chartsButton.addEventListener('click', () => informComingSoon('Переход к графикам будет добавлен позже.'));
    }
    if (backButton) {
        backButton.addEventListener('click', () => navigateBack());
    }
}

function informComingSoon(message) {
    updateStatus(message, 'muted');
}

async function loadFunctions(force = false) {
    if (!force && functionsList.dataset.loading === 'true') return;
@@ -75,122 +76,154 @@ function renderFunctions(functions) {
function buildFunctionMarkup(fn) {
    const safeName = escapeHtml(fn?.name) || 'Без названия';
    const safeType = escapeHtml(fn?.type || '');
    const safeSource = escapeHtml(fn?.source || 'base');
    const safeId = escapeHtml(String(fn?.id || '—'));

    return `
        <div class="fn-name">${safeName}</div>
        <div class="fn-meta">
            <span class="badge">ID: ${safeId}</span>
            <span class="badge">Тип: ${safeType}</span>
            <span class="badge">Источник: ${safeSource}</span>
        </div>
    `;
}

function handleFunctionClick(fn) {
    const safeName = fn?.name ? `"${fn.name}"` : 'без названия';
    updateStatus(`Откройте окно функции ${safeName} (ID: ${fn?.id ?? 'неизвестно'}) после подключения интерфейса.`, 'muted');
}

async function getJson(endpoint) {
    const url = buildUrl(endpoint);
    let response;

    const headers = {};
    const authHeader = buildAuthHeader(endpoint);
    if (authHeader) headers['Authorization'] = authHeader;

    try {
        response = await fetch(url, { method: 'GET', headers });
    } catch (networkError) {
        const cleanMessage = stripHtml(networkError.message || networkError.toString());
        const message = `Не удалось связаться с сервером: ${cleanMessage}. Повторите попытку позже.`;
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

function determineApiBase() {
    const stored = localStorage.getItem('funfunctions_api_base');
    if (stored) return normalizeBase(withHttp(stored));

    const safeProtocol = window.location.protocol && window.location.protocol.startsWith('http')
        ? window.location.protocol
        : 'http:';
    return `${safeProtocol}//localhost:3000`;
}

function normalizeBase(value) {
    if (!value) return '/';
    return value.replace(/\/$/, '');
}

function withHttp(value) {
    if (!value) return value;
    if (/^https?:\/\//i.test(value)) return value;
    return `http://${value}`;
}

function buildUrl(endpoint) {
    const sanitizedEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    return `${apiBase}${sanitizedEndpoint}`;
}

function buildAuthHeader(endpoint) {
    if (!shouldIncludeAuth(endpoint)) return null;
    const credentials = loadCredentials();
    if (!credentials) return null;

    const token = btoa(`${credentials.username}:${credentials.password}`);
    return `Basic ${token}`;
}

function shouldIncludeAuth(endpoint = '') {
    const normalized = endpoint.toLowerCase();
    return normalized !== '/users/auth' && normalized !== '/users/register';
}

function loadCredentials() {
    try {
        const stored = localStorage.getItem(BASIC_AUTH_KEY);
        if (!stored) return null;
        const parsed = JSON.parse(stored);
        if (parsed?.username && parsed?.password) {
            return { username: parsed.username, password: parsed.password };
        }
    } catch (e) {
        console.error('Не удалось прочитать данные авторизации', e);
    }
    return null;
}

function stripHtml(rawText = '') {
    const temp = document.createElement('div');
    temp.innerHTML = rawText;
    return (temp.textContent || temp.innerText || '').trim();
}

function escapeHtml(rawText = '') {
    const temp = document.createElement('div');
    temp.textContent = rawText;
    return temp.innerHTML;
}

function navigateBack() {
    if (window.history && window.history.length > 1) {
        window.history.back();
        return;
    }

    const target = buildPageUrl('home.html');
    window.location.href = target;
}

function buildPageUrl(target) {
    try {
        const current = new URL(window.location.href);
        return new URL(target, current).toString();
    } catch (e) {
        return target;
    }
}

function setLoading(state) {
    if (state) {
        functionsList.dataset.loading = 'true';
    } else {
        delete functionsList.dataset.loading;
    }
    document.querySelectorAll('button').forEach(btn => btn.disabled = state && btn.id === 'refreshList');
}

function updateStatus(message, type = 'muted') {
    if (!functionsStatus) return;
    functionsStatus.textContent = message || '';
    functionsStatus.className = `feedback ${type}`.trim();
}