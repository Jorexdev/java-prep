# Ejercicios — 34 Spring MVC

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — CRUD REST completo**
Implementa una clase `TareaController` con un `Map` interno que soporte las cinco operaciones básicas: `getAll()`, `getById(int id)`, `create(String titulo)`, `update(int id, boolean completada)` y `delete(int id)`. Los ids se autoincrementan.

**Ejercicio 2 — Paginación manual**
Implementa un método genérico `paginar(List<T> lista, int page, int size)` que extraiga la sublist correcta. Envuelve el resultado en un `PageResponse<T>` con `content`, `page`, `size`, `totalPages` y `totalElements`.

**Ejercicio 3 — Interceptor de tiempo de respuesta**
Define una interfaz `Interceptor` con `preHandle(String req)` y `postHandle(String req, long ms)`. Implementa `TimingInterceptor` que mida el tiempo con `System.nanoTime()`. Crea un `Dispatcher` que aplique la lista de interceptores antes y después de ejecutar el handler.

**Ejercicio 4 — Versionado de API**
Implementa una clase `ApiRouter` con un mapa `versión → handler`. La versión v1 devuelve `Producto(id, nombre)` y la versión v2 devuelve `ProductoV2(id, nombre, categoria, stock)`. El router selecciona el handler según la versión indicada.

**Ejercicio 5 — Content negotiation**
Define una interfaz `Serializer` con `String serialize(Object obj)`. Implementa `JsonSerializer` (formato `{"campo":"valor"}`) y `PlainTextSerializer` (formato `campo=valor`). Crea un `ContentNegotiator` que elija el serializador correcto según el valor del Accept header.

---

**Ejercicio 6 — Content negotiation con XML**
Extiende el ejercicio anterior añadiendo un tercer serializador `XmlSerializer` (formato `<campo>valor</campo>`). Crea un `NegotiatedEndpoint` con un único método `handle(String path, Map<String,String> data, String acceptHeader)` que delegue la serialización al serializador correcto según el Accept header (`application/json`, `application/xml` o `text/plain`). El método debe devolver también el `Content-Type` de la respuesta. El `main` llama al mismo endpoint tres veces, una por cada tipo, y muestra que la representación cambia sin alterar los datos.

---
