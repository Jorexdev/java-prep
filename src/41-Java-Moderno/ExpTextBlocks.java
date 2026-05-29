// Text Blocks (Java 15 estable) — strings multilínea con indentación inteligente.
// El compilador elimina la indentación común (incidental whitespace) automáticamente.

public class ExpTextBlocks {

    public static void main(String[] args) {

        System.out.println("=== TEXT BLOCKS ===\n");

        // 1. String clásico vs Text Block
        System.out.println("--- String clasico vs Text Block ---");

        // Antes: concatenaciones, escapes, ilegible
        String jsonClasico = "{\n" +
                "    \"nombre\": \"Java\",\n" +
                "    \"version\": 21\n" +
                "}";

        // Con text block: la forma del código == la forma del string
        String jsonModerno = """
                {
                    "nombre": "Java",
                    "version": 21
                }
                """;

        System.out.println("Clasico:\n" + jsonClasico);
        System.out.println("Moderno:\n" + jsonModerno);
        System.out.println("Son iguales: " + jsonClasico.equals(jsonModerno.stripTrailing()));

        // 2. Indentation stripping: la indentación común se elimina
        System.out.println("--- Indentation stripping ---");
        // La columna del """ de cierre determina cuántos espacios se eliminan
        String conIndent = """
                linea uno
                linea dos
                linea tres
                """;
        // Cada línea tiene 16 espacios en el fuente, pero el cierre """ también tiene 16
        // El resultado NO tiene espacios al inicio
        System.out.println("Con strip (cierre alineado a izquierda):");
        for (String linea : conIndent.split("\n")) {
            System.out.println("  '" + linea + "'");
        }

        // 3. Escape sequences especiales en text blocks
        System.out.println("--- Escapes especiales ---");

        // \s: fuerza un espacio al final (evita que el compilador lo elimine)
        String conEspacioFinal = """
                hola   \s
                mundo  \s
                """;
        System.out.println("Con \\s al final (preserva espacio):");
        for (String linea : conEspacioFinal.split("\n")) {
            System.out.printf("  '%s' (len=%d)%n", linea, linea.length());
        }

        // \<newline>: suprime el salto de línea (line continuation)
        String sinSalto = """
                primer fragmento \
                segundo fragmento \
                tercer fragmento
                """;
        System.out.println("\nCon \\ (line continuation): '" + sinSalto.trim() + "'");

        // 4. JSON embebido real
        System.out.println("\n--- JSON de configuracion ---");
        String host = "localhost";
        int puerto = 8080;
        String entorno = "development";

        // formatted() para interpolacion de variables
        String config = """
                {
                    "server": {
                        "host": "%s",
                        "port": %d
                    },
                    "environment": "%s",
                    "debug": true,
                    "features": [
                        "records",
                        "sealed-classes",
                        "pattern-matching"
                    ]
                }
                """.formatted(host, puerto, entorno);

        System.out.println(config);

        // 5. SQL embebido
        System.out.println("--- SQL embebido ---");
        String tabla = "usuarios";
        int limite = 10;

        String sql = """
                SELECT u.id,
                       u.nombre,
                       u.email,
                       COUNT(p.id) AS num_pedidos
                FROM %s u
                LEFT JOIN pedidos p ON p.usuario_id = u.id
                WHERE u.activo = TRUE
                GROUP BY u.id, u.nombre, u.email
                ORDER BY num_pedidos DESC
                LIMIT %d
                """.formatted(tabla, limite);

        System.out.println(sql);

        // 6. HTML embebido
        System.out.println("--- HTML embebido ---");
        String titulo = "Java Moderno";
        String descripcion = "Records, Sealed, Pattern Matching";

        String html = """
                <!DOCTYPE html>
                <html lang="es">
                  <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                  </head>
                  <body>
                    <h1>%s</h1>
                    <p>%s</p>
                  </body>
                </html>
                """.formatted(titulo, titulo, descripcion);

        System.out.println(html);

        // 7. Diferencia entre cierre """ en distintas columnas
        System.out.println("--- Posicion del cierre \"\"\" ---");

        // Cierre al nivel del contenido: NO elimina indentación interna relativa
        String indentadoA = """
                    sin extras
                    """;
        // Cierre más a la izquierda que el contenido: elimina esa cantidad
        System.out.println("Longitud sin extras: " + indentadoA.trim().length());

        // 8. stripIndent() y translateEscapes() — métodos de String complementarios
        System.out.println("\n--- stripIndent() y translateEscapes() ---");
        String raw = "    hola\\n    mundo";
        System.out.println("raw: " + raw);
        System.out.println("translateEscapes: " + raw.translateEscapes());
        String conMargen = "   linea1\n   linea2\n   linea3\n";
        System.out.println("stripIndent:");
        System.out.println(conMargen.stripIndent());

        // 9. Text block como parte de expresión
        System.out.println("--- Como parte de expresion ---");
        int longitud = """
                texto de ejemplo para contar
                """.trim().length();
        System.out.println("Longitud del texto: " + longitud);

        boolean esJson = """
                {"clave": "valor"}
                """.trim().startsWith("{");
        System.out.println("Empieza con {: " + esJson);
    }
}
