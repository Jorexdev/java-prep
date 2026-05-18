import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ejercicio3 {

    static class Producto implements Comparable<Producto> {
        private final String nombre;
        private final double precio;

        Producto(String nombre, double precio) {
            this.nombre = nombre;
            this.precio = precio;
        }

        @Override public int compareTo(Producto o) { return Double.compare(this.precio, o.precio); }
        @Override public String toString() { return nombre + "(" + precio + "€)"; }
    }

    public static void main(String[] args) {
        List<Producto> productos = new ArrayList<>(List.of(
            new Producto("Teclado", 49.99),
            new Producto("Monitor", 299.00),
            new Producto("Ratón",   19.99),
            new Producto("Webcam",  89.99)
        ));

        Collections.sort(productos);
        System.out.println("Ordenados: " + productos);
        System.out.println("Más barato: " + Collections.min(productos));
        System.out.println("Más caro:   " + Collections.max(productos));
    }
}
