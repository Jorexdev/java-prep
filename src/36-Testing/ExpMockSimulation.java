import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Simulación del patrón Mockito con Java puro (sin dependencias externas).
 *
 * Demuestra:
 * - Interfaz RepositorioUsuarios con buscarPorEmail(String)
 * - Mock manual que registra llamadas y permite configurar respuestas
 * - when().thenReturn() implementado a mano
 * - verify() con conteo de invocaciones
 * - ArgumentCaptor simulado
 *
 * Ejecutar: java -cp target/classes ExpMockSimulation
 */
public class ExpMockSimulation {

    // ── Modelo ───────────────────────────────────────────────────────────────

    record Usuario(Long id, String nombre, String email) {}

    // ── Interfaz a mockear ───────────────────────────────────────────────────

    interface RepositorioUsuarios {
        Optional<Usuario> buscarPorEmail(String email);
        void guardar(Usuario usuario);
        void eliminar(Long id);
    }

    // ── Implementación del servicio bajo prueba ──────────────────────────────

    static class ServicioUsuarios {

        private final RepositorioUsuarios repositorio;

        ServicioUsuarios(RepositorioUsuarios repositorio) {
            this.repositorio = repositorio;
        }

        public String obtenerNombrePorEmail(String email) {
            return repositorio.buscarPorEmail(email)
                .map(Usuario::nombre)
                .orElse("Usuario no encontrado");
        }

        public void registrar(String nombre, String email) {
            Optional<Usuario> existente = repositorio.buscarPorEmail(email);
            if (existente.isPresent()) {
                throw new IllegalStateException("El email ya está registrado: " + email);
            }
            repositorio.guardar(new Usuario(null, nombre, email));
        }
    }

    // ── Mock manual — simula Mockito ─────────────────────────────────────────

    /**
     * Mock manual de RepositorioUsuarios.
     *
     * Equivale a:  RepositorioUsuarios mock = Mockito.mock(RepositorioUsuarios.class);
     */
    static class MockRepositorioUsuarios implements RepositorioUsuarios {

        // Registro de llamadas: método → lista de argumentos recibidos
        private final Map<String, List<Object[]>> llamadas = new HashMap<>();

        // Respuestas configuradas: clave "método:arg" → valor a devolver
        private final Map<String, Object> respuestas = new HashMap<>();

        // ── Configuración del mock ───────────────────────────────────────────

        /**
         * Simula:  when(mock.buscarPorEmail("ana@email.com")).thenReturn(Optional.of(usuario));
         */
        @SuppressWarnings("unchecked")
        public <T> MockRepositorioUsuarios cuando(String metodo, Object argumento, T respuesta) {
            String clave = metodo + ":" + argumento;
            respuestas.put(clave, respuesta);
            return this;
        }

        // ── Implementación de la interfaz ────────────────────────────────────

        @Override
        @SuppressWarnings("unchecked")
        public Optional<Usuario> buscarPorEmail(String email) {
            registrarLlamada("buscarPorEmail", email);
            String clave = "buscarPorEmail:" + email;
            if (respuestas.containsKey(clave)) {
                return (Optional<Usuario>) respuestas.get(clave);
            }
            return Optional.empty();   // comportamiento por defecto — igual que Mockito
        }

        @Override
        public void guardar(Usuario usuario) {
            registrarLlamada("guardar", usuario);
        }

        @Override
        public void eliminar(Long id) {
            registrarLlamada("eliminar", id);
        }

        // ── Mecanismo de registro ────────────────────────────────────────────

        private void registrarLlamada(String metodo, Object... args) {
            llamadas.computeIfAbsent(metodo, k -> new ArrayList<>()).add(args);
        }

        // ── Verificaciones — simulan Mockito.verify() ────────────────────────

        /**
         * Simula:  verify(mock).buscarPorEmail("ana@email.com");
         */
        public void verify(String metodo, int veceesEsperadas) {
            int vecesReales = llamadas.getOrDefault(metodo, List.of()).size();
            if (vecesReales == veceesEsperadas) {
                System.out.printf("  PASS  verify(%s, times(%d))%n", metodo, veceesEsperadas);
            } else {
                System.out.printf("  FAIL  verify(%s) — esperado %d invocaciones, fue %d%n",
                    metodo, veceesEsperadas, vecesReales);
            }
        }

