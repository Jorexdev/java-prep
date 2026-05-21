import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio3 {

    static class ArgumentCaptor<T> {
        private T valor;

        void capturar(T v) { this.valor = v; }
        T getValue()       { return valor; }
    }

    interface AuditService {
        void log(String evento, String detalle, LocalDateTime timestamp);
    }

    static class MockAuditService implements AuditService {
        private final ArgumentCaptor<String> capEvento    = new ArgumentCaptor<>();
        private final ArgumentCaptor<String> capDetalle   = new ArgumentCaptor<>();
        private final ArgumentCaptor<LocalDateTime> capTs = new ArgumentCaptor<>();
        private int llamadas = 0;

        @Override
        public void log(String evento, String detalle, LocalDateTime timestamp) {
            capEvento.capturar(evento);
            capDetalle.capturar(detalle);
            capTs.capturar(timestamp);
            llamadas++;
        }

        ArgumentCaptor<String>        getCapEvento()  { return capEvento; }
        ArgumentCaptor<String>        getCapDetalle() { return capDetalle; }
        ArgumentCaptor<LocalDateTime> getCapTs()      { return capTs; }
        int getLlamadas()                             { return llamadas; }
    }

    static class UsuarioService {
        private final Map<Integer, String> usuarios = new HashMap<>();
        private final AuditService audit;

        UsuarioService(AuditService audit) { this.audit = audit; }

        void crear(int id, String nombre) { usuarios.put(id, nombre); }

        void eliminar(int id) {
            String nombre = usuarios.remove(id);
            if (nombre != null)
                audit.log("USUARIO_ELIMINADO", "id=" + id + ", nombre=" + nombre, LocalDateTime.now());
        }
    }

    static void assertEquals(Object expected, Object actual, String nombre) {
        if (expected.equals(actual)) {
            System.out.println("PASS: " + nombre);
        } else {
            System.out.println("FAIL: " + nombre + " — esperado <" + expected + "> pero fue <" + actual + ">");
        }
    }

    static void assertNotNull(Object o, String nombre) {
        if (o != null) {
            System.out.println("PASS: " + nombre);
        } else {
            System.out.println("FAIL: " + nombre + " — valor es null");
        }
    }

    public static void main(String[] args) {
        MockAuditService mockAudit = new MockAuditService();
        UsuarioService svc = new UsuarioService(mockAudit);

        svc.crear(7, "Mario");
        svc.eliminar(7);

        assertEquals(1, mockAudit.getLlamadas(), "log llamado exactamente una vez al eliminar");
        assertEquals("USUARIO_ELIMINADO", mockAudit.getCapEvento().getValue(), "evento capturado es USUARIO_ELIMINADO");
        assertEquals("id=7, nombre=Mario",  mockAudit.getCapDetalle().getValue(), "detalle capturado contiene id y nombre");
        assertNotNull(mockAudit.getCapTs().getValue(), "timestamp capturado no es null");

        LocalDateTime ts = mockAudit.getCapTs().getValue();
        boolean reciente = ts.isAfter(LocalDateTime.now().minusSeconds(5));
        if (reciente) {
            System.out.println("PASS: timestamp capturado es reciente");
        } else {
            System.out.println("FAIL: timestamp capturado no es reciente");
        }
    }
}
