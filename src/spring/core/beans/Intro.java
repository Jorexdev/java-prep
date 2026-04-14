package spring.core.beans;

/*
    BEANS EN SPRING — Ciclo de vida y scopes

    ► ¿Qué es un Bean?
      Un Bean es cualquier objeto gestionado por el contenedor IoC de Spring.
      Spring se encarga de crearlo, configurarlo, inyectar sus dependencias
      y destruirlo cuando ya no se necesita.

    ── CÓMO DECLARAR UN BEAN ──────────────────────────────────────────────────

    ► Anotaciones de estereotipo (detección automática con @ComponentScan)

      @Component   → bean genérico
      @Service     → lógica de negocio (semántica, sin diferencia técnica)
      @Repository  → acceso a datos (activa traducción de excepciones JPA)
      @Controller  → controlador MVC
      @RestController → @Controller + @ResponseBody

    ► Declaración explícita con @Bean

        @Configuration
        public class AppConfig {

            @Bean
            public ObjectMapper objectMapper() {
                return new ObjectMapper()
                    .registerModule(new JavaTimeModule());
            }

            @Bean
            public RestTemplate restTemplate() {
                return new RestTemplate();
            }
        }

      Usar @Bean cuando la clase viene de una librería externa (no se puede anotar).

    ── CICLO DE VIDA DE UN BEAN ───────────────────────────────────────────────

      El ciclo completo de un bean es:

      1. Instanciación → Spring crea el objeto con el constructor.
      2. Inyección de dependencias → @Autowired, @Value, etc.
      3. Llamada a @PostConstruct → método de inicialización personalizado.
      4. Uso → el bean está disponible para inyección.
      5. Llamada a @PreDestroy → justo antes de que el contexto se cierre.
      6. Destrucción → el objeto se elimina de memoria.

    ► @PostConstruct y @PreDestroy

        @Service
        public class ConexionService {

            private Connection conexion;

            @PostConstruct
            public void init() {
                // Se ejecuta después de inyectar todas las dependencias
                this.conexion = abrirConexion();
                System.out.println("Conexión establecida");
            }

            @PreDestroy
            public void cleanup() {
                // Se ejecuta antes de destruir el bean
                conexion.close();
                System.out.println("Conexión cerrada");
            }
        }

    ► InitializingBean / DisposableBean (alternativa, menos usada)

        public class MiBean implements InitializingBean, DisposableBean {
            public void afterPropertiesSet() { /* init */ }
            public void destroy() { /* cleanup */ }
        }

    ► Hooks en @Bean

        @Bean(initMethod = "init", destroyMethod = "cleanup")
        public MiServicio miServicio() { return new MiServicio(); }

    ── SCOPES DE UN BEAN ──────────────────────────────────────────────────────

    ► Singleton (por defecto)
      Una sola instancia del bean por ApplicationContext.
      Se crea al arrancar el contexto (eager) o al primer uso (lazy).

        @Service  // singleton por defecto
        public class UsuarioService { ... }

        @Lazy
        @Service  // se crea solo al primer uso
        public class PesadoServicio { ... }

      ⚠ Cuidado con el estado mutable en singletons → problemas de concurrencia.

    ► Prototype
      Se crea una nueva instancia cada vez que se solicita el bean.
      Spring NO gestiona su destrucción (@PreDestroy no se invoca).

        @Scope("prototype")
        @Component
        public class ReporteGenerator { ... }

    ► Request (solo en contexto web)
      Una instancia por cada request HTTP.

        @Scope(value = WebApplicationContext.SCOPE_REQUEST,
               proxyMode = ScopedProxyMode.TARGET_CLASS)
        @Component
        public class RequestContext { ... }

    ► Session (solo en contexto web)
      Una instancia por cada sesión HTTP.

        @Scope(value = WebApplicationContext.SCOPE_SESSION,
               proxyMode = ScopedProxyMode.TARGET_CLASS)
        @Component
        public class CarritoCompra { ... }

    ► Application (solo en contexto web)
      Una instancia por ServletContext (similar a singleton en web apps).

    ── RESUMEN DE SCOPES ──────────────────────────────────────────────────────

      Scope       | Instancias         | Contexto
      ------------|--------------------|----------------
      singleton   | 1 por contexto     | Cualquiera
      prototype   | 1 por petición     | Cualquiera
      request     | 1 por HTTP request | Web
      session     | 1 por HTTP session | Web
      application | 1 por ServletCtx   | Web

    ── PROBLEMA: SINGLETON CON DEPENDENCIA PROTOTYPE ──────────────────────────

      Si un singleton inyecta un bean prototype, siempre recibirá la misma
      instancia (la que se inyectó al crear el singleton).

      Solución: inyectar ApplicationContext o usar @Lookup:

        @Service
        public class MiServicio {

            @Autowired
            private ApplicationContext ctx;

            public void procesar() {
                // nueva instancia cada vez
                MiPrototype p = ctx.getBean(MiPrototype.class);
            }
        }

    ► Preguntas típicas de entrevista
      - ¿Cuál es el scope por defecto de un bean en Spring?
      - ¿Cuándo usarías prototype en lugar de singleton?
      - ¿Qué pasa si tienes estado mutable en un bean singleton con concurrencia?
      - ¿En qué orden se ejecuta @PostConstruct respecto a @Autowired?
      - ¿Por qué @PreDestroy no se llama en beans prototype?
*/
public class Intro {}
