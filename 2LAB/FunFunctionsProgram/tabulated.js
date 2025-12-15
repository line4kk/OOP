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
const manualActionsRow = document.getElementById('manualActionsRow');
const submitManualButton = document.getElementById('submitManual');
const manualFeedback = document.getElementById('manualFeedback');
const samplingFeedback = document.getElementById('samplingFeedback');
const xFromInput = document.getElementById('xFrom');
const xToInput = document.getElementById('xTo');
const pointsCountInput = document.getElementById('pointsCount');
const analyticSelect = document.getElementById('analyticSelect');
const submitSamplingButton = document.getElementById('submitSampling');
const functionModal = document.getElementById('functionModal');
const functionTitle = document.getElementById('functionModalTitle');
const functionMeta = document.getElementById('functionMeta');
const functionFeedback = document.getElementById('functionFeedback');
const functionXRow = document.getElementById('functionXRow');
const functionYRow = document.getElementById('functionYRow');
const functionActionsRow = document.getElementById('functionActionsRow');
const addFunctionPointButton = document.getElementById('addFunctionPoint');
const applyFunctionChangesButton = document.getElementById('applyFunctionChanges');
const deleteAllPointsButton = document.getElementById('deleteAllPoints');
const deleteFunctionButton = document.getElementById('deleteFunction');
const closeFunctionModalButton = document.getElementById('closeFunctionModal');
const functionModalOverlay = document.querySelector('[data-close-function-modal]');
const functionChartCanvas = document.getElementById('functionChart');
const functionChartStatus = document.getElementById('functionChartStatus');
const resetFunctionChartButton = document.getElementById('resetFunctionChart');
const refreshFunctionChartButton = document.getElementById('refreshFunctionChart');

const chartsModal = document.getElementById('chartsModal');
const chartsModalOverlay = document.querySelector('[data-close-charts-modal]');
const closeChartsModalButton = document.getElementById('closeChartsModal');
const chartFunctionSelect = document.getElementById('chartFunctionSelect');
const addFunctionToChartButton = document.getElementById('addFunctionToChart');
const globalChartCanvas = document.getElementById('globalChart');
const chartLegend = document.getElementById('chartLegend');
const chartsStatus = document.getElementById('chartsStatus');
const resetGlobalChartButton = document.getElementById('resetGlobalChart');
const clearChartCanvasButton = document.getElementById('clearChartCanvas');

const apiBase = shared?.determineApiBase?.() || determineApiBase();
const BASIC_AUTH_KEY = 'funfunctions_basic_credentials';
const FACTORY_TYPE_KEY = 'funfunctions_factory_type';
let analyticFunctionsLoaded = false;
let currentFunction = null;
let cachedFunctions = [];
let functionChart;
let globalChart;
let globalChartSeries = [];
let chartUpdateTimeout;
const chartPalette = [
    '#4e79a7', '#f28e2b', '#e15759', '#76b7b2', '#59a14f',
    '#edc949', '#af7aa1', '#ff9da7', '#9c755f', '#bab0ac'
];

