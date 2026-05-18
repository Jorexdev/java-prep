import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

public class Ejercicio3 {

    static final Map<String, Integer> PRECEDENCIA = Map.of(
            "+", 1, "-", 1, "*", 2, "/", 2
    );

    /** Convierte expresión infija a RPN usando Shunting-yard */
    static String infijaARPN(String expresion) {
        Deque<String> operadores = new ArrayDeque<>();
        StringBuilder salida = new StringBuilder();

        for (String token : expresion.split(" ")) {
            if (token.matches("-?\\d+(\\.\\d+)?")) {
                // Número: va directo a la salida
                salida.append(token).append(" ");
            } else if (PRECEDENCIA.containsKey(token)) {
                // Operador: sacar operadores de mayor o igual precedencia
                while (!operadores.isEmpty()
                        && !operadores.peek().equals("(")
                        && PRECEDENCIA.getOrDefault(operadores.peek(), -1) >= PRECEDENCIA.get(token)) {
                    salida.append(operadores.pop()).append(" ");
                }
                operadores.push(token);
            } else if (token.equals("(")) {
                operadores.push(token);
            } else if (token.equals(")")) {
                while (!operadores.isEmpty() && !operadores.peek().equals("(")) {
                    salida.append(operadores.pop()).append(" ");
                }
                if (!operadores.isEmpty()) operadores.pop(); // elimina '('
            }
        }
        while (!operadores.isEmpty()) {
            salida.append(operadores.pop()).append(" ");
        }
        return salida.toString().trim();
    }

    /** Evalúa expresión RPN */
    static double evaluarRPN(String rpn) {
        Deque<Double> stack = new ArrayDeque<>();
        for (String token : rpn.split(" ")) {
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
        String[] expresiones = {
            "( 3 + 4 ) * 2",       // 14
            "3 + 4 * 2",           // 11  (precedencia sin paréntesis)
            "( 1 + 2 ) * ( 3 + 4 )", // 21
            "10 / ( 2 + 3 ) * 4"   // 8
        };

        for (String expr : expresiones) {
            String rpn = infijaARPN(expr);
            double resultado = evaluarRPN(rpn);
            System.out.printf("  %-30s → RPN: %-20s = %.1f%n", expr, rpn, resultado);
        }
    }
}
