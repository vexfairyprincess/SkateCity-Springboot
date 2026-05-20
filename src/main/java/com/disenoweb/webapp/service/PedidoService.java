package com.disenoweb.webapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.disenoweb.webapp.dto.PedidoForm;
import com.disenoweb.webapp.model.DetallePedido;
import com.disenoweb.webapp.model.Pedido;
import com.disenoweb.webapp.model.Producto;
import com.disenoweb.webapp.repository.PedidoRepository;
import com.disenoweb.webapp.repository.ProductoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public Pedido crearPedido(PedidoForm form) {
        Producto producto = productoRepository.findById(form.getProductoId())
                .orElseThrow(() -> new ProductoNoEncontradoException(form.getProductoId()));

        if (form.getCantidad() > producto.getStock()) {
            throw new StockInsuficienteException();
        }

        producto.reducirStock(form.getCantidad());

        Pedido pedido = new Pedido(form.getNombreCliente(), form.getCorreo(), form.getComentario());
        pedido.agregarDetalle(new DetallePedido(producto, form.getCantidad()));
        return pedidoRepository.save(pedido);
    }
}
