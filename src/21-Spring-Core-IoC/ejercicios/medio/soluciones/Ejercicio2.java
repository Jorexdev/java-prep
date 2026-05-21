import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Ejercicio2 {

    static class ConexionDB {
        private static int contador = 0;
        private final int id;

        ConexionDB() {
            this.id = ++contador;
            System.out.println("  [new] ConexionDB creada, id=" + id);
        }

        @Override
        public String toString() {
            return "ConexionDB#" + id;
        }
    }

    static class SesionUsuario {
        private static int contador = 0;
        private final int id;

        SesionUsuario() {
            this.id = ++contador;
            System.out.println("  [new] SesionUsuario creada, id=" + id);
        }

        @Override
        public String toString() {
            return "SesionUsuario#" + id;
        }
    }

    static class ContenedorScopes {
        private final Map<Class<?>, Object> singletons = new HashMap<>();
        private final Map<Class<?>, Supplier<?>> prototypes = new HashMap<>();

        void registerSingleton(Class<?> tipo, Object instancia) {
            singletons.put(tipo, instancia);
        }

        void registerPrototype(Class<?> tipo, Supplier<?> factory) {
            prototypes.put(tipo, factory);
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(Class<T> tipo) {
            if (singletons.containsKey(tipo)) {
                // Siempre la misma instancia
                return (T) singletons.get(tipo);
            }
            if (prototypes.containsKey(tipo)) {
                // Nueva instancia cada vez
                return (T) prototypes.get(tipo).get();
            }
            throw new IllegalStateException("Bean no registrado: " + tipo.getSimpleName());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Singleton vs Prototype scope ===\n");

        ContenedorScopes contenedor = new ContenedorScopes();

        // Singleton: creamos la instancia UNA vez y la registramos
        contenedor.registerSingleton(ConexionDB.class, new ConexionDB());

        // Prototype: registramos una fábrica, cada getBean crea nueva instancia
        contenedor.registerPrototype(SesionUsuario.class, SesionUsuario::new);

        System.out.println("\n--- SINGLETON (ConexionDB) ---");
        ConexionDB db1 = contenedor.getBean(ConexionDB.class);
        ConexionDB db2 = contenedor.getBean(ConexionDB.class);
        System.out.println("db1: " + db1 + " | hashCode=" + db1.hashCode());
        System.out.println("db2: " + db2 + " | hashCode=" + db2.hashCode());
        System.out.println("db1 == db2: " + (db1 == db2) + " <- misma instancia");

        System.out.println("\n--- PROTOTYPE (SesionUsuario) ---");
        SesionUsuario s1 = contenedor.getBean(SesionUsuario.class);
        SesionUsuario s2 = contenedor.getBean(SesionUsuario.class);
        System.out.println("s1: " + s1 + " | hashCode=" + s1.hashCode());
        System.out.println("s2: " + s2 + " | hashCode=" + s2.hashCode());
        System.out.println("s1 == s2: " + (s1 == s2) + " <- instancias distintas");
    }
}
