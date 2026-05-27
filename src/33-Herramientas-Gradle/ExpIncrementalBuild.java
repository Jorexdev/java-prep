import java.util.*;

// ===== Representación de un archivo fuente =====

class SourceFile {
    final String path;
    long lastModified;

    public SourceFile(String path, long lastModified) {
        this.path         = path;
        this.lastModified = lastModified;
    }

    // Hash simulado: combina ruta + timestamp
    public String hash() {
        return path + "@" + lastModified;
    }

    @Override
    public String toString() { return path; }
}

// ===== Tarea incremental: rastrea @InputFiles y @OutputFiles =====

class IncrementalTask {
    private final String name;

    /*
     * Conceptos de anotaciones Gradle:
     *
     *   @Input       → valor escalar (String, boolean, version...)
     *   @InputFile   → un único archivo de entrada
     *   @InputFiles  → colección de archivos de entrada (FileCollection)
     *   @OutputFile  → un único archivo de salida
     *   @OutputDir   → directorio de salida con múltiples archivos
     */

    // @InputFiles — archivos que, si cambian, invalidan los outputs
    private final Map<String, SourceFile> inputFiles   = new LinkedHashMap<>();
    // @OutputDir  — directorio producido
    private final List<String>            outputDirs   = new ArrayList<>();
    // @Input      — valor escalar (ej. flags de compilación)
    private String inputValue;

    // Estado guardado del run anterior (hash de cada input)
    private final Map<String, String> cachedHashes = new LinkedHashMap<>();
    private String cachedInputValue;

    public IncrementalTask(String name) { this.name = name; }

    public void addInput(SourceFile file)   { inputFiles.put(file.path, file); }
    public void addOutputDir(String dir)    { outputDirs.add(dir); }
    public void setInputValue(String value) { this.inputValue = value; }

    // Guardar snapshot del estado actual (simula el .gradle/buildOutputCleanup)
    public void snapshot() {
        cachedHashes.clear();
        for (SourceFile f : inputFiles.values()) {
            cachedHashes.put(f.path, f.hash());
        }
        cachedInputValue = inputValue;
    }

    // UP-TO-DATE: todos los hashes de inputs coinciden con el snapshot previo
    public boolean isUpToDate() {
        if (cachedHashes.isEmpty()) return false; // nunca ejecutado
        if (inputValue != null && !inputValue.equals(cachedInputValue)) return false;
        for (SourceFile f : inputFiles.values()) {
            if (!f.hash().equals(cachedHashes.getOrDefault(f.path, ""))) return false;
        }
        return true;
    }

    // Archivos cuyo hash cambió respecto al snapshot (incrementales)
    public List<SourceFile> getChangedInputs() {
        List<SourceFile> changed = new ArrayList<>();
        for (SourceFile f : inputFiles.values()) {
            if (!f.hash().equals(cachedHashes.getOrDefault(f.path, ""))) {
                changed.add(f);
            }
        }
        return changed;
    }

    public void execute() {
        if (isUpToDate()) {
            System.out.println("  > Task :" + name + " UP-TO-DATE");
            return;
        }

        List<SourceFile> changed = getChangedInputs();
        boolean isIncremental = !changed.isEmpty() && changed.size() < inputFiles.size();

        if (isIncremental) {
            System.out.println("  > Task :" + name
                    + " (incremental — " + changed.size() + "/" + inputFiles.size() + " archivos)");
            for (SourceFile f : changed) {
                System.out.println("    compilando: " + f.path);
            }
        } else {
            System.out.println("  > Task :" + name
                    + " (full recompile — " + inputFiles.size() + " archivos)");
            for (SourceFile f : inputFiles.values()) {
                System.out.println("    compilando: " + f.path);
            }
        }

        for (String dir : outputDirs) {
            System.out.println("    → output: " + dir);
        }

        snapshot(); // guardar estado tras ejecución exitosa
    }
}

public class ExpIncrementalBuild {

    static IncrementalTask freshTask(SourceFile[] files) {
        IncrementalTask task = new IncrementalTask("compileJava");
        task.setInputValue("--release 21");
        for (SourceFile f : files) task.addInput(f);
        task.addOutputDir("build/classes/java/main/");
        return task;
    }

