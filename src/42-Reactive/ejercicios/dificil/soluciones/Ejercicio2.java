import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

// Simulación de Spring WebFlux request handling: non-blocking, async, con error handling
public class Ejercicio2 {

    // ======================= MODELOS =======================
    record Usuario(int id, String nombre, String email) {}

    record UsuarioEnriquecido(int id, String nombre, String email, String role, long lastSeen) {
        @Override
        public String toString() {
            return "{id=" + id + ", nombre=" + nombre + ", email=" + email
                + ", role=" + role + ", lastSeen=" + lastSeen + "}";
        }
    }

    // Respuesta HTTP simulada
    sealed interface Response {
        record Ok(UsuarioEnriquecido body) implements Response {}
        record NotFound(String message) implements Response {}
        record ServerError(String message) implements Response {}
    }

    // ======================= "BASE DE DATOS" ASYNC =======================
    // Simula latencia de DB de ~50ms
    private static final Map<Integer, Usuario> DB = Map.of(
        1, new Usuario(1, "Ana Garcia",   "ana@ejemplo.com"),
        2, new Usuario(2, "Bob Martinez", "bob@ejemplo.com"),
        3, new Usuario(3, "Carlos Lopez", "carlos@ejemplo.com")
        // id=4 y id=5 no existen → 404
        // id=99 → simular error de DB
    );

    static CompletableFuture<Optional<Usuario>> buscarEnDB(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            if (id == 99) {
                throw new RuntimeException("DB Error: connection timeout para id=" + id);
            }

            Optional<Usuario> usuario = Optional.ofNullable(DB.get(id));
            if (usuario.isPresent()) {
                System.out.println("  [DB] id=" + id + " → encontrado: " + usuario.get().nombre());
            } else {
                System.out.println("  [DB] id=" + id + " → no encontrado (404)");
            }
            return usuario;
        });
    }

    // ======================= HANDLER (no-blocking) =======================
    static CompletableFuture<Response> handleRequest(int id) {
        System.out.println("[Request] GET /usuarios/" + id + " | hilo=" + Thread.currentThread().getName());

        return buscarEnDB(id)
            .thenApply(optUsuario -> {
                // Etapa de transformación: enriquecer si existe
                if (optUsuario.isEmpty()) {
                    return (Response) new Response.NotFound("Usuario id=" + id + " no encontrado");
                }
                Usuario u = optUsuario.get();
                // Determinar role según ID (simulación)
                String role = (u.id() == 1) ? "ADMIN" : "USER";
                UsuarioEnriquecido enriquecido = new UsuarioEnriquecido(
                    u.id(), u.nombre(), u.email(), role, System.currentTimeMillis()
                );
                return (Response) new Response.Ok(enriquecido);
            })
            .exceptionally(e -> {
                // Manejo de error de DB → 500
                System.out.println("  [Handler] Error de DB: " + e.getMessage());
                return new Response.ServerError("Error interno: " + e.getMessage());
            });
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Simulación WebFlux: 5 requests concurrentes ===\n");
        System.out.println("IDs a consultar: [1, 2, 3, 4, 99]");
        System.out.println("  id=1,2,3 → OK");
        System.out.println("  id=4     → 404 Not Found");
        System.out.println("  id=99    → 500 Server Error\n");

        List<Integer> ids = List.of(1, 2, 3, 4, 99);

        long start = System.currentTimeMillis();

        // Lanzar todas las requests en paralelo (non-blocking)
        List<CompletableFuture<Response>> futures = ids.stream()
            .map(Ejercicio2::handleRequest)
            .toList();

        // Esperar todas las respuestas
        CompletableFuture<Void> allDone = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        allDone.join();

        long elapsed = System.currentTimeMillis() - start;

        System.out.println();
        System.out.println("=== Respuestas ===");
        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            Response response = futures.get(i).join();
            String status = switch (response) {
                case Response.Ok ok         -> "200 OK    → " + ok.body();
                case Response.NotFound nf   -> "404 NOT FOUND → " + nf.message();
                case Response.ServerError se -> "500 ERROR → " + se.message();
            };
            System.out.println("  id=" + id + ": " + status);
        }

        System.out.println();
        System.out.printf("Tiempo total: %dms (5 requests x 50ms latencia DB = esperado ~50ms)%n", elapsed);
        System.out.println("Non-blocking: ningún hilo quedó bloqueado esperando la DB.");
        System.out.println("Todos los requests corrieron concurrentemente con CompletableFuture.");
    }
}
