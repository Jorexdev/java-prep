package base.genericos.ejemplos;

import java.util.*;


// Fijate que en la cabecera de clase que queremos que tenga un atributo generico lo ponemos aqui,
// podemos poner la cantidad de genericos que queramos separados por ,
class Caja<T> {

    // Este atributo sera generico
    private T contenido;

    // El setter recibe un valor de tipo generico por param
    // Fijate que al usar la misma letra (T) estaremos haciendo
    // referencia al mismo tipo de dato que se ponga en la posicion
    // de la cabecera de la clase
    public void set(T valor) {
        this.contenido = valor;
    }

    // Devolvemos un valor de tipo generico
    public T get() {
        return contenido;
    }

    @Override
    public String toString() {
        return "Caja{" + contenido + "}";
    }
}

public class ExpGenericsBasics {

    // Métod0 genérico: imprime cualquier lista List<T>
    // fijate que para que un metodo acepte un param generico
    // debemos poner <> con la letra correspondiente en la
    // cabecera para que el compilador sepa que esperarse en el param
    // si lo quitamos, daria error
    public static <T> void imprimirLista(List<T> lista) {
        for (T elem : lista) {
            System.out.println("Elemento: " + elem);
        }
    }

    // Métod0 genérico acotado (upper bound): solo subtipos de Number
    // IMPORTANTE: NO confundir lo que pongamos entre <> con el
    // tipo de dato que devuelve el metodo, son modificadores diferentes
    public static <N extends Number> double sumar(List<N> numeros) {
        double acc = 0d;
        for (N n : numeros) acc += n.doubleValue();
        return acc;
    }

    public static void main(String[] args) {
        // Clase genérica
        // Es exactamente igual como cuando hacemos ArrayLists
        // Decimos que vamos a almacenar en la clase mediante <>
        Caja<String> c1 = new Caja<>();
        c1.set("Hola genéricos");
        System.out.println(c1.get());

        Caja<Integer> c2 = new Caja<>();
        c2.set(42);
        System.out.println(c2);

        // Métodos genéricos
        List<String> textos = new ArrayList<>(List.of("Java", "Spring", "Hibernate"));
        imprimirLista(textos); // acepta cualquier List<T>

        List<Integer> ints = List.of(1, 2, 3);
        List<Double> dbls = List.of(1.5, 2.5);
        System.out.println("sumar(ints) = " + sumar(ints));   // 6.0
        System.out.println("sumar(dbls) = " + sumar(dbls));   // 4.0
    }
}
