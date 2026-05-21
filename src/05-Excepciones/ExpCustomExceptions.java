// ================== EXCEPCIONES PERSONALIZADAS ==================

// Checked Exception → el compilador obliga a declararla o capturarla
class CredencialesInvalidasException extends Exception {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}

// Unchecked Exception → hereda de RuntimeException, no obliga a declararla
class OperacionInvalidaException extends RuntimeException {
    public OperacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}

public class ExpCustomExceptions {

    // Métod0 que lanza una checked exception
    public static void autenticar(String user, String pass) throws CredencialesInvalidasException {
        if (!"admin".equals(user) || !"password".equals(pass)) {
            throw new CredencialesInvalidasException("Usuario o contraseña inválidos");
        }
    }

    // Métod0 que lanza una unchecked exception
    public static int dividir(int a, int b) {
        if (b == 0) {
            throw new OperacionInvalidaException("No se puede dividir entre 0");
        }
        return a / b;
    }

    public static void main(String[] args) {

        try {
            autenticar("admin", "1234");
        } catch (CredencialesInvalidasException e) {
            System.out.println("Excepción personalizada (checked): " + e.getMessage());
        }

        try {
            int resultado = dividir(10, 0);
            System.out.println("Resultado = " + resultado);
        } catch (OperacionInvalidaException e) {
            System.out.println("Excepción personalizada (unchecked): " + e.getMessage());
        }
    }
}
