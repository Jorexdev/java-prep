import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ejercicio5 {

    static class MultiMap<K, V> {
        private final Map<K, List<V>> interno = new HashMap<>();

        public void put(K clave, V valor) {
            interno.computeIfAbsent(clave, k -> new ArrayList<>()).add(valor);
        }

        public List<V> get(K clave) {
            return interno.getOrDefault(clave, Collections.emptyList());
        }

        public Map<K, List<V>> getAll() {
            return Collections.unmodifiableMap(interno);
        }

        public boolean remove(K clave, V valor) {
            List<V> lista = interno.get(clave);
            if (lista == null) return false;
            boolean eliminado = lista.remove(valor);
            if (lista.isEmpty()) interno.remove(clave);
            return eliminado;
        }

        @Override
        public String toString() {
            return interno.toString();
        }
    }

    public static void main(String[] args) {
        MultiMap<String, String> tags = new MultiMap<>();

        tags.put("java", "lenguaje");
        tags.put("java", "backend");
        tags.put("java", "tipado-estático");
        tags.put("python", "lenguaje");
        tags.put("python", "scripting");

        System.out.println("MultiMap completo: " + tags);
        System.out.println("Tags de 'java':    " + tags.get("java"));
        System.out.println("Tags de 'python':  " + tags.get("python"));
        System.out.println("Tags de 'go':      " + tags.get("go")); // []

        tags.remove("java", "backend");
        System.out.println("\nTras remove('java', 'backend'): " + tags.get("java"));

        tags.remove("python", "lenguaje");
        tags.remove("python", "scripting");
        System.out.println("Tras vaciar 'python': contiene 'python'? " +
            tags.getAll().containsKey("python")); // false — se elimina la clave
    }
}