class ChartCanvas {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas?.getContext('2d');
        this.series = [];
        this.view = null;
        this.padding = { left: 56, right: 16, top: 24, bottom: 44 };
        this.attachEvents();
    }

    attachEvents() {
        if (!this.canvas) return;
        this.canvas.addEventListener('wheel', (event) => {
            if (!this.view) return;
            event.preventDefault();
            const { offsetX, offsetY, deltaY } = event;
            const anchor = this.toData(offsetX, offsetY);
            const factor = deltaY < 0 ? 0.9 : 1.1;
            this.zoom(anchor, factor);
        });

        let dragging = false;
        let last = null;

        this.canvas.addEventListener('mousedown', (event) => {
            if (!this.view) return;
            dragging = true;
            last = { x: event.offsetX, y: event.offsetY };
        });

        window.addEventListener('mouseup', () => dragging = false);

        this.canvas.addEventListener('mousemove', (event) => {
            if (!dragging || !this.view) return;
            const dx = event.offsetX - last.x;
            const dy = event.offsetY - last.y;
            last = { x: event.offsetX, y: event.offsetY };
            this.pan(dx, dy);
        });

        this.canvas.addEventListener('dblclick', () => this.resetView());
        window.addEventListener('resize', () => this.resize());
    }

    setSeries(series = [], preserveView = false) {
        this.series = Array.isArray(series) ? series : [];
        if (preserveView && this.view) {
            this.draw();
        } else {
            this.resetView();
        }
    }

    resize() {
        if (!this.canvas) return;
        const parent = this.canvas.parentElement;
        if (parent) {
            this.canvas.width = parent.clientWidth;
            this.canvas.height = Math.max(320, parent.clientHeight);
        }
        this.draw();
    }

    resetView() {
        if (!this.series.length || !this.series.some(s => (s.points || []).length)) {
            this.view = null;
            this.draw();
            return;
        }

        const allPoints = this.series.flatMap(s => s.points || []);
        const xs = allPoints.map(p => p.x);
        const ys = allPoints.map(p => p.y);
        const minX = Math.min(...xs);
        const maxX = Math.max(...xs);
        const minY = Math.min(...ys);
        const maxY = Math.max(...ys);
        const paddingX = (maxX - minX || 1) * 0.1;
        const paddingY = (maxY - minY || 1) * 0.1;

        this.view = {
            xMin: minX - paddingX,
            xMax: maxX + paddingX,
            yMin: minY - paddingY,
            yMax: maxY + paddingY
        };
        this.resize();
    }

    zoom(anchor, factor) {
        if (!this.view) return;
        const { xMin, xMax, yMin, yMax } = this.view;
        const newXMin = anchor.x - (anchor.x - xMin) * factor;
        const newXMax = anchor.x + (xMax - anchor.x) * factor;
        const newYMin = anchor.y - (anchor.y - yMin) * factor;
        const newYMax = anchor.y + (yMax - anchor.y) * factor;
        this.view = { xMin: newXMin, xMax: newXMax, yMin: newYMin, yMax: newYMax };
        this.draw();
    }

    pan(dx, dy) {
        if (!this.view) return;
        const { width, height } = this.canvas;
        const factorX = (this.view.xMax - this.view.xMin) / Math.max(1, width - this.padding.left - this.padding.right);
        const factorY = (this.view.yMax - this.view.yMin) / Math.max(1, height - this.padding.top - this.padding.bottom);
        const deltaX = dx * factorX;
        const deltaY = dy * factorY;
        this.view = {
            xMin: this.view.xMin - deltaX,
            xMax: this.view.xMax - deltaX,
            yMin: this.view.yMin + deltaY,
            yMax: this.view.yMax + deltaY
        };
        this.draw();
    }

    toScreen(x, y) {
        const width = this.canvas.width;
        const height = this.canvas.height;
        const plotWidth = width - this.padding.left - this.padding.right;
        const plotHeight = height - this.padding.top - this.padding.bottom;
        const sx = this.padding.left + (x - this.view.xMin) / (this.view.xMax - this.view.xMin) * plotWidth;
        const sy = this.padding.top + (1 - (y - this.view.yMin) / (this.view.yMax - this.view.yMin)) * plotHeight;
        return { x: sx, y: sy };
    }

    toData(sx, sy) {
        const width = this.canvas.width;
        const height = this.canvas.height;
        const plotWidth = width - this.padding.left - this.padding.right;
        const plotHeight = height - this.padding.top - this.padding.bottom;
        const x = this.view.xMin + (sx - this.padding.left) / plotWidth * (this.view.xMax - this.view.xMin);
        const y = this.view.yMax - (sy - this.padding.top) / plotHeight * (this.view.yMax - this.view.yMin);
        return { x, y };
    }

    draw() {
        if (!this.canvas || !this.ctx) return;
        const ctx = this.ctx;
        ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        if (!this.view) {
            this.drawEmptyState();
            return;
        }

        this.drawGrid();
        this.drawAxes();
        this.series.forEach((series, index) => this.drawSeries(series, index));
    }

    drawEmptyState() {
        const ctx = this.ctx;
        ctx.save();
        ctx.fillStyle = 'rgba(255,255,255,0.6)';
        ctx.font = '16px Inter, sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('Нет данных для отображения графика', this.canvas.width / 2, this.canvas.height / 2);
        ctx.restore();
    }

    drawGrid() {
        const ctx = this.ctx;
        ctx.save();
        ctx.strokeStyle = 'rgba(255,255,255,0.08)';
        ctx.lineWidth = 1;
        const steps = 6;
        for (let i = 0; i <= steps; i++) {
            const ratio = i / steps;
            const x = this.padding.left + ratio * (this.canvas.width - this.padding.left - this.padding.right);
            const y = this.padding.top + ratio * (this.canvas.height - this.padding.top - this.padding.bottom);
            ctx.beginPath();
            ctx.moveTo(x, this.padding.top);
            ctx.lineTo(x, this.canvas.height - this.padding.bottom);
            ctx.stroke();
            ctx.beginPath();
            ctx.moveTo(this.padding.left, y);
            ctx.lineTo(this.canvas.width - this.padding.right, y);
            ctx.stroke();
        }
        ctx.restore();
    }

    drawAxes() {
        const ctx = this.ctx;
        ctx.save();
        ctx.strokeStyle = 'rgba(255,255,255,0.6)';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(this.padding.left, this.padding.top);
        ctx.lineTo(this.padding.left, this.canvas.height - this.padding.bottom);
        ctx.lineTo(this.canvas.width - this.padding.right, this.canvas.height - this.padding.bottom);
        ctx.stroke();

        ctx.fillStyle = 'rgba(255,255,255,0.7)';
        ctx.font = '12px Inter, sans-serif';
        ctx.textAlign = 'center';
        const steps = 4;
        for (let i = 0; i <= steps; i++) {
            const ratio = i / steps;
            const xValue = this.view.xMin + ratio * (this.view.xMax - this.view.xMin);
            const x = this.padding.left + ratio * (this.canvas.width - this.padding.left - this.padding.right);
            ctx.fillText(xValue.toFixed(2), x, this.canvas.height - this.padding.bottom + 18);
        }

        ctx.textAlign = 'right';
        for (let i = 0; i <= steps; i++) {
            const ratio = i / steps;
            const yValue = this.view.yMin + ratio * (this.view.yMax - this.view.yMin);
            const y = this.padding.top + (1 - ratio) * (this.canvas.height - this.padding.top - this.padding.bottom);
            ctx.fillText(yValue.toFixed(2), this.padding.left - 6, y + 4);
        }
        ctx.restore();
    }

    drawSeries(series, index) {
        const ctx = this.ctx;
        const points = (series.points || []).slice().sort((a, b) => a.x - b.x);
        if (points.length === 0) return;
        ctx.save();
        ctx.strokeStyle = series.color || chartPalette[index % chartPalette.length];
        ctx.fillStyle = ctx.strokeStyle;
        ctx.lineWidth = 2;
        ctx.beginPath();
        points.forEach((point, i) => {
            const { x, y } = this.toScreen(point.x, point.y);
            if (i === 0) {
                ctx.moveTo(x, y);
            } else {
                ctx.lineTo(x, y);
            }
        });
        ctx.stroke();

        points.forEach(point => {
            const { x, y } = this.toScreen(point.x, point.y);
            ctx.beginPath();
            ctx.arc(x, y, 4, 0, Math.PI * 2);
            ctx.fill();
        });
        ctx.restore();
    }
}

