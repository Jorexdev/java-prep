# Ejercicios — 34 Spring MVC

## Difícil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Mini DispatcherServlet**
Implementa una clase `DispatcherServlet` con `register(String method, String path, HandlerMethod h)` y `dispatch(Request req)`. Soporta rutas estáticas y rutas con parámetro (`/productos/{id}`). Registra tres endpoints: `GET /productos`, `POST /productos` y `GET /productos/{id}`.

**Ejercicio 2 — Handler Interceptor Chain con cortocircuito**
Define una interfaz `HandlerInterceptor` con `preHandle`, `postHandle` y `afterCompletion`. Implementa `InterceptorChain` que ejecute todos los interceptores en orden y corte la cadena si algún `preHandle` devuelve `false`. Implementa `AuthInterceptor` (rechaza sin token) y `LoggingInterceptor` (imprime timing).

**Ejercicio 3 — Validador con reflexión**
Define anotaciones simuladas como `@interface` (`NotBlank`, `Min`, `Max`). Implementa `BeanValidator` que use reflexión (`getDeclaredFields()`, `getAnnotations()`) para validar automáticamente un bean. Aplícalo a `CreateProductRequest` con `@NotBlank String nombre` y `@Min(1) @Max(1000) int precio`.

**Ejercicio 4 — ProblemDetail (RFC 7807)**
Implementa `ProblemDetail(type, title, status, detail, instance)` y `ApiException`. Crea `ProblemDetailFactory` con métodos estáticos `notFound`, `badRequest` y `conflict`. Implementa `ErrorHandler` que convierta cualquier excepción en un ProblemDetail serializado como JSON.
