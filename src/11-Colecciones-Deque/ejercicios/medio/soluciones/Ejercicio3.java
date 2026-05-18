import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio3 {

    static class Browser {
        private final Deque<String> historial = new ArrayDeque<>();
        private final Deque<String> forward   = new ArrayDeque<>();
        private String actual = null;

        void visit(String url) {
            if (actual != null) historial.push(actual);
            actual = url;
            forward.clear(); // limpiar forward al visitar nueva página
            System.out.println("Visitando: " + actual);
        }

        void back() {
            if (historial.isEmpty()) {
                System.out.println("back() — sin historial previo");
                return;
            }
            forward.push(actual);
            actual = historial.pop();
            System.out.println("back() → " + actual);
        }

        void forward() {
            if (forward.isEmpty()) {
                System.out.println("forward() — no hay páginas hacia adelante");
                return;
            }
            historial.push(actual);
            actual = forward.pop();
            System.out.println("forward() → " + actual);
        }
    }

    public static void main(String[] args) {
        Browser browser = new Browser();

        browser.visit("https://google.com");
        browser.visit("https://github.com");
        browser.visit("https://stackoverflow.com");
        browser.back();
        browser.back();
        browser.forward();
        browser.visit("https://spring.io"); // limpia forward
        browser.forward(); // sin páginas hacia adelante
    }
}
