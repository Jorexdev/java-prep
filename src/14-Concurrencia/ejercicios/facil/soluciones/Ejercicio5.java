public class Ejercicio5 {
    public static void main(String[] args) throws Exception {
        Thread[] hilos = {
            new Thread(() -> { try { Thread.sleep(200); System.out.println("Hilo-1 completado (200ms)"); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }),
            new Thread(() -> { try { Thread.sleep(100); System.out.println("Hilo-2 completado (100ms)"); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }),
            new Thread(() -> { try { Thread.sleep(300); System.out.println("Hilo-3 completado (300ms)"); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } })
        };
        for (Thread h : hilos) h.start();
        for (Thread h : hilos) h.join();
        System.out.println("Todos completados");
    }
}
