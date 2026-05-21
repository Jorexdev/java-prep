// Ejercicio 1 — Dynamic profile switching
// ProfileContext permite cambiar el perfil en runtime y notifica a beans ProfileAware.

import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {

    // Interfaz que deben implementar los beans que reaccionan a cambios de perfil
    interface ProfileAware {
        void onProfileChange(String oldProfile, String newProfile);
        String getBeanName();
    }

    // Contexto que gestiona el perfil activo y notifica a los beans registrados
    static class ProfileContext {
        private String activeProfile;
        private final List<ProfileAware> registeredBeans = new ArrayList<>();

        ProfileContext(String initialProfile) {
            this.activeProfile = initialProfile;
            System.out.println("[ProfileContext] Perfil inicial: " + initialProfile);
        }

        void register(ProfileAware bean) {
            registeredBeans.add(bean);
            System.out.println("[ProfileContext] Bean registrado: " + bean.getBeanName());
        }

        void switchProfile(String newProfile) {
            String oldProfile = this.activeProfile;
            if (oldProfile.equals(newProfile)) {
                System.out.println("[ProfileContext] Perfil ya activo: " + newProfile + " (sin cambios)");
                return;
            }
            System.out.printf("%n[ProfileContext] === Cambiando perfil: '%s' → '%s' ===%n", oldProfile, newProfile);
            this.activeProfile = newProfile;

            // Notificar a todos los beans registrados
            System.out.println("[ProfileContext] Notificando a " + registeredBeans.size() + " beans...");
            for (ProfileAware bean : registeredBeans) {
                bean.onProfileChange(oldProfile, newProfile);
            }
            System.out.println("[ProfileContext] Cambio completado\n");
        }

        String getActiveProfile() {
            return activeProfile;
        }
    }

    // Bean de base de datos
    static class DataSourceBean implements ProfileAware {
        private String currentUrl;
        private int poolSize;

        DataSourceBean(String initialProfile) {
            reinitialize(initialProfile);
        }

        private void reinitialize(String profile) {
            if ("prod".equals(profile)) {
                this.currentUrl = "jdbc:postgresql://prod-db:5432/app";
                this.poolSize = 20;
            } else if ("staging".equals(profile)) {
                this.currentUrl = "jdbc:postgresql://staging-db:5432/app";
                this.poolSize = 5;
            } else {
                this.currentUrl = "jdbc:h2:mem:devdb";
                this.poolSize = 2;
            }
        }

        @Override
        public void onProfileChange(String oldProfile, String newProfile) {
            reinitialize(newProfile);
            System.out.printf("  [DataSourceBean] Reinicializado → url=%s, pool=%d%n", currentUrl, poolSize);
        }

        @Override
        public String getBeanName() { return "DataSourceBean"; }

        void query(String sql) {
            System.out.printf("  [DataSourceBean] Ejecutando '%s' en %s (pool=%d)%n", sql, currentUrl, poolSize);
        }
    }

    // Bean de caché
    static class CacheBean implements ProfileAware {
        private boolean enabled;
        private int ttlSeconds;

        CacheBean(String initialProfile) {
            reinitialize(initialProfile);
        }

        private void reinitialize(String profile) {
            if ("prod".equals(profile) || "staging".equals(profile)) {
                this.enabled = true;
                this.ttlSeconds = 3600;
            } else {
                this.enabled = false;
                this.ttlSeconds = 0;
            }
        }

        @Override
        public void onProfileChange(String oldProfile, String newProfile) {
            reinitialize(newProfile);
            System.out.printf("  [CacheBean] Reinicializado → enabled=%b, ttl=%ds%n", enabled, ttlSeconds);
        }

        @Override
        public String getBeanName() { return "CacheBean"; }

        void get(String key) {
            if (enabled) {
                System.out.printf("  [CacheBean] Cache HIT para '%s' (ttl=%ds)%n", key, ttlSeconds);
            } else {
                System.out.printf("  [CacheBean] Cache DESACTIVADA — bypass para '%s'%n", key);
            }
        }
    }

    // Bean de logging
    static class LoggingBean implements ProfileAware {
        private String level;

        LoggingBean(String initialProfile) {
            reinitialize(initialProfile);
        }

        private void reinitialize(String profile) {
            this.level = switch (profile) {
                case "prod"    -> "WARN";
                case "staging" -> "INFO";
                default        -> "DEBUG";
            };
        }

        @Override
        public void onProfileChange(String oldProfile, String newProfile) {
            reinitialize(newProfile);
            System.out.printf("  [LoggingBean] Reinicializado → level=%s%n", level);
        }

        @Override
        public String getBeanName() { return "LoggingBean"; }

        void log(String message) {
            System.out.printf("  [LoggingBean] [%s] %s%n", level, message);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 1 — Dynamic profile switching ===\n");

        // Crear contexto con perfil inicial "dev"
        ProfileContext ctx = new ProfileContext("dev");

        DataSourceBean ds      = new DataSourceBean("dev");
        CacheBean      cache   = new CacheBean("dev");
        LoggingBean    logging = new LoggingBean("dev");

        ctx.register(ds);
        ctx.register(cache);
        ctx.register(logging);

        System.out.println("\n--- Estado inicial (dev) ---");
        ds.query("SELECT * FROM users");
        cache.get("user:123");
        logging.log("Aplicación iniciada");

        System.out.println("\n--- Cambio a 'prod' ---");
        ctx.switchProfile("prod");

        ds.query("SELECT * FROM orders");
        cache.get("product:456");
        logging.log("Request procesada");

        System.out.println("--- Cambio a 'staging' ---");
        ctx.switchProfile("staging");

        ds.query("SELECT * FROM reports");
        cache.get("report:789");
        logging.log("Reporte generado");

        System.out.println("--- Cambio al mismo perfil (sin efecto) ---");
        ctx.switchProfile("staging");

        System.out.println("\n--- En Spring real ---");
        System.out.println("El ApplicationContext se recrea al cambiar perfiles.");
        System.out.println("No es posible cambiar perfiles en runtime sin reiniciar el contexto.");
        System.out.println("Esta demo ilustra el concepto de notificación de cambios.");
    }
}
