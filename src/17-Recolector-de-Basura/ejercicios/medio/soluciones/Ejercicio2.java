import java.util.WeakHashMap;

public class Ejercicio2 {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== WeakHashMap Demo ===");
        System.out.println();

        WeakHashMap<String, String> map = new WeakHashMap<>();

        // IMPORTANTE: new String(...) para evitar el string pool (interning)
        // Las strings internadas nunca son recolectadas mientras la clase esté cargada
        String k1 = new String("clave1");
        String k2 = new String("clave2");
        String k3 = new String("clave3");
        String k4 = new String("clave4");
        String k5 = new String("clave5");

        map.put(k1, "valor1");
        map.put(k2, "valor2");
        map.put(k3, "valor3");
        map.put(k4, "valor4");
        map.put(k5, "valor5");

        System.out.println("Mapa inicial (" + map.size() + " entradas):");
        map.forEach((k, v) -> System.out.println("  " + k + " -> " + v));

        System.out.println();
        System.out.println("Eliminando referencias fuertes a k1, k3, k5...");
        k1 = null;
        k3 = null;
        k5 = null;

        System.gc();
        Thread.sleep(300);

        System.out.println("Mapa después de gc (" + map.size() + " entradas):");
        if (map.isEmpty()) {
            System.out.println("  (vacío)");
        } else {
            map.forEach((k, v) -> System.out.println("  " + k + " -> " + v));
        }

        System.out.println();
        System.out.println("=== Análisis ===");
        System.out.println("WeakHashMap retiene entradas solo mientras la CLAVE tiene referencias fuertes.");
        System.out.println("Cuando la clave se recolecta, la entrada desaparece automáticamente.");
        System.out.println("Útil para: metadatos asociados a objetos sin retenerlos artificialmente.");
        System.out.println();
        System.out.println("NOTA: usa new String() para evitar el string pool — las literals");
        System.out.println("como \"abc\" son internadas y NUNCA se recolectan en este demo.");

        // Mantener referencias a k2 y k4 para que no sean recolectadas
        System.out.println();
        System.out.println("k2 sigue siendo: " + k2);
        System.out.println("k4 sigue siendo: " + k4);
    }
}
