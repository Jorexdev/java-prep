import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// Scopes de Spring beans: singleton, prototype y el problema de inyectar
// un prototype dentro de un singleton (el prototype no rota).
// Solución: ObjectProvider / lookup method — aquí simulado con Supplier.
public class ExpBeanScopes {

    // ── Tracking de instancias ────────────────────────────────────────────────

    static int instanceCounter = 0;

    // ── Beans ─────────────────────────────────────────────────────────────────

    // @Scope("singleton") — una sola instancia en todo el contexto
    static class AuditoriaService {
        final int id = ++instanceCounter;

        AuditoriaService() {
            System.out.println("  [AuditoriaService] instancia #" + id + " creada");
        }

        public void registrar(String evento) {
            System.out.println("  [Auditoria#" + id + "] " + evento);
        }
    }

    // @Scope("prototype") — nueva instancia en cada punto de inyección
    static class SolicitudContext {
        final int id = ++instanceCounter;
        String usuario;

        SolicitudContext() {
            System.out.println("  [SolicitudContext] instancia #" + id + " creada");
        }

        SolicitudContext conUsuario(String u) { this.usuario = u; return this; }

        @Override public String toString() { return "SolicitudContext#" + id + "(" + usuario + ")"; }
    }

    // ── El problema: singleton con prototype inyectado ────────────────────────

    // Este servicio es singleton, pero necesita un SolicitudContext fresco por llamada.
    // Si Spring inyecta el prototype una sola vez en el constructor, siempre es el mismo.
    static class PedidoServiceMal {
        // En Spring: @Autowired (prototype, pero inyectado una sola vez en el singleton)
        // → el prototype NO rota: todas las llamadas usan la misma instancia
        private final SolicitudContext contextoFijo;

        PedidoServiceMal(SolicitudContext ctx) {
            this.contextoFijo = ctx;
            System.out.println("  [PedidoServiceMal] singleton creado con " + ctx);
        }

        void procesarPedido(String usuario) {
            // Problema: contextoFijo.usuario es siempre el del primer llamador
            contextoFijo.usuario = usuario;
            System.out.println("  [MAL] procesando pedido para " + contextoFijo);
        }
    }

    // ── La solución: ObjectProvider / Supplier ────────────────────────────────

    // En Spring: @Autowired ObjectProvider<SolicitudContext> provider;
    // Aquí simulamos con Supplier<SolicitudContext> que la fábrica de beans inyecta.
    static class PedidoServiceBien {
        // En Spring: @Autowired private ObjectProvider<SolicitudContext> ctxProvider;
        private final Supplier<SolicitudContext> ctxProvider;  // equivale a ObjectProvider

        PedidoServiceBien(Supplier<SolicitudContext> ctxProvider) {
            this.ctxProvider = ctxProvider;
            System.out.println("  [PedidoServiceBien] singleton creado con Supplier<SolicitudContext>");
        }

        void procesarPedido(String usuario) {
            // ctxProvider.get() crea una NUEVA instancia en cada llamada — prototype correcto
            // En Spring: ctxProvider.getObject() o @Lookup method
            SolicitudContext ctx = ctxProvider.get();
            ctx.usuario = usuario;
            System.out.println("  [BIEN] procesando pedido para " + ctx);
        }
    }

    // ── Contenedor manual ─────────────────────────────────────────────────────

    static class BeanContainer {
        // Singletons: se crean una vez y se reutilizan
        private final Map<String, Object> singletons = new HashMap<>();
        // Prototypes: fábrica que crea nueva instancia en cada getBean()
        private final Map<String, Supplier<?>> prototypes = new HashMap<>();

        void registerSingleton(String name, Object bean) {
            singletons.put(name, bean);
            System.out.println("  [Container] singleton '" + name + "' registrado");
        }

        void registerPrototype(String name, Supplier<?> factory) {
            prototypes.put(name, factory);
            System.out.println("  [Container] prototype '" + name + "' registrado");
        }

        @SuppressWarnings("unchecked")
        <T> T getBean(String name) {
            if (singletons.containsKey(name)) {
                return (T) singletons.get(name);   // siempre la misma instancia
            }
            if (prototypes.containsKey(name)) {
                return (T) prototypes.get(name).get();  // nueva instancia
            }
            throw new IllegalArgumentException("Bean no encontrado: " + name);
        }

