package com.disenoweb.webapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.disenoweb.webapp.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findTop4ByOrderByIdAsc();

    List<Producto> findAllByOrderByIdAsc();

    List<Producto> findByNombreContainingIgnoreCaseOrderByIdAsc(String nombre);

    List<Producto> findByCategoriaIdOrderByIdAsc(Long categoriaId);

    List<Producto> findByNombreContainingIgnoreCaseAndCategoriaIdOrderByIdAsc(String nombre, Long categoriaId);
}
