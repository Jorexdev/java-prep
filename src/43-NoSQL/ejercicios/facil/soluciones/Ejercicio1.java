import java.util.*;

/**
 * Ejercicio 1 — CRUD en MongoDB
 * CRUD básico simulando Document y Collection de MongoDB.
 */
public class Ejercicio1 {

    static class Document {
        private final Map<String, Object> fields = new LinkedHashMap<>();

        Document put(String key, Object value) {
            fields.put(key, value);
            return this;
        }

        Object get(String key) {
            return fields.get(key);
        }

        @Override
        public String toString() {
            return fields.toString();
        }
    }

    static class Collection {
        private final List<Document> docs = new ArrayList<>();

        void insertOne(Document doc) {
            docs.add(doc);
        }

        List<Document> findAll() {
            return Collections.unmodifiableList(docs);
        }

        Optional<Document> findById(String id) {
            return docs.stream()
                    .filter(doc -> id.equals(doc.get("id")))
                    .findFirst();
        }
    }

    public static void main(String[] args) {
        Collection usuarios = new Collection();

        usuarios.insertOne(new Document().put("id", "U01").put("nombre", "Ana").put("email", "ana@ejemplo.com"));
        usuarios.insertOne(new Document().put("id", "U02").put("nombre", "Carlos").put("email", "carlos@ejemplo.com"));
        usuarios.insertOne(new Document().put("id", "U03").put("nombre", "Bea").put("email", "bea@ejemplo.com"));

        System.out.println("Buscar U02: " + usuarios.findById("U02").map(d -> d.get("nombre")).orElse("no encontrado"));
        System.out.println("Todos los usuarios:");
        usuarios.findAll().forEach(doc -> System.out.println("  " + doc));
    }
}
