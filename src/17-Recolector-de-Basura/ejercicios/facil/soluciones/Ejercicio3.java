import java.lang.ref.SoftReference;

public class Ejercicio3 {

    static String humanReadable(long bytes) {
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== SoftReference Demo ===");
        System.out.println();

        byte[] array = new byte[1024 * 1024]; // 1 MB
        SoftReference<byte[]> softRef = new SoftReference<>(array);

        // Mantener acceso múltiple sin presión de memoria
        System.out.println("Accediendo sin presión de memoria:");
        for (int i = 0; i < 5; i++) {
            byte[] data = softRef.get();
            if (data != null) {
                System.out.println("  Acceso " + (i + 1) + ": disponible, tamaño=" + data.length + " bytes");
            } else {
                System.out.println("  Acceso " + (i + 1) + ": null (recolectado)");
            }
        }

        System.out.println();
        System.out.println("Memoria libre antes de gc: " +
            humanReadable(Runtime.getRuntime().freeMemory()));

        // Eliminar la referencia fuerte y sugerir GC
        array = null;
        System.gc();
        Thread.sleep(200);

        System.out.println("Memoria libre después de gc: " +
            humanReadable(Runtime.getRuntime().freeMemory()));

        byte[] afterGc = softRef.get();
        System.out.println();
        if (afterGc != null) {
            System.out.println("SoftRef después de gc: DISPONIBLE (" + afterGc.length + " bytes)");
            System.out.println("  -> La JVM no tenía presión de memoria, no la liberó.");
        } else {
            System.out.println("SoftRef después de gc: null -> liberada bajo presión de memoria.");
        }

        System.out.println();
        System.out.println("Conclusión: SoftReference se libera SOLO cuando la JVM necesita memoria.");
        System.out.println("Ideal para caches en memoria: retiene datos cuando hay espacio,");
        System.out.println("los libera automáticamente antes de lanzar OutOfMemoryError.");
    }
}
