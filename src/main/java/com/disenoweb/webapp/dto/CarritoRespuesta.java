package com.disenoweb.webapp.dto;

import java.math.BigDecimal;

public record CarritoRespuesta(
        int cantidadCarrito,
        BigDecimal total,
        boolean carritoVacio,
        CarritoItem item,
        String mensaje) {
}
