import java.util.ArrayList;
import java.util.List;

public class Ejercicio1 {

    record Usuario(int id, String nombre, String email) {}

    interface RepositorioUsuarios {
        void guardar(Usuario u);
        boolean existe(String email);
    }

    interface ServicioEmail {
        void enviarBienvenida(String email, String nombre);
    }

    interface GeneradorInforme {
        String generar(List<Usuario> usuarios);
    }

    static class RepositorioMemoria implements RepositorioUsuarios {
        private final List<Usuario> store = new ArrayList<>();
        @Override public void guardar(Usuario u)         { store.add(u); }
        @Override public boolean existe(String email)    { return store.stream().anyMatch(u -> u.email().equals(email)); }
        List<Usuario> todos()                            { return store; }
    }

    static class EmailConsola implements ServicioEmail {
        @Override public void enviarBienvenida(String email, String nombre) {
            System.out.println("Email de bienvenida → " + email + " (Hola " + nombre + ")");
        }
    }

    static class GeneradorTexto implements GeneradorInforme {
        @Override public String generar(List<Usuario> usuarios) {
            var sb = new StringBuilder("=== Informe de Usuarios ===\n");
            usuarios.forEach(u -> sb.append("  ").append(u.id()).append(": ").append(u.nombre()).append("\n"));
            return sb.toString();
        }
    }

    static class GestorUsuarios {
        private final RepositorioMemoria repo;
        private final ServicioEmail email;
        private final GeneradorInforme informe;
        private int nextId = 1;

        GestorUsuarios(RepositorioMemoria repo, ServicioEmail email, GeneradorInforme informe) {
            this.repo = repo; this.email = email; this.informe = informe;
        }

        void registrar(String nombre, String correo) {
            if (repo.existe(correo)) { System.out.println("Email ya registrado: " + correo); return; }
            Usuario u = new Usuario(nextId++, nombre, correo);
            repo.guardar(u);
            email.enviarBienvenida(correo, nombre);
        }

        void imprimirInforme() { System.out.println(informe.generar(repo.todos())); }
    }

    public static void main(String[] args) {
        var repo    = new RepositorioMemoria();
        var gestor  = new GestorUsuarios(repo, new EmailConsola(), new GeneradorTexto());

        gestor.registrar("Ana García", "ana@example.com");
        gestor.registrar("Luis Pérez", "luis@example.com");
        gestor.registrar("Ana García", "ana@example.com"); // duplicado
        gestor.imprimirInforme();
    }
}
