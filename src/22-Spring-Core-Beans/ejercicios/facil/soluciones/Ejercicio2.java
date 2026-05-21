import java.util.UUID;
import java.util.function.Supplier;

public class Ejercicio2 {

    // Con scope=prototype, Spring crea una nueva instancia en cada getBean()
    static class SesionUsuario {
        private final String id;
        private final long creada;

        SesionUsuario() {
            this.id = UUID.randomUUID().toString().substring(0, 8);
            this.creada = System.currentTimeMillis();
            System.out.println("  [new] SesionUsuario creada: id=" + id);
        }

        String getId() { return id; }

        @Override
        public String toString() {
            return "SesionUsuario{id=" + id + "}";
        }
    }

    // Simula scope=prototype: cada llamada a nuevaSesion() crea una nueva instancia
    static class ContenedorPrototype {
        private final Supplier<SesionUsuario> factory;

        ContenedorPrototype(Supplier<SesionUsuario> factory) {
            this.factory = factory;
        }

        // Equivale a context.getBean(SesionUsuario.class) con scope=prototype
        SesionUsuario nuevaSesion() {
            return factory.get();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Prototype Scope ===\n");

        // Registramos el factory — aún no se crea ninguna instancia
        ContenedorPrototype contenedor = new ContenedorPrototype(SesionUsuario::new);

        System.out.println("Creando 3 sesiones:");
        SesionUsuario s1 = contenedor.nuevaSesion();
        SesionUsuario s2 = contenedor.nuevaSesion();
        SesionUsuario s3 = contenedor.nuevaSesion();

        System.out.println();
        System.out.println("s1: " + s1 + " | hashCode=" + System.identityHashCode(s1));
        System.out.println("s2: " + s2 + " | hashCode=" + System.identityHashCode(s2));
        System.out.println("s3: " + s3 + " | hashCode=" + System.identityHashCode(s3));

        System.out.println();
        System.out.println("s1 == s2: " + (s1 == s2));
        System.out.println("s2 == s3: " + (s2 == s3));
        System.out.println("IDs distintos: "
            + !s1.getId().equals(s2.getId()) + ", "
            + !s2.getId().equals(s3.getId()));

        System.out.println("\nCada getBean() con prototype = nueva instancia");
    }
}
