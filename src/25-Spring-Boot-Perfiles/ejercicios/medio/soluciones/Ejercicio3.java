// Ejercicio 3 — Beans por perfil con herencia
// BaseServicio → DevServicio (dev) / ProdServicio (prod).
// Contenedor registra la implementación correcta y permite cambiar en runtime.

import java.util.HashMap;
import java.util.Map;

public class Ejercicio3 {

    // Clase base con comportamiento común
    static abstract class BaseServicio {
        abstract String getPerfil();
        abstract String getDataSourceUrl();

        void procesar(String tarea) {
            System.out.printf("[%s] Procesando: %s (ds: %s)%n",
                getClass().getSimpleName(), tarea, getDataSourceUrl());
        }

        void inicializar() {
            System.out.printf("[%s] Inicializando bean para perfil '%s'%n",
                getClass().getSimpleName(), getPerfil());
        }

        void destruir() {
            System.out.printf("[%s] Cerrando bean%n", getClass().getSimpleName());
        }
    }

    // @Profile("dev")
    static class DevServicio extends BaseServicio {
        @Override public String getPerfil() { return "dev"; }
        @Override public String getDataSourceUrl() { return "jdbc:h2:mem:devdb"; }

        @Override
        void procesar(String tarea) {
            System.out.printf("[DevServicio] [TRACE] Procesando: %s%n", tarea);
            System.out.println("[DevServicio]   → usando H2 en memoria");
            System.out.println("[DevServicio]   → transacción: auto-commit=true");
            super.procesar(tarea);
        }
    }

    // @Profile("prod")
    static class ProdServicio extends BaseServicio {
        @Override public String getPerfil() { return "prod"; }
        @Override public String getDataSourceUrl() { return "jdbc:postgresql://prod:5432/app"; }

        @Override
        void procesar(String tarea) {
            System.out.printf("[ProdServicio] Procesando: %s%n", tarea);
            System.out.println("[ProdServicio]  → usando PostgreSQL pool");
            System.out.println("[ProdServicio]  → métricas enviadas a Prometheus");
            super.procesar(tarea);
        }
    }

    // Contenedor que simula el ApplicationContext de Spring
    static class ServicioContainer {
        private final Map<String, BaseServicio> registry = new HashMap<>();
        private String activeProfile;
        private BaseServicio current;

        ServicioContainer() {
            registry.put("dev",  new DevServicio());
            registry.put("prod", new ProdServicio());
        }

        void setActiveProfile(String profile) {
            if (current != null) {
                current.destruir();
            }
            this.activeProfile = profile;
            this.current = registry.get(profile);
            if (current == null) {
                throw new IllegalArgumentException("No hay bean para el perfil: " + profile);
            }
            current.inicializar();
            System.out.println();
        }

        BaseServicio getServicio() {
            if (current == null) {
                throw new IllegalStateException("Ningún perfil activo configurado");
            }
            return current;
        }

        String getActiveProfile() {
            return activeProfile;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 3 — Beans por perfil con herencia ===\n");

        ServicioContainer container = new ServicioContainer();

        System.out.println("--- Activando perfil 'dev' ---");
        container.setActiveProfile("dev");
        container.getServicio().procesar("cargar-usuarios");
        container.getServicio().procesar("generar-reporte");
        System.out.println();

        System.out.println("--- Cambiando a perfil 'prod' en runtime ---");
        container.setActiveProfile("prod");
        container.getServicio().procesar("cargar-usuarios");
        container.getServicio().procesar("generar-reporte");
        System.out.println();

        System.out.println("--- Volviendo a 'dev' ---");
        container.setActiveProfile("dev");
        container.getServicio().procesar("test-task");
        System.out.println();

        System.out.println("--- Herencia de comportamiento ---");
        System.out.println("DevServicio  hereda de BaseServicio: " +
            (new DevServicio() instanceof BaseServicio));
        System.out.println("ProdServicio hereda de BaseServicio: " +
            (new ProdServicio() instanceof BaseServicio));
        System.out.println("Ambos son intercambiables donde se espera BaseServicio");
    }
}
