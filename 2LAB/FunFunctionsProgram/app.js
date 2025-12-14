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
    try {
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
        try {
            data = text ? JSON.parse(text) : {};
        } catch {
            data = { message: stripHtml(text) || text };
        }

        if (!response.ok) {
            const message = buildErrorMessage(response.status, response.statusText, data, url);
            throw new Error(message);
        }
        return data;
    } catch (networkError) {
        const cleanMessage = stripHtml(networkError.message || networkError.toString());
        const message = `Не удалось связаться с сервером: ${cleanMessage}.\nТекущий адрес backend: ${currentApiBase}. Убедитесь, что backend запущен и доступен.`;
        throw new Error(message);
    }
}

function buildErrorMessage(status, statusText, data, endpoint) {
    const reason = stripHtml(data?.message) || stripHtml(data?.error) || statusText || 'Неизвестная ошибка';
    let hint = 'Проверьте введённые данные и повторите попытку.';

    if (status === 403) {
        hint = 'Сервер вернул 403 Forbidden. Обычно это значит, что запрос пришёл без авторизации или с истекшей сессией.';
    } else if (status === 404) {
        hint = `Эндпоинт не найден. Убедитесь, что URL корректен: ${endpoint}.`;
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
    localStorage.setItem('funfunctions_api_base', currentApiBase);

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