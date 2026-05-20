document.addEventListener('DOMContentLoaded', () => {
    const countEl = document.querySelector('#cart-count');
    const toast = document.querySelector('#toast');

    const money = (value) => `$${Number(value).toFixed(2)}`;

    const updateCartCount = (value) => {
        if (countEl) {
            countEl.textContent = String(value);
        }
    };

    const showMessage = (message) => {
        const messageEl = document.querySelector('[data-cart-message]');
        if (messageEl) {
            messageEl.textContent = message;
            messageEl.hidden = false;
        }
        if (toast) {
            toast.textContent = message;
            toast.hidden = false;
            window.setTimeout(() => {
                toast.hidden = true;
            }, 2200);
        }
    };

    const postCartAction = async (url, body = null) => {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body
        });

        if (!response.ok) {
            throw new Error('No se pudo actualizar el carrito.');
        }

        return response.json();
    };

    const bindProductDetailCart = () => {
        const form = document.querySelector('[data-product-detail-cart]');
        if (!form) {
            return;
        }

        const quantityInput = form.querySelector('[data-detail-quantity]');
        const currentQuantity = form.querySelector('[data-product-current-quantity]');

        const normalizeQuantity = () => {
            const value = Math.max(1, Number(quantityInput.value) || 1);
            quantityInput.value = String(value);
            return value;
        };

        form.querySelector('[data-detail-decrease]').addEventListener('click', () => {
            quantityInput.value = String(Math.max(1, normalizeQuantity() - 1));
        });

        form.querySelector('[data-detail-increase]').addEventListener('click', () => {
            quantityInput.value = String(normalizeQuantity() + 1);
        });

        quantityInput.addEventListener('input', normalizeQuantity);

        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            const params = new URLSearchParams();
            params.set('cantidad', String(normalizeQuantity()));

            const data = await postCartAction(form.action, params);
            updateCartCount(data.cantidadCarrito);
            if (currentQuantity && data.item) {
                currentQuantity.textContent = String(data.item.cantidad);
            }
            showMessage(data.mensaje);
        });
    };

    const toggleCartEmptyState = (empty) => {
        const emptyEl = document.querySelector('[data-cart-empty]');
        const layoutEl = document.querySelector('[data-cart-layout]');
        if (emptyEl) {
            emptyEl.hidden = !empty;
        }
        if (layoutEl) {
            layoutEl.hidden = empty;
        }
    };

    const updateCartPage = (data, row = null) => {
        updateCartCount(data.cantidadCarrito);

        const totalEl = document.querySelector('[data-cart-total]');
        if (totalEl) {
            totalEl.textContent = money(data.total);
        }

        if (row && data.item) {
            const quantityEl = row.querySelector('[data-cart-item-quantity]');
            const subtotalEl = row.querySelector('[data-cart-item-subtotal]');
            if (quantityEl) {
                quantityEl.textContent = String(data.item.cantidad);
            }
            if (subtotalEl) {
                subtotalEl.textContent = money(data.item.subtotal);
            }
        }

        toggleCartEmptyState(data.carritoVacio);
        showMessage(data.mensaje);
    };

    const bindCartPage = () => {
        document.querySelectorAll('[data-cart-action]').forEach((button) => {
            button.addEventListener('click', async () => {
                const row = button.closest('[data-cart-row]');
                const data = await postCartAction(button.dataset.cartAction);
                updateCartPage(data, row);
            });
        });

        document.querySelectorAll('[data-cart-remove-form]').forEach((form) => {
            form.addEventListener('submit', async (event) => {
                event.preventDefault();
                const row = form.closest('[data-cart-row]');
                const data = await postCartAction(form.action);
                if (row) {
                    row.remove();
                }
                updateCartPage(data);
            });
        });

        const finalizeForm = document.querySelector('[data-cart-finalize-form]');
        if (finalizeForm) {
            finalizeForm.addEventListener('submit', async (event) => {
                event.preventDefault();
                const data = await postCartAction(finalizeForm.action);
                document.querySelectorAll('[data-cart-row]').forEach((row) => row.remove());
                updateCartPage(data);
            });
        }
    };

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

    bindProductDetailCart();
    bindCartPage();

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
