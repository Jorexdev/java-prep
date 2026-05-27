import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ExpDequePatterns {

    // ── 1. SLIDING WINDOW MAX — O(n) con deque monotónica ────────────────────
    // Problema: dado un array y un tamaño de ventana k, devolver el máximo de
    // cada ventana de tamaño k.
    // Fuerza bruta: O(n·k). Con deque monotónica: O(n).
    //
    // Invariante: la deque almacena ÍNDICES en orden decreciente de valor.
    // El frente siempre apunta al máximo de la ventana actual.
    static int[] slidingWindowMax(int[] nums, int k) {
        int n = nums.length;
        int[] resultado = new int[n - k + 1];
        // Deque de índices — el frente es siempre el índice del máximo
        Deque<Integer> dq = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            // Eliminar índices fuera de la ventana actual
            if (!dq.isEmpty() && dq.peekFirst() < i - k + 1) {
                dq.pollFirst();
            }
            // Eliminar desde el final los índices cuyo valor es menor al actual
            // — nunca serán el máximo mientras nums[i] esté en la ventana
            while (!dq.isEmpty() && nums[dq.peekLast()] < nums[i]) {
                dq.pollLast();
            }
            dq.offerLast(i);

            // La ventana completa empieza cuando i >= k-1
            if (i >= k - 1) {
                resultado[i - k + 1] = nums[dq.peekFirst()];
            }
        }
        return resultado;
    }

    // ── 2. PALINDROME CHECKER con deque ──────────────────────────────────────
    // Añadimos cada carácter al final; luego comparamos frente y fondo.
    // La deque permite acceso O(1) a ambos extremos, ideal para este patrón.
    static boolean esPalindromo(String s) {
        Deque<Character> dq = new ArrayDeque<>();
        for (char c : s.toCharArray()) dq.offerLast(c);

        while (dq.size() > 1) {
            if (!dq.pollFirst().equals(dq.pollLast())) return false;
        }
        return true;
    }

    // ── 3. HISTORIAL DE NAVEGADOR ────────────────────────────────────────────
    // Dos deques simulan historial hacia atrás y hacia adelante.
    // navigate(): guarda página actual en historial-back, limpia historial-forward.
    // back():     pasa la página actual a forward y retrocede al frente de back.
    // forward():  pasa la página actual a back y avanza al frente de forward.
    static class Navegador {
        private final Deque<String> back    = new ArrayDeque<>();
        private final Deque<String> forward = new ArrayDeque<>();
        private String actual = "about:blank";

        void navegar(String url) {
            back.push(actual);      // guardamos la actual en historial back
            forward.clear();        // nueva navegación invalida el historial forward
            actual = url;
            System.out.println("  navegar → " + actual);
        }

        void atras() {
            if (back.isEmpty()) { System.out.println("  atras → sin historial"); return; }
            forward.push(actual);   // guardamos la actual en forward
            actual = back.pop();
            System.out.println("  atras  → " + actual);
        }

        void adelante() {
            if (forward.isEmpty()) { System.out.println("  adelante → sin forward"); return; }
            back.push(actual);
            actual = forward.pop();
            System.out.println("  adelante → " + actual);
        }

        String pagina() { return actual; }
    }

    // ── 4. RATE LIMITER con ventana deslizante ────────────────────────────────
    // Guardamos timestamps de peticiones en una deque.
    // Al llegar una nueva petición: eliminamos del frente los timestamps
    // anteriores a (ahora - ventana). Si quedan < maxPeticiones, permitimos.
    //
    // La deque es perfecta porque solo añadimos al final y eliminamos del frente.
    static class RateLimiter {
        private final Deque<Long> timestamps = new ArrayDeque<>();
        private final int maxPeticiones;
        private final long ventanaMs;

        RateLimiter(int max, long ventanaMs) {
            this.maxPeticiones = max;
            this.ventanaMs = ventanaMs;
        }

        boolean permitir(long ahoraMs) {
            // Limpiar timestamps fuera de la ventana
            while (!timestamps.isEmpty() && timestamps.peekFirst() < ahoraMs - ventanaMs) {
                timestamps.pollFirst();
            }
            if (timestamps.size() < maxPeticiones) {
                timestamps.offerLast(ahoraMs);
                return true;
            }
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Sliding window max — O(n) con deque monotónica ===");
        int[] nums = {1, 3, -1, -3, 5, 3, 6, 7};
        int k = 3;
        int[] resultado = slidingWindowMax(nums, k);
        System.out.print("  nums=" + java.util.Arrays.toString(nums) + "  k=" + k);
        System.out.println("  → maximos=" + java.util.Arrays.toString(resultado));
        // → [3, 3, 5, 5, 6, 7]

        System.out.println("\n  Por qué Deque: acceso O(1) a ambos extremos.");
        System.out.println("  addLast para encolar candidatos, pollFirst para retirar expirados,");
        System.out.println("  pollLast para mantener orden decreciente (invariante monotónica).");

        System.out.println("\n=== 2. Palindrome checker con deque ===");
        String[] palabras = {"radar", "java", "level", "racecar", "hello"};
        for (String p : palabras) {
            System.out.printf("  %-10s → %s%n", p, esPalindromo(p) ? "palindromo" : "no palindromo");
        }
        System.out.println("  Por qué Deque: pollFirst + pollLast son O(1), sin necesidad de índices.");

        System.out.println("\n=== 3. Historial de navegador ===");
        Navegador nav = new Navegador();
        nav.navegar("google.com");
        nav.navegar("github.com");
        nav.navegar("stackoverflow.com");
        nav.atras();
        nav.atras();
        nav.adelante();
        nav.navegar("reddit.com");  // invalida forward
        nav.adelante();             // sin historial forward
        System.out.println("  Página actual: " + nav.pagina());
        System.out.println("  Por qué Deque: push/pop O(1) en ambos extremos = historial eficiente.");

        System.out.println("\n=== 4. Rate limiter con ventana deslizante ===");
        // 3 peticiones máx en 1000 ms
        RateLimiter rl = new RateLimiter(3, 1000);
        long[] tiempos = {0, 200, 500, 800, 900, 1100, 1500};
        for (long t : tiempos) {
            boolean ok = rl.permitir(t);
            System.out.printf("  t=%4dms → %s%n", t, ok ? "PERMITIDA" : "BLOQUEADA");
        }
        System.out.println("  Por qué Deque: offerLast para registrar, pollFirst para expirar — O(1).");
    }
}
