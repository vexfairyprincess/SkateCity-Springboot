package com.disenoweb.webapp.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CatalogoServiceTest {

    @Autowired
    private CatalogoService catalogoService;

    @Test
    void cargaProductosYCategoriasDesdeLasSemillas() {
        assertThat(catalogoService.listarProductos()).hasSize(21);
        assertThat(catalogoService.listarCategorias()).hasSize(5);
        assertThat(catalogoService.obtenerDestacados()).hasSize(4);
    }

    @Test
    void buscaProductosPorNombreYCategoria() {
        assertThat(catalogoService.buscarProductos("Santa Cruz", 1L))
                .isNotEmpty()
                .allMatch(producto -> producto.getNombre().contains("Santa Cruz"))
                .allMatch(producto -> producto.getCategoria().getId().equals(1L));
    }
}
