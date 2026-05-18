import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Ejercicio2 {

    interface ServicioArchivos {
        String leer(String ruta);
        void escribir(String ruta, String contenido);
    }

    static class ServicioArchivosReal implements ServicioArchivos {
        private final Map<String, String> disco = new HashMap<>();

        @Override public String leer(String ruta) {
            System.out.println("[Real] Leyendo: " + ruta);
            return disco.getOrDefault(ruta, "(vacio)");
        }
        @Override public void escribir(String ruta, String contenido) {
            System.out.println("[Real] Escribiendo: " + ruta);
            disco.put(ruta, contenido);
        }
    }

    static class ProxyCache implements ServicioArchivos {
        private final ServicioArchivos servicio;
        private final Map<String, String> cache = new HashMap<>();

        ProxyCache(ServicioArchivos servicio) { this.servicio = servicio; }

        @Override public String leer(String ruta) {
            if (cache.containsKey(ruta)) { System.out.println("[Cache] HIT: " + ruta); return cache.get(ruta); }
            String v = servicio.leer(ruta);
            cache.put(ruta, v);
            return v;
        }
        @Override public void escribir(String ruta, String contenido) {
            cache.remove(ruta);
            servicio.escribir(ruta, contenido);
        }
    }

    static class ProxySeguridad implements ServicioArchivos {
        private final ServicioArchivos servicio;
        private final String usuario;
        private static final Set<String> ESCRITORES = Set.of("admin", "editor");

        ProxySeguridad(ServicioArchivos servicio, String usuario) {
            this.servicio = servicio; this.usuario = usuario;
        }
        @Override public String leer(String ruta) { return servicio.leer(ruta); }
        @Override public void escribir(String ruta, String contenido) {
            if (!ESCRITORES.contains(usuario))
                throw new SecurityException("'" + usuario + "' sin permiso de escritura");
            servicio.escribir(ruta, contenido);
        }
    }

    public static void main(String[] args) {
        ServicioArchivos cadena = new ProxySeguridad(new ProxyCache(new ServicioArchivosReal()), "admin");

        cadena.escribir("/etc/config.txt", "timeout=30");
        System.out.println(cadena.leer("/etc/config.txt")); // desde disco
        System.out.println(cadena.leer("/etc/config.txt")); // desde cache

        ServicioArchivos sinPermiso = new ProxySeguridad(new ProxyCache(new ServicioArchivosReal()), "invitado");
        try {
            sinPermiso.escribir("/etc/config.txt", "hack");
        } catch (SecurityException e) {
            System.out.println("Bloqueado: " + e.getMessage());
        }
        System.out.println(sinPermiso.leer("/etc/config.txt")); // lectura OK
    }
}
