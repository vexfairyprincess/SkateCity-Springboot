package com.disenoweb.webapp.dto;

import java.math.BigDecimal;

import com.disenoweb.webapp.model.Producto;

public class CarritoItem {

    private final Long productoId;
    private final String nombre;
    private final BigDecimal precio;
    private final String imagen;
    private int cantidad;

    public CarritoItem(Producto producto) {
        this.productoId = producto.getId();
        this.nombre = producto.getNombre();
        this.precio = producto.getPrecio();
        this.imagen = producto.getImagen();
        this.cantidad = 1;
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public String getImagen() {
        return imagen;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getSubtotal() {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    public void incrementarCantidad() {
        cantidad++;
    }

    public void incrementarCantidad(int cantidad) {
        this.cantidad += cantidad;
    }

    public void disminuirCantidad() {
        if (cantidad > 1) {
            cantidad--;
        }
    }
}
