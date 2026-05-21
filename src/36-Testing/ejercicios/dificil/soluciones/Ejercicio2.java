import java.util.*;

public class Ejercicio2 {

    interface PagoService {
        boolean cobrar(String userId, double importe);
        void reembolsar(String userId, double importe);
        double getSaldo(String userId);
    }

    record Pedido(String id, String userId, double importe) {}

    static class ProcesamientoPedido {
        private final PagoService pagos;
        ProcesamientoPedido(PagoService p) { this.pagos = p; }

        boolean procesar(Pedido pedido) {
            if (pagos.getSaldo(pedido.userId()) < pedido.importe()) return false;
            return pagos.cobrar(pedido.userId(), pedido.importe());
        }

        void cancelar(Pedido pedido) { pagos.reembolsar(pedido.userId(), pedido.importe()); }
    }

    // 1. DUMMY — se pasa pero nunca se invoca en ese flujo
    static class DummyPagoService implements PagoService {
        @Override public boolean cobrar(String u, double i) { throw new AssertionError("Dummy no debe invocarse"); }
        @Override public void reembolsar(String u, double i){ throw new AssertionError("Dummy no debe invocarse"); }
        @Override public double getSaldo(String u)          { throw new AssertionError("Dummy no debe invocarse"); }
    }

    // 2. STUB — respuestas cableadas, sin lógica
    static class StubPagoService implements PagoService {
        @Override public boolean cobrar(String u, double i)  { return true; }
        @Override public void reembolsar(String u, double i) {}
        @Override public double getSaldo(String u)           { return 999.0; }
    }

    // 3. FAKE — implementación funcional simplificada
    static class FakePagoService implements PagoService {
        private final Map<String, Double> saldos = new HashMap<>();
        FakePagoService(Map<String, Double> ini) { saldos.putAll(ini); }
        @Override public boolean cobrar(String u, double i) {
            double s = saldos.getOrDefault(u, 0.0);
            if (s < i) return false;
            saldos.put(u, s - i); return true;
        }
        @Override public void reembolsar(String u, double i) { saldos.merge(u, i, Double::sum); }
        @Override public double getSaldo(String u) { return saldos.getOrDefault(u, 0.0); }
    }

    // 4. MOCK — verifica qué métodos se llamaron y cuántas veces
    static class MockPagoService implements PagoService {
        final List<String> llamadas = new ArrayList<>();
        @Override public boolean cobrar(String u, double i)  { llamadas.add("cobrar(" + u + "," + i + ")"); return true; }
        @Override public void reembolsar(String u, double i) { llamadas.add("reembolsar(" + u + "," + i + ")"); }
        @Override public double getSaldo(String u)           { llamadas.add("getSaldo(" + u + ")"); return 500.0; }

        void verify(String llamada) {
            boolean ok = llamadas.contains(llamada);
            System.out.println((ok ? "PASS" : "FAIL") + " verify: " + llamada);
        }
        void verifyNeverCalled(String metodo) {
            boolean called = llamadas.stream().anyMatch(l -> l.startsWith(metodo));
            System.out.println((!called ? "PASS" : "FAIL") + " neverCalled: " + metodo);
        }
    }

    // 5. SPY — envuelve implementación real, registra llamadas
    static class SpyPagoService implements PagoService {
        private final PagoService real;
        final List<String> log = new ArrayList<>();
        SpyPagoService(PagoService real) { this.real = real; }
        @Override public boolean cobrar(String u, double i)  { log.add("cobrar");    return real.cobrar(u, i); }
        @Override public void reembolsar(String u, double i) { log.add("reembolsar"); real.reembolsar(u, i); }
        @Override public double getSaldo(String u)           { log.add("getSaldo");   return real.getSaldo(u); }
    }

    public static void main(String[] args) {
        System.out.println("=== 1. DUMMY — pasado pero no invocado ===");
        System.out.println("DummyPagoService creado sin invocación → OK (si se invocase lanzaría AssertionError)");

        System.out.println("\n=== 2. STUB — respuestas fijas ===");
        var stub = new ProcesamientoPedido(new StubPagoService());
        System.out.println("Procesar: " + stub.procesar(new Pedido("P1", "u1", 50.0)));

        System.out.println("\n=== 3. FAKE — lógica real simplificada ===");
        var fake = new FakePagoService(new HashMap<>(Map.of("user1", 100.0)));
        var proc = new ProcesamientoPedido(fake);
        proc.procesar(new Pedido("P2", "user1", 30.0));
        System.out.println("Saldo tras cobrar 30: " + fake.getSaldo("user1"));
        System.out.println("Cobro imposible (200): " + proc.procesar(new Pedido("P3", "user1", 200.0)));

        System.out.println("\n=== 4. MOCK — verificar comportamiento ===");
        var mock = new MockPagoService();
        new ProcesamientoPedido(mock).procesar(new Pedido("P4", "user2", 75.0));
        mock.verify("getSaldo(user2)");
        mock.verify("cobrar(user2,75.0)");
        mock.verifyNeverCalled("reembolsar");

        System.out.println("\n=== 5. SPY — clase real observada ===");
        var spy = new SpyPagoService(new FakePagoService(new HashMap<>(Map.of("user3", 200.0))));
        var proc2 = new ProcesamientoPedido(spy);
        proc2.procesar(new Pedido("P5", "user3", 50.0));
        proc2.cancelar(new Pedido("P5", "user3", 50.0));
        System.out.println("Llamadas registradas: " + spy.log);
    }
}
