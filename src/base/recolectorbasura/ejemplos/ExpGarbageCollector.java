package base.recolectorbasura.ejemplos;

public class ExpGarbageCollector {

    static class Dummy {
        private int[] data = new int[100000]; // ocupa memoria
    }

    public static void main(String[] args) throws InterruptedException {
        Runtime rt = Runtime.getRuntime();

        System.out.println("Memoria total: " + rt.totalMemory());
        System.out.println("Memoria libre inicial: " + rt.freeMemory());

        // Crear objetos que quedarán sin referencia
        for (int i = 0; i < 1000; i++) {
            new Dummy(); // no guardamos referencia → se vuelven candidatos a GC
        }

        System.out.println("Memoria libre tras crear objetos: " + rt.freeMemory());

        // Sugerir al GC que limpie
        System.gc();

        Thread.sleep(1000); // dar tiempo al GC
        System.out.println("Memoria libre tras GC: " + rt.freeMemory());
    }
}
