import java.util.*;

// Spring Cloud Config — configuración centralizada y versionada para microservicios.
//
// Problema que resuelve:
//   Con application.properties local, cada servicio lleva su propia config.
//   En un sistema con 20 microservicios en 3 entornos (dev/staging/prod)
//   hay 60 ficheros de config difíciles de sincronizar y auditar.
//   Config Server los centraliza en un repo Git → versionado, historial, PR reviews.
//
// Flujo:
//   1. Config Server expone las propiedades vía REST desde un repo Git.
//   2. Config Client las descarga al arrancar (bootstrap context).
//   3. Cambios en Git → push → /actuator/refresh → @RefreshScope recarga sin reiniciar.
//
// URL pattern del Config Server:
//   /{application}/{profile}/{label}
//   - application → spring.application.name del cliente
//   - profile      → spring.profiles.active (default si no se especifica)
//   - label        → branch o tag Git (main por defecto)
//   Ejemplo: GET /order-service/prod/main
//
// Bootstrap context:
//   El cliente necesita conocer la URL del Config Server ANTES de que arranque
//   el ApplicationContext. Lo lee del bootstrap.yml (Spring Cloud 2020+:
//   spring.config.import=configserver:http://...). Así las propiedades del
//   servidor están disponibles desde el inicio del ciclo de vida del contexto.
//
// spring.cloud.config.fail-fast=true:
//   Si el Config Server no está disponible al arrancar → la aplicación falla
//   inmediatamente en lugar de arrancar con config incompleta o por defecto.
//   Recomendado en producción.
//
// @RefreshScope:
//   Los beans anotados con @RefreshScope se destruyen y re-crean cuando se llama
//   a POST /actuator/refresh → útil para feature flags, conexiones, URL de terceros.
//   Beans @Configuration no se refrescan automáticamente; usar @RefreshScope con cuidado.

// ── Propiedad ─────────────────────────────────────────────────────────────────

record Property(String key, String value, String source) {
    @Override
    public String toString() {
        return String.format("  %-30s = %-20s  [from: %s]", key, value, source);
    }
}

// ── ConfigServer ──────────────────────────────────────────────────────────────

// Simula el Config Server de Spring Cloud Config.
// En producción: @EnableConfigServer + spring.cloud.config.server.git.uri
// La spec de la respuesta: PropertySource[] (nombre + propiedades)
class ConfigServer {
    // Repositorio Git simulado: app → profile → Map<key, value>
    private final Map<String, Map<String, Map<String, String>>> gitRepo = new LinkedHashMap<>();
    private final String serverUrl;

    ConfigServer(String serverUrl) { this.serverUrl = serverUrl; }

    // Simula añadir un fichero de propiedades al repo Git (commit en Git real).
    // Convención de nombre: {application}-{profile}.yml o {application}.yml (todas)
    ConfigServer addConfig(String application, String profile, Map<String, String> props) {
        gitRepo.computeIfAbsent(application, k -> new LinkedHashMap<>())
               .put(profile, new LinkedHashMap<>(props));
        return this;
    }

    // GET /{application}/{profile}/{label}
    // Responde con las propiedades fundidas (merge): application-default + application-profile
    // La jerarquía de Spring Config: perfil específico sobreescribe al default.
    List<Property> fetch(String application, String profile, String label) {
        System.out.println("  [ConfigServer] GET /" + application + "/" + profile + "/" + label);
        List<Property> result = new ArrayList<>();

        // 1. Propiedades comunes (application.yml — para todos los servicios)
        Map<String, String> common = gitRepo.getOrDefault("application", Map.of())
                                            .getOrDefault("default", Map.of());
        common.forEach((k, v) -> result.add(new Property(k, v, "application.yml")));

        // 2. Propiedades del servicio en perfil default
        Map<String, String> appDefault = gitRepo.getOrDefault(application, Map.of())
                                                 .getOrDefault("default", Map.of());
        appDefault.forEach((k, v) -> result.add(new Property(k, v, application + ".yml")));

        // 3. Propiedades del servicio en el perfil solicitado (mayor prioridad)
        if (!"default".equals(profile)) {
            Map<String, String> appProfile = gitRepo.getOrDefault(application, Map.of())
                                                      .getOrDefault(profile, Map.of());
            appProfile.forEach((k, v) -> result.add(new Property(k, v, application + "-" + profile + ".yml")));
        }

        System.out.println("  [ConfigServer] " + result.size() + " propiedades devueltas");
        return result;
    }

    String serverUrl() { return serverUrl; }
}

// ── ConfigClient ──────────────────────────────────────────────────────────────

