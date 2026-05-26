import java.util.ArrayList;
import java.util.List;

public class ExpAdviceTypes {

    // -----------------------------------------------------------------------
    // Simple result type for the PaymentService
    // -----------------------------------------------------------------------
    static class PaymentResult {
        final String id;
        final double amount;
        PaymentResult(String id, double amount) { this.id = id; this.amount = amount; }
        @Override public String toString() { return "Payment{id=" + id + ", amount=" + amount + "}"; }
    }

    // -----------------------------------------------------------------------
    // Target: the real business logic (the "joinpoint")
    // -----------------------------------------------------------------------
    static class PaymentService {
        PaymentResult process(String id, double amount) {
            if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
            System.out.println("    [PaymentService] processing " + id + " for $" + amount);
            return new PaymentResult(id, amount);
        }
    }

    // -----------------------------------------------------------------------
    // Advice contracts
    // -----------------------------------------------------------------------
    interface BeforeAdvice    { void before(String method, Object[] args); }
    interface AfterReturningAdvice { void afterReturning(String method, Object result); }
    interface AfterThrowingAdvice  { void afterThrowing(String method, Throwable ex); }
    interface AfterAdvice     { void after(String method); }  // always runs, like finally
    interface AroundAdvice    {
        Object around(String method, Object[] args, InvocationCallback target) throws Throwable;
    }

    @FunctionalInterface
    interface InvocationCallback { Object proceed() throws Throwable; }

    // -----------------------------------------------------------------------
    // Concrete advice implementations
    // -----------------------------------------------------------------------
    static class LoggingBeforeAdvice implements BeforeAdvice {
        @Override public void before(String method, Object[] args) {
            System.out.printf("  [Before]         %s called with %s%n", method, java.util.Arrays.toString(args));
        }
    }

    static class AuditAfterReturningAdvice implements AfterReturningAdvice {
        @Override public void afterReturning(String method, Object result) {
            System.out.printf("  [AfterReturning] %s succeeded → %s%n", method, result);
        }
    }

    static class AlertAfterThrowingAdvice implements AfterThrowingAdvice {
        @Override public void afterThrowing(String method, Throwable ex) {
            System.out.printf("  [AfterThrowing]  %s threw: %s%n", method, ex.getMessage());
        }
    }

    static class CleanupAfterAdvice implements AfterAdvice {
        @Override public void after(String method) {
            System.out.printf("  [After]          %s finished (always runs)%n", method);
        }
    }

    static class TimingAroundAdvice implements AroundAdvice {
        @Override public Object around(String method, Object[] args, InvocationCallback target) throws Throwable {
            long start = System.nanoTime();
            System.out.printf("  [Around-before]  %s starting timer%n", method);
            Object result = target.proceed();
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.printf("  [Around-after]   %s took %d ms%n", method, ms);
            return result;
        }
    }

    // -----------------------------------------------------------------------
    // AspectWeaver — applies all advice types around a target call
    // -----------------------------------------------------------------------
    static class AspectWeaver {
        private final BeforeAdvice         before         = new LoggingBeforeAdvice();
        private final AfterReturningAdvice afterReturning = new AuditAfterReturningAdvice();
        private final AfterThrowingAdvice  afterThrowing  = new AlertAfterThrowingAdvice();
        private final AfterAdvice          after          = new CleanupAfterAdvice();
        private final AroundAdvice         around         = new TimingAroundAdvice();

        Object invoke(String method, Object[] args, InvocationCallback target) {
            before.before(method, args);

            try {
                Object result = around.around(method, args, target);
                afterReturning.afterReturning(method, result);
                return result;
            } catch (Throwable ex) {
                afterThrowing.afterThrowing(method, ex);
                return null;
            } finally {
                after.after(method);
            }
        }
    }

    public static void main(String[] args) {
        PaymentService svc = new PaymentService();
        AspectWeaver weaver = new AspectWeaver();

        System.out.println("=== Call that succeeds ===");
        System.out.println("  Expected chain: Before → Around-before → [method] → Around-after → AfterReturning → After");
        weaver.invoke("process", new Object[]{"TXN-001", 150.0},
                () -> svc.process("TXN-001", 150.0));

        System.out.println();
        System.out.println("=== Call that throws ===");
        System.out.println("  Expected chain: Before → Around-before → [exception] → AfterThrowing → After");
        weaver.invoke("process", new Object[]{"TXN-002", -1.0},
                () -> svc.process("TXN-002", -1.0));

        System.out.println();
        System.out.println("--- Advice type summary ---");
        System.out.println("  @Before          → runs before the method (validation, logging)");
        System.out.println("  @AfterReturning  → runs only on success (audit, metrics)");
        System.out.println("  @AfterThrowing   → runs only on exception (alerts, error tracking)");
        System.out.println("  @After           → always runs, like finally (cleanup, close resources)");
        System.out.println("  @Around          → full control: timing, caching, retry, modify result");
    }
}
