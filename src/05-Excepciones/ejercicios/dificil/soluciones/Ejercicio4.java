public class Ejercicio4 {
    public static void main(String[] args) throws InterruptedException {
        Thread.setDefaultUncaughtExceptionHandler((hilo, ex) ->
            System.out.println("[HANDLER] Excepción en " + hilo.getName() + ": " + ex.getMessage())
        );
        Thread hilo = new Thread(() -> {
            throw new RuntimeException("Excepción no capturada");
        });
        hilo.setName("hilo-demo");
        hilo.start();
        hilo.join();
        System.out.println("Main continúa después del hilo");
    }
}
