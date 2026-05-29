import java.util.*;
import java.util.function.Supplier;

// Plugin architecture aplicando los 5 principios SOLID con extensibilidad real
// Dominio: sistema de procesamiento de datos con plugins de transformacion y destino

public class Ejercicio5 {

    // ====== ISP: interfaces pequeñas y especificas ======

    // Plugin capaz de leer datos
    interface DataSource {
        List<String> read();
        String name();
    }

    // Plugin capaz de transformar datos (puede encadenarse)
    interface Transformer {
        List<String> transform(List<String> input);
        String name();
    }

    // Plugin capaz de escribir datos al destino
    interface DataSink {
        void write(List<String> data);
        String name();
    }

    // Plugin con capacidad de validacion (ISP: no todos los plugins validan)
    interface Validator {
        List<String> validate(List<String> data); // devuelve errores (vacio = ok)
    }

    // ====== SRP: cada plugin tiene una sola responsabilidad ======

    static class CsvSource implements DataSource {
        private final List<String> rows;
        CsvSource(List<String> rows) { this.rows = rows; }
        public List<String> read() { return new ArrayList<>(rows); }
        public String name() { return "CsvSource"; }
    }

    static class UpperCaseTransformer implements Transformer {
        public List<String> transform(List<String> input) {
            return input.stream().map(String::toUpperCase).toList();
        }
        public String name() { return "UpperCaseTransformer"; }
    }

    static class TrimTransformer implements Transformer {
        public List<String> transform(List<String> input) {
            return input.stream().map(String::strip).toList();
        }
        public String name() { return "TrimTransformer"; }
    }

    // OCP: añadir PrefixTransformer no modifica ningun transformador existente
    static class PrefixTransformer implements Transformer {
        private final String prefix;
        PrefixTransformer(String prefix) { this.prefix = prefix; }
        public List<String> transform(List<String> input) {
            return input.stream().map(s -> prefix + s).toList();
        }
        public String name() { return "PrefixTransformer[" + prefix + "]"; }
    }

    static class ConsoleSink implements DataSink {
        public void write(List<String> data) {
            System.out.println("  [ConsoleSink] " + data.size() + " registros:");
            data.forEach(r -> System.out.println("    " + r));
        }
        public String name() { return "ConsoleSink"; }
    }

    // OCP: nuevo destino sin tocar Pipeline
    static class CollectorSink implements DataSink {
        private final List<String> collected = new ArrayList<>();
        public void write(List<String> data) { collected.addAll(data); }
        public String name() { return "CollectorSink"; }
        public List<String> getCollected() { return collected; }
    }

