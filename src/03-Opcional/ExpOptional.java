import java.util.Optional;

public class ExpOptional {



    public static void main(String[] args) {

        // =========================
        // 1. CREACIÓN DE OPTIONALS
        // =========================

        // Optional.of(valor) → el valor NO puede ser null (si es null, lanza NullPointerException)
        Optional<String> optNombre = Optional.of("Jorex");

        // Optional.ofNullable(valor) → permite null (si es null, devuelve Optional.empty())
        String posibleNull = null;
        Optional<String> optPosible = Optional.ofNullable(posibleNull); // aquí será Optional.empty()

        // Optional.empty() → Optional vacío explícito
        Optional<String> optVacio = Optional.empty();

        // ======================================
        // 2. orElse, orElseGet y orElseThrow
        // ======================================

        // orElse → devuelve el valor o un valor por defecto inmediato
        String nombre = optNombre.orElse("Desconocido"); // "Jorex"

        // Como optPosible está vacío, se usa el valor por defecto
        String otroNombre = optPosible.orElse("Sin nombre");

        // orElseGet → igual que orElse, pero usa un Supplier (evaluación lazy)
        String calculado = optPosible.orElseGet(() -> {
            // Este bloque solo se ejecuta si el Optional está vacío
            return "Generado por orElseGet";
        });

        // orElseThrow → lanza excepción si no hay valor
        String obligatorio = optNombre.orElseThrow(() -> new IllegalStateException("Nombre obligatorio"));

        // ======================================
        // 3. ifPresent e ifPresentOrElse
        // ======================================

        Optional<String> optSaludo = Optional.of("Hola");

        // ifPresent → ejecuta la acción solo si hay valor
        optSaludo.ifPresent(valor -> {
            System.out.println("Saludo presente: " + valor);
        });

        // ifPresentOrElse → acción si hay valor / acción alternativa si está vacío
        Optional<String> sinSaludo = Optional.empty();
        sinSaludo.ifPresentOrElse(
                valor -> System.out.println("Hay saludo: " + valor),
                () -> System.out.println("No hay saludo definido")
        );

        // ======================================
        // 4. map y flatMap
        // ======================================

        Optional<String> optTexto = Optional.of("java-prep");

        // map(Function) → transforma el valor si está presente
        Optional<Integer> longitud = optTexto.map(String::length); // Optional<Integer> con 8
        System.out.println("Longitud: " + longitud.orElse(0));

        // Ejemplo: encadenar transformaciones
        Optional<String> mayus = optTexto
                .map(String::toUpperCase)     // "JAVA-PREP"
                .map(s -> s + "!!!");         // "JAVA-PREP!!!"

        System.out.println("Transformado: " + mayus.orElse("VACÍO"));

        // flatMap(Function) → se usa cuando la función ya devuelve un Optional
        Optional<String> optConMayus = optTexto.flatMap(ExpOptional::aMayusOptional);
        System.out.println("flatMap: " + optConMayus.orElse("VACÍO"));

        // ======================================
        // 5. PATRÓN CORRECTO vs PATRÓN INCORRECTO
        // ======================================

        Optional<String> optEmail = Optional.of("user@mail.com");

        // MAL USO: tratar Optional como si fuera null con isPresent + get
        if (optEmail.isPresent()) {
            String email = optEmail.get(); // get() solo si está presente, pero es código muy verboso
            System.out.println("Email (patrón no recomendado): " + email);
        }

        // BUEN USO: programación funcional con map/orElse
        String emailBuenaPractica = optEmail
                .map(String::toLowerCase)
                .orElse("sin-email@default.com");

        System.out.println("Email (buena práctica): " + emailBuenaPractica);

        // ======================================
        // 6. Optional en métodos que pueden no encontrar resultado
        // ======================================

        Optional<String> usuarioEncontrado = findUserById(1);
        Optional<String> usuarioNoEncontrado = findUserById(99);

        // Consumimos el Optional sin usar null
        System.out.println("Usuario 1: " + usuarioEncontrado.orElse("No existe"));
        System.out.println("Usuario 99: " + usuarioNoEncontrado.orElse("No existe"));

        // ======================================
        // 7. Optional encadenado con lógica de dominio
        // ======================================

        Optional<String> optRol = findUserById(1)
                .flatMap(ExpOptional::findRoleByUserName); // la función ya devuelve Optional

        String rol = optRol.orElse("ROLE_GUEST");
        System.out.println("Rol de usuario 1: " + rol);
    }

    // Ejemplo para flatMap: devuelve un Optional
    private static Optional<String> aMayusOptional(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(texto.toUpperCase());
    }

    // Simula un "repositorio" que puede o no encontrar un usuario
    private static Optional<String> findUserById(int id) {
        if (id == 1) return Optional.of("Jorex");
        if (id == 2) return Optional.of("ProgramandoConJorge");
        return Optional.empty();
    }

    // Simula la búsqueda del rol de un usuario por su nombre
    private static Optional<String> findRoleByUserName(String name) {
        if ("Jorex".equals(name)) {
            return Optional.of("ROLE_ADMIN");
        }
        if ("ProgramandoConJorge".equals(name)) {
            return Optional.of("ROLE_USER");
        }
        return Optional.empty();
    }
}
