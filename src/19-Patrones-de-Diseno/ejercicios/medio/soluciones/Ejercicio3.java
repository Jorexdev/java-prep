import java.util.ArrayList;
import java.util.List;

public class Ejercicio3 {

    interface Componente {
        String nombre();
        long tamaño();
        void mostrar(String indent);
    }

    static class Archivo implements Componente {
        private final String nombre;
        private final long tamaño;
        Archivo(String nombre, long tamaño) { this.nombre = nombre; this.tamaño = tamaño; }

        @Override public String nombre()        { return nombre; }
        @Override public long tamaño()          { return tamaño; }
        @Override public void mostrar(String i) { System.out.printf("%s[archivo] %s (%d KB)%n", i, nombre, tamaño); }
    }

    static class Directorio implements Componente {
        private final String nombre;
        private final List<Componente> hijos = new ArrayList<>();
        Directorio(String nombre) { this.nombre = nombre; }

        void agregar(Componente c)              { hijos.add(c); }
        @Override public String nombre()        { return nombre; }
        @Override public long tamaño()          { return hijos.stream().mapToLong(Componente::tamaño).sum(); }
        @Override public void mostrar(String i) {
            System.out.printf("%s[dir]     %s/ (%d KB)%n", i, nombre, tamaño());
            hijos.forEach(h -> h.mostrar(i + "  "));
        }
    }

    public static void main(String[] args) {
        Directorio raiz = new Directorio("proyecto");
        raiz.agregar(new Archivo("README.md", 2));

        Directorio src = new Directorio("src");
        src.agregar(new Archivo("Main.java", 5));
        src.agregar(new Archivo("Utils.java", 3));

        Directorio test = new Directorio("test");
        test.agregar(new Archivo("MainTest.java", 4));
        src.agregar(test);
        raiz.agregar(src);

        raiz.mostrar("");
        System.out.println("Total: " + raiz.tamaño() + " KB");
    }
}
