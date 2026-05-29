import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// ConfigurationProperties con listas y mapas anidados + refresh dinamico simulado

public class Ejercicio5 {

    // ====== Modelo de ConfigurationProperties complejo ======

    static class DataSourceProperties {
        String url;
        String username;
        String password;
        int maxPoolSize;
        List<String> schemas;            // lista anidada
        Map<String, String> extraParams; // mapa anidado

        @Override public String toString() {
            return String.format("DataSource{url=%s, user=%s, pool=%d, schemas=%s, extras=%s}",
                    url, username, maxPoolSize, schemas, extraParams);
        }
    }

    static class FeatureFlags {
        boolean darkMode;
        boolean betaApi;
        Map<String, Boolean> experiments; // mapa de feature flags

        @Override public String toString() {
            return String.format("Features{darkMode=%b, betaApi=%b, experiments=%s}",
                    darkMode, betaApi, experiments);
        }
    }

    static class ServiceEndpoint {
        String host;
        int port;
        int timeoutMs;

        @Override public String toString() {
            return host + ":" + port + "(t=" + timeoutMs + "ms)";
        }
    }

    static class AppProperties {
        String name;
        String version;
        List<String> allowedOrigins;              // lista de strings
        List<ServiceEndpoint> serviceEndpoints;   // lista de objetos
        Map<String, String> customHeaders;        // mapa string->string
        Map<String, List<String>> rolePermissions; // mapa string->lista
        DataSourceProperties dataSource;
        FeatureFlags features;

        @Override public String toString() {
            return String.format(
                "AppProperties{name=%s, version=%s}%n" +
                "  origins=%s%n" +
                "  endpoints=%s%n" +
                "  headers=%s%n" +
                "  roles=%s%n" +
                "  %s%n" +
                "  %s",
                name, version, allowedOrigins, serviceEndpoints,
                customHeaders, rolePermissions, dataSource, features);
        }
    }

    // ====== Binder que soporta listas y mapas anidados ======

    static class ComplexBinder {
        private final Map<String, String> props;

        ComplexBinder(Map<String, String> props) {
            this.props = new HashMap<>(props);
        }

        AppProperties bind() {
            AppProperties app = new AppProperties();
            app.name    = get("app.name", "default-app");
            app.version = get("app.version", "1.0.0");

            // Lista simple: app.allowedOrigins[0], app.allowedOrigins[1], ...
            app.allowedOrigins = bindList("app.allowedOrigins");

            // Lista de objetos: app.serviceEndpoints[0].host, app.serviceEndpoints[0].port, ...
            app.serviceEndpoints = bindEndpointList("app.serviceEndpoints");

            // Mapa simple: app.customHeaders.X-App-Name, app.customHeaders.X-Version
            app.customHeaders = bindMapString("app.customHeaders");

            // Mapa de listas: app.rolePermissions.admin[0], app.rolePermissions.user[0], ...
            app.rolePermissions = bindMapOfLists("app.rolePermissions");

            // Objeto anidado DataSource
            app.dataSource = new DataSourceProperties();
            app.dataSource.url         = get("app.dataSource.url", null);
            app.dataSource.username    = get("app.dataSource.username", "root");
            app.dataSource.password    = get("app.dataSource.password", "");
            app.dataSource.maxPoolSize = getInt("app.dataSource.maxPoolSize", 10);
            app.dataSource.schemas     = bindList("app.dataSource.schemas");
            app.dataSource.extraParams = bindMapString("app.dataSource.extraParams");

            // FeatureFlags
            app.features = new FeatureFlags();
            app.features.darkMode  = getBool("app.features.darkMode", false);
            app.features.betaApi   = getBool("app.features.betaApi", false);
            app.features.experiments = bindMapBoolean("app.features.experiments");

            return app;
        }

        // Binds app.key[0], app.key[1], ... -> List<String>
        List<String> bindList(String prefix) {
            List<String> list = new ArrayList<>();
            for (int i = 0; ; i++) {
                String v = props.get(prefix + "[" + i + "]");
                if (v == null) break;
                list.add(v);
            }
            return list;
        }

