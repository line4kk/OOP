const shared = window.funFunctionsShared;
if (shared) {
    shared.applyStoredPreferences();
}

const functionsStatus = document.getElementById('functionsStatus');
const functionsList = document.getElementById('functionsList');
const refreshButton = document.getElementById('refreshList');
const createButton = document.getElementById('createFunction');
const chartsButton = document.getElementById('openCharts');
const backButton = document.getElementById('backButton');

const creationModal = document.getElementById('creationModal');
const modalOverlay = creationModal?.querySelector('[data-close-modal]');
const closeModalButton = document.getElementById('closeCreationModal');
const modeButtons = creationModal ? creationModal.querySelectorAll('.choice-btn') : [];
const manualPanel = document.getElementById('manualMode');
const samplingPanel = document.getElementById('samplingMode');
const manualNameInput = document.getElementById('manualName');
const samplingNameInput = document.getElementById('samplingName');
const addPointButton = document.getElementById('addPoint');
const xRow = document.getElementById('xRow');
const yRow = document.getElementById('yRow');
const submitManualButton = document.getElementById('submitManual');
const manualFeedback = document.getElementById('manualFeedback');
const samplingFeedback = document.getElementById('samplingFeedback');
const xFromInput = document.getElementById('xFrom');
const xToInput = document.getElementById('xTo');
const pointsCountInput = document.getElementById('pointsCount');
const analyticSelect = document.getElementById('analyticSelect');
const submitSamplingButton = document.getElementById('submitSampling');

const apiBase = shared?.determineApiBase?.() || determineApiBase();
const BASIC_AUTH_KEY = 'funfunctions_basic_credentials';
const FACTORY_TYPE_KEY = 'funfunctions_factory_type';
let analyticFunctionsLoaded = false;

attachActions();
initCreationModal();
loadFunctions();

function attachActions() {
    if (refreshButton) {
        refreshButton.addEventListener('click', () => loadFunctions(true));
    }
    if (createButton) {
        createButton.addEventListener('click', () => openCreationModal('manual'));
    }
    if (chartsButton) {
        chartsButton.addEventListener('click', () => informComingSoon('Переход к графикам будет добавлен позже.'));
    }
    if (backButton) {
        backButton.addEventListener('click', () => navigateBack());
    }
    const settingsBtn = document.querySelector('.settings-btn');
    if (settingsBtn) {
        settingsBtn.addEventListener('click', () => {
            window.location.href = 'settings.html';
        });
    }
}

function initCreationModal() {
    if (!creationModal) return;

    addPointButton?.addEventListener('click', () => {
        addPointColumn();
        validateManualForm();
    });

    modeButtons.forEach(btn => {
        btn.addEventListener('click', () => switchMode(btn.dataset.mode));
    });

    submitManualButton?.addEventListener('click', handleManualSubmit);
    submitSamplingButton?.addEventListener('click', handleSamplingSubmit);
    closeModalButton?.addEventListener('click', closeCreationModal);
    modalOverlay?.addEventListener('click', closeCreationModal);
    window.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && !creationModal.classList.contains('hidden')) {
            closeCreationModal();
        }
    });

    [manualNameInput, samplingNameInput, xFromInput, xToInput, pointsCountInput, analyticSelect]
        .filter(Boolean)
        .forEach(input => input.addEventListener('input', () => {
            if (manualPanel && !manualPanel.classList.contains('hidden')) {
                validateManualForm();
            }
            if (samplingPanel && !samplingPanel.classList.contains('hidden')) {
                validateSamplingForm();
            }
        }));

    resetManualForm();
    switchMode('manual');
}

function informComingSoon(message) {
    updateStatus(message, 'muted');
}

async function loadFunctions(force = false) {
    if (!functionsList) return;
    if (!force && functionsList.dataset.loading === 'true') return;

    setLoading(true);
    updateStatus('Загружаем список функций...', 'muted');

    try {
        const response = await getJson('/functions');
        const functions = Array.isArray(response) ? response : response?.functions || [];
        renderFunctions(functions);
        updateStatus(`Загружено функций: ${functions.length}.`, 'success');
    } catch (error) {
        renderFunctions([]);
        const message = error?.message || 'Не удалось загрузить список функций.';
        updateStatus(message, 'error');
    } finally {
        setLoading(false);
    }
}

