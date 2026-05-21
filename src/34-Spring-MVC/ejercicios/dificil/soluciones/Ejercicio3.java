import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Ejercicio3 {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NotBlank {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Min {
        int value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Max {
        int value();
    }

    static class CreateProductRequest {
        @NotBlank
        String nombre;

        @Min(1) @Max(1000)
        int precio;

        CreateProductRequest(String nombre, int precio) {
            this.nombre = nombre;
            this.precio = precio;
        }
    }

    static class BeanValidator {

        static List<String> validate(Object bean) throws IllegalAccessException {
            List<String> errors = new ArrayList<>();
            Class<?> clazz = bean.getClass();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(bean);

                if (field.isAnnotationPresent(NotBlank.class)) {
                    if (value == null || value.toString().isBlank()) {
                        errors.add("Campo '" + field.getName() + "' no puede estar vacío");
                    }
                }

                if (field.isAnnotationPresent(Min.class)) {
                    int min = field.getAnnotation(Min.class).value();
                    int intVal = (int) value;
                    if (intVal < min) {
                        errors.add("Campo '" + field.getName() + "' debe ser >= " + min + " (era " + intVal + ")");
                    }
                }

                if (field.isAnnotationPresent(Max.class)) {
                    int max = field.getAnnotation(Max.class).value();
                    int intVal = (int) value;
                    if (intVal > max) {
                        errors.add("Campo '" + field.getName() + "' debe ser <= " + max + " (era " + intVal + ")");
                    }
                }
            }

            return errors;
        }
    }

    public static void main(String[] args) throws IllegalAccessException {
        CreateProductRequest valido = new CreateProductRequest("Teclado", 49);
        CreateProductRequest sinNombre = new CreateProductRequest("  ", 100);
        CreateProductRequest precioInvalido = new CreateProductRequest("Ratón", 1500);

        for (CreateProductRequest req : List.of(valido, sinNombre, precioInvalido)) {
            List<String> errors = BeanValidator.validate(req);
            System.out.println("nombre='" + req.nombre + "', precio=" + req.precio);
            if (errors.isEmpty()) {
                System.out.println("  Válido");
            } else {
                errors.forEach(e -> System.out.println("  Error: " + e));
            }
        }
    }
}
