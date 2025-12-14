const functionsStatus = document.getElementById('functionsStatus');
const functionsList = document.getElementById('functionsList');
const refreshButton = document.getElementById('refreshList');
const createButton = document.getElementById('createFunction');
const chartsButton = document.getElementById('openCharts');
const backButton = document.getElementById('backButton');

const apiBase = determineApiBase();

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
    setLoading(true);
    updateStatus('Загружаем список функций...', 'muted');

    try {
        const data = await getJson('/functions');
        const tabulated = filterTabulated(data?.functions);
        renderFunctions(tabulated);
    } catch (error) {
        updateStatus(error.message || 'Не удалось получить список функций.', 'error');
        functionsList.innerHTML = '';
    } finally {
        setLoading(false);
    }
}

function filterTabulated(functions) {
    if (!Array.isArray(functions)) return [];
    return functions.filter(fn => (fn?.type || '').includes('tabulated'));
}

function renderFunctions(functions) {
    functionsList.innerHTML = '';

    if (!functions.length) {
        updateStatus('Табулированные функции отсутствуют. Создайте новую функцию, чтобы начать работу.', 'muted');
        return;
    }

    updateStatus(`Найдено табулированных функций: ${functions.length}`, 'success');

    functions.forEach(fn => {
        const item = document.createElement('button');
        item.type = 'button';
        item.className = 'function-item';
        item.setAttribute('role', 'listitem');
        item.innerHTML = buildFunctionMarkup(fn);
        item.addEventListener('click', () => handleFunctionClick(fn));
        functionsList.appendChild(item);
    });
}

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

    try {
        response = await fetch(url, { method: 'GET' });
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