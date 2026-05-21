public class Ejercicio1 {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Primer Virtual Thread ===");

        Thread virtual = Thread.ofVirtual().name("mi-virtual-thread").unstarted(() -> {
            Thread t = Thread.currentThread();
            System.out.println("[Virtual]   nombre=" + t.getName() +
                               " | isVirtual=" + t.isVirtual() +
                               " | id=" + t.threadId() +
                               " | isDaemon=" + t.isDaemon());
        });

        Thread platform = Thread.ofPlatform().name("mi-platform-thread").unstarted(() -> {
            Thread t = Thread.currentThread();
            System.out.println("[Platform]  nombre=" + t.getName() +
                               " | isVirtual=" + t.isVirtual() +
                               " | id=" + t.threadId() +
                               " | isDaemon=" + t.isDaemon());
        });

        System.out.println("Antes de start:");
        System.out.println("  Virtual  isVirtual=" + virtual.isVirtual());
        System.out.println("  Platform isVirtual=" + platform.isVirtual());
        System.out.println();

        virtual.start();
        platform.start();

        virtual.join();
        platform.join();

        System.out.println();
        System.out.println("=== Diferencias clave ===");
        System.out.println("Virtual thread:");
        System.out.println("  - Ligero: ~300 bytes de memoria inicial");
        System.out.println("  - Gestionado por JVM, montado sobre carrier threads");
        System.out.println("  - Siempre daemon=true");
        System.out.println("  - Ideal para operaciones I/O-bound");
        System.out.println("Platform thread:");
        System.out.println("  - Mapeado 1:1 con OS thread (~1MB de stack)");
        System.out.println("  - Limitado por el numero de cores disponibles en parallel");
        System.out.println("  - Mejor para CPU-bound sin I/O");
    }
}
