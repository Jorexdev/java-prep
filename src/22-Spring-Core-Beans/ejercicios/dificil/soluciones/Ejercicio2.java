import java.util.function.Supplier;

public class Ejercicio2 {

    static class ScopedProxy<T> {
        private final Supplier<T> factory;

        ScopedProxy(Supplier<T> factory) {
            this.factory = factory;
        }

        T get() {
            return factory.get();
        }
    }

    static class ConexionBD {
        private static int count = 0;
        final int id;

        ConexionBD() {
            this.id = ++count;
            System.out.println("    [ConexionBD] Nueva instancia #" + id + " creada");
        }

        void ejecutar(String sql) {
            System.out.println("    [ConexionBD #" + id + "] Ejecutando: " + sql);
        }
    }

    // Singleton que necesita un prototype en cada operación
    static class ServicioPedidos {
        // Inyección de prototype a través de ScopedProxy
        private final ScopedProxy<ConexionBD> conexionProxy;

        ServicioPedidos(ScopedProxy<ConexionBD> proxy) {
            this.conexionProxy = proxy;
        }

        void procesarPedido(int id) {
            ConexionBD conn = conexionProxy.get(); // nueva instancia cada llamada
            conn.ejecutar("INSERT INTO pedidos VALUES (" + id + ")");
        }
    }

    public static void main(String[] args) {
        System.out.println("=== A) Singleton directo — siempre el mismo objeto ===");
        ConexionBD singleton = new ConexionBD();
        System.out.println("Acceso 1: " + singleton.hashCode());
        System.out.println("Acceso 2: " + singleton.hashCode());
        System.out.println("Mismo objeto: " + true);

        System.out.println("\n=== B) ScopedProxy wrapping prototype — nuevo en cada acceso ===");
        ScopedProxy<ConexionBD> proxy = new ScopedProxy<>(ConexionBD::new);
        ConexionBD c1 = proxy.get();
        ConexionBD c2 = proxy.get();
        System.out.println("c1.id=" + c1.id + ", c2.id=" + c2.id + ", mismos: " + (c1 == c2));

        System.out.println("\n=== C) Caso real: singleton inyectado con prototype via proxy ===");
        ServicioPedidos servicio = new ServicioPedidos(new ScopedProxy<>(ConexionBD::new));
        System.out.println("  ServicioPedidos es singleton, ConexionBD es prototype:");
        servicio.procesarPedido(1001);
        servicio.procesarPedido(1002);
        servicio.procesarPedido(1003);
        System.out.println("  Cada pedido usó una conexión distinta.");
    }
}
