<div align="center">
  <a href="#"><img src="../../assets/modules/banner-20-aop-v1.svg" width="100%" alt=""/></a>
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

**Aspect-Oriented Programming (AOP)** es un paradigma que permite separar los **cross-cutting concerns** (preocupaciones transversales) del código de negocio. Un cross-cutting concern es lógica que se repite en múltiples puntos del sistema: logging, seguridad, transacciones, auditoría, métricas.

Sin AOP:
```java
public void crearPedido(Pedido p) {
    log.info("Inicio crearPedido"); // logging mezclado con negocio
    checkSeguridad();               // seguridad mezclada
    // lógica de negocio real
    log.info("Fin crearPedido");
}
```

Con AOP, la lógica de negocio queda limpia y los concerns transversales se definen en **Aspectos** separados.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-key-features-v2.svg" width="100%" alt="// key features"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

**Conceptos clave:**

| Concepto | Descripción |
|---|---|
| **Aspect** | Clase que encapsula el cross-cutting concern |
| **JoinPoint** | Punto de ejecución donde se puede aplicar un aspecto (método, constructor...) |
| **Pointcut** | Expresión que selecciona qué JoinPoints aplicar |
| **Advice** | El código que se ejecuta en el JoinPoint |
| **Weaving** | Proceso de insertar los aspectos en el código |

**Tipos de Advice:**

| Advice | Cuándo se ejecuta |
|---|---|
| `@Before` | Antes del método |
| `@After` | Después del método (siempre) |
| `@AfterReturning` | Solo si el método retorna normalmente |
| `@AfterThrowing` | Solo si el método lanza excepción |
| `@Around` | Rodea el método — control total |

**Spring AOP usa proxies:** en lugar de modificar el bytecode, Spring envuelve el bean en un proxy (JDK dynamic proxy para interfaces, CGLIB para clases). Por eso AOP no intercepta llamadas internas (this.metodo()).

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/shared/section-ventajas-v2.svg" width="100%" alt="// ventajas"/></a>
</div>

<div align="center"><img height="16" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='16'/%3E"/></div>

-  Código de negocio limpio: sin logging, seguridad ni transacciones mezcladas.
- Reutilización: un aspecto aplica a cualquier método que coincida con el pointcut.
- Spring Boot usa AOP internamente para `@Transactional`, `@Cacheable`, `@Secured`.
- Cambios en concerns transversales no requieren modificar la lógica de negocio.

Ver [Examples.java](Examples.java) para ejemplos de aspectos con `@Before`, `@Around` y `@AfterThrowing`.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
