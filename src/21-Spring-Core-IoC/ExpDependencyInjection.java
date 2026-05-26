import java.lang.reflect.Field;

// DI styles: constructor (preferred) vs setter vs field injection.
// Spring supports all three; this demo shows the trade-offs with plain Java.
public class ExpDependencyInjection {

    interface RepoUsuario {
        String buscar(int id);
    }

    static class RepoUsuarioImpl implements RepoUsuario {
        @Override
        public String buscar(int id) {
            return "usuario-" + id;
        }
    }

    // ── Constructor injection ──────────────────────────────────────────────────
    // @Component
    static class ServicioConstructor {
        // final: la dependencia es inmutable después de construir — imposible olvidar inyectarla
        private final RepoUsuario repo;

        // @Autowired (implícito en Spring cuando hay un solo constructor)
        ServicioConstructor(RepoUsuario repo) {
            this.repo = repo;
        }

        public String obtener(int id) { return repo.buscar(id); }
    }

    // ── Setter injection ──────────────────────────────────────────────────────
    // @Component
    static class ServicioSetter {
        // No es final: puede ser null hasta que el setter sea llamado
        private RepoUsuario repo;

        // @Autowired
        public void setRepo(RepoUsuario repo) {
            this.repo = repo;
        }

        public String obtener(int id) {
            if (repo == null) throw new IllegalStateException("repo no inyectado");
            return repo.buscar(id);
        }
    }

    // ── Field injection (simulado vía reflexión) ──────────────────────────────
    // @Component
    static class ServicioCampo {
        // @Autowired — Spring escribe directamente en el campo privado
        private RepoUsuario repo;   // Spring inyecta esto por reflexión

        public String obtener(int id) {
            if (repo == null) throw new IllegalStateException("repo no inyectado");
            return repo.buscar(id);
        }
    }

    // Inyecta un valor en un campo privado por reflexión, tal como hace Spring
    static void inyectarCampo(Object target, String nombre, Object valor) throws Exception {
        Field f = target.getClass().getDeclaredField(nombre);
        f.setAccessible(true);
        f.set(target, valor);
    }

    public static void main(String[] args) throws Exception {
        RepoUsuario repo = new RepoUsuarioImpl();

        System.out.println("=== Constructor injection ===");
        // Spring llama al constructor con los beans resueltos del contexto
        ServicioConstructor sc = new ServicioConstructor(repo);
        System.out.println(sc.obtener(1));
        // sc.repo no puede ser null — el compilador lo garantiza (final)

        System.out.println("\n=== Setter injection ===");
        ServicioSetter ss = new ServicioSetter();
        // Spring llama al setter después de instanciar con el constructor por defecto
        ss.setRepo(repo);
        System.out.println(ss.obtener(1));
        // Problema: si olvidamos llamar al setter, NPE en tiempo de ejecución

        System.out.println("\n=== Field injection (reflexión) ===");
        ServicioCampo sf = new ServicioCampo();
        inyectarCampo(sf, "repo", repo);
        System.out.println(sf.obtener(1));
        // Problema: imposible hacer final, el campo queda oculto → dificulta tests unitarios

        System.out.println("\n=== Por qué preferir constructor injection ===");
        System.out.println("Constructor → final, fail-fast, testable sin contenedor");
        System.out.println("Setter      → opcional/reconfigurable, pero no fail-fast");
        System.out.println("Campo       → conciso, pero oculta dependencias y rompe encapsulación");

        // Demostración de fail-fast: constructor impide crear un objeto inválido
        try {
            new ServicioConstructor(null); // se podría añadir Objects.requireNonNull
        } catch (Exception e) {
            System.out.println("Constructor detectó null: " + e.getMessage());
        }
    }
}
