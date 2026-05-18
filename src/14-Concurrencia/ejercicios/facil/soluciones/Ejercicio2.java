public class Ejercicio2 {

    static class ContadorInseguro {
        int count = 0;
        void incrementar() { count++; }
    }

    static class ContadorSeguro {
        int count = 0;
        synchronized void incrementar() { count++; }
    }

    static int ejecutar(Object contador, boolean seguro) throws Exception {
        Thread[] hilos = new Thread[10];
        if (seguro) {
            ContadorSeguro c = (ContadorSeguro) contador;
            for (int i = 0; i < 10; i++)
                hilos[i] = new Thread(() -> { for (int j = 0; j < 1000; j++) c.incrementar(); });
        } else {
            ContadorInseguro c = (ContadorInseguro) contador;
            for (int i = 0; i < 10; i++)
                hilos[i] = new Thread(() -> { for (int j = 0; j < 1000; j++) c.incrementar(); });
        }
        for (Thread h : hilos) h.start();
        for (Thread h : hilos) h.join();
        return seguro ? ((ContadorSeguro)contador).count : ((ContadorInseguro)contador).count;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Inseguro:  " + ejecutar(new ContadorInseguro(), false) + " (esperado 10000)");
        System.out.println("Seguro:    " + ejecutar(new ContadorSeguro(), true)   + " (esperado 10000)");
    }
}
