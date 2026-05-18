import java.util.concurrent.Phaser;

public class Ejercicio3 {
    public static void main(String[] args) throws Exception {
        Phaser phaser = new Phaser(1);

        String[] trabajadores = {"W1", "W2", "W3", "W4"};
        for (String nombre : trabajadores) {
            phaser.register();
            new Thread(() -> {
                try {
                    System.out.println(nombre + ": CARGA");          Thread.sleep((long)(Math.random()*100+50));
                    phaser.arriveAndAwaitAdvance();
                    System.out.println(nombre + ": TRANSFORMACION"); Thread.sleep((long)(Math.random()*100+50));
                    phaser.arriveAndAwaitAdvance();
                    System.out.println(nombre + ": ALMACENAMIENTO"); Thread.sleep((long)(Math.random()*100+50));
                    phaser.arriveAndDeregister();
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, nombre).start();
        }

        phaser.arriveAndAwaitAdvance(); System.out.println("=== Fase CARGA completada ===");
        phaser.arriveAndAwaitAdvance(); System.out.println("=== Fase TRANSFORMACION completada ===");
        phaser.arriveAndDeregister();
        System.out.println("=== Pipeline completado ===");
    }
}
