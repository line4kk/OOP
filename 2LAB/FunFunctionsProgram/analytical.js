const shared = window.funFunctionsShared;
shared?.applyStoredPreferences();

const analyticStatus = document.getElementById('analyticStatus');
const analyticList = document.getElementById('analyticList');
const refreshButton = document.getElementById('refreshAnalytic');
const createButton = document.getElementById('createAnalytic');
const editButton = document.getElementById('editAnalytic');
const deleteButton = document.getElementById('deleteAnalytic');
const backButton = document.getElementById('backButton');
const settingsBtn = document.querySelector('.settings-btn');

const modal = document.getElementById('analyticModal');
const modalOverlay = modal?.querySelector('[data-close-modal]');
const closeModalButton = document.getElementById('closeAnalyticModal');
const choiceButtons = modal ? modal.querySelectorAll('.choice-btn') : [];
const baseMode = document.getElementById('baseMode');
const compositeMode = document.getElementById('compositeMode');
const baseNameSelect = document.getElementById('baseName');
const customBaseRow = document.getElementById('customBaseRow');
const customBaseInput = document.getElementById('customBaseName');
const compositeNameInput = document.getElementById('compositeName');
const componentPicker = document.getElementById('componentPicker');
const addComponentButton = document.getElementById('addComponent');
const compositionList = document.getElementById('compositionList');
const modalFeedback = document.getElementById('analyticModalFeedback');
const submitButton = document.getElementById('submitAnalytic');

const AVAILABLE_BASE_FUNCTIONS = [
    'IdentityFunction',
    'SqrFunction',
    'CubeFunction',
    'UnitFunction',
    'ZeroFunction',
    'SinFunction',
    'CosFunction',
    'ExpFunction',
    'LogFunction'
];

const state = {
    functions: [],
    selectedId: null,
    compositionDraft: [],
    originalComposition: [],
    editingFunction: null
};

init();

function init() {
    attachActions();
    populateBaseOptions();
    loadFunctions();
}

function attachActions() {
    refreshButton?.addEventListener('click', () => loadFunctions(true));
    createButton?.addEventListener('click', () => openModal('create', 'base'));
    editButton?.addEventListener('click', handleEditRequest);
    deleteButton?.addEventListener('click', handleDelete);
    backButton?.addEventListener('click', navigateBack);
    settingsBtn?.addEventListener('click', () => window.location.href = 'settings.html');

    modalOverlay?.addEventListener('click', closeModal);
    closeModalButton?.addEventListener('click', closeModal);
    window.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && !modal?.classList.contains('hidden')) {
            closeModal();
        }
    });

    choiceButtons.forEach(btn => {
        btn.addEventListener('click', () => switchMode(btn.dataset.mode));
    });

    baseNameSelect?.addEventListener('change', () => toggleCustomBase(baseNameSelect.value === 'custom'));
    addComponentButton?.addEventListener('click', addComponentToDraft);
    submitButton?.addEventListener('click', handleSubmit);
}

async function loadFunctions(force = false) {
    if (!analyticList) return;
    if (!force && analyticList.dataset.loading === 'true') return;

    setLoading(true);
    updateStatus('Загружаем аналитические функции...', 'muted');

    try {
        const response = await shared.sendJson('/functions/analytical_functions', { withAuth: true });
        const list = Array.isArray(response) ? response : (response?.functions || []);
        state.functions = list;
        renderFunctionList(list);
        updateStatus(`Загружено функций: ${list.length}.`, 'success');
        refreshComponentPicker();
    } catch (error) {
        console.error('Ошибка при загрузке аналитических функций:', error);
        renderFunctionList([]);
        updateStatus(error?.message || 'Не удалось загрузить функции.', 'error');
    } finally {
        setLoading(false);
    }
}

function renderFunctionList(list) {
    analyticList.innerHTML = '';
    state.selectedId = null;
    updateActionAvailability();

    if (!list.length) {
        const empty = document.createElement('div');
        empty.className = 'empty-placeholder';
        empty.textContent = 'Аналитические функции отсутствуют. Создайте новую или импортируйте существующую.';
        analyticList.appendChild(empty);
        return;
    }

    list.forEach(fn => {
        const item = document.createElement('button');
        item.type = 'button';
        item.className = 'function-item';
        item.innerHTML = buildFunctionMarkup(fn);
        item.addEventListener('click', () => selectFunction(fn.id));
        item.dataset.id = fn.id;
        analyticList.appendChild(item);
    });
}

function buildFunctionMarkup(fn) {
    const safeName = escapeHtml(fn?.name || 'Без названия');
    const badges = [];

    badges.push(`<span class="badge">Источник: ${fn?.source === 'composite' ? 'Композиция' : 'Базовая'}</span>`);
    if (fn?.id) {
        badges.push(`<span class="badge subtle">ID: ${fn.id}</span>`);
    }

    return `
        <div class="fn-name">${safeName}</div>
        <div class="fn-meta">${badges.join(' ')}</div>
    `;
}

