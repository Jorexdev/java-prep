import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Ejercicio3 {

    static class Producto {
        private final String nombre;
        private final double precio;

        Producto(String nombre, double precio) {
            this.nombre = nombre;
            this.precio = precio;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Producto)) return false;
            return Objects.equals(nombre, ((Producto) o).nombre);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nombre); // solo por nombre
        }

        @Override
        public String toString() {
            return nombre + "(" + precio + "€)";
        }
    }

    public static void main(String[] args) {
        Set<Producto> catalogo = new HashSet<>();

        Producto p1 = new Producto("Laptop", 999.0);
        Producto p2 = new Producto("Laptop", 1200.0); // mismo nombre, precio diferente

        catalogo.add(p1);
        catalogo.add(p2); // no se añade: equals/hashCode dicen que ya existe

        System.out.println("Elementos en el Set: " + catalogo.size()); // esperado: 1
        System.out.println("Contenido: " + catalogo);
        System.out.println("¿Contiene Laptop 1200€? " + catalogo.contains(new Producto("Laptop", 1200.0)));
    }
}
