import java.util.Optional;

public class Ejercicio7 {

    // Simula una operación costosa — imprime un mensaje para saber cuándo se ejecuta
    static String valorCostoso() {
        System.out.println("  [valorCostoso] Calculando valor costoso...");
        return "resultado-costoso";
    }

    public static void main(String[] args) {
        Optional<String> opt = Optional.of("valor");

        System.out.println("=== orElse ===");
        // orElse evalúa su argumento SIEMPRE, aunque opt tenga valor
        // → "Calculando valor costoso..." se imprime aunque no se use el resultado
        String resultado1 = opt.orElse(valorCostoso());
        System.out.println("Resultado: " + resultado1);

        System.out.println("\n=== orElseGet ===");
        // orElseGet recibe un Supplier y solo lo invoca si opt está vacío
        // → como opt tiene valor, el Supplier NO se ejecuta
        String resultado2 = opt.orElseGet(() -> valorCostoso());
        System.out.println("Resultado: " + resultado2);

        System.out.println("\nConclusión: orElse siempre evalúa; orElseGet es lazy.");
    }
}