function renderFunctions(functions) {
    if (!functionsList) return;

    functionsList.innerHTML = '';

    if (!Array.isArray(functions) || functions.length === 0) {
        const emptyState = document.createElement('div');
        emptyState.className = 'function-item empty';
        emptyState.textContent = 'Список функций пуст.';
        functionsList.appendChild(emptyState);
        return;
    }

    functions.forEach(fn => {
        const item = document.createElement('button');
        item.type = 'button';
        item.className = 'function-item';
        item.innerHTML = buildFunctionMarkup(fn);
        item.addEventListener('click', () => handleFunctionClick(fn));
        functionsList.appendChild(item);
    });
}

function handleFunctionClick(fn) {
    const safeName = fn?.name ? `"${fn.name}"` : 'без названия';
    updateStatus(`Откройте окно функции ${safeName} (ID: ${fn?.id ?? 'неизвестно'}) после подключения интерфейса.`, 'muted');
}

function openCreationModal(mode = 'manual') {
    if (!creationModal) return;
    resetManualForm();
    resetSamplingForm();
    switchMode(mode);
    creationModal.classList.remove('hidden');
    if (mode === 'manual') {
        manualNameInput?.focus();
    } else {
        samplingNameInput?.focus();
    }
}

function closeCreationModal() {
    if (!creationModal) return;
    creationModal.classList.add('hidden');
    showFormFeedback(manualFeedback, '');
    showFormFeedback(samplingFeedback, '');
    setModalBusy(false);
}

function switchMode(targetMode) {
    if (!manualPanel || !samplingPanel) return;

    modeButtons.forEach(btn => {
        btn.classList.toggle('active', btn.dataset.mode === targetMode);
    });

    manualPanel.classList.toggle('hidden', targetMode !== 'manual');
    samplingPanel.classList.toggle('hidden', targetMode !== 'sampling');

    if (targetMode === 'manual') {
        validateManualForm();
    } else {
        ensureAnalyticFunctions();
        validateSamplingForm();
    }
}

function resetManualForm() {
    if (manualNameInput) manualNameInput.value = '';
    if (xRow) xRow.innerHTML = '';
    if (yRow) yRow.innerHTML = '';
    addPointColumn();
    addPointColumn();
    showFormFeedback(manualFeedback, '');
    validateManualForm();
}

function addPointColumn() {
    if (!xRow || !yRow) return;
    const index = xRow.children.length + 1;
    xRow.appendChild(buildPointInput('x', index));
    yRow.appendChild(buildPointInput('y', index));
}

function buildPointInput(prefix, index) {
    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = `${prefix}${index}`;
    input.inputMode = 'decimal';
    input.autocomplete = 'off';
    input.addEventListener('input', validateManualForm);
    return input;
}

function validateManualForm() {
    const { valid, message } = readManualPoints();
    showFormFeedback(manualFeedback, message, valid ? 'muted' : 'error');
    if (submitManualButton) submitManualButton.disabled = !valid;
    return valid;
}

function readManualPoints() {
    if (!xRow || !yRow) return { valid: false, message: 'Добавьте точки.' };

    const xInputs = Array.from(xRow.querySelectorAll('input'));
    const yInputs = Array.from(yRow.querySelectorAll('input'));

    if (xInputs.length === 0 || yInputs.length === 0) {
        return { valid: false, message: 'Добавьте минимум одну точку.' };
    }

    const points = [];
    const usedX = new Set();

    for (let i = 0; i < Math.min(xInputs.length, yInputs.length); i++) {
        const xVal = parseNumber(xInputs[i].value);
        const yVal = parseNumber(yInputs[i].value);

        if (!xVal.valid || !yVal.valid) {
            return { valid: false, message: 'Точки должны содержать только числа. Используйте запятую или точку для дробей.' };
        }

        const xValue = xVal.value;
        if (usedX.has(xValue)) {
            return { valid: false, message: 'Значения x должны быть уникальны: повторения недопустимы.' };
        }
        usedX.add(xValue);
        points.push({ x: xValue, y: yVal.value });
    }

    if (points.length < 2) {
        return { valid: false, message: 'Нужно минимум две точки для построения функции.' };
    }

    const name = manualNameInput?.value.trim();
    if (!name) {
        return { valid: false, message: 'Введите имя функции.' };
    }

    return { valid: true, message: '', points, name };
}

function validateSamplingForm() {
    const validation = readSamplingFields();
    showFormFeedback(samplingFeedback, validation.message, validation.valid ? 'muted' : 'error');
    if (submitSamplingButton) submitSamplingButton.disabled = !validation.valid;
    return validation.valid;
}

