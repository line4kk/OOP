const tabs = document.querySelectorAll('.tab');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const feedback = document.getElementById('feedback');
const resultBlock = document.getElementById('result');
const resultBody = document.getElementById('result-body');
const quickBaseButtons = document.querySelectorAll('[data-base-target]');

const storedUser = localStorage.getItem('funfunctions_username');
const storedFactory = localStorage.getItem('funfunctions_factory');
const currentFromStorage = localStorage.getItem('funfunctions_api_base');
let currentApiBase = determineApiBase(currentFromStorage);
const detectionPromise = detectApiBase();

tabs.forEach(tab => tab.addEventListener('click', () => switchTab(tab.dataset.tab)));

quickBaseButtons.forEach(button => {
    button.addEventListener('click', () => {
        const target = button.getAttribute('data-base-target');
        updateApiBase(target);
    });
});

function switchTab(target) {
    tabs.forEach(tab => tab.classList.toggle('active', tab.dataset.tab === target));
    loginForm.classList.toggle('hidden', target !== 'login');
    registerForm.classList.toggle('hidden', target !== 'register');
    feedback.textContent = '';
    feedback.className = 'feedback';
}

function determineApiBase(saved) {
    if (saved && saved.includes('localhost:3000')) return normalizeBase(saved);

    // Фронтенд всегда ориентируется на backend на localhost:3000
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

function updateApiBase(nextBase) {
    const normalized = normalizeBase(withHttp((nextBase || '').trim()));
    if (!normalized) return;

    currentApiBase = normalized;
    localStorage.setItem('funfunctions_api_base', normalized);
    feedback.textContent = `Адрес backend переключён на ${normalized}`;
    feedback.className = 'feedback success';
}

function buildUrl(endpoint) {
    const sanitizedEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    return `${currentApiBase}${sanitizedEndpoint}`;
}

async function detectApiBase() {
    const candidates = buildCandidates();

    for (const candidate of candidates) {
        const reachable = await isReachable(candidate);
        if (reachable) {
            currentApiBase = normalizeBase(candidate);
            localStorage.setItem('funfunctions_api_base', currentApiBase);
            feedback.textContent = `Автоопределён адрес backend: ${currentApiBase}`;
            feedback.className = 'feedback success';
            return currentApiBase;
        }
    }

    feedback.textContent = `Не удалось автоматически определить backend. Используется текущий адрес: ${currentApiBase}`;
    feedback.className = 'feedback error';
    return currentApiBase;
}

function buildCandidates() {
    const safeProtocol = window.location.protocol && window.location.protocol.startsWith('http')
        ? window.location.protocol
        : 'http:';
    const bases = [
        currentFromStorage && currentFromStorage.includes('localhost:3000') ? currentFromStorage : null,
        `${safeProtocol}//localhost:3000`
    ].filter(Boolean);
    return Array.from(new Set(bases.map(base => normalizeBase(withHttp(base)))));
}

async function isReachable(base) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 2000);
    try {
        const response = await fetch(`${base}/`, {
            method: 'GET',
            mode: 'cors',
            credentials: 'include',
            signal: controller.signal
        });
        return true;
    } catch (error) {
        return false;
    } finally {
        clearTimeout(timeout);
    }
}

async function sendJson(endpoint, payload) {
    try {
        await detectionPromise.catch(() => {});
        const url = buildUrl(endpoint);
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        const text = await response.text();
        let data;
        try { data = text ? JSON.parse(text) : {}; } catch { data = { message: text }; }

        if (!response.ok) {
            const message = buildErrorMessage(response.status, response.statusText, data, url);
            throw new Error(message);
        }
        return data;
    } catch (networkError) {
        const message = `Не удалось связаться с сервером: ${networkError.message}.\nТекущий адрес backend: ${currentApiBase}. Проверьте, что сервер запущен на localhost:3000.`;
        throw new Error(message);
    }
}

function buildErrorMessage(status, statusText, data, endpoint) {
    const reason = data?.message || data?.error || statusText || 'Неизвестная ошибка';
    let hint = 'Проверьте введённые данные и повторите попытку.';

    if (status === 403) {
        hint = 'Сервер вернул 403 Forbidden. Обычно это значит, что запрос пришёл без авторизации. ' +
            'Если запускаете фронтенд на localhost:3000, убедитесь, что proxy направляет запросы на backend и что Spring Security разрешает OPTIONS.';
    } else if (status === 404) {
        hint = 'Эндпоинт не найден. Убедитесь, что URL корректен: ' + endpoint + '. ' +
            'Текущий адрес backend: ' + currentApiBase + '. Все запросы должны идти на localhost:3000.';
    } else if (status >= 500) {
        hint = 'Проблема на стороне сервера. Проверьте логи backend и повторите попытку позже.';
    }

    return `Ошибка ${status}: ${reason}.\n${hint}`;
}

function setLoading(state) {
    document.querySelectorAll('button').forEach(btn => btn.disabled = state);
}

function showResult(data) {
    const details = [
        data.username ? `Пользователь: ${data.username}` : null,
        data.factory_type ? `Тип фабрики: ${data.factory_type}` : null,
        data.fromStorage ? 'Данные взяты из localStorage.' : null
    ].filter(Boolean).join('\n');

    resultBody.textContent = details || 'Авторизация прошла успешно.';
    resultBlock.classList.remove('hidden');
    feedback.textContent = 'Авторизация прошла успешно';
    feedback.className = 'feedback success';
    loginForm.classList.add('hidden');
    registerForm.classList.add('hidden');
}

function handleSuccess(data) {
    if (data?.username) {
        localStorage.setItem('funfunctions_username', data.username);
    }
    if (data?.factory_type) {
        localStorage.setItem('funfunctions_factory', data.factory_type);
    }
    showResult(data);
    setTimeout(() => {
        const target = buildNextPageUrl();
        window.location.href = target;
    }, 500);
}

function buildNextPageUrl() {
    try {
        const current = new URL(window.location.href);
        const parts = current.pathname.split('/');
        if (parts[parts.length - 1].toLowerCase() === 'index.html') {
            parts[parts.length - 1] = 'next.html';
            return `${current.origin}${parts.join('/')}${current.search}${current.hash}`;
        }
        return new URL('next.html', current).toString();
    } catch (e) {
        return 'next.html';
    }
}

loginForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    feedback.textContent = '';
    feedback.className = 'feedback';
    setLoading(true);
    try {
        const payload = {
            username: loginForm.username.value.trim(),
            password: loginForm.password.value
        };
        const data = await sendJson('/users/auth', payload);
        handleSuccess(data);
    } catch (error) {
        feedback.textContent = error.message;
        feedback.className = 'feedback error';
    } finally {
        setLoading(false);
    }
});

registerForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    feedback.textContent = '';
    feedback.className = 'feedback';
    setLoading(true);
    try {
        const payload = {
            username: registerForm.username.value.trim(),
            password: registerForm.password.value,
            factory_type: registerForm.factory_type.value,
            role: 'user'
        };
        const data = await sendJson('/users/register', payload);
        handleSuccess(data);
    } catch (error) {
        feedback.textContent = error.message;
        feedback.className = 'feedback error';
    } finally {
        setLoading(false);
    }
});