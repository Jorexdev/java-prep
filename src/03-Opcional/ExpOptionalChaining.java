import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ExpOptionalChaining {

    public static void main(String[] args) {

        // ======================================
        // 1. PROBLEMA — cadena de null checks para acceso profundo
        // ======================================

        // Versión con null checks: verbosa y propensa a errores
        Pedido pedido = new Pedido("P1",
                new Cliente("Jorex",
                        new Direccion("Madrid", "España")));

        // Versión imperativa: 4 niveles de anidamiento
        String ciudadImperativa = null;
        if (pedido != null) {
            Cliente cliente = pedido.cliente();
            if (cliente != null) {
                Direccion dir = cliente.direccion();
                if (dir != null) {
                    ciudadImperativa = dir.ciudad();
                }
            }
        }
        System.out.println("Imperativa: " + ciudadImperativa);

        // Versión con Optional: una línea, sin anidamiento
        String ciudad = Optional.ofNullable(pedido)
                .map(Pedido::cliente)
                .map(Cliente::direccion)
                .map(Direccion::ciudad)
                .orElse("Ciudad desconocida");
        System.out.println("Optional chain: " + ciudad);

        // ======================================
        // 2. map — transforma el valor si está presente
        // ======================================

        Optional<String> optNombre = Optional.of("  jorex  ");
        Optional<String> normalizado = optNombre
                .map(String::trim)
                .map(String::toUpperCase);
        System.out.println("map chain: " + normalizado.orElse("?"));

        // ======================================
        // 3. flatMap — cuando la función ya devuelve un Optional
        //    usar map daría Optional<Optional<String>>; flatMap lo aplana
        // ======================================

        Optional<Cliente> optCliente = Optional.of(pedido.cliente());

        Optional<String> paísOpt = optCliente
                .flatMap(c -> Optional.ofNullable(c.direccion()))
                .flatMap(d -> Optional.ofNullable(d.pais()));
        System.out.println("flatMap chain: " + paísOpt.orElse("País desconocido"));

        // ======================================
        // 4. filter — descarta el valor si no cumple la condición
        // ======================================

        Optional<Integer> edad = Optional.of(17);
        Optional<Integer> edadMayor = edad.filter(e -> e >= 18);
        System.out.println("filter (>=18): " + edadMayor.isPresent()); // false

        // ======================================
        // 5. or (Java 9+) — alternativa que devuelve otro Optional (no un valor raw)
        // ======================================

        Optional<String> config = Optional.empty();
        Optional<String> conFallback = config
                .or(() -> Optional.of("valor-por-defecto")); // fallback como Optional
        System.out.println("or: " + conFallback.orElseThrow());

        // ======================================
        // 6. ifPresentOrElse (Java 9+)
        // ======================================

        Optional<String> token = Optional.empty();
        token.ifPresentOrElse(
                t -> System.out.println("Token activo: " + t),
                () -> System.out.println("Sin token — redirigir a login")
        );

        // ======================================
        // 7. stream (Java 9+) — integrar Optional en pipelines de Stream
        // ======================================

        List<Optional<String>> optionals = List.of(
                Optional.of("Spring"),
                Optional.empty(),
                Optional.of("Kafka"),
                Optional.empty()
        );

        // Optional.stream() emite 0 o 1 elemento — flatMap elimina los vacíos
        List<String> presentes = optionals.stream()
                .flatMap(Optional::stream)
                .toList();
        System.out.println("stream + flatMap: " + presentes);

        // ======================================
        // 8. DEMO — lookup de configuración con defaults encadenados
        // ======================================

        System.out.println("\n--- Lookup de config ---");
        System.out.println("db.url: "   + lookupConfig("db.url").orElse("localhost:5432"));
        System.out.println("db.pool: "  + lookupConfig("db.pool").orElse("10"));
        System.out.println("app.name: " + lookupConfig("app.name")
                .map(String::toUpperCase)
                .or(() -> Optional.of("DEFAULT-APP"))
                .orElseThrow());
    }

    // Simula un repositorio de configuración que puede no tener la clave
    private static Optional<String> lookupConfig(String clave) {
        HashMap<String, String> config = new HashMap<>();
        config.put("db.url",   "prod-db.empresa.com:5432");
        config.put("app.name", "java-prep");
        return Optional.ofNullable(config.get(clave));
    }

    record Direccion(String ciudad, String pais) {}
    record Cliente(String nombre, Direccion direccion) {}
    record Pedido(String id, Cliente cliente) {}
}