function selectFunction(id) {
    state.selectedId = id;
    updateActionAvailability();
    document.querySelectorAll('.function-item').forEach(item => {
        item.classList.toggle('selected', Number(item.dataset.id) === Number(id));
    });
}

function updateActionAvailability() {
    const hasSelection = Boolean(state.selectedId);
    editButton && (editButton.disabled = !hasSelection);
    deleteButton && (deleteButton.disabled = !hasSelection);
}

function setLoading(stateLoading) {
    if (!analyticList) return;
    analyticList.dataset.loading = stateLoading ? 'true' : 'false';
    [refreshButton, createButton, editButton, deleteButton].forEach(btn => {
        if (btn) btn.disabled = stateLoading;
    });
}

function updateStatus(message, type = 'muted') {
    if (!analyticStatus) return;
    analyticStatus.textContent = message || '';
    analyticStatus.className = `feedback ${type}`.trim();
}

async function openModal(mode, targetMode = 'base') {
    if (!modal) return;
    state.editingFunction = mode === 'edit' ? findSelectedFunction() : null;
    state.compositionDraft = [];
    state.originalComposition = [];
    resetModalFeedback();
    baseNameSelect.value = AVAILABLE_BASE_FUNCTIONS[0];
    toggleCustomBase(false);
    compositeNameInput.value = '';
    compositionList.innerHTML = '';

    const title = mode === 'edit' ? 'Изменить функцию' : 'Добавить функцию';
    document.getElementById('analyticModalTitle').textContent = title;

    choiceButtons.forEach(btn => btn.disabled = mode === 'edit');

    const resolvedMode = mode === 'edit' ? deriveModeFromFunction(state.editingFunction) : targetMode;
    switchMode(resolvedMode);

    if (mode === 'edit' && state.editingFunction) {
        await preloadEditData(state.editingFunction);
    }

    modal.classList.remove('hidden');
}

function closeModal() {
    if (!modal) return;
    modal.classList.add('hidden');
    state.editingFunction = null;
    state.compositionDraft = [];
    state.originalComposition = [];
}

function switchMode(targetMode) {
    choiceButtons.forEach(btn => btn.classList.toggle('active', btn.dataset.mode === targetMode));
    baseMode?.classList.toggle('hidden', targetMode !== 'base');
    compositeMode?.classList.toggle('hidden', targetMode !== 'composite');
}

function toggleCustomBase(showCustom) {
    if (!customBaseRow || !customBaseInput) return;
    customBaseRow.classList.toggle('hidden', !showCustom);
    if (!showCustom) {
        customBaseInput.value = '';
    }
}

function deriveModeFromFunction(fn) {
    if (!fn) return 'base';
    return fn.source === 'composite' ? 'composite' : 'base';
}

async function preloadEditData(fn) {
    if (!fn) return;
    if (fn.source === 'composite') {
        compositeNameInput.value = fn.name || '';
        await loadComposition(fn.id);
        switchMode('composite');
    } else {
        baseNameSelect.value = AVAILABLE_BASE_FUNCTIONS.includes(fn.name) ? fn.name : 'custom';
        toggleCustomBase(baseNameSelect.value === 'custom');
        if (baseNameSelect.value === 'custom') {
            customBaseInput.value = fn.name || '';
        }
        switchMode('base');
    }
}

async function loadComposition(functionId) {
    try {
        const response = await shared.sendJson(`/functions/${functionId}/composition`, { withAuth: true });
        const composition = Array.isArray(response) ? response : [];
        const orderedIds = composition
            .sort((a, b) => (a.order ?? 0) - (b.order ?? 0))
            .map(item => item.function_id);
        state.compositionDraft = [...orderedIds];
        state.originalComposition = [...orderedIds];
        renderCompositionDraft();
        refreshComponentPicker();
    } catch (error) {
        console.error('Не удалось получить состав композиции', error);
        showModalFeedback(error?.message || 'Не удалось загрузить состав функции.', 'error');
    }
}

function populateBaseOptions() {
    if (!baseNameSelect) return;
    const current = baseNameSelect.value;
    baseNameSelect.innerHTML = '';
    AVAILABLE_BASE_FUNCTIONS.forEach(name => {
        const option = document.createElement('option');
        option.value = name;
        option.textContent = `${name} (${describeBase(name)})`;
        baseNameSelect.appendChild(option);
    });
    const customOption = document.createElement('option');
    customOption.value = 'custom';
    customOption.textContent = 'Другое имя из каталога functions...';
    baseNameSelect.appendChild(customOption);
    baseNameSelect.value = AVAILABLE_BASE_FUNCTIONS.includes(current) ? current : 'custom';
    toggleCustomBase(baseNameSelect.value === 'custom');
}

