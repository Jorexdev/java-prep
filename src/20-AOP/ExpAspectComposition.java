import java.util.HashMap;
import java.util.Map;

public class ExpAspectComposition {

    // -----------------------------------------------------------------------
    // Shared invocation chain abstraction
    // -----------------------------------------------------------------------
    @FunctionalInterface
    interface InvocationChain { Object proceed() throws Exception; }

    interface Aspect {
        int order();           // lower = higher priority (runs outermost)
        String name();
        Object invoke(String method, Object[] args, InvocationChain next) throws Exception;
    }

    // -----------------------------------------------------------------------
    // Aspect 1 — @RateLimit  (order 1, outermost)
    //   Rejects call immediately if limit exceeded; no inner aspect runs.
    // -----------------------------------------------------------------------
    static class RateLimitAspect implements Aspect {
        private final int maxCallsPerRun;
        private int callCount = 0;

        RateLimitAspect(int max) { this.maxCallsPerRun = max; }

        @Override public int order()    { return 1; }
        @Override public String name()  { return "@RateLimit"; }

        @Override
        public Object invoke(String method, Object[] args, InvocationChain next) throws Exception {
            callCount++;
            if (callCount > maxCallsPerRun) {
                System.out.println("    [RateLimit] REJECTED (limit=" + maxCallsPerRun + ")");
                return null; // short-circuit: inner aspects never run
            }
            System.out.println("    [RateLimit] allowed (" + callCount + "/" + maxCallsPerRun + ")");
            return next.proceed();
        }
    }

    // -----------------------------------------------------------------------
    // Aspect 2 — @Cacheable  (order 2)
    //   Returns cached value if present; skips @Transactional and real method.
    // -----------------------------------------------------------------------
    static class CacheAspect implements Aspect {
        private final Map<String, Object> cache = new HashMap<>();

        @Override public int order()   { return 2; }
        @Override public String name() { return "@Cacheable"; }

        @Override
        public Object invoke(String method, Object[] args, InvocationChain next) throws Exception {
            String key = method + ":" + java.util.Arrays.toString(args);
            if (cache.containsKey(key)) {
                System.out.println("    [Cache] HIT for key=" + key);
                return cache.get(key); // short-circuit: no DB call needed
            }
            System.out.println("    [Cache] MISS for key=" + key);
            Object result = next.proceed();
            if (result != null) {
                cache.put(key, result);
                System.out.println("    [Cache] stored result for key=" + key);
            }
            return result;
        }
    }

    // -----------------------------------------------------------------------
    // Aspect 3 — @Transactional  (order 3, innermost before real method)
    //   Opens a transaction, calls the real method, commits or rolls back.
    // -----------------------------------------------------------------------
    static class TransactionalAspect implements Aspect {
        @Override public int order()   { return 3; }
        @Override public String name() { return "@Transactional"; }

        @Override
        public Object invoke(String method, Object[] args, InvocationChain next) throws Exception {
            System.out.println("    [Tx] BEGIN transaction");
            try {
                Object result = next.proceed();
                System.out.println("    [Tx] COMMIT");
                return result;
            } catch (Exception ex) {
                System.out.println("    [Tx] ROLLBACK — " + ex.getMessage());
                throw ex;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Real service method
    // -----------------------------------------------------------------------
    static class PaymentService {
        static String findPayment(String id) {
            System.out.println("    [PaymentService] DB query for id=" + id);
            return "Payment{id=" + id + ", amount=99.0}";
        }
    }

    // -----------------------------------------------------------------------
    // AspectChain — composes N aspects in priority order and runs them
    // -----------------------------------------------------------------------
    static class AspectChain {
        private final Aspect[] aspects;

        AspectChain(Aspect... aspects) {
            this.aspects = aspects.clone();
            // sort by order ascending (lowest order = outermost)
            java.util.Arrays.sort(this.aspects, java.util.Comparator.comparingInt(Aspect::order));
        }

        Object invoke(String method, Object[] args, InvocationChain target) throws Exception {
            System.out.print("  Aspect order: ");
            for (Aspect a : aspects) System.out.print(a.name() + " → ");
            System.out.println("[method]");

            return buildChain(0, method, args, target).proceed();
        }

        private InvocationChain buildChain(int idx, String method, Object[] args, InvocationChain target) {
            if (idx == aspects.length) return target;
            Aspect current = aspects[idx];
            InvocationChain rest = buildChain(idx + 1, method, args, target);
            return () -> current.invoke(method, args, rest);
        }
    }

    public static void main(String[] args) throws Exception {
        RateLimitAspect rateLimit  = new RateLimitAspect(2);
        CacheAspect     cache      = new CacheAspect();
        TransactionalAspect tx     = new TransactionalAspect();

        AspectChain chain = new AspectChain(rateLimit, cache, tx);

        System.out.println("=== Call 1: cache miss → DB path ===");
        Object r1 = chain.invoke("findPayment", new Object[]{"PAY-1"},
                () -> PaymentService.findPayment("PAY-1"));
        System.out.println("  result: " + r1);

        System.out.println("\n=== Call 2: cache HIT → skip @Transactional and DB ===");
        Object r2 = chain.invoke("findPayment", new Object[]{"PAY-1"},
                () -> PaymentService.findPayment("PAY-1"));
        System.out.println("  result: " + r2);

        System.out.println("\n=== Call 3: RateLimit blocks (limit=2, this is call 3) ===");
        Object r3 = chain.invoke("findPayment", new Object[]{"PAY-2"},
                () -> PaymentService.findPayment("PAY-2"));
        System.out.println("  result: " + r3);

        System.out.println("\n--- Key takeaways ---");
        System.out.println("  @Order controls which aspect wraps which: lower = outermost");
        System.out.println("  Any aspect can short-circuit by not calling next.proceed()");
        System.out.println("  Spring default order: undefined unless @Order is explicit");
    }
}
