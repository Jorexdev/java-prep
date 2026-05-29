public class Ejercicio5 {

    public static void main(String[] args) {
        String host = "api.ejemplo.com";
        int puerto = 443;
        String env = "prod";

        // Text block con interpolación vía formatted()
        // No hay escapes \" visibles: las comillas dobles son literales dentro del text block
        String config = """
                {
                    "host": "%s",
                    "port": %d,
                    "environment": "%s",
                    "ssl": true
                }
                """.formatted(host, puerto, env);

        System.out.println("=== Configuracion JSON ===");
        System.out.println(config);

        // Verificar que el resultado tiene el formato correcto
        System.out.println("Contiene host: " + config.contains("\"api.ejemplo.com\""));
        System.out.println("Contiene puerto 443: " + config.contains("443"));
        System.out.println("Contiene ssl: true: " + config.contains("\"ssl\": true"));

        // Comparacion: string clásico requeriría escapes molestos
        String clasico = "{\n" +
                "    \"host\": \"" + host + "\",\n" +
                "    \"port\": " + puerto + ",\n" +
                "    \"environment\": \"" + env + "\",\n" +
                "    \"ssl\": true\n" +
                "}";
        System.out.println("\nSon equivalentes: " + config.stripTrailing().equals(clasico));
    }
}
