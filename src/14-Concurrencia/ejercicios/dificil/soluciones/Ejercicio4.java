public class Ejercicio4 {
    static final ThreadLocal<String> requestId = new ThreadLocal<>();

    static void procesarRequest(String id) {
        requestId.set(id);
        try {
            System.out.println(Thread.currentThread().getName() + " requestId=" + requestId.get());
            simularServicio();
            System.out.println(Thread.currentThread().getName() + " fin requestId=" + requestId.get());
        } finally {
            requestId.remove();
        }
    }

    static void simularServicio() {
        System.out.println("  [servicio] requestId=" + requestId.get());
    }

    public static void main(String[] args) throws Exception {
        Thread[] hilos = new Thread[4];
        for (int i = 0; i < 4; i++) {
            final String id = "REQ-" + (1000 + i);
            hilos[i] = new Thread(() -> procesarRequest(id), "Hilo-" + i);
        }
        for (Thread h : hilos) h.start();
        for (Thread h : hilos) h.join();
    }
}
