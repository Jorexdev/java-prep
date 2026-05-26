public class ExpAbstractFactory {

    public static void main(String[] args) {

        // La aplicación solo conoce UIFactory — no sabe qué tema se está usando
        runApp(new DarkThemeFactory());
        System.out.println();
        runApp(new LightThemeFactory());
    }

    static void runApp(UIFactory factory) {
        System.out.println("=== " + factory.themeName() + " ===");
        Button  btn    = factory.createButton();
        Dialog  dialog = factory.createDialog();

        btn.render();
        btn.onClick();
        dialog.show("¿Confirmar acción?");
        dialog.close();
    }

    // ── Fábrica abstracta ───────────────────────────────────────────────────

    abstract static class UIFactory {
        abstract Button createButton();
        abstract Dialog createDialog();
        abstract String themeName();
    }

    // ── Productos abstractos ────────────────────────────────────────────────

    abstract static class Button {
        abstract void render();
        abstract void onClick();
    }

    abstract static class Dialog {
        abstract void show(String message);
        void close() { System.out.println("  [dialog] cerrado"); }   // comportamiento base compartido
    }

    // ── Dark theme ──────────────────────────────────────────────────────────

    static class DarkThemeFactory extends UIFactory {
        @Override public Button createButton() { return new DarkButton(); }
        @Override public Dialog createDialog()  { return new DarkDialog(); }
        @Override public String themeName()     { return "Dark Theme"; }
    }

    static class DarkButton extends Button {
        @Override public void render()   { System.out.println("  [button] ████ BOTÓN OSCURO ████"); }
        @Override public void onClick()  { System.out.println("  [button] click oscuro (ripple #1a1a1a)"); }
    }

    static class DarkDialog extends Dialog {
        @Override public void show(String msg) {
            System.out.println("  [dialog] ┌─────────────────────────┐");
            System.out.println("  [dialog] │ " + msg);
            System.out.println("  [dialog] └─────────────────────────┘");
        }
    }

    // ── Light theme ─────────────────────────────────────────────────────────

    static class LightThemeFactory extends UIFactory {
        @Override public Button createButton() { return new LightButton(); }
        @Override public Dialog createDialog()  { return new LightDialog(); }
        @Override public String themeName()     { return "Light Theme"; }
    }

    static class LightButton extends Button {
        @Override public void render()   { System.out.println("  [button] [ BOTÓN CLARO ]"); }
        @Override public void onClick()  { System.out.println("  [button] click claro (ripple #f5f5f5)"); }
    }

    static class LightDialog extends Dialog {
        @Override public void show(String msg) {
            System.out.println("  [dialog] +-------------------------+");
            System.out.println("  [dialog] | " + msg);
            System.out.println("  [dialog] +-------------------------+");
        }
    }
}
