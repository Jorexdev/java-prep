import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class ExpIoC {

    // Interfaz — el servicio depende de la abstracción, no de la implementación concreta
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

    // DI por constructor — Spring inyecta Notificador sin que ServicioAlertas sepa cuál
    static class ServicioAlertas {
        private final Notificador notificador;

        ServicioAlertas(Notificador notificador) {
            this.notificador = notificador;
        }

        public void alertar(String mensaje) {
            notificador.enviar(mensaje);
        }
    }

    @Configuration
    static class AppConfig {

        @Bean("email")
        Notificador notificadorEmail() { return new NotificadorEmail(); }

        @Bean("sms")
        Notificador notificadorSMS() { return new NotificadorSMS(); }

        // @Qualifier resuelve la ambigüedad cuando hay múltiples beans del mismo tipo
        @Bean
        ServicioAlertas servicioAlertas(@Qualifier("email") Notificador notificador) {
            return new ServicioAlertas(notificador);
        }
    }

    public static void main(String[] args) {
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {

            ServicioAlertas servicio = ctx.getBean(ServicioAlertas.class);
            servicio.alertar("Temperatura crítica detectada");

            // Cambiar "email" por "sms" en @Qualifier → mismo servicio, distinto canal
            // ServicioAlertas no cambia — eso es IoC
            Notificador sms = ctx.getBean("sms", Notificador.class);
            sms.enviar("Mensaje directo al bean por nombre");
        }
    }
}
