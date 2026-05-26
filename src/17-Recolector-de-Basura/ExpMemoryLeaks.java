import java.lang.ref.*;
import java.util.*;

public class ExpMemoryLeaks {

    // -----------------------------------------------------------------------
    // Pattern 1: static collection that grows without bound
    // -----------------------------------------------------------------------
    static final List<byte[]> CACHE_BROKEN = new ArrayList<>();

    static void leakStaticCollection(int iterations) {
        for (int i = 0; i < iterations; i++) {
            CACHE_BROKEN.add(new byte[1024]); // grows forever
        }
    }

    // Fix: use WeakHashMap so entries are reclaimed when keys are GC'd,
    // or add an explicit eviction policy.
    static final WeakHashMap<String, byte[]> CACHE_FIXED = new WeakHashMap<>();

    static void fixStaticCollection(int iterations) {
        for (int i = 0; i < iterations; i++) {
            String key = "key-" + i; // key is a local var — eligible for GC after loop body
            CACHE_FIXED.put(key, new byte[1024]);
        }
    }

    // -----------------------------------------------------------------------
    // Pattern 2: listener not unregistered
    // -----------------------------------------------------------------------
    interface EventListener { void onEvent(String event); }

    static class EventBus {
        // Strong references: listeners are never collected while registered.
        private final List<EventListener> strongListeners = new ArrayList<>();
        // Weak references: listener collected when nothing else holds it.
        private final List<WeakReference<EventListener>> weakListeners = new ArrayList<>();

        void registerStrong(EventListener l)  { strongListeners.add(l); }
        void registerWeak(EventListener l)    { weakListeners.add(new WeakReference<>(l)); }

        void publishStrong(String e) { strongListeners.forEach(l -> l.onEvent(e)); }
        void publishWeak(String e) {
            weakListeners.removeIf(ref -> ref.get() == null); // purge collected entries
            weakListeners.forEach(ref -> {
                EventListener l = ref.get();
                if (l != null) l.onEvent(e);
            });
        }

        int strongSize() { return strongListeners.size(); }
        int weakSize()   { return weakListeners.size(); }
    }

    // -----------------------------------------------------------------------
    // Pattern 3: non-static inner class holds implicit outer-class reference
    // -----------------------------------------------------------------------
    static class Outer {
        private final byte[] bigBuffer = new byte[512 * 1024]; // 512 KB

        // BROKEN: non-static inner class implicitly holds `Outer.this`
        class InnerBroken {
            void run() { System.out.println("  InnerBroken holds outer ref, size=" + bigBuffer.length); }
        }

        // Fix: static inner class has no implicit reference to Outer
        static class InnerFixed {
            void run() { System.out.println("  InnerFixed: no outer reference captured"); }
        }
    }

    // -----------------------------------------------------------------------
    // Pattern 4: ThreadLocal not removed
    // -----------------------------------------------------------------------
    static final ThreadLocal<byte[]> THREAD_LOCAL = new ThreadLocal<>();

    static void leakThreadLocal() {
        THREAD_LOCAL.set(new byte[64 * 1024]); // stored in thread's ThreadLocalMap
        // If this thread is pooled (e.g. tomcat thread pool) and remove() is never called,
        // the 64 KB hangs around for the thread's lifetime.
        System.out.println("  ThreadLocal set (but not removed — leak in pooled thread)");
    }

    static void fixThreadLocal() {
        THREAD_LOCAL.set(new byte[64 * 1024]);
        try {
            System.out.println("  ThreadLocal set, will be removed in finally");
        } finally {
            THREAD_LOCAL.remove(); // always clean up
        }
    }

    // -----------------------------------------------------------------------
    // Demo: simulate "heap growth" counter to show leak vs fix behaviour
    // -----------------------------------------------------------------------
    public static void main(String[] args) throws InterruptedException {
        Runtime rt = Runtime.getRuntime();

        System.out.println("=== Pattern 1: static collection leak ===");
        long before = rt.totalMemory() - rt.freeMemory();
        leakStaticCollection(5000);
        long after = rt.totalMemory() - rt.freeMemory();
        System.out.println("  BROKEN  — heap grew ~" + (after - before) / 1024 + " KB");
        System.out.println("  Entries in static list: " + CACHE_BROKEN.size());

        fixStaticCollection(5000);
        System.gc(); Thread.sleep(100);
        System.out.println("  FIXED   — WeakHashMap size after GC: " + CACHE_FIXED.size() + " (keys collected)");

        System.out.println("\n=== Pattern 2: unregistered listener ===");
        EventBus bus = new EventBus();
        EventListener l1 = e -> {}; // kept in local var → strong ref
        bus.registerStrong(l1);
        bus.registerWeak(l1);
        System.out.println("  After register: strong=" + bus.strongSize() + " weak=" + bus.weakSize());
        // l1 stays alive because of the local variable above; to test weak GC we'd null it
        l1 = null;
        System.gc(); Thread.sleep(100);
        bus.publishWeak("tick"); // triggers purge of collected weak refs
        System.out.println("  After l1=null+GC: strong=" + bus.strongSize()
                + " (leak), weak=" + bus.weakSize() + " (fixed)");

        System.out.println("\n=== Pattern 3: inner class reference ===");
        Outer outer = new Outer();
        Outer.InnerBroken broken = outer.new InnerBroken();
        broken.run();
        Outer.InnerFixed fixed = new Outer.InnerFixed();
        fixed.run();

        System.out.println("\n=== Pattern 4: ThreadLocal leak ===");
        leakThreadLocal();
        fixThreadLocal();
        System.out.println("  ThreadLocal after fix: " + THREAD_LOCAL.get() + " (null = removed)");
    }
}
