package patronesdiseno.adaptador;

public class AdapterDemo {

    // Target: la interfaz que el cliente conoce y espera usar.
    interface MediaPlayer {
        void play(String filename);
    }

    // Adaptee: clase existente con una interfaz distinta.
    // No podemos o no queremos modificarla.
    static class AdvancedPlayer {
        public void playMp4(String file) {
            System.out.println("Reproduciendo MP4: " + file);
        }
    }

    // Adapter: implementa la interfaz del cliente y delega al Adaptee.
    // Traduce play() a playMp4() internamente.
    static class Mp4Adapter implements MediaPlayer {
        private final AdvancedPlayer advanced = new AdvancedPlayer();

        public void play(String filename) {
            advanced.playMp4(filename); // traducción de interfaz
        }
    }

    public static void main(String[] args) {
        // El cliente solo conoce MediaPlayer, no sabe que por debajo hay un AdvancedPlayer
        MediaPlayer player = new Mp4Adapter();
        player.play("video.mp4");
    }
}