        // Binds prefix[i].host, prefix[i].port -> List<ServiceEndpoint>
        List<ServiceEndpoint> bindEndpointList(String prefix) {
            List<ServiceEndpoint> list = new ArrayList<>();
            for (int i = 0; ; i++) {
                String host = props.get(prefix + "[" + i + "].host");
                if (host == null) break;
                ServiceEndpoint ep = new ServiceEndpoint();
                ep.host      = host;
                ep.port      = getInt(prefix + "[" + i + "].port", 80);
                ep.timeoutMs = getInt(prefix + "[" + i + "].timeoutMs", 5000);
                list.add(ep);
            }
            return list;
        }

        // Binds prefix.key -> Map<String,String> (busca todas las claves con ese prefijo)
        Map<String, String> bindMapString(String prefix) {
            Map<String, String> map = new LinkedHashMap<>();
            String dotPrefix = prefix + ".";
            for (Map.Entry<String, String> e : props.entrySet()) {
                if (e.getKey().startsWith(dotPrefix)) {
                    String subKey = e.getKey().substring(dotPrefix.length());
                    if (!subKey.contains(".") && !subKey.contains("[")) {
                        map.put(subKey, e.getValue());
                    }
                }
            }
            return map;
        }

        // Binds prefix.key -> Map<String,Boolean>
        Map<String, Boolean> bindMapBoolean(String prefix) {
            Map<String, Boolean> map = new LinkedHashMap<>();
            String dotPrefix = prefix + ".";
            for (Map.Entry<String, String> e : props.entrySet()) {
                if (e.getKey().startsWith(dotPrefix)) {
                    String subKey = e.getKey().substring(dotPrefix.length());
                    if (!subKey.contains(".") && !subKey.contains("[")) {
                        map.put(subKey, Boolean.parseBoolean(e.getValue()));
                    }
                }
            }
            return map;
        }

        // Binds prefix.role[0], prefix.role[1] -> Map<String,List<String>>
        Map<String, List<String>> bindMapOfLists(String prefix) {
            Map<String, List<String>> result = new LinkedHashMap<>();
            String dotPrefix = prefix + ".";
            // Detectar roles presentes
            Set<String> roles = new LinkedHashSet<>();
            for (String key : props.keySet()) {
                if (key.startsWith(dotPrefix)) {
                    String rest = key.substring(dotPrefix.length());
                    int bracketIdx = rest.indexOf('[');
                    if (bracketIdx > 0) roles.add(rest.substring(0, bracketIdx));
                }
            }
            for (String role : roles) {
                result.put(role, bindList(prefix + "." + role));
            }
            return result;
        }

        private String get(String key, String def) {
            return props.getOrDefault(key, def);
        }
        private int getInt(String key, int def) {
            String v = props.get(key);
            return v == null ? def : Integer.parseInt(v);
        }
        private boolean getBool(String key, boolean def) {
            String v = props.get(key);
            return v == null ? def : Boolean.parseBoolean(v);
        }
    }

    // ====== Refresh dinamico simulado ======

    static class RefreshableConfig {
        private volatile AppProperties current;
        private final AtomicInteger refreshCount = new AtomicInteger(0);
        private final List<Runnable> refreshListeners = new CopyOnWriteArrayList<>();

        void load(Map<String, String> props) {
            current = new ComplexBinder(props).bind();
            int n = refreshCount.incrementAndGet();
            System.out.printf("  [Config] carga #%d completada%n", n);
            refreshListeners.forEach(Runnable::run);
        }

        AppProperties get() { return current; }

        void onRefresh(Runnable listener) { refreshListeners.add(listener); }

        int getRefreshCount() { return refreshCount.get(); }
    }

    // ====== DEMO ======

