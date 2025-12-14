const tabs = document.querySelectorAll('.tab');
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const feedback = document.getElementById('feedback');
const resultBlock = document.getElementById('result');
const resultBody = document.getElementById('result-body');

const currentApiBase = determineApiBase();

tabs.forEach(tab => tab.addEventListener('click', () => switchTab(tab.dataset.tab)));

function switchTab(target) {
    tabs.forEach(tab => tab.classList.toggle('active', tab.dataset.tab === target));
    loginForm.classList.toggle('hidden', target !== 'login');
    registerForm.classList.toggle('hidden', target !== 'register');
    feedback.textContent = '';
    feedback.className = 'feedback';
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
    return `${currentApiBase}${sanitizedEndpoint}`;
}

function stripHtml(rawText = '') {
    const temp = document.createElement('div');
    temp.innerHTML = rawText;
    return (temp.textContent || temp.innerText || '').trim();
}

async function sendJson(endpoint, payload) {
    const url = buildUrl(endpoint);
    let response;

    try {
        response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
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
        const message = buildErrorMessage(response.status, response.statusText, data);
        throw new Error(message);
    }
    return data;
}

function buildErrorMessage(status, statusText, data) {
    return stripHtml(data?.message)
        || stripHtml(data?.error)
        || statusText
        || `Ошибка ${status}`;
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
            parts[parts.length - 1] = 'home.html';
            return `${current.origin}${parts.join('/')}${current.search}${current.hash}`;
        }
        return new URL('home.html', current).toString();
    } catch (e) {
        return 'home.html';
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