initCharts();
attachActions();
initCreationModal();
initFunctionModal();
loadFunctions();

function initCharts() {
    if (functionChartCanvas) {
        functionChart = new ChartCanvas(functionChartCanvas);
        updateFunctionChartStatus('График обновится после загрузки точек функции.', 'muted');
    }
    if (globalChartCanvas) {
        globalChart = new ChartCanvas(globalChartCanvas);
        setChartsStatus('Добавьте функции, чтобы сравнить их на одном полотне.', 'muted');
    }

    refreshFunctionChartButton?.addEventListener('click', refreshFunctionChartFromInputs);
    resetFunctionChartButton?.addEventListener('click', () => {
        functionChart?.resetView();
        updateFunctionChartStatus('Масштаб сброшен.', 'muted');
    });

    closeChartsModalButton?.addEventListener('click', closeChartsModal);
    chartsModalOverlay?.addEventListener('click', closeChartsModal);
    window.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && chartsModal && !chartsModal.classList.contains('hidden')) {
            closeChartsModal();
        }
    });

    addFunctionToChartButton?.addEventListener('click', addSelectedFunctionToChart);
    resetGlobalChartButton?.addEventListener('click', () => {
        globalChart?.resetView();
        setChartsStatus('Масштаб сброшен.', 'muted');
    });
    clearChartCanvasButton?.addEventListener('click', () => {
        globalChartSeries = [];
        renderGlobalChart();
        setChartsStatus('Полотно очищено.', 'muted');
    });
}

