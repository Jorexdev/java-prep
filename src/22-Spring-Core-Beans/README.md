<div align="center">
  <a href="#"><img src="../../assets/modules/banner-22-spring-beans-v1.svg" width="100%" alt=""/></a>
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

Un **Bean** es cualquier objeto gestionado por el contenedor Spring. Spring crea, configura, inyecta y destruye los beans — el programador solo los declara.

Spring detecta los beans mediante anotaciones:

```java
@Component   // bean genérico
@Service     // capa de servicio (semántica)
@Repository  // capa de acceso a datos (+ traducción de excepciones)
@Controller  // capa web MVC
@Bean        // bean declarado manualmente en @Configuration
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

**Ciclo de vida de un Bean:**

```
1. Instanciación (constructor)
2. Inyección de dependencias
3. @PostConstruct         ← inicialización personalizada
4. Bean en uso
5. @PreDestroy            ← limpieza antes de destrucción
6. Destrucción
```

**Scopes:**

| Scope | Instancias | Contexto |
|---|---|---|
| `singleton` | Una por contenedor (default) | Cualquiera |
| `prototype` | Nueva por cada inyección | Cualquiera |
| `request` | Una por petición HTTP | Web |
| `session` | Una por sesión HTTP | Web |
| `application` | Una por ServletContext | Web |

**¡Cuidado con inyectar prototype en singleton!**
Si un bean `singleton` inyecta un bean `prototype`, el prototype solo se crea una vez (en la inicialización del singleton). Para obtener una nueva instancia en cada llamada, usa `ApplicationContext.getBean()` o `@Lookup`.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Spring gestiona el ciclo de vida automáticamente.
- Scope `singleton` por defecto: eficiente para stateless services.
- `@PostConstruct` para inicialización después de DI (conexiones, caché inicial).
- `@PreDestroy` para limpieza ordenada (cerrar conexiones, flush de recursos).

Este módulo es solo teoría — ver los ejemplos de configuración en Spring Boot.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
