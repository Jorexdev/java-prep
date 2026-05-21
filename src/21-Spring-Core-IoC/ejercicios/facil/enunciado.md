# Ejercicios — 21 Spring Core: IoC

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — DI por constructor simulada**
Define una interfaz `Repositorio` con un método `guardar(String dato)`. Implementa dos clases: `RepositorioMemoria` (guarda en una lista en memoria e imprime cada entrada) y `RepositorioFichero` (imprime como si escribiera en disco). Crea la clase `Servicio` que recibe un `Repositorio` por constructor y expone un método `procesar(String dato)` que delega en el repositorio. En `main`, instancia manualmente ambas implementaciones, inyéctalas en `Servicio` y llama a `procesar`.

**Ejercicio 2 — DI por setter simulada**
Repite el ejercicio anterior pero usa un setter `setRepositorio(Repositorio r)` en lugar del constructor. Demuestra que puedes cambiar la implementación en runtime: empieza con `RepositorioMemoria`, procesa un dato, cambia a `RepositorioFichero` con el setter y procesa otro dato. Muestra que el comportamiento cambia sin recrear el `Servicio`.

**Ejercicio 3 — Contenedor IoC mínimo**
Implementa un `ContenedorIoC` con un `Map<Class<?>, Object>` interno. Añade los métodos `register(Class<?> tipo, Object instancia)` y `get(Class<?> tipo)`. En `main`, registra dos beans (un `String` con valor `"hola"` y un `Integer` con valor `42`), recupéralos con `get` y muéstralos por consola. Lanza `IllegalStateException` si se pide un tipo no registrado.

**Ejercicio 4 — @Primary simulado**
Define la interfaz `Formatter` con `String formatear(String texto)`. Implementa `FormatterMayusculas` y `FormatterMinusculas`. Crea `ContenedorSimple` que guarda una lista de formateadores y sabe cuál es el "primary" (el primero registrado). Método `getFormatter()` devuelve el primary. En `main`, registra los dos, muestra cuál es el primary y lo usa para formatear un texto.

**Ejercicio 5 — @Qualifier simulado**
Usa las mismas implementaciones de `Formatter` del ejercicio 4. Crea un `Map<String, Formatter>` que asocia nombres ("mayusculas", "minusculas") a implementaciones. La clase `Procesador` recibe este mapa y un método `procesar(String qualifier, String texto)` que busca el formatter por nombre. En `main`, procesa el mismo texto con cada qualifier y muestra el resultado.

**Ejercicio 6 — Circular dependency detection**
Crea `ContenedorConDeteccion` con un `Set<String>` de clases en construcción. El método `crear(String nombreClase)` añade el nombre al Set antes de "construir" el bean y lo elimina al terminar. Si al llamar `crear` el nombre ya está en el Set, lanza `CircularDependencyException` con un mensaje que incluya el nombre del bean. En `main`, demuestra el flujo normal y el flujo con dependencia circular.