function describeBase(name) {
    switch (name) {
        case 'IdentityFunction':
            return 'f(x) = x';
        case 'SqrFunction':
            return 'f(x) = x²';
        case 'CubeFunction':
            return 'f(x) = x³';
        case 'UnitFunction':
            return 'f(x) = 1';
        case 'ZeroFunction':
            return 'f(x) = 0';
        case 'SinFunction':
            return 'f(x) = sin(x)';
        case 'CosFunction':
            return 'f(x) = cos(x)';
        case 'ExpFunction':
            return 'f(x) = eˣ';
        case 'LogFunction':
            return 'f(x) = ln|x|';
        default:
            return 'функция из каталога';
    }
}

function refreshComponentPicker() {
    if (!componentPicker) return;
    const previous = componentPicker.value;
    componentPicker.innerHTML = '';

    const available = state.functions.filter(fn => fn.type === 'analytical');
    if (!available.length) {
        const option = document.createElement('option');
        option.value = '';
        option.textContent = 'Нет доступных функций';
        option.disabled = true;
        componentPicker.appendChild(option);
        addComponentButton && (addComponentButton.disabled = true);
        return;
    }

    addComponentButton && (addComponentButton.disabled = false);

    available
        .filter(fn => fn.id !== state.editingFunction?.id)
        .forEach(fn => {
            const option = document.createElement('option');
            option.value = fn.id;
            option.textContent = fn.name || `Функция #${fn.id}`;
            componentPicker.appendChild(option);
        });

    const values = Array.from(componentPicker.options).map(opt => opt.value);
    if (values.includes(previous)) {
        componentPicker.value = previous;
    }
}

function addComponentToDraft() {
    if (!componentPicker) return;
    const chosen = Number(componentPicker.value);
    if (!chosen) return;
    state.compositionDraft.push(chosen);
    renderCompositionDraft();
    refreshComponentPicker();
}

function renderCompositionDraft() {
    if (!compositionList) return;
    compositionList.innerHTML = '';

    if (!state.compositionDraft.length) {
        const placeholder = document.createElement('div');
        placeholder.className = 'empty-placeholder';
        placeholder.textContent = 'Добавьте минимум две функции, чтобы собрать композицию.';
        compositionList.appendChild(placeholder);
        return;
    }
    const countMap = new Map();

    state.compositionDraft.forEach((id, index) => {
        const fn = state.functions.find(item => item.id === id);
        const name = fn?.name || `Функция #${id}`;
        const count = (countMap.get(id) || 0) + 1;
        countMap.set(id, count);

        const displayName = count > 1 ? `${name} (${count})` : name;

        const item = document.createElement('div');
        item.className = 'composition-item';
        item.innerHTML = `
            <div class="composition-name">${index + 1}. ${escapeHtml(displayName)}</div>
            <div class="composition-actions">
                <button type="button" class="icon-btn" data-action="up" data-index="${index}" aria-label="Выше">↑</button>
                <button type="button" class="icon-btn" data-action="down" data-index="${index}" aria-label="Ниже">↓</button>
                <button type="button" class="icon-btn" data-action="remove" data-index="${index}" aria-label="Удалить">×</button>
            </div>
        `;
        item.querySelectorAll('button').forEach(btn => {
            btn.addEventListener('click', handleCompositionAction);
        });
        compositionList.appendChild(item);
    });
}

function handleCompositionAction(event) {
    const index = Number(event.currentTarget.dataset.index);
    const action = event.currentTarget.dataset.action;
    if (!Number.isInteger(index)) return;

    if (action === 'remove') {
        state.compositionDraft.splice(index, 1);
    } else if (action === 'up' && index > 0) {
        [state.compositionDraft[index - 1], state.compositionDraft[index]] = [
            state.compositionDraft[index],
            state.compositionDraft[index - 1]
        ];
    } else if (action === 'down' && index < state.compositionDraft.length - 1) {
        [state.compositionDraft[index + 1], state.compositionDraft[index]] = [
            state.compositionDraft[index],
            state.compositionDraft[index + 1]
        ];
    }
    renderCompositionDraft();
    refreshComponentPicker();
}

function resetModalFeedback() {
    showModalFeedback('', 'muted');
}

function showModalFeedback(message, type = 'muted') {
    if (!modalFeedback) return;
    modalFeedback.textContent = message || '';
    modalFeedback.className = `feedback ${type}`.trim();
}

function escapeHtml(rawText = '') {
    const temp = document.createElement('div');
    temp.textContent = rawText;
    return temp.innerHTML;
}

