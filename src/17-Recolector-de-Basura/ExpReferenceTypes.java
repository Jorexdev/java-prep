import java.lang.ref.*;
import java.util.*;

public class ExpReferenceTypes {

    static class HeavyObject {
        private final String name;
        private final byte[] data;

        HeavyObject(String name, int sizeKb) {
            this.name = name;
            this.data = new byte[sizeKb * 1024];
        }

        @Override
        public String toString() { return "HeavyObject(" + name + ")"; }
    }

    // --- 1. StrongReference ---
    // Normal Java variable: object lives as long as this reference exists.
    static void demoStrong() {
        HeavyObject strong = new HeavyObject("strong", 10);
        System.out.println("Strong ref alive: " + strong);
        // strong = null; → now eligible for GC
    }

    // --- 2. SoftReference ---
    // JVM collects it only when memory is tight — ideal for caches.
    static void demoSoft() {
        SoftReference<HeavyObject> softRef = new SoftReference<>(new HeavyObject("soft", 10));
        HeavyObject obj = softRef.get();
        System.out.println("Soft ref before GC: " + obj);
        System.gc();
        obj = softRef.get();
        // Under normal conditions soft ref survives a suggested GC
        System.out.println("Soft ref after GC hint: " + (obj != null ? "still alive" : "collected"));
    }

    // --- 3. WeakReference ---
    // Collected on the *next* GC cycle regardless of memory — good for canonicalized mappings.
    static void demoWeak() throws InterruptedException {
        WeakReference<HeavyObject> weakRef = new WeakReference<>(new HeavyObject("weak", 10));
        System.out.println("Weak ref before GC: " + weakRef.get());
        System.gc();
        Thread.sleep(100); // give GC thread time to run
        System.out.println("Weak ref after GC: " + (weakRef.get() != null ? "still alive" : "collected"));
    }

    // --- 4. PhantomReference + ReferenceQueue ---
    // The get() always returns null; the queue is notified *after* finalization.
    // Use for resource cleanup without overriding finalize().
    static void demoPhantom() throws InterruptedException {
        ReferenceQueue<HeavyObject> queue = new ReferenceQueue<>();
        PhantomReference<HeavyObject> phantomRef =
                new PhantomReference<>(new HeavyObject("phantom", 10), queue);

        System.out.println("Phantom get() always null: " + phantomRef.get());

        System.gc();
        Thread.sleep(200);

        Reference<?> enqueued = queue.poll();
        System.out.println("Phantom enqueued after GC: " + (enqueued != null ? "yes — cleanup can run" : "not yet"));
    }

    // --- 5. SoftReference-based LRU cache ---
    static class SoftCache<K, V> {
        private final Map<K, SoftReference<V>> map = new LinkedHashMap<>();

        void put(K key, V value) {
            map.put(key, new SoftReference<>(value));
        }

        V get(K key) {
            SoftReference<V> ref = map.get(key);
            return ref != null ? ref.get() : null; // null means GC already collected the value
        }

        int size() { return map.size(); }
    }

    // --- 6. WeakHashMap for metadata ---
    // Entries are automatically removed when the key object is collected.
    static void demoWeakHashMap() throws InterruptedException {
        WeakHashMap<Object, String> metadata = new WeakHashMap<>();
        Object key = new Object();
        metadata.put(key, "session-data");
        System.out.println("WeakHashMap entry before key GC: " + metadata.size());

        key = null; // drop strong reference to key
        System.gc();
        Thread.sleep(100);
        System.out.println("WeakHashMap entry after key GC: " + metadata.size() + " (may be 0)");
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Strong Reference ===");
        demoStrong();

        System.out.println("\n=== Soft Reference (cache-friendly) ===");
        demoSoft();

        System.out.println("\n=== Weak Reference ===");
        demoWeak();

        System.out.println("\n=== Phantom Reference + ReferenceQueue ===");
        demoPhantom();

        System.out.println("\n=== SoftReference LRU Cache ===");
        SoftCache<String, HeavyObject> cache = new SoftCache<>();
        cache.put("img-1", new HeavyObject("img-1", 50));
        cache.put("img-2", new HeavyObject("img-2", 50));
        System.out.println("Cache entries: " + cache.size());
        System.out.println("Cache hit img-1: " + (cache.get("img-1") != null ? "hit" : "miss (GC'd)"));

        System.out.println("\n=== WeakHashMap for metadata ===");
        demoWeakHashMap();
    }
}
