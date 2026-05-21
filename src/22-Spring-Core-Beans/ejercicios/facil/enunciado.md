# Ejercicios — 22 Spring Core: Beans

## Fácil

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — Ciclo de vida básico**
Crea la clase `ConexionBD` con dos métodos: `init()` que imprime `"[ConexionBD] Abriendo conexión a jdbc:memoria"` y simula la apertura con un flag `boolean abierta`, y `destroy()` que imprime `"[ConexionBD] Cerrando conexión"` y pone el flag en false. En `main`, instancia manualmente, llama a `init()`, usa la conexión (imprime una query simulada), luego llama a `destroy()`. Muestra el estado en cada fase.

**Ejercicio 2 — Prototype scope**
Crea la clase `SesionUsuario` con un `id` generado con `UUID.randomUUID()` asignado en el constructor. Implementa `ContenedorPrototype` con un `Supplier<SesionUsuario>` configurado en el constructor. El método `nuevaSesion()` llama al supplier cada vez. En `main`, crea 3 sesiones, imprime sus ids y verifica con `==` y `.equals()` que son objetos distintos.

**Ejercicio 3 — Singleton scope**
Implementa `ConfiguracionGlobal` con el patrón Singleton: constructor privado, instancia estática, método `getInstance()`. Añade un contador de llamadas a `getInstance()` que se incrementa en cada llamada y un campo `entorno = "produccion"`. En `main`, obtén la instancia 4 veces, muestra el contador, compara con `==` y demuestra que siempre es el mismo objeto.

**Ejercicio 4 — @Lazy simulado**
Crea `CacheCaliente` con un constructor que imprime `"[CacheCaliente] Inicializando cache (tardará 2s simulados)..."` y un campo `lista` que simula una carga de 100 elementos. Implementa `ContenedorLazy` con registro de beans como `Supplier<T>` y flag `lazy`. Si `lazy=true`, el bean no se instancia hasta que alguien lo pida. Muestra en la consola el momento exacto de la inicialización comparando comportamiento eager vs lazy.

**Ejercicio 5 — @DependsOn simulado**
Crea tres servicios: `ServicioC` (sin deps), `ServicioB` (necesita C), `ServicioA` (necesita B). Cada constructor imprime `"Iniciando ServicioX..."`. Implementa `OrdenContenedor` que acepta beans con una lista de dependencias: `register(String nombre, List<String> dependencias, Supplier<Object> factory)`. El método `startAll()` ordena los beans topológicamente e inicia en ese orden. Demuestra que el orden siempre es C → B → A.

**Ejercicio 6 — Named beans**
Implementa `NamedContainer` con `register(String nombre, Object bean)` y `get(String nombre)`. En `main`, registra tres beans con nombres `"dataSource"`, `"userRepo"` y `"emailService"`, recupéralos por nombre y llama a un método en cada uno. Lanza `NoSuchBeanException` si el nombre no existe.
