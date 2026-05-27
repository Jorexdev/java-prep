import java.util.*;

// ===== Modelo de tarea Gradle =====

class GradleTask {
    private final String name;
    private final List<String> dependsOn = new ArrayList<>();
    private final String action;
    private String inputHash;    // hash simulado de inputs
    private String outputHash;   // hash guardado del último run

    public GradleTask(String name, String action, String... deps) {
        this.name   = name;
        this.action = action;
        this.dependsOn.addAll(Arrays.asList(deps));
    }

    public String getName()          { return name; }
    public List<String> getDepsOn()  { return dependsOn; }

    public void setInputHash(String hash)  { this.inputHash  = hash; }
    public void setOutputHash(String hash) { this.outputHash = hash; }

    // Tarea está UP-TO-DATE si el hash de inputs no ha cambiado respecto al último run
    public boolean isUpToDate() {
        return inputHash != null && inputHash.equals(outputHash);
    }

    public void execute() {
        if (isUpToDate()) {
            System.out.println("  > Task :" + name + " UP-TO-DATE");
        } else {
            System.out.println("  > Task :" + name);
            System.out.println("    " + action);
            outputHash = inputHash; // simular que los outputs se actualizan
        }
    }
}

// ===== Grafo de tareas con orden topológico =====

class GradleTaskGraph {
    private final Map<String, GradleTask> tasks = new LinkedHashMap<>();

    public void register(GradleTask task) {
        tasks.put(task.getName(), task);
    }

    // Ordenación topológica (DFS post-order) para respetar dependencias
    public List<GradleTask> resolveOrder(String targetTask) {
        List<GradleTask> ordered = new ArrayList<>();
        Set<String>      visited = new LinkedHashSet<>();
        visit(targetTask, visited, ordered);
        return ordered;
    }

    private void visit(String taskName, Set<String> visited, List<GradleTask> ordered) {
        if (visited.contains(taskName)) return;
        visited.add(taskName);
        GradleTask task = tasks.get(taskName);
        if (task == null) return;
        for (String dep : task.getDepsOn()) {
            visit(dep, visited, ordered);
        }
        ordered.add(task);
    }

    public void run(String targetTask) {
        System.out.println("\n> Task graph for :" + targetTask);
        List<GradleTask> order = resolveOrder(targetTask);
        System.out.println("  Execution order: "
                + order.stream().map(GradleTask::getName).toList());
        System.out.println();
        for (GradleTask t : order) {
            t.execute();
        }
    }
}

public class ExpTaskGraph {

    static GradleTaskGraph buildStandardGraph() {
        GradleTaskGraph graph = new GradleTaskGraph();

        // Cadena estándar: compileJava → processResources → classes → jar
        graph.register(new GradleTask("compileJava",
                "Compilando src/main/java → build/classes/java/main/"));
        graph.register(new GradleTask("processResources",
                "Copiando src/main/resources → build/resources/main/"));
        graph.register(new GradleTask("classes",
                "(tarea agregadora — sin acción propia)",
                "compileJava", "processResources"));
        graph.register(new GradleTask("jar",
                "Empaquetando build/libs/app.jar",
                "classes"));
        graph.register(new GradleTask("compileTestJava",
                "Compilando src/test/java → build/classes/java/test/",
                "classes"));
        graph.register(new GradleTask("processTestResources",
                "Copiando src/test/resources → build/resources/test/"));
        graph.register(new GradleTask("testClasses",
                "(tarea agregadora de test)",
                "compileTestJava", "processTestResources"));
        graph.register(new GradleTask("test",
                "Ejecutando tests JUnit → build/reports/tests/",
                "testClasses"));
        graph.register(new GradleTask("check",
                "(verifica tests y análisis estáticos)",
                "test"));
        graph.register(new GradleTask("assemble",
                "(ensambla todos los artefactos)",
                "jar"));
        graph.register(new GradleTask("build",
                "(build completo)",
                "check", "assemble"));

        return graph;
    }

    // ─── Configurar hashes para simular UP-TO-DATE y cambios ─────────────────
    static void configureHashes(GradleTaskGraph graph, boolean allUpToDate, String changedTask) {
        // Acceso directo por nombre (simplificado)
        String[] taskNames = {"compileJava","processResources","classes",
                "jar","compileTestJava","processTestResources",
                "testClasses","test","check","assemble","build"};

        // Simular usando reflexión-friendly: usamos un mapa auxiliar
        Map<String, GradleTask> taskMap = new LinkedHashMap<>();
        GradleTaskGraph fresh = buildStandardGraph();
        List<GradleTask> all  = fresh.resolveOrder("build");
        for (GradleTask t : all) taskMap.put(t.getName(), t);

        for (GradleTask t : all) {
            String hash = "hash-" + t.getName() + "-v1";
            t.setInputHash(hash);
            if (allUpToDate) {
                t.setOutputHash(hash);                     // UP-TO-DATE
            } else if (t.getName().equals(changedTask)) {
                t.setOutputHash("hash-" + t.getName() + "-OLD"); // modificado
            } else {
                t.setOutputHash(hash);                     // los demás UP-TO-DATE
            }
        }
    }

    public static void main(String[] args) {

        System.out.println("════════════════════════════════════════");
        System.out.println(" Gradle Task Graph");
        System.out.println("════════════════════════════════════════");

        // ─── Primer run: todo se ejecuta ─────────────────────────────────
        System.out.println("\n=== Primer run (todo sin caché) ===");
        GradleTaskGraph graph1 = buildStandardGraph();
        // Sin outputHash → ninguna tarea está UP-TO-DATE
        for (GradleTask t : graph1.resolveOrder("build")) {
            t.setInputHash("hash-" + t.getName() + "-v1");
        }
        graph1.run("build");

        // ─── Segundo run: todo UP-TO-DATE ─────────────────────────────────
        System.out.println("\n=== Segundo run (inputs no cambiaron — todo UP-TO-DATE) ===");
        GradleTaskGraph graph2 = buildStandardGraph();
        for (GradleTask t : graph2.resolveOrder("build")) {
            String hash = "hash-" + t.getName() + "-v1";
            t.setInputHash(hash);
            t.setOutputHash(hash); // mismo hash → UP-TO-DATE
        }
        graph2.run("build");

        // ─── Tercer run: solo compileJava cambió (incremental) ────────────
        System.out.println("\n=== Tercer run (compileJava modificado — build incremental) ===");
        GradleTaskGraph graph3 = buildStandardGraph();
        List<GradleTask> all3  = graph3.resolveOrder("build");
        Set<String> affectedByCompile = Set.of("compileJava","classes","jar","assemble",
                "compileTestJava","testClasses","test","check","build");
        for (GradleTask t : all3) {
            String hash = "hash-" + t.getName() + "-v1";
            t.setInputHash(hash);
            if (affectedByCompile.contains(t.getName())) {
                t.setOutputHash("hash-" + t.getName() + "-OLD"); // obsoleto
            } else {
                t.setOutputHash(hash); // UP-TO-DATE
            }
        }
        graph3.run("build");

        System.out.println("\n════════════════════════════════════════");
        System.out.println(" Conceptos clave");
        System.out.println("════════════════════════════════════════");
        System.out.println("  • Las tareas declaran dependsOn → Gradle calcula el orden automáticamente.");
        System.out.println("  • UP-TO-DATE: inputs no cambiaron → la tarea se salta.");
        System.out.println("  • Incremental: solo las tareas afectadas por un cambio se re-ejecutan.");
        System.out.println("  • ./gradlew jar        → classes → processResources + compileJava");
        System.out.println("  • ./gradlew build      → check + assemble → test + jar → ...");
    }
}
