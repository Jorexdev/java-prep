import java.util.HashMap;
import java.util.Map;

// IoC (Inversión de Control) simulado con un contenedor Java plano
// En Spring: AnnotationConfigApplicationContext + @Configuration + @Bean + @Qualifier
// Aquí: SimpleContainer — Map<String,Object> que actúa como ApplicationContext
public class ExpIoC {

    // --- Interfaz — el servicio depende de la abstracción, no de la implementación concreta ---
    interface Notificador {
        void enviar(String mensaje);
    }

    static class NotificadorEmail implements Notificador {
        @Override
        public void enviar(String mensaje) {
            System.out.println("[EMAIL] → " + mensaje);
        }
    }

    static class NotificadorSMS implements Notificador {
        @Override
        public void enviar(String mensaje) {
            System.out.println("[SMS] → " + mensaje);
        }
    }

    // DI por constructor — el contenedor inyecta Notificador; ServicioAlertas no sabe cuál
    static class ServicioAlertas {
        private final Notificador notificador;

        ServicioAlertas(Notificador notificador) {
            this.notificador = notificador;
        }

        public void alertar(String mensaje) {
            notificador.enviar(mensaje);
        }
    }

    // --- Contenedor simple (equivale a ApplicationContext) ---
    // En Spring: @Configuration
    static class SimpleContainer {

        private final Map<String, Object> beans = new HashMap<>();

        // Registra un bean con nombre explícito — equivale a @Bean("nombre")
        void register(String name, Object bean) {
            beans.put(name, bean);
        }

        // Recupera un bean por nombre — equivale a ctx.getBean("nombre", Tipo.class)
        <T> T getBean(String name, Class<T> type) {
            Object bean = beans.get(name);
            if (bean == null) throw new RuntimeException("Bean no encontrado: " + name);
            return type.cast(bean);
        }

        // Recupera el primer bean del tipo indicado — equivale a ctx.getBean(Tipo.class)
        <T> T getBean(Class<T> type) {
            return beans.values().stream()
                    .filter(type::isInstance)
                    .map(type::cast)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Bean de tipo " + type.getSimpleName() + " no encontrado"));
        }
    }

    // --- Configuración de beans (equivale a la clase @Configuration) ---
    // En Spring: @Configuration
    static class AppConfig {

        static SimpleContainer crearContexto() {
            SimpleContainer ctx = new SimpleContainer();

            // En Spring: @Bean("email")
            ctx.register("email", new NotificadorEmail());

            // En Spring: @Bean("sms")
            ctx.register("sms", new NotificadorSMS());

            // @Qualifier("email") resuelve la ambigüedad cuando hay múltiples beans del mismo tipo
            // En Spring: @Bean + @Qualifier("email") Notificador notificador
            Notificador notificadorEmail = ctx.getBean("email", Notificador.class);
            ctx.register("servicioAlertas", new ServicioAlertas(notificadorEmail));

            return ctx;
        }
    }

    public static void main(String[] args) {
        SimpleContainer ctx = AppConfig.crearContexto();

        ServicioAlertas servicio = ctx.getBean(ServicioAlertas.class);
        servicio.alertar("Temperatura crítica detectada");

        // Cambiar "email" por "sms" en @Qualifier → mismo servicio, distinto canal
        // ServicioAlertas no cambia — eso es IoC
        Notificador sms = ctx.getBean("sms", Notificador.class);
        sms.enviar("Mensaje directo al bean por nombre");
    }
}
