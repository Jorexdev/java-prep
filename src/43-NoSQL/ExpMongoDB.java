import java.util.*;
import java.util.stream.*;
import java.util.function.Predicate;

/**
 * Simulación de MongoDB con Java puro.
 *
 * Conceptos demostrados:
 *  - Document: mapa de campos (equivalente a un documento BSON)
 *  - Collection: lista de documentos con CRUD básico
 *  - MongoDatabase: contenedor de colecciones
 *  - Aggregation pipeline: $match, $group ($sum), $sort
 *  - Índices: simulación de aceleración de búsquedas
 */
public class ExpMongoDB {

    // ─────────────────────────────────────────────
    // DOCUMENT: mapa de campos tipado dinámicamente
    // ─────────────────────────────────────────────

    static class Document {
        private final Map<String, Object> fields = new LinkedHashMap<>();

        Document put(String key, Object value) {
            fields.put(key, value);
            return this;
        }

        @SuppressWarnings("unchecked")
        <T> T get(String key) {
            return (T) fields.get(key);
        }

        boolean has(String key) {
            return fields.containsKey(key);
        }

        Map<String, Object> toMap() {
            return Collections.unmodifiableMap(fields);
        }

        @Override
        public String toString() {
            return fields.toString();
        }
    }

    // ─────────────────────────────────────────────
    // COLLECTION: lista de documentos con CRUD
    // ─────────────────────────────────────────────

    static class Collection {
        private final String nombre;
        private final List<Document> documentos = new ArrayList<>();
        private final Set<String> indices = new HashSet<>();
        private int contadorId = 1;

        Collection(String nombre) {
            this.nombre = nombre;
        }

        // INSERT
        void insertOne(Document doc) {
            if (!doc.has("_id")) {
                doc.put("_id", String.valueOf(contadorId++));
            }
            documentos.add(doc);
            System.out.printf("  [INSERT] %s → %s%n", nombre, doc);
        }

        // FIND todos los documentos
        List<Document> findAll() {
            return Collections.unmodifiableList(documentos);
        }

        // FIND con filtro (Predicate sobre Document)
        List<Document> find(Predicate<Document> filtro) {
            return documentos.stream()
                    .filter(filtro)
                    .collect(Collectors.toList());
        }

        // UPDATE: actualiza el primer documento que cumple el filtro
        boolean updateOne(Predicate<Document> filtro, String campo, Object valor) {
            return documentos.stream()
                    .filter(filtro)
                    .findFirst()
                    .map(doc -> {
                        doc.put(campo, valor);
                        System.out.printf("  [UPDATE] %s → campo '%s' = %s%n", nombre, campo, valor);
                        return true;
                    })
                    .orElse(false);
        }

        // DELETE: elimina el primer documento que cumple el filtro
        boolean deleteOne(Predicate<Document> filtro) {
            return documentos.removeIf(doc -> {
                if (filtro.test(doc)) {
                    System.out.printf("  [DELETE] %s → eliminado %s%n", nombre, doc.get("_id"));
                    return true;
                }
                return false;
            });
        }

        // ÍNDICE: simula creación de índice sobre un campo
        void createIndex(String campo) {
            indices.add(campo);
            System.out.printf("  [INDEX] Índice creado sobre '%s' en colección '%s'%n", campo, nombre);
        }

        // FIND con índice: simula búsqueda acelerada cuando el campo tiene índice
        List<Document> findByField(String campo, Object valor) {
            boolean tieneIndice = indices.contains(campo);
            System.out.printf("  [QUERY] Buscar %s=%s — %s%n",
                    campo, valor, tieneIndice ? "usando índice (O(log n))" : "collection scan (O(n))");
            return documentos.stream()
                    .filter(doc -> valor.equals(doc.get(campo)))
                    .collect(Collectors.toList());
        }

        int size() { return documentos.size(); }
    }

    // ─────────────────────────────────────────────
    // MONGODB: contenedor de colecciones
    // ─────────────────────────────────────────────

    static class MongoDatabase {
        private final String nombre;
        private final Map<String, Collection> colecciones = new HashMap<>();

        MongoDatabase(String nombre) {
            this.nombre = nombre;
        }

