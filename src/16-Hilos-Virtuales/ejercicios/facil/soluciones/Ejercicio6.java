import java.util.ArrayList;
import java.util.List;

public class Ejercicio6 {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Nombres y Metadatos de Virtual Threads ===\n");

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            final int id = i;
            Thread t = Thread.ofVirtual().name("worker-" + id).unstarted(() -> {
                Thread current = Thread.currentThread();
                System.out.printf("  nombre=%-12s | isDaemon=%-5b | isVirtual=%-5b | id=%d%n",
                    current.getName(),
                    current.isDaemon(),
                    current.isVirtual(),
                    current.threadId());
            });
            threads.add(t);
        }

        System.out.println("Iniciando 5 virtual threads...");
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println();
        System.out.println("=== Verificacion ===");
        long virtuales = threads.stream().filter(Thread::isVirtual).count();
        long daemons = threads.stream().filter(Thread::isDaemon).count();
        System.out.println("Threads virtuales  : " + virtuales + "/5 " + (virtuales == 5 ? "[OK]" : "[FALLO]"));
        System.out.println("Threads daemon     : " + daemons + "/5 " + (daemons == 5 ? "[OK - virtual => siempre daemon]" : "[FALLO]"));
        System.out.println();
        System.out.println("Nota: los virtual threads son SIEMPRE daemon.");
        System.out.println("Si el main thread termina, los virtual threads tambien terminan.");
        System.out.println("Usa join() para esperar a que completen.");
    }
}
