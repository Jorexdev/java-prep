import java.util.HashMap;
import java.util.Map;

public class Ejercicio3 {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> mergeRecursivo(
            Map<String, Object> base,
            Map<String, Object> override) {

        Map<String, Object> resultado = new HashMap<>(base);

        for (Map.Entry<String, Object> entry : override.entrySet()) {
            String clave = entry.getKey();
            Object valorOverride = entry.getValue();
            Object valorBase = resultado.get(clave);

            if (valorBase instanceof Map && valorOverride instanceof Map) {
                // Ambos son mapas: fusionar recursivamente
                resultado.put(clave, mergeRecursivo(
                    (Map<String, Object>) valorBase,
                    (Map<String, Object>) valorOverride
                ));
            } else {
                // Override gana para valores simples o tipos distintos
                resultado.put(clave, valorOverride);
            }
        }
        return resultado;
    }

    public static void main(String[] args) {
        // Configuración base
        Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put("host", "localhost");
        dbConfig.put("port", 5432);
        dbConfig.put("name", "mydb");

        Map<String, Object> base = new HashMap<>();
        base.put("app", "mi-servicio");
        base.put("version", "1.0");
        base.put("db", dbConfig);

        // Override de producción
        Map<String, Object> dbOverride = new HashMap<>();
        dbOverride.put("host", "prod-server.example.com");
        dbOverride.put("port", 5433);

        Map<String, Object> override = new HashMap<>();
        override.put("version", "2.0");
        override.put("db", dbOverride);

        Map<String, Object> resultado = mergeRecursivo(base, override);

        System.out.println("Configuración base:     " + base);
        System.out.println("Override:               " + override);
        System.out.println("Resultado merge:");
        resultado.forEach((k, v) -> System.out.println("  " + k + " -> " + v));
    }
}