function readSamplingFields() {
    const name = samplingNameInput?.value.trim();
    if (!name) return { valid: false, message: 'Введите имя функции.' };

    const xFrom = parseNumber(xFromInput?.value);
    const xTo = parseNumber(xToInput?.value);

    if (!xFrom.valid || !xTo.valid) {
        return { valid: false, message: 'Координаты x начальное/конечное должны быть числом.' };
    }

    if (xFrom.value === xTo.value) {
        return { valid: false, message: 'Начало и конец отрезка должны отличаться.' };
    }

    const countRaw = pointsCountInput?.value;
    const count = Number.parseInt(countRaw ?? '', 10);
    if (!Number.isFinite(count) || count < 2) {
        return { valid: false, message: 'Количество точек должно быть целым числом не меньше 2.' };
    }

    const analyticId = analyticSelect?.value;
    if (!analyticId) {
        return { valid: false, message: 'Выберите аналитическую функцию.' };
    }

    return {
        valid: true,
        message: '',
        name,
        xFrom: xFrom.value,
        xTo: xTo.value,
        count,
        analyticId: Number(analyticId)
    };
}

async function handleManualSubmit() {
    const validation = readManualPoints();
    if (!validation.valid) {
        showFormFeedback(manualFeedback, validation.message, 'error');
        return;
    }

    setModalBusy(true);
    try {
        const fn = await createTabulatedFunction(validation.name);
        if (!fn?.id) throw new Error('Не удалось создать функцию.');
        await postJson(`/functions/${fn.id}/points`, validation.points);
        showFormFeedback(manualFeedback, 'Функция создана и точки сохранены.', 'success');
        updateStatus('Функция создана, обновляем список...', 'success');
        await loadFunctions(true);
        closeCreationModal();
    } catch (error) {
        showFormFeedback(manualFeedback, error.message || 'Не удалось создать функцию.', 'error');
    } finally {
        setModalBusy(false);
    }
}

async function handleSamplingSubmit() {
    const validation = readSamplingFields();
    if (!validation.valid) {
        showFormFeedback(samplingFeedback, validation.message, 'error');
        return;
    }

    setModalBusy(true);
    try {
        const fn = await createTabulatedFunction(validation.name);
        if (!fn?.id) throw new Error('Не удалось создать функцию.');
        const payload = {
            function_id: fn.id,
            analytical_function_id: validation.analyticId,
            x_from: validation.xFrom,
            x_to: validation.xTo,
            count: validation.count
        };
        await postJson(`/functions/${fn.id}/sampling`, payload);
        showFormFeedback(samplingFeedback, 'Функция создана с использованием дискретизации.', 'success');
        updateStatus('Функция создана, обновляем список...', 'success');
        await loadFunctions(true);
        closeCreationModal();
    } catch (error) {
        showFormFeedback(samplingFeedback, error.message || 'Не удалось создать функцию.', 'error');
    } finally {
        setModalBusy(false);
    }
}

async function ensureAnalyticFunctions() {
    if (analyticFunctionsLoaded || !analyticSelect) return;
    showFormFeedback(samplingFeedback, 'Загружаем аналитические функции...', 'muted');
    try {
        const response = await getJson('/functions/analytical_functions');
        const list = Array.isArray(response) ? response : response?.functions || [];
        populateAnalyticOptions(list);
        analyticFunctionsLoaded = true;
        showFormFeedback(samplingFeedback, 'Функции загружены.', 'success');
    } catch (error) {
        showFormFeedback(samplingFeedback, error.message || 'Не удалось загрузить функции.', 'error');
    }
}

function populateAnalyticOptions(functions = []) {
    if (!analyticSelect) return;
    analyticSelect.innerHTML = '';

    if (!functions.length) {
        const option = document.createElement('option');
        option.value = '';
        option.disabled = true;
        option.selected = true;
        option.textContent = 'Нет доступных аналитических функций';
        analyticSelect.appendChild(option);
        return;
    }

    const placeholder = document.createElement('option');
    placeholder.value = '';
    placeholder.disabled = true;
    placeholder.selected = true;
    placeholder.textContent = 'Выберите аналитическую функцию';
    analyticSelect.appendChild(placeholder);

    functions.forEach(fn => {
        const option = document.createElement('option');
        option.value = fn.id;
        option.textContent = fn.name || `Функция #${fn.id}`;
        analyticSelect.appendChild(option);
    });
}

async function createTabulatedFunction(name) {
    const payload = {
        name: name?.trim(),
        type: resolveTabulatedType(),
        source: 'base'
    };
    return await postJson('/functions', payload);
}

function resolveTabulatedType() {
    const stored = localStorage.getItem(FACTORY_TYPE_KEY) || 'linked_list';
    if (stored === 'array' || stored === 'array_tabulated') return 'array_tabulated';
    if (stored === 'linked_list' || stored === 'linked_list_tabulated') return 'linked_list_tabulated';
    if (stored.endsWith('_tabulated')) return stored;
    return `${stored}_tabulated`;
}

