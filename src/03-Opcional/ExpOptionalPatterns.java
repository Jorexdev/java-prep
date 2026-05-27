import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ExpOptionalPatterns {

    // ── 1. flatMap ANIDADO ───────────────────────────────────────────────────
    // flatMap evita Optional<Optional<T>> cuando la función de transformación
    // ya devuelve un Optional. Se puede encadenar en profundidad sin anidamiento.

    static Optional<String> findUser(int id) {
        if (id == 1) return Optional.of("jorex");
        return Optional.empty();
    }

    static Optional<String> findEmail(String username) {
        if ("jorex".equals(username)) return Optional.of("jorex@dev.com");
        return Optional.empty();
    }

    static Optional<String> findDomain(String email) {
        int at = email.indexOf('@');
        return at >= 0 ? Optional.of(email.substring(at + 1)) : Optional.empty();
    }

    static void flatMapAnidado() {
        System.out.println("── 1. flatMap anidado ──────────────────────────────────────");

        // map daría Optional<Optional<String>> — flatMap lo aplana automáticamente
        Optional<String> domain = findUser(1)
                .flatMap(ExpOptionalPatterns::findEmail)     // Optional<String> email
                .flatMap(ExpOptionalPatterns::findDomain);   // Optional<String> domain

        System.out.println("Dominio encontrado:    " + domain.orElse("ninguno"));

        // Con usuario inexistente el Optional.empty() se propaga sin NullPointerException
        Optional<String> sinDomain = findUser(99)
                .flatMap(ExpOptionalPatterns::findEmail)
                .flatMap(ExpOptionalPatterns::findDomain);

        System.out.println("Dominio inexistente:   " + sinDomain.orElse("ninguno"));
    }

    // ── 2. Optional.stream() (Java 9+) ──────────────────────────────────────
    // Optional.stream() emite 0 o 1 elemento. Clave para integrar Optional
    // en pipelines de Stream sin necesidad de filter(Optional::isPresent) + map(Optional::get).

    static void optionalStream() {
        System.out.println("\n── 2. Optional.stream() ────────────────────────────────────");

        List<Optional<String>> opts = List.of(
                Optional.of("Spring"),
                Optional.empty(),
                Optional.of("Kafka"),
                Optional.empty(),
                Optional.of("Java")
        );

        // Patrón idiomático: flatMap(Optional::stream) filtra los vacíos y aplana
        List<String> presentes = opts.stream()
                .flatMap(Optional::stream)
                .toList();
        System.out.println("Valores presentes:     " + presentes);

        // Combinación con otras operaciones de Stream
        long total = opts.stream()
                .flatMap(Optional::stream)
                .filter(s -> s.length() > 4)
                .count();
        System.out.println("Nombres con >4 chars:  " + total);
    }

    // ── 3. COMBINACIÓN Optional + Stream ────────────────────────────────────
    // Optional.stream() permite incluir búsquedas opcionales dentro de pipelines
    // de Stream sin romper la cadena funcional.

    static Optional<String> buscarPorId(int id) {
        return switch (id) {
            case 1 -> Optional.of("jorex");
            case 2 -> Optional.of("ana");
            case 3 -> Optional.of("luis");
            default -> Optional.empty();
        };
    }

    static void combinacionConStream() {
        System.out.println("\n── 3. Combinación con Stream ───────────────────────────────");

        // Resolver varios IDs, algunos inexistentes, sin if/null
        List<Integer> ids = List.of(1, 99, 2, 88, 3);

        List<String> usuarios = ids.stream()
                .flatMap(id -> buscarPorId(id).stream())   // ausentes = 0 elementos
                .map(String::toUpperCase)
                .toList();

        System.out.println("Usuarios resueltos:    " + usuarios);

        // Primer match que cumpla condición (Stream.findFirst devuelve Optional)
        Optional<String> primerAdmin = ids.stream()
                .flatMap(id -> buscarPorId(id).stream())
                .filter(u -> u.startsWith("j"))
                .findFirst();

        System.out.println("Primer usuario 'j':    " + primerAdmin.orElse("ninguno"));
    }

    // ── 4. Optional COMO RESULTADO vs COMO CAMPO (antipatrón) ───────────────
    // CORRECTO: Optional como tipo de retorno de un método "find" que puede no encontrar.
    // INCORRECTO: Optional como campo de una clase (serialización rota, hashCode/equals raros).

    static void optionalComoResultadoVsCampo() {
        System.out.println("\n── 4. Resultado de búsqueda vs campo de clase ──────────────");

        // BIEN: Optional como return type — señaliza al llamador que puede estar vacío
        Optional<String> resultado = buscarPorId(1);
        resultado.ifPresent(u -> System.out.println("Usuario encontrado:    " + u));

        // MAL (solo comentario): Optional como campo
        // class UsuarioMal { Optional<String> email; } // no serializable, no hashCode correcto
        System.out.println("Campo como Optional:   antipatrón — usar @Nullable o sobrecarga");

        // BIEN: campo nullable convencional + método de acceso que devuelve Optional
        Perfil p1 = new Perfil("jorex", "jorex@dev.com");
        Perfil p2 = new Perfil("anon", null);

        System.out.println("Email p1:              " + p1.email().orElse("sin email"));
        System.out.println("Email p2:              " + p2.email().orElse("sin email"));
    }

    // ── 5. orElseGet LAZY vs orElse EAGER ───────────────────────────────────
    // orElse(valor): el valor se evalúa SIEMPRE, incluso si el Optional tiene contenido.
    // orElseGet(Supplier): el Supplier se ejecuta SOLO si el Optional está vacío (lazy).
    // Diferencia crítica cuando el valor por defecto es costoso de calcular.

    static String calcularDefault() {
        System.out.println("  [calcularDefault] ejecutado");
        return "CALCULADO";
    }

    static void orElseVsOrElseGet() {
        System.out.println("\n── 5. orElse eager vs orElseGet lazy ───────────────────────");

        Optional<String> presente = Optional.of("valor-real");

        System.out.println("--- orElse (eager) ---");
        // calcularDefault() se llama aunque 'presente' tenga valor
        String r1 = presente.orElse(calcularDefault());
        System.out.println("  resultado:            " + r1);

        System.out.println("--- orElseGet (lazy) ---");
        // calcularDefault() NO se llama porque 'presente' tiene valor
        String r2 = presente.orElseGet(ExpOptionalPatterns::calcularDefault);
        System.out.println("  resultado:            " + r2);

        System.out.println("--- con Optional vacío ambos llaman al default ---");
        Optional<String> vacio = Optional.empty();
        String r3 = vacio.orElseGet(ExpOptionalPatterns::calcularDefault);
        System.out.println("  resultado:            " + r3);

        System.out.println("REGLA: orElse solo para valores ya calculados (constantes).");
        System.out.println("       orElseGet para operaciones costosas o I/O.");
    }

    // ── Clases auxiliares ────────────────────────────────────────────────────

    static class Perfil {
        private final String nombre;
        private final String emailRaw; // campo nullable, no Optional<String>

        Perfil(String nombre, String email) {
            this.nombre   = nombre;
            this.emailRaw = email;
        }

        // El acceso devuelve Optional — la clase no almacena Optional
        Optional<String> email() {
            return Optional.ofNullable(emailRaw);
        }

        @Override public String toString() { return "Perfil{" + nombre + "}"; }
    }

    // ── main ─────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        flatMapAnidado();
        optionalStream();
        combinacionConStream();
        optionalComoResultadoVsCampo();
        orElseVsOrElseGet();
    }
}
