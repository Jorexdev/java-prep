public class Ejercicio1 {
    public static void main(String[] args) throws Exception {
        for (int t = 1; t <= 3; t++) {
            final int id = t;
            new Thread(() -> {
                for (int i = 1; i <= 3; i++)
                    System.out.println("Hilo-" + id + ": " + i);
            }, "Hilo-" + t).start();
        }
    }
}