        Collection getCollection(String nombre) {
            return colecciones.computeIfAbsent(nombre, Collection::new);
        }
    }

    // ─────────────────────────────────────────────
    // AGGREGATION PIPELINE: $match → $group → $sort
    // ─────────────────────────────────────────────

    static List<Document> aggregatePipeline(List<Document> fuente,
                                             Predicate<Document> matchFiltro,
                                             String groupField,
                                             String sumField) {
        System.out.println("\n  [AGGREGATE] Ejecutando pipeline:");

        // Stage 1: $match
        List<Document> matched = fuente.stream()
                .filter(matchFiltro)
                .collect(Collectors.toList());
        System.out.printf("    $match → %d documentos%n", matched.size());

        // Stage 2: $group con $sum
        Map<Object, Double> grupos = new LinkedHashMap<>();
        for (Document doc : matched) {
            Object key = doc.get(groupField);
            Number valor = doc.get(sumField);
            if (key != null && valor != null) {
                grupos.merge(key, valor.doubleValue(), Double::sum);
            }
        }
        System.out.printf("    $group por '%s' → %d grupos%n", groupField, grupos.size());

        // Stage 3: $sort (descendente por total)
        List<Document> resultado = grupos.entrySet().stream()
                .sorted(Map.Entry.<Object, Double>comparingByValue().reversed())
                .map(entry -> new Document()
                        .put("_id", entry.getKey())
                        .put("total", entry.getValue()))
                .collect(Collectors.toList());
        System.out.println("    $sort → descendente por total");

        return resultado;
    }

    // ─────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== ExpMongoDB: Simulación de MongoDB con Java puro ===\n");

        MongoDatabase db = new MongoDatabase("tienda");
        Collection pedidos = db.getCollection("pedidos");

        // ── Insertar documentos ──
        System.out.println("── 1. insertOne ──");
        pedidos.insertOne(new Document().put("clienteId", "C01").put("producto", "Laptop").put("total", 1200.0).put("estado", "completado"));
        pedidos.insertOne(new Document().put("clienteId", "C02").put("producto", "Monitor").put("total", 350.0).put("estado", "pendiente"));
        pedidos.insertOne(new Document().put("clienteId", "C01").put("producto", "Teclado").put("total", 80.0).put("estado", "completado"));
        pedidos.insertOne(new Document().put("clienteId", "C03").put("producto", "Ratón").put("total", 45.0).put("estado", "completado"));
        pedidos.insertOne(new Document().put("clienteId", "C02").put("producto", "Auriculares").put("total", 120.0).put("estado", "cancelado"));

        System.out.printf("%n  Total documentos: %d%n", pedidos.size());

        // ── Find con filtro ──
        System.out.println("\n── 2. find (completados) ──");
        List<Document> completados = pedidos.find(doc -> "completado".equals(doc.get("estado")));
        completados.forEach(doc -> System.out.println("  " + doc));

        // ── updateOne ──
        System.out.println("\n── 3. updateOne ──");
        pedidos.updateOne(doc -> "C02".equals(doc.get("clienteId")) && "pendiente".equals(doc.get("estado")),
                "estado", "completado");

        // ── Índice y findByField ──
        System.out.println("\n── 4. createIndex + findByField ──");
        // Sin índice primero:
        pedidos.findByField("clienteId", "C01");
        // Crear índice y buscar de nuevo:
        pedidos.createIndex("clienteId");
        pedidos.findByField("clienteId", "C01");

        // ── Aggregation pipeline ──
        System.out.println("\n── 5. Aggregation pipeline ($match→$group→$sort) ──");
        List<Document> resultado = aggregatePipeline(
                pedidos.findAll(),
                doc -> !"cancelado".equals(doc.get("estado")),
                "clienteId",
                "total"
        );
        System.out.println("  Resultado:");
        resultado.forEach(doc -> System.out.printf("    clienteId=%s → total=%.2f%n",
                doc.get("_id"), (Double) doc.get("total")));

        // ── deleteOne ──
        System.out.println("\n── 6. deleteOne ──");
        pedidos.deleteOne(doc -> "cancelado".equals(doc.get("estado")));
        System.out.printf("  Documentos tras delete: %d%n", pedidos.size());
    }
}
