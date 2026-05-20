package com.disenoweb.webapp.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.disenoweb.webapp.dto.CarritoItem;
import com.disenoweb.webapp.dto.CarritoRespuesta;
import com.disenoweb.webapp.dto.PedidoForm;
import com.disenoweb.webapp.model.Categoria;
import com.disenoweb.webapp.model.Pedido;
import com.disenoweb.webapp.model.Producto;
import com.disenoweb.webapp.service.CarritoService;
import com.disenoweb.webapp.service.CatalogoService;
import com.disenoweb.webapp.service.PedidoService;
import com.disenoweb.webapp.service.ProductoNoEncontradoException;
import com.disenoweb.webapp.service.StockInsuficienteException;

import jakarta.validation.Valid;

@Controller
public class TiendaController {

    private final CatalogoService catalogoService;
    private final PedidoService pedidoService;
    private final CarritoService carritoService;

    public TiendaController(CatalogoService catalogoService, PedidoService pedidoService, CarritoService carritoService) {
        this.catalogoService = catalogoService;
        this.pedidoService = pedidoService;
        this.carritoService = carritoService;
    }

    @ModelAttribute("cantidadCarrito")
    public int cantidadCarrito() {
        return carritoService.contarProductos();
    }

    @GetMapping("/")
    public String inicio(@RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            Model model) {
        model.addAttribute("destacados", catalogoService.obtenerDestacados());
        cargarCatalogo(model, nombre, categoriaId);
        configurarCatalogo(model, "Búsqueda instantánea", "Explora todo el catálogo", "/");
        return esSolicitudAjax(requestedWith) ? "fragments/catalogo :: catalogo" : "index";
    }

    @GetMapping("/productos")
    public String productos(@RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoriaId,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            Model model) {
        cargarCatalogo(model, nombre, categoriaId);
        configurarCatalogo(model, "Catálogo", "Productos", "/productos");
        return esSolicitudAjax(requestedWith) ? "fragments/catalogo :: catalogo" : "productos";
    }

    @GetMapping("/productos/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Producto producto = catalogoService.buscarProductoPorId(id);
        model.addAttribute("producto", producto);
        model.addAttribute("cantidadEnCarrito", carritoService.contarProducto(id));
        return "producto-detalle";
    }

    @GetMapping("/carrito")
    public String carrito(Model model) {
        model.addAttribute("items", carritoService.obtenerItems());
        model.addAttribute("total", carritoService.calcularTotal());
        model.addAttribute("carritoVacio", carritoService.estaVacio());
        return "carrito";
    }

    @PostMapping("/carrito/agregar/{id}")
    public Object agregarAlCarrito(@PathVariable Long id,
            @RequestParam(defaultValue = "1") int cantidad,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes) {
        Producto producto = catalogoService.buscarProductoPorId(id);
        CarritoItem item = carritoService.agregarProducto(producto, cantidad);
        String mensaje = "Producto agregado al carrito.";
        if (esSolicitudAjax(requestedWith)) {
            return ResponseEntity.ok(crearRespuesta(item, mensaje));
        }
        redirectAttributes.addFlashAttribute("mensajeCarrito", "Producto agregado al carrito.");
        return redirigirAOrigen(referer);
    }

    @PostMapping("/carrito/aumentar/{id}")
    public ResponseEntity<CarritoRespuesta> aumentarCantidad(@PathVariable Long id) {
        return ResponseEntity.ok(crearRespuesta(carritoService.aumentarCantidad(id), "Cantidad actualizada."));
    }

    @PostMapping("/carrito/disminuir/{id}")
    public ResponseEntity<CarritoRespuesta> disminuirCantidad(@PathVariable Long id) {
        return ResponseEntity.ok(crearRespuesta(carritoService.disminuirCantidad(id), "Cantidad actualizada."));
    }

    @PostMapping("/carrito/eliminar/{id}")
    public Object eliminarDelCarrito(@PathVariable Long id,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            RedirectAttributes redirectAttributes) {
        carritoService.eliminarProducto(id);
        if (esSolicitudAjax(requestedWith)) {
            return ResponseEntity.ok(crearRespuesta(null, "Producto eliminado del carrito."));
        }
        redirectAttributes.addFlashAttribute("mensajeCarrito", "Producto eliminado del carrito.");
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/finalizar")
    public Object finalizarCompra(@RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            RedirectAttributes redirectAttributes) {
        carritoService.vaciar();
        if (esSolicitudAjax(requestedWith)) {
            return ResponseEntity.ok(crearRespuesta(null, "Pedido confirmado correctamente."));
        }
        redirectAttributes.addFlashAttribute("mensajeCarrito", "Pedido confirmado correctamente.");
        return "redirect:/carrito";
    }

    @GetMapping("/pedido")
    public String formularioPedido(@ModelAttribute("pedidoForm") PedidoForm pedidoForm, Model model) {
        model.addAttribute("productos", catalogoService.listarProductos());
        return "pedido-form";
    }

    @PostMapping("/pedido")
    public String crearPedido(@Valid @ModelAttribute("pedidoForm") PedidoForm pedidoForm,
            BindingResult bindingResult,
            Model model) {
        model.addAttribute("productos", catalogoService.listarProductos());
        if (bindingResult.hasErrors()) {
            return "pedido-form";
        }

        try {
            Pedido pedido = pedidoService.crearPedido(pedidoForm);
            model.addAttribute("pedido", pedido);
            return "pedido-exito";
        } catch (StockInsuficienteException ex) {
            bindingResult.rejectValue("cantidad", "stock.insuficiente", ex.getMessage());
            return "pedido-form";
        } catch (ProductoNoEncontradoException ex) {
            bindingResult.rejectValue("productoId", "producto.inexistente", ex.getMessage());
            return "pedido-form";
        }
    }

    @ExceptionHandler(ProductoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String manejarProductoNoEncontrado() {
        return "error/404";
    }

    private void cargarCatalogo(Model model, String nombre, Long categoriaId) {
        List<Categoria> categorias = catalogoService.listarCategorias();
        model.addAttribute("productos", catalogoService.buscarProductos(nombre, categoriaId));
        model.addAttribute("categorias", categorias);
        model.addAttribute("nombre", nombre == null ? "" : nombre.trim());
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("categoriaNombreSeleccionada", obtenerNombreCategoria(categorias, categoriaId));
    }

    private String obtenerNombreCategoria(List<Categoria> categorias, Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categorias.stream()
                .filter(categoria -> categoria.getId().equals(categoriaId))
                .map(Categoria::getNombre)
                .findFirst()
                .orElse(null);
    }

    private boolean esSolicitudAjax(String requestedWith) {
        return "XMLHttpRequest".equals(requestedWith);
    }

    private void configurarCatalogo(Model model, String eyebrow, String titulo, String clearUrl) {
        model.addAttribute("catalogEyebrow", eyebrow);
        model.addAttribute("catalogTitulo", titulo);
        model.addAttribute("catalogClearUrl", clearUrl);
    }

    private String redirigirAOrigen(String referer) {
        if (referer == null || referer.isBlank()) {
            return "redirect:/productos";
        }
        return "redirect:" + referer;
    }

    private CarritoRespuesta crearRespuesta(CarritoItem item, String mensaje) {
        return new CarritoRespuesta(
                carritoService.contarProductos(),
                carritoService.calcularTotal(),
                carritoService.estaVacio(),
                item,
                mensaje);
    }
}