// Simula el Config Client (cualquier microservicio con spring-cloud-starter-config).
// Configuración mínima en bootstrap.yml:
//   spring.application.name: order-service
//   spring.config.import: configserver:http://config-server:8888
//   spring.cloud.config.profile: prod
//   spring.cloud.config.fail-fast: true
//
// El cliente hace el fetch en el bootstrap phase → antes que @Bean, @Value, etc.
class ConfigClient {
    private final String appName;
    private final String profile;
    private final String label;
    private final ConfigServer server;
    private final boolean failFast;

    // Propiedades locales (application.properties del cliente).
    // Son la base; el servidor las SOBREESCRIBE si tienen la misma clave.
    private final Map<String, String> localProps = new LinkedHashMap<>();
    // Propiedades obtenidas del servidor (tras fetch)
    private final Map<String, String> mergedProps = new LinkedHashMap<>();

    private boolean loaded = false;
    // Beans marcados con @RefreshScope se recargan al llamar /actuator/refresh
    private final Set<String> refreshScopeBeans = new LinkedHashSet<>();

    ConfigClient(String appName, String profile, String label,
                 ConfigServer server, boolean failFast) {
        this.appName  = appName;
        this.profile  = profile;
        this.label    = label;
        this.server   = server;
        this.failFast = failFast;
    }

    ConfigClient localProperty(String key, String value) {
        localProps.put(key, value);
        return this;
    }

    ConfigClient registerRefreshScopeBean(String beanName) {
        refreshScopeBeans.add(beanName);
        return this;
    }

    // Simula el arranque del bootstrap context: carga config antes del ApplicationContext.
    // En producción: spring.config.import=configserver:http://...
    void bootstrap() {
        System.out.println("  [" + appName + "] Bootstrap phase — fetching config server...");
        System.out.println("  [" + appName + "] Configuración: " + server.serverUrl()
                + "/" + appName + "/" + profile + "/" + label);
        System.out.println("  [" + appName + "] fail-fast=" + failFast);

        List<Property> serverProps;
        try {
            serverProps = server.fetch(appName, profile, label);
        } catch (Exception e) {
            if (failFast) {
                throw new RuntimeException("[" + appName + "] FATAL: Config Server no disponible. fail-fast=true");
            }
            System.out.println("  [" + appName + "] WARN: Config Server no disponible, usando config local");
            serverProps = List.of();
        }

        // Merge: locales primero, servidor sobreescribe
        mergedProps.putAll(localProps);
        serverProps.forEach(p -> mergedProps.put(p.key(), p.value()));

        loaded = true;
        System.out.println("  [" + appName + "] Config cargada: " + mergedProps.size() + " propiedades");
    }

    // Simula POST /actuator/refresh: recarga las propiedades y destruye @RefreshScope beans
    void refresh() {
        if (!loaded) { System.out.println("  [" + appName + "] No inicializado"); return; }
        System.out.println("  [" + appName + "] /actuator/refresh → re-fetching...");
        List<Property> fresh = server.fetch(appName, profile, label);
        mergedProps.putAll(localProps);
        fresh.forEach(p -> mergedProps.put(p.key(), p.value()));
        // Beans con @RefreshScope se destruyen para que Spring los recree con la nueva config
        System.out.println("  [" + appName + "] Destruyendo @RefreshScope beans: " + refreshScopeBeans);
        System.out.println("  [" + appName + "] Refresh completado");
    }

    String getProperty(String key) {
        return mergedProps.getOrDefault(key, "(no definida)");
    }

    void printAll() {
        System.out.println("  Propiedades finales de " + appName + " (" + profile + "):");
        mergedProps.forEach((k, v) -> System.out.printf("    %-30s = %s%n", k, v));
    }
}

// ── Main ──────────────────────────────────────────────────────────────────────

public class ExpSpringCloudConfig {

