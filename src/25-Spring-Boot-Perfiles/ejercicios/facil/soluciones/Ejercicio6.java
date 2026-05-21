// Ejercicio 6 — SPRING_PROFILES_ACTIVE
// Leer el perfil de System.getProperty("spring.profiles.active") con fallback a "dev".

public class Ejercicio6 {

    interface AppService {
        void start();
        String getBeanName();
    }

    // Bean para dev
    static class DevAppService implements AppService {
        @Override
        public void start() {
            System.out.println("[DevAppService] Iniciando con H2 + logs verbose + hot reload");
        }
        @Override public String getBeanName() { return "DevAppService"; }
    }

    // Bean para prod
    static class ProdAppService implements AppService {
        @Override
        public void start() {
            System.out.println("[ProdAppService] Iniciando con PostgreSQL + logs mínimos + métricas");
        }
        @Override public String getBeanName() { return "ProdAppService"; }
    }

    // Bean para test
    static class TestAppService implements AppService {
        @Override
        public void start() {
            System.out.println("[TestAppService] Iniciando con mocks + base de datos en memoria");
        }
        @Override public String getBeanName() { return "TestAppService"; }
    }

    static String resolveActiveProfile() {
        // Spring lee esta system property o la variable de entorno SPRING_PROFILES_ACTIVE
        String profile = System.getProperty("spring.profiles.active");
        if (profile == null || profile.isBlank()) {
            System.out.println("  [!] spring.profiles.active no está definida → usando 'dev' como fallback");
            return "dev";
        }
        // Spring permite varios perfiles separados por coma → tomar el primero
        String resolved = profile.split(",")[0].trim();
        System.out.println("  [*] spring.profiles.active = \"" + profile + "\" → perfil resuelto: \"" + resolved + "\"");
        return resolved;
    }

    static AppService createService(String profile) {
        return switch (profile) {
            case "prod" -> new ProdAppService();
            case "test" -> new TestAppService();
            default     -> new DevAppService();  // dev y cualquier desconocido
        };
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 6 — SPRING_PROFILES_ACTIVE ===\n");

        // --- Caso 1: sin propiedad definida (comportamiento por defecto) ---
        System.out.println("--- Caso 1: sin propiedad definida ---");
        System.clearProperty("spring.profiles.active");
        String profile1 = resolveActiveProfile();
        AppService service1 = createService(profile1);
        System.out.println("  Bean seleccionado: " + service1.getBeanName());
        service1.start();
        System.out.println();

        // --- Caso 2: propiedad definida como "prod" ---
        System.out.println("--- Caso 2: spring.profiles.active = \"prod\" ---");
        System.setProperty("spring.profiles.active", "prod");
        String profile2 = resolveActiveProfile();
        AppService service2 = createService(profile2);
        System.out.println("  Bean seleccionado: " + service2.getBeanName());
        service2.start();
        System.out.println();

        // --- Caso 3: propiedad definida como "test" ---
        System.out.println("--- Caso 3: spring.profiles.active = \"test\" ---");
        System.setProperty("spring.profiles.active", "test");
        String profile3 = resolveActiveProfile();
        AppService service3 = createService(profile3);
        System.out.println("  Bean seleccionado: " + service3.getBeanName());
        service3.start();
        System.out.println();

        // --- Caso 4: múltiples perfiles (Spring acepta coma separados) ---
        System.out.println("--- Caso 4: spring.profiles.active = \"dev,debug\" ---");
        System.setProperty("spring.profiles.active", "dev,debug");
        String profile4 = resolveActiveProfile();
        AppService service4 = createService(profile4);
        System.out.println("  Bean seleccionado: " + service4.getBeanName());
        service4.start();

        System.out.println();
        System.out.println("--- En un proyecto Spring Boot real ---");
        System.out.println("  java -Dspring.profiles.active=prod -jar app.jar");
        System.out.println("  SPRING_PROFILES_ACTIVE=prod java -jar app.jar");
        System.out.println("  En application.properties: spring.profiles.active=dev");

        // Limpiar para no afectar a otros tests
        System.clearProperty("spring.profiles.active");
    }
}