    static class NonEmptyValidator implements Validator {
        public List<String> validate(List<String> data) {
            List<String> errors = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i) == null || data.get(i).isBlank())
                    errors.add("fila " + i + " vacia");
            }
            return errors;
        }
    }

    // ====== DIP: Pipeline depende solo de abstracciones (interfaces) ======
    // ====== OCP: añadir plugins sin modificar Pipeline ======

    static class Pipeline {
        private DataSource source;
        private final List<Transformer> transformers = new ArrayList<>();
        private DataSink sink;
        private Validator validator;

        // Fluent builder (LSP: cualquier DataSource/Transformer/DataSink funciona)
        Pipeline source(DataSource s) { this.source = s; return this; }
        Pipeline transform(Transformer t) { transformers.add(t); return this; }
        Pipeline sink(DataSink s) { this.sink = s; return this; }
        Pipeline validate(Validator v) { this.validator = v; return this; }

        void execute() {
            System.out.printf("  [Pipeline] fuente: %s%n", source.name());
            List<String> data = source.read();
            System.out.printf("  [Pipeline] leidos %d registros%n", data.size());

            // Validacion opcional
            if (validator != null) {
                List<String> errors = validator.validate(data);
                if (!errors.isEmpty()) {
                    System.out.println("  [Pipeline] errores de validacion: " + errors);
                    return;
                }
                System.out.println("  [Pipeline] validacion OK");
            }

            // Cadena de transformaciones
            for (Transformer t : transformers) {
                data = t.transform(data);
                System.out.printf("  [Pipeline] tras %s: %d registros%n", t.name(), data.size());
            }

            System.out.printf("  [Pipeline] destino: %s%n", sink.name());
            sink.write(data);
        }
    }

    // ====== Plugin registry: DIP + OCP para registrar plugins por tipo ======

    static class PluginRegistry {
        private final Map<String, Supplier<Transformer>> transformerFactories = new HashMap<>();
        private final Map<String, Supplier<DataSink>> sinkFactories = new HashMap<>();

        void registerTransformer(String name, Supplier<Transformer> factory) {
            transformerFactories.put(name, factory);
            System.out.println("  [Registry] transformer registrado: " + name);
        }

        void registerSink(String name, Supplier<DataSink> factory) {
            sinkFactories.put(name, factory);
            System.out.println("  [Registry] sink registrado: " + name);
        }

        Transformer getTransformer(String name) {
            Supplier<Transformer> f = transformerFactories.get(name);
            if (f == null) throw new IllegalArgumentException("Transformer no encontrado: " + name);
            return f.get();
        }

        DataSink getSink(String name) {
            Supplier<DataSink> f = sinkFactories.get(name);
            if (f == null) throw new IllegalArgumentException("Sink no encontrado: " + name);
            return f.get();
        }

        void listAll() {
            System.out.println("  Transformers: " + transformerFactories.keySet());
            System.out.println("  Sinks       : " + sinkFactories.keySet());
        }
    }

    // ====== DEMO ======

    public static void main(String[] args) {
        System.out.println("=== Plugin Architecture aplicando los 5 principios SOLID ===");
        System.out.println();

        // Configuracion del registry (DIP: Pipeline no conoce implementaciones concretas)
        PluginRegistry registry = new PluginRegistry();
        System.out.println("[ Registro de plugins ]");
        registry.registerTransformer("trim",         TrimTransformer::new);
        registry.registerTransformer("uppercase",    UpperCaseTransformer::new);
        registry.registerTransformer("prefix-LOG",   () -> new PrefixTransformer("[LOG] "));
        // OCP: añadir nuevo plugin sin tocar clases existentes
        registry.registerTransformer("prefix-AUDIT", () -> new PrefixTransformer("[AUDIT] "));
        registry.registerSink("console",    ConsoleSink::new);
        registry.registerSink("collector",  CollectorSink::new);
        System.out.println();
        System.out.print("[ Plugins disponibles ] -> ");
        registry.listAll();
        System.out.println();

        // --- Pipeline 1: trim + uppercase + console ---
        System.out.println("[ Pipeline 1 ] trim -> uppercase -> console");
        List<String> datos1 = List.of("  alice  ", "  bob  ", "  carlos  ");
        new Pipeline()
            .source(new CsvSource(datos1))
            .validate(new NonEmptyValidator())
            .transform(registry.getTransformer("trim"))
            .transform(registry.getTransformer("uppercase"))
            .sink(registry.getSink("console"))
            .execute();
        System.out.println();

        // --- Pipeline 2: prefix-LOG + collector (LSP: CollectorSink sustituye a ConsoleSink) ---
        System.out.println("[ Pipeline 2 ] prefix-LOG -> collector (LSP demo)");
        List<String> datos2 = List.of("login:user1", "error:db", "request:api");
        CollectorSink collector = new CollectorSink();
        new Pipeline()
            .source(new CsvSource(datos2))
            .transform(registry.getTransformer("prefix-LOG"))
            .sink(collector)
            .execute();
        System.out.println("  Resultado en collector: " + collector.getCollected());
        System.out.println();

        // --- Pipeline 3: validacion que falla ---
        System.out.println("[ Pipeline 3 ] validacion con datos vacios (debe fallar)");
        List<String> datosConVacios = List.of("valido", "", "  ", "otro");
        new Pipeline()
            .source(new CsvSource(datosConVacios))
            .validate(new NonEmptyValidator())
            .transform(registry.getTransformer("uppercase"))
            .sink(registry.getSink("console"))
            .execute();
        System.out.println();

        System.out.println("=== Analisis SOLID ===");
        System.out.println("SRP : DataSource, Transformer, DataSink, Validator, Pipeline tienen una sola resp.");
        System.out.println("OCP : nuevos plugins (PrefixTransformer, CollectorSink) sin tocar Pipeline.");
        System.out.println("LSP : CollectorSink sustituye a ConsoleSink; PrefixTransformer a TrimTransformer.");
        System.out.println("ISP : DataSource, Transformer, DataSink, Validator son interfaces separadas.");
        System.out.println("     Pipeline solo ve las interfaces que necesita.");
        System.out.println("DIP : Pipeline y PluginRegistry dependen de interfaces, no de clases concretas.");
    }
}
