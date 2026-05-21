import java.util.Objects;

public class Ejercicio2 {

    static class Persona {
        private final int id;
        private String nombre;

        Persona(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        void setNombre(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Persona p)) return false;
            return id == p.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(id);
        }

        @Override
        public String toString() {
            return "Persona{id=" + id + ", nombre='" + nombre + "'}";
        }
    }

    static final class Direccion {
        private final String calle;
        private final String ciudad;
        private final String cp;

        Direccion(String calle, String ciudad, String cp) {
            this.calle = calle;
            this.ciudad = ciudad;
            this.cp = cp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Direccion d)) return false;
            return Objects.equals(calle, d.calle)
                && Objects.equals(ciudad, d.ciudad)
                && Objects.equals(cp, d.cp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(calle, ciudad, cp);
        }

        @Override
        public String toString() {
            return "Direccion{calle='" + calle + "', ciudad='" + ciudad + "', cp='" + cp + "'}";
        }
    }

    public static void main(String[] args) {
        Persona p1 = new Persona(1, "Ana");
        Persona p2 = new Persona(2, "Ana");

        System.out.println("p1: " + p1);
        System.out.println("p2: " + p2);
        System.out.println("p1.equals(p2) (mismo nombre, distinto id): " + p1.equals(p2));

        p1.setNombre("Ana García");
        System.out.println("p1 tras mutación: " + p1);

        Direccion d1 = new Direccion("Gran Vía 1", "Madrid", "28013");
        Direccion d2 = new Direccion("Gran Vía 1", "Madrid", "28013");

        System.out.println("\nd1: " + d1);
        System.out.println("d2: " + d2);
        System.out.println("d1.equals(d2) (mismos campos, distintas instancias): " + d1.equals(d2));
        System.out.println("d1 == d2 (misma referencia): " + (d1 == d2));
    }
}