        // Expone el Supplier del prototype — útil para inyectar en singletons
        @SuppressWarnings("unchecked")
        <T> Supplier<T> getPrototypeProvider(String name) {
            Supplier<?> factory = prototypes.get(name);
            if (factory == null) throw new IllegalArgumentException("Prototype no encontrado: " + name);
            return () -> (T) factory.get();
        }
    }

    // ── Secciones demostrativas ───────────────────────────────────────────────

    // ── 1. SINGLETON ─────────────────────────────────────────────────────────
    static void demoSingleton(BeanContainer ctx) {
        System.out.println("\n=== 1. Singleton: siempre la misma instancia ===");
        AuditoriaService a1 = ctx.getBean("auditoria");
        AuditoriaService a2 = ctx.getBean("auditoria");
        System.out.println("  a1 == a2 : " + (a1 == a2));  // true
        a1.registrar("login de jorex");
        a2.registrar("pago procesado");
    }

    // ── 2. PROTOTYPE ────────────────────────────────────────────────────────
    static void demoPrototype(BeanContainer ctx) {
        System.out.println("\n=== 2. Prototype: nueva instancia en cada getBean() ===");
        SolicitudContext s1 = ctx.getBean("solicitud");
        SolicitudContext s2 = ctx.getBean("solicitud");
        System.out.println("  s1 == s2 : " + (s1 == s2));  // false
        System.out.println("  s1 = " + s1.conUsuario("jorex"));
        System.out.println("  s2 = " + s2.conUsuario("ana"));
    }

    // ── 3. PROBLEMA: PROTOTYPE EN SINGLETON ───────────────────────────────
    static void demoProblema(BeanContainer ctx) {
        System.out.println("\n=== 3. Problema: prototype inyectado en singleton (no rota) ===");
        // El singleton recibió UNA instancia de SolicitudContext en su construcción
        PedidoServiceMal servicio = ctx.getBean("pedidoServiceMal");
        servicio.procesarPedido("jorex");   // instancia #N, usuario=jorex
        servicio.procesarPedido("ana");     // MISMO instancia #N — debería ser nueva
        System.out.println("  PROBLEMA: ambas llamadas usaron la misma instancia de SolicitudContext.");
    }

    // ── 4. SOLUCIÓN: SUPPLIER / OBJECTPROVIDER ────────────────────────────
    static void demoSolucion(BeanContainer ctx) {
        System.out.println("\n=== 4. Solución: Supplier<SolicitudContext> (ObjectProvider) ===");
        PedidoServiceBien servicio = ctx.getBean("pedidoServiceBien");
        servicio.procesarPedido("jorex");   // nueva instancia
        servicio.procesarPedido("ana");     // OTRA nueva instancia
        System.out.println("  CORRECTO: cada llamada recibió su propio SolicitudContext.");
    }

    public static void main(String[] args) {
        System.out.println("=== Configurando contenedor ===");
        BeanContainer ctx = new BeanContainer();

        // Singletons
        ctx.registerSingleton("auditoria", new AuditoriaService());

        // Prototypes — fábrica que crea nueva instancia en cada get()
        ctx.registerPrototype("solicitud", SolicitudContext::new);

        // Singleton con prototype inyectado directamente (MAL)
        SolicitudContext ctxFijo = ctx.getBean("solicitud");   // una sola instancia
        ctx.registerSingleton("pedidoServiceMal", new PedidoServiceMal(ctxFijo));

        // Singleton con Supplier del prototype (BIEN)
        // En Spring: @Autowired ObjectProvider<SolicitudContext> ctxProvider
        Supplier<SolicitudContext> proveedor = ctx.getPrototypeProvider("solicitud");
        ctx.registerSingleton("pedidoServiceBien", new PedidoServiceBien(proveedor));

        demoSingleton(ctx);
        demoPrototype(ctx);
        demoProblema(ctx);
        demoSolucion(ctx);

        System.out.println("\n=== Resumen de scopes en Spring ===");
        System.out.println("  singleton   → 1 instancia por ApplicationContext (default)");
        System.out.println("  prototype   → nueva instancia en cada getBean() / @Autowired");
        System.out.println("  request     → 1 instancia por HTTP request (solo en web)");
        System.out.println("  session     → 1 instancia por HTTP session  (solo en web)");
        System.out.println("  Regla: prototype en singleton → usar ObjectProvider<T> o @Lookup");
    }
}
