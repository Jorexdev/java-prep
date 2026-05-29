import java.util.HashMap;
import java.util.Map;

// LRU Cache con lista doblemente enlazada manual + HashMap → get/put en O(1)

public class Ejercicio5 {

    static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key   = key;
            this.value = value;
        }
    }

    static class LRUCache<K, V> {
        private final int capacity;
        private final Map<K, Node<K, V>> map = new HashMap<>();

        // Centinelas: head (más reciente) ← → tail (más antiguo)
        private final Node<K, V> head = new Node<>(null, null);
        private final Node<K, V> tail = new Node<>(null, null);

        LRUCache(int capacity) {
            this.capacity = capacity;
            head.next = tail;
            tail.prev = head;
        }

        public V get(K key) {
            Node<K, V> node = map.get(key);
            if (node == null) return null;
            moveToFront(node);
            return node.value;
        }

        public void put(K key, V value) {
            Node<K, V> node = map.get(key);
            if (node != null) {
                node.value = value;
                moveToFront(node);
            } else {
                Node<K, V> newNode = new Node<>(key, value);
                map.put(key, newNode);
                addToFront(newNode);

                if (map.size() > capacity) {
                    Node<K, V> evicted = removeTail();
                    map.remove(evicted.key);
                    System.out.println("  [EVICT] clave=" + evicted.key);
                }
            }
        }

        private void addToFront(Node<K, V> node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
        }

        private void remove(Node<K, V> node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        private void moveToFront(Node<K, V> node) {
            remove(node);
            addToFront(node);
        }

        private Node<K, V> removeTail() {
            Node<K, V> node = tail.prev;
            remove(node);
            return node;
        }

        public void printState() {
            System.out.print("  Cache [MRU→LRU]: ");
            Node<K, V> cur = head.next;
            StringBuilder sb = new StringBuilder();
            while (cur != tail) {
                sb.append(cur.key).append("=").append(cur.value);
                if (cur.next != tail) sb.append(" → ");
                cur = cur.next;
            }
            System.out.println(sb + "  (size=" + map.size() + "/" + capacity + ")");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== LRU Cache O(1) con lista doblemente enlazada ===\n");

        LRUCache<Integer, String> cache = new LRUCache<>(3);

        System.out.println("put(1, A)");
        cache.put(1, "A"); cache.printState();

        System.out.println("put(2, B)");
        cache.put(2, "B"); cache.printState();

        System.out.println("put(3, C)");
        cache.put(3, "C"); cache.printState();

        System.out.println("get(1) → " + cache.get(1) + "  (1 sube a MRU)");
        cache.printState();

        System.out.println("put(4, D)  (evicta el LRU actual)");
        cache.put(4, "D"); cache.printState();

        System.out.println("get(2) → " + cache.get(2) + "  (null: fue evictado)");

        System.out.println("put(5, E)  (evicta el LRU actual)");
        cache.put(5, "E"); cache.printState();

        System.out.println("get(3) → " + cache.get(3) + "  (null: fue evictado)");

        System.out.println("get(1) → " + cache.get(1));
        cache.printState();

        System.out.println("get(5) → " + cache.get(5) + "  (5 sube a MRU)");
        cache.printState();

        System.out.println("put(6, F)  (evicta el LRU actual)");
        cache.put(6, "F"); cache.printState();

        System.out.println("\n=== Complejidad ===");
        System.out.println("get(key) : O(1) — HashMap lookup + relink dos punteros");
        System.out.println("put(key) : O(1) — HashMap put + add-to-front + optional evict");
        System.out.println("Ventaja sobre LinkedHashMap: control total sin overhead de iteración.");
    }
}
