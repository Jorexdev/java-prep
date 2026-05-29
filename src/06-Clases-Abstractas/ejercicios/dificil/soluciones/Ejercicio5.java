import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Mini framework de plugins con clases abstractas, prioridades y contexto compartido

class Contexto {
    private final Map<String, Object> datos = new HashMap<>();

    public void set(String clave, Object valor) { datos.put(clave, valor); }

    public Object get(String clave) { return datos.get(clave); }

    public Map<String, Object> getDatos() { return datos; }
}

abstract class Plugin {
    public abstract String nombre();
    public abstract String version();
    public abstract void ejecutar(Contexto ctx);
    public int prioridad() { return 0; } // puede sobreescribirse
}

class PluginRegistry {
    private final List<Plugin> plugins = new ArrayList<>();

    public void registrar(Plugin p) {
        plugins.add(p);
        System.out.println("[Registry] Registrado: " + p.nombre() + " v" + p.version() +
                           " (prioridad=" + p.prioridad() + ")");
    }

    public void ejecutarTodos(Contexto ctx) {
        System.out.println("\n[Registry] Ejecutando " + plugins.size() +
                           " plugins en orden de prioridad...");
        plugins.stream()
               .sorted(Comparator.comparingInt(Plugin::prioridad).reversed())
               .forEach(p -> {
                   System.out.println("\n  >> Plugin: " + p.nombre());
                   p.ejecutar(ctx);
               });
    }
}

class PluginLogger extends Plugin {
    @Override public String nombre()  { return "PluginLogger"; }
    @Override public String version() { return "1.0"; }
    @Override public int prioridad()  { return 10; }

    @Override
    public void ejecutar(Contexto ctx) {
        ctx.set("log", "Ejecucion iniciada");
        ctx.set("entrada", "hola mundo");
        System.out.println("  Logger: contexto inicializado con 'entrada'='" + ctx.get("entrada") + "'");
    }
}

class PluginTransformador extends Plugin {
    @Override public String nombre()  { return "PluginTransformador"; }
    @Override public String version() { return "2.1"; }
    @Override public int prioridad()  { return 5; }

    @Override
    public void ejecutar(Contexto ctx) {
        Object entrada = ctx.get("entrada");
        if (entrada instanceof String s) {
            String transformado = s.toUpperCase();
            ctx.set("entrada", transformado);
            System.out.println("  Transformador: 'entrada' → '" + transformado + "'");
        } else {
            System.out.println("  Transformador: no hay 'entrada' en el contexto, saltando.");
        }
    }
}

class PluginAuditoria extends Plugin {
    @Override public String nombre()  { return "PluginAuditoria"; }
    @Override public String version() { return "1.3"; }
    @Override public int prioridad()  { return 1; }

    @Override
    public void ejecutar(Contexto ctx) {
        System.out.println("  Auditoria — estado final del contexto:");
        ctx.getDatos().forEach((k, v) -> System.out.println("    " + k + " = " + v));
    }
}

public class Ejercicio5 {
    public static void main(String[] args) {
        System.out.println("=== Mini Framework de Plugins ===\n");

        PluginRegistry registry = new PluginRegistry();

        // Registro desordenado para demostrar que la prioridad manda
        registry.registrar(new PluginAuditoria());
        registry.registrar(new PluginTransformador());
        registry.registrar(new PluginLogger());

        Contexto ctx = new Contexto();
        registry.ejecutarTodos(ctx);

        System.out.println("\n=== Resultado final ===");
        System.out.println("Orden de ejecución garantizado: Logger(10) → Transformador(5) → Auditoria(1)");
        System.out.println("Valor final de 'entrada': " + ctx.get("entrada"));
    }
}
