import java.util.TreeSet;

public class Ejercicio4 {
    public static void main(String[] args) {
        TreeSet<String> ciudades = new TreeSet<>();

        // Insertamos en orden aleatorio
        ciudades.add("Zaragoza");
        ciudades.add("Madrid");
        ciudades.add("Barcelona");
        ciudades.add("Valencia");
        ciudades.add("Bilbao");
        ciudades.add("Sevilla");

        System.out.println("TreeSet (orden alfabético automático):");
        ciudades.forEach(c -> System.out.println("  " + c));

        System.out.println("\nPrimero: " + ciudades.first());
        System.out.println("Último:  " + ciudades.last());
    }
}
