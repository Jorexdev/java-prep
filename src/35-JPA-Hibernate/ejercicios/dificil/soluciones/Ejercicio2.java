import java.util.*;

public class Ejercicio2 {

    // @Entity @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
    // @DiscriminatorColumn(name = "tipo")
    static abstract class Vehiculo {
        int id;
        String marca;
        final String tipo;
        Vehiculo(int id, String marca, String tipo) { this.id = id; this.marca = marca; this.tipo = tipo; }
        abstract String detalles();
        @Override public String toString() { return tipo + "{id=" + id + ", marca=" + marca + detalles() + "}"; }
    }

    // @DiscriminatorValue("COCHE")
    static class Coche extends Vehiculo {
        int puertas;
        Coche(int id, String marca, int puertas) { super(id, marca, "COCHE"); this.puertas = puertas; }
        @Override String detalles() { return ", puertas=" + puertas; }
    }

    // @DiscriminatorValue("CAMION")
    static class Camion extends Vehiculo {
        double cargaMaxTon;
        Camion(int id, String marca, double cargaMaxTon) { super(id, marca, "CAMION"); this.cargaMaxTon = cargaMaxTon; }
        @Override String detalles() { return ", cargaMax=" + cargaMaxTon + "t"; }
    }

    static class VehiculoRepository {
        private final Map<Integer, Vehiculo> tabla = new HashMap<>();
        private int nextId = 1;

        void persist(Vehiculo v) { v.id = nextId++; tabla.put(v.id, v); }
        List<Vehiculo> findAll() { return new ArrayList<>(tabla.values()); }
        List<Coche>    findCoches()   { return tabla.values().stream().filter(v -> v instanceof Coche).map(v -> (Coche) v).toList(); }
        List<Camion>   findCamiones() { return tabla.values().stream().filter(v -> v instanceof Camion).map(v -> (Camion) v).toList(); }
        List<Vehiculo> findByMarca(String marca) { return tabla.values().stream().filter(v -> v.marca.equals(marca)).toList(); }
    }

    public static void main(String[] args) {
        VehiculoRepository repo = new VehiculoRepository();
        repo.persist(new Coche(0, "Toyota", 4));
        repo.persist(new Camion(0, "Volvo", 20.5));
        repo.persist(new Coche(0, "Ford", 2));
        repo.persist(new Camion(0, "Mercedes", 15.0));
        repo.persist(new Coche(0, "Toyota", 5));

        System.out.println("--- Todos (tabla única, discriminador 'tipo') ---");
        repo.findAll().forEach(System.out::println);

        System.out.println("\n--- Solo Coches ---");
        repo.findCoches().forEach(System.out::println);

        System.out.println("\n--- Solo Camiones ---");
        repo.findCamiones().forEach(System.out::println);

        System.out.println("\n--- findByMarca('Toyota') ---");
        repo.findByMarca("Toyota").forEach(System.out::println);
    }
}
