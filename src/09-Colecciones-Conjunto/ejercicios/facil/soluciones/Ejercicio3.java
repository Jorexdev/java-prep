import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Ejercicio3 {
    public static void main(String[] args) {
        List<String> elementos = List.of("banana", "apple", "cherry", "date", "elderberry");

        Set<String> hashSet = new HashSet<>(elementos);
        Set<String> linkedHashSet = new LinkedHashSet<>(elementos);

        System.out.println("Orden de inserción: " + elementos);
        System.out.println("HashSet (sin orden): " + hashSet);
        System.out.println("LinkedHashSet (orden preservado): " + linkedHashSet);
    }
}
