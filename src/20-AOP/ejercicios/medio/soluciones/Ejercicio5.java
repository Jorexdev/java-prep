import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class Ejercicio5 {

    // Interfaz del gestor de transacciones (simula PlatformTransactionManager de Spring)
    interface TransactionManager {
        void begin();
        void commit();
        void rollback();
    }

    // Implementacion de demo
    static class ConsoleTransactionManager implements TransactionManager {
        @Override public void begin()    { System.out.println("  [TX] BEGIN"); }
        @Override public void commit()   { System.out.println("  [TX] COMMIT"); }
        @Override public void rollback() { System.out.println("  [TX] ROLLBACK"); }
    }

    // Interfaz de negocio
    interface PedidoServicio {
        void crearPedido(String producto, int cantidad);
        void cancelarPedido(String pedidoId);
    }

    static class PedidoServicioReal implements PedidoServicio {
        @Override
        public void crearPedido(String producto, int cantidad) {
            System.out.println("  [Real] Creando pedido: " + producto + " x" + cantidad);
            // Exito
        }

        @Override
        public void cancelarPedido(String pedidoId) {
            System.out.println("  [Real] Cancelando pedido: " + pedidoId);
            if (pedidoId.equals("PEDIDO-999")) {
                throw new RuntimeException("Pedido no encontrado: " + pedidoId);
            }
            System.out.println("  [Real] Pedido cancelado con exito");
        }
    }

    // TransactionalAspect: envuelve cada metodo en una transaccion
    // Equivale a @Transactional de Spring
    @SuppressWarnings("unchecked")
    static <T> T wrapTransactional(T target, TransactionManager tx) {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            (proxy, method, args) -> {
                tx.begin();
                try {
                    Object result = method.invoke(target, args);
                    tx.commit();
                    return result;
                } catch (InvocationTargetException e) {
                    tx.rollback();
                    throw e.getCause();
                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }
        );
    }

    public static void main(String[] args) {
        System.out.println("=== @Transactional simulado ===\n");

        TransactionManager tx = new ConsoleTransactionManager();
        PedidoServicio servicio = wrapTransactional(new PedidoServicioReal(), tx);

        System.out.println("--- Caso 1: operacion exitosa ---");
        servicio.crearPedido("Laptop", 2);
        System.out.println();

        System.out.println("--- Caso 2: operacion con ID valido ---");
        servicio.cancelarPedido("PEDIDO-123");
        System.out.println();

        System.out.println("--- Caso 3: operacion que falla -> ROLLBACK ---");
        try {
            servicio.cancelarPedido("PEDIDO-999");
        } catch (RuntimeException e) {
            System.out.println("  [App] Excepcion capturada: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Equivalencia en Spring ===");
        System.out.println("@Transactional en Spring hace exactamente esto:");
        System.out.println("  1. begin()   -> TransactionSynchronizationManager.bindResource()");
        System.out.println("  2. proceed() -> ejecucion del metodo");
        System.out.println("  3. commit()  -> si no hay excepcion o la excepcion es noRollbackFor");
        System.out.println("  4. rollback()-> si la excepcion es RuntimeException o esta en rollbackFor");
        System.out.println("La propagacion (REQUIRED, REQUIRES_NEW, etc.) controla si se reutiliza");
        System.out.println("la transaccion existente o se crea una nueva.");
    }
}
