<div align="center">
  <a href="#"><img src="../../assets/modules/banner-20-aop-v1.svg" width="100%" alt=""/></a>
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

**¿Qué es un cross-cutting concern y por qué AOP lo resuelve mejor que herencia?**
Un cross-cutting concern es lógica que se repite en múltiples módulos no relacionados (logging, seguridad, transacciones). La herencia no puede modelarlo porque una clase solo puede extender una, y además acoplaría módulos no relacionados. AOP lo separa en un Aspecto independiente que se aplica declarativamente a cualquier método seleccionado.

---

**¿Cuál es la diferencia entre `@Before` y `@Around`?**
`@Before` ejecuta código antes del método pero no puede interceptar el retorno ni modificar el resultado. `@Around` rodea completamente la ejecución: puede ejecutar código antes y después, modificar argumentos, cambiar el valor de retorno, capturar excepciones, o incluso no invocar el método original. Es el más poderoso y el que usa `@Transactional`.

---

**¿Qué es un Pointcut y cómo se define?**
Un Pointcut es una expresión que selecciona los JoinPoints donde se aplica un Advice. En Spring AOP se define con AspectJ expressions: `execution(* com.ejemplo.servicio.*.*(..))` — cualquier método de cualquier clase en el paquete `servicio`. Se puede combinar con `&&`, `||`, `!`.

---

**¿Qué tipo de proxy usa Spring por defecto?**
Si el bean implementa al menos una interfaz, usa JDK dynamic proxy (proxy de la interfaz). Si no implementa ninguna interfaz, usa CGLIB (subclase generada). Con Spring Boot 2+, CGLIB es el defecto para `@Configuration` aunque implemente interfaces.

---

**¿Puede AOP interceptar llamadas a métodos privados?**
No en Spring AOP (basado en proxies). Al llamar `this.metodoPrivado()` dentro de un bean, la llamada va directamente al objeto, no pasa por el proxy. Para interceptar métodos privados o llamadas internas se necesita AspectJ con weaving en compilación o en carga de clases.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
