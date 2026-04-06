package base.genericos.ejemplos;

// GENERADA POR CHATGPT //

/*
    ==============================
           GENERICS EN JAVA
    ==============================

    ► Concepto:
      Los genéricos permiten parametrizar tipos. Con ellos puedes escribir clases, 
      interfaces y métodos que funcionen con distintos tipos manteniendo seguridad 
      de tipos (type-safety) en tiempo de compilación.

    ► Sintaxis:
      - <> se llama "operador diamante".
      - Por convención se usan letras en mayúscula:
        T = Type
        E = Element
        K = Key
        V = Value
        R = Return type

      Ejemplo: 
        class Caja<T> { ... }   // Clase genérica
        <T> void metodo(T x) { } // Métod0 genérico

    --------------------------------------------------------------------------------
    CLASES GENÉRICAS
    --------------------------------------------------------------------------------

      class Caja<T> {
          private T contenido;
          public void set(T valor) { this.contenido = valor; }
          public T get() { return contenido; }
      }

      // Al instanciar decides el tipo:
      Caja<String> c1 = new Caja<>();
      Caja<Integer> c2 = new Caja<>();

      - T se sustituye por el tipo que declares.
      - El compilador valida que solo guardes/saques ese tipo.

    --------------------------------------------------------------------------------
    MÉTODOS GENÉRICOS
    --------------------------------------------------------------------------------

      public static <T> void imprimir(T valor) {
          System.out.println(valor);
      }

      - Se declaran con <T> antes del tipo de retorno.
      - <T> es independiente del genérico de la clase.
      - Se infiere automáticamente en la llamada:
        imprimir("Hola");  // T = String
        imprimir(10);      // T = Integer

    --------------------------------------------------------------------------------
    BOUNDED TYPES (LIMITES)
    --------------------------------------------------------------------------------

      // Upper Bound (extends) → acepta T y subclases
      <T extends Number> void sumar(List<T> nums) { ... }

      // Lower Bound (super) → acepta T y superclases
      void agregar(List<? super Integer> nums) { ... }

      ► Upper Bound:
        - Puedo leer como Number.
        - No puedo añadir nada (salvo null).

      ► Lower Bound:
        - Puedo añadir Integer.
        - Solo puedo leer como Object.

      ► Regla PECS:
        Producer Extends → cuando la colección produce datos, usa extends.
        Consumer Super   → cuando la colección consume datos, usa super.

    --------------------------------------------------------------------------------
    WILDCARDS (?)
    --------------------------------------------------------------------------------

      - ? significa "algún tipo desconocido".
      - Ejemplo: List<?> lista = ...;

      ► ? extends T
        List<? extends Number> = lista de Number o subtipos (Integer, Double...).
        * Lectura: puedo sacar como Number.
        * Escritura: no puedo añadir (excepto null).

      ► ? super T
        List<? super Integer> = lista de Integer o supertipos (Number, Object).
        * Escritura: puedo añadir Integer.
        * Lectura: solo puedo sacar como Object.

    --------------------------------------------------------------------------------
    TYPE ERASURE
    --------------------------------------------------------------------------------

      - Los genéricos existen solo en tiempo de compilación.
      - En runtime desaparecen (se convierten en Object).
      - Caja<String> y Caja<Integer> son la misma clase en runtime.
      - Restricciones derivadas:
        * No se pueden crear arrays de genéricos: new List<String>[10] ❌
        * No se puede usar instanceof con tipo parametrizado: x instanceof List<String> ❌
        * No se puede hacer new T(): hay que usar Supplier<T> o Class<T>.
        * No se pueden usar tipos primitivos en generics: List<int> ❌ (usar List<Integer> ✅).

    --------------------------------------------------------------------------------
    MULTIPLE BOUNDS
    --------------------------------------------------------------------------------

      class Ejemplo<T extends Number & Comparable<T>> { ... }

      - Puedes poner varias restricciones (una clase + múltiples interfaces).
      - Si hay clase, debe ir primero.
      - Ejemplo: T debe ser Number y además Comparable.

    --------------------------------------------------------------------------------
    CONVENCIONES DE NOMBRES
    --------------------------------------------------------------------------------

      - T → Type
      - E → Element (colecciones)
      - K,V → Key, Value (mapas)
      - R → Return type
      - ? → wildcard (tipo desconocido)
*/
public class GenericsCheatSheet {}
