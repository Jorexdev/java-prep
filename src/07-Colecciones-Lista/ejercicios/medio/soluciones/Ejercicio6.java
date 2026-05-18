import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class Ejercicio6 {

    public static void main(String[] args) {
        // --- Collections.frequency ---
        List<String> colores = new ArrayList<>(
            List.of("rojo", "azul", "rojo", "verde", "azul", "rojo")
        );
        int frecRojo = Collections.frequency(colores, "rojo");
        int frecAzul = Collections.frequency(colores, "azul");
        System.out.println("Lista de colores: " + colores);
        System.out.println("Frecuencia de 'rojo': " + frecRojo); // 3
        System.out.println("Frecuencia de 'azul': " + frecAzul); // 2

        // --- Collections.disjoint ---
        List<String> frutas = List.of("manzana", "pera", "naranja");
        List<String> verduras = List.of("zanahoria", "espinaca", "brócoli");

        System.out.println("\nFrutas:   " + frutas);
        System.out.println("Verduras: " + verduras);
        System.out.println("¿Disjuntas (sin elementos comunes)? " +
            Collections.disjoint(frutas, verduras)); // true

        // Añadir elemento común
        List<String> frutasConComun = new ArrayList<>(frutas);
        frutasConComun.add("zanahoria");
        System.out.println("\nFrutas + zanahoria: " + frutasConComun);
        System.out.println("¿Disjuntas ahora? " +
            Collections.disjoint(frutasConComun, verduras)); // false
    }
}
