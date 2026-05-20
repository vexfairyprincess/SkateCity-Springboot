package com.disenoweb.webapp.web;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TiendaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void muestraInicioCatalogoDetalleYFormulario() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("destacados"));

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos"))
                .andExpect(model().attributeExists("productos", "categorias"));

        mockMvc.perform(get("/productos/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("producto-detalle"))
                .andExpect(model().attributeExists("producto"));

        mockMvc.perform(get("/pedido"))
                .andExpect(status().isOk())
                .andExpect(view().name("pedido-form"))
                .andExpect(model().attributeExists("pedidoForm", "productos"));
    }

    @Test
    void responde404CuandoElProductoNoExiste() throws Exception {
        mockMvc.perform(get("/productos/999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void filtraProductosPorNombreYCategoriaYMantieneFiltros() throws Exception {
        mockMvc.perform(get("/productos")
                        .param("nombre", "Tabla")
                        .param("categoriaId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos"))
                .andExpect(model().attributeExists("productos", "categorias"))
                .andExpect(model().attribute("nombre", "Tabla"))
                .andExpect(model().attribute("categoriaId", 1L))
                .andExpect(model().attribute("productos", not(empty())));
    }

    @Test
    void devuelveListaVaciaCuandoNoHayCoincidencias() throws Exception {
        mockMvc.perform(get("/productos")
                        .param("nombre", "Producto inexistente"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos"))
                .andExpect(model().attribute("productos", empty()));
    }
}
