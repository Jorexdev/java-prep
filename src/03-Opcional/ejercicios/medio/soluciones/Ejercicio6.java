import java.util.Optional;

public class Ejercicio6 {

    // Fuente local — en este ejemplo siempre vacía (simula que no hay config local)
    static Optional<String> configuracionLocal() {
        return Optional.empty();
    }

    // Fuente remota — siempre tiene valor como fallback
    static Optional<String> configuracionRemota() {
        System.out.println("  [remota] Consultando configuración remota...");
        return Optional.of("config-remota");
    }

    // Fuente local alternativa — tiene valor para el segundo escenario
    static Optional<String> configuracionLocalConValor() {
        return Optional.of("config-local");
    }

    public static void main(String[] args) {
        System.out.println("=== Caso 1: local vacía → cae en remota ===");
        Optional<String> config1 = configuracionLocal()
                .or(() -> configuracionRemota());
        System.out.println("Configuración: " + config1.orElse("ninguna"));

        System.out.println("\n=== Caso 2: local con valor → no consulta remota ===");
        Optional<String> config2 = configuracionLocalConValor()
                .or(() -> configuracionRemota()); // el Supplier no se invoca
        System.out.println("Configuración: " + config2.orElse("ninguna"));
    }
}