        /**
         * Simula:  verify(mock, never()).eliminar(any());
         */
        public void verifyNever(String metodo) {
            verify(metodo, 0);
        }

        /**
         * Simula ArgumentCaptor — devuelve los argumentos capturados de la última llamada.
         */
        public Object[] capturarUltimaLlamada(String metodo) {
            List<Object[]> invocaciones = llamadas.getOrDefault(metodo, List.of());
            if (invocaciones.isEmpty()) {
                return new Object[0];
            }
            return invocaciones.get(invocaciones.size() - 1);
        }
    }

    // ── Main — ejecuta los tests ─────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  ExpMockSimulation — Patrón Mockito con Java puro");
        System.out.println("═══════════════════════════════════════════════");

        // ── Test 1: buscar usuario existente ────────────────────────────────
        System.out.println("\n── Test 1: obtenerNombrePorEmail con usuario existente");

        MockRepositorioUsuarios mock = new MockRepositorioUsuarios();
        Usuario ana = new Usuario(1L, "Ana García", "ana@email.com");

        // when(mock.buscarPorEmail("ana@email.com")).thenReturn(Optional.of(ana))
        mock.cuando("buscarPorEmail", "ana@email.com", Optional.of(ana));

        ServicioUsuarios servicio = new ServicioUsuarios(mock);
        String nombre = servicio.obtenerNombrePorEmail("ana@email.com");

        System.out.println("  Nombre obtenido: " + nombre);
        System.out.println("  " + ("Ana García".equals(nombre) ? "PASS" : "FAIL")
            + "  nombre debe ser Ana García");

        // verify(mock, times(1)).buscarPorEmail(...)
        mock.verify("buscarPorEmail", 1);

        // ── Test 2: buscar usuario inexistente ───────────────────────────────
        System.out.println("\n── Test 2: obtenerNombrePorEmail con usuario inexistente");

        MockRepositorioUsuarios mock2 = new MockRepositorioUsuarios();
        // sin configurar when() → devuelve Optional.empty() por defecto
        ServicioUsuarios servicio2 = new ServicioUsuarios(mock2);

        String resultado = servicio2.obtenerNombrePorEmail("fantasma@email.com");
        System.out.println("  Resultado: " + resultado);
        System.out.println("  " + ("Usuario no encontrado".equals(resultado) ? "PASS" : "FAIL")
            + "  debe retornar 'Usuario no encontrado'");
        mock2.verify("buscarPorEmail", 1);

        // ── Test 3: registrar nuevo usuario llama a guardar ──────────────────
        System.out.println("\n── Test 3: registrar usuario nuevo");

        MockRepositorioUsuarios mock3 = new MockRepositorioUsuarios();
        // email no existe → buscarPorEmail devuelve empty → guardar debe ser llamado
        ServicioUsuarios servicio3 = new ServicioUsuarios(mock3);
        servicio3.registrar("Carlos", "carlos@email.com");

        mock3.verify("buscarPorEmail", 1);
        mock3.verify("guardar", 1);
        mock3.verifyNever("eliminar");

        // ArgumentCaptor: inspeccionamos qué Usuario se pasó a guardar
        Object[] args3 = mock3.capturarUltimaLlamada("guardar");
        if (args3.length > 0 && args3[0] instanceof Usuario u) {
            System.out.println("  ArgumentCaptor — usuario guardado: " + u);
            System.out.println("  " + ("Carlos".equals(u.nombre()) ? "PASS" : "FAIL")
                + "  nombre del usuario guardado debe ser Carlos");
        }

        // ── Test 4: registrar email duplicado lanza excepción ────────────────
        System.out.println("\n── Test 4: registrar email ya existente");

        MockRepositorioUsuarios mock4 = new MockRepositorioUsuarios();
        mock4.cuando("buscarPorEmail", "ana@email.com", Optional.of(ana));
        ServicioUsuarios servicio4 = new ServicioUsuarios(mock4);

        try {
            servicio4.registrar("Otro Ana", "ana@email.com");
            System.out.println("  FAIL  debería haber lanzado IllegalStateException");
        } catch (IllegalStateException e) {
            System.out.println("  PASS  IllegalStateException lanzada: " + e.getMessage());
        }

        // guardar NO debe haberse llamado
        mock4.verifyNever("guardar");

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("  Fin de la simulación Mockito");
        System.out.println("═══════════════════════════════════════════════");
    }
}
