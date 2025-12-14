const tabulatedButton = document.querySelector('[data-action="tabulated"]');

if (tabulatedButton) {
    tabulatedButton.addEventListener('click', (event) => {
        const target = tabulatedButton.getAttribute('href') || 'tabulated.html';
        const targetPage = buildNextPageUrl(target);
        if (event) event.preventDefault();
        window.location.href = targetPage;
    });
}

function buildNextPageUrl(target) {
    try {
        const current = new URL(window.location.href);
        return new URL(target, current).toString();
    } catch (e) {
        return target;
    }
}