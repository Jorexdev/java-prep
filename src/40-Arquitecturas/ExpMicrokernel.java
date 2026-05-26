import java.util.*;

/**
 * Simulación de Microkernel (plugin) architecture con Java puro.
 *
 * Componentes:
 *  - Core: funcionalidad mínima (request routing básico)
 *  - Plugin: interfaz con name(), execute(), dependencies()
 *  - PluginRegistry: carga plugins, resuelve dependencias (topological sort), hot-reload
 *  - RequestContext: datos del request compartidos entre plugins y core
 */
public class ExpMicrokernel {

    // ─────────────────────────────────────────────
    // REQUEST CONTEXT — datos compartidos
    // ─────────────────────────────────────────────

    static class RequestContext {
        private final String requestId;
        private final String path;
        private final Map<String, Object> attributes = new LinkedHashMap<>();
        private final List<String> processingLog = new ArrayList<>();
        private int statusCode = 200;

        RequestContext(String requestId, String path) {
            this.requestId = requestId;
            this.path = path;
        }

        void setAttribute(String key, Object value) { attributes.put(key, value); }
        Object getAttribute(String key) { return attributes.get(key); }
        void log(String entry) { processingLog.add(entry); }
        void setStatus(int code) { this.statusCode = code; }

        String requestId() { return requestId; }
        String path()      { return path; }
        int statusCode()   { return statusCode; }
        List<String> processingLog() { return Collections.unmodifiableList(processingLog); }

        @Override
        public String toString() {
            return String.format("Request{id='%s', path='%s', status=%d}",
                    requestId, path, statusCode);
        }
    }

    // ─────────────────────────────────────────────
    // PLUGIN INTERFACE
    // ─────────────────────────────────────────────

    interface Plugin {
        String name();
        // Devuelve los nombres de los plugins de los que depende (ejecutar antes)
        List<String> dependencies();
        // Ejecuta la lógica del plugin. Devuelve false para cortar la cadena.
        boolean execute(RequestContext ctx, Core core);
    }

    // ─────────────────────────────────────────────
    // CORE — funcionalidad mínima del sistema
    // ─────────────────────────────────────────────

    static class Core {
        private final Map<String, String> routes = new LinkedHashMap<>();
        private final PluginRegistry pluginRegistry;

        Core(PluginRegistry pluginRegistry) {
            this.pluginRegistry = pluginRegistry;
        }

        void registerRoute(String path, String handler) {
            routes.put(path, handler);
        }

        // Procesar un request: ejecutar plugins en orden de dependencia, luego el handler
        RequestContext process(String path) {
            String reqId = "REQ-" + System.nanoTime() % 10000;
            RequestContext ctx = new RequestContext(reqId, path);
            System.out.printf("%n[Core] Procesando: %s%n", ctx);

            // Ejecutar plugins en orden resuelto
            for (Plugin plugin : pluginRegistry.getOrderedPlugins()) {
                System.out.printf("  [Core] Ejecutando plugin: '%s'%n", plugin.name());
                boolean continueProcessing = plugin.execute(ctx, this);
                if (!continueProcessing) {
                    System.out.printf("  [Core] Plugin '%s' cortó la cadena (status=%d)%n",
                            plugin.name(), ctx.statusCode());
                    return ctx;
                }
            }

            // Handler del core
            String handler = routes.getOrDefault(path, "default-handler");
            System.out.printf("  [Core] Handler: '%s'%n", handler);
            ctx.log("Core: handled by " + handler);
            return ctx;
        }

        // Permite a plugins hacer lookups de rutas
        boolean hasRoute(String path) { return routes.containsKey(path); }
    }

    // ─────────────────────────────────────────────
    // PLUGIN REGISTRY — carga, ordena (topological) y gestiona plugins
    // ─────────────────────────────────────────────

    static class PluginRegistry {
        private final Map<String, Plugin> plugins = new LinkedHashMap<>();
        private List<Plugin> orderedPlugins = new ArrayList<>();

        void register(Plugin plugin) {
            plugins.put(plugin.name(), plugin);
            System.out.printf("  [Registry] Plugin registrado: '%s' (deps=%s)%n",
                    plugin.name(), plugin.dependencies());
            rebuildOrder();
        }

        // Simula hot-reload: desregistrar un plugin sin reiniciar el sistema
        void unregister(String pluginName) {
            plugins.remove(pluginName);
            System.out.printf("  [Registry] Plugin desregistrado: '%s'%n", pluginName);
            rebuildOrder();
        }

        List<Plugin> getOrderedPlugins() { return Collections.unmodifiableList(orderedPlugins); }

