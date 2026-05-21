public class Ejercicio5 {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Thread States ===\n");

        Thread t = Thread.ofPlatform().name("estado-demo").unstarted(() -> {
            try {
                // Durante la ejecucion del thread, su estado es RUNNABLE
                Thread.sleep(200); // -> TIMED_WAITING
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Al salir del sleep vuelve a RUNNABLE hasta terminar
        });

        // 1. NEW
        System.out.println("1. Antes de start():");
        System.out.println("   Estado: " + t.getState() + " (esperado: NEW)\n");

        t.start();

        // 2. RUNNABLE (inmediatamente despues de start, antes de que entre en sleep)
        // Dar un tiny window para que el thread se lance y entre en ejecucion
        Thread.sleep(5);
        System.out.println("2. Justo despues de start() + 5ms:");
        System.out.println("   Estado: " + t.getState() + " (esperado: TIMED_WAITING - durmiendo 200ms)\n");

        // 3. TIMED_WAITING - durante el sleep del thread
        // Ya estamos en el estado de sleep porque sleepea 200ms y solo esperamos 5ms
        Thread.sleep(50);
        System.out.println("3. Mientras duerme (50ms despues de start):");
        System.out.println("   Estado: " + t.getState() + " (esperado: TIMED_WAITING)\n");

        // 4. Esperamos a que termine
        t.join();
        System.out.println("4. Despues de join():");
        System.out.println("   Estado: " + t.getState() + " (esperado: TERMINATED)\n");

        System.out.println("=== Ciclo de vida completo ===");
        System.out.println("NEW -> (start) -> RUNNABLE -> (sleep) -> TIMED_WAITING -> RUNNABLE -> TERMINATED");
        System.out.println();
        System.out.println("Otros estados posibles:");
        System.out.println("  BLOCKED       : esperando un monitor lock (synchronized)");
        System.out.println("  WAITING       : en Object.wait(), Thread.join() sin timeout");
        System.out.println("  TIMED_WAITING : en sleep(n), wait(n), join(n)");
    }
}
