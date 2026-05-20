package com.disenoweb.webapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.disenoweb.webapp.dto.PedidoForm;
import com.disenoweb.webapp.model.Pedido;
import com.disenoweb.webapp.repository.PedidoRepository;

@SpringBootTest
@ActiveProfiles("test")
class PedidoServiceTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Test
    void creaPedidoConUnDetalleYTotalCalculado() {
        PedidoForm form = new PedidoForm("Ana Rivera", "ana@example.com", 1L, 2, "Entregar por la tarde");

        Pedido pedido = pedidoService.crearPedido(form);

        assertThat(pedido.getId()).isNotNull();
        assertThat(pedido.getTotal()).isEqualByComparingTo(new BigDecimal("170.00"));
        assertThat(pedido.getDetalles()).hasSize(1);
        assertThat(pedido.getDetalles().get(0).getPrecioUnitario()).isEqualByComparingTo(new BigDecimal("85.00"));
        assertThat(pedidoRepository.findById(pedido.getId())).isPresent();
    }

    @Test
    void rechazaPedidoCuandoLaCantidadSuperaElStock() {
        PedidoForm form = new PedidoForm("Luis Pérez", "luis@example.com", 3L, 99, "Urgente");

        assertThatThrownBy(() -> pedidoService.crearPedido(form))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("stock");
    }
}