        // Topological sort (Kahn's algorithm) para respetar dependencias
        private void rebuildOrder() {
            Map<String, Integer> inDegree = new LinkedHashMap<>();
            Map<String, List<String>> dependents = new LinkedHashMap<>();

            // Inicializar solo con plugins registrados
            for (String name : plugins.keySet()) {
                inDegree.put(name, 0);
                dependents.put(name, new ArrayList<>());
            }

            // Construir grafo de dependencias
            for (Plugin p : plugins.values()) {
                for (String dep : p.dependencies()) {
                    if (plugins.containsKey(dep)) {
                        // dep debe ejecutarse antes que p → dep → p
                        dependents.get(dep).add(p.name());
                        inDegree.merge(p.name(), 1, Integer::sum);
                    }
                }
            }

            // BFS desde nodos sin dependencias
            Queue<String> queue = new LinkedList<>();
            inDegree.entrySet().stream()
                    .filter(e -> e.getValue() == 0)
                    .forEach(e -> queue.add(e.getKey()));

            List<Plugin> ordered = new ArrayList<>();
            while (!queue.isEmpty()) {
                String current = queue.poll();
                Plugin p = plugins.get(current);
                if (p != null) ordered.add(p);
                for (String dependent : dependents.getOrDefault(current, Collections.emptyList())) {
                    int newDegree = inDegree.merge(dependent, -1, Integer::sum);
                    if (newDegree == 0) queue.add(dependent);
                }
            }

            this.orderedPlugins = ordered;
            System.out.printf("  [Registry] Orden de ejecución: %s%n",
                    ordered.stream().map(Plugin::name).toList());
        }
    }

    // ─────────────────────────────────────────────
    // PLUGINS CONCRETOS
    // ─────────────────────────────────────────────

    // Plugin de logging — sin dependencias, primero en ejecutarse
    static class LoggingPlugin implements Plugin {
        @Override
        public String name() { return "logging"; }

        @Override
        public List<String> dependencies() { return Collections.emptyList(); }

        @Override
        public boolean execute(RequestContext ctx, Core core) {
            System.out.printf("    [LoggingPlugin] → %s%n", ctx.requestId());
            ctx.log("LoggingPlugin: request logged");
            ctx.setAttribute("startTime", System.nanoTime());
            return true; // continuar
        }
    }

    // Plugin de autenticación — depende de logging (para que el log incluya el auth result)
    static class AuthPlugin implements Plugin {
        private final Set<String> publicPaths = Set.of("/public", "/health");

        @Override
        public String name() { return "auth"; }

        @Override
        public List<String> dependencies() { return List.of("logging"); }

        @Override
        public boolean execute(RequestContext ctx, Core core) {
            if (publicPaths.contains(ctx.path())) {
                System.out.printf("    [AuthPlugin] Path público '%s' — skip auth%n", ctx.path());
                ctx.setAttribute("authenticated", true);
                return true;
            }
            // Simular: si el token está en el contexto → autenticado
            Object token = ctx.getAttribute("auth-token");
            if (token == null) {
                System.out.printf("    [AuthPlugin] Sin token → 401 Unauthorized%n");
                ctx.setStatus(401);
                ctx.log("AuthPlugin: unauthorized");
                return false; // cortar la cadena
            }
            System.out.printf("    [AuthPlugin] Token válido: '%s'%n", token);
            ctx.setAttribute("authenticated", true);
            ctx.log("AuthPlugin: authenticated");
            return true;
        }
    }

    // Plugin de caché — depende de auth (solo cachear requests autenticados)
    static class CachePlugin implements Plugin {
        private final Map<String, String> cache = new HashMap<>();

        @Override
        public String name() { return "cache"; }

        @Override
        public List<String> dependencies() { return List.of("auth"); }

        @Override
        public boolean execute(RequestContext ctx, Core core) {
            if (!Boolean.TRUE.equals(ctx.getAttribute("authenticated"))) {
                return true; // no cachear requests no autenticados
            }
            if (cache.containsKey(ctx.path())) {
                System.out.printf("    [CachePlugin] HIT para '%s'%n", ctx.path());
                ctx.setAttribute("cached-response", cache.get(ctx.path()));
                ctx.setAttribute("cache-hit", true);
                ctx.log("CachePlugin: cache hit");
                // Cortar la cadena: responder desde caché sin llegar al handler
                return false;
            }
            System.out.printf("    [CachePlugin] MISS para '%s' — continuando%n", ctx.path());
            // Guardar en caché para la próxima llamada (en producción lo haría un response filter)
            cache.put(ctx.path(), "cached-response-for-" + ctx.path());
            ctx.log("CachePlugin: cache miss, cacheado para próxima vez");
            return true;
        }
    }

    // Plugin de rate limiting — depende de auth para limitar por usuario
    static class RateLimitPlugin implements Plugin {
        private final Map<String, Integer> counts = new HashMap<>();
        private final int maxRequests;

        RateLimitPlugin(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        @Override
        public String name() { return "rate-limit"; }

        @Override
        public List<String> dependencies() { return List.of("auth"); }

        @Override
        public boolean execute(RequestContext ctx, Core core) {
            String key = String.valueOf(ctx.getAttribute("auth-token"));
            int count = counts.merge(key, 1, Integer::sum);
            System.out.printf("    [RateLimitPlugin] token='%s' requests=%d/%d%n", key, count, maxRequests);
            if (count > maxRequests) {
                ctx.setStatus(429);
                ctx.log("RateLimitPlugin: rate limit exceeded");
                return false;
            }
            ctx.log("RateLimitPlugin: OK");
            return true;
        }
    }

