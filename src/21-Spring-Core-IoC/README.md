<div align="center">
  <a href="#"><img src="../../assets/modules/banner-21-spring-ioc-v1.svg" width="100%" alt=""/></a>
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

**Inversion of Control (IoC)** es el principio por el que la responsabilidad de crear y gestionar objetos pasa de la propia aplicación al **contenedor** (Spring). En lugar de que un objeto cree sus dependencias, el contenedor las provee.

**Dependency Injection (DI)** es el mecanismo que implementa IoC: el contenedor *inyecta* las dependencias en el objeto.

```java
// Sin IoC: el objeto gestiona su dependencia
public class UserService {
    private UserRepository repo = new MySQLUserRepository(); // acoplado
}

// Con IoC: Spring inyecta la dependencia
@Service
public class UserService {
    private final UserRepository repo; // desacoplado — interfaz

    public UserService(UserRepository repo) { this.repo = repo; }
}
```

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Tipos de Inyección de Dependencias:**

| Tipo | Cuándo usar | Anotación |
|---|---|---|
| **Constructor** | Siempre que sea posible (recomendado) | `@Autowired` (opcional si un constructor) |
| **Setter** | Dependencias opcionales | `@Autowired` en setter |
| **Campo** | Evitar (oculta dependencias, dificulta tests) | `@Autowired` en campo |

**¿Por qué preferir inyección por constructor?**
- Las dependencias son explícitas e inmutables (`final`).
- Facilita los tests: se puede crear el objeto manualmente sin contenedor.
- Spring puede detectar dependencias circulares en compilación.

**Anotaciones clave:**

```java
@Autowired   // inyección automática por tipo
@Qualifier("miBean")  // especifica qué bean inyectar si hay ambigüedad
@Primary     // bean preferido cuando hay múltiples del mismo tipo
@Value("${app.nombre}")  // inyecta valor de properties
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

-  Bajo acoplamiento: código de negocio depende de interfaces, no de implementaciones.
- Testabilidad: se puede inyectar mocks en los tests.
- Configuración centralizada: Spring gestiona el ciclo de vida.
- Menos boilerplate: no hay `new SomeService(new SomeRepo(new DataSource(...)))`.

Este módulo es solo teoría — ver los módulos de Spring Boot para ejemplos ejecutables.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
