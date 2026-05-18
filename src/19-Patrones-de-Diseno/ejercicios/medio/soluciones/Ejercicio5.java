public class Ejercicio5 {

    record Solicitud(String token, String rol, int bodySize) {}

    static abstract class Manejador {
        private Manejador siguiente;

        Manejador setSiguiente(Manejador sig) { this.siguiente = sig; return sig; }

        final void manejar(Solicitud s) {
            if (!procesar(s)) return;
            if (siguiente != null) siguiente.manejar(s);
        }

        abstract boolean procesar(Solicitud s);
    }

    static class ValidadorAutenticacion extends Manejador {
        @Override public boolean procesar(Solicitud s) {
            if (s.token() == null || s.token().isBlank()) {
                System.out.println("RECHAZADO: Token invalido");
                return false;
            }
            System.out.println("Autenticacion OK");
            return true;
        }
    }

    static class ValidadorAutorizacion extends Manejador {
        @Override public boolean procesar(Solicitud s) {
            if (!"ADMIN".equals(s.rol()) && !"USER".equals(s.rol())) {
                System.out.println("RECHAZADO: Rol desconocido → " + s.rol());
                return false;
            }
            System.out.println("Autorizacion OK (rol: " + s.rol() + ")");
            return true;
        }
    }

    static class ValidadorTamano extends Manejador {
        private static final int MAX = 1024;
        @Override public boolean procesar(Solicitud s) {
            if (s.bodySize() > MAX) {
                System.out.println("RECHAZADO: Body demasiado grande (" + s.bodySize() + " > " + MAX + ")");
                return false;
            }
            System.out.println("Tamano OK (" + s.bodySize() + " bytes)");
            return true;
        }
    }

    static class ProcesadorFinal extends Manejador {
        @Override public boolean procesar(Solicitud s) {
            System.out.println("PROCESADO: Solicitud aceptada");
            return true;
        }
    }

    static Manejador buildChain() {
        ValidadorAutenticacion auth = new ValidadorAutenticacion();
        auth.setSiguiente(new ValidadorAutorizacion())
            .setSiguiente(new ValidadorTamano())
            .setSiguiente(new ProcesadorFinal());
        return auth;
    }

    public static void main(String[] args) {
        Manejador chain = buildChain();

        System.out.println("--- Sin token ---");
        chain.manejar(new Solicitud(null, "ADMIN", 100));

        System.out.println("\n--- Rol invalido ---");
        chain.manejar(new Solicitud("tok123", "GUEST", 100));

        System.out.println("\n--- Body grande ---");
        chain.manejar(new Solicitud("tok123", "USER", 2000));

        System.out.println("\n--- Valida ---");
        chain.manejar(new Solicitud("tok123", "ADMIN", 512));
    }
}