    // ─────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("═".repeat(65));
        System.out.println("  MICROKERNEL (PLUGIN) ARCHITECTURE — Java puro");
        System.out.println("═".repeat(65));

        PluginRegistry registry = new PluginRegistry();
        Core core = new Core(registry);
        core.registerRoute("/api/orders", "OrderHandler");
        core.registerRoute("/public",     "PublicHandler");
        core.registerRoute("/health",     "HealthHandler");

        // ── Cargar plugins (el registry resuelve el orden por dependencias) ──
        System.out.println("\n── Cargando plugins ──");
        registry.register(new LoggingPlugin());
        registry.register(new AuthPlugin());
        registry.register(new CachePlugin());
        registry.register(new RateLimitPlugin(3));

        // ── Demo 1: Request autenticado ────────────────────────────────
        System.out.println("\n══ DEMO 1: Request autenticado ══");
        RequestContext ctx1 = new RequestContext("REQ-1", "/api/orders");
        ctx1.setAttribute("auth-token", "token-abc");
        // Ejecutar el pipeline manualmente para mostrar el flujo completo
        for (Plugin p : registry.getOrderedPlugins()) {
            if (!p.execute(ctx1, core)) break;
        }
        System.out.printf("  Resultado: %s | log=%s%n", ctx1, ctx1.processingLog());

        // ── Demo 2: Request sin token → auth corta la cadena ──────────
        System.out.println("\n══ DEMO 2: Request sin autenticación ══");
        RequestContext ctx2 = new RequestContext("REQ-2", "/api/orders");
        for (Plugin p : registry.getOrderedPlugins()) {
            if (!p.execute(ctx2, core)) break;
        }
        System.out.printf("  Resultado: status=%d | log=%s%n", ctx2.statusCode(), ctx2.processingLog());

        // ── Demo 3: Path público → AuthPlugin hace skip ───────────────
        System.out.println("\n══ DEMO 3: Path público (sin auth requerida) ══");
        RequestContext ctx3 = new RequestContext("REQ-3", "/public");
        for (Plugin p : registry.getOrderedPlugins()) {
            if (!p.execute(ctx3, core)) break;
        }
        System.out.printf("  Resultado: status=%d%n", ctx3.statusCode());

        // ── Demo 4: Caché — segundo request al mismo path ─────────────
        System.out.println("\n══ DEMO 4: Cache hit en segundo request ══");
        RequestContext ctx4 = new RequestContext("REQ-4", "/api/orders");
        ctx4.setAttribute("auth-token", "token-abc");
        for (Plugin p : registry.getOrderedPlugins()) {
            if (!p.execute(ctx4, core)) break;
        }
        System.out.printf("  Cache hit: %b | cached: %s%n",
                Boolean.TRUE.equals(ctx4.getAttribute("cache-hit")),
                ctx4.getAttribute("cached-response"));

        // ── Demo 5: Hot-reload — añadir plugin en caliente ────────────
        System.out.println("\n══ DEMO 5: Hot-reload — añadir plugin en tiempo de ejecución ══");
        registry.register(new Plugin() {
            @Override public String name() { return "metrics"; }
            @Override public List<String> dependencies() { return List.of("logging"); }
            @Override public boolean execute(RequestContext ctx, Core core) {
                System.out.printf("    [MetricsPlugin] Registrando métrica para '%s'%n", ctx.path());
                ctx.log("MetricsPlugin: recorded");
                return true;
            }
        });

        System.out.println("  Ejecutando request tras hot-reload del MetricsPlugin:");
        RequestContext ctx5 = new RequestContext("REQ-5", "/api/orders");
        ctx5.setAttribute("auth-token", "token-xyz");
        for (Plugin p : registry.getOrderedPlugins()) {
            if (!p.execute(ctx5, core)) break;
        }

        // ── Demo 6: Desregistrar plugin ───────────────────────────────
        System.out.println("\n══ DEMO 6: Desregistrar CachePlugin en caliente ══");
        registry.unregister("cache");
        System.out.println("  Orden tras desregistrar cache: " +
                registry.getOrderedPlugins().stream().map(Plugin::name).toList());

        System.out.println("\n" + "═".repeat(65));
        System.out.println("  RESUMEN MICROKERNEL");
        System.out.println("═".repeat(65));
        System.out.println("  Core: mínimo (routing, ciclo de vida del request)");
        System.out.println("  Plugins: añaden autenticación, caché, logging, métricas...");
        System.out.println("  Dependencias: topological sort garantiza el orden correcto");
        System.out.println("  Hot-reload: añadir/quitar plugins sin reiniciar el sistema");
        System.out.println("  Ejemplos reales: Eclipse IDE, Jenkins, Webpack, Kafka Connect");
        System.out.println("═".repeat(65));
    }
}
