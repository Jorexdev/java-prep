import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Ejercicio 1 (Difícil) — Remote config source
// Simula descarga con 50ms delay, integración en chain y refresh()
public class Ejercicio1 {

    interface PropertySource {
        String getName();
        Optional<String> get(String key);
    }

    static class MapPropertySource implements PropertySource {
        private final String name;
        private final Map<String, String> props;

        MapPropertySource(String name, Map<String, String> props) {
            this.name = name;
            this.props = Map.copyOf(props);
        }

        @Override public String getName() { return name; }
        @Override public Optional<String> get(String key) { return Optional.ofNullable(props.get(key)); }
    }

    /**
     * Simula una fuente de configuración remota (Config Server, Consul, etcd...).
     * Cada carga/refresh tarda ~50ms (simulado con Thread.sleep).
     */
    static class RemoteConfigSource implements PropertySource {
        private volatile Map<String, String> cachedProps = new HashMap<>();
        private final String serverUrl;
        private int fetchCount = 0;

        RemoteConfigSource(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        /** Carga inicial: descarga las propiedades del servidor remoto */
        public void load() {
            long start = System.currentTimeMillis();
            System.out.println("[Remote] Conectando a " + serverUrl + "...");
            try {
                Thread.sleep(50); // simula latencia de red
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            fetchCount++;
            // Simula la respuesta del servidor
            Map<String, String> fetched = new HashMap<>();
            fetched.put("remote.feature.enabled", "true");
            fetched.put("remote.max.connections", "100");
            fetched.put("db.url", "jdbc:postgresql://remote-host/mydb");
            fetched.put("fetch.count", String.valueOf(fetchCount));
            this.cachedProps = fetched;

            long elapsed = System.currentTimeMillis() - start;
            System.out.println("[Remote] Cargadas " + fetched.size()
                    + " propiedades en " + elapsed + "ms (fetch #" + fetchCount + ")");
        }

        /** Recarga las propiedades del servidor remoto */
        public void refresh() {
            System.out.println("[Remote] Refrescando configuración...");
            long start = System.currentTimeMillis();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            fetchCount++;
            Map<String, String> fetched = new HashMap<>(cachedProps);
            // Simula que el servidor devuelve valores actualizados
            fetched.put("remote.max.connections", String.valueOf(100 + fetchCount * 10));
            fetched.put("fetch.count", String.valueOf(fetchCount));
            this.cachedProps = fetched;

            long elapsed = System.currentTimeMillis() - start;
            System.out.println("[Remote] Refrescadas " + fetched.size()
                    + " propiedades en " + elapsed + "ms (fetch #" + fetchCount + ")");
        }

        @Override public String getName() { return "remoteConfig(" + serverUrl + ")"; }
        @Override public Optional<String> get(String key) { return Optional.ofNullable(cachedProps.get(key)); }
    }

    static class PropertySourceChain {
        private final List<PropertySource> sources = new ArrayList<>();

        public void addSource(PropertySource source) { sources.add(source); }

        public Optional<String> get(String key) {
            for (int i = sources.size() - 1; i >= 0; i--) {
                Optional<String> val = sources.get(i).get(key);
                if (val.isPresent()) return val;
            }
            return Optional.empty();
        }

        public String getSource(String key) {
            for (int i = sources.size() - 1; i >= 0; i--) {
                if (sources.get(i).get(key).isPresent()) return sources.get(i).getName();
            }
            return "ninguna";
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Remote config source ===");
        System.out.println();

        Map<String, String> localProps = new HashMap<>();
        localProps.put("app.name", "mi-app");
        localProps.put("db.url", "jdbc:h2:mem:localdb"); // será sobreescrito por remote

        RemoteConfigSource remote = new RemoteConfigSource("https://config-server.example.com");

        System.out.println("--- Carga inicial ---");
        long t0 = System.currentTimeMillis();
        remote.load();
        System.out.println("Tiempo total de carga: " + (System.currentTimeMillis() - t0) + "ms");
        System.out.println();

        PropertySourceChain chain = new PropertySourceChain();
        chain.addSource(new MapPropertySource("local", localProps));
        chain.addSource(remote); // remote tiene mayor prioridad

        System.out.println("--- Consulta de propiedades ---");
        String[] keys = {"app.name", "db.url", "remote.feature.enabled", "remote.max.connections", "fetch.count"};
        for (String key : keys) {
            System.out.printf("  %-30s = %-20s [%s]%n",
                    key, chain.get(key).orElse("N/A"), chain.getSource(key));
        }

        System.out.println();
        System.out.println("--- Refresh ---");
        remote.refresh();
        System.out.println();

        System.out.println("--- Propiedades después del refresh ---");
        for (String key : keys) {
            System.out.printf("  %-30s = %s%n", key, chain.get(key).orElse("N/A"));
        }
    }
}
