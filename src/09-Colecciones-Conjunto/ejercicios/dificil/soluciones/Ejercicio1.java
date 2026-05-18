import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Ejercicio1 {

    static class SetMultimap<K, V> {
        private final Map<K, Set<V>> mapa = new HashMap<>();

        public void put(K clave, V valor) {
            mapa.computeIfAbsent(clave, k -> new HashSet<>()).add(valor);
        }

        public Set<V> get(K clave) {
            return mapa.getOrDefault(clave, Collections.emptySet());
        }

        public boolean remove(K clave, V valor) {
            Set<V> valores = mapa.get(clave);
            if (valores == null) return false;
            boolean eliminado = valores.remove(valor);
            if (valores.isEmpty()) mapa.remove(clave);
            return eliminado;
        }

        public boolean containsEntry(K clave, V valor) {
            return get(clave).contains(valor);
        }

        @Override
        public String toString() {
            return mapa.toString();
        }
    }

    public static void main(String[] args) {
        SetMultimap<String, Integer> mm = new SetMultimap<>();

        mm.put("a", 1);
        mm.put("a", 2);
        mm.put("a", 2); // duplicado: no se añade
        mm.put("a", 3);
        mm.put("b", 1);

        System.out.println("Contenido: " + mm);
        System.out.println("get(a): " + mm.get("a"));          // {1, 2, 3}
        System.out.println("get(b): " + mm.get("b"));          // {1}
        System.out.println("containsEntry(a,2): " + mm.containsEntry("a", 2)); // true

        mm.remove("a", 2);
        System.out.println("Tras remove(a,2): " + mm.get("a")); // {1, 3}
    }
}
