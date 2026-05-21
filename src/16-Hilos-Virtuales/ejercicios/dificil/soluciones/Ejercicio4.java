import java.util.concurrent.StructuredTaskScope;

public class Ejercicio4 {

    // Nieto: tarea hoja del arbol
    static String ejecutarNieto(String nombre, boolean fallar, int sleepMs) throws Exception {
        System.out.println("    [" + nombre + "] iniciado");
        Thread.sleep(sleepMs);
        if (fallar) {
            System.out.println("    [" + nombre + "] LANZANDO EXCEPCION");
            throw new RuntimeException(nombre + " fallo");
        }
        System.out.println("    [" + nombre + "] completado");
        return nombre + "-ok";
    }

    // Hijo: lanza 2 nietos, usa ShutdownOnFailure interno
    static String ejecutarHijo(String nombre, boolean hijoBConFallo) throws Exception {
        System.out.println("  [" + nombre + "] iniciado, lanzando nietos...");
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            String nieto1 = nombre + "1";
            String nieto2 = nombre + "2";
            boolean nieto2Falla = hijoBConFallo && nombre.equals("B");

            var t1 = scope.fork(() -> ejecutarNieto(nieto1, false, 30));
            var t2 = scope.fork(() -> ejecutarNieto(nieto2, nieto2Falla, nieto2Falla ? 50 : 40));

            scope.join();
            scope.throwIfFailed(); // propaga excepcion del nieto fallido

            System.out.println("  [" + nombre + "] ambos nietos OK: " + t1.get() + ", " + t2.get());
            return nombre + "-ok";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Structured Concurrency Tree ===");
        System.out.println("Raiz lanza 3 hijos (A, B, C) en paralelo.");
        System.out.println("Cada hijo lanza 2 nietos. Nieto B2 falla.");
        System.out.println("-".repeat(50));
        System.out.println();

        try (var rootScope = new StructuredTaskScope.ShutdownOnFailure()) {

            var hijoA = rootScope.fork(() -> ejecutarHijo("A", false));
            var hijoB = rootScope.fork(() -> ejecutarHijo("B", true));  // B2 falla
            var hijoC = rootScope.fork(() -> ejecutarHijo("C", false));

            rootScope.join();

            System.out.println();
            System.out.println("Estado de hijos tras join:");
            System.out.println("  hijoA estado: " + hijoA.state());
            System.out.println("  hijoB estado: " + hijoB.state());
            System.out.println("  hijoC estado: " + hijoC.state());
            System.out.println();

            rootScope.throwIfFailed(); // propaga el fallo de hijoB

        } catch (Exception e) {
            System.out.println("=== Excepcion en raiz ===");
            System.out.println("Tipo   : " + e.getClass().getSimpleName());
            System.out.println("Mensaje: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Analisis del arbol ===");
        System.out.println("  B2 (nieto)  -> lanza excepcion");
        System.out.println("  B  (hijo)   -> scope interno cancela B1, propaga excepcion");
        System.out.println("  Raiz        -> scope raiz cancela A y C, propaga excepcion");
        System.out.println();
        System.out.println("Structured concurrency garantiza:");
        System.out.println("  - Ningun thread escapa su scope padre");
        System.out.println("  - Los fallos se propagan hacia arriba");
        System.out.println("  - El ciclo de vida de los threads es predecible");
    }
}
