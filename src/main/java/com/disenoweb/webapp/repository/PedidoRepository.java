package com.disenoweb.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.disenoweb.webapp.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
