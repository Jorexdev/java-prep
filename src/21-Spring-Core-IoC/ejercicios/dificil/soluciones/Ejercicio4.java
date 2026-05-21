import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Ejercicio4 {

    // --- Event hierarchy ---

    static abstract class ApplicationEvent {
        private final long timestamp;
        private final Object source;

        ApplicationEvent(Object source) {
            this.timestamp = Instant.now().toEpochMilli();
            this.source = source;
        }

        long getTimestamp() { return timestamp; }
        Object getSource() { return source; }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[source=" + source + ", ts=" + timestamp + "]";
        }
    }

    // Evento built-in: el contexto se ha inicializado/refrescado
    static class ContextRefreshedEvent extends ApplicationEvent {
        ContextRefreshedEvent(Object source) {
            super(source);
        }
    }

    // Evento de dominio personalizado
    static class LogEvent extends ApplicationEvent {
        private final String nivel;
        private final String mensaje;

        LogEvent(Object source, String nivel, String mensaje) {
            super(source);
            this.nivel = nivel;
            this.mensaje = mensaje;
        }

        String getNivel() { return nivel; }
        String getMensaje() { return mensaje; }
    }

    // --- Listener interface ---

    interface ApplicationListener<T extends ApplicationEvent> {
        void onEvent(T event);
    }

    // Listener 1: reacciona a ContextRefreshedEvent
    static class ContextListener implements ApplicationListener<ContextRefreshedEvent> {
        @Override
        public void onEvent(ContextRefreshedEvent event) {
            System.out.println("[ContextListener] Contexto refrescado desde: " + event.getSource());
        }
    }

    // Listener 2: reacciona a LogEvent
    static class AuditListener implements ApplicationListener<LogEvent> {
        private int contador = 0;

        @Override
        public void onEvent(LogEvent event) {
            contador++;
            System.out.printf("[AuditListener] #%d [%s] %s%n",
                contador, event.getNivel(), event.getMensaje());
        }
    }

    // Listener 3: reacciona a TODOS los eventos (usa ApplicationEvent base)
    static class MetricsListener implements ApplicationListener<ApplicationEvent> {
        private int total = 0;

        @Override
        public void onEvent(ApplicationEvent event) {
            total++;
            System.out.println("[MetricsListener] Evento recibido #" + total + ": "
                + event.getClass().getSimpleName());
        }
    }

    // --- Event Publisher ---

    static class ApplicationEventPublisher {
        // Lista raw porque Java no puede tener List<ApplicationListener<T>> sin unchecked
        private final List<ApplicationListener> listeners = new ArrayList<>();

        <T extends ApplicationEvent> void register(ApplicationListener<T> listener) {
            listeners.add(listener);
            System.out.println("Listener registrado: " + listener.getClass().getSimpleName());
        }

        @SuppressWarnings("unchecked")
        <T extends ApplicationEvent> void publish(T event) {
            System.out.println("\n>>> Publicando: " + event);
            for (ApplicationListener listener : listeners) {
                // Solo notificar si el listener acepta este tipo de evento
                // Spring hace esto con reflection analizando el tipo genérico
                try {
                    listener.onEvent(event);
                } catch (ClassCastException ignored) {
                    // El listener no acepta este tipo — lo ignoramos silenciosamente
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Event System ===\n");

        ApplicationEventPublisher publisher = new ApplicationEventPublisher();

        ContextListener contextListener = new ContextListener();
        AuditListener auditListener = new AuditListener();
        MetricsListener metricsListener = new MetricsListener();

        publisher.register(contextListener);
        publisher.register(auditListener);
        publisher.register(metricsListener);

        System.out.println();

        // Publicar ContextRefreshedEvent
        publisher.publish(new ContextRefreshedEvent("ApplicationContext"));

        // Publicar eventos de log
        publisher.publish(new LogEvent("App", "INFO", "Usuario admin se conectó"));
        publisher.publish(new LogEvent("App", "WARN", "Reintentos de conexión a DB"));
        publisher.publish(new LogEvent("App", "ERROR", "Timeout al llamar servicio externo"));

        System.out.println("\n=== Resumen ===");
        System.out.println("ContextListener: solo recibió ContextRefreshedEvent");
        System.out.println("AuditListener: solo recibió LogEvents (" + auditListener.contador + ")");
        System.out.println("MetricsListener: recibió todos (" + metricsListener.total + ")");
    }
}
