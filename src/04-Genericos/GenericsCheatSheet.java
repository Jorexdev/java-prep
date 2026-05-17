/*
    GENERICS EN JAVA — Cheat Sheet

    Concepto:
    Los genéricos permiten parametrizar tipos. Escribes clases, interfaces y métodos
    que funcionan con distintos tipos manteniendo type-safety en tiempo de compilación.
    Sintaxis: <> (operador diamante). Convención: T=Type, E=Element, K=Key, V=Value, R=Return.

    Clases genéricas:
        class Caja<T> {
            private T contenido;
            public void set(T valor) { this.contenido = valor; }
            public T get() { return contenido; }
        }
        Caja<String> c1 = new Caja<>();   // T = String
        Caja<Integer> c2 = new Caja<>();  // T = Integer

    Métodos genéricos:
        public static <T> void imprimir(T valor) { System.out.println(valor); }
        imprimir("Hola"); // T inferido como String

    Bounded Types (límites):
        <T extends Number>        // Upper Bound — T y subclases. Lectura como Number, sin escritura.
        <? super Integer>         // Lower Bound — T y superclases. Escritura de Integer, lectura como Object.

    Regla PECS:
        Producer Extends → colección produce datos, usa extends.
        Consumer Super   → colección consume datos, usa super.

    Wildcards (?):
        List<?>            → tipo desconocido, solo lectura (salvo null).
        List<? extends T>  → T o subtipos. Lee como T, no añade.
        List<? super T>    → T o supertipos. Añade T, lee como Object.

    Type Erasure:
        Los genéricos existen solo en compilación. En runtime se convierten en Object.
        Caja<String> y Caja<Integer> son la misma clase en runtime.
        Restricciones:
          - No se pueden crear arrays de genéricos: new List<String>[10] ❌
          - No se puede usar instanceof con tipo parametrizado ❌
          - No se puede hacer new T() — usar Supplier<T> o Class<T>
          - No se admiten primitivos: List<int> ❌ → List<Integer> ✅

    Multiple Bounds:
        class Ejemplo<T extends Number & Comparable<T>> { ... }
        Si hay clase, debe ir primero; luego interfaces.
*/
public class GenericsCheatSheet {}