function attachActions() {
    if (refreshButton) {
        refreshButton.addEventListener('click', () => loadFunctions(true));
    }
    if (createButton) {
        createButton.addEventListener('click', () => openCreationModal('manual'));
    }
    if (chartsButton) {
        chartsButton.addEventListener('click', openChartsModal);
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

function initFunctionModal() {
    if (!functionModal) return;

    addFunctionPointButton?.addEventListener('click', () => {
        addFunctionPointColumn();
        validateFunctionForm();
    });

    applyFunctionChangesButton?.addEventListener('click', applyFunctionChanges);
    deleteAllPointsButton?.addEventListener('click', deleteAllFunctionPoints);
    deleteFunctionButton?.addEventListener('click', deleteFunctionHandler);
    closeFunctionModalButton?.addEventListener('click', closeFunctionModal);
    functionModalOverlay?.addEventListener('click', closeFunctionModal);

    window.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && !functionModal.classList.contains('hidden')) {
            closeFunctionModal();
        }
    });
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
        cachedFunctions = functions;
        populateChartSelect();
        renderFunctions(functions);
        updateStatus(`Загружено функций: ${functions.length}.`, 'success');
    } catch (error) {
        cachedFunctions = [];
        populateChartSelect();
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
        const item = document.createElement('div');
        item.className = 'function-item with-actions';
        item.innerHTML = `
            <div class="fn-main">${buildFunctionMarkup(fn)}</div>
            <div class="fn-actions">
                <button type="button" class="ghost action-btn" data-action="open">Открыть</button>
                <button type="button" class="ghost action-btn" data-action="chart">Посмотреть график</button>
            </div>
        `;

        const openButton = item.querySelector('[data-action="open"]');
        const chartButton = item.querySelector('[data-action="chart"]');

        openButton?.addEventListener('click', () => handleFunctionClick(fn));
        chartButton?.addEventListener('click', (event) => {
            event.stopPropagation();
            openChartForFunction(fn);
        });
        item.addEventListener('click', () => handleFunctionClick(fn));
        functionsList.appendChild(item);
    });
}

function handleFunctionClick(fn) {
    openFunctionModal(fn);
}

