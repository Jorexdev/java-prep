package base.opcional;

/*
    OPTIONAL — Introducción

    ¿Qué es Optional?
    Contenedor de Java 8 que representa un valor que puede existir o no.
    Alternativa segura a devolver/recibir null y a los NullPointerException.

        Optional<T> puede contener:
          - un valor de tipo T
          - estar vacío: Optional.empty()

    ¿Por qué existe?
    Sin Optional: null provoca NullPointerException silenciosos.
        User user = findUser();
        user.getName(); // NPE si user es null

    Con Optional: la ausencia queda explícita.
        Optional<User> user = findUser();
        user.map(User::getName).orElse("Desconocido");

    ¿Cuándo usar Optional?
      Sí: como valor de retorno cuando el resultado puede no existir.
      No: en atributos de entidades (JPA/JSON rompen serialización).
      No: en parámetros de métodos (mejor sobrecargar o validar antes).
      No: en colecciones (ya pueden estar vacías).

    Métodos principales:
      Optional.empty()             → Optional vacío
      Optional.of(valor)           → con valor (lanza NPE si null)
      Optional.ofNullable(valor)   → con valor o vacío si es null
      isPresent() / isEmpty()      → comprobar estado (evitar en código funcional)
      ifPresent(Consumer)          → ejecuta si hay valor
      orElse(valor)                → valor por defecto
      orElseGet(Supplier)          → valor por defecto lazy (se evalúa solo si vacío)
      orElseThrow()                → lanza NoSuchElementException si vacío
      map(Function)                → transforma el valor interno
      flatMap(Function)            → trabaja con Optional anidados
      filter(Predicate)            → vacía el Optional si el valor no cumple la condición

    Ejemplo conceptual:
        // antes
        public User findUser(String id) { ... return null; }

        // con Optional
        public Optional<User> findUser(String id) { ... return Optional.of(usuario); }

        // consumo
        User u = findUser("1").orElseThrow(() -> new RuntimeException("No encontrado"));
*/
public class Intro {}
