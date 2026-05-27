<div align="center">
  <a href="#"><img src="../../assets/modules/banner-22-spring-beans-v1.svg" width="100%" alt=""/></a>
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

**¿Cuál es la diferencia entre singleton y prototype scope?**
Singleton: Spring crea una única instancia por contenedor y la reutiliza siempre. Prototype: Spring crea una nueva instancia cada vez que se solicita el bean (cada inyección, cada `getBean()`). Singleton para servicios stateless; prototype para objetos con estado por solicitud.

---

**¿Cuándo se ejecuta `@PostConstruct`?**
Después de que el contenedor inyecta todas las dependencias pero antes de que el bean quede disponible para su uso. Se usa para inicialización que requiere las dependencias: conectar a un servicio externo, cargar caché inicial, validar configuración.

---

**¿Qué pasa si inyectas un bean prototype en un bean singleton?**
El prototype se crea solo una vez — en el momento de inicializar el singleton. Si necesitas una instancia nueva en cada invocación, debes obtenerla directamente del `ApplicationContext` con `getBean()`, usar el método `@Lookup`, o inyectar un `Provider<T>` (Jakarta CDI) u `ObjectFactory<T>` (Spring).

---

**¿Qué diferencia hay entre `@Component`, `@Service` y `@Repository`?**
Funcionalmente son equivalentes — todos declaran un bean Spring. La diferencia es semántica y de funcionalidades extras: `@Repository` añade traducción automática de excepciones específicas de la base de datos a `DataAccessException`. `@Service` comunica intención. Úsalos para documentar la capa arquitectónica.

---

**¿Qué es BeanFactory vs ApplicationContext?**
`BeanFactory` es la interfaz base: carga y gestiona beans (lazy initialization). `ApplicationContext` extiende BeanFactory con: event publishing, internacionalización (MessageSource), AOP integration, inicialización eager de singletons. En aplicaciones Spring Boot siempre se usa ApplicationContext.

---

**¿Qué es un `FactoryBean<T>` y cuándo lo usarías?**
`FactoryBean<T>` es una interfaz que permite a Spring obtener el bean del método `getObject()` en vez de instanciar la clase directamente. Se usa para crear objetos cuya construcción es compleja (conexiones, proxies, objetos de terceros sin constructor público). Accedes a la propia factory con `&nombreBean` en el contexto. Internamente, `ProxyFactoryBean` de Spring AOP es un `FactoryBean`.

---

**¿Qué problema resuelve `@Scope("request")` y qué infraestructura necesita?**
El scope `request` crea una instancia nueva del bean por cada petición HTTP, destruyéndola al finalizarla. Resuelve el problema de compartir estado mutable entre peticiones en un bean singleton. Requiere que el contexto web esté activo (un `DispatcherServlet` o un `RequestContextListener`). En beans singleton que inyectan un bean request-scoped, Spring inyecta un proxy ScopedProxyMode para que cada llamada resuelva el bean correcto del hilo actual.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