function findSelectedFunction() {
    return state.functions.find(fn => fn.id === state.selectedId);
}

function navigateBack() {
    if (window.history && window.history.length > 1) {
        window.history.back();
        return;
    }
    window.location.href = 'home.html';
}

async function handleEditRequest() {
    const fn = findSelectedFunction();
    if (!fn) return;
    await openModal('edit', deriveModeFromFunction(fn));
}

async function handleDelete() {
    const fn = findSelectedFunction();
    if (!fn) return;

    const confirmed = confirm(`Удалить функцию "${fn.name}"?`);
    if (!confirmed) return;

    setLoading(true);
    updateStatus('Удаляем функцию...', 'muted');
    try {
        await shared.sendJson(`/functions/${fn.id}`, { method: 'DELETE', withAuth: true });
        updateStatus('Функция удалена.', 'success');
        await loadFunctions(true);
    } catch (error) {
        console.error('Ошибка удаления функции', error);
        updateStatus(error?.message || 'Не удалось удалить функцию.', 'error');
    } finally {
        setLoading(false);
    }
}

function resolveBaseName() {
    if (baseNameSelect?.value === 'custom') {
        return customBaseInput?.value.trim();
    }
    return baseNameSelect?.value;
}

function validateBase() {
    const name = resolveBaseName();
    if (!name) {
        return { valid: false, message: 'Укажите имя функции из каталога functions.' };
    }
    return { valid: true, name };
}

function validateComposite() {
    const name = compositeNameInput?.value.trim();
    if (!name) return { valid: false, message: 'Введите имя композиции.' };
    if (state.compositionDraft.length < 2) {
        return { valid: false, message: 'Добавьте минимум две функции для композиции.' };
    }
    return { valid: true, name, components: state.compositionDraft };
}

async function handleSubmit() {
    const mode = getActiveMode();
    resetModalFeedback();
    submitButton && (submitButton.disabled = true);

    try {
        if (mode === 'base') {
            const validation = validateBase();
            if (!validation.valid) {
                showModalFeedback(validation.message, 'error');
                return;
            }
            if (state.editingFunction) {
                await updateFunction(state.editingFunction.id, validation.name, 'base');
                showModalFeedback('Функция обновлена.', 'success');
            } else {
                await createBaseFunction(validation.name);
                showModalFeedback('Базовая функция добавлена.', 'success');
            }
        } else {
            const validation = validateComposite();
            if (!validation.valid) {
                showModalFeedback(validation.message, 'error');
                return;
            }
            if (state.editingFunction) {
                await updateCompositeFunction(state.editingFunction, validation);
            } else {
                await createCompositeFunction(validation.name, validation.components);
                showModalFeedback('Композиция создана.', 'success');
            }
        }

        await loadFunctions(true);
        closeModal();
    } catch (error) {
        console.error('Ошибка сохранения функции', error);
        showModalFeedback(error?.message || 'Не удалось сохранить функцию.', 'error');
    } finally {
        submitButton && (submitButton.disabled = false);
    }
}

function getActiveMode() {
    const active = Array.from(choiceButtons).find(btn => btn.classList.contains('active'));
    return active?.dataset.mode || 'base';
}

async function createBaseFunction(name) {
    return await shared.sendJson('/functions', {
        method: 'POST',
        payload: { name, type: 'analytical', source: 'base' },
        withAuth: true
    });
}

async function createCompositeFunction(name, components) {
    return await shared.sendJson('/functions/analytical_functions', {
        method: 'POST',
        payload: { name, function_ids_in_order: components },
        withAuth: true
    });
}

async function updateFunction(id, name, variant) {
    const source = variant === 'base'
        ? (state.editingFunction?.source && state.editingFunction.source !== 'composite'
            ? state.editingFunction.source
            : 'base')
        : 'composite';
    return await shared.sendJson(`/functions/${id}`, {
        method: 'PUT',
        payload: { name, type: 'analytical', source },
        withAuth: true
    });
}

async function updateCompositeFunction(existingFn, validation) {
    const sameOrder = arraysEqual(state.originalComposition, validation.components);

    if (sameOrder) {
        await updateFunction(existingFn.id, validation.name, 'composite');
        showModalFeedback('Имя композиции обновлено.', 'success');
        return;
    }

    try {
        await shared.sendJson(`/functions/${existingFn.id}`, { method: 'DELETE', withAuth: true });
    } catch (error) {
        throw new Error(error?.message || 'Не удалось заменить композицию. Убедитесь, что она не используется.');
    }

    await createCompositeFunction(validation.name, validation.components);
    showModalFeedback('Композиция пересобрана.', 'success');
}

function arraysEqual(a = [], b = []) {
    if (a.length !== b.length) return false;
    return a.every((val, idx) => val === b[idx]);
}