package com.disenoweb.webapp.service;

public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException() {
        super("No hay stock suficiente para completar el pedido");
    }
}