    public static void main(String[] args) {

        // 5 archivos fuente — t=1000 equivale a "sin cambios recientes"
        SourceFile userFile    = new SourceFile("src/UserService.java",          1000L);
        SourceFile orderFile   = new SourceFile("src/OrderService.java",         1000L);
        SourceFile productFile = new SourceFile("src/ProductRepository.java",    1000L);
        SourceFile paymentFile = new SourceFile("src/PaymentGateway.java",       1000L);
        SourceFile notifFile   = new SourceFile("src/NotificationService.java",  1000L);
        SourceFile[] allFiles  = {userFile, orderFile, productFile, paymentFile, notifFile};

        System.out.println("════════════════════════════════════════");
        System.out.println(" Incremental Build — Gradle");
        System.out.println("════════════════════════════════════════");

        // ─── Run 1: primer build — full compile (sin snapshot previo) ─────
        System.out.println("\n=== Run 1: primer build (sin estado previo) ===");
        IncrementalTask run1 = freshTask(allFiles);
        run1.execute();

        // ─── Run 2: sin cambios — todo UP-TO-DATE ─────────────────────────
        System.out.println("\n=== Run 2: sin cambios ===");
        IncrementalTask run2 = freshTask(allFiles);
        run2.snapshot(); // simular que ya se ejecutó con estos inputs
        run2.execute();  // → UP-TO-DATE

        // ─── Run 3: solo UserService.java modificado — incremental ─────────
        System.out.println("\n=== Run 3: UserService.java modificado (build incremental) ===");
        IncrementalTask run3 = freshTask(allFiles);
        run3.snapshot();                    // snapshot con todos en t=1000
        userFile.lastModified = 2000L;      // simular edición del archivo
        run3.execute();                     // incremental: solo UserService.java

        // ─── Run 4: run3 completado → snapshot actualizado → UP-TO-DATE ───
        System.out.println("\n=== Run 4: sin cambios tras run3 ===");
        IncrementalTask run4 = freshTask(allFiles);
        run4.snapshot();   // snapshot con userFile en t=2000
        run4.execute();    // → UP-TO-DATE

        // ─── Run 5: nuevo archivo añadido — invalida snapshot → full compile
        System.out.println("\n=== Run 5: nuevo archivo añadido (ApiController.java) ===");
        SourceFile apiFile = new SourceFile("src/ApiController.java", 3000L);
        IncrementalTask run5 = freshTask(allFiles); // snapshot con 5 archivos
        run5.snapshot();
        // Crear nueva tarea con 6 archivos — el nuevo no está en el snapshot
        // Transferir snapshot previo (5 archivos) manualmente:
        // reusar el mismo objeto para simplificar
        IncrementalTask run5clean = new IncrementalTask("compileJava");
        run5clean.setInputValue("--release 21");
        for (SourceFile f : allFiles)  run5clean.addInput(f);
        run5clean.addOutputDir("build/classes/java/main/");
        run5clean.snapshot();           // snapshot de 5 archivos
        run5clean.addInput(apiFile);    // añadir el sexto DESPUÉS del snapshot
        run5clean.execute();            // full recompile: ApiController no estaba en snapshot

        System.out.println("\n════════════════════════════════════════");
        System.out.println(" Conceptos clave");
        System.out.println("════════════════════════════════════════");
        System.out.println("  @Input        → valor escalar (String, enum, version)");
        System.out.println("  @InputFile    → un único archivo de entrada");
        System.out.println("  @InputFiles   → FileCollection o List<File>");
        System.out.println("  @OutputFile   → un único archivo de salida generado");
        System.out.println("  @OutputDir    → directorio con múltiples outputs");
        System.out.println("  Sin @Input/@Output → Gradle no puede detectar cambios → siempre ejecuta.");
        System.out.println("  Cambiar un @InputFile → solo ese archivo se recompila (incremental).");
        System.out.println("  Añadir un archivo nuevo → full recompile (snapshot no tiene el archivo).");
    }
}
