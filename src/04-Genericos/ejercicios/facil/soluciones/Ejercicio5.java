public class Ejercicio5 {

    // Bounded type parameter: T debe implementar Comparable<T>
    // Esto garantiza que podemos llamar compareTo en tiempo de compilación
    static <T extends Comparable<T>> T maximo(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static void main(String[] args) {
        System.out.println(maximo(10, 25));           // 25
        System.out.println(maximo(3.14, 2.71));       // 3.14
        System.out.println(maximo("banana", "mango")); // mango (lexicográfico)
        System.out.println(maximo('z', 'a'));          // z
    }
}
