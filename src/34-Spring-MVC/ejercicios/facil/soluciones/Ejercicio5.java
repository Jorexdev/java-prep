import java.util.ArrayList;
import java.util.List;

public class Ejercicio5 {

    static class RegistroForm {
        String email;
        String password;

        RegistroForm(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    static class ValidationResult {
        boolean valid;
        List<String> errores;

        ValidationResult() {
            this.errores = new ArrayList<>();
        }

        void addError(String error) {
            errores.add(error);
        }

        void resolve() {
            this.valid = errores.isEmpty();
        }
    }

    static ValidationResult validar(RegistroForm f) {
        ValidationResult result = new ValidationResult();

        if (f.email == null || !f.email.contains("@")) {
            result.addError("El email debe contener '@'");
        }
        if (f.password == null || f.password.length() < 8) {
            result.addError("La contraseña debe tener al menos 8 caracteres");
        }

        result.resolve();
        return result;
    }

    public static void main(String[] args) {
        RegistroForm valido = new RegistroForm("ana@ejemplo.com", "segura123");
        RegistroForm sinArroba = new RegistroForm("anaeejemplo.com", "segura123");
        RegistroForm passwordCorta = new RegistroForm("luis@ejemplo.com", "abc");

        for (RegistroForm form : List.of(valido, sinArroba, passwordCorta)) {
            ValidationResult r = validar(form);
            System.out.println("Email: " + form.email + " | Password: " + form.password);
            System.out.println("  Válido: " + r.valid);
            if (!r.errores.isEmpty()) {
                r.errores.forEach(e -> System.out.println("  Error: " + e));
            }
        }
    }
}
