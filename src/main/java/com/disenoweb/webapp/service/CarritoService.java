package com.disenoweb.webapp.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import com.disenoweb.webapp.dto.CarritoItem;
import com.disenoweb.webapp.model.Producto;

@Service
@SessionScope
public class CarritoService {

    private final Map<Long, CarritoItem> items = new LinkedHashMap<>();

    public CarritoItem agregarProducto(Producto producto) {
        return agregarProducto(producto, 1);
    }

    public CarritoItem agregarProducto(Producto producto, int cantidad) {
        int cantidadNormalizada = Math.max(1, cantidad);
        items.compute(producto.getId(), (id, item) -> {
            if (item == null) {
                CarritoItem nuevoItem = new CarritoItem(producto);
                if (cantidadNormalizada > 1) {
                    nuevoItem.incrementarCantidad(cantidadNormalizada - 1);
                }
                return nuevoItem;
            }
            item.incrementarCantidad(cantidadNormalizada);
            return item;
        });
        return items.get(producto.getId());
    }

    public void eliminarProducto(Long productoId) {
        items.remove(productoId);
    }

    public CarritoItem aumentarCantidad(Long productoId) {
        CarritoItem item = items.get(productoId);
        if (item != null) {
            item.incrementarCantidad();
        }
        return item;
    }

    public CarritoItem disminuirCantidad(Long productoId) {
        CarritoItem item = items.get(productoId);
        if (item != null) {
            item.disminuirCantidad();
        }
        return item;
    }

    public void vaciar() {
        items.clear();
    }

    public List<CarritoItem> obtenerItems() {
        return new ArrayList<>(items.values());
    }

    public BigDecimal calcularTotal() {
        return items.values().stream()
                .map(CarritoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int contarProductos() {
        return items.values().stream()
                .mapToInt(CarritoItem::getCantidad)
                .sum();
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public int contarProducto(Long productoId) {
        CarritoItem item = items.get(productoId);
        return item == null ? 0 : item.getCantidad();
    }
}
