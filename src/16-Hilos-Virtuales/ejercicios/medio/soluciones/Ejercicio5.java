public class Ejercicio5 {

    // ScopedValue: alternativa inmutable y thread-safe a ThreadLocal
    // El valor solo es visible dentro del bloque where().run()
    static final ScopedValue<String> USER_ID = ScopedValue.newInstance();

    static void nivel3() {
        System.out.println("    [nivel3] USER_ID = " + USER_ID.get());
    }

    static void nivel2() {
        System.out.println("  [nivel2] USER_ID = " + USER_ID.get());
        nivel3();
    }

    static void nivel1() {
        System.out.println("[nivel1] USER_ID = " + USER_ID.get());
        nivel2();
    }

    public static void main(String[] args) {
        System.out.println("=== Scoped Values ===\n");

        // Verificar que fuera del scope no hay valor
        System.out.println("Fuera del scope, USER_ID.isBound() = " + USER_ID.isBound());
        try {
            USER_ID.get(); // debe lanzar NoSuchElementException
            System.out.println("ERROR: deberia haber lanzado excepcion");
        } catch (java.util.NoSuchElementException e) {
            System.out.println("USER_ID.get() fuera del scope -> NoSuchElementException [OK]");
        }

        System.out.println();
        System.out.println("Ejecutando dentro del scope con USER_ID='user-42':");
        System.out.println("-".repeat(45));

        ScopedValue.where(USER_ID, "user-42").run(() -> {
            System.out.println("Dentro del run(), USER_ID.isBound() = " + USER_ID.isBound());
            nivel1(); // llamada encadenada de 3 niveles sin pasar el valor como parametro
        });

        System.out.println("-".repeat(45));
        System.out.println();

        // Fuera del scope, el valor ya no existe
        System.out.println("Despues del run(), USER_ID.isBound() = " + USER_ID.isBound());
        System.out.println();

        // Anidado: un scope puede sobreescribir el valor para su sub-arbol
        System.out.println("Demo de scope anidado:");
        ScopedValue.where(USER_ID, "admin").run(() -> {
            System.out.println("  Scope externo: " + USER_ID.get());
            ScopedValue.where(USER_ID, "guest").run(() -> {
                System.out.println("    Scope interno (sobreescrito): " + USER_ID.get());
            });
            System.out.println("  Scope externo (restaurado): " + USER_ID.get());
        });

        System.out.println();
        System.out.println("=== Ventajas vs ThreadLocal ===");
        System.out.println("ScopedValue: inmutable, sin set(), limpieza automatica al salir del scope.");
        System.out.println("ThreadLocal: mutable, puede filtrarse entre tareas en thread pools.");
        System.out.println("Con virtual threads, ThreadLocal puede ser muy costoso; ScopedValue es preferido.");
    }
}
