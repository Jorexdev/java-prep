import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// Simula @Valid + BindingResult de Spring MVC.
// Las restricciones (@NotBlank, @Size, @Email, @Min) son anotaciones reales que
// el Validator lee via reflection. El controller rechaza con 400 si hay errores.

// ── Anotaciones de validación ─────────────────────────────────────────────────

// Equivale a jakarta.validation.constraints.NotBlank
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface NotBlank {
    String message() default "no puede estar vacío";
}

// Equivale a jakarta.validation.constraints.Size
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Size {
    int min() default 0;
    int max() default Integer.MAX_VALUE;
    String message() default "tamaño fuera de rango";
}

// Equivale a jakarta.validation.constraints.Email
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Email {
    String message() default "formato de email inválido";
}

// Equivale a jakarta.validation.constraints.Min
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Min {
    long value();
    String message() default "valor demasiado pequeño";
}

// ── DTO de entrada ────────────────────────────────────────────────────────────

// Equivale al @RequestBody que Spring deserializa antes de @Valid
class RegistroUsuarioDto {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    String nombre;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email no tiene formato válido")
    String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password;

    @Min(value = 18, message = "La edad mínima es 18")
    int edad;

    RegistroUsuarioDto(String nombre, String email, String password, int edad) {
        this.nombre   = nombre;
        this.email    = email;
        this.password = password;
        this.edad     = edad;
    }

    @Override
    public String toString() {
        return "RegistroDto{nombre='" + nombre + "', email='" + email
            + "', edad=" + edad + "}";
    }
}

// ── FieldError (equivale a org.springframework.validation.FieldError) ─────────

class FieldError {
    final String field;
    final Object rejectedValue;
    final String defaultMessage;

    FieldError(String field, Object rejectedValue, String defaultMessage) {
        this.field           = field;
        this.rejectedValue   = rejectedValue;
        this.defaultMessage  = defaultMessage;
    }

    @Override
    public String toString() {
        return "  [" + field + "] valor='" + rejectedValue + "' → " + defaultMessage;
    }
}

// ── Validator ─────────────────────────────────────────────────────────────────

// Equivale a LocalValidatorFactoryBean (implementación JSR-380 de Hibernate Validator)
class BeanValidator {

    // Recorre los campos del objeto buscando anotaciones de restricción vía reflection
    List<FieldError> validate(Object obj) {
        List<FieldError> errores = new ArrayList<>();

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                continue;
            }

            String fieldName = field.getName();

            // @NotBlank — solo aplica a String
            if (field.isAnnotationPresent(NotBlank.class) && value instanceof String str) {
                if (str == null || str.isBlank()) {
                    errores.add(new FieldError(fieldName, value,
                        field.getAnnotation(NotBlank.class).message()));
                }
            }

            // @Size — solo aplica a String
            if (field.isAnnotationPresent(Size.class) && value instanceof String str) {
                Size sizeAnn = field.getAnnotation(Size.class);
                int len = str == null ? 0 : str.length();
                if (len < sizeAnn.min() || len > sizeAnn.max()) {
                    errores.add(new FieldError(fieldName, value, sizeAnn.message()));
                }
            }

            // @Email — comprobación básica de formato
            if (field.isAnnotationPresent(Email.class) && value instanceof String str) {
                if (str != null && !str.isBlank() && !str.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                    errores.add(new FieldError(fieldName, value,
                        field.getAnnotation(Email.class).message()));
                }
            }

            // @Min — aplica a int/long
            if (field.isAnnotationPresent(Min.class)) {
                long val = ((Number) value).longValue();
                Min minAnn = field.getAnnotation(Min.class);
                if (val < minAnn.value()) {
                    errores.add(new FieldError(fieldName, value,
                        minAnn.message() + " (mínimo=" + minAnn.value() + ", recibido=" + val + ")"));
                }
            }
        }

        return errores;
    }
}

// ── Controller ────────────────────────────────────────────────────────────────

// @RestController
// @RequestMapping("/api/usuarios")
class UsuarioController {

    private final BeanValidator validator = new BeanValidator();

    // @PostMapping("/registro")
    // public ResponseEntity<?> registrar(@Valid @RequestBody RegistroUsuarioDto dto,
    //                                     BindingResult bindingResult)
    void registrar(RegistroUsuarioDto dto) {
        System.out.println("  [Controller] POST /api/usuarios/registro ← " + dto);

        List<FieldError> errores = validator.validate(dto);   // equivale a @Valid

        if (!errores.isEmpty()) {
            // En Spring: if (bindingResult.hasErrors()) return ResponseEntity.badRequest()...
            System.out.println("  → 400 Bad Request — errores de validación:");
            errores.forEach(System.out::println);
            return;
        }

        System.out.println("  → 201 Created — usuario registrado: " + dto.nombre);
    }
}

// ── Punto de entrada ──────────────────────────────────────────────────────────

public class ExpValidationMVC {
    public static void main(String[] args) {

        UsuarioController ctrl = new UsuarioController();

        System.out.println("=== Simulación @Valid + BindingResult ===\n");

        // ─── Caso 1: petición válida → 201 ───────────────────────────────────
        System.out.println("--- Caso 1: datos válidos ---");
        ctrl.registrar(new RegistroUsuarioDto("Jorge", "jorge@example.com", "segura123", 25));

        System.out.println();

        // ─── Caso 2: múltiples errores → 400 ─────────────────────────────────
        System.out.println("--- Caso 2: nombre vacío + email inválido + contraseña corta + menor de edad ---");
        ctrl.registrar(new RegistroUsuarioDto("", "no-es-email", "1234", 15));

        System.out.println();

        // ─── Caso 3: solo email mal formado → 400 ────────────────────────────
        System.out.println("--- Caso 3: todo correcto excepto formato de email ---");
        ctrl.registrar(new RegistroUsuarioDto("Ana", "ana-sin-arroba.com", "password99", 30));

        System.out.println();

        // ─── Caso 4: nombre demasiado largo → 400 ────────────────────────────
        System.out.println("--- Caso 4: nombre supera @Size(max=50) ---");
        ctrl.registrar(new RegistroUsuarioDto(
            "NombreExageradamente LargoQueNoDeberiaPermitirse PorLaAnotación",
            "valido@email.com", "mipassword", 22));
    }
}
