public class Ejercicio2 {

    static abstract class Boton {
        abstract void render();
        abstract void onClick();
    }

    static class BotonWindows extends Boton {
        @Override public void render()   { System.out.println("[Windows Button]"); }
        @Override public void onClick()  { System.out.println("Click! Windows style"); }
    }

    static class BotonMac extends Boton {
        @Override public void render()   { System.out.println("( Mac Button )"); }
        @Override public void onClick()  { System.out.println("Click! Mac style"); }
    }

    static abstract class FabricaDialog {
        abstract Boton crearBoton();

        void render() {
            Boton boton = crearBoton();
            System.out.println("Renderizando diálogo:");
            boton.render();
            boton.onClick();
        }
    }

    static class DialogWindows extends FabricaDialog {
        @Override Boton crearBoton() { return new BotonWindows(); }
    }

    static class DialogMac extends FabricaDialog {
        @Override Boton crearBoton() { return new BotonMac(); }
    }

    public static void main(String[] args) {
        for (FabricaDialog dialogo : new FabricaDialog[]{new DialogWindows(), new DialogMac()}) {
            dialogo.render();
            System.out.println();
        }
    }
}
