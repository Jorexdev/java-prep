import java.util.ArrayDeque;
import java.util.Deque;

public class Ejercicio5 {

    static double evaluarRPN(String expresion) {
        Deque<Double> stack = new ArrayDeque<>();

        for (String token : expresion.split(" ")) {
            switch (token) {
                case "+" -> { double b = stack.pop(), a = stack.pop(); stack.push(a + b); }
                case "-" -> { double b = stack.pop(), a = stack.pop(); stack.push(a - b); }
                case "*" -> { double b = stack.pop(), a = stack.pop(); stack.push(a * b); }
                case "/" -> { double b = stack.pop(), a = stack.pop(); stack.push(a / b); }
                default  -> stack.push(Double.parseDouble(token));
            }
        }
        return stack.pop();
    }

    public static void main(String[] args) {
        // (3 + 4) * 2 / 7 = 2.0
        String expr1 = "3 4 + 2 * 7 /";
        System.out.println(expr1 + " = " + evaluarRPN(expr1));

        // 5 + ((1 + 2) * 4) − 3 = 14
        String expr2 = "5 1 2 + 4 * + 3 -";
        System.out.println(expr2 + " = " + evaluarRPN(expr2));

        // 2 ^ simple: 2 * 3 + 4 = 10
        String expr3 = "2 3 * 4 +";
        System.out.println(expr3 + " = " + evaluarRPN(expr3));
    }
}
