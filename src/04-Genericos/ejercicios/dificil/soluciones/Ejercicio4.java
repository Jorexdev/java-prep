import java.util.ArrayList;
import java.util.List;

public class Ejercicio4 {

    static class Stack<T> {
        private final List<T> elementos = new ArrayList<>();

        synchronized void push(T elemento) {
            elementos.add(elemento);
        }

        synchronized T pop() {
            if (isEmpty()) throw new java.util.EmptyStackException();
            return elementos.remove(elementos.size() - 1);
        }

        synchronized T peek() {
            if (isEmpty()) throw new java.util.EmptyStackException();
            return elementos.get(elementos.size() - 1);
        }

        synchronized boolean isEmpty() {
            return elementos.isEmpty();
        }

        synchronized int size() {
            return elementos.size();
        }
    }

    public static void main(String[] args) throws InterruptedException {

        Stack<Integer> stack = new Stack<>();

        Thread productor = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                stack.push(i);
                System.out.println("Push: " + i);
            }
        });

        Thread consumidor = new Thread(() -> {
            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            while (!stack.isEmpty()) {
                System.out.println("Pop:  " + stack.pop());
            }
        });

        productor.start();
        consumidor.start();
        productor.join();
        consumidor.join();

        System.out.println("Stack vacío: " + stack.isEmpty());
    }
}
