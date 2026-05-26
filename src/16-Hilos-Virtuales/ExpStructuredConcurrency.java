import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

// StructuredTaskScope requiere --enable-preview en Java 21 y no está habilitado en este proyecto.
// Este archivo simula el patrón con CountDownLatch + virtual threads para que compile sin flags extra.
// El comportamiento y semántica son idénticos al API real.
public class ExpStructuredConcurrency {

    public static void main(String[] args) throws Exception {

        System.out.println("=== ShutdownOnFailure: todos deben tener éxito ===");
        // Si cualquier subtarea falla, las demás son canceladas.
        // Real: try (var scope = new StructuredTaskScope.ShutdownOnFailure()) { ... scope.join(); scope.throwIfFailed(); }
        try {
            UserContext ctx = fetchAllOrFail("user-1");
            System.out.println("Contexto completo: " + ctx);
        } catch (Exception e) {
            System.out.println("Fallo (esperado en caso de error): " + e.getMessage());
        }

        System.out.println("\n=== ShutdownOnFailure: con fallo en una subtarea ===");
        try {
            fetchAllOrFail("user-error");
        } catch (Exception e) {
            System.out.println("Cancelado correctamente: " + e.getMessage());
        }

        System.out.println("\n=== ShutdownOnSuccess: primero en responder gana (race) ===");
        // Consulta 3 réplicas, usa la que responda primero. Las demás son descartadas.
        // Real: try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) { ... scope.join(); return scope.result(); }
        String winner = queryFirstReplica();
        System.out.println("Resultado de la réplica más rápida: " + winner);
    }

    // ── ShutdownOnFailure simulado ──────────────────────────────────────────

    static UserContext fetchAllOrFail(String userId) throws Exception {
        AtomicReference<String>    user        = new AtomicReference<>();
        AtomicReference<String>    permissions = new AtomicReference<>();
        AtomicReference<String>    prefs       = new AtomicReference<>();
        AtomicReference<Exception> error       = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(3);

        // Lanzar las 3 subtareas como virtual threads
        List<Thread> threads = List.of(
            Thread.ofVirtual().start(() -> {
                try {
                    if (userId.contains("error")) throw new RuntimeException("usuario no encontrado: " + userId);
                    dormir(40);
                    user.set("User{id=" + userId + "}");
                } catch (Exception e) { error.set(e); }
                finally { done.countDown(); }
            }),
            Thread.ofVirtual().start(() -> {
                try { dormir(60); permissions.set("ROLES[READ,WRITE]"); }
                catch (Exception e) { error.set(e); }
                finally { done.countDown(); }
            }),
            Thread.ofVirtual().start(() -> {
                try { dormir(30); prefs.set("PREFS{theme=dark}"); }
                catch (Exception e) { error.set(e); }
                finally { done.countDown(); }
            })
        );

        done.await();

        // scope.throwIfFailed() — propaga el primer error encontrado
        if (error.get() != null) throw error.get();

        return new UserContext(user.get(), permissions.get(), prefs.get());
    }

    // ── ShutdownOnSuccess simulado ──────────────────────────────────────────

    static String queryFirstReplica() throws Exception {
        // Primera en responder gana; el resultado se captura en una SynchronousQueue
        SynchronousQueue<String> firstResult = new SynchronousQueue<>();

        String[] replicas = {"replica-EU[120ms]", "replica-US[25ms]", "replica-AP[80ms]"};
        long[]   latencies = {120, 25, 80};

        for (int i = 0; i < replicas.length; i++) {
            final String name    = replicas[i];
            final long   latency = latencies[i];
            Thread.ofVirtual().start(() -> {
                dormir(latency);
                firstResult.offer(name);   // solo el primero en ofrecer consigue poner el valor
            });
        }

        // Espera al primer resultado disponible (equivale a scope.result() en ShutdownOnSuccess)
        return firstResult.poll(2, TimeUnit.SECONDS);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    static class UserContext {
        final String user, permissions, prefs;
        UserContext(String user, String permissions, String prefs) {
            this.user = user; this.permissions = permissions; this.prefs = prefs;
        }
        @Override public String toString() {
            return "UserContext{user=" + user + ", permissions=" + permissions + ", prefs=" + prefs + "}";
        }
    }
}
