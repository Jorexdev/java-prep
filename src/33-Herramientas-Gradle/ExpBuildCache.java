import java.util.*;

// ===== Tarea con soporte de caché =====

class CacheableTask {
    private final String name;
    private final String action;
    private String inputHash;   // hash de los inputs de la tarea

    public CacheableTask(String name, String action) {
        this.name   = name;
        this.action = action;
    }

    public String getName()      { return name; }
    public String getInputHash() { return inputHash; }

    public void setInputHash(String hash) { this.inputHash = hash; }

    // Ejecutar la tarea y producir su output (simulado como String)
    public String execute() {
        System.out.println("  [EXECUTE] :" + name + " — " + action);
        return "output-of-" + name + "-" + inputHash;
    }
}

// ===== Caché local =====

class LocalBuildCache {
    private final String owner;
    private final Map<String, String> store = new LinkedHashMap<>(); // hash → output

    public LocalBuildCache(String owner) { this.owner = owner; }

    public boolean has(String key)  { return store.containsKey(key); }

    public String get(String key) {
        System.out.println("  [LOCAL HIT]  " + owner + " — clave: " + key);
        return store.get(key);
    }

    public void put(String key, String output) {
        store.put(key, output);
        System.out.println("  [LOCAL STORE] " + owner + " — clave: " + key);
    }
}

// ===== Caché remota (simula un servidor compartido) =====

class RemoteBuildCache {
    private final Map<String, String> store = new LinkedHashMap<>();

    public boolean has(String key)  { return store.containsKey(key); }

    public String get(String key) {
        System.out.println("  [REMOTE HIT]  — clave: " + key);
        return store.get(key);
    }

    public void put(String key, String output) {
        store.put(key, output);
        System.out.println("  [REMOTE STORE] — clave: " + key);
    }
}

// ===== Motor de caché combinado local + remota =====

class BuildCacheEngine {
    private final String developer;
    private final LocalBuildCache  local;
    private final RemoteBuildCache remote;

    public BuildCacheEngine(String developer, LocalBuildCache local, RemoteBuildCache remote) {
        this.developer = developer;
        this.local     = local;
        this.remote    = remote;
    }

    public String runTask(CacheableTask task) {
        String key = task.getInputHash();
        System.out.println("\n  [" + developer + "] ejecutando :" + task.getName()
                + " (input-hash=" + key + ")");

        // 1. Buscar en caché local
        if (local.has(key)) {
            return local.get(key);
        }

        // 2. Buscar en caché remota → si hay hit, poblar local
        if (remote.has(key)) {
            String output = remote.get(key);
            local.put(key, output);  // populate local from remote
            return output;
        }

        // 3. Miss total → ejecutar + guardar en ambas
        String output = task.execute();
        local.put(key, output);
        remote.put(key, output);
        return output;
    }
}

public class ExpBuildCache {

    public static void main(String[] args) {

        // ─── Infraestructura compartida ───────────────────────────────────
        RemoteBuildCache remoteCache = new RemoteBuildCache();

        LocalBuildCache localDev1 = new LocalBuildCache("developer-1");
        LocalBuildCache localDev2 = new LocalBuildCache("developer-2");

        BuildCacheEngine engine1 = new BuildCacheEngine("developer-1", localDev1, remoteCache);
        BuildCacheEngine engine2 = new BuildCacheEngine("developer-2", localDev2, remoteCache);

        // ─── Tareas con sus hashes de input ───────────────────────────────
        CacheableTask compileJava   = new CacheableTask("compileJava",   "Compilando src/main/java");
        CacheableTask processRes    = new CacheableTask("processResources", "Copiando resources");
        CacheableTask jar           = new CacheableTask("jar",           "Empaquetando .jar");

        compileJava.setInputHash("abc123");
        processRes.setInputHash("def456");
        jar.setInputHash("ghi789");

        System.out.println("════════════════════════════════════════");
        System.out.println(" Developer 1 — primer run");
        System.out.println("════════════════════════════════════════");
        // Primer developer: local vacío, remota vacía → todo ejecuta y se almacena
        engine1.runTask(compileJava);
        engine1.runTask(processRes);
        engine1.runTask(jar);

        System.out.println("\n════════════════════════════════════════");
        System.out.println(" Developer 2 — mismo commit, mismos inputs");
        System.out.println("════════════════════════════════════════");
        // Segundo developer: local vacío, remota TIENE outputs → hits remotos
        CacheableTask compileJava2 = new CacheableTask("compileJava",   "Compilando src/main/java");
        CacheableTask processRes2  = new CacheableTask("processResources", "Copiando resources");
        CacheableTask jar2         = new CacheableTask("jar",           "Empaquetando .jar");
        compileJava2.setInputHash("abc123");  // mismo hash → hit remoto
        processRes2.setInputHash("def456");
        jar2.setInputHash("ghi789");

        engine2.runTask(compileJava2);
        engine2.runTask(processRes2);
        engine2.runTask(jar2);

        System.out.println("\n════════════════════════════════════════");
        System.out.println(" Developer 1 — un archivo modificado");
        System.out.println("════════════════════════════════════════");
        // compileJava tiene nuevo hash (código fuente cambió), resto igual
        CacheableTask compileChanged = new CacheableTask("compileJava", "Compilando src/main/java");
        CacheableTask processRes3    = new CacheableTask("processResources", "Copiando resources");
        CacheableTask jar3           = new CacheableTask("jar",           "Empaquetando .jar");

        compileChanged.setInputHash("abc999");  // NUEVO hash → miss → ejecuta
        processRes3.setInputHash("def456");     // sin cambios → local hit
        jar3.setInputHash("ghi789");            // sin cambios → local hit

        engine1.runTask(compileChanged);  // miss → ejecuta + almacena
        engine1.runTask(processRes3);     // hit local
        engine1.runTask(jar3);            // hit local

        System.out.println("\n════════════════════════════════════════");
        System.out.println(" Conceptos clave");
        System.out.println("════════════════════════════════════════");
        System.out.println("  • Clave de caché = hash(inputs + tipo-tarea + version-gradle).");
        System.out.println("  • Local cache: ~/.gradle/caches — persiste entre builds del mismo dev.");
        System.out.println("  • Remote cache: servidor HTTP compartido (Gradle Enterprise, etc.).");
        System.out.println("  • Remote cache miss → ejecuta → almacena en local Y remota.");
        System.out.println("  • Remote cache hit  → descarga output, no ejecuta nada.");
        System.out.println("  • Solo tareas @CacheableTask participan en la caché.");
    }
}
