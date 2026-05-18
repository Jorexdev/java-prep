<div align="center">
  <a href="#"><img src="../../assets/modules/banner-36-testing-v1.svg" width="100%" alt=""/></a>
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

El **testing** en Java se organiza en la **pirámide de testing**: una base amplia de tests unitarios rápidos, una capa intermedia de tests de integración, y en la cima los tests E2E (end-to-end) más lentos y costosos. El estándar de la industria es **JUnit 5** (Jupiter), combinado con **Mockito** para dobles de prueba.

```
        /\
       /  \       E2E — pocos, lentos, costosos
      /----\
     /      \     Integración — BD, HTTP, contexto Spring
    /--------\
   /          \   Unitarios — rápidos, aislados, muchos
  /____________\
```

**JUnit 5 — anotaciones esenciales:**

```java
@Test                     // marca un método como test
@BeforeEach               // se ejecuta antes de cada @Test
@AfterEach                // se ejecuta después de cada @Test
@BeforeAll                // una sola vez antes de todos (método static)
@AfterAll                 // una sola vez después de todos (método static)
@ParameterizedTest        // test con múltiples valores de entrada
@ValueSource(ints = {1, 2, 3})     // fuente de valores simples
@CsvSource({"1,2,3", "4,5,9"})     // fuente de pares CSV
@DisplayName("descripción legible") // nombre descriptivo
@Disabled("motivo")       // deshabilita temporalmente
```

**Aserciones clave:**

```java
assertEquals(expected, actual);
assertTrue(condition);
assertFalse(condition);
assertNull(value);
assertNotNull(value);
assertThrows(IllegalArgumentException.class, () -> servicio.metodo(null));
assertAll(
    () -> assertEquals(1, resultado.getId()),
    () -> assertEquals("Ana", resultado.getNombre())
);
```

**TDD — ciclo red → green → refactor:**

```
1. RED    — escribe un test que falla (la funcionalidad no existe)
2. GREEN  — escribe el mínimo código para que el test pase
3. REFACTOR — mejora el código sin romper los tests
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

**Mockito — dobles de prueba:**

```java
// Crear mock
RepositorioUsuarios repo = mock(RepositorioUsuarios.class);

// Configurar comportamiento
when(repo.buscarPorId(1L)).thenReturn(new Usuario(1L, "Ana"));
when(repo.buscarPorId(99L)).thenThrow(new NotFoundException());

// Inyección automática con anotaciones
@Mock
RepositorioUsuarios repositorio;

@InjectMocks
ServicioUsuarios servicio;    // Mockito inyecta los @Mock en el constructor

// Verificar interacciones
verify(repositorio).buscarPorId(1L);
verify(repositorio, times(2)).guardar(any());
verify(repositorio, never()).eliminar(any());

// Capturar argumentos
ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
verify(repositorio).guardar(captor.capture());
assertEquals("Ana", captor.getValue().getNombre());
```

**@Mock vs @Spy:**

| | @Mock | @Spy |
|---|---|---|
| Comportamiento por defecto | Devuelve null/0/false | Llama al método real |
| Uso | Aislar dependencias externas | Verificar llamadas en objetos reales |
| Cuando usarlo | La mayoría de casos | Cuando necesitas lógica real + verificación |

**Slices de test en Spring Boot:**

```java
// Levanta el contexto completo — tests de integración
@SpringBootTest

// Solo la capa web (Controllers + MockMvc) — sin BD
@WebMvcTest(UsuarioController.class)

// Solo la capa de persistencia (JPA + BD en memoria)
@DataJpaTest
```

**MockMvc — test de controllers HTTP:**

```java
@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean ServicioUsuarios servicio;

    @Test
    void debeRetornar200CuandoUsuarioExiste() throws Exception {
        when(servicio.buscarPorId(1L)).thenReturn(new Usuario(1L, "Ana"));

        mockMvc.perform(get("/usuarios/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.nombre").value("Ana"));
    }
}
```

**Testcontainers — integración con BD real:**

```java
@SpringBootTest
@Testcontainers
class RepositorioUsuariosIT {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Test
    void debeGuardarYRecuperarUsuario() {
        // usa una BD PostgreSQL real en Docker
    }
}
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

- **Detección temprana de bugs**: un test roto en local es infinitamente más barato que un bug en producción.
- **Documentación viva**: los tests describen el comportamiento esperado del sistema mejor que cualquier doc.
- **Refactoring seguro**: con una suite sólida puedes reestructurar código con confianza — si los tests pasan, el comportamiento es correcto.
- **CI/CD confiable**: el pipeline rechaza cambios que rompen tests antes de llegar a producción.
- **TDD fuerza buen diseño**: escribir el test primero obliga a pensar en la API pública antes que en la implementación, lo que resulta en código más desacoplado.

Ver [ExpUnitTest.java](ExpUnitTest.java) para un mini-framework de testing desde cero y [ExpMockSimulation.java](ExpMockSimulation.java) para la simulación del patrón Mockito con Java puro.

<div align="center"><img height="32" width="1" src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1' height='32'/%3E"/></div>

<div align="center">
  <a href="#"><img src="../../assets/separator-v2.svg" width="100%" alt=""/></a>
</div>
