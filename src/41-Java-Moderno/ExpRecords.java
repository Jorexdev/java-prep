import java.util.List;
import java.util.Objects;

// Records (Java 16 estable) — clases de datos inmutables con componentes declarados en la cabecera.
// El compilador genera: constructor canónico, accessors, equals, hashCode, toString.
public class ExpRecords {

    // Record básico: los componentes son la API pública
    record Punto(double x, double y) {

        // Compact constructor: valida sin repetir los parámetros
        Punto {
            if (Double.isNaN(x) || Double.isNaN(y)) throw new IllegalArgumentException("NaN no permitido");
        }

        // Campos estáticos: permitidos
        static final Punto ORIGEN = new Punto(0, 0);

        // Métodos de instancia custom
        double distanciaAlOrigen() {
            return Math.sqrt(x * x + y * y);
        }

        double distanciaA(Punto otro) {
            double dx = this.x - otro.x;
            double dy = this.y - otro.y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        Punto trasladar(double dx, double dy) {
            return new Punto(x + dx, y + dy); // immutable: devuelve nuevo record
        }
    }

    // Record genérico: los records pueden tener type parameters
    record Par<A, B>(A primero, B segundo) {
        // Override de accessor permitido (mismo tipo de retorno)
        @Override
        public A primero() {
            System.out.println("  [accessor] primero() llamado");
            return primero;
        }

        Par<B, A> invertir() {
            return new Par<>(segundo, primero);
        }
    }

    // Record con validación compleja en compact constructor
    record Rango(int min, int max) {
        Rango {
            if (min > max) throw new IllegalArgumentException("min=" + min + " > max=" + max);
        }

        boolean contiene(int valor) {
            return valor >= min && valor <= max;
        }

        int longitud() {
            return max - min;
        }
    }

    // Interfaz implementada por un record
    interface Describible {
        String describir();
    }

    // Record implementando una interfaz (los records pueden implementar interfaces)
    record Producto(String nombre, double precio, int stock) implements Describible {
        // Compact constructor: normalizar y validar
        Producto {
            Objects.requireNonNull(nombre, "nombre no puede ser null");
            nombre = nombre.trim(); // se puede reasignar el componente en compact constructor
            if (precio < 0) throw new IllegalArgumentException("precio negativo");
            if (stock < 0) throw new IllegalArgumentException("stock negativo");
        }

        @Override
        public String describir() {
            return String.format("%s (%.2f€, stock: %d)", nombre, precio, stock);
        }

        boolean disponible() {
            return stock > 0;
        }

        Producto conDescuento(double porcentaje) {
            return new Producto(nombre, precio * (1 - porcentaje / 100), stock);
        }
    }

    public static void main(String[] args) {

        System.out.println("=== RECORDS ===\n");

        // 1. Record básico + equals/hashCode generados
        System.out.println("--- Punto básico ---");
        Punto p1 = new Punto(3, 4);
        Punto p2 = new Punto(3, 4);
        Punto p3 = new Punto(0, 5);

        System.out.println("p1 = " + p1);                          // toString generado
        System.out.println("p1.x() = " + p1.x());                  // accessor (sin "get")
        System.out.println("p1 equals p2: " + p1.equals(p2));      // equals estructural
        System.out.println("p1 == p2: " + (p1 == p2));             // identidad: false
        System.out.println("distancia al origen: " + p1.distanciaAlOrigen()); // 5.0
        System.out.println("distancia p1->p3: " + p1.distanciaA(p3));
        System.out.println("ORIGEN = " + Punto.ORIGEN);
        System.out.println("p1 trasladado: " + p1.trasladar(1, 1)); // nuevo record

        // 2. Compact constructor validando
        System.out.println("\n--- Compact constructor ---");
        try {
            new Punto(Double.NaN, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("Validacion correcta: " + e.getMessage());
        }

        // 3. Record genérico
        System.out.println("\n--- Record generico Par<A,B> ---");
        Par<String, Integer> par = new Par<>("Java", 21);
        System.out.println("par = " + par);
        System.out.println("par.primero() = " + par.primero()); // llama accessor override
        System.out.println("invertido = " + par.invertir());

        // 4. Rango con validación
        System.out.println("\n--- Record Rango ---");
        Rango r = new Rango(10, 20);
        System.out.println("Rango: " + r);
        System.out.println("contiene 15: " + r.contiene(15));
        System.out.println("contiene 25: " + r.contiene(25));
        System.out.println("longitud: " + r.longitud());
        try {
            new Rango(20, 10);
        } catch (IllegalArgumentException e) {
            System.out.println("Validacion: " + e.getMessage());
        }

        // 5. Record implementando interfaz
        System.out.println("\n--- Producto (record + interfaz) ---");
        Producto prod = new Producto("  Laptop  ", 999.99, 5);
        System.out.println(prod.describir());
        System.out.println("disponible: " + prod.disponible());
        System.out.println("con 10% descuento: " + prod.conDescuento(10).describir());

        // 6. Records en colecciones (equals/hashCode correcto)
        System.out.println("\n--- Records en colecciones ---");
        List<Punto> puntos = List.of(new Punto(1, 2), new Punto(3, 4), new Punto(1, 2));
        long unicos = puntos.stream().distinct().count();
        System.out.println("Total: " + puntos.size() + ", distintos: " + unicos); // 3, 2

        // 7. Deconstruction en instanceof (Java 21)
        System.out.println("\n--- Pattern matching con record ---");
        Object obj = new Punto(6, 8);
        if (obj instanceof Punto(var x, var y)) {
            System.out.println("Deconstruccion: x=" + x + ", y=" + y);
            System.out.printf("Hipotenusa: %.1f%n", Math.sqrt(x * x + y * y));
        }
    }
}