    static Map<String, String> buildProps(String environment) {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("app.name", "mi-plataforma-" + environment);
        p.put("app.version", environment.equals("prod") ? "2.1.0" : "2.1.0-SNAPSHOT");

        // Lista simple
        p.put("app.allowedOrigins[0]", "https://app.example.com");
        p.put("app.allowedOrigins[1]", "https://admin.example.com");
        if (!environment.equals("prod")) p.put("app.allowedOrigins[2]", "http://localhost:3000");

        // Lista de objetos
        p.put("app.serviceEndpoints[0].host", "auth." + environment + ".internal");
        p.put("app.serviceEndpoints[0].port", "8081");
        p.put("app.serviceEndpoints[0].timeoutMs", "3000");
        p.put("app.serviceEndpoints[1].host", "catalog." + environment + ".internal");
        p.put("app.serviceEndpoints[1].port", "8082");
        p.put("app.serviceEndpoints[1].timeoutMs", "5000");

        // Mapa simple
        p.put("app.customHeaders.X-App-Name", "mi-plataforma");
        p.put("app.customHeaders.X-Environment", environment);
        p.put("app.customHeaders.X-Version", p.get("app.version"));

        // Mapa de listas
        p.put("app.rolePermissions.admin[0]", "READ");
        p.put("app.rolePermissions.admin[1]", "WRITE");
        p.put("app.rolePermissions.admin[2]", "DELETE");
        p.put("app.rolePermissions.user[0]", "READ");
        p.put("app.rolePermissions.viewer[0]", "READ");

        // DataSource con mapa y lista anidados
        p.put("app.dataSource.url", "jdbc:postgresql://" + environment + "-db:5432/app");
        p.put("app.dataSource.username", "appuser");
        p.put("app.dataSource.password", "***");
        p.put("app.dataSource.maxPoolSize", environment.equals("prod") ? "50" : "5");
        p.put("app.dataSource.schemas[0]", "public");
        p.put("app.dataSource.schemas[1]", "audit");
        p.put("app.dataSource.extraParams.ssl", "true");
        p.put("app.dataSource.extraParams.connectTimeout", "10");

        // Features con mapa de booleanos
        p.put("app.features.darkMode", "true");
        p.put("app.features.betaApi", environment.equals("prod") ? "false" : "true");
        p.put("app.features.experiments.newCheckout", environment.equals("prod") ? "false" : "true");
        p.put("app.features.experiments.aiRecommendations", "true");
        p.put("app.features.experiments.newSearch", "false");

        return p;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ConfigurationProperties: listas/mapas anidados + refresh dinamico ===");
        System.out.println();

        RefreshableConfig config = new RefreshableConfig();

        // Listener de refresh (simula @RefreshScope)
        config.onRefresh(() -> System.out.println("  [RefreshScope] beans invalidados y recreados"));

        // Carga inicial (environment = dev)
        System.out.println("[ Carga inicial: dev ]");
        config.load(buildProps("dev"));
        AppProperties dev = config.get();
        System.out.println(dev);
        System.out.println();

        // Refresh con nuevas propiedades (environment = prod)
        System.out.println("[ Refresh #1: cambiando a prod ]");
        config.load(buildProps("prod"));
        AppProperties prod = config.get();
        System.out.println(prod);
        System.out.println();

        // Mostrar diferencias entre dev y prod
        System.out.println("[ Diferencias dev vs prod ]");
        System.out.printf("  allowedOrigins:   dev=%d | prod=%d%n",
                dev.allowedOrigins.size(), prod.allowedOrigins.size());
        System.out.printf("  dataSource.pool:  dev=%d | prod=%d%n",
                dev.dataSource.maxPoolSize, prod.dataSource.maxPoolSize);
        System.out.printf("  features.betaApi: dev=%b | prod=%b%n",
                dev.features.betaApi, prod.features.betaApi);
        System.out.printf("  experiments.newCheckout: dev=%b | prod=%b%n",
                dev.features.experiments.get("newCheckout"),
                prod.features.experiments.get("newCheckout"));
        System.out.println();

        System.out.printf("Total refreshes realizados: %d%n", config.getRefreshCount());
        System.out.println();

        System.out.println("=== Conclusion ===");
        System.out.println("El binder detecta listas por patron prefix[N] y mapas por prefix.clave.");
        System.out.println("Mapas de listas combinan ambos patrones: prefix.role[N].");
        System.out.println("El refresh dinamico simula @RefreshScope de Spring Cloud Config.");
        System.out.println("En Spring Boot: @ConfigurationProperties(prefix='app') + @EnableConfigurationProperties.");
    }
}
