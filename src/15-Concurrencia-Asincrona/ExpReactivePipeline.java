import java.util.*;
import java.util.concurrent.*;

public class ExpReactivePipeline {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Pipeline reactivo asíncrono ===");
        System.out.println("Generador → filtro pares → cuadrado → impresor\n");

        // Cola acotada entre etapas: modela backpressure — si el consumidor es lento,
        // el productor se bloquea en lugar de acumular sin límite
        BlockingQueue<Integer> rawQueue      = new LinkedBlockingQueue<>(4);
        BlockingQueue<Integer> filteredQueue = new LinkedBlockingQueue<>(4);
        BlockingQueue<Integer> squaredQueue  = new LinkedBlockingQueue<>(4);

        int POISON = Integer.MIN_VALUE;  // señal de fin de stream

        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();

        // Etapa 1: Publisher — genera números 1..12
        Future<?> pub = exec.submit(() -> {
            for (int i = 1; i <= 12; i++) {
                put(rawQueue, i);
                System.out.println("  pub: emite " + i);
            }
            put(rawQueue, POISON);
        });

        // Etapa 2: Processor — filtra solo pares
        Future<?> filter = exec.submit(() -> {
            while (true) {
                int val = take(rawQueue);
                if (val == POISON) { put(filteredQueue, POISON); break; }
                if (val % 2 == 0) put(filteredQueue, val);   // backpressure: put bloquea si filteredQueue llena
            }
        });

        // Etapa 3: Processor — eleva al cuadrado
        Future<?> mapper = exec.submit(() -> {
            while (true) {
                int val = take(filteredQueue);
                if (val == POISON) { put(squaredQueue, POISON); break; }
                put(squaredQueue, val * val);
            }
        });

        // Etapa 4: Subscriber — consume e imprime; simula consumidor lento
        List<Integer> collected = new ArrayList<>();
        Future<?> sub = exec.submit(() -> {
            while (true) {
                int val = take(squaredQueue);
                if (val == POISON) break;
                dormir(30);   // consumidor lento → backpressure sube hasta el publisher
                collected.add(val);
                System.out.println("  sub: recibe " + val);
            }
        });

        // Esperar a que todo el pipeline termine
        pub.get(); filter.get(); mapper.get(); sub.get();

        System.out.println("\nResultado: " + collected);

        // Comparación con versión síncrona equivalente
        System.out.println("\n=== Versión síncrona (mismos pasos, mismo resultado) ===");
        List<Integer> syncResult = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            if (i % 2 == 0) syncResult.add(i * i);
        }
        System.out.println("Resultado: " + syncResult);
        System.out.println("Diferencia: el pipeline asíncrono solaparía etapas y toleraría consumidores lentos");

        exec.shutdown();
    }

    static void put(BlockingQueue<Integer> q, int val) {
        try { q.put(val); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    static int take(BlockingQueue<Integer> q) {
        try { return q.take(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return Integer.MIN_VALUE; }
    }

    static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
