import java.util.*;

public class Ejercicio6 {

    interface Observer {
        void onEvento(String evento, Object datos);
    }

    static class EventBus {
        private final Map<String, List<Observer>> suscriptores = new HashMap<>();

        void suscribir(String evento, Observer obs) {
            suscriptores.computeIfAbsent(evento, k -> new ArrayList<>()).add(obs);
        }

        void publicar(String evento, Object datos) {
            List<Observer> obs = suscriptores.getOrDefault(evento, List.of());
            for (Observer o : obs) o.onEvento(evento, datos);
        }
    }

    public static void main(String[] args) {
        EventBus bus = new EventBus();

        bus.suscribir("pago", (e, d) -> System.out.println("EmailService: pago recibido → " + d));
        bus.suscribir("pago", (e, d) -> System.out.println("AuditoríaService: registrando pago " + d));
        bus.suscribir("pago", (e, d) -> System.out.println("StockService: actualizando stock tras pago " + d));
        bus.suscribir("login", (e, d) -> System.out.println("SecurityService: login de " + d));

        System.out.println("--- Evento: pago ---");
        bus.publicar("pago", "#ORD-001");
        System.out.println("--- Evento: login ---");
        bus.publicar("login", "jorge@example.com");
    }
}
