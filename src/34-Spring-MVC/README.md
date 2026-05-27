<div align="center">
  <a href="#"><img src="../../assets/modules/banner-34-spring-mvc-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-concepto-v2.svg" width="100%" alt="// concepto"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Spring MVC** implementa el patrón **Front Controller**: toda request HTTP entra por un único punto — el `DispatcherServlet` — que la enruta al handler correcto.

**Ciclo de vida de una request en Spring MVC:**

```
HTTP Request
    ↓
DispatcherServlet          ← punto de entrada único (Front Controller)
    ↓
HandlerMapping             ← ¿qué @Controller/@RestController maneja esta URL?
    ↓
HandlerAdapter             ← invoca el método anotado
    ↓
@Controller method         ← lógica de negocio, devuelve datos o ModelAndView
    ↓
MessageConverter / View    ← serializa a JSON (Jackson) o resuelve la vista
    ↓
HTTP Response
```

Con `@RestController`, los métodos devuelven el objeto directamente — Spring aplica `HttpMessageConverter` (Jackson por defecto) para serializar a JSON sin pasar por un motor de plantillas.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**@Controller vs @RestController:**

| Anotación | Devuelve | Uso típico |
|---|---|---|
| `@Controller` | Vista (nombre de template) | Aplicaciones web con Thymeleaf/JSP |
| `@RestController` | Datos serializados (JSON/XML) | APIs REST — equivale a `@Controller` + `@ResponseBody` |

**Mapeo de rutas:**

```java
@RestController
@RequestMapping("/api/productos")   // prefijo común
public class ProductoController {

    @GetMapping("/{id}")            // GET /api/productos/42
    public Producto obtener(@PathVariable Long id) { ... }

    @GetMapping                     // GET /api/productos?categoria=libros
    public List<Producto> listar(@RequestParam String categoria) { ... }

    @PostMapping                    // POST /api/productos
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoDto dto) { ... }

    @PutMapping("/{id}")            // PUT /api/productos/42
    public Producto actualizar(@PathVariable Long id, @RequestBody ProductoDto dto) { ... }

    @DeleteMapping("/{id}")         // DELETE /api/productos/42
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { ... }
}
```

**ResponseEntity\<T\>** — control total sobre la respuesta HTTP:

```java
// Devuelve 201 Created con cabecera Location y el cuerpo
return ResponseEntity
    .created(URI.create("/api/productos/" + producto.getId()))
    .body(producto);

// Devuelve 404 con cuerpo vacío
return ResponseEntity.notFound().build();
```

**Manejo centralizado de errores con @ControllerAdvice:**

```java
@RestControllerAdvice               // aplica a todos los controllers
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse manejarNotFound(RecursoNoEncontradoException ex) {
        return new ErrorResponse(404, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse manejarValidacion(MethodArgumentNotValidException ex) {
        String errores = ex.getBindingResult().getFieldErrors()
            .stream().map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return new ErrorResponse(400, errores);
    }
}
```

**Validación del body con Bean Validation:**

```java
public class ProductoDto {
    @NotNull(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100)
    private String nombre;

    @Positive
    private BigDecimal precio;
}

// En el controller:
@PostMapping
public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoDto dto) { ... }
//                                     ^^^^^^ activa validación — lanza MethodArgumentNotValidException si falla
```

**CORS con @CrossOrigin:**

```java
@CrossOrigin(origins = "https://mi-frontend.com")  // en el controller o método
@RestController
public class ProductoController { ... }

// O configuración global en WebMvcConfigurer:
// registry.addMapping("/api/**").allowedOrigins("https://mi-frontend.com");
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

- **Separación de capas limpia**: Controller solo coordina — delega lógica al Service y persistencia al Repository.
- **Serialización automática JSON**: Jackson convierte POJOs a JSON y viceversa sin código manual.
- **Manejo centralizado de errores**: `@ControllerAdvice` evita try/catch en cada controller — un único lugar para todas las excepciones.
- **Validación declarativa**: `@Valid` + Bean Validation annotations en el DTO, sin lógica de validación en el controller.
- **ResponseEntity\<T\>** da control preciso sobre códigos HTTP, cabeceras y cuerpo sin comprometer la legibilidad.

Ver [ExpRestController.java](ExpRestController.java), [ExpRequestMapping.java](ExpRequestMapping.java), [ExpValidationMVC.java](ExpValidationMVC.java), [ExpExceptionHandler.java](ExpExceptionHandler.java), [ExpInterceptor.java](ExpInterceptor.java) y [ExpContentNegotiation.java](ExpContentNegotiation.java) para ejemplos ejecutables con `@RestController`, mapeo de rutas, validación, manejo de errores e interceptores.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
