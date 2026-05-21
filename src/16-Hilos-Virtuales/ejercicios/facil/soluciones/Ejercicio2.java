import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class Ejercicio2 {
    public static void main(String[] args) throws InterruptedException {
        // Factory con prefijo "vt-" y contador desde 0
        ThreadFactory factory = Thread.ofVirtual().name("vt-", 0).factory();

        System.out.println("=== Virtual Thread Factory ===");
        System.out.println("Creando 10 threads con factory...\n");

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Thread t = factory.newThread(() -> {
                Thread current = Thread.currentThread();
                System.out.printf("  nombre=%-12s | isVirtual=%-5b | id=%d%n",
                    current.getName(), current.isVirtual(), current.threadId());
            });
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println();
        System.out.println("Total threads creados: " + threads.size());
        System.out.println("Todos son virtuales: " + threads.stream().allMatch(Thread::isVirtual));
        System.out.println();
        System.out.println("ThreadFactory es util para integrar virtual threads con");
        System.out.println("frameworks que aceptan ThreadFactory (ej. Executors, ForkJoinPool).");
    }
}
