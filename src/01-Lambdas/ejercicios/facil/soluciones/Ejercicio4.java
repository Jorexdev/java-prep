public class Ejercicio4 {

    public static void main(String[] args) {

        Runnable saludo = () -> System.out.println("Hola desde un Runnable");

        saludo.run();
    }
}
