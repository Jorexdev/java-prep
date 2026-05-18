public class Ejercicio4 {

    static class Buffer {
        private String elemento = null;

        synchronized void producir(String valor) throws InterruptedException {
            while (elemento != null) wait();
            elemento = valor;
            System.out.println("Producido: " + valor);
            notifyAll();
        }

        synchronized String consumir() throws InterruptedException {
            while (elemento == null) wait();
            String valor = elemento;
            elemento = null;
            System.out.println("Consumido: " + valor);
            notifyAll();
            return valor;
        }
    }

    public static void main(String[] args) throws Exception {
        Buffer buffer = new Buffer();
        Thread productor  = new Thread(() -> { try { for (int i = 1; i <= 5; i++) { buffer.producir("item-" + i); Thread.sleep(50); } } catch (InterruptedException e) { Thread.currentThread().interrupt(); } });
        Thread consumidor = new Thread(() -> { try { for (int i = 0; i < 5; i++)  { buffer.consumir(); Thread.sleep(80); } } catch (InterruptedException e) { Thread.currentThread().interrupt(); } });
        productor.start(); consumidor.start();
        productor.join();  consumidor.join();
    }
}
