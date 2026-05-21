// @ControllerAdvice
public class Ejercicio6 {

    interface Handler {
        Object handle(String request);
    }

    // @RestController
    static class ProductoHandler implements Handler {
        @Override
        public Object handle(String request) {
            if ("bad".equals(request)) {
                throw new IllegalArgumentException("Producto no encontrado");
            }
            return "Producto: " + request;
        }
    }

    static class GlobalExceptionHandler implements Handler {
        private final Handler delegate;

        GlobalExceptionHandler(Handler delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object handle(String request) {
            try {
                return delegate.handle(request);
            } catch (Exception e) {
                return "Error 400: " + e.getMessage();
            }
        }
    }

    public static void main(String[] args) {
        Handler handler = new GlobalExceptionHandler(new ProductoHandler());

        System.out.println(handler.handle("laptop"));
        System.out.println(handler.handle("bad"));
    }
}
