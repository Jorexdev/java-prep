import java.io.IOException;

public class Ejercicio6 {

    // Metodo checked: obliga al caller a manejar IOException
    static String leerArchivo(String ruta) throws IOException {
        if (ruta == null || !ruta.endsWith(".txt")) {
            throw new IOException("Ruta invalida: " + ruta);
        }
        return "contenido de " + ruta;
    }

    // Wrap: convierte checked -> unchecked, el caller ya no necesita try-catch
    static String leerArchivoUnchecked(String ruta) {
        try {
            return leerArchivo(ruta);
        } catch (IOException e) {
            throw new RuntimeException("Fallo al leer archivo: " + ruta, e);
        }
    }

    public static void main(String[] args) {
        // Llamada unchecked: no requiere try-catch obligatorio en el compilador
        // (aunque aqui lo ponemos para mostrar el resultado)

        // Caso valido
        String contenido = leerArchivoUnchecked("datos.txt");
        System.out.println("Leido: " + contenido);

        // Caso invalido: captura RuntimeException
        try {
            leerArchivoUnchecked("datos.xml");
        } catch (RuntimeException e) {
            System.out.println("RuntimeException: " + e.getMessage());
            System.out.println("Causa original (IOException): " + e.getCause().getMessage());
        }
    }
}
