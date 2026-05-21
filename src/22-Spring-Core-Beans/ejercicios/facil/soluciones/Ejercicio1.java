public class Ejercicio1 {

    static class ConexionBD {
        private boolean abierta = false;
        private final String url;

        ConexionBD(String url) {
            this.url = url;
            System.out.println("[ConexionBD] Objeto creado (conexión AÚN no abierta)");
        }

        // En Spring: @PostConstruct
        void init() {
            abierta = true;
            System.out.println("[ConexionBD] init() — Abriendo conexión a " + url);
            System.out.println("[ConexionBD] Estado: abierta=" + abierta);
        }

        // En Spring: @PreDestroy
        void destroy() {
            System.out.println("[ConexionBD] destroy() — Cerrando conexión");
            abierta = false;
            System.out.println("[ConexionBD] Estado: abierta=" + abierta);
        }

        String ejecutar(String sql) {
            if (!abierta) {
                throw new IllegalStateException("La conexión está cerrada");
            }
            return "[ConexionBD] Resultado de: " + sql;
        }

        boolean isAbierta() { return abierta; }
    }

    public static void main(String[] args) {
        System.out.println("=== Ciclo de vida básico de un Bean ===\n");

        System.out.println("-- Fase 1: Construcción --");
        ConexionBD conn = new ConexionBD("jdbc:memoria://localhost/testdb");

        System.out.println("\n-- Fase 2: Inicialización (@PostConstruct) --");
        conn.init();

        System.out.println("\n-- Fase 3: Uso del bean --");
        System.out.println(conn.ejecutar("SELECT * FROM productos"));
        System.out.println(conn.ejecutar("INSERT INTO logs VALUES ('evento-1')"));

        System.out.println("\n-- Fase 4: Destrucción (@PreDestroy) --");
        conn.destroy();

        System.out.println("\n-- Verificación post-destroy --");
        try {
            conn.ejecutar("SELECT 1");
        } catch (IllegalStateException e) {
            System.out.println("Error esperado: " + e.getMessage());
        }

        System.out.println("\nCiclo completo: constructor -> init -> uso -> destroy");
    }
}
