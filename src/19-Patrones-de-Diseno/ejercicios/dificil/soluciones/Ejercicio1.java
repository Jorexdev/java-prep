public class Ejercicio1 {

    interface Boton     { void render(); }
    interface Checkbox  { void check(boolean val); }
    interface TextField { void input(String texto); }

    static class BotonHTML     implements Boton     { @Override public void render()         { System.out.println("<button>Enviar</button>"); } }
    static class CheckboxHTML  implements Checkbox  { @Override public void check(boolean v) { System.out.println("<input type='checkbox'" + (v ? " checked" : "") + "/>"); } }
    static class TextFieldHTML implements TextField { @Override public void input(String t)  { System.out.println("<input type='text' value='" + t + "'/>"); } }

    static class BotonDesktop     implements Boton     { @Override public void render()         { System.out.println("[ Enviar ]"); } }
    static class CheckboxDesktop  implements Checkbox  { @Override public void check(boolean v) { System.out.println("[" + (v ? "X" : " ") + "] recordar sesion"); } }
    static class TextFieldDesktop implements TextField { @Override public void input(String t)  { System.out.println("|" + t + "               |"); } }

    interface FabricaUI {
        Boton crearBoton();
        Checkbox crearCheckbox();
        TextField crearTextField();
    }

    static class FabricaWeb     implements FabricaUI {
        @Override public Boton crearBoton()         { return new BotonHTML(); }
        @Override public Checkbox crearCheckbox()   { return new CheckboxHTML(); }
        @Override public TextField crearTextField() { return new TextFieldHTML(); }
    }

    static class FabricaDesktop implements FabricaUI {
        @Override public Boton crearBoton()         { return new BotonDesktop(); }
        @Override public Checkbox crearCheckbox()   { return new CheckboxDesktop(); }
        @Override public TextField crearTextField() { return new TextFieldDesktop(); }
    }

    static class App {
        private final FabricaUI fabrica;
        App(FabricaUI fabrica) { this.fabrica = fabrica; }

        void mostrarLogin() {
            fabrica.crearTextField().input("usuario");
            fabrica.crearTextField().input("contraseña");
            fabrica.crearCheckbox().check(false);
            fabrica.crearBoton().render();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Web ===");
        new App(new FabricaWeb()).mostrarLogin();

        System.out.println("\n=== Desktop ===");
        new App(new FabricaDesktop()).mostrarLogin();
    }
}
