document.addEventListener('DOMContentLoaded', () => {
    const countEl = document.querySelector('#cart-count');
    const toast = document.querySelector('#toast');
    let count = 0;

    const bindAddToCart = (scope = document) => {
        scope.querySelectorAll('.add-to-cart').forEach((button) => {
            if (button.dataset.bound === 'true') {
                return;
            }
            button.dataset.bound = 'true';
            button.addEventListener('click', () => {
                count += 1;
                if (countEl) {
                    countEl.textContent = String(count);
                }
                if (toast) {
                    toast.hidden = false;
                    window.setTimeout(() => {
                        toast.hidden = true;
                    }, 2200);
                }
            });
        });
    };

    bindAddToCart();

    const bindLiveCatalog = (root) => {
        const form = root.querySelector('[data-catalog-form]');
        const clearLink = root.querySelector('[data-clear-filters]');
        const clearTextButton = root.querySelector('[data-clear-text]');
        const clearUrl = root.dataset.clearUrl || window.location.pathname;
        if (!form) {
            return;
        }

        let timer;
        let requestId = 0;
        const nameInput = form.querySelector('[name="nombre"]');
        const categorySelect = form.querySelector('[name="categoriaId"]');

        const syncClearTextState = () => {
            if (!clearTextButton || !nameInput) {
                return;
            }
            clearTextButton.hidden = nameInput.value.trim() === '';
        };

        const updateCatalog = async () => {
            const formData = new FormData(form);
            const params = new URLSearchParams();
            formData.forEach((value, key) => {
                if (typeof value === 'string' && value.trim() !== '') {
                    params.set(key, value.trim());
                }
            });

            const url = params.toString() ? `${window.location.pathname}?${params.toString()}` : window.location.pathname;
            root.dataset.loading = 'true';
            const currentRequestId = ++requestId;
            const selectionStart = nameInput ? nameInput.selectionStart : null;
            const selectionEnd = nameInput ? nameInput.selectionEnd : null;

            try {
                const response = await fetch(url, {
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                });
                const html = await response.text();
                if (currentRequestId !== requestId) {
                    return;
                }

                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const nextResults = doc.querySelector('[data-catalog-results]');
                const currentResults = root.querySelector('[data-catalog-results]');
                if (nextResults && currentResults) {
                    currentResults.replaceWith(nextResults);
                    bindAddToCart(root);
                }

                if (nameInput) {
                    nameInput.focus({ preventScroll: true });
                    if (selectionStart !== null && selectionEnd !== null) {
                        nameInput.setSelectionRange(selectionStart, selectionEnd);
                    }
                }

                syncClearTextState();
                window.history.replaceState({}, '', url);
            } finally {
                if (currentRequestId === requestId) {
                    delete root.dataset.loading;
                }
            }
        };

        const scheduleUpdate = () => {
            window.clearTimeout(timer);
            timer = window.setTimeout(updateCatalog, 260);
        };

        form.addEventListener('submit', (event) => {
            event.preventDefault();
            updateCatalog();
        });

        form.querySelectorAll('input, select').forEach((field) => {
            const eventName = field.tagName === 'SELECT' ? 'change' : 'input';
            field.addEventListener(eventName, scheduleUpdate);
        });

        if (nameInput) {
            nameInput.addEventListener('input', syncClearTextState);
        }

        if (clearLink) {
            clearLink.addEventListener('click', (event) => {
                event.preventDefault();
                form.reset();
                if (nameInput) {
                    nameInput.value = '';
                }
                if (categorySelect) {
                    categorySelect.value = '';
                }
                syncClearTextState();
                window.history.replaceState({}, '', clearUrl);
                updateCatalog();
            });
        }

        if (clearTextButton && nameInput) {
            clearTextButton.addEventListener('click', () => {
                nameInput.value = '';
                syncClearTextState();
                nameInput.focus({ preventScroll: true });
                scheduleUpdate();
            });
        }

        syncClearTextState();
    };

    const liveCatalog = document.querySelector('[data-live-catalog]');
    if (liveCatalog) {
        bindLiveCatalog(liveCatalog);
    }

    const form = document.querySelector('#pedido-form');
    if (form) {
        form.addEventListener('submit', (event) => {
            const name = form.querySelector('[name="nombreCliente"]');
            const email = form.querySelector('[name="correo"]');
            const product = form.querySelector('[name="productoId"]');
            const quantity = form.querySelector('[name="cantidad"]');
            const validEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

            if (!name.value.trim() || !validEmail.test(email.value) || !product.value || Number(quantity.value) < 1) {
                event.preventDefault();
                window.alert('Completa correctamente nombre, correo, producto y cantidad.');
            }
        });
    }
});
