package spring.core.ioc;

/*
    IOC Y DEPENDENCY INJECTION — Spring Core

    ► ¿Qué es IoC (Inversion of Control)?
      Principio de diseño en el que el control del flujo de la aplicación
      se invierte: en lugar de que el objeto cree sus propias dependencias,
      es un contenedor externo (Spring) quien las crea e inyecta.

      Sin IoC (control en manos del objeto):
        class ServicioUsuario {
            private RepositorioUsuario repo = new RepositorioUsuarioDB();  // acoplamiento fuerte
        }

      Con IoC (control en manos del contenedor):
        class ServicioUsuario {
            private final RepositorioUsuario repo;
            // el contenedor inyecta la implementación concreta
        }

    ► ¿Qué es DI (Dependency Injection)?
      Implementación concreta del principio IoC. El contenedor de Spring
      instancia los objetos y "pasa" (inyecta) sus dependencias.

      Spring actúa como ApplicationContext: fábrica y registro de todos los beans.

    ── TIPOS DE INYECCIÓN ─────────────────────────────────────────────────────

    ► 1. Inyección por constructor (RECOMENDADA)

        @Service
        public class PedidoService {

            private final InventarioService inventario;
            private final NotificacionService notificacion;

            // Spring inyecta automáticamente si hay un solo constructor
            public PedidoService(InventarioService inventario,
                                 NotificacionService notificacion) {
                this.inventario = inventario;
                this.notificacion = notificacion;
            }
        }

      Ventajas:
        - Las dependencias son inmutables (final).
        - El objeto siempre está completamente inicializado.
        - Facilita el testing (se puede inyectar mocks sin Spring).
        - Detecta dependencias circulares en tiempo de arranque.

    ► 2. Inyección por setter

        @Service
        public class PedidoService {

            private InventarioService inventario;

            @Autowired
            public void setInventario(InventarioService inventario) {
                this.inventario = inventario;
            }
        }

      Usar solo para dependencias opcionales.

    ► 3. Inyección por campo (NO recomendada en producción)

        @Service
        public class PedidoService {

            @Autowired
            private InventarioService inventario;  // difícil de testear
        }

      Problemas:
        - No se puede usar con final → objeto mutable.
        - Requiere reflexión → testing más difícil.
        - Dependencias circulares se detectan tarde (en runtime).

    ── ANOTACIONES DE INYECCIÓN ───────────────────────────────────────────────

    ► @Autowired
      Indica a Spring que debe inyectar el bean correspondiente.
      Desde Spring 4.3, es opcional si hay un solo constructor.

    ► @Qualifier
      Cuando hay varias implementaciones de una interfaz, especifica cuál usar:

        @Autowired
        @Qualifier("inventarioRedis")
        private InventarioService inventario;

    ► @Primary
      Marca un bean como preferido cuando hay ambigüedad:

        @Primary
        @Service("inventarioRedis")
        public class InventarioRedisService implements InventarioService { ... }

    ► @Value
      Inyecta valores de properties o expresiones SpEL:

        @Value("${app.timeout:30}")
        private int timeout;

    ── EJEMPLO COMPLETO ───────────────────────────────────────────────────────

        public interface NotificacionService {
            void notificar(String mensaje);
        }

        @Service
        public class EmailNotificacionService implements NotificacionService {
            public void notificar(String mensaje) {
                System.out.println("Email: " + mensaje);
            }
        }

        @Service
        public class PedidoService {

            private final NotificacionService notificacion;

            public PedidoService(NotificacionService notificacion) {
                this.notificacion = notificacion;
            }

            public void crearPedido(Pedido pedido) {
                // lógica de negocio...
                notificacion.notificar("Pedido creado: " + pedido.getId());
            }
        }

        // En el test, se puede inyectar un mock sin levantar Spring:
        class PedidoServiceTest {
            @Test
            void crearPedido_debeNotificar() {
                NotificacionService mockNotif = mock(NotificacionService.class);
                PedidoService service = new PedidoService(mockNotif);
                service.crearPedido(new Pedido("123"));
                verify(mockNotif).notificar(contains("123"));
            }
        }

    ► ApplicationContext
      El contenedor IoC de Spring. Gestiona el ciclo de vida de todos los beans.

        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        PedidoService service = ctx.getBean(PedidoService.class);

      En Spring Boot el contexto se crea automáticamente al arrancar.

    ► Preguntas típicas de entrevista
      - ¿Qué diferencia hay entre IoC y DI?
      - ¿Por qué es preferible la inyección por constructor?
      - ¿Qué pasa si hay dos beans del mismo tipo sin @Qualifier?
      - ¿Cómo detecta Spring las dependencias circulares?
      - ¿Qué es BeanFactory vs ApplicationContext?
*/
public class Intro {}