function openChartForFunction(fn) {
    openFunctionModal(fn);
    setTimeout(() => {
        functionChartCanvas?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 250);
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
    if (manualActionsRow) manualActionsRow.innerHTML = '';
    addPointColumn();
    addPointColumn();
    showFormFeedback(manualFeedback, '');
    validateManualForm();
}

function addPointColumn() {
    if (!xRow || !yRow || !manualActionsRow) return;
    const columnId = `manual-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`;
    const index = xRow.children.length + 1;
    xRow.appendChild(buildPointInput('x', index, columnId));
    yRow.appendChild(buildPointInput('y', index, columnId));

    const removeButton = document.createElement('button');
    removeButton.type = 'button';
    removeButton.dataset.columnId = columnId;
    removeButton.className = 'point-remove-btn';
    removeButton.textContent = '🗑️';
    removeButton.setAttribute('aria-label', 'Удалить точку');
    removeButton.addEventListener('click', () => {
        removeManualPoint(columnId);
        validateManualForm();
    });

    manualActionsRow.appendChild(removeButton);
}

function buildPointInput(prefix, index, columnId) {
    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = `${prefix}${index}`;
    input.inputMode = 'decimal';
    input.autocomplete = 'off';
    input.dataset.columnId = columnId;
    input.addEventListener('input', validateManualForm);
    return input;
}

function removeManualPoint(columnId) {
    if (!xRow || !yRow || !manualActionsRow) return;
    [xRow, yRow, manualActionsRow].forEach(row => {
        const target = Array.from(row.children).find(el => el.dataset?.columnId === columnId);
        if (target) {
            target.remove();
        }
    });
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

async function openFunctionModal(fn) {
    if (!functionModal) return;
    currentFunction = fn;

    functionTitle.textContent = fn?.name || 'Без названия';
    functionMeta.textContent = buildFunctionMeta(fn);
    resetFunctionModal();
    functionModal.classList.remove('hidden');

    if (!fn?.id) {
        showFormFeedback(functionFeedback, 'Не удалось определить идентификатор функции.', 'error');
        return;
    }

    setFunctionBusy(true);
    showFormFeedback(functionFeedback, 'Загружаем точки функции...', 'muted');
    try {
        const response = await getJson(`/functions/${fn.id}/points`);
        const points = Array.isArray(response) ? response : response?.points || [];
        renderFunctionPoints(points);
        refreshFunctionChartFromInputs();
        showFormFeedback(functionFeedback, `Найдено точек: ${points.length}.`, 'muted');
    } catch (error) {
        renderFunctionPoints([]);
        refreshFunctionChartFromInputs();
        showFormFeedback(functionFeedback, error?.message || 'Не удалось загрузить точки функции.', 'error');
    } finally {
        setFunctionBusy(false);
        validateFunctionForm();
    }
}

function closeFunctionModal() {
    if (!functionModal) return;
    functionModal.classList.add('hidden');
    currentFunction = null;
    resetFunctionModal();
}

function resetFunctionModal() {
    clearFunctionRows();
    showFormFeedback(functionFeedback, '');
    if (functionChart) {
        functionChart.setSeries([]);
        updateFunctionChartStatus('Откройте функцию, чтобы увидеть график.', 'muted');
    }
}

function renderFunctionPoints(points = []) {
    if (!functionXRow || !functionYRow || !functionActionsRow) return;
    clearFunctionRows();

    const list = Array.isArray(points) ? points : [];
    if (list.length === 0) {
        addFunctionPointColumn();
        addFunctionPointColumn();
        scheduleFunctionChartUpdate();
        return;
    }

    list.forEach(point => addFunctionPointColumn(point, true));
    scheduleFunctionChartUpdate();
}

function clearFunctionRows() {
    if (functionXRow) functionXRow.innerHTML = '';
    if (functionYRow) functionYRow.innerHTML = '';
    if (functionActionsRow) functionActionsRow.innerHTML = '';
}

function addFunctionPointColumn(point = {}, lockX = false) {
    if (!functionXRow || !functionYRow || !functionActionsRow) return;
    const columnId = `col-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`;
    const xInput = buildFunctionPointInput('x', columnId, point?.x, lockX || Boolean(point?.id));
    const yInput = buildFunctionPointInput('y', columnId, point?.y, false);

    const removeButton = document.createElement('button');
    removeButton.type = 'button';
    removeButton.dataset.columnId = columnId;
    removeButton.className = 'point-remove-btn';
    removeButton.textContent = '🗑️';
    removeButton.setAttribute('aria-label', 'Удалить точку');
    removeButton.addEventListener('click', () => {
        removeFunctionPoint(columnId);
        validateFunctionForm();
        scheduleFunctionChartUpdate();
    });

    functionXRow.appendChild(xInput);
    functionYRow.appendChild(yInput);
    functionActionsRow.appendChild(removeButton);
}

function removeFunctionPoint(columnId) {
    if (!functionXRow || !functionYRow || !functionActionsRow) return;
    [functionXRow, functionYRow, functionActionsRow].forEach(row => {
        const target = Array.from(row.children).find(el => el.dataset?.columnId === columnId);
        if (target) {
            target.remove();
        }
    });
}

function buildFunctionPointInput(prefix, columnId, value, lockX = false) {
    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = `${prefix}${(functionXRow?.children.length || 0) + 1}`;
    input.inputMode = 'decimal';
    input.autocomplete = 'off';
    input.dataset.columnId = columnId;
    if (typeof value !== 'undefined') {
        input.value = value;
    }
    if (prefix === 'x' && lockX) {
        input.readOnly = true;
    }
    input.addEventListener('input', () => {
        validateFunctionForm();
        scheduleFunctionChartUpdate();
    });
    return input;
}

function validateFunctionForm() {
    const { valid, message } = readFunctionPoints();
    showFormFeedback(functionFeedback, message, valid ? 'muted' : 'error');
    if (applyFunctionChangesButton) applyFunctionChangesButton.disabled = !valid;
    return valid;
}

function readFunctionPoints() {
    if (!functionXRow || !functionYRow) {
        return { valid: false, message: 'Добавьте точки.' };
    }

    const xInputs = Array.from(functionXRow.querySelectorAll('input'));
    const yInputs = Array.from(functionYRow.querySelectorAll('input'));

    if (xInputs.length === 0 || yInputs.length === 0) {
        return { valid: false, message: 'Добавьте минимум две точки.' };
    }

    const points = [];
    const usedX = new Set();

    for (let i = 0; i < Math.min(xInputs.length, yInputs.length); i++) {
        const xVal = parseNumber(xInputs[i].value);
        const yVal = parseNumber(yInputs[i].value);

        if (!xVal.valid || !yVal.valid) {
            return { valid: false, message: 'Точки должны содержать только числа. Для дробей используйте точку или запятую.' };
        }

        if (usedX.has(xVal.value)) {
            return { valid: false, message: 'Значения x должны быть уникальны.' };
        }

        usedX.add(xVal.value);
        points.push({ x: xVal.value, y: yVal.value });
    }

    if (points.length < 2) {
        return { valid: false, message: 'Нужно минимум две точки для сохранения функции.' };
    }

    return { valid: true, message: '', points };
}

function scheduleFunctionChartUpdate() {
    if (!functionChart) return;
    clearTimeout(chartUpdateTimeout);
    chartUpdateTimeout = setTimeout(refreshFunctionChartFromInputs, 140);
}

function refreshFunctionChartFromInputs() {
    if (!functionChart) return;
    const validation = readFunctionPoints();
    if (!validation.valid) {
        functionChart.setSeries([]);
        updateFunctionChartStatus(validation.message || 'Исправьте точки, чтобы увидеть график.', 'error');
        return;
    }

    const points = normalizePointsList(validation.points);
    functionChart.setSeries([{ name: currentFunction?.name || 'Функция', color: chartPalette[0], points }], true);
    updateFunctionChartStatus(`Отрисовано точек: ${points.length}. Масштабируйте колесом мыши.`, 'muted');
}

function updateFunctionChartStatus(message, type = 'muted') {
    if (!functionChartStatus) return;
    functionChartStatus.textContent = message || '';
    functionChartStatus.className = `feedback ${type}`.trim();
}

async function applyFunctionChanges() {
    const validation = readFunctionPoints();
    if (!validation.valid) {
        showFormFeedback(functionFeedback, validation.message, 'error');
        return;
    }

    if (!currentFunction?.id) {
        showFormFeedback(functionFeedback, 'Не удалось определить идентификатор функции.', 'error');
        return;
    }

    setFunctionBusy(true);
    try {
        await deleteJson(`/functions/${currentFunction.id}/points`);
        await postJson(`/functions/${currentFunction.id}/points`, validation.points);
        showFormFeedback(functionFeedback, 'Изменения сохранены.', 'success');
        updateStatus('Точки функции обновлены.', 'success');
        await loadFunctions(true);
        refreshFunctionChartFromInputs();
    } catch (error) {
        showFormFeedback(functionFeedback, error?.message || 'Не удалось применить изменения.', 'error');
    } finally {
        setFunctionBusy(false);
    }
}

async function deleteAllFunctionPoints() {
    if (!currentFunction?.id) {
        showFormFeedback(functionFeedback, 'Функция не найдена.', 'error');
        return;
    }

    const confirmed = window.confirm('Удалить все точки функции? Действие нельзя отменить.');
    if (!confirmed) return;

    setFunctionBusy(true);
    try {
        await deleteJson(`/functions/${currentFunction.id}/points`);
        renderFunctionPoints([]);
        refreshFunctionChartFromInputs();
        showFormFeedback(functionFeedback, 'Все точки удалены. Добавьте новые и нажмите «Применить изменения».', 'success');
        updateStatus('Все точки функции удалены.', 'muted');
    } catch (error) {
        showFormFeedback(functionFeedback, error?.message || 'Не удалось удалить точки функции.', 'error');
    } finally {
        setFunctionBusy(false);
        validateFunctionForm();
    }
}

async function deleteFunctionHandler() {
    if (!currentFunction?.id) {
        showFormFeedback(functionFeedback, 'Функция не найдена.', 'error');
        return;
    }

    const confirmed = window.confirm('Удалить функцию целиком? Точки и данные будут потеряны.');
    if (!confirmed) return;

    setFunctionBusy(true);
    try {
        await deleteJson(`/functions/${currentFunction.id}`);
        showFormFeedback(functionFeedback, 'Функция удалена.', 'success');
        updateStatus('Функция удалена.', 'muted');
        closeFunctionModal();
        await loadFunctions(true);
    } catch (error) {
        showFormFeedback(functionFeedback, error?.message || 'Не удалось удалить функцию.', 'error');
    } finally {
        setFunctionBusy(false);
    }
}

function setFunctionBusy(state) {
    [applyFunctionChangesButton, addFunctionPointButton, deleteAllPointsButton, deleteFunctionButton].forEach(btn => {
        if (btn) btn.disabled = state;
    });

    const inputs = functionModal?.querySelectorAll('input');
    if (inputs) {
        inputs.forEach(input => {
            if (state) {
                input.dataset.prevDisabled = input.disabled ? '1' : '';
                input.disabled = true;
            } else {
                if (!input.dataset.prevDisabled) {
                    input.disabled = false;
                }
                delete input.dataset.prevDisabled;
            }
        });
    }
}

function buildFunctionMeta(fn) {
    const type = formatFunctionType(fn?.type);
    const source = formatFunctionSource(fn?.source);
    const idText = fn?.id ? `ID: ${fn.id}` : '';
    const parts = [idText, `Тип: ${type}`].filter(Boolean);
    if (source) parts.push(source);
    return parts.join(' · ');
}

function openChartsModal() {
    if (!chartsModal) return;
    chartsModal.classList.remove('hidden');
    populateChartSelect();
    renderGlobalChart();
    setChartsStatus(globalChartSeries.length ? 'Используйте колесо мыши для зума и перетаскивайте полотно мышью.' : 'Выберите функцию и нажмите «Добавить».', 'muted');
    globalChart?.resize();
}

function closeChartsModal() {
    chartsModal?.classList.add('hidden');
}

function populateChartSelect() {
    if (!chartFunctionSelect) return;
    chartFunctionSelect.innerHTML = '';
    const placeholder = document.createElement('option');
    placeholder.value = '';
    placeholder.disabled = true;
    placeholder.selected = true;
    placeholder.textContent = cachedFunctions.length ? 'Выберите функцию' : 'Нет доступных функций';
    chartFunctionSelect.appendChild(placeholder);

    cachedFunctions.forEach(fn => {
        const option = document.createElement('option');
        option.value = fn.id;
        option.textContent = fn.name || `Функция #${fn.id}`;
        chartFunctionSelect.appendChild(option);
    });
}

async function addSelectedFunctionToChart() {
    if (!chartFunctionSelect) return;
    const id = Number(chartFunctionSelect.value);
    if (!Number.isFinite(id)) {
        setChartsStatus('Выберите функцию, чтобы добавить её на полотно.', 'error');
        return;
    }
    if (globalChartSeries.some(series => series.id === id)) {
        setChartsStatus('Эта функция уже добавлена на полотно.', 'muted');
        return;
    }

    setChartsStatus('Загружаем точки функции...', 'muted');
    try {
        const response = await getJson(`/functions/${id}/points`);
        const points = normalizePointsList(Array.isArray(response) ? response : response?.points || []);
        if (!points.length) {
            throw new Error('У функции нет точек для отображения.');
        }
        const fn = cachedFunctions.find(item => item.id === id) || { name: `Функция #${id}` };
        const color = chartPalette[globalChartSeries.length % chartPalette.length];
        globalChartSeries.push({ id, name: fn.name || `Функция #${id}`, color, points });
        renderGlobalChart();
        setChartsStatus(`Добавлена функция «${fn.name || id}».`, 'success');
    } catch (error) {
        setChartsStatus(error?.message || 'Не удалось добавить функцию на график.', 'error');
    }
}

function renderGlobalChart(preserveView = false) {
    if (globalChart) {
        globalChart.setSeries(buildGlobalSeriesPayload(), preserveView);
    }
    renderChartLegend();
}

function renderChartLegend() {
    if (!chartLegend) return;
    chartLegend.innerHTML = '';
    if (!globalChartSeries.length) {
        const empty = document.createElement('div');
        empty.className = 'legend-empty';
        empty.textContent = 'На полотне пока нет функций.';
        chartLegend.appendChild(empty);
        return;
    }

    globalChartSeries.forEach(series => {
        const row = document.createElement('div');
        row.className = 'legend-item';

        const colorInput = document.createElement('input');
        colorInput.type = 'color';
        colorInput.value = colorToHex(series.color);
        colorInput.className = 'legend-color';
        colorInput.addEventListener('input', (event) => {
            series.color = event.target.value;
            if (globalChart) {
                globalChart.setSeries(buildGlobalSeriesPayload(), true);
            }
        });

        const name = document.createElement('div');
        name.className = 'legend-name';
        name.textContent = series.name || `Функция #${series.id}`;

        const removeBtn = document.createElement('button');
        removeBtn.type = 'button';
        removeBtn.className = 'ghost action-btn';
        removeBtn.textContent = 'Убрать';
        removeBtn.addEventListener('click', () => {
            globalChartSeries = globalChartSeries.filter(item => item.id !== series.id);
            renderGlobalChart();
            setChartsStatus('Функция убрана с полотна.', 'muted');
        });

        row.append(colorInput, name, removeBtn);
        chartLegend.appendChild(row);
    });
}

function setChartsStatus(message, type = 'muted') {
    if (!chartsStatus) return;
    chartsStatus.textContent = message || '';
    chartsStatus.className = `feedback ${type}`.trim();
}

function buildGlobalSeriesPayload() {
    return globalChartSeries.map(item => ({
        name: item.name,
        color: item.color,
        points: item.points
    }));
}

function normalizePointsList(list = []) {
    return (Array.isArray(list) ? list : [])
        .map(point => ({ x: Number(point.x), y: Number(point.y) }))
        .filter(point => Number.isFinite(point.x) && Number.isFinite(point.y))
        .sort((a, b) => a.x - b.x);
}

function colorToHex(color) {
    if (!color) return '#4e79a7';
    if (color.startsWith('#')) return color;
    const ctx = document.createElement('canvas').getContext('2d');
    ctx.fillStyle = color;
    const computed = ctx.fillStyle;
    if (computed.startsWith('#')) return computed;
    const rgb = computed.match(/\d+/g) || [];
    if (rgb.length >= 3) {
        const [r, g, b] = rgb.map(Number).map(v => v.toString(16).padStart(2, '0'));
        return `#${r}${g}${b}`;
    }
    return '#4e79a7';
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


async function requestJson(endpoint, { method = 'GET', payload } = {}) {
    const url = buildUrl(endpoint);
    let response;

    const headers = {};
    if (payload !== undefined) {
        headers['Content-Type'] = 'application/json';
    }

    const authHeader = buildAuthHeader(endpoint);
    if (authHeader) headers['Authorization'] = authHeader;

    const options = { method, headers };
    if (payload !== undefined) {
        options.body = JSON.stringify(payload);
    }

    try {
        response = await fetch(url, options);
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

async function getJson(endpoint) {
    return await requestJson(endpoint, { method: 'GET' });
}

async function postJson(endpoint, payload) {
    return await requestJson(endpoint, { method: 'POST', payload });
}

async function deleteJson(endpoint) {
    return await requestJson(endpoint, { method: 'DELETE' });
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