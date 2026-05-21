import java.util.List;

public class Ejercicio3 {

    interface Interceptor {
        boolean preHandle(String req);
        void postHandle(String req, long ms);
    }

    interface Handler {
        String handle(String req);
    }

    static class TimingInterceptor implements Interceptor {
        private long start;

        @Override
        public boolean preHandle(String req) {
            start = System.nanoTime();
            System.out.println("[TimingInterceptor] preHandle: " + req);
            return true;
        }

        @Override
        public void postHandle(String req, long ms) {
            long elapsed = System.nanoTime() - start;
            System.out.println("[TimingInterceptor] postHandle: " + req + " — " + elapsed / 1_000_000.0 + " ms");
        }
    }

    static class Dispatcher {
        private final List<Interceptor> interceptors;

        Dispatcher(List<Interceptor> interceptors) {
            this.interceptors = interceptors;
        }

        String dispatch(String req, Handler handler) {
            long start = System.nanoTime();
            for (Interceptor i : interceptors) {
                if (!i.preHandle(req)) return null;
            }
            String result = handler.handle(req);
            long elapsed = System.nanoTime() - start;
            for (Interceptor i : interceptors) {
                i.postHandle(req, elapsed / 1_000_000);
            }
            return result;
        }
    }

    public static void main(String[] args) {
        Dispatcher dispatcher = new Dispatcher(List.of(new TimingInterceptor()));

        System.out.println("-- Handler A --");
        String r1 = dispatcher.dispatch("GET /productos", req -> {
            return "Lista de productos";
        });
        System.out.println("Resultado: " + r1);

        System.out.println("\n-- Handler B --");
        String r2 = dispatcher.dispatch("GET /usuarios", req -> {
            return "Lista de usuarios";
        });
        System.out.println("Resultado: " + r2);
    }
}