    public static void main(String[] args) {

        System.out.println("═".repeat(64));
        System.out.println("  ExpSpringCloudConfig — Config Server + Config Client");
        System.out.println("═".repeat(64));

        // ── Configurar el Config Server ──────────────────────────────────────
        ConfigServer configServer = new ConfigServer("http://config-server:8888");

        // Propiedades comunes a todos los servicios (application.yml en Git)
        configServer.addConfig("application", "default", Map.of(
            "management.endpoints.web.exposure.include", "health,info,refresh",
            "logging.level.root",                        "WARN"
        ));

        // order-service: propiedades comunes (todas las entornos)
        configServer.addConfig("order-service", "default", Map.of(
            "server.port",                               "8081",
            "spring.datasource.url",                     "jdbc:postgresql://localhost/orders_dev",
            "feature.new-checkout",                      "false"
        ));

        // order-service: sobreescrituras para producción
        configServer.addConfig("order-service", "prod", Map.of(
            "spring.datasource.url",                     "jdbc:postgresql://prod-db/orders",
            "spring.datasource.hikari.maximum-pool-size", "20",
            "feature.new-checkout",                      "true",
            "logging.level.root",                        "ERROR"
        ));

        // inventory-service: propiedades default
        configServer.addConfig("inventory-service", "default", Map.of(
            "server.port",                               "8082",
            "spring.datasource.url",                     "jdbc:postgresql://localhost/inventory"
        ));

        // ── Caso 1: order-service en prod ────────────────────────────────────
        System.out.println("\n── Caso 1: order-service arrancando en perfil prod ───────────");
        ConfigClient orderProd = new ConfigClient(
            "order-service", "prod", "main",
            configServer, true          // fail-fast=true
        );
        orderProd
            .localProperty("spring.application.name", "order-service")  // propiedad local
            .localProperty("custom.local-only",        "valor-local")    // no sobreescrita
            .registerRefreshScopeBean("CheckoutService")
            .registerRefreshScopeBean("FeatureFlagConfig");

        orderProd.bootstrap();
        System.out.println();
        orderProd.printAll();

        System.out.println();
        System.out.println("  Propiedad clave: spring.datasource.url = "
                + orderProd.getProperty("spring.datasource.url"));
        System.out.println("  Feature flag:    feature.new-checkout  = "
                + orderProd.getProperty("feature.new-checkout"));
        System.out.println("  Solo local:      custom.local-only     = "
                + orderProd.getProperty("custom.local-only"));

        // ── Caso 2: order-service en dev (perfil default) ────────────────────
        System.out.println("\n── Caso 2: order-service en dev (perfil default) ────────────");
        ConfigClient orderDev = new ConfigClient(
            "order-service", "default", "main",
            configServer, false         // fail-fast=false: arranca aunque no haya server
        );
        orderDev.bootstrap();
        System.out.println("  DB URL: " + orderDev.getProperty("spring.datasource.url"));
        System.out.println("  Pool:   " + orderDev.getProperty("spring.datasource.hikari.maximum-pool-size"));

        // ── Caso 3: @RefreshScope — recargar sin reiniciar ───────────────────
        System.out.println("\n── Caso 3: @RefreshScope + /actuator/refresh ─────────────────");
        System.out.println("  Antes del refresh, feature.new-checkout = "
                + orderProd.getProperty("feature.new-checkout"));

        // Simulamos un cambio en el repo Git: feature flag desactivado
        configServer.addConfig("order-service", "prod", Map.of(
            "spring.datasource.url",                     "jdbc:postgresql://prod-db/orders",
            "spring.datasource.hikari.maximum-pool-size", "20",
            "feature.new-checkout",                      "false",  // cambiado en Git
            "logging.level.root",                        "ERROR"
        ));

        orderProd.refresh();
        System.out.println("  Después del refresh, feature.new-checkout = "
                + orderProd.getProperty("feature.new-checkout"));

        // ── Caso 4: fail-fast cuando el servidor no está disponible ───────────
        System.out.println("\n── Caso 4: fail-fast=true con servidor no disponible ────────");
        ConfigServer unreachable = new ConfigServer("http://config-server-down:8888") {
            @Override
            List<Property> fetch(String application, String profile, String label) {
                throw new RuntimeException("Connection refused");
            }
        };
        ConfigClient failFastClient = new ConfigClient(
            "payment-service", "prod", "main",
            unreachable, true
        );
        try {
            failFastClient.bootstrap();
        } catch (RuntimeException e) {
            System.out.println("  " + e.getMessage());
        }

        // ── Resumen ──────────────────────────────────────────────────────────
        System.out.println("\n── Resumen ───────────────────────────────────────────────────");
        System.out.println("  Config Server: sirve config desde Git → versión, historial, PR");
        System.out.println("  URL pattern:   /{application}/{profile}/{label}");
        System.out.println("  Bootstrap:     carga config ANTES del ApplicationContext");
        System.out.println("  spring.config.import=configserver:http://... (Boot 2.4+)");
        System.out.println("  fail-fast=true → falla al arrancar si el server no responde");
        System.out.println("  @RefreshScope  → bean se destruye/recrea al llamar /refresh");
        System.out.println("  Merge order:   application.yml < {app}.yml < {app}-{profile}.yml");
        System.out.println("  vs local:      Config Server centraliza y versiona; local es por servicio");
        System.out.println("═".repeat(64));
    }
}
