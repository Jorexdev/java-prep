package base.genericos;


public class Intro {

    /*
       Los Genéricos son tipos parametrizados: permiten escribir clases, interfaces
       y métodos que funcionan con distintos tipos sin perder seguridad de tipos.

       En lugar de duplicar código para cada tipo (String, Integer, etc.), usamos
       un parámetro de tipo (por convención, letras en mayúscula: T, E, K, V, R...).

      - T → Type
      - E → Element (colecciones)
      - K,V → Key, Value (mapas)
      - R → Return type
      - ? → wildcard (tipo desconocido)

       Se declaran con el operador diamante <>:
         - En clases/interficies: class Caja<T> { ... }
         - En métodos: <T> T nombreMetodo(List<T> lista) { ... }

       También existen comodines para flexibilizar APIs:
         - <? extends T>  → upper bound (subtipos de T)
         - <? super T>    → lower bound (supertipos de T)

         Regla PECS:
         Producer Extends → si produces datos, el compilador te asegura que todos son al menos T.

         Consumer Super → si consumes datos, el compilador te asegura que puedes meter T, pero al leer lo único seguro es Object.

         Vale, esto es dificil de entender a la primera, sirve para las collections, imagina que tienes una lista de Integers

         List<? super Integer> lista = new ArrayList<>();

        Y estas indicando con la ? que no sabes que tipo de dato es (un Generico) y con super que sabes que ese generico
        es un supertipo de Integer (IMPORTANTE; SUPERTIPO NO NECESARIAMENTE CLASE PADRE)

        Siguiendo la regla PECS, podemos ver que esta lista consumira valores, por lo podemos meter valores Integer sin problema,
        pero para la lectura solo sabemos al 100% que es un objeto ya que es el ultimo padre de toda la jerarquia de Integer

        List<? super Integer> lista = new ArrayList<Number>();

        lista.add(42);   // puedo meter enteros sin problema
        Object obj = lista.get(0); // compila, solo puede leer Object
        Number num = lista.get(0); // error de compilación, porque no sabemos 100% si es un Number o incluso un Integer
        Integer num2 = lista.get(0); // error de compilación

        Pero si usamos extends:

        List<? extends Number> lista = new ArrayList<Integer>();

        Number n = lista.get(0); // permitido
        numeros.add(10);         // error, no sé si es Integer, Double o Long
    */

    /* ---------------------------- COMENTARIO ----------------------------

            Aqui te dejo cuando me di cuenta de lo similar que son los wildcards con los casteos:

            Esto es por la misma razon de los errores en los casteos, si quisiera pasar de un tipo de
            dato mas pequeño a uno mas grande (como en el extends), estoy seguro de que el valor cabe
            ya que paso de algo mas pequeño como puede ser Integer a Number algo mas grande mientras
            que en super, al querer pasar de algo mas grande a algo mas pequeño al no saber si puede
            entrar ese valor al ser demasiado grande no lo permite
    */

    /* ---------------------------- EJEMPLO CONOCIDO ----------------------------

       Un ejemplo clásico de clase genérica del JDK es ArrayList:

         // Correcto (tipo declarado a la izquierda; inferencia diamond a la derecha desde Java 7)
         List<String> lista = new ArrayList<>();

       En esta lista, los elementos serán de tipo String porque lo especificamos
       al instanciarla. Si miras la declaración de ArrayList:

         public class ArrayList<E> extends AbstractList<E>
             implements List<E>, RandomAccess, Cloneable, java.io.Serializable

       Fíjate en el parámetro de tipo <E>. Los métodos usan esa E para ser genéricos:
         public E get(int index)

       Es decir, el mismo parámetro de tipo E “fluye” por toda la API y el compilador
       verifica que tod0 encaje (type-safety).
    -------------------------------------------------------------------------- */


}
