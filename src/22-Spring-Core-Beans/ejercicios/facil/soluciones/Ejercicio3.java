public class Ejercicio3 {

    // Singleton con conteo de accesos
    // En Spring: @Component con scope=singleton (por defecto)
    static class ConfiguracionGlobal {
        private static final ConfiguracionGlobal INSTANCIA = new ConfiguracionGlobal();
        private static int llamadas = 0;

        private final String entorno;
        private final String version;

        // Constructor privado: nadie puede instanciar externamente
        private ConfiguracionGlobal() {
            this.entorno = "produccion";
            this.version = "2.1.0";
            System.out.println("[ConfiguracionGlobal] Instancia creada (solo ocurre UNA vez)");
        }

        // Método de acceso estático — equivale a context.getBean() en Spring
        static ConfiguracionGlobal getInstance() {
            llamadas++;
            System.out.println("[ConfiguracionGlobal] getInstance() llamado #" + llamadas);
            return INSTANCIA;
        }

        static int getLlamadas() { return llamadas; }

        String getEntorno() { return entorno; }
        String getVersion() { return version; }

        @Override
        public String toString() {
            return "Config{entorno=" + entorno + ", version=" + version + "}";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Singleton Scope ===\n");

        // 4 accesos al singleton
        ConfiguracionGlobal c1 = ConfiguracionGlobal.getInstance();
        ConfiguracionGlobal c2 = ConfiguracionGlobal.getInstance();
        ConfiguracionGlobal c3 = ConfiguracionGlobal.getInstance();
        ConfiguracionGlobal c4 = ConfiguracionGlobal.getInstance();

        System.out.println();
        System.out.println("Total llamadas: " + ConfiguracionGlobal.getLlamadas());
        System.out.println();

        System.out.println("c1: " + c1 + " | hashCode=" + System.identityHashCode(c1));
        System.out.println("c2: " + c2 + " | hashCode=" + System.identityHashCode(c2));
        System.out.println("c3: " + c3 + " | hashCode=" + System.identityHashCode(c3));
        System.out.println("c4: " + c4 + " | hashCode=" + System.identityHashCode(c4));

        System.out.println();
        System.out.println("c1 == c2: " + (c1 == c2));
        System.out.println("c1 == c3: " + (c1 == c3));
        System.out.println("c1 == c4: " + (c1 == c4));
        System.out.println("Todos apuntan a la misma instancia: " + (c1 == c2 && c2 == c3 && c3 == c4));

        System.out.println("\nEntorno: " + c1.getEntorno());
        System.out.println("Versión: " + c1.getVersion());
    }
}
