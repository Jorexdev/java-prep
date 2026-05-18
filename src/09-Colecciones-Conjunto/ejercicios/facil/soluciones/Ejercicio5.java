import java.util.HashSet;
import java.util.Set;

public class Ejercicio5 {
    public static void main(String[] args) {
        Set<String> setA = new HashSet<>(Set.of("rojo", "verde", "azul", "negro"));
        Set<String> setB = new HashSet<>(Set.of("rojo", "blanco", "negro"));

        System.out.println("Conjunto A: " + setA);
        System.out.println("Conjunto B: " + setB);

        // Diferencia A - B: elementos en A que no están en B
        Set<String> diferencia = new HashSet<>(setA);
        diferencia.removeAll(setB);

        System.out.println("Diferencia A - B: " + diferencia);
    }
}
