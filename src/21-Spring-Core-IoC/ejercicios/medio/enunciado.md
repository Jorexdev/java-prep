# Ejercicios — 21 Spring Core: IoC

## Medio

Las soluciones están en [soluciones/](soluciones/).

---

**Ejercicio 1 — ApplicationContext mínimo**
Implementa `AppContext` con `scan(Object... beans)` que registra los beans usando su clase como clave. Añade `getBean(String name)` que busca por nombre simple de clase (ignorando mayúsculas/minúsculas). Usa reflection (`getClass().getSimpleName()`) para el nombre. En `main`, registra tres beans de tipos distintos, recupéralos por nombre y demuestra que se resuelven correctamente.

**Ejercicio 2 — Scope singleton vs prototype**
Implementa `ContenedorScopes` con dos métodos: `registerSingleton(Class<?>, Object)` y `registerPrototype(Class<?>, Supplier<?>)`. `getBean(Class<?>)` devuelve siempre la misma instancia para singleton y llama al Supplier para prototype. En `main`, obtén el mismo bean singleton dos veces y compara con `==`. Obtén el mismo bean prototype dos veces y compara con `==` y `hashCode`. Muestra claramente la diferencia.

**Ejercicio 3 — Lazy initialization**
Crea `ContenedorLazy` que acepta beans como `Supplier<T>`. El bean no se crea hasta la primera llamada a `get(Class<?>)`. Al crear el Supplier, imprime `"[LAZY] Inicializando CacheCaliente..."`. En `main`, registra el bean lazy, demuestra que no se inicializa al registrar (no aparece el print), luego llama a `get` dos veces y verifica que el print solo aparece una vez (la segunda llamada devuelve la instancia ya creada).

**Ejercicio 4 — BeanFactory con auto-wiring**
Implementa `BeanFactory` con registro de clases (no instancias). Al llamar `getBean(Class<?>)`, el factory analiza el constructor de la clase con `getDeclaredConstructors()`, detecta qué tipos necesita y los resuelve recursivamente del propio contenedor. Demuestra con tres clases: `RepositorioImpl` (sin deps), `ServicioImpl` (necesita `RepositorioImpl`) y `ControladorImpl` (necesita `ServicioImpl`). Registra solo las clases y pide el `ControladorImpl`.

**Ejercicio 5 — Conditional bean**
Implementa `ContenedorCondicional` con un `Map<String, String>` de configuración y el método `registerIf(String propiedad, String valorEsperado, Supplier<?> factory)`. El bean solo se crea y registra si la propiedad tiene el valor esperado. Crea una clase `DataSource` simulada. En `main`, ejecuta con `"db.enabled"="true"` (debe crear el bean) y con `"db.enabled"="false"` (no debe crearlo). Muestra un mensaje en cada caso.
