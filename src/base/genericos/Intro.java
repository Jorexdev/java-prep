package base.genericos;

/*
    GENÉRICOS — PECS y wildcards

    Los Genéricos son tipos parametrizados: permiten escribir clases, interfaces
    y métodos que funcionan con distintos tipos sin perder seguridad de tipos.

    En lugar de duplicar código para cada tipo usamos un parámetro de tipo:
      T → Type,  E → Element (colecciones),  K,V → Key/Value (mapas),  R → Return

    Se declaran con el operador diamante <>:
      - En clases/interfaces: class Caja<T> { ... }
      - En métodos:           <T> T nombreMetodo(List<T> lista) { ... }

    COMODINES
      <? extends T>  → upper bound: subtipos de T. Lectura como T, sin escritura.
      <? super T>    → lower bound: supertipos de T. Escritura de T, lectura como Object.

    REGLA PECS
      Producer Extends → la colección produce datos: usa extends.
      Consumer Super   → la colección consume datos: usa super.

    Ejemplo con super (Consumer):
        List<? super Integer> lista = new ArrayList<Number>();
        lista.add(42);            // OK: meto enteros sin problema
        Object obj = lista.get(0); // OK: solo garantiza Object al leer
        Number num = lista.get(0); // error: no sabemos si es Number o más

    Ejemplo con extends (Producer):
        List<? extends Number> lista = new ArrayList<Integer>();
        Number n = lista.get(0);  // OK: cualquier Number
        lista.add(10);            // error: no sabemos si es Integer, Double o Long

    EJEMPLO CLÁSICO DEL JDK
    ArrayList es una clase genérica con parámetro E:

        public class ArrayList<E> extends AbstractList<E>
            implements List<E>, RandomAccess, Cloneable, java.io.Serializable

    Al instanciarla, E se fija y fluye por toda la API garantizando type-safety:
        List<String> lista = new ArrayList<>(); // E = String
        public E get(int index)                 // devuelve String, no Object
*/
public class Intro {}
