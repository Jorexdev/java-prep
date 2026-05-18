import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio2 {

    static boolean estaBalanceado(String expr) {
        Deque<Character> stack = new ArrayDeque<>();

        for (char c : expr.toCharArray()) {
            switch (c) {
                case '(', '{', '[' -> stack.push(c);
                case ')' -> { if (stack.isEmpty() || stack.pop() != '(') return false; }
                case '}' -> { if (stack.isEmpty() || stack.pop() != '{') return false; }
                case ']' -> { if (stack.isEmpty() || stack.pop() != '[') return false; }
            }
        }
        return stack.isEmpty();
    }

    public static void main(String[] args) {
        String[] expresiones = {"({[]})", "([)]", "{[]}", "(((",  "", "({[}", "()[]{}"};

        for (String expr : expresiones) {
            System.out.printf("  %-12s → %s%n", "\"" + expr + "\"",
                    estaBalanceado(expr) ? "BALANCEADO" : "NO balanceado");
        }
    }
}
