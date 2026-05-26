import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ExpOptionalVsException {

    public static void main(String[] args) {

        // ======================================
        // LA REGLA
        //   Optional  → cuando "no encontrar" es un caso normal de negocio
        //   Excepción → cuando la ausencia indica un error de programación o estado inválido
        // ======================================

        UsuarioRepositorio repo = new UsuarioRepositorio();

        // ======================================
        // 1. findById — devuelve Optional porque el usuario puede no existir
        // ======================================

        Optional<Usuario> opt1 = repo.findById(1);
        Optional<Usuario> opt2 = repo.findById(99);

        // El llamador decide qué hacer con la ausencia — no hay sorpresas
        opt1.ifPresent(u -> System.out.println("findById(1): " + u.nombre()));
        System.out.println("findById(99): " + opt2.isPresent()); // false, esperado

        // ======================================
        // 2. getById — lanza excepción porque si no existe es un error
        //    (se usa cuando el código asume que el ID es válido)
        // ======================================

        try {
            Usuario u = repo.getById(99); // nunca debería llegar aquí con ID inválido
        } catch (EntidadNoEncontradaException e) {
            System.out.println("getById(99) excepción: " + e.getMessage());
        }

        // ======================================
        // 3. PUENTE — orElseThrow convierte Optional en excepción
        //    Útil en capas de servicio que garantizan que el dato existe
        // ======================================

        try {
            // findById devuelve Optional; si se llama desde un contexto que garantiza existencia,
            // convertimos al vuelo en lugar de duplicar lógica
            Usuario u = repo.findById(99)
                    .orElseThrow(() -> new EntidadNoEncontradaException(99));
        } catch (EntidadNoEncontradaException e) {
            System.out.println("orElseThrow bridge: " + e.getMessage());
        }

        // El mismo patrón con mensaje distinto según contexto
        Usuario admin = repo.findById(1)
                .orElseThrow(() -> new IllegalStateException("El admin debe existir siempre"));
        System.out.println("Admin encontrado: " + admin.nombre());

        // ======================================
        // 4. DEMO — capa de servicio real
        // ======================================

        ServicioUsuario servicio = new ServicioUsuario(repo);

        // Operación que depende de un ID validado externamente → excepción si falla
        try {
            servicio.actualizarEmail(1, "nuevo@email.com");
            servicio.actualizarEmail(99, "otro@email.com"); // lanzará excepción
        } catch (EntidadNoEncontradaException e) {
            System.out.println("Servicio error: " + e.getMessage());
        }

        // Búsqueda con criterio opcional → Optional porque puede no haber resultados
        Optional<Usuario> porEmail = servicio.findByEmail("nuevo@email.com");
        System.out.println("findByEmail: " + porEmail.map(Usuario::nombre).orElse("no encontrado"));
    }

    // ======================================
    // INFRAESTRUCTURA
    // ======================================

    record Usuario(int id, String nombre, String email) {}

    static class EntidadNoEncontradaException extends RuntimeException {
        EntidadNoEncontradaException(int id) {
            super("Entidad no encontrada con id=" + id);
        }
    }

    static class UsuarioRepositorio {
        private final HashMap<Integer, Usuario> store = new HashMap<>();

        UsuarioRepositorio() {
            store.put(1, new Usuario(1, "Jorex",  "jorex@dev.com"));
            store.put(2, new Usuario(2, "Ana",    "ana@dev.com"));
        }

        // "puede no existir" → Optional
        Optional<Usuario> findById(int id) {
            return Optional.ofNullable(store.get(id));
        }

        // "debe existir" → excepción si no está
        Usuario getById(int id) {
            return findById(id)
                    .orElseThrow(() -> new EntidadNoEncontradaException(id));
        }
    }

    static class ServicioUsuario {
        private final UsuarioRepositorio repo;

        ServicioUsuario(UsuarioRepositorio repo) {
            this.repo = repo;
        }

        // Asume ID válido (viene de un endpoint con @PathVariable validado) → excepción si falla
        void actualizarEmail(int id, String nuevoEmail) {
            Usuario u = repo.getById(id); // lanza EntidadNoEncontradaException si no existe
            System.out.println("Email actualizado para " + u.nombre() + " → " + nuevoEmail);
        }

        // Búsqueda por criterio variable → Optional porque puede no haber match
        Optional<Usuario> findByEmail(String email) {
            return repo.findById(1) // simplificado: busca solo en los conocidos
                    .filter(u -> u.email().equalsIgnoreCase(email));
        }
    }
}
