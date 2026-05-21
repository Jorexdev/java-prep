import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Ejercicio 5 (Fácil) — Starter composition
// "web" activa automáticamente "jackson" y "tomcat" (con sus transitivos)
public class Ejercicio5 {

    static class Starter {
        final String name;
        final List<String> activates; // starters que este activa automáticamente

        Starter(String name, String... activates) {
            this.name = name;
            this.activates = List.of(activates);
        }
    }

    static class StarterActivator {
        private final Map<String, Starter> registry = new LinkedHashMap<>();
        private final Set<String> active = new LinkedHashSet<>();

        public void registerStarter(Starter starter) {
            registry.put(starter.name, starter);
        }

        /**
         * Activa el starter indicado y todos sus transitivos recursivamente.
         */
        public void activate(String starterName) {
            if (active.contains(starterName)) return; // ya activo

            Starter starter = registry.get(starterName);
            if (starter == null) {
                System.out.println("[Activator] AVISO: starter '" + starterName + "' no registrado");
                return;
            }

            // Primero activar las dependencias
            for (String dep : starter.activates) {
                activate(dep);
            }

            active.add(starterName);
            System.out.println("[Activator] Activado: spring-boot-starter-" + starterName
                    + (starter.activates.isEmpty() ? "" : " (transitivo de sus deps: " + starter.activates + ")"));
        }

        public Set<String> getActiveStarters() { return Set.copyOf(active); }

        public void printReport() {
            System.out.println("Starters activos (" + active.size() + "):");
            for (String name : active) {
                System.out.println("  [OK] spring-boot-starter-" + name);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Starter composition ===");
        System.out.println();

        StarterActivator activator = new StarterActivator();

        // Definir todos los starters y sus dependencias
        activator.registerStarter(new Starter("logging"));
        activator.registerStarter(new Starter("json"));
        activator.registerStarter(new Starter("jackson", "json", "logging"));
        activator.registerStarter(new Starter("tomcat", "logging"));
        activator.registerStarter(new Starter("validation"));
        activator.registerStarter(new Starter("web", "jackson", "tomcat", "validation"));
        activator.registerStarter(new Starter("security", "web"));
        activator.registerStarter(new Starter("data-jpa", "logging"));

        System.out.println("=== Activando starter 'web' ===");
        activator.activate("web");
        System.out.println();
        activator.printReport();

        System.out.println();
        System.out.println("=== Activando starter 'security' (encima de web) ===");
        StarterActivator activator2 = new StarterActivator();
        activator2.registerStarter(new Starter("logging"));
        activator2.registerStarter(new Starter("json"));
        activator2.registerStarter(new Starter("jackson", "json", "logging"));
        activator2.registerStarter(new Starter("tomcat", "logging"));
        activator2.registerStarter(new Starter("validation"));
        activator2.registerStarter(new Starter("web", "jackson", "tomcat", "validation"));
        activator2.registerStarter(new Starter("security", "web"));

        activator2.activate("security");
        System.out.println();
        activator2.printReport();
    }
}
