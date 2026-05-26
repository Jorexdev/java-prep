public class ExpTemplateMethod {

    public static void main(String[] args) {

        DataProcessor csv  = new CsvProcessor("datos.csv");
        DataProcessor json = new JsonProcessor("datos.json");

        System.out.println("=== CSV ===");
        csv.process();

        System.out.println("\n=== JSON ===");
        json.process();
    }

    // Plantilla: define el esqueleto del algoritmo, los pasos varían en subclases
    abstract static class DataProcessor {

        final String source;

        DataProcessor(String source) { this.source = source; }

        // Template method — final para que las subclases no rompan el orden
        final void process() {
            String raw  = readData();
            validateData(raw);           // hook: no-op por defecto, sobreescribible
            String result = processData(raw);
            writeResult(result);
        }

        abstract String readData();
        abstract String processData(String raw);

        // Hook: paso opcional con comportamiento por defecto vacío
        void validateData(String raw) {}

        // Paso concreto compartido — todas las subclases escriben igual
        void writeResult(String result) {
            System.out.println("  [write] resultado guardado: " + result);
        }
    }

    static class CsvProcessor extends DataProcessor {

        CsvProcessor(String source) { super(source); }

        @Override
        String readData() {
            System.out.println("  [read]  leyendo CSV desde " + source);
            return "col1,col2\n1,2\n3,4";
        }

        @Override
        void validateData(String raw) {
            // CsvProcessor necesita cabecera — sobreescribe el hook
            if (!raw.contains(",")) throw new IllegalArgumentException("CSV sin comas");
            System.out.println("  [valid] cabecera CSV ok");
        }

        @Override
        String processData(String raw) {
            int lines = raw.split("\n").length - 1; // descontar cabecera
            System.out.println("  [proc]  parseadas " + lines + " filas CSV");
            return lines + " filas procesadas";
        }
    }

    static class JsonProcessor extends DataProcessor {

        JsonProcessor(String source) { super(source); }

        @Override
        String readData() {
            System.out.println("  [read]  leyendo JSON desde " + source);
            return "{\"items\":[1,2,3]}";
        }

        // No sobreescribe validateData — usa el hook vacío (no necesita validación extra)

        @Override
        String processData(String raw) {
            System.out.println("  [proc]  deserializando JSON");
            return "JSON procesado (" + raw.length() + " chars)";
        }
    }
}
