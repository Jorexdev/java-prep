<div align="center">
  <a href="#"><img src="../../assets/modules/banner-34-spring-mvc-v1.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="24" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='24'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-entrevista-v2.svg" width="100%" alt="// entrevista"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**¿Qué es el DispatcherServlet y qué rol cumple?**
Es el Front Controller de Spring MVC: un único servlet que recibe todas las peticiones HTTP de la aplicación. Su trabajo es delegar — consulta al `HandlerMapping` para encontrar el controller adecuado, invoca el método correspondiente via `HandlerAdapter`, y luego pasa la respuesta al `MessageConverter` (para APIs REST) o al `ViewResolver` (para aplicaciones web con plantillas). Al concentrar el enrutamiento en un único punto, permite aplicar filtros, interceptores y manejo de errores de forma transversal.

---

**Diferencia entre @Controller y @RestController**
`@Controller` marca una clase como handler de Spring MVC cuyo método devuelve el nombre de una vista (template de Thymeleaf, JSP…). `@RestController` es un atajo que combina `@Controller` + `@ResponseBody`: indica que el valor de retorno de cada método se escribe directamente en el cuerpo de la respuesta HTTP (serializado como JSON por Jackson). Se usa para APIs REST donde no hay vistas que renderizar.

---

**¿Cómo manejas errores de forma centralizada en Spring MVC?**
Con `@ControllerAdvice` (o `@RestControllerAdvice` para APIs). Esta anotación marca una clase cuyos métodos `@ExceptionHandler` aplican a todos los controllers. Cada método recibe la excepción concreta y devuelve la respuesta apropiada (código HTTP + cuerpo de error). Ventaja: un único lugar para toda la gestión de errores — los controllers quedan limpios de try/catch. Se puede combinar con `@ResponseStatus` para fijar el código HTTP sin usar `ResponseEntity`.

---

**¿Qué es ResponseEntity y cuándo lo usas en lugar de devolver el objeto directamente?**
`ResponseEntity<T>` encapsula el cuerpo de la respuesta, el código HTTP y las cabeceras. Devolver el objeto directamente equivale a `200 OK` sin cabeceras adicionales. Se usa `ResponseEntity` cuando necesitas control explícito: `201 Created` con cabecera `Location` al crear un recurso, `404 Not Found` con cuerpo vacío, o `204 No Content` al eliminar. Si el código siempre es 200 y no hay cabeceras especiales, devolver el objeto directamente es más limpio.

---

**¿Cómo validas el body de una request POST?**
Con Bean Validation: añades anotaciones al DTO (`@NotNull`, `@Size`, `@Positive`…) y usas `@Valid` en el parámetro `@RequestBody` del controller. Si la validación falla, Spring lanza `MethodArgumentNotValidException` automáticamente. Para responder con un error descriptivo, capturas esa excepción en un `@ControllerAdvice` y extraes los mensajes con `ex.getBindingResult().getFieldErrors()`. Nunca valides manualmente en el controller.

---

**¿Qué es @PathVariable vs @RequestParam? Da un ejemplo de cuándo usar cada uno.**
`@PathVariable` extrae un segmento de la propia URL: `GET /productos/42` → `@PathVariable Long id`. Se usa para identificar un recurso concreto — el valor forma parte de la ruta y es obligatorio. `@RequestParam` extrae parámetros de la query string: `GET /productos?categoria=libros&pagina=2` → `@RequestParam String categoria`. Se usa para filtros, paginación y opciones que no identifican por sí solos el recurso. Regla práctica: si sin ese valor la URL no tiene sentido → `@PathVariable`; si es opcional o de búsqueda → `@RequestParam`.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
