<div align="center">
  <a href="#"><img src="../../assets/modules/banner-36-testing-v1.svg" width="100%" alt=""/></a>
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

**¿Cuál es la diferencia entre `@Mock` y `@Spy` en Mockito?**

`@Mock` crea un objeto completamente falso: todos sus métodos devuelven null/0/false por defecto y no ejecutan lógica real. `@Spy` envuelve una instancia real del objeto y ejecuta los métodos reales salvo que configures un comportamiento con `when()`. Usa `@Mock` para aislar dependencias externas (repositorios, clientes HTTP). Usa `@Spy` cuando necesitas que parte de la lógica real se ejecute pero quieres verificar o interceptar llamadas.

---

**¿Cuándo usarías `@SpringBootTest` vs `@WebMvcTest`?**

`@SpringBootTest` levanta el contexto completo de Spring, ideal para tests de integración que verifican la colaboración entre capas (controller → service → repositorio → BD). Es lento. `@WebMvcTest` solo levanta la capa web (controllers, filtros, `MockMvc`) sin la capa de servicio ni persistencia — los servicios se mockean con `@MockBean`. Es mucho más rápido y adecuado para tests unitarios de controllers. Regla práctica: `@WebMvcTest` para probar que el controller mapea bien las rutas, valida entradas y devuelve los códigos HTTP correctos.

---

**¿Qué es TDD y qué ventajas tiene frente a escribir tests después?**

TDD (Test-Driven Development) es escribir el test antes que el código de producción, siguiendo el ciclo **red → green → refactor**. Ventajas frente a testear después: (1) fuerza a pensar en la API pública y los casos de uso antes de implementar, resultando en diseños más desacoplados; (2) el test siempre falla por la razón correcta, validando que el test es útil; (3) evita escribir código innecesario porque solo implementas lo mínimo para pasar el test; (4) el refactor posterior tiene cobertura de seguridad inmediata.

---

**¿Cómo verificas que un método fue llamado con ciertos argumentos en Mockito?**

Con `verify()` combinado con `ArgumentCaptor` cuando necesitas inspeccionar el argumento exacto:

```java
ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
verify(repositorio).guardar(captor.capture());
assertEquals("Ana", captor.getValue().getNombre());
assertEquals("ana@email.com", captor.getValue().getEmail());
```

Para verificaciones simples puedes usar matchers directamente: `verify(repo).guardar(argThat(u -> u.getNombre().equals("Ana")))`. Para verificar número de invocaciones: `verify(repo, times(2)).buscarPorId(any())`, `verify(repo, never()).eliminar(any())`.

---

**¿Qué es un test de integración y cómo lo diferencias de un test unitario?**

Un **test unitario** prueba una sola unidad en aislamiento — las dependencias se sustituyen por mocks, es rápido (milisegundos) y no requiere infraestructura externa. Un **test de integración** verifica que varios componentes colaboran correctamente: puede incluir BD real (con H2 en memoria o Testcontainers), llamadas HTTP reales, o el contexto Spring completo. Es más lento y más representativo del comportamiento en producción. La pirámide de testing recomienda muchos unitarios, pocos de integración, y muy pocos E2E.

---

**¿Para qué sirve `@ParameterizedTest`? Da un ejemplo.**

`@ParameterizedTest` ejecuta el mismo test con distintos valores de entrada, evitando duplicar código. Ejemplo:

```java
@ParameterizedTest
@CsvSource({
    "2, true",
    "3, true",
    "4, false",
    "17, true",
    "18, false"
})
void debeDetectarNumeroPrimo(int numero, boolean esperado) {
    assertEquals(esperado, calculadora.esPrimo(numero));
}
```

Con `@ValueSource` para un solo parámetro: `@ValueSource(strings = {"", " ", null})` para probar entradas inválidas. Con `@MethodSource` para objetos complejos generados por un método factory.

---

**¿Qué son Testcontainers y cuándo los usarías?**

Testcontainers es una librería Java que levanta contenedores Docker reales durante los tests de integración. En lugar de usar H2 (que tiene diferencias de comportamiento con PostgreSQL), arranca un contenedor de la BD real y lo destruye al terminar. Úsalos cuando: (1) la lógica depende de características específicas de la BD (tipos JSON, funciones específicas, índices); (2) quieres tener confianza real de que las queries JPA funcionan en producción; (3) tests de integración con Kafka, Redis, o cualquier servicio externo. El coste es un startup más lento, compensado por la fidelidad con producción.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
