<div align="center">
  <a href="#"><img src="../../assets/modules/banner-21-spring-ioc-v1.svg" width="100%" alt=""/></a>
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

**¿Qué diferencia hay entre IoC y DI?**
IoC es el principio: el control de la creación de objetos se invierte — ya no es el propio objeto quien crea sus dependencias. DI es el patrón que implementa IoC: las dependencias se *inyectan* desde el exterior. IoC es el "qué"; DI es el "cómo".

---

**¿Por qué se prefiere la inyección por constructor sobre la inyección por campo?**
Tres razones: (1) las dependencias son explícitas — el constructor documenta qué necesita la clase; (2) permiten declarar los campos como `final` (inmutabilidad); (3) facilitan los tests unitarios — puedes crear el objeto directamente `new UserService(mockRepo)` sin necesitar el contenedor Spring.

---

**¿Para qué sirve `@Qualifier` vs `@Primary`?**
Cuando hay múltiples beans del mismo tipo, Spring no sabe cuál inyectar. `@Primary` marca un bean como preferido — se inyecta por defecto. `@Qualifier("nombre")` es más específico: en el punto de inyección indicas exactamente qué bean quieres. `@Primary` es general; `@Qualifier` es específico por punto de inyección.

---

**¿Qué pasa si hay dos beans del mismo tipo sin `@Qualifier`?**
Spring lanza `NoUniqueBeanDefinitionException` al arrancar. El contenedor no puede decidir cuál inyectar. La solución es añadir `@Primary` al bean preferido, o usar `@Qualifier` en el punto de inyección.

---

**¿Cómo inyectas un valor de properties con `@Value`?**
`@Value("${app.nombre}")` inyecta el valor de la propiedad `app.nombre` del `application.properties`. Si la propiedad no existe, falla al arrancar a menos que especifiques un default: `@Value("${app.nombre:defaultValue}")`.

---

**¿Qué es `@Lazy` y cuándo conviene usarlo?**
Un bean `@Lazy` no se instancia al arrancar el contexto sino la primera vez que se necesita. Es útil para beans pesados o que rara vez se usan para reducir el tiempo de arranque. Si un bean `@Lazy` se inyecta en uno eager, Spring crea un proxy y retrasa la inicialización real hasta la primera llamada. Se usa también para romper dependencias circulares en casos puntuales.

---

**¿Cómo funciona el component scan y cómo lo limitas a un paquete concreto?**
`@ComponentScan` hace que Spring busque clases anotadas con `@Component`, `@Service`, `@Repository` y `@Controller` dentro del paquete base y sus subpaquetes. Sin configuración explícita, `@SpringBootApplication` escanea el paquete donde está la clase principal. Puedes acotarlo con `@ComponentScan(basePackages = "com.example.domain")` o excluir clases con `excludeFilters`. Un scan demasiado amplio ralentiza el arranque y puede registrar beans no deseados.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
