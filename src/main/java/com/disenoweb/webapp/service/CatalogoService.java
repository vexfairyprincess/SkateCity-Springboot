package com.disenoweb.webapp.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.disenoweb.webapp.model.Categoria;
import com.disenoweb.webapp.model.Producto;
import com.disenoweb.webapp.repository.CategoriaRepository;
import com.disenoweb.webapp.repository.ProductoRepository;

@Service
public class CatalogoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public CatalogoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Producto> obtenerDestacados() {
        return productoRepository.findTop4ByOrderByIdAsc();
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAllByOrderByIdAsc();
    }

    public List<Producto> buscarProductos(String nombre, Long categoriaId) {
        String nombreNormalizado = normalizarNombre(nombre);
        if (nombreNormalizado != null && categoriaId != null) {
            return productoRepository.findByNombreContainingIgnoreCaseAndCategoriaIdOrderByIdAsc(
                    nombreNormalizado, categoriaId);
        }
        if (nombreNormalizado != null) {
            return productoRepository.findByNombreContainingIgnoreCaseOrderByIdAsc(nombreNormalizado);
        }
        if (categoriaId != null) {
            return productoRepository.findByCategoriaIdOrderByIdAsc(categoriaId);
        }
        return productoRepository.findAllByOrderByIdAsc();
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll(Sort.by("id"));
    }

    public Producto buscarProductoPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
    }

    private String normalizarNombre(String nombre) {
        if (nombre == null) {
            return null;
        }
        String valor = nombre.trim();
        return valor.isEmpty() ? null : valor;
    }
}
