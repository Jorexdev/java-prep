import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

// Pipeline de transformación async: Leer → Parsear → Enriquecer → Guardar
public class Ejercicio5 {

    // Modelo de evento
    record Evento(int id, String tipo, String payload) {}
    record EventoEnriquecido(int id, String tipo, String payload, long timestamp) {}

    // Contadores de cada etapa
    static final AtomicInteger leidos     = new AtomicInteger(0);
    static final AtomicInteger parseados  = new AtomicInteger(0);
    static final AtomicInteger enriquecidos = new AtomicInteger(0);
    static final AtomicInteger guardados  = new AtomicInteger(0);

    // Etapa 1: generar eventos (algunos con payload inválido)
    static CompletableFuture<List<Evento>> leer() {
        return CompletableFuture.supplyAsync(() -> {
            List<Evento> eventos = List.of(
                new Evento(1, "CLICK",   "btn-submit"),
                new Evento(2, "VIEW",    ""),           // payload vacío → inválido
                new Evento(3, "PURCHASE","item-123"),
                new Evento(4, "LOGIN",   null),         // payload nulo → inválido
                new Evento(5, "CLICK",   "btn-cancel"),
                new Evento(6, "ERROR",   "timeout-502")
            );
            leidos.set(eventos.size());
            System.out.println("[Leer] " + eventos.size() + " eventos generados");
            return eventos;
        });
    }

    // Etapa 2: validar y filtrar eventos con payload inválido
    static CompletableFuture<List<Evento>> parsear(List<Evento> eventos) {
        return CompletableFuture.supplyAsync(() -> {
            List<Evento> validos = new ArrayList<>();
            for (Evento e : eventos) {
                if (e.payload() != null && !e.payload().isBlank()) {
                    validos.add(e);
                    System.out.println("[Parsear] OK: id=" + e.id() + " tipo=" + e.tipo());
                } else {
                    System.out.println("[Parsear] DESCARTADO: id=" + e.id() + " (payload inválido)");
                }
            }
            parseados.set(validos.size());
            return validos;
        });
    }

    // Etapa 3: enriquecer con timestamp
    static CompletableFuture<List<EventoEnriquecido>> enriquecer(List<Evento> eventos) {
        return CompletableFuture.supplyAsync(() -> {
            List<EventoEnriquecido> enriquecidos = new ArrayList<>();
            for (Evento e : eventos) {
                long ts = System.currentTimeMillis();
                EventoEnriquecido ee = new EventoEnriquecido(e.id(), e.tipo(), e.payload(), ts);
                enriquecidos.add(ee);
                System.out.println("[Enriquecer] id=" + ee.id() + " timestamp=" + ee.timestamp());
            }
            Ejercicio5.enriquecidos.set(enriquecidos.size());
            return enriquecidos;
        });
    }

    // Etapa 4: "guardar" (simular persistencia)
    static CompletableFuture<Void> guardar(List<EventoEnriquecido> eventos) {
        return CompletableFuture.runAsync(() -> {
            for (EventoEnriquecido e : eventos) {
                System.out.println("[Guardar] Guardado: {id=" + e.id() + ", tipo=" + e.tipo()
                    + ", payload=" + e.payload() + ", ts=" + e.timestamp() + "}");
                guardados.incrementAndGet();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Pipeline async: Leer → Parsear → Enriquecer → Guardar ===\n");

        long start = System.currentTimeMillis();

        // Encadenar las 4 etapas con CompletableFuture.thenCompose
        CompletableFuture<Void> pipeline = leer()
            .thenCompose(Ejercicio5::parsear)
            .thenCompose(Ejercicio5::enriquecer)
            .thenCompose(Ejercicio5::guardar);

        pipeline.join(); // esperar a que todo el pipeline complete

        long elapsed = System.currentTimeMillis() - start;

        System.out.println();
        System.out.println("=== Resumen del pipeline ===");
        System.out.println("Etapa 1 - Leídos:      " + leidos.get());
        System.out.println("Etapa 2 - Parseados:   " + parseados.get() + " (descartados=" + (leidos.get() - parseados.get()) + ")");
        System.out.println("Etapa 3 - Enriquecidos: " + enriquecidos.get());
        System.out.println("Etapa 4 - Guardados:   " + guardados.get());
        System.out.printf("Tiempo total: %dms%n", elapsed);

        System.out.println();
        System.out.println("=== En Project Reactor ===");
        System.out.println("Flux.fromIterable(leer())");
        System.out.println("    .filter(e -> e.payload() != null && !e.payload().isBlank())");
        System.out.println("    .map(e -> enriquecer(e))");
        System.out.println("    .flatMap(e -> guardar(e))");
        System.out.println("    .subscribe(...);");
    }
}
