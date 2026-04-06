package patronesdiseno.adaptador;

/*
    PATRÓN ADAPTER - Estructural

    ¿Qué es?
    Permite que dos interfaces incompatibles trabajen juntas actuando como traductor.
    Envuelve una clase existente (Adaptee) y expone la interfaz que el cliente espera.

    ¿Para qué sirve?
    Para reutilizar código existente cuya interfaz no encaja con lo que necesitas,
    sin modificar ni el cliente ni la clase original.

    ¿Cuándo usarlo?
    - Cuando integras una librería de terceros que no puedes modificar.
    - Cuando tienes clases legacy con interfaces distintas a las del sistema actual.
    - Cuando quieres aislar el código externo detrás de una interfaz propia.

    Preguntas típicas de entrevista:
    - ¿En qué se diferencia Adapter de Decorator? (Adapter cambia la interfaz, Decorator la amplía)
    - ¿Qué diferencia hay entre Adapter de clase y Adapter de objeto?
    - ¿Dónde usa Java internamente el patrón Adapter? (Arrays.asList, InputStreamReader)
*/
public class AdapterDemo {

    /*
        Target: la interfaz que el cliente conoce y espera usar.
    */
    interface MediaPlayer {
        void play(String filename);
    }

    /*
        Adaptee: clase existente con una interfaz distinta.
        No podemos o no queremos modificarla.
    */
    static class AdvancedPlayer {
        public void playMp4(String file) {
            System.out.println("Reproduciendo MP4: " + file);
        }
    }

    /*
        Adapter: implementa la interfaz del cliente y delega al Adaptee.
        Traduce play() a playMp4() internamente.
    */
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
