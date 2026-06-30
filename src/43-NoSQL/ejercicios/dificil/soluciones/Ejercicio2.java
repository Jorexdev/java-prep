import java.util.*;
import java.util.stream.Collectors;

/**
 * Ejercicio 2 (Difícil) — MongoDB $lookup (equivalente a LEFT JOIN)
 *
 * $lookup enriquece cada documento de la colección local con el documento
 * relacionado de la colección extranjera, añadiéndolo como campo anidado.
 *
 * Equivalente SQL:
 *   SELECT p.*, c.* FROM pedidos p LEFT JOIN clientes c ON p.clienteId = c.id
 */
public class Ejercicio2 {

    static class Document {
        private final Map<String, Object> fields = new LinkedHashMap<>();

        Document put(String key, Object value) {
            fields.put(key, value);
            return this;
        }

        Object get(String key) { return fields.get(key); }

        @Override
        public String toString() { return fields.toString(); }
    }

    static class Collection {
        private final List<Document> docs = new ArrayList<>();

        void insertOne(Document doc) { docs.add(doc); }

        List<Document> findAll() { return Collections.unmodifiableList(docs); }

        // $lookup: LEFT JOIN con foreignCollection
        // localField: campo en esta colección
        // foreignField: campo en la colección extranjera que debe coincidir
        // as: nombre del campo anidado que se añade al resultado
        List<Document> lookup(Collection foreignCollection,
                              String localField,
                              String foreignField,
                              String as) {
            List<Document> resultado = new ArrayList<>();

            for (Document local : docs) {
                Object localValue = local.get(localField);

                // Buscar documentos en la colección extranjera donde foreignField == localValue
                List<Document> matches = foreignCollection.findAll().stream()
                        .filter(foreign -> localValue != null && localValue.equals(foreign.get(foreignField)))
                        .collect(Collectors.toList());

                // Crear documento enriquecido: copia del local + campo "as" con los matches
                Document enriched = new Document();
                local.fields.forEach(enriched::put);
                // $lookup en MongoDB añade un array (aunque haya un solo match)
                enriched.put(as, matches.isEmpty() ? Collections.emptyList() : matches);
                resultado.add(enriched);
            }

            return resultado;
        }
    }

    public static void main(String[] args) {
        // Colección: pedidos
        Collection pedidos = new Collection();
        pedidos.insertOne(new Document().put("id", "ORD-001").put("clienteId", "C01").put("total", 250.0));
        pedidos.insertOne(new Document().put("id", "ORD-002").put("clienteId", "C02").put("total", 120.0));
        pedidos.insertOne(new Document().put("id", "ORD-003").put("clienteId", "C01").put("total", 380.0));
        pedidos.insertOne(new Document().put("id", "ORD-004").put("clienteId", "C99").put("total", 50.0)); // cliente inexistente

        // Colección: clientes
        Collection clientes = new Collection();
        clientes.insertOne(new Document().put("id", "C01").put("nombre", "Ana García").put("email", "ana@ejemplo.com"));
        clientes.insertOne(new Document().put("id", "C02").put("nombre", "Carlos López").put("email", "carlos@ejemplo.com"));

        // $lookup: une pedidos con clientes
        System.out.println("db.pedidos.aggregate([");
        System.out.println("  { $lookup: { from: 'clientes', localField: 'clienteId',");
        System.out.println("               foreignField: 'id', as: 'cliente' } }");
        System.out.println("])\n");

        List<Document> resultado = pedidos.lookup(clientes, "clienteId", "id", "cliente");

        resultado.forEach(doc -> {
            System.out.printf("Pedido %s (total=%.0f€)%n", doc.get("id"), (Double) doc.get("total"));
            @SuppressWarnings("unchecked")
            List<Document> clienteList = (List<Document>) doc.get("cliente");
            if (clienteList.isEmpty()) {
                System.out.println("  cliente: [] ← LEFT JOIN: no hay match (clienteId C99 inexistente)");
            } else {
                Document cliente = clienteList.get(0);
                System.out.printf("  cliente: nombre='%s', email='%s'%n",
                        cliente.get("nombre"), cliente.get("email"));
            }
        });
    }
}
