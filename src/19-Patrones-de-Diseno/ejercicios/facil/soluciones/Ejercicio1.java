public class Ejercicio1 {

    static class ConfiguracionApp {
        private static ConfiguracionApp instancia;
        private final java.util.Map<String, String> propiedades = new java.util.HashMap<>();

        private ConfiguracionApp() {
            propiedades.put("version", "1.0.0");
            propiedades.put("env", "produccion");
        }

        static synchronized ConfiguracionApp getInstance() {
            if (instancia == null) instancia = new ConfiguracionApp();
            return instancia;
        }

        String get(String clave) { return propiedades.getOrDefault(clave, "N/A"); }
        void set(String clave, String valor) { propiedades.put(clave, valor); }
    }

    public static void main(String[] args) {
        ConfiguracionApp c1 = ConfiguracionApp.getInstance();
        ConfiguracionApp c2 = ConfiguracionApp.getInstance();

        System.out.println("Misma instancia: " + (c1 == c2));
        System.out.println("version: " + c1.get("version"));

        c1.set("timeout", "30");
        System.out.println("timeout desde c2: " + c2.get("timeout")); // 30 — misma instancia
    }
}
