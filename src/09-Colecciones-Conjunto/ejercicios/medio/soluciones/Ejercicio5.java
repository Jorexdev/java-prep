import java.util.Iterator;
import java.util.LinkedHashSet;

public class Ejercicio5 {

    static final int LIMITE = 4;

    static void visitar(LinkedHashSet<String> historial, String url) {
        if (historial.size() >= LIMITE) {
            // Elimina el elemento más antiguo (primero en orden de inserción)
            String masAntiguo = historial.iterator().next();
            historial.remove(masAntiguo);
            System.out.println("  Historial lleno, eliminando: " + masAntiguo);
        }
        historial.add(url);
        System.out.println("  Visitada: " + url);
    }

    public static void main(String[] args) {
        LinkedHashSet<String> historial = new LinkedHashSet<>();

        visitar(historial, "https://google.com");
        visitar(historial, "https://github.com");
        visitar(historial, "https://stackoverflow.com");
        visitar(historial, "https://docs.oracle.com");
        visitar(historial, "https://maven.apache.org");  // desborda
        visitar(historial, "https://spring.io");         // desborda

        System.out.println("\nHistorial final (orden de visita): " + historial);
        System.out.println("Tamaño: " + historial.size());
    }
}