function parseNumber(value) {
    const normalized = (value ?? '').toString().trim().replace(',', '.');
    if (!normalized) return { valid: false, message: 'Введите число.' };
    const num = Number(normalized);
    if (!Number.isFinite(num)) return { valid: false, message: 'Введите корректное число.' };
    return { valid: true, value: num };
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

    const data = await parseResponse(response);
    if (!response.ok) {
        const message = data?.message || response.statusText || `Ошибка ${response.status}`;
        throw new Error(message);
    }
    return data;
}

async function postJson(endpoint, payload) {
    const url = buildUrl(endpoint);
    let response;

    const headers = {
        'Content-Type': 'application/json'
    };

    const authHeader = buildAuthHeader(endpoint);
    if (authHeader) headers['Authorization'] = authHeader;

    try {
        response = await fetch(url, {
            method: 'POST',
            headers,
            body: JSON.stringify(payload)
        });
    } catch (networkError) {
        const cleanMessage = stripHtml(networkError.message || networkError.toString());
        const message = `Не удалось связаться с сервером: ${cleanMessage}. Повторите попытку позже.`;
        throw new Error(message);
    }

    const data = await parseResponse(response);
    if (!response.ok) {
        const message = data?.message || response.statusText || `Ошибка ${response.status}`;
        throw new Error(message);
    }
    return data;
}

async function parseResponse(response) {
    const text = await response.text();
    let data;
    try {
        data = text ? JSON.parse(text) : {};
    } catch {
        data = { message: stripHtml(text) || text };
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
    if (!credentials) {
        return shared?.buildAuthHeader?.(null);
    }

    if (shared?.buildAuthHeader) {
        return shared.buildAuthHeader(credentials);
    }

    const token = safeBase64(`${credentials.username}:${credentials.password}`);
    return token ? `Basic ${token}` : null;
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

function safeBase64(value) {
    try {
        if (shared?.toBase64) {
            return shared.toBase64(value);
        }
        const encoder = new TextEncoder();
        const bytes = encoder.encode(value);
        let binary = '';
        bytes.forEach(byte => binary += String.fromCharCode(byte));
        return btoa(binary);
    } catch (error) {
        console.error('Не удалось создать токен авторизации', error);
        return null;
    }
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
    if (!functionsList) return;
    if (state) {
        functionsList.dataset.loading = 'true';
    } else {
        delete functionsList.dataset.loading;
    }
    document.querySelectorAll('button').forEach(btn => {
        if (btn.id === 'refreshList') {
            btn.disabled = state;
        }
    });
}

function setModalBusy(state) {
    [submitManualButton, submitSamplingButton, addPointButton].forEach(btn => {
        if (btn) btn.disabled = state;
    });
    if (modeButtons) {
        modeButtons.forEach(btn => btn.disabled = state);
    }
}

function updateStatus(message, type = 'muted') {
    if (!functionsStatus) return;
    functionsStatus.textContent = message || '';
    functionsStatus.className = `feedback ${type}`.trim();
}

function showFormFeedback(target, message, type = 'muted') {
    if (!target) return;
    target.textContent = message || '';
    target.className = `feedback ${type}`.trim();
}

function buildFunctionMarkup(fn) {
    const safeName = escapeHtml(fn?.name || 'Без названия');
    const type = formatFunctionType(fn?.type);
    const source = formatFunctionSource(fn?.source);

    const badges = [`<span class="badge">Тип: ${type}</span>`];
    if (source) {
        badges.push(`<span class="badge">${source}</span>`);
    }

    return `
        <div class="fn-name">${safeName}</div>
        <div class="fn-meta">
            ${badges.join(' ')}
        </div>
    `;
}

function formatFunctionType(type) {
    const humanReadable = {
        linked_list_tabulated: 'Связный список',
        array_tabulated: 'Динамический массив'
    };

    const resolved = humanReadable[type] || type || 'неизвестно';
    return escapeHtml(resolved);
}

function formatFunctionSource(source) {
    if (source === 'operation') {
        return 'В результате операции';
    }
    return '';
}

function resetSamplingForm() {
    if (samplingNameInput) samplingNameInput.value = '';
    if (xFromInput) xFromInput.value = '';
    if (xToInput) xToInput.value = '';
    if (pointsCountInput) pointsCountInput.value = '';
    if (analyticSelect && analyticSelect.options.length) {
        analyticSelect.selectedIndex = 0;
    }
    showFormFeedback(samplingFeedback, '');
